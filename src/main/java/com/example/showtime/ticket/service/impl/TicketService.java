package com.example.showtime.ticket.service.impl;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.common.pdf.PdfGenerator;
import com.example.showtime.event.model.entity.Event;
import com.example.showtime.event.services.IEventService;
import com.example.showtime.ticket.model.entity.Category;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.model.request.BuyTicketRequest;
import com.example.showtime.ticket.model.request.TicketOwnerInformationRequest;
import com.example.showtime.ticket.model.response.BuyTicketResponse;
import com.example.showtime.ticket.repository.TicketRepository;
import com.example.showtime.ticket.service.ICategoryService;
import com.example.showtime.ticket.service.ITicketService;
import com.example.showtime.transaction.enums.TransactionStatusEnum;
import com.example.showtime.transaction.model.entity.TransactionItem;
import com.example.showtime.transaction.service.ITransactionService;
import com.example.showtime.transaction.ssl.SSLTransactionInitiator;
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TicketService implements ITicketService {

    private final TicketRepository ticketRepository;
    private final IUserService userService;
    private final IEventService eventService;
    private final ICategoryService categoryService;
    private final ITransactionService transactionService;
    private final SSLTransactionInitiator sslTransactionInitiator;
    private final PdfGenerator pdfGenerator;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BuyTicketResponse createTicket(BuyTicketRequest buyTicketRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdByUserEmail = authentication.getName();

        UserAccount createdBy = userService.getUserByEmail(createdByUserEmail);

        try {
            validateRequest(buyTicketRequest);

            TransactionItem transactionItem = prepareTicketModel(buyTicketRequest, createdBy);
            String sslCommerzPaymentUrl = sslTransactionInitiator.initiateSSLTransaction(transactionItem, createdBy);

            return BuyTicketResponse.builder()
                    .transactionRefNo(transactionItem.getTransactionRefNo())
                    .sslPaymentUrl(sslCommerzPaymentUrl)
                    .build();
        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    private TransactionItem prepareTicketModel(BuyTicketRequest buyTicketRequest, UserAccount createdBy) {
        List<Ticket> newTickets;

        Event selectedEvent = eventService.getEventById(buyTicketRequest.getEventId());
        List<TicketOwnerInformationRequest> ticketOwnerInformation = buyTicketRequest.getTicketOwnerInformation();
        String refId = transactionService.generateUniqueIdForTransaction(selectedEvent.getEventId().substring(0, 2));

        newTickets = IntStream.range(0, Math.toIntExact(ticketOwnerInformation.size()))
                .parallel()
                .mapToObj(i -> {
                    Ticket ticket = new Ticket();
                    ticket.setTicketQrCode(generateQRCode(selectedEvent));
                    ticket.setEventName(selectedEvent.getEventName());
                    ticket.setUsed(false);
                    ticket.setActive(true);
                    ticket.setTicketTransactionId(refId);
                    ticket.setTicketCreatedDate(Calendar.getInstance().getTime());
                    ticket.setEventId(selectedEvent.getEventId());
                    ticket.setValidityDate(selectedEvent.getEventEndDate());
                    ticket.setTicketCategory(buyTicketRequest.getTicketCategory());
                    ticket.setTicketOwnerName(ticketOwnerInformation.get(i).getTicketOwnerName());
                    ticket.setTicketOwnerEmail(ticketOwnerInformation.get(i).getTicketOwnerEmail());
                    ticket.setTicketOwnerNumber(ticketOwnerInformation.get(i).getTicketOwnerNumber());
                    ticket.setTicketCreatedBy(createdBy.getEmail());
                    ticket.setTicketPrice(categoryService.getTicketPrice(buyTicketRequest.getTicketCategory(), selectedEvent.getEventId()));
                    ticket.setTicketFilePath(generateTicketPdf(createdBy, ticket));
                    eventService.updateAvailableTickets(selectedEvent.getEventId());
                    categoryService.updateAvailableTickets(buyTicketRequest.getTicketCategory(), selectedEvent.getEventId());

                    return ticket;
                })
                .collect(Collectors.toList());

        TransactionItem transactionItem = new TransactionItem();
        transactionItem.setTransactionRefNo(refId);
        transactionItem.setTotalAmount(newTickets.stream().mapToDouble(Ticket::getTicketPrice).sum());
        transactionItem.setEventId(selectedEvent.getEventId());
        transactionItem.setTransactionDate(Calendar.getInstance().getTime());
        transactionItem.setTransactionStatus(TransactionStatusEnum.INITIATED.getValue());
        transactionItem.setUserEmail(createdBy.getEmail());

        transactionService.saveTransaction(transactionItem);

        ticketRepository.saveAll(newTickets);
        return transactionItem;
    }

    @Async
    public String generateTicketPdf(UserAccount createdBy, Ticket ticket) {
        return pdfGenerator.generateTicketPdf(createdBy, ticket);
    }

    private void validateRequest(BuyTicketRequest buyTicketRequest) {
        if (Objects.isNull(buyTicketRequest) ||
                StringUtils.isEmpty(buyTicketRequest.getEventId()) ||
                Objects.isNull(buyTicketRequest.getTicketCategory()) ||
                Objects.isNull(buyTicketRequest.getTicketOwnerInformation())) {

            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }

        Event selectedEvent = eventService.getEventById(buyTicketRequest.getEventId());

        if (selectedEvent == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Event not found");
        }

        if (!isTicketInStock(buyTicketRequest.getTicketCategory(), buyTicketRequest.getEventId(), (long) buyTicketRequest.getTicketOwnerInformation().size())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Ticket is not in stock");
        }
    }

    private boolean isTicketInStock(Long category, String event, Long numberOfTicket) {
        Category selectedCategory = categoryService.getCategoryByIdAndEventId(category, event);

        if (selectedCategory == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Category not found");
        }

        return selectedCategory.getCategoryAvailableCount() - numberOfTicket > 0;
    }

    @Override
    public Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "Ticket not found"));
    }

    @Override
    public void markTicketAsUsed(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setUsed(true);
        ticketRepository.save(ticket);
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public List<Ticket> getTicketsByEventId(String eventId) {
        return ticketRepository.findByEventId(eventId);
    }

    @Override
    public List<Ticket> getTicketListByTransactionRefNo(String transactionRefNo) {
        return ticketRepository.findByTicketTransactionId(transactionRefNo);
    }

    private String generateQRCode(Event event) {

        List<Ticket> ticketsFromEvent = getTicketsByEventId(event.getEventId());

        return event.getEventId() + event.getId() + (ticketsFromEvent.size() + 1); // Placeholder for the actual generation code
    }
}