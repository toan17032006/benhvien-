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

import java.time.LocalDate;

/**
 * Lịch làm việc — 1 lịch gắn với 1 Bác sĩ + 1 Phòng khám + ngày;
 * quan hệ N-N với Ca trực (bảng lich_ca_truc);
 * Lần khám bệnh "Thuộc" 1 Lịch làm việc; Xét nghiệm "Thực hiện" trên 1 Lịch làm việc.
 */
@Entity
@Table(name = "lich_lam_viec")
@Getter
@Setter
@NoArgsConstructor
public class LichLamViec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate ngay;

    private String ghiChu;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bac_si_id")
    private BacSi bacSi;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "phong_kham_id")
    private PhongKham phongKham;
}
