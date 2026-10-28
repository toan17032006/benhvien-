package com.benhvien.web.dto;

import jakarta.validation.constraints.NotNull;

public record ChamSocReq(
        @NotNull Long yTaId,
        @NotNull Long lanNhapVienId
) {}
