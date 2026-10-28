package com.benhvien.repository;

import com.benhvien.domain.BenhNhan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BenhNhanRepository extends JpaRepository<BenhNhan, Long> {
    List<BenhNhan> findByHoTenContainingIgnoreCaseOrMaBenhNhanContainingIgnoreCaseOrSoDienThoaiContainingOrderByIdDesc(String hoTen, String ma, String sdt);
}
