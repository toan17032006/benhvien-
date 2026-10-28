package com.benhvien.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BacSiReq(
        String maBacSi,
        @NotBlank String hoTen,
        String chuyenMon,
        String hocHam,
        String soDienThoai,
        String email,
        @NotNull Long khoaId
) {}
