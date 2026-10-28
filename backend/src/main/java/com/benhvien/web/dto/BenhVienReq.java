package com.benhvien.web.dto;

import jakarta.validation.constraints.NotBlank;

public record BenhVienReq(
        @NotBlank String ten,
        String diaChi,
        String soDienThoai,
        String email
) {}
