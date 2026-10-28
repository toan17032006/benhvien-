package com.benhvien.web;

import com.benhvien.domain.YTa;
import com.benhvien.repository.KhoaRepository;
import com.benhvien.repository.YTaRepository;
import com.benhvien.web.dto.YTaReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/y-ta")
public class YTaController {

    private final YTaRepository repo;
    private final KhoaRepository khoaRepo;

    public YTaController(YTaRepository repo, KhoaRepository khoaRepo) {
        this.repo = repo;
        this.khoaRepo = khoaRepo;
    }

    @GetMapping
    public List<YTa> list(@RequestParam(required = false) Long khoaId) {
        return khoaId == null ? repo.findAll() : repo.findByKhoaIdOrderById(khoaId);
    }

    @GetMapping("/{id}")
    public YTa get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy y tá " + id));
    }

    @PostMapping
    @Transactional
    public YTa create(@Valid @RequestBody YTaReq r) {
        YTa e = new YTa();
        apply(e, r);
        e = repo.save(e);
        if (e.getMaYTa() == null || e.getMaYTa().isBlank()) {
            e.setMaYTa("YT-" + String.format("%04d", e.getId()));
        }
        return repo.save(e);
    }

    @PutMapping("/{id}")
    @Transactional
    public YTa update(@PathVariable Long id, @Valid @RequestBody YTaReq r) {
        YTa e = get(id);
        apply(e, r);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        repo.delete(get(id));
    }

    private void apply(YTa e, YTaReq r) {
        e.setMaYTa(r.maYTa());
        e.setHoTen(r.hoTen());
        e.setTrinhDo(r.trinhDo());
        e.setSoDienThoai(r.soDienThoai());
        e.setEmail(r.email());
        e.setKhoa(khoaRepo.findById(r.khoaId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy khoa " + r.khoaId())));
    }
}
