package com.benhvien.web;

import com.benhvien.domain.LichCaTruc;
import com.benhvien.repository.CaTrucRepository;
import com.benhvien.repository.LichCaTrucRepository;
import com.benhvien.repository.LichLamViecRepository;
import com.benhvien.web.dto.LichCaTrucReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/** Quan hệ N-N giữa Lịch làm việc và Ca trực. */
@RestController
@RequestMapping("/api/lich-ca-truc")
public class LichCaTrucController {

    private final LichCaTrucRepository repo;
    private final LichLamViecRepository lichRepo;
    private final CaTrucRepository caTrucRepo;

    public LichCaTrucController(LichCaTrucRepository repo, LichLamViecRepository lichRepo,
                                CaTrucRepository caTrucRepo) {
        this.repo = repo;
        this.lichRepo = lichRepo;
        this.caTrucRepo = caTrucRepo;
    }

    @GetMapping
    public List<LichCaTruc> list(@RequestParam(required = false) Long lichLamViecId,
                                 @RequestParam(required = false) Long caTrucId) {
        if (lichLamViecId != null) {
            return repo.findByLichLamViecIdOrderByIdAsc(lichLamViecId);
        }
        if (caTrucId != null) {
            return repo.findByCaTrucIdOrderByIdDesc(caTrucId);
        }
        return repo.findAll();
    }

    @PostMapping
    @Transactional
    public LichCaTruc create(@Valid @RequestBody LichCaTrucReq r) {
        if (repo.findByLichLamViecIdAndCaTrucId(r.lichLamViecId(), r.caTrucId()).isPresent()) {
            throw new IllegalArgumentException("Ca trực này đã được gán cho lịch làm việc đó.");
        }
        LichCaTruc e = new LichCaTruc();
        e.setLichLamViec(lichRepo.findById(r.lichLamViecId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lịch làm việc " + r.lichLamViecId())));
        e.setCaTruc(caTrucRepo.findById(r.caTrucId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy ca trực " + r.caTrucId())));
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        repo.delete(repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phân ca " + id)));
    }
}
