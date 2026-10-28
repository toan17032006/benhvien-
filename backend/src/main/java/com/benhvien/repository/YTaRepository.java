package com.benhvien.repository;

import com.benhvien.domain.YTa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YTaRepository extends JpaRepository<YTa, Long> {
    List<YTa> findByKhoaIdOrderById(Long khoaId);
}
