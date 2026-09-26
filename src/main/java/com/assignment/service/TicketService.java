package com.assignment.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.assignment.dto.AddCommentRequest;
import com.assignment.dto.AssignTicketRequest;
import com.assignment.dto.CreateTicketRequest;
import com.assignment.dto.UpdatePriorityRequest;
import com.assignment.dto.UpdateStatusRequest;
import com.assignment.entity.Comment;
import com.assignment.entity.Role;
import com.assignment.entity.Ticket;
import com.assignment.entity.TicketActivity;
import com.assignment.entity.User;
import com.assignment.enums.Priority;
import com.assignment.enums.TicketStatus;
import com.assignment.repository.CommentRepository;
import com.assignment.repository.TicketActivityRepository;
import com.assignment.repository.TicketRepository;
import com.assignment.repository.UserRepository;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketActivityRepository ticketActivityRepository;
    private final CommentRepository commentRepository;

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            TicketActivityRepository ticketActivityRepository,
            CommentRepository commentRepository) {

        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketActivityRepository = ticketActivityRepository;
        this.commentRepository = commentRepository;
    }

    // =========================
    // STUDENT
    // =========================

    public Ticket createTicket(
            CreateTicketRequest request,
            String studentEmail) {

        User student = userRepository
                .findByEmail(studentEmail)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new RuntimeException(
                    "Only students can create tickets");
        }

        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = new Ticket();

        ticket.setTicketNumber(generateTicketNumber());
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());

        ticket.setStatus(TicketStatus.OPEN);
        ticket.setStudent(student);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticket.setDueAt(
                calculateDueDate(now, request.getPriority()));

        Ticket savedTicket = ticketRepository.save(ticket);

        createActivity(
                savedTicket,
                student,
                "TICKET_CREATED",
                "Ticket created by student");

        return savedTicket;
    }

    public List<Ticket> getStudentTickets(String studentEmail) {

        User student = userRepository
                .findByEmail(studentEmail)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        return ticketRepository.findByStudent(student);
    }

    public List<TicketActivity> getTicketActivities(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new RuntimeException("Ticket not found"));

        return ticketActivityRepository
                .findByTicketOrderByCreatedAtAsc(ticket);
    }

    // =========================
    // COMMENTS
    // =========================

    public Comment addComment(
            Long ticketId,
            AddCommentRequest request,
            String userEmail) {

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Ticket ticket = findTicket(ticketId);

        validateCommentAccess(ticket, user);

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new RuntimeException(
                    "Closed ticket cannot be commented on");
        }

        String message = request.getMessage().trim();

        if (message.isEmpty()) {
            throw new RuntimeException(
                    "Comment message is required");
        }

        LocalDateTime now = LocalDateTime.now();

        Comment comment = new Comment();

        comment.setTicket(ticket);
        comment.setUser(user);
        comment.setMessage(message);
        comment.setCreatedAt(now);

        Comment savedComment = commentRepository.save(comment);

        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);

        createActivity(
                ticket,
                user,
                "COMMENT_ADDED",
                "Comment added to ticket");

        return savedComment;
    }

    public List<Comment> getTicketComments(
            Long ticketId,
            String userEmail) {

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Ticket ticket = findTicket(ticketId);

        validateCommentAccess(ticket, user);

        return commentRepository
                .findByTicketOrderByCreatedAtAsc(ticket);
    }

    private void validateCommentAccess(
            Ticket ticket,
            User user) {

        if (user.getRole() == Role.STUDENT) {

            if (ticket.getStudent() == null ||
                    !ticket.getStudent()
                            .getId()
                            .equals(user.getId())) {

                throw new RuntimeException(
                        "You are not authorized to access this ticket");
            }

            return;
        }

        if (user.getRole() == Role.STAFF) {

            if (ticket.getAssignedTo() == null ||
                    !ticket.getAssignedTo()
                            .getId()
                            .equals(user.getId())) {

                throw new RuntimeException(
                        "Only the assigned staff member can access this ticket");
            }

            return;
        }

        throw new RuntimeException(
                "You are not authorized to access this ticket");
    }

    // =========================
    // STAFF
    // =========================

    public List<Ticket> getAllTicketsForStaff(String staffEmail) {
        getStaff(staffEmail);
        return ticketRepository.findAll();
    }

    public List<Ticket> getUnassignedTickets(String staffEmail) {
        getStaff(staffEmail);
        return ticketRepository.findByAssignedToIsNull();
    }

    public List<Ticket> getAssignedTickets(String staffEmail) {
        User staff = getStaff(staffEmail);
        return ticketRepository.findByAssignedTo(staff);
    }

    public Ticket getTicketForStaff(
            Long ticketId,
            String staffEmail) {

        getStaff(staffEmail);

        return findTicket(ticketId);
    }

    public Ticket assignTicket(
            Long ticketId,
            AssignTicketRequest request,
            String assigningStaffEmail) {

        User assigningStaff = getStaff(assigningStaffEmail);
        Ticket ticket = findTicket(ticketId);

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new RuntimeException(
                    "Closed ticket cannot be assigned");
        }

        User assignedStaff = userRepository
                .findByEmail(request.getStaffEmail())
                .orElseThrow(() ->
                        new RuntimeException("Staff user not found"));

        if (assignedStaff.getRole() != Role.STAFF) {
            throw new RuntimeException(
                    "Ticket can only be assigned to staff");
        }

        ticket.setAssignedTo(assignedStaff);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        createActivity(
                savedTicket,
                assigningStaff,
                "TICKET_ASSIGNED",
                "Ticket assigned to " + assignedStaff.getEmail());

        return savedTicket;
    }

    public Ticket updateStatus(
            Long ticketId,
            UpdateStatusRequest request,
            String staffEmail) {

        User staff = getStaff(staffEmail);
        Ticket ticket = findTicket(ticketId);

        TicketStatus oldStatus = ticket.getStatus();
        TicketStatus newStatus = request.getStatus();

        if (oldStatus == TicketStatus.CLOSED) {
            throw new RuntimeException(
                    "Closed ticket cannot be modified");
        }

        if (oldStatus == newStatus) {
            throw new RuntimeException(
                    "Ticket already has this status");
        }

        validateStatusTransition(oldStatus, newStatus);

        if (newStatus == TicketStatus.RESOLVED) {

            if (ticket.getResolution() == null ||
                    ticket.getResolution().trim().isEmpty()) {

                throw new RuntimeException(
                        "Resolution is required before resolving ticket");
            }

            ticket.setResolvedAt(LocalDateTime.now());
        }

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        createActivity(
                savedTicket,
                staff,
                "STATUS_CHANGED",
                "Status changed from "
                        + oldStatus
                        + " to "
                        + newStatus);

        return savedTicket;
    }

    public Ticket updatePriority(
            Long ticketId,
            UpdatePriorityRequest request,
            String staffEmail) {

        User staff = getStaff(staffEmail);
        Ticket ticket = findTicket(ticketId);

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new RuntimeException(
                    "Closed ticket cannot be modified");
        }

        Priority oldPriority = ticket.getPriority();
        Priority newPriority = request.getPriority();

        if (oldPriority == newPriority) {
            throw new RuntimeException(
                    "Ticket already has this priority");
        }

        ticket.setPriority(newPriority);

        ticket.setDueAt(
                calculateDueDate(
                        LocalDateTime.now(),
                        newPriority));

        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        createActivity(
                savedTicket,
                staff,
                "PRIORITY_CHANGED",
                "Priority changed from "
                        + oldPriority
                        + " to "
                        + newPriority);

        return savedTicket;
    }

    // =========================
    // COMMON METHODS
    // =========================

    private User getStaff(String email) {

        User staff = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Staff not found"));

        if (staff.getRole() != Role.STAFF) {
            throw new RuntimeException(
                    "Only staff can perform this operation");
        }

        return staff;
    }

    private Ticket findTicket(Long ticketId) {

        return ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new RuntimeException("Ticket not found"));
    }

    private void createActivity(
            Ticket ticket,
            User user,
            String action,
            String description) {

        TicketActivity activity = new TicketActivity();

        activity.setTicket(ticket);
        activity.setUser(user);
        activity.setAction(action);
        activity.setDescription(description);
        activity.setCreatedAt(LocalDateTime.now());

        ticketActivityRepository.save(activity);
    }

    private String generateTicketNumber() {

        String timestamp = LocalDateTime.now()
                .format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMddHHmmssSSS"));

        return "TKT-" + timestamp;
    }

    private LocalDateTime calculateDueDate(
            LocalDateTime createdAt,
            Priority priority) {

        return switch (priority) {

            case LOW ->
                    createdAt.plusHours(72);

            case MEDIUM ->
                    createdAt.plusHours(48);

            case HIGH ->
                    createdAt.plusHours(24);

            case CRITICAL ->
                    createdAt.plusHours(8);
        };
    }

    private void validateStatusTransition(
            TicketStatus oldStatus,
            TicketStatus newStatus) {

        boolean valid = switch (oldStatus) {

            case OPEN ->
                    newStatus == TicketStatus.IN_PROGRESS;

            case IN_PROGRESS ->
                    newStatus == TicketStatus.WAITING_FOR_STUDENT
                    || newStatus == TicketStatus.RESOLVED;

            case WAITING_FOR_STUDENT ->
                    newStatus == TicketStatus.IN_PROGRESS;

            case RESOLVED ->
                    newStatus == TicketStatus.CLOSED
                    || newStatus == TicketStatus.IN_PROGRESS;

            case CLOSED ->
                    false;
        };

        if (!valid) {

            throw new RuntimeException(
                    "Invalid status transition from "
                            + oldStatus
                            + " to "
                            + newStatus);
        }
    }
}