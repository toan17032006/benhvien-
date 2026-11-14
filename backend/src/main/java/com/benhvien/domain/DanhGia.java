package com.benhvien.domain;

import jakarta.persistence.Column;
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

/** Đánh Giá — bệnh nhân viết review cho một Bệnh viện (số sao 1–5 + nhận xét). */
@Entity
@Table(name = "danh_gia")
@Getter
@Setter
@NoArgsConstructor
public class DanhGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "benh_vien_id")
    private BenhVien benhVien;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    @Column(nullable = false)
    private int soSao;

    @Column(length = 1000)
    private String noiDung;

    @Column(nullable = false)
    private LocalDateTime thoiGian;
}
