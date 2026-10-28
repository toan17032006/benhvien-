package com.benhvien.repository;

import com.benhvien.domain.LichLamViec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LichLamViecRepository extends JpaRepository<LichLamViec, Long> {
    List<LichLamViec> findByBacSiIdOrderByNgayDesc(Long bacSiId);
    List<LichLamViec> findByPhongKhamIdOrderByNgayDesc(Long phongKhamId);
    List<LichLamViec> findByNgayOrderByIdDesc(LocalDate ngay);
    List<LichLamViec> findAllByOrderByNgayDescIdDesc();
}
