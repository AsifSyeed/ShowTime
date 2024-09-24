package com.example.showtime.ticket.service;

import com.example.showtime.event.model.entity.Event;
import com.example.showtime.ticket.model.entity.PhysicalTicket;
import com.example.showtime.ticket.model.request.CreatePhysicalTicketRequest;
import com.example.showtime.ticket.model.request.SellPhysicalTicketRequest;

public interface IPhysicalTicketService {
    void savePhysicalTicket(CreatePhysicalTicketRequest physicalTicketRequest);
    boolean isPhysicalTicketValid(String physicalTicketId);
    void usePhysicalTicket(String physicalTicketId);

    PhysicalTicket getPhysicalTicketById(String physicalTicketId);

    void updatePhysicalTicket(PhysicalTicket physicalTicket);

    void sellPhysicalTicket(SellPhysicalTicketRequest sellPhysicalTicketRequest);
}
