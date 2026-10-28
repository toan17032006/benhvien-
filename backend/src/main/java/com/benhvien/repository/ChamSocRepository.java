package com.benhvien.repository;

import com.benhvien.domain.ChamSoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChamSocRepository extends JpaRepository<ChamSoc, Long> {
    List<ChamSoc> findByLanNhapVienIdOrderByIdAsc(Long lanNhapVienId);
    List<ChamSoc> findByYTaIdOrderByIdDesc(Long yTaId);
    Optional<ChamSoc> findByYTaIdAndLanNhapVienId(Long yTaId, Long lanNhapVienId);
}
