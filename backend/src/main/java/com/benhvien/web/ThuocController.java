package com.benhvien.web;

import com.benhvien.domain.Thuoc;
import com.benhvien.repository.ThuocRepository;
import com.benhvien.web.dto.ThuocReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/thuoc")
public class ThuocController {

    private final ThuocRepository repo;

    public ThuocController(ThuocRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Thuoc> list(@RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            return repo.findByTenThuocContainingIgnoreCaseOrMaThuocContainingIgnoreCaseOrderById(q, q);
        }
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Thuoc get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy thuốc " + id));
    }

    @PostMapping
    @Transactional
    public Thuoc create(@Valid @RequestBody ThuocReq r) {
        Thuoc e = new Thuoc();
        apply(e, r);
        e = repo.save(e);
        if (e.getMaThuoc() == null || e.getMaThuoc().isBlank()) {
            e.setMaThuoc("TH-" + String.format("%04d", e.getId()));
        }
        return repo.save(e);
    }

    @PutMapping("/{id}")
    @Transactional
    public Thuoc update(@PathVariable Long id, @Valid @RequestBody ThuocReq r) {
        Thuoc e = get(id);
        apply(e, r);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        repo.delete(get(id));
    }

    private void apply(Thuoc e, ThuocReq r) {
        e.setMaThuoc(r.maThuoc());
        e.setTenThuoc(r.tenThuoc());
        e.setDonViTinh(r.donViTinh());
        e.setDonGia(r.donGia());
        e.setSoLuongTon(r.soLuongTon());
        e.setCongDung(r.congDung());
    }
}
