package com.benhvien.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * Xét nghiệm — thuộc 1 Lần khám bệnh và được "Thực hiện" trên 1 Lịch làm việc
 * (bác sĩ/kỹ thuật viên trực ca thực hiện xét nghiệm).
 */
@Entity
@Table(name = "xet_nghiem")
@Getter
@Setter
@NoArgsConstructor
public class XetNghiem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maXetNghiem;

    private String tenXetNghiem;

    private String loaiXetNghiem;

    private LocalDateTime thoiGian;

    private String ketQua;

    @Enumerated(EnumType.STRING)
    private TrangThaiXetNghiem trangThai = TrangThaiXetNghiem.CHO_KET_QUA;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lan_kham_benh_id")
    private LanKhamBenh lanKhamBenh;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lich_lam_viec_id")
    private LichLamViec lichLamViec;
}
