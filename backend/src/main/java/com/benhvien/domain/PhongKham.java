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

/** Phòng Khám — N phòng thuộc 1 Khoa; được dùng trong Lịch làm việc. */
@Entity
@Table(name = "phong_kham")
@Getter
@Setter
@NoArgsConstructor
public class PhongKham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maPhong;

    private String tenPhong;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "khoa_id")
    private Khoa khoa;
}
