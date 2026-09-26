package com.assignment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.assignment.entity.Ticket;
import com.assignment.entity.User;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStudent(User student);

    List<Ticket> findByAssignedTo(User staff);

    List<Ticket> findByAssignedToIsNull();

    Optional<Ticket> findByTicketNumber(String ticketNumber);
}