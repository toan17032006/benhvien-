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

/** Bảng trung gian N-N giữa Lịch làm việc và Ca trực. */
@Entity
@Table(name = "lich_ca_truc",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lich_lam_viec_id", "ca_truc_id"}))
@Getter
@Setter
@NoArgsConstructor
public class LichCaTruc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lich_lam_viec_id")
    private LichLamViec lichLamViec;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ca_truc_id")
    private CaTruc caTruc;
}
