package com.benhvien.web.dto;

public record DanhGiaView(
        Long id,
        String hoTen,
        String vaiTro,
        int soSao,
        String noiDung,
        String thoiGian
) {}
