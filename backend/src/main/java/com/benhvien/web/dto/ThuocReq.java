package com.benhvien.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ThuocReq(
        String maThuoc,
        @NotBlank String tenThuoc,
        String donViTinh,
        @DecimalMin("0") BigDecimal donGia,
        @Min(0) Integer soLuongTon,
        String congDung
) {}
