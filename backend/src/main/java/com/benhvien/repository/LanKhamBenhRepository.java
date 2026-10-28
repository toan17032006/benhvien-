package com.benhvien.repository;

import com.benhvien.domain.LanKhamBenh;
import com.benhvien.domain.TrangThaiKham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LanKhamBenhRepository extends JpaRepository<LanKhamBenh, Long> {
    List<LanKhamBenh> findByBenhNhanIdOrderByThoiGianKhamDesc(Long benhNhanId);
    List<LanKhamBenh> findByLichLamViecIdOrderByThoiGianKhamDesc(Long lichLamViecId);
    List<LanKhamBenh> findByLanNhapVienIdOrderByThoiGianKhamAsc(Long lanNhapVienId);
    List<LanKhamBenh> findByTrangThaiOrderByThoiGianKhamDesc(TrangThaiKham trangThai);
    List<LanKhamBenh> findAllByOrderByThoiGianKhamDesc();
}
