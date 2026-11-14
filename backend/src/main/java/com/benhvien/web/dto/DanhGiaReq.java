package com.benhvien.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DanhGiaReq(
        @NotNull @Min(1) @Max(5) Integer soSao,
        @NotBlank String noiDung
) {}
