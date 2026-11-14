package com.benhvien.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalTime;

public record BenhVienReq(
        @NotBlank String ten,
        String diaChi,
        String soDienThoai,
        String email,
        Double viDo,
        Double kinhDo,
        /** Nhiều URL cách nhau bằng dấu "|". */
        String anhChinh,
        LocalTime gioMoCua,
        LocalTime gioDongCua
) {}
