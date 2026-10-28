package com.benhvien.web.dto;

import jakarta.validation.constraints.NotNull;

public record LichCaTrucReq(
        @NotNull Long lichLamViecId,
        @NotNull Long caTrucId
) {}
