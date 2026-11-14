package com.benhvien.web.dto;

import java.util.List;

/** Nhân sự (bác sĩ + y tá) của một bệnh viện trên bản đồ. */
public record NhanSuRes(
        Long benhVienId,
        String tenBenhVien,
        List<BacSiView> bacSi,
        List<YTaView> yTa
) {}
