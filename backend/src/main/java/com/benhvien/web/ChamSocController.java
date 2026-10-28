package com.benhvien.web;

import com.benhvien.domain.ChamSoc;
import com.benhvien.repository.ChamSocRepository;
import com.benhvien.repository.LanNhapVienRepository;
import com.benhvien.repository.YTaRepository;
import com.benhvien.web.dto.ChamSocReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/** Quan hệ "Chăm sóc" N-N giữa Y tá và Lần Nhập Viện. */
@RestController
@RequestMapping("/api/cham-soc")
public class ChamSocController {

    private final ChamSocRepository repo;
    private final YTaRepository yTaRepo;
    private final LanNhapVienRepository lanNhapVienRepo;

    public ChamSocController(ChamSocRepository repo, YTaRepository yTaRepo, LanNhapVienRepository lanNhapVienRepo) {
        this.repo = repo;
        this.yTaRepo = yTaRepo;
        this.lanNhapVienRepo = lanNhapVienRepo;
    }

    @GetMapping
    public List<ChamSoc> list(@RequestParam(required = false) Long lanNhapVienId,
                              @RequestParam(required = false) Long yTaId) {
        if (lanNhapVienId != null) {
            return repo.findByLanNhapVienIdOrderByIdAsc(lanNhapVienId);
        }
        if (yTaId != null) {
            return repo.findByYTaIdOrderByIdDesc(yTaId);
        }
        return repo.findAll();
    }

    @PostMapping
    @Transactional
    public ChamSoc create(@Valid @RequestBody ChamSocReq r) {
        if (repo.findByYTaIdAndLanNhapVienId(r.yTaId(), r.lanNhapVienId()).isPresent()) {
            throw new IllegalArgumentException("Y tá này đã được phân công chăm sóc lần nhập viện đó.");
        }
        ChamSoc e = new ChamSoc();
        e.setYTa(yTaRepo.findById(r.yTaId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy y tá " + r.yTaId())));
        e.setLanNhapVien(lanNhapVienRepo.findById(r.lanNhapVienId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lần nhập viện " + r.lanNhapVienId())));
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        repo.delete(repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phân công chăm sóc " + id)));
    }
}
