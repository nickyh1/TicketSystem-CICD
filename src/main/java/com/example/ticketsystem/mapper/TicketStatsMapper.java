package com.example.ticketsystem.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TicketStatsMapper {

    @Insert("INSERT INTO ticket_stats (tenant_id, status, count) VALUES (#{tenantId}, #{status}, 1) " +
            "ON DUPLICATE KEY UPDATE count = count + 1")
    void increment(@Param("tenantId") Long tenantId, @Param("status") String status);

    @Insert("INSERT INTO ticket_stats (tenant_id, status, count) VALUES (#{tenantId}, #{status}, 0) " +
            "ON DUPLICATE KEY UPDATE count = IF(count > 0, count - 1, 0)")
    void decrement(@Param("tenantId") Long tenantId, @Param("status") String status);
}
