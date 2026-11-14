package com.benhvien.repository;

import com.benhvien.domain.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Long> {

    List<DanhGia> findByBenhVienIdOrderByThoiGianDesc(Long benhVienId);

    boolean existsByBenhVienId(Long benhVienId);
}
