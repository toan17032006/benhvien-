package com.benhvien.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DangKyKhamReq(
        @NotBlank String benhLy,
        @NotNull LocalDateTime thoiGianHen,
        String ghiChu
) {}
