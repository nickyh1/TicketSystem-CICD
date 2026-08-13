package com.example.ticketsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticketsystem.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {
    @Select("SELECT status, COUNT(*) as cnt FROM ticket WHERE tenant_id = #{tenantId} AND deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus(@Param("tenantId") Long tenantId);

    @Select("SELECT priority, COUNT(*) as cnt FROM ticket WHERE tenant_id = #{tenantId} AND deleted = 0 GROUP BY priority")
    List<Map<String, Object>> countByPriority(@Param("tenantId") Long tenantId);

    @Select("SELECT AVG(TIMESTAMPDIFF(HOUR, create_time, update_time)) as avg_hours " +
            "FROM ticket WHERE tenant_id = #{tenantId} AND deleted = 0 AND status IN ('RESOLVED', 'CLOSED')")
    Double avgProcessingHours(@Param("tenantId") Long tenantId);

}
