package com.example.showtime.ticket.service.impl;

import com.example.showtime.ticket.model.entity.PhysicalTicket;
import com.example.showtime.ticket.model.request.CreatePhysicalTicketRequest;
import com.example.showtime.ticket.repository.PhysicalTicketRepository;
import com.example.showtime.ticket.service.IPhysicalTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhysicalTicketService implements IPhysicalTicketService {

    private final PhysicalTicketRepository physicalTicketRepository;

    @Override
    public void savePhysicalTicket(CreatePhysicalTicketRequest physicalTicketRequest) {
        // start a loop with the quantity of physical tickets
        for (int i = 0; i < physicalTicketRequest.getQuantity(); i++) {
            // create a new physical ticket
            PhysicalTicket physicalTicket = new PhysicalTicket();
            physicalTicket.setEventId(physicalTicketRequest.getEventId());
            physicalTicket.setActive(false);
            physicalTicket.setUsed(false);

            PhysicalTicket savedTicket = physicalTicketRepository.save(physicalTicket);

            List<PhysicalTicket> physicalTickets = physicalTicketRepository.findByEventId(physicalTicketRequest.getEventId());

            int ticketId = physicalTickets.size() + 1;

            String eventIdPrefix = physicalTicketRequest.getEventId().substring(0, Math.min(physicalTicketRequest.getEventId().length(), 3));  // Ensure eventId is at least 3 characters
            String physicalTicketId = eventIdPrefix + String.format("%05d", ticketId);    // Pad ticketId with leading zeros if needed

            savedTicket.setPhysicalTicketId(physicalTicketId);
            physicalTicketRepository.save(savedTicket);
            // save the physical ticket
            physicalTicketRepository.save(physicalTicket);
        }
    }

    @Override
    public boolean isPhysicalTicketValid(String physicalTicketId) {
        return false;
    }

    @Override
    public void usePhysicalTicket(String physicalTicketId) {

    }
}
