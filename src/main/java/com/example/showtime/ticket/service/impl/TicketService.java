package com.example.showtime.ticket.service.impl;

import com.example.showtime.admin.repository.AdminRepository;
import com.example.showtime.common.exception.BaseException;
import com.example.showtime.common.uniqueId.UniqueIdGenerator;
import com.example.showtime.email.service.IEmailService;
import com.example.showtime.event.model.entity.Event;
import com.example.showtime.event.services.IEventService;
import com.example.showtime.ticket.enums.TicketTypeEnum;
import com.example.showtime.ticket.model.entity.Category;
import com.example.showtime.ticket.model.entity.PhysicalTicket;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.model.request.*;
import com.example.showtime.ticket.model.response.BuyTicketResponse;
import com.example.showtime.ticket.model.response.MyTicketResponse;
import com.example.showtime.ticket.repository.TicketRepository;
import com.example.showtime.ticket.service.ICategoryService;
import com.example.showtime.ticket.service.IPhysicalTicketService;
import com.example.showtime.ticket.service.ITicketService;
import com.example.showtime.transaction.enums.TransactionStatusEnum;
import com.example.showtime.transaction.ssl.SSLTransactionInitiator;
import com.example.showtime.user.enums.UserRole;
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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
    private final IEmailService emailService;
    private final IPhysicalTicketService physicalTicketService;
    private final UniqueIdGenerator uniqueIdGenerator;
    private final AdminRepository adminRepository;

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

        String refId = uniqueIdGenerator.generateUniqueUUID(selectedEvent.getEventId().substring(0, 2));

        newTickets = IntStream.range(0, Math.toIntExact(ticketOwnerInformation.size()))
                .parallel()
                .mapToObj(i -> {
                    Ticket ticket = new Ticket();

                    if (buyTicketRequest.getCouponCode().isEmpty()) {
                        ticket.setTicketId(uniqueIdGenerator.generateUniqueId(selectedEvent.getEventId()));
                    } else {
                        ticket.setTicketId(uniqueIdGenerator.generateUniqueId(buyTicketRequest.getCouponCode()));
                    }

                    ticket.setEventName(selectedEvent.getEventName());
                    ticket.setTicketTransactionStatus(TransactionStatusEnum.INITIATED.getValue());
                    ticket.setUsed(false);
                    ticket.setActive(true);
                    ticket.setTicketTransactionId(refId);
                    ticket.setTicketCreatedDate(Calendar.getInstance().getTime());
                    ticket.setEventId(selectedEvent.getEventId());
                    ticket.setEventImageUrl(selectedEvent.getEventThumbnailUrl());
                    ticket.setValidityDate(selectedEvent.getEventEndDate());
                    ticket.setTicketCategory(buyTicketRequest.getTicketCategory());
                    ticket.setTicketOwnerName(ticketOwnerInformation.get(i).getTicketOwnerName());
                    ticket.setTicketOwnerEmail(ticketOwnerInformation.get(i).getTicketOwnerEmail());
                    ticket.setTicketOwnerNumber(ticketOwnerInformation.get(i).getTicketOwnerNumber());
                    ticket.setTicketCreatedBy(createdBy.getEmail());
                    ticket.setTicketPrice(categoryService.getTicketPrice(buyTicketRequest.getTicketCategory(), selectedEvent.getEventId()));
                    ticket.setAppliedCoupon(buyTicketRequest.getCouponCode());
                    ticket.setTicketType(TicketTypeEnum.ONLINE.getValue());

                    return ticket;
                })
                .collect(Collectors.toList());

        eventService.updateAvailableTickets(selectedEvent.getEventId(), buyTicketRequest.getTicketCategory(), newTickets.size());

        ticketRepository.saveAll(newTickets);
        return Pair.of(refId, buyTicketRequest.getTotalPrice());
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
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Ticket is not in stock");
        }
    }

    private boolean isTicketInStock(Long category, String event, Long numberOfTicket) {
        Category selectedCategory = categoryService.getCategoryByIdAndEventId(category, event);

        if (selectedCategory == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Category not found");
        }

        return !(selectedCategory.getCategoryAvailableCount() - numberOfTicket < 0);
    }

    @Override
    public Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "Ticket not found"));
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
    public Ticket getTicketByTicketId(String ticketId) {
        return ticketRepository.findByTicketId(ticketId);
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

            return ticketRepository.findByTicketCreatedByOrderByTicketCreatedDateDesc(createdBy.getEmail()).stream()
                    .filter(ticket -> ticket.getTicketTransactionStatus() == TransactionStatusEnum.SUCCESS.getValue()) // Add this line to filter tickets
                    .map(ticket -> MyTicketResponse.builder()
                            .ticketId(ticket.getTicketId())
                            .eventName(ticket.getEventName())
                            .ticketOwnerName(ticket.getTicketOwnerName())
                            .ticketOwnerEmail(ticket.getTicketOwnerEmail())
                            .ticketOwnerNumber(ticket.getTicketOwnerNumber())
                            .ticketCategoryName(categoryService.getCategoryByIdAndEventId(ticket.getTicketCategory(), ticket.getEventId()).getCategoryName())
                            .ticketPrice(String.valueOf(ticket.getTicketPrice()))
                            .ticketStatus(ticket.isUsed())
                            .eventImageUrl(ticket.getEventImageUrl())
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
        } else {
            CompletableFuture.runAsync(() -> sendEmailToCustomer(selectedTickets));
        }

        selectedTickets.forEach(ticket -> {
            ticket.setTicketTransactionStatus(transactionStatus);
            ticketRepository.save(ticket);
        });
    }

    @Override
    public void verifyTicket(CheckTicketRequest checkTicketRequest) {

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String userRole = authentication.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .findFirst()
//                .orElse(null);
//
//        if (userRole == null || Integer.parseInt(userRole) != UserRole.ADMIN.getValue()) {
//            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "User not authorized to do the action");
//        }

        if (checkTicketRequest.getTicketId().isEmpty()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Ticket id is required");
        }

        Ticket ticket = getTicketByTicketId(checkTicketRequest.getTicketId());

        if (ticket == null) {
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Ticket not found");
        }

        if (ticket.isUsed()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Ticket already used");
        }

        if (ticket.getTicketType() != TicketTypeEnum.ONLINE.getValue()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Online ticket is not valid");
        }

        if (ticket.getTicketTransactionStatus() != TransactionStatusEnum.SUCCESS.getValue()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Ticket not verified");
        }

        ticket.setUsed(true);
        ticketRepository.save(ticket);
    }

    @Override
    public void sendEmail(String ticketId) {
        if (ticketId.isEmpty()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Ticket id is required");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdByUserEmail = authentication.getName();

        UserAccount createdBy = userService.getUserByEmail(createdByUserEmail);

        try {
            Ticket ticket = getTicketByUserAndTicketId(createdBy, ticketId);

            if (ticket == null) {
                throw new BaseException(HttpStatus.NOT_FOUND.value(), "Ticket not found");
            }

            if (ticket.getTicketTransactionStatus() != TransactionStatusEnum.SUCCESS.getValue()) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Transaction not verified");
            }

            if (ticket.isUsed()) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Ticket already used");
            }

            if (ticket.getTicketType() != TicketTypeEnum.ONLINE.getValue()) {
                throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Online ticket is not valid");
            }

            List<Ticket> selectedTickets = List.of(ticket);
            sendEmailToCustomer(selectedTickets);

        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    @Override
    public void createPhysicalTicket(CreatePhysicalTicketRequest physicalTicketRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        if (userRole == null || Integer.parseInt(userRole) != UserRole.ADMIN.getValue() || Integer.parseInt(userRole) != UserRole.SUPER_ADMIN.getValue()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "You are not authorized to create physical ticket");
        }

        if (Objects.isNull(physicalTicketRequest) ||
                StringUtils.isEmpty(physicalTicketRequest.getEventId())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }

        Event selectedEvent = eventService.getEventById(physicalTicketRequest.getEventId());

        if (selectedEvent == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Event not found");
        }

        physicalTicketService.savePhysicalTicket(physicalTicketRequest);
    }

    @Override
    public void tagPhysicalTicket(TagPhysicalTicketRequest tagPhysicalTicketRequest) {
        if (Objects.isNull(tagPhysicalTicketRequest) ||
                StringUtils.isEmpty(tagPhysicalTicketRequest.getPhysicalTicketId()) ||
                StringUtils.isEmpty(tagPhysicalTicketRequest.getOnlineTicketId())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }

        Ticket ticket = getTicketByTicketId(tagPhysicalTicketRequest.getOnlineTicketId());
        PhysicalTicket physicalTicket = physicalTicketService.getPhysicalTicketById(tagPhysicalTicketRequest.getPhysicalTicketId());

        if (ticket == null) {
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Online Ticket not found");
        }

        if (ticket.isUsed()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Online Ticket already used");
        }

        if (physicalTicket == null) {
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Physical Ticket not found");
        }

        if (ticket.getTicketType() != TicketTypeEnum.ONLINE.getValue()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Physical Ticket already tagged");
        }

        ticket.setTicketType(TicketTypeEnum.PHYSICAL.getValue());
        ticketRepository.save(ticket);

        physicalTicket.setOnlineTicketId(tagPhysicalTicketRequest.getOnlineTicketId());
        physicalTicket.setTicketOwnerEmail(ticket.getTicketOwnerEmail());
        physicalTicket.setTicketOwnerName(ticket.getTicketOwnerName());
        physicalTicket.setTicketOwnerNumber(ticket.getTicketOwnerNumber());
        physicalTicketService.updatePhysicalTicket(physicalTicket);
    }

    @Override
    public void sendEmailToFailedTransaction() {
        // find all tickets with transaction status is not success
        List<Ticket> failedTickets = ticketRepository.findByTicketTransactionStatusNot(TransactionStatusEnum.SUCCESS.getValue());

        // run a loop to failedTickets length
        failedTickets.forEach(ticket -> {
            // send email to ticket owner
            String htmlContent = String.format(
                    "<p style=\"font-size: 16px;\">Dear <strong>%s</strong>,</p>"
                            + "<p>We hope this message finds you well. We wanted to inform you that we recently experienced some technical issues on our site, and we sincerely apologize for any inconvenience this may have caused.</p>"
                            + "<p>As a token of our appreciation for your understanding, we are pleased to offer you an exclusive discount of <strong>350 Taka</strong> on your next purchase.</p>"
                            + "<p>Simply use the code <strong>&quot;DHAKAVIBES350&quot;</strong> at checkout. You can purchase up to a maximum of 4 tickets under this code.</p>"
                            + "<p><strong>Note:</strong> This coupon is valid till <strong>11:59 PM, 26th September, 2024</strong>.</p>"
                            + "<p>Don't miss out on <a href=\"https://www.countersbd.com/events/dhakavibes\">Relevent Presents Dhaka Vibes</a>! Check out the event for more details.</p>"
                            + "<p>Thank you for your continued support! If you have any questions or need assistance, feel free to reach out to us at <a href=\"tel:01871535919\">01871535919</a>.</p>"
                            + "<p>Regards,</p>"
                            + "<p>Team Counters</p>",
                    ticket.getTicketOwnerName()
            );

            emailService.sendGenericEmail(ticket.getTicketOwnerEmail(), "Special Coupon Code due to Technical Issue", htmlContent);
        });
    }

    @Override
    public List<Ticket> getTicketsByTransactionStatus(int value) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        // check if userRole is null or not either SUPER_ADMIN or ADMIN
        if (userRole == null) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "User Role not found");
        }

        if (Integer.parseInt(userRole) != UserRole.SUPER_ADMIN.getValue() && Integer.parseInt(userRole) != UserRole.ADMIN.getValue()) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "You are not authorized");
        }

        if (Integer.parseInt(userRole) == UserRole.SUPER_ADMIN.getValue()) {
            return ticketRepository.findByTicketTransactionStatus(value);
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();

            List<Event> events = eventService.getEventByCreatedBy(userEmail);
            // get event ids
            List<String> eventIds = events.stream()
                    .map(Event::getEventId)
                    .collect(Collectors.toList());
            // return tickets by event ids and transaction status
            return ticketRepository.findByEventIdInAndTicketTransactionStatus(eventIds, value);
        }
    }

    @Override
    public List<Ticket> getTicketsByEventIdsAndTransactionStatus(List<String> eventIds, int value) {
        return ticketRepository.findByEventIdInAndTicketTransactionStatus(eventIds, value);
    }

    @Override
    public List<Ticket> getTicketsByEventIdAndTransactionStatus(String eventId, int value) {
        return ticketRepository.findByEventIdAndTicketTransactionStatus(eventId, value);
    }

    private Ticket getTicketByUserAndTicketId(UserAccount createdBy, String ticketId) {
        return ticketRepository.findByTicketIdAndTicketCreatedBy(ticketId, createdBy.getEmail());
    }

    private void sendEmailToCustomer(List<Ticket> selectedTickets) {
        emailService.sendTicketConfirmationMail(selectedTickets);
    }
}