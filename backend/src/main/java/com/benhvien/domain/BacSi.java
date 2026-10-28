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

/** Bác Sĩ — thuộc 1 Khoa, có nhiều Lịch làm việc và thực hiện Lần khám bệnh. */
@Entity
@Table(name = "bac_si")
@Getter
@Setter
@NoArgsConstructor
public class BacSi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maBacSi;

    private String hoTen;

    private String chuyenMon;

    private String hocHam;

    private String soDienThoai;

    private String email;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "khoa_id")
    private Khoa khoa;
}
