package com.benhvien.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record LichLamViecReq(
        @NotNull LocalDate ngay,
        String ghiChu,
        @NotNull Long bacSiId,
        @NotNull Long phongKhamId,
        List<Long> caTrucIds
) {}
