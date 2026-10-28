package com.benhvien.web;

import com.benhvien.domain.ChiTietThuoc;
import com.benhvien.repository.ChiTietThuocRepository;
import com.benhvien.repository.DonThuocRepository;
import com.benhvien.repository.ThuocRepository;
import com.benhvien.web.dto.ChiTietThuocReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/chi-tiet-thuoc")
public class ChiTietThuocController {

    private final ChiTietThuocRepository repo;
    private final DonThuocRepository donThuocRepo;
    private final ThuocRepository thuocRepo;

    public ChiTietThuocController(ChiTietThuocRepository repo, DonThuocRepository donThuocRepo,
                                  ThuocRepository thuocRepo) {
        this.repo = repo;
        this.donThuocRepo = donThuocRepo;
        this.thuocRepo = thuocRepo;
    }

    @GetMapping
    public List<ChiTietThuoc> list(@RequestParam(required = false) Long donThuocId,
                                   @RequestParam(required = false) Long thuocId) {
        if (donThuocId != null) {
            return repo.findByDonThuocIdOrderByIdAsc(donThuocId);
        }
        if (thuocId != null) {
            return repo.findByThuocIdOrderByIdDesc(thuocId);
        }
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ChiTietThuoc get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy chi tiết thuốc " + id));
    }

    @PostMapping
    @Transactional
    public ChiTietThuoc create(@Valid @RequestBody ChiTietThuocReq r) {
        if (repo.findByDonThuocIdAndThuocId(r.donThuocId(), r.thuocId()).isPresent()) {
            throw new IllegalArgumentException("Thuốc này đã có trong đơn, hãy sửa số lượng ở dòng hiện có.");
        }
        ChiTietThuoc e = new ChiTietThuoc();
        apply(e, r);
        return repo.save(e);
    }

    @PutMapping("/{id}")
    @Transactional
    public ChiTietThuoc update(@PathVariable Long id, @Valid @RequestBody ChiTietThuocReq r) {
        ChiTietThuoc e = get(id);
        apply(e, r);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        repo.delete(get(id));
    }

    private void apply(ChiTietThuoc e, ChiTietThuocReq r) {
        e.setSoLuong(r.soLuong());
        e.setLieuDung(r.lieuDung());
        e.setGhiChu(r.ghiChu());
        e.setDonThuoc(donThuocRepo.findById(r.donThuocId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn thuốc " + r.donThuocId())));
        e.setThuoc(thuocRepo.findById(r.thuocId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thuốc " + r.thuocId())));
    }
}
