package com.benhvien.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Chi tiết Thuốc — bảng trung gian N-N giữa Đơn thuốc và Thuốc,
 * mang thuộc tính số lượng, liều dùng và "Ghi chú thuốc" (có trong sơ đồ ERD).
 */
@Entity
@Table(name = "chi_tiet_thuoc")
@Getter
@Setter
@NoArgsConstructor
public class ChiTietThuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer soLuong;

    private String lieuDung;

    private String ghiChu;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "don_thuoc_id")
    private DonThuoc donThuoc;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "thuoc_id")
    private Thuoc thuoc;
}
