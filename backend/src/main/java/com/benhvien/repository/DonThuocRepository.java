package com.benhvien.repository;

import com.benhvien.domain.DonThuoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonThuocRepository extends JpaRepository<DonThuoc, Long> {
    List<DonThuoc> findByLanKhamBenhIdOrderByNgayKeDesc(Long lanKhamBenhId);
}
