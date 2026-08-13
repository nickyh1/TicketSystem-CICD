package com.example.ticketsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ticketsystem.common.BusinessException;
import com.example.ticketsystem.common.TenantContext;
import com.example.ticketsystem.config.JwtUtils;
import com.example.ticketsystem.dto.*;
import com.example.ticketsystem.entity.SysUser;
import com.example.ticketsystem.mapper.SysPermissionMapper;
import com.example.ticketsystem.mapper.SysUserMapper;
import com.example.ticketsystem.mapper.SysUserRoleMapper;
import com.example.ticketsystem.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SysUserRoleMapper userRoleMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public void register(RegisterRequest request) {
        try {
            Long tenantId = request.getTenantId();
            TenantContext.setTenantId(tenantId);

            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, request.getUsername());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(400, "用户名已存在");
            }

            SysUser user = new SysUser();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setStatus(1);
            user.setTenantId(tenantId);
            userMapper.insert(user);

            userRoleMapper.insertUserRole(user.getId(), 2L);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public String login(LoginRequest request) {
        try {
            Long tenantId = request.getTenantId();
            TenantContext.setTenantId(tenantId);

            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, request.getUsername());
            SysUser user = userMapper.selectOne(wrapper);

            if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BusinessException(401, "用户名或密码错误");
            }

            if (user.getStatus() == 0) {
                throw new BusinessException(403, "账号已被禁用");
            }

            List<String> permissions = permissionMapper.selectPermKeysByUserId(user.getId());
            return jwtUtils.generateToken(user.getId(), user.getUsername(), permissions, user.getTenantId());
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
