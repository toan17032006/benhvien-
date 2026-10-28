package com.benhvien.repository;

import com.benhvien.domain.Khoa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KhoaRepository extends JpaRepository<Khoa, Long> {
    List<Khoa> findByBenhVienIdOrderById(Long benhVienId);
}
