package com.example.ticketsystem.service;

import java.util.Map;

public interface TicketStatsService {
    Map<String, Integer> getStats();
    void refreshCache();
}
