package com.benhvien.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DonThuocReq(
        String maDonThuoc,
        LocalDateTime ngayKe,
        String ghiChu,
        @NotNull Long lanKhamBenhId
) {}
