package com.benhvien.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Thuốc — tham chiếu bởi Chi tiết Thuốc trong mỗi Đơn thuốc. */
@Entity
@Table(name = "thuoc")
@Getter
@Setter
@NoArgsConstructor
public class Thuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maThuoc;

    private String tenThuoc;

    private String donViTinh;

    private BigDecimal donGia;

    private Integer soLuongTon;

    private String congDung;
}
