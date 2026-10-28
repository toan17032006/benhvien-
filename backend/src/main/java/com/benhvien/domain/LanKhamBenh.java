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
 * Lần khám bệnh — thuộc 1 Bệnh Nhân ("Có"), gắn với 1 Lịch làm việc ("Thuộc"),
 * có thể dẫn tới 1 Lần Nhập Viện ("Nhập viên"); có nhiều Đơn thuốc và Xét nghiệm.
 */
@Entity
@Table(name = "lan_kham_benh")
@Getter
@Setter
@NoArgsConstructor
public class LanKhamBenh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maLanKham;

    private LocalDateTime thoiGianKham;

    private String trieuChung;

    private String chanDoan;

    @Enumerated(EnumType.STRING)
    private TrangThaiKham trangThai = TrangThaiKham.DANG_KHAM;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "benh_nhan_id")
    private BenhNhan benhNhan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lich_lam_viec_id")
    private LichLamViec lichLamViec;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lan_nhap_vien_id")
    private LanNhapVien lanNhapVien;
}
