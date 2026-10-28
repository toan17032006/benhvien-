package com.benhvien.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
