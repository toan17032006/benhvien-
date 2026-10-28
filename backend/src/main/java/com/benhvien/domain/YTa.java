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

/** Y tá — thuộc 1 Khoa; quan hệ N-N "Chăm sóc" với Lần Nhập Viện. */
@Entity
@Table(name = "y_ta")
@Getter
@Setter
@NoArgsConstructor
public class YTa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maYTa;

    private String hoTen;

    private String trinhDo;

    private String soDienThoai;

    private String email;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "khoa_id")
    private Khoa khoa;
}
