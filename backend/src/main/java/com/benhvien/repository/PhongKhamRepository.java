package com.benhvien.repository;

import com.benhvien.domain.PhongKham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhongKhamRepository extends JpaRepository<PhongKham, Long> {
    List<PhongKham> findByKhoaIdOrderById(Long khoaId);
}
