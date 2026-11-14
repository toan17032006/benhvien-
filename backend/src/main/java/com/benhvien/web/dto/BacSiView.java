package com.benhvien.web.dto;

public record BacSiView(
        Long id,
        String maBacSi,
        String hoTen,
        String chuyenMon,
        String hocHam,
        String soDienThoai,
        String email,
        String tenKhoa
) {}
