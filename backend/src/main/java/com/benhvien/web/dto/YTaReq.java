package com.benhvien.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record YTaReq(
        String maYTa,
        @NotBlank String hoTen,
        String trinhDo,
        String soDienThoai,
        String email,
        @NotNull Long khoaId
) {}
