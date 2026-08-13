package com.example.ticketsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticketsystem.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenant> {

    @Select("SELECT id FROM sys_tenant WHERE status = 1")
    List<Long> selectEnabledTenantIds();
}
