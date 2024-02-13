package com.example.showtime.ticket.service.impl;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.common.pdf.PdfGenerator;
import com.example.showtime.event.model.entity.Event;
import com.example.showtime.event.model.response.EventResponse;
import com.example.showtime.event.services.IEventService;
import com.example.showtime.ticket.model.entity.Category;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.model.request.BuyTicketRequest;
import com.example.showtime.ticket.model.response.BuyTicketResponse;
import com.example.showtime.ticket.repository.CategoryRepository;
import com.example.showtime.ticket.repository.TicketRepository;
import com.example.showtime.ticket.service.ICategoryService;
import com.example.showtime.ticket.service.ITicketService;
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService implements ITicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final IEventService eventService;
    private final ICategoryService categoryService;

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
                            .userName(ticket.getTicketOwner())
                            .build())
                    .collect(Collectors.toList());

        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    private List<Ticket> prepareTicketModel(BuyTicketRequest buyTicketRequest) {

        List<Ticket> newTickets = new ArrayList<>(Math.toIntExact(buyTicketRequest.getNumberOfTicket()));

        for (int i = 0; i < buyTicketRequest.getNumberOfTicket(); i++) {
            Ticket ticket = new Ticket();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String createdByUserEmail = authentication.getName();

            UserAccount createdBy = userRepository.findByEmail(createdByUserEmail)
                    .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "User not found"));

            Event selectedEvent = eventService.getEventById(buyTicketRequest.getEventId());

            ticket.setTicketQrCode(generateQRCode(selectedEvent));
            ticket.setUsed(false);
            ticket.setActive(true);
            ticket.setEventId(selectedEvent.getEventId());
            ticket.setValidityDate(selectedEvent.getEventEndDate());
            ticket.setTicketCategory(buyTicketRequest.getTicketCategory());
            ticket.setTicketOwner(createdBy.getEmail());
            ticketRepository.save(ticket);
            eventService.updateAvailableTickets(selectedEvent.getEventId());
            categoryService.updateAvailableTickets(buyTicketRequest.getTicketCategory(), selectedEvent.getEventId());
            pdfGenerator.generateTicketPdf(createdBy, ticket);

            newTickets.add(ticket);
        }

        return newTickets;
    }

    private void validateRequest(BuyTicketRequest buyTicketRequest) {
        if (Objects.isNull(buyTicketRequest) ||
                StringUtils.isEmpty(buyTicketRequest.getEventId()) ||
                Objects.isNull(buyTicketRequest.getTicketCategory()) ||
                Objects.isNull(buyTicketRequest.getNumberOfTicket())) {

            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }

        Event selectedEvent = eventService.getEventById(buyTicketRequest.getEventId());

        if (selectedEvent == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Event not found");
        }

        if (!isTicketInStock(buyTicketRequest.getTicketCategory(), buyTicketRequest.getEventId(), buyTicketRequest.getNumberOfTicket())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Ticket is not in stock");
        }
    }

    private boolean isTicketInStock(Long category, String event, Long numberOfTicket) {
        Category selectedCategory = categoryService.getCategoryByIdAndEventId(category, event);

        if (selectedCategory == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Category not found");
        }

        if (selectedCategory.getCategoryAvailableCount() - numberOfTicket > 0) {
            return true;
        }

        return false;
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
}