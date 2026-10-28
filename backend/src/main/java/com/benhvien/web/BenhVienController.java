package com.benhvien.web;

import com.benhvien.domain.BenhVien;
import com.benhvien.repository.BenhVienRepository;
import com.benhvien.web.dto.BenhVienReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/benh-vien")
public class BenhVienController {

    private final BenhVienRepository repo;

    public BenhVienController(BenhVienRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<BenhVien> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public BenhVien get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy bệnh viện " + id));
    }

    @PostMapping
    @Transactional
    public BenhVien create(@Valid @RequestBody BenhVienReq r) {
        BenhVien e = new BenhVien();
        apply(e, r);
        return repo.save(e);
    }

    @PutMapping("/{id}")
    @Transactional
    public BenhVien update(@PathVariable Long id, @Valid @RequestBody BenhVienReq r) {
        BenhVien e = get(id);
        apply(e, r);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        repo.delete(get(id));
    }

    private void apply(BenhVien e, BenhVienReq r) {
        e.setTen(r.ten());
        e.setDiaChi(r.diaChi());
        e.setSoDienThoai(r.soDienThoai());
        e.setEmail(r.email());
    }
}
