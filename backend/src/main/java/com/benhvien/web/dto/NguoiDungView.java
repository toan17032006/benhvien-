package com.benhvien.web.dto;

import com.benhvien.domain.VaiTro;

/** Thông tin người dùng trả về cho frontend — không bao gồm mật khẩu. */
public record NguoiDungView(
        Long id,
        String username,
        String hoTen,
        String email,
        String soDienThoai,
        VaiTro vaiTro,
        String anhDaiDien
) {}
