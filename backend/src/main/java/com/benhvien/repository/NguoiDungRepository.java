package com.benhvien.repository;

import com.benhvien.domain.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {

    Optional<NguoiDung> findByUsername(String username);

    Optional<NguoiDung> findByEmail(String email);

    boolean existsByUsername(String username);
}
