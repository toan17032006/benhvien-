package com.benhvien.web.dto;

import com.benhvien.domain.TrangThaiGiuong;
import jakarta.validation.constraints.NotNull;

public record GiuongBenhReq(
        String maGiuong,
        String loaiGiuong,
        TrangThaiGiuong trangThai,
        @NotNull Long khoaId
) {}
