package com.benhvien.repository;

import com.benhvien.domain.ChiTietThuoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChiTietThuocRepository extends JpaRepository<ChiTietThuoc, Long> {
    List<ChiTietThuoc> findByDonThuocIdOrderByIdAsc(Long donThuocId);
    List<ChiTietThuoc> findByThuocIdOrderByIdDesc(Long thuocId);
    Optional<ChiTietThuoc> findByDonThuocIdAndThuocId(Long donThuocId, Long thuocId);
    void deleteByDonThuocId(Long donThuocId);
}
