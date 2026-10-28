package com.benhvien.repository;

import com.benhvien.domain.LichCaTruc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LichCaTrucRepository extends JpaRepository<LichCaTruc, Long> {
    List<LichCaTruc> findByLichLamViecIdOrderByIdAsc(Long lichLamViecId);
    List<LichCaTruc> findByCaTrucIdOrderByIdDesc(Long caTrucId);
    Optional<LichCaTruc> findByLichLamViecIdAndCaTrucId(Long lichLamViecId, Long caTrucId);
}
