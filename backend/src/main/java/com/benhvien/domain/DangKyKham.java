package com.benhvien.domain;

import jakarta.persistence.Column;
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

/** Đăng Ký Khám — bệnh nhân đăng ký khám bệnh tại một Bệnh viện từ bản đồ. */
@Entity
@Table(name = "dang_ky_kham")
@Getter
@Setter
@NoArgsConstructor
public class DangKyKham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "benh_vien_id")
    private BenhVien benhVien;

    /** Tài khoản bệnh nhân đã đăng ký. */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    /** Bệnh lý / triệu chứng mong muốn khám. */
    @Column(length = 500)
    private String benhLy;

    /** Thời gian hẹn mong muốn. */
    @Column(nullable = false)
    private LocalDateTime thoiGianHen;

    private String ghiChu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TrangThaiDangKy trangThai;

    @Column(nullable = false)
    private LocalDateTime thoiGianDangKy;
}
