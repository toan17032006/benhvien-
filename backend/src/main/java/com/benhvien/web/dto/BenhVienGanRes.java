package com.benhvien.web.dto;

import java.util.List;

/** Bệnh viện gợi ý trên bản đồ — ảnh, trạng thái mở cửa, thời gian đi đường, khoảng cách. */
public record BenhVienGanRes(
        Long id,
        String ten,
        String diaChi,
        String soDienThoai,
        String email,
        Double viDo,
        Double kinhDo,
        List<String> anhChinh,
        String gioMoCua,
        String gioDongCua,
        boolean dangMoCua,
        Double khoangCachKm,
        /** Quãng đường đi đường bộ thực tế (km — từ OSRM; null nếu không có vị trí). */
        Double quangDuongKm,
        /** Thời gian di chuyển (phút, đi xe máy/ô tô theo đường bộ). */
        Double thoiGianPhut,
        /** true nếu thời gian là ước lượng (OSRM không phản hồi), false nếu là dữ liệu đường bộ thật. */
        boolean thoiGianUocLuong,
        List<KhoaPhuHopRes> khoaPhuHop,
        Double trungBinhSao,
        int soDanhGia,
        int soKhoa,
        int soBacSi,
        int soYTa
) {}
