package com.benhvien.web.dto;

import java.util.List;

/** Bệnh viện gợi ý trên bản đồ — sắp xếp theo khoảng cách gần nhất từ vị trí bệnh nhân. */
public record BenhVienGanRes(
        Long id,
        String ten,
        String diaChi,
        String soDienThoai,
        String email,
        Double viDo,
        Double kinhDo,
        Double khoangCachKm,
        List<KhoaPhuHopRes> khoaPhuHop,
        int soKhoa,
        int soBacSi,
        int soYTa
) {}
