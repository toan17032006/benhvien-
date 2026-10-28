package com.benhvien.repository;

import com.benhvien.domain.GiuongBenh;
import com.benhvien.domain.TrangThaiGiuong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiuongBenhRepository extends JpaRepository<GiuongBenh, Long> {
    List<GiuongBenh> findByKhoaIdOrderById(Long khoaId);
    List<GiuongBenh> findByTrangThaiOrderById(TrangThaiGiuong trangThai);
    List<GiuongBenh> findByKhoaIdAndTrangThaiOrderById(Long khoaId, TrangThaiGiuong trangThai);
}
