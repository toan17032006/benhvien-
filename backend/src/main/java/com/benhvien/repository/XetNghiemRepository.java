package com.benhvien.repository;

import com.benhvien.domain.XetNghiem;
import com.benhvien.domain.TrangThaiXetNghiem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface XetNghiemRepository extends JpaRepository<XetNghiem, Long> {
    List<XetNghiem> findByLanKhamBenhIdOrderByIdDesc(Long lanKhamBenhId);
    List<XetNghiem> findByLichLamViecIdOrderByIdDesc(Long lichLamViecId);
    List<XetNghiem> findByTrangThaiOrderByIdDesc(TrangThaiXetNghiem trangThai);
    List<XetNghiem> findAllByOrderByIdDesc();
}
