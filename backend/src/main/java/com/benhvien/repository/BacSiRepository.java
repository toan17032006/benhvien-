package com.benhvien.repository;

import com.benhvien.domain.BacSi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BacSiRepository extends JpaRepository<BacSi, Long> {
    List<BacSi> findByKhoaIdOrderById(Long khoaId);
    List<BacSi> findByHoTenContainingIgnoreCaseOrMaBacSiContainingIgnoreCaseOrderById(String hoTen, String maBacSi);
}
