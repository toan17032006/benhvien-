package com.benhvien.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalTime;

public record CaTrucReq(
        String maCa,
        @NotBlank String tenCa,
        LocalTime gioBatDau,
        LocalTime gioKetThuc
) {}
