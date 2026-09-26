package com.assignment.repository;

import java.util.List;
import java.util.Locale.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.assignment.entity.Ticket;
import com.assignment.enums.Priority;
import com.assignment.enums.TicketStatus;

@Repository
public interface TicketRepository
        extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStudentId(Long studentId);

    List<Ticket> findByAssignedToId(Long staffId);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByPriority(Priority priority);

    long countByStatus(TicketStatus status);

    long countByCategory(Category category);
}