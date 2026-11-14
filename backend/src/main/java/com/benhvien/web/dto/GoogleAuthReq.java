package com.benhvien.web.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthReq(@NotBlank String idToken) {}
