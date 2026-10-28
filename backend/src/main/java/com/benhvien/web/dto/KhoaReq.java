package com.benhvien.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KhoaReq(
        String maKhoa,
        @NotBlank String tenKhoa,
        String moTa,
        @NotNull Long benhVienId
) {}
