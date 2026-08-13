package com.example.ticketsystem.service;


import com.example.ticketsystem.dto.LoginRequest;
import com.example.ticketsystem.dto.RegisterRequest;
import com.example.ticketsystem.dto.UserVO;

public interface SysUserService {
    void register(RegisterRequest request);
    String login(LoginRequest request);
    UserVO getUserInfo(Long userId);
}
