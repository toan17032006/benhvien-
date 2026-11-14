package com.benhvien.web.dto;

import java.util.List;

/** Đánh giá (review) của một bệnh viện: điểm trung bình + danh sách nhận xét. */
public record DanhGiaRes(
        Long benhVienId,
        String tenBenhVien,
        Double trungBinhSao,
        int soDanhGia,
        List<DanhGiaView> danhSach
) {}
