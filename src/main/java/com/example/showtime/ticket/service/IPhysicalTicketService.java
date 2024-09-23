package com.example.showtime.ticket.service;

import com.example.showtime.event.model.entity.Event;
import com.example.showtime.ticket.model.request.CreatePhysicalTicketRequest;

public interface IPhysicalTicketService {
    void savePhysicalTicket(CreatePhysicalTicketRequest physicalTicketRequest);
    boolean isPhysicalTicketValid(String physicalTicketId);
    void usePhysicalTicket(String physicalTicketId);
}
