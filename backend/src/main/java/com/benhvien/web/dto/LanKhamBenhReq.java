package com.benhvien.web.dto;

import com.benhvien.domain.TrangThaiKham;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record LanKhamBenhReq(
        String maLanKham,
        LocalDateTime thoiGianKham,
        String trieuChung,
        String chanDoan,
        TrangThaiKham trangThai,
        @NotNull Long benhNhanId,
        Long lichLamViecId,
        Long lanNhapVienId
) {}
