package com.assignment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.assignment.dto.AddCommentRequest;
import com.assignment.dto.CreateTicketRequest;
import com.assignment.entity.Comment;
import com.assignment.entity.Ticket;
import com.assignment.entity.TicketActivity;
import com.assignment.service.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication) {

        Ticket ticket = ticketService.createTicket(
                request,
                authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticket);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Ticket>> getMyTickets(
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.getStudentTickets(
                        authentication.getName()));
    }

    @PutMapping("/{ticketId}/close")
    public ResponseEntity<Ticket> closeTicket(
            @PathVariable Long ticketId,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.closeTicket(
                        ticketId,
                        authentication.getName()));
    }

    @GetMapping("/{ticketId}/activities")
    public ResponseEntity<List<TicketActivity>> getTicketActivities(
            @PathVariable Long ticketId,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.getTicketActivities(ticketId));
    }

    @PostMapping("/{ticketId}/comments")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddCommentRequest request,
            Authentication authentication) {

        Comment comment = ticketService.addComment(
                ticketId,
                request,
                authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(comment);
    }

    @GetMapping("/{ticketId}/comments")
    public ResponseEntity<List<Comment>> getTicketComments(
            @PathVariable Long ticketId,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.getTicketComments(
                        ticketId,
                        authentication.getName()));
    }
}