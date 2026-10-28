package com.benhvien.repository;

import com.benhvien.domain.Thuoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThuocRepository extends JpaRepository<Thuoc, Long> {
    List<Thuoc> findByTenThuocContainingIgnoreCaseOrMaThuocContainingIgnoreCaseOrderById(String ten, String ma);
}
