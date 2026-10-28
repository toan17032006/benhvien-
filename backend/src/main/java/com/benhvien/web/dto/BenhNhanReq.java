package com.benhvien.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record BenhNhanReq(
        String maBenhNhan,
        @NotBlank String hoTen,
        LocalDate ngaySinh,
        String gioiTinh,
        String soDienThoai,
        String diaChi,
        String tienSuBenhAn
) {}
