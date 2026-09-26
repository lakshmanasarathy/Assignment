package com.assignment.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.assignment.dto.AssignTicketRequest;
import com.assignment.dto.UpdatePriorityRequest;
import com.assignment.dto.UpdateResolutionRequest;
import com.assignment.dto.UpdateStatusRequest;
import com.assignment.entity.Ticket;
import com.assignment.service.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/staff/tickets")
public class StaffTicketController {

    private final TicketService ticketService;

    public StaffTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets(
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.getAllTicketsForStaff(
                        authentication.getName()));
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<Ticket>> getUnassignedTickets(
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.getUnassignedTickets(
                        authentication.getName()));
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<Ticket>> getAssignedTickets(
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.getAssignedTickets(
                        authentication.getName()));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Ticket> getTicket(
            @PathVariable Long ticketId,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.getTicketForStaff(
                        ticketId,
                        authentication.getName()));
    }

    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<Ticket> assignTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AssignTicketRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.assignTicket(
                        ticketId,
                        request,
                        authentication.getName()));
    }

    @PutMapping("/{ticketId}/resolution")
    public ResponseEntity<Ticket> updateResolution(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateResolutionRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.updateResolution(
                        ticketId,
                        request,
                        authentication.getName()));
    }

    @PutMapping("/{ticketId}/status")
    public ResponseEntity<Ticket> updateStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateStatusRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.updateStatus(
                        ticketId,
                        request,
                        authentication.getName()));
    }

    @PutMapping("/{ticketId}/priority")
    public ResponseEntity<Ticket> updatePriority(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdatePriorityRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.updatePriority(
                        ticketId,
                        request,
                        authentication.getName()));
    }
}