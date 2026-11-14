package com.benhvien.web.dto;

public record DangKyKhamView(
        Long id,
        String tenBenhVien,
        String benhLy,
        String thoiGianHen,
        String ghiChu,
        String trangThai,
        String thoiGianDangKy
) {}
