package com.benhvien.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ChiTietThuocReq(
        @NotNull @Min(1) Integer soLuong,
        String lieuDung,
        String ghiChu,
        @NotNull Long donThuocId,
        @NotNull Long thuocId
) {}
