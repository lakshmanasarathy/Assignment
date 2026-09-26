package com.assignment.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.assignment.dto.CreateTicketRequest;
import com.assignment.entity.Ticket;
import com.assignment.entity.User;
import com.assignment.enums.TicketStatus;
import com.assignment.repository.TicketActivityRepository;
import com.assignment.repository.TicketRepository;
import com.assignment.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketActivityRepository activityRepository;

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketActivityRepository activityRepository) {

        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
    }

    public Ticket createTicket(
            CreateTicketRequest request,
            String email) {

        User student = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Ticket ticket = new Ticket();

        ticket.setTicketNumber(
                generateTicketNumber());

        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());

        ticket.setStatus(TicketStatus.OPEN);

        ticket.setStudent(student);

        LocalDateTime now = LocalDateTime.now();

        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        ticket.setDueAt(
                calculateDueDate(
                    now,
                    request.getPriority()
                )
        );

        Ticket saved =
                ticketRepository.save(ticket);

        createActivity(
                saved,
                student,
                "CREATED",
                "Ticket created"
        );

        return saved;
    }
}
