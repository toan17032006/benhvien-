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

/** Giường Bệnh — N giường thuộc 1 Khoa; 1 Lần Nhập Viện nằm trên 1 giường. */
@Entity
@Table(name = "giuong_benh")
@Getter
@Setter
@NoArgsConstructor
public class GiuongBenh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maGiuong;

    private String loaiGiuong;

    @Enumerated(EnumType.STRING)
    private TrangThaiGiuong trangThai = TrangThaiGiuong.TRONG;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "khoa_id")
    private Khoa khoa;
}
