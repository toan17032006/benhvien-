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
 * Lần Nhập Viện — nằm trên 1 Giường Bệnh ("Nằm");
 * được Y tá Chăm sóc (N-N qua cham_soc);
 * gắn với các Lần khám bệnh trong suốt thời gian điều trị nội trú ("Nhập viên").
 */
@Entity
@Table(name = "lan_nhap_vien")
@Getter
@Setter
@NoArgsConstructor
public class LanNhapVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maNhapVien;

    private LocalDateTime ngayNhap;

    private LocalDateTime ngayXuat;

    private String lyDoNhapVien;

    private String ghiChu;

    @Enumerated(EnumType.STRING)
    private TrangThaiNhapVien trangThai = TrangThaiNhapVien.DANG_DIEU_TRI;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "giuong_benh_id")
    private GiuongBenh giuongBenh;
}
