package com.benhvien.web;

import com.benhvien.domain.CaTruc;
import com.benhvien.repository.CaTrucRepository;
import com.benhvien.web.dto.CaTrucReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/ca-truc")
public class CaTrucController {

    private final CaTrucRepository repo;

    public CaTrucController(CaTrucRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<CaTruc> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public CaTruc get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy ca trực " + id));
    }

    @PostMapping
    @Transactional
    public CaTruc create(@Valid @RequestBody CaTrucReq r) {
        CaTruc e = new CaTruc();
        apply(e, r);
        e = repo.save(e);
        if (e.getMaCa() == null || e.getMaCa().isBlank()) {
            e.setMaCa("CA-" + String.format("%03d", e.getId()));
        }
        return repo.save(e);
    }

    @PutMapping("/{id}")
    @Transactional
    public CaTruc update(@PathVariable Long id, @Valid @RequestBody CaTrucReq r) {
        CaTruc e = get(id);
        apply(e, r);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        repo.delete(get(id));
    }

    private void apply(CaTruc e, CaTrucReq r) {
        e.setMaCa(r.maCa());
        e.setTenCa(r.tenCa());
        e.setGioBatDau(r.gioBatDau());
        e.setGioKetThuc(r.gioKetThuc());
    }
}
