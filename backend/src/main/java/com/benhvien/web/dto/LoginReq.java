package com.benhvien.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginReq(
        @NotBlank String username,
        @NotBlank String matKhau
) {}
