package com.benhvien.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterReq(
        @NotBlank @Size(min = 3, max = 100) String username,
        @NotBlank @Size(min = 6, max = 100) String matKhau,
        @NotBlank String hoTen,
        String email,
        String soDienThoai,
        /** Mặc định BENH_NHAN nếu không truyền (ADMIN | BAC_SI | BENH_NHAN). */
        String vaiTro
) {}
