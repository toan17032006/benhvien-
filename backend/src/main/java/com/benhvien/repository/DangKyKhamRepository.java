package com.benhvien.repository;

import com.benhvien.domain.DangKyKham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DangKyKhamRepository extends JpaRepository<DangKyKham, Long> {

    List<DangKyKham> findByNguoiDungIdOrderByThoiGianDangKyDesc(Long nguoiDungId);

    List<DangKyKham> findByNguoiDungIdAndBenhVienIdOrderByThoiGianDangKyDesc(Long nguoiDungId, Long benhVienId);
}
