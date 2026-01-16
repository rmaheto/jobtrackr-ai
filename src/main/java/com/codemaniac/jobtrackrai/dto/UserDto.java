package com.codemaniac.jobtrackrai.dto;

import com.codemaniac.jobtrackrai.enums.PlanCode;

public record UserDto(Long id, String email, String name, PlanCode plan) {}
