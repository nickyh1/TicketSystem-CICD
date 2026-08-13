package com.example.ticketsystem.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketStatusTest {

    @Test
    void allowedTransitions_followWorkflowRules() {
        assertTrue(TicketStatus.PENDING.canTransitionTo(TicketStatus.PROCESSING));
        assertTrue(TicketStatus.PROCESSING.canTransitionTo(TicketStatus.PENDING));
        assertTrue(TicketStatus.PROCESSING.canTransitionTo(TicketStatus.RESOLVED));
        assertTrue(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.CLOSED));
    }

    @Test
    void forbiddenTransitions_areRejected() {
        assertFalse(TicketStatus.PENDING.canTransitionTo(TicketStatus.CLOSED));
        assertFalse(TicketStatus.PROCESSING.canTransitionTo(TicketStatus.CLOSED));
        assertFalse(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.PROCESSING));
        assertFalse(TicketStatus.CLOSED.canTransitionTo(TicketStatus.PENDING));
    }
}
