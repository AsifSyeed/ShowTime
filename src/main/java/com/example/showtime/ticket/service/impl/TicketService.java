package com.example.showtime.ticket.service.impl;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.common.uniqueId.UniqueIdGenerator;
import com.example.showtime.event.model.entity.Event;
import com.example.showtime.event.services.IEventService;
import com.example.showtime.ticket.model.entity.Category;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.model.request.BuyTicketRequest;
import com.example.showtime.ticket.model.request.TicketOwnerInformationRequest;
import com.example.showtime.ticket.model.response.BuyTicketResponse;
import com.example.showtime.ticket.model.response.MyTicketResponse;
import com.example.showtime.ticket.repository.TicketRepository;
import com.example.showtime.ticket.service.ICategoryService;
import com.example.showtime.ticket.service.ITicketService;
import com.example.showtime.transaction.enums.TransactionStatusEnum;
import com.example.showtime.transaction.ssl.SSLTransactionInitiator;
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
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
    private final SSLTransactionInitiator sslTransactionInitiator;

    @Value("${node.id}")
    private Long nodeId;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BuyTicketResponse createTicket(BuyTicketRequest buyTicketRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdByUserEmail = authentication.getName();

        UserAccount createdBy = userService.getUserByEmail(createdByUserEmail);

        try {
            validateRequest(buyTicketRequest);

            Pair<String, Double> result = prepareTicketModel(buyTicketRequest, createdBy);
            String transactionRef = result.getFirst();
            Double totalPrice = result.getSecond();
            String sslCommerzPaymentUrl = sslTransactionInitiator.initiateSSLTransaction(transactionRef, totalPrice, createdBy);

            return BuyTicketResponse.builder()
                    .transactionRefNo(transactionRef)
                    .sslPaymentUrl(sslCommerzPaymentUrl)
                    .build();
        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    private Pair<String, Double> prepareTicketModel(BuyTicketRequest buyTicketRequest, UserAccount createdBy) {
        List<Ticket> newTickets;

        Event selectedEvent = eventService.getEventById(buyTicketRequest.getEventId());
        List<TicketOwnerInformationRequest> ticketOwnerInformation = buyTicketRequest.getTicketOwnerInformation();

        UniqueIdGenerator uniqueIdGenerator = new UniqueIdGenerator(nodeId);

        String refId = uniqueIdGenerator.generateUniqueTransactionReferenceNo(selectedEvent.getEventId().substring(0, 2));

        newTickets = IntStream.range(0, Math.toIntExact(ticketOwnerInformation.size()))
                .parallel()
                .mapToObj(i -> {
                    Ticket ticket = new Ticket();
                    ticket.setTicketId(uniqueIdGenerator.generateUniqueId(selectedEvent.getEventId()));
                    ticket.setEventName(selectedEvent.getEventName());
                    ticket.setTicketTransactionStatus(TransactionStatusEnum.INITIATED.getValue());
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

                    return ticket;
                })
                .collect(Collectors.toList());

        eventService.updateAvailableTickets(selectedEvent.getEventId(), buyTicketRequest.getTicketCategory(), newTickets.size());

        ticketRepository.saveAll(newTickets);
        return Pair.of(refId, newTickets.stream().mapToDouble(Ticket::getTicketPrice).sum());
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

    @Override
    public List<MyTicketResponse> getMyTickets() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdByUserEmail = authentication.getName();

        UserAccount createdBy = userService.getUserByEmail(createdByUserEmail);

        try {
            return ticketRepository.findByTicketCreatedBy(createdBy.getEmail()).stream()
                    .map(ticket -> MyTicketResponse.builder()
                            .ticketId(ticket.getTicketId())
                            .eventName(ticket.getEventName())
                            .ticketOwnerName(ticket.getTicketOwnerName())
                            .ticketOwnerEmail(ticket.getTicketOwnerEmail())
                            .ticketOwnerNumber(ticket.getTicketOwnerNumber())
                            .ticketCategoryName(categoryService.getCategoryByIdAndEventId(ticket.getTicketCategory(), ticket.getEventId()).getCategoryName())
                            .ticketPrice(String.valueOf(ticket.getTicketPrice()))
                            .ticketStatus(ticket.isUsed())
                            .validityDate(String.valueOf(ticket.getValidityDate()))
                            .build())
                    .collect(Collectors.toList());
        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    @Override
    public void updateTicketStatus(List<Ticket> selectedTickets, int transactionStatus) {
        if (transactionStatus != TransactionStatusEnum.SUCCESS.getValue()) {
            eventService.updateAvailableTickets(selectedTickets.get(0).getEventId(), selectedTickets.get(0).getTicketCategory(), (-selectedTickets.size()));
        }

        selectedTickets.forEach(ticket -> {
            ticket.setTicketTransactionStatus(transactionStatus);
            ticketRepository.save(ticket);
        });
    }
}