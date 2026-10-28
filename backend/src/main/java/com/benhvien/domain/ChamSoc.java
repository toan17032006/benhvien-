package com.benhvien.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Bảng trung gian "Chăm sóc" (N-N): Y tá chăm sóc Lần Nhập Viện. */
@Entity
@Table(name = "cham_soc",
        uniqueConstraints = @UniqueConstraint(columnNames = {"y_ta_id", "lan_nhap_vien_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ChamSoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "y_ta_id")
    private YTa yTa;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lan_nhap_vien_id")
    private LanNhapVien lanNhapVien;
}
