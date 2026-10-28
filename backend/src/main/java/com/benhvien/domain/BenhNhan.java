package com.benhvien.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Bệnh Nhân — 1 bệnh nhân có nhiều Lần khám bệnh (quan hệ "Có" 1-N). */
@Entity
@Table(name = "benh_nhan")
@Getter
@Setter
@NoArgsConstructor
public class BenhNhan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maBenhNhan;

    private String hoTen;

    private java.time.LocalDate ngaySinh;

    private String gioiTinh;

    private String soDienThoai;

    private String diaChi;

    private String tienSuBenhAn;
}
