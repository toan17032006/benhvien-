package com.benhvien.web;

import com.benhvien.domain.BenhNhan;
import com.benhvien.repository.BenhNhanRepository;
import com.benhvien.web.dto.BenhNhanReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/benh-nhan")
public class BenhNhanController {

    private final BenhNhanRepository repo;

    public BenhNhanController(BenhNhanRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<BenhNhan> list(@RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            return repo.findByHoTenContainingIgnoreCaseOrMaBenhNhanContainingIgnoreCaseOrSoDienThoaiContainingOrderByIdDesc(q, q, q);
        }
        return repo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
    }

    @GetMapping("/{id}")
    public BenhNhan get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy bệnh nhân " + id));
    }

    @PostMapping
    @Transactional
    public BenhNhan create(@Valid @RequestBody BenhNhanReq r) {
        BenhNhan e = new BenhNhan();
        apply(e, r);
        e = repo.save(e);
        if (e.getMaBenhNhan() == null || e.getMaBenhNhan().isBlank()) {
            e.setMaBenhNhan("BN-" + String.format("%05d", e.getId()));
        }
        return repo.save(e);
    }

    @PutMapping("/{id}")
    @Transactional
    public BenhNhan update(@PathVariable Long id, @Valid @RequestBody BenhNhanReq r) {
        BenhNhan e = get(id);
        apply(e, r);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        repo.delete(get(id));
    }

    private void apply(BenhNhan e, BenhNhanReq r) {
        e.setMaBenhNhan(r.maBenhNhan());
        e.setHoTen(r.hoTen());
        e.setNgaySinh(r.ngaySinh());
        e.setGioiTinh(r.gioiTinh());
        e.setSoDienThoai(r.soDienThoai());
        e.setDiaChi(r.diaChi());
        e.setTienSuBenhAn(r.tienSuBenhAn());
    }
}
