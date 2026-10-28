package com.benhvien.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Ca trực — quan hệ N-N với Lịch làm việc qua bảng lich_ca_truc. */
@Entity
@Table(name = "ca_truc")
@Getter
@Setter
@NoArgsConstructor
public class CaTruc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maCa;

    private String tenCa;

    private java.time.LocalTime gioBatDau;

    private java.time.LocalTime gioKetThuc;
}
