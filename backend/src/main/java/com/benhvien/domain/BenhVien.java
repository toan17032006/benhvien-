package com.benhvien.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/** Bệnh Viện — thực thể gốc trong sơ đồ ERD. */
@Entity
@Table(name = "benh_vien")
@Getter
@Setter
@NoArgsConstructor
public class BenhVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ten;

    private String diaChi;

    private String soDienThoai;

    private String email;

    /** Vĩ độ (latitude) — dùng cho tính năng bản đồ & tìm bệnh viện gần nhất. */
    private Double viDo;

    /** Kinh độ (longitude) — dùng cho tính năng bản đồ & tìm bệnh viện gần nhất. */
    private Double kinhDo;

    /** Ảnh bệnh viện — nhiều URL cách nhau bằng dấu "|". */
    @Column(length = 1000)
    private String anhChinh;

    /** Giờ mở cửa (dùng cho trạng thái "Đang mở cửa / Đã đóng cửa" trên bản đồ). */
    private LocalTime gioMoCua;

    /** Giờ đóng cửa (nếu nhỏ hơn giờ mở cửa nghĩa là mở qua đêm / 24h). */
    private LocalTime gioDongCua;
}
