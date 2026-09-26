package com.assignment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.assignment.entity.Ticket;
import com.assignment.entity.TicketActivity;

@Repository
public interface TicketActivityRepository extends JpaRepository<TicketActivity, Long> {

    List<TicketActivity> findByTicketOrderByCreatedAtAsc(Ticket ticket);
}