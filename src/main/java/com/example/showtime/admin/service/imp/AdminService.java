package com.example.showtime.admin.service.imp;

import com.example.showtime.admin.model.entity.Admin;
import com.example.showtime.admin.model.request.AdminSignUpRequest;
import com.example.showtime.admin.model.response.CategorySalesInfo;
import com.example.showtime.admin.model.response.DashboardInfoResponse;
import com.example.showtime.admin.repository.AdminRepository;
import com.example.showtime.admin.service.IAdminService;
import com.example.showtime.common.exception.BaseException;
import com.example.showtime.common.model.response.UserProfileResponse;
import com.example.showtime.event.model.entity.Event;
import com.example.showtime.event.services.IEventService;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.model.response.EventCategoryResponse;
import com.example.showtime.ticket.service.ITicketService;
import com.example.showtime.transaction.enums.TransactionStatusEnum;
import com.example.showtime.transaction.model.entity.TransactionItem;
import com.example.showtime.transaction.service.impl.TransactionService;
import com.example.showtime.user.enums.UserRole;
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService implements IAdminService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final IUserService userService;
    private final ITicketService ticketService;
    private final IEventService eventService;
    private final TransactionService transactionService;

    @Override
    public void signUpAdmin(AdminSignUpRequest adminSignUpRequest) {
        validateRequest(adminSignUpRequest);

        Admin admin = prepareAdminModel(adminSignUpRequest);

        adminRepository.save(admin);
    }

    @Override
    public UserProfileResponse getAdminProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String createdByUserEmail = authentication.getName();

            Admin admin = adminRepository.findByEmail(createdByUserEmail)
                    .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "User not found"));

            return UserProfileResponse.builder()
                    .userName(admin.getAdminName())
                    .emailId(admin.getEmail())
                    .phoneNumber(admin.getPhoneNumber())
                    .userRole(admin.getRole())
                    .build();
        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<UserProfileResponse> getUserList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        if (userRole == null || Integer.parseInt(userRole) != UserRole.SUPER_ADMIN.getValue()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "User Role not authorized");
        }

        List<UserAccount> userList = userService.getUserList();

        return userList.stream()
                .map(userAccount -> UserProfileResponse.builder()
                        .userName(userAccount.getUserName())
                        .emailId(userAccount.getEmail())
                        .phoneNumber(userAccount.getPhoneNumber())
                        .userRole(userAccount.getRole())
                        .userFirstName(userAccount.getFirstName())
                        .userLastName(userAccount.getLastName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public DashboardInfoResponse getDashboardInfo(String eventId) {
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

        // create an empty list of Ticket
        List<Ticket> purchasedTickets;
        List<TransactionItem> transactionItems;
        double totalRevenue;

        // create an empty list of category
        List<CategorySalesInfo> categorySalesInfo = new ArrayList<>();

        if (eventId != null) {
            purchasedTickets = ticketService.getTicketsByEventIdAndTransactionStatus(eventId, TransactionStatusEnum.SUCCESS.getValue());
            transactionItems = transactionService.getTransactionsByEventIdAndTransactionStatus(eventId, TransactionStatusEnum.SUCCESS.getValue());

            // Get the list of category for the event
            List<EventCategoryResponse> eventCategoryList = eventService.getCategoryList(eventId);
            // create a loop and get the category sales info
            categorySalesInfo = eventCategoryList.stream()
                    .map(eventCategoryResponse -> {
                        long categoryId = eventCategoryResponse.getCategoryId();
                        List<Ticket> purchasedTicketsByCategory = purchasedTickets.stream()
                                .filter(ticket -> ticket.getTicketCategory() == categoryId)
                                .collect(Collectors.toList());

                        long totalTickets = purchasedTicketsByCategory.size();

                        // need to map total amount with transactionItem.transactionId == ticket.transactionId first
                        double totalAmount = transactionItems.stream()
                                .filter(transactionItem -> purchasedTicketsByCategory.stream()
                                        .anyMatch(ticket -> ticket.getTicketTransactionId().equals(transactionItem.getTransactionRefNo())))
                                .mapToDouble(TransactionItem::getTotalAmount)
                                .sum();

                        return CategorySalesInfo.builder()
                                .categoryId(eventCategoryResponse.getCategoryId())
                                .categoryName(eventCategoryResponse.getCategoryName())
                                .totalPurchasedTicket(totalTickets)
                                .totalRevenue(totalAmount)
                                .eventId(eventId)
                                .build();
                    })
                    .collect(Collectors.toList());

            // set the total revenue
            totalRevenue = categorySalesInfo.stream()
                    .mapToDouble(CategorySalesInfo::getTotalRevenue)
                    .sum();
        } else {
            if (Integer.parseInt(userRole) == UserRole.SUPER_ADMIN.getValue()) {
                purchasedTickets = ticketService.getTicketsByTransactionStatus(TransactionStatusEnum.SUCCESS.getValue());
                transactionItems = transactionService.getTransactionsByTransactionStatus(TransactionStatusEnum.SUCCESS.getValue());

                totalRevenue = transactionItems.stream()
                        .mapToDouble(TransactionItem::getTotalAmount)
                        .sum();
            } else {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = auth.getName();

                List<Event> events = eventService.getEventByCreatedBy(userEmail);

                List<String> eventIds = events.stream()
                        .map(Event::getEventId)
                        .collect(Collectors.toList());

                purchasedTickets = ticketService.getTicketsByEventIdsAndTransactionStatus(eventIds, TransactionStatusEnum.SUCCESS.getValue());
                transactionItems = transactionService.getTransactionsByEventIdsAndTransactionStatus(eventIds, TransactionStatusEnum.SUCCESS.getValue());

                totalRevenue = transactionItems.stream()
                        .mapToDouble(TransactionItem::getTotalAmount)
                        .sum();
            }
        }

        // get total users
        Long totalUser = userService.getTotalUserCount();

        return DashboardInfoResponse.builder()
                .totalRevenue(totalRevenue)
                .totalPurchasedTicket((long) purchasedTickets.size())
                .totalUser(totalUser)
                .categorySalesInfoList(categorySalesInfo)
                .build();
    }

    private Admin prepareAdminModel(AdminSignUpRequest adminSignUpRequest) {
        Admin admin = new Admin();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdByUserEmail = authentication.getName();

        Admin createdBy = adminRepository.findByEmail(createdByUserEmail)
                .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND.value(), "Admin not found"));

        admin.setEmail(adminSignUpRequest.getEmail());
        admin.setPhoneNumber(adminSignUpRequest.getPhoneNumber());
        admin.setAdminName(adminSignUpRequest.getUserName());
        admin.setPassword(passwordEncoder.encode(adminSignUpRequest.getPassword()));
        admin.setRole(UserRole.ADMIN.getValue());
        admin.setCreatedBy(createdBy.getAdminName());
        admin.setAdminCreationDate(new Date());
        return admin;
    }

    private void validateRequest(AdminSignUpRequest adminSignUpRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        if (userRole == null || Integer.parseInt(userRole) != UserRole.SUPER_ADMIN.getValue()) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "You are not authorized to create an admin");
        }

        if (Objects.isNull(adminSignUpRequest) ||
                StringUtils.isEmpty(adminSignUpRequest.getEmail()) ||
                StringUtils.isEmpty(adminSignUpRequest.getPhoneNumber()) ||
                StringUtils.isEmpty(adminSignUpRequest.getPassword()) ||
                StringUtils.isEmpty(adminSignUpRequest.getUserName())) {

            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }

        if (isAdminEmailExists(adminSignUpRequest.getEmail())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Email ID already used");
        }

        if (isUserNameExists(adminSignUpRequest.getUserName())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Username already used");
        }

        if (isPhoneNumberExists(adminSignUpRequest.getPhoneNumber())) {
            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Phone Number already used");
        }
    }

    private boolean isAdminEmailExists(String email) {
        return adminRepository.existsByEmail(email);
    }

    private boolean isUserNameExists(String userName) {
        return adminRepository.existsAdminByAdminName(userName);
    }

    private boolean isPhoneNumberExists(String phoneNumber) {
        return adminRepository.existsByPhoneNumber(phoneNumber);
    }
}
