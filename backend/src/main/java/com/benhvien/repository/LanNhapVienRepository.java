package com.benhvien.repository;

import com.benhvien.domain.LanNhapVien;
import com.benhvien.domain.TrangThaiNhapVien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LanNhapVienRepository extends JpaRepository<LanNhapVien, Long> {
    List<LanNhapVien> findByTrangThaiOrderByNgayNhapDesc(TrangThaiNhapVien trangThai);
    List<LanNhapVien> findByGiuongBenhIdOrderByNgayNhapDesc(Long giuongBenhId);
    List<LanNhapVien> findAllByOrderByNgayNhapDesc();
}
