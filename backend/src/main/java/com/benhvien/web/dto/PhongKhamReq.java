package com.benhvien.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PhongKhamReq(
        String maPhong,
        @NotBlank String tenPhong,
        @NotNull Long khoaId
) {}
