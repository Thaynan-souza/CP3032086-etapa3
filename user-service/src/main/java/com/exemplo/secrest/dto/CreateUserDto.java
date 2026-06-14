package com.exemplo.secrest.dto;

import com.exemplo.secrest.enums.RoleName;

public record CreateUserDto(String name, String email, String password, RoleName role) {
}