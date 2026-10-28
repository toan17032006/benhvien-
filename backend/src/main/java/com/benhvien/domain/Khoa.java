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

/** Khoa — N khoa thuộc 1 Bệnh Viện; là trung tâm của sơ đồ ERD. */
@Entity
@Table(name = "khoa")
@Getter
@Setter
@NoArgsConstructor
public class Khoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maKhoa;

    private String tenKhoa;

    private String moTa;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "benh_vien_id")
    private BenhVien benhVien;
}
