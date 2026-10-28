package com.benhvien.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NhapVienTuLanKhamReq(
        @NotNull Long giuongBenhId,
        @NotBlank String lyDoNhapVien,
        String ghiChu
) {}
