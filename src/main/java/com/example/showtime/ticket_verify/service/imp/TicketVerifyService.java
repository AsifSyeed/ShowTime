package com.example.showtime.ticket_verify.service.imp;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.event.model.entity.Event;
import com.example.showtime.event.services.IEventService;
import com.example.showtime.ticket.model.entity.Category;
import com.example.showtime.ticket.model.entity.PhysicalTicket;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.service.ICategoryService;
import com.example.showtime.ticket.service.IPhysicalTicketService;
import com.example.showtime.ticket.service.ITicketService;
import com.example.showtime.ticket_verify.model.entity.TicketVerifier;
import com.example.showtime.ticket_verify.model.request.CreateVerifierRequest;
import com.example.showtime.ticket_verify.model.request.TicketVerifierValidationRequest;
import com.example.showtime.ticket_verify.model.request.TicketVerifyRequest;
import com.example.showtime.ticket_verify.model.response.TicketVerifierValidationResponse;
import com.example.showtime.ticket_verify.repository.TicketVerifierRepository;
import com.example.showtime.ticket_verify.service.ITicketVerifyService;
import com.example.showtime.transaction.enums.TransactionStatusEnum;
import com.example.showtime.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TicketVerifyService implements ITicketVerifyService {

    private final TicketVerifierRepository ticketVerifierRepository;
    private final IEventService eventService;
    private final ICategoryService categoryService;
    private final ITicketService ticketService;
    private final IPhysicalTicketService physicalTicketService;

    @Override
    public TicketVerifierValidationResponse validateVerifier(TicketVerifierValidationRequest ticketVerifierValidationRequest) {
        validateRequest(ticketVerifierValidationRequest);

        TicketVerifier ticketVerifier = ticketVerifierRepository.findByVerifierId(ticketVerifierValidationRequest.getVerifierId());

        if (ticketVerifier == null) {
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Verifier not found");
        }

        if (!ticketVerifier.getEventId().equals(ticketVerifierValidationRequest.getEventId())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Invalid verifier for this event");
        }

        Event event = eventService.getEventById(ticketVerifier.getEventId());
        Category category = categoryService.getCategoryById(ticketVerifier.getCategoryId());

        return TicketVerifierValidationResponse.builder()
                .verifierId(ticketVerifier.getVerifierId())
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .build();
    }

    @Override
    public void createVerifier(CreateVerifierRequest createVerifierRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userRole = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse(null);

            if (userRole == null) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "User role not found");
            }

            if (Integer.parseInt(userRole) != UserRole.SUPER_ADMIN.getValue() && Integer.parseInt(userRole) != UserRole.ADMIN.getValue()) {
                throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "You are not authorized");
            }

            if (Objects.isNull(createVerifierRequest) ||
                    Objects.isNull(createVerifierRequest.getEventId()) ||
                    Objects.isNull(createVerifierRequest.getCategoryId())) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Event ID and Category ID are required");
            }

            Event event = eventService.getEventById(createVerifierRequest.getEventId());
            Category category = categoryService.getCategoryById(createVerifierRequest.getCategoryId());

            if (event == null) {
                throw new BaseException(HttpStatus.NOT_FOUND.value(), "Event not found");
            }

            if (category == null) {
                throw new BaseException(HttpStatus.NOT_FOUND.value(), "Category not found");
            }

            if (!Objects.equals(category.getEventId(), event.getEventId())) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Invalid category for this event");
            }

            if (Integer.parseInt(userRole) == UserRole.ADMIN.getValue()) {
                if (!Objects.equals(event.getCreatedBy(), authentication.getName())) {
                    throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "You are not authorized to create a ticket verifier for this event");
                }
            }

            TicketVerifier ticketVerifier = new TicketVerifier();
            ticketVerifier.setEventId(event.getEventId());
            ticketVerifier.setCategoryId(category.getCategoryId());
            ticketVerifier.setVerifierId(generateVerifierId(createVerifierRequest));
            ticketVerifierRepository.save(ticketVerifier);

        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "You are not authorized to create a ticket verifier");
        }
    }

    @Override
    public void verifyTicket(TicketVerifyRequest ticketVerifyRequest) {
        validateTicketVerifyRequest(ticketVerifyRequest);
        TicketVerifier ticketVerifier = ticketVerifierRepository.findByVerifierId(ticketVerifyRequest.getVerifierId());
        // check if ticketId is greater than 8 characters
        if (ticketVerifyRequest.getTicketId().length() > 8) {
            Ticket ticket = ticketService.getTicketByTicketId(ticketVerifyRequest.getTicketId());

            if (ticket == null) {
                throw new BaseException(HttpStatus.NOT_FOUND.value(), "Ticket not found");
            }

            if (!ticket.getEventId().equals(ticketVerifier.getEventId())) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Invalid ticket for this event");
            }

            if (!ticketVerifier.getCategoryId().equals(ticket.getTicketCategory())) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Invalid ticket for this category");
            }

            if (ticket.getTicketTransactionStatus() != TransactionStatusEnum.SUCCESS.getValue()) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Ticket transaction status is not successful");
            }

            if (ticket.isUsed()) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Ticket has already been used");
            }

            if (!ticket.isActive()) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Ticket is not active");
            }

            ticket.setUsed(true);
            ticket.setActive(false);
            ticket.setVerifierId(ticketVerifier.getVerifierId());

            ticketService.updateTicket(ticket);
        } else {
            PhysicalTicket ticket = physicalTicketService.getPhysicalTicketById(ticketVerifyRequest.getTicketId());

            if (ticket == null) {
                throw new BaseException(HttpStatus.NOT_FOUND.value(), "Ticket not found");
            }

            if (!ticket.getEventId().equals(ticketVerifier.getEventId())) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Invalid ticket for this event");
            }

            if (!ticketVerifier.getCategoryId().equals(ticket.getTicketCategory())) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Invalid ticket for this category");
            }

            if (!ticket.isActive()) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Ticket is not active");
            }

            if (ticket.isUsed()) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Ticket has already been used");
            }

            ticket.setUsed(true);
            ticket.setActive(false);
            ticket.setVerifierId(ticketVerifier.getVerifierId());

            physicalTicketService.updatePhysicalTicket(ticket);
        }
    }

    private void validateTicketVerifyRequest(TicketVerifyRequest ticketVerifyRequest) {
        if (Objects.isNull(ticketVerifyRequest) ||
                Objects.isNull(ticketVerifyRequest.getEventId()) ||
                Objects.isNull(ticketVerifyRequest.getVerifierId()) ||
                Objects.isNull(ticketVerifyRequest.getTicketId())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Event ID, Verifier ID, and Ticket ID are required");
        }

        if (ticketVerifierRepository.findByVerifierId(ticketVerifyRequest.getVerifierId()) == null) {
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Verifier not found");
        }

        if (!ticketVerifierRepository.findByVerifierId(ticketVerifyRequest.getVerifierId()).getEventId().equals(ticketVerifyRequest.getEventId())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Invalid verifier for this event");
        }
    }

    private String generateVerifierId(CreateVerifierRequest createVerifierRequest) {
        // Generate a unique verifier ID with event ID, category ID, and a shortened timestamp
        long shortTime = System.nanoTime() / 1_000_000; // Divides by 1 million to get a shorter time value
        return createVerifierRequest.getEventId() + createVerifierRequest.getCategoryId() + shortTime;
    }

    private void validateRequest(TicketVerifierValidationRequest ticketVerifierValidationRequest) {
        if (ticketVerifierValidationRequest.getVerifierId() == null || ticketVerifierValidationRequest.getVerifierId().isEmpty()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Verifier ID is required");
        }
        if (ticketVerifierValidationRequest.getEventId() == null || ticketVerifierValidationRequest.getEventId().isEmpty()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Event ID is required");
        }

        TicketVerifier ticketVerifier = ticketVerifierRepository.findByVerifierId(ticketVerifierValidationRequest.getVerifierId());

        if (ticketVerifier == null) {
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Verifier not found");
        }

        if (!ticketVerifier.getEventId().equals(ticketVerifierValidationRequest.getEventId())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Invalid verifier for this event");
        }
    }
}
