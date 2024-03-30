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
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final IEventService eventService;
    private final ICategoryService categoryService;
    private final ITransactionService transactionService;

    PdfGenerator pdfGenerator = new PdfGenerator();

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<BuyTicketResponse> createTicket(BuyTicketRequest buyTicketRequest) {
        try {
            validateRequest(buyTicketRequest);

            List<Ticket> newTickets = prepareTicketModel(buyTicketRequest);

            return newTickets.stream()
                    .map(ticket -> BuyTicketResponse.builder()
                            .ticketId(ticket.getTicketQrCode())
                            .eventId(ticket.getEventId())
                            .userName(ticket.getTicketOwnerName())
                            .userEmail(ticket.getTicketOwnerEmail())
                            .userNumber(ticket.getTicketOwnerNumber())
                            .ticketCategory(ticket.getTicketCategory())
                            .transactionId(ticket.getTicketTransactionId())
                            .build())
                    .collect(Collectors.toList());

        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    private List<Ticket> prepareTicketModel(BuyTicketRequest buyTicketRequest) {
        List<Ticket> newTickets;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdByUserEmail = authentication.getName();

        UserAccount createdBy = userRepository.findByEmail(createdByUserEmail)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "User not found"));

        Event selectedEvent = eventService.getEventById(buyTicketRequest.getEventId());
        List<TicketOwnerInformationRequest> ticketOwnerInformation = buyTicketRequest.getTicketOwnerInformation();
        String refId = transactionService.generateUniqueIdForTransaction(selectedEvent.getEventId().substring(0, 2));

        newTickets = IntStream.range(0, Math.toIntExact(ticketOwnerInformation.size()))
                .parallel()
                .mapToObj(i -> {
                    Ticket ticket = new Ticket();
                    ticket.setTicketQrCode(generateQRCode(selectedEvent));
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
                    ticket.setTicketCreatedBy(createdByUserEmail);
                    ticket.setTicketPrice(categoryService.getTicketPrice(buyTicketRequest.getTicketCategory(), selectedEvent.getEventId()));
                    eventService.updateAvailableTickets(selectedEvent.getEventId());
                    categoryService.updateAvailableTickets(buyTicketRequest.getTicketCategory(), selectedEvent.getEventId());

                    generateTicketPdf(createdBy, ticket);

                    return ticket;
                })
                .collect(Collectors.toList());

        TransactionItem transactionItem = new TransactionItem();
        transactionItem.setTransactionRefNo(refId);
        transactionItem.setTotalAmount(newTickets.stream().mapToDouble(Ticket::getTicketPrice).sum());
        transactionItem.setEventId(selectedEvent.getEventId());
        transactionItem.setTransactionDate(Calendar.getInstance().getTime());
        transactionItem.setTransactionStatus(TransactionStatusEnum.INITIATED.getValue());
        transactionItem.setUserId(createdByUserEmail);

        transactionService.saveTransaction(transactionItem);

        ticketRepository.saveAll(newTickets);
        return newTickets;
    }

    @Async
    public void generateTicketPdf(UserAccount createdBy, Ticket ticket) {
        pdfGenerator.generateTicketPdf(createdBy, ticket);
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

        if (!isTicketInStock(buyTicketRequest.getTicketCategory(), buyTicketRequest.getEventId(), Long.valueOf(buyTicketRequest.getTicketOwnerInformation().size()))) {
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

    private String generateQRCode(Event event) {

        List<Ticket> ticketsFromEvent = getTicketsByEventId(event.getEventId());

        return event.getEventId() + event.getId() + (ticketsFromEvent.size() + 1); // Placeholder for the actual generation code
    }

    private String generateTransactionRefId(BuyTicketRequest buyTicketRequest) {
        //Generate a 9 digit reference number with the first 3 digits of the event id but in random order and 6 random numbers
        return buyTicketRequest.getEventId().substring(0, 3) + (int) (Math.random() * 1000000);
    }
}