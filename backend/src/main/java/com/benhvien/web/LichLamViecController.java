package com.benhvien.web;

import com.benhvien.domain.CaTruc;
import com.benhvien.domain.LanKhamBenh;
import com.benhvien.domain.LichCaTruc;
import com.benhvien.domain.LichLamViec;
import com.benhvien.domain.XetNghiem;
import com.benhvien.repository.BacSiRepository;
import com.benhvien.repository.CaTrucRepository;
import com.benhvien.repository.LanKhamBenhRepository;
import com.benhvien.repository.LichCaTrucRepository;
import com.benhvien.repository.LichLamViecRepository;
import com.benhvien.repository.PhongKhamRepository;
import com.benhvien.repository.XetNghiemRepository;
import com.benhvien.web.dto.LichLamViecReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/lich-lam-viec")
public class LichLamViecController {

    private final LichLamViecRepository repo;
    private final LichCaTrucRepository lichCaTrucRepo;
    private final BacSiRepository bacSiRepo;
    private final PhongKhamRepository phongKhamRepo;
    private final CaTrucRepository caTrucRepo;
    private final LanKhamBenhRepository lanKhamRepo;
    private final XetNghiemRepository xetNghiemRepo;

    public LichLamViecController(LichLamViecRepository repo, LichCaTrucRepository lichCaTrucRepo,
                                 BacSiRepository bacSiRepo, PhongKhamRepository phongKhamRepo,
                                 CaTrucRepository caTrucRepo, LanKhamBenhRepository lanKhamRepo,
                                 XetNghiemRepository xetNghiemRepo) {
        this.repo = repo;
        this.lichCaTrucRepo = lichCaTrucRepo;
        this.bacSiRepo = bacSiRepo;
        this.phongKhamRepo = phongKhamRepo;
        this.caTrucRepo = caTrucRepo;
        this.lanKhamRepo = lanKhamRepo;
        this.xetNghiemRepo = xetNghiemRepo;
    }

    @GetMapping
    public List<LichLamViec> list(@RequestParam(required = false) Long bacSiId,
                                  @RequestParam(required = false) Long phongKhamId,
                                  @RequestParam(required = false) LocalDate ngay) {
        if (bacSiId != null) {
            return repo.findByBacSiIdOrderByNgayDesc(bacSiId);
        }
        if (phongKhamId != null) {
            return repo.findByPhongKhamIdOrderByNgayDesc(phongKhamId);
        }
        if (ngay != null) {
            return repo.findByNgayOrderByIdDesc(ngay);
        }
        return repo.findAllByOrderByNgayDescIdDesc();
    }

    /** Danh sách ca trực của một lịch làm việc (quan hệ N-N). */
    @GetMapping("/{id}/ca-truc")
    public List<CaTruc> caTrucCuaLich(@PathVariable Long id) {
        return lichCaTrucRepo.findByLichLamViecIdOrderByIdAsc(get(id).getId()).stream()
                .map(LichCaTruc::getCaTruc)
                .toList();
    }

    @GetMapping("/{id}")
    public LichLamViec get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy lịch làm việc " + id));
    }

    @PostMapping
    @Transactional
    public LichLamViec create(@Valid @RequestBody LichLamViecReq r) {
        LichLamViec e = new LichLamViec();
        apply(e, r);
        e = repo.save(e);
        replaceCaTruc(e, r.caTrucIds());
        return e;
    }

    @PutMapping("/{id}")
    @Transactional
    public LichLamViec update(@PathVariable Long id, @Valid @RequestBody LichLamViecReq r) {
        LichLamViec e = get(id);
        apply(e, r);
        e = repo.save(e);
        replaceCaTruc(e, r.caTrucIds());
        return e;
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        LichLamViec e = get(id);
        lichCaTrucRepo.deleteAll(lichCaTrucRepo.findByLichLamViecIdOrderByIdAsc(e.getId()));
        // Tháo các lần khám / xét nghiệm khỏi lịch (giữ lại bản ghi, chỉ bỏ liên kết)
        List<LanKhamBenh> cacLanKham = lanKhamRepo.findByLichLamViecIdOrderByThoiGianKhamDesc(e.getId());
        for (LanKhamBenh k : cacLanKham) {
            k.setLichLamViec(null);
        }
        lanKhamRepo.saveAll(cacLanKham);
        List<XetNghiem> cacXetNghiem = xetNghiemRepo.findByLichLamViecIdOrderByIdDesc(e.getId());
        for (XetNghiem x : cacXetNghiem) {
            x.setLichLamViec(null);
        }
        xetNghiemRepo.saveAll(cacXetNghiem);
        repo.delete(e);
    }

    private void replaceCaTruc(LichLamViec lich, List<Long> caTrucIds) {
        lichCaTrucRepo.deleteAll(lichCaTrucRepo.findByLichLamViecIdOrderByIdAsc(lich.getId()));
        if (caTrucIds == null || caTrucIds.isEmpty()) {
            return;
        }
        List<LichCaTruc> rows = new ArrayList<>();
        for (Long caId : caTrucIds.stream().distinct().toList()) {
            CaTruc ca = caTrucRepo.findById(caId)
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy ca trực " + caId));
            LichCaTruc row = new LichCaTruc();
            row.setLichLamViec(lich);
            row.setCaTruc(ca);
            rows.add(row);
        }
        lichCaTrucRepo.saveAll(rows);
    }

    private void apply(LichLamViec e, LichLamViecReq r) {
        e.setNgay(r.ngay());
        e.setGhiChu(r.ghiChu());
        e.setBacSi(bacSiRepo.findById(r.bacSiId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bác sĩ " + r.bacSiId())));
        e.setPhongKham(phongKhamRepo.findById(r.phongKhamId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phòng khám " + r.phongKhamId())));
    }
}
