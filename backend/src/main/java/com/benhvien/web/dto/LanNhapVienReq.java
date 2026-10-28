package com.benhvien.web.dto;

import com.benhvien.domain.TrangThaiNhapVien;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record LanNhapVienReq(
        String maNhapVien,
        LocalDateTime ngayNhap,
        LocalDateTime ngayXuat,
        String lyDoNhapVien,
        String ghiChu,
        TrangThaiNhapVien trangThai,
        @NotNull Long giuongBenhId,
        List<Long> yTaIds
) {}
