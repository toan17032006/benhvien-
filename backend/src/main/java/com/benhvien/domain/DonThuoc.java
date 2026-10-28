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

import java.time.LocalDateTime;

/** Đơn thuốc — 1 Lần khám bệnh có nhiều Đơn thuốc; mỗi đơn có nhiều Chi tiết Thuốc. */
@Entity
@Table(name = "don_thuoc")
@Getter
@Setter
@NoArgsConstructor
public class DonThuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maDonThuoc;

    private LocalDateTime ngayKe;

    private String ghiChu;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lan_kham_benh_id")
    private LanKhamBenh lanKhamBenh;
}
