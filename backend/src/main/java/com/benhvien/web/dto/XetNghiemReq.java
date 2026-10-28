package com.benhvien.web.dto;

import com.benhvien.domain.TrangThaiXetNghiem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record XetNghiemReq(
        String maXetNghiem,
        @NotBlank String tenXetNghiem,
        String loaiXetNghiem,
        LocalDateTime thoiGian,
        String ketQua,
        TrangThaiXetNghiem trangThai,
        @NotNull Long lanKhamBenhId,
        Long lichLamViecId
) {}
