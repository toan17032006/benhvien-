package com.benhvien.seed;

import com.benhvien.domain.BacSi;
import com.benhvien.domain.BenhNhan;
import com.benhvien.domain.BenhVien;
import com.benhvien.domain.CaTruc;
import com.benhvien.domain.ChamSoc;
import com.benhvien.domain.ChiTietThuoc;
import com.benhvien.domain.DonThuoc;
import com.benhvien.domain.GiuongBenh;
import com.benhvien.domain.Khoa;
import com.benhvien.domain.LanKhamBenh;
import com.benhvien.domain.LanNhapVien;
import com.benhvien.domain.LichCaTruc;
import com.benhvien.domain.LichLamViec;
import com.benhvien.domain.PhongKham;
import com.benhvien.domain.Thuoc;
import com.benhvien.domain.TrangThaiGiuong;
import com.benhvien.domain.TrangThaiKham;
import com.benhvien.domain.TrangThaiNhapVien;
import com.benhvien.domain.TrangThaiXetNghiem;
import com.benhvien.domain.XetNghiem;
import com.benhvien.domain.YTa;
import com.benhvien.repository.BacSiRepository;
import com.benhvien.repository.BenhNhanRepository;
import com.benhvien.repository.BenhVienRepository;
import com.benhvien.repository.CaTrucRepository;
import com.benhvien.repository.ChamSocRepository;
import com.benhvien.repository.ChiTietThuocRepository;
import com.benhvien.repository.DonThuocRepository;
import com.benhvien.repository.GiuongBenhRepository;
import com.benhvien.repository.KhoaRepository;
import com.benhvien.repository.LanKhamBenhRepository;
import com.benhvien.repository.LanNhapVienRepository;
import com.benhvien.repository.LichCaTrucRepository;
import com.benhvien.repository.LichLamViecRepository;
import com.benhvien.repository.PhongKhamRepository;
import com.benhvien.repository.ThuocRepository;
import com.benhvien.repository.XetNghiemRepository;
import com.benhvien.repository.YTaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Tạo dữ liệu mẫu tiếng Việt lần đầu chạy (chỉ khi CSDL còn rỗng). */
@Component
public class DataSeeder implements ApplicationRunner {

    private final BenhVienRepository benhVienRepo;
    private final KhoaRepository khoaRepo;
    private final PhongKhamRepository phongKhamRepo;
    private final GiuongBenhRepository giuongRepo;
    private final BacSiRepository bacSiRepo;
    private final YTaRepository yTaRepo;
    private final CaTrucRepository caTrucRepo;
    private final LichLamViecRepository lichRepo;
    private final LichCaTrucRepository lichCaTrucRepo;
    private final BenhNhanRepository benhNhanRepo;
    private final LanKhamBenhRepository lanKhamRepo;
    private final LanNhapVienRepository lanNhapVienRepo;
    private final ChamSocRepository chamSocRepo;
    private final DonThuocRepository donThuocRepo;
    private final ChiTietThuocRepository chiTietThuocRepo;
    private final ThuocRepository thuocRepo;
    private final XetNghiemRepository xetNghiemRepo;

    public DataSeeder(BenhVienRepository benhVienRepo, KhoaRepository khoaRepo,
                      PhongKhamRepository phongKhamRepo, GiuongBenhRepository giuongRepo,
                      BacSiRepository bacSiRepo, YTaRepository yTaRepo, CaTrucRepository caTrucRepo,
                      LichLamViecRepository lichRepo, LichCaTrucRepository lichCaTrucRepo,
                      BenhNhanRepository benhNhanRepo, LanKhamBenhRepository lanKhamRepo,
                      LanNhapVienRepository lanNhapVienRepo, ChamSocRepository chamSocRepo,
                      DonThuocRepository donThuocRepo, ChiTietThuocRepository chiTietThuocRepo,
                      ThuocRepository thuocRepo, XetNghiemRepository xetNghiemRepo) {
        this.benhVienRepo = benhVienRepo;
        this.khoaRepo = khoaRepo;
        this.phongKhamRepo = phongKhamRepo;
        this.giuongRepo = giuongRepo;
        this.bacSiRepo = bacSiRepo;
        this.yTaRepo = yTaRepo;
        this.caTrucRepo = caTrucRepo;
        this.lichRepo = lichRepo;
        this.lichCaTrucRepo = lichCaTrucRepo;
        this.benhNhanRepo = benhNhanRepo;
        this.lanKhamRepo = lanKhamRepo;
        this.lanNhapVienRepo = lanNhapVienRepo;
        this.chamSocRepo = chamSocRepo;
        this.donThuocRepo = donThuocRepo;
        this.chiTietThuocRepo = chiTietThuocRepo;
        this.thuocRepo = thuocRepo;
        this.xetNghiemRepo = xetNghiemRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (benhVienRepo.count() > 0) {
            return;
        }

        // ===== Bệnh viện =====
        BenhVien bv = new BenhVien();
        bv.setTen("Bệnh viện Đa khoa Trung ương");
        bv.setDiaChi("78 Đường Giải Phóng, Hai Bà Trưng, Hà Nội");
        bv.setSoDienThoai("024-3826-7149");
        bv.setEmail("lienhe@bvdktw.vn");
        bv = benhVienRepo.save(bv);

        // ===== Khoa =====
        Khoa kNoi = khoa("KHOA-01", "Khoa Nội", "Khám và điều trị bệnh nội khoa", bv);
        Khoa kNgoai = khoa("KHOA-02", "Khoa Ngoại", "Phẫu thuật và điều trị ngoại khoa", bv);
        Khoa kNhi = khoa("KHOA-03", "Khoa Nhi", "Chăm sóc sức khỏe trẻ em", bv);
        Khoa kCapCuu = khoa("KHOA-04", "Khoa Cấp cứu", "Tiếp nhận cấp cứu 24/7", bv);
        Khoa kXetNghiem = khoa("KHOA-05", "Khoa Xét nghiệm", "Xét nghiệm chẩn đoán", bv);
        Khoa kSan = khoa("KHOA-06", "Khoa Sản", "Sản phụ khoa", bv);

        // ===== Phòng khám =====
        PhongKham pk1 = phongKham("PK-101", "Phòng khám Nội tổng quát", kNoi);
        PhongKham pk2 = phongKham("PK-102", "Phòng khám Tim mạch", kNoi);
        phongKham("PK-201", "Phòng khám Ngoại tổng quát", kNgoai);
        PhongKham pk3 = phongKham("PK-202", "Phòng khám Chấn thương", kNgoai);
        PhongKham pk4 = phongKham("PK-301", "Phòng khám Nhi", kNhi);
        phongKham("PK-401", "Phòng Cấp cứu A", kCapCuu);
        phongKham("PK-501", "Phòng Xét nghiệm", kXetNghiem);

        // ===== Giường bệnh =====
        GiuongBenh g1 = giuong("G-0101", "Giường thường", TrangThaiGiuong.TRONG, kNoi);
        GiuongBenh g2 = giuong("G-0102", "Giường thường", TrangThaiGiuong.TRONG, kNoi);
        GiuongBenh g3 = giuong("G-0103", "Giường ưu tiên", TrangThaiGiuong.TRONG, kNoi);
        giuong("G-0201", "Giường thường", TrangThaiGiuong.TRONG, kNgoai);
        giuong("G-0202", "Giường hậu phẫu", TrangThaiGiuong.BAO_TRI, kNgoai);
        giuong("G-0301", "Giường nhi", TrangThaiGiuong.TRONG, kNhi);
        giuong("G-0302", "Giường nhi", TrangThaiGiuong.TRONG, kNhi);
        giuong("G-0401", "Giường cấp cứu", TrangThaiGiuong.TRONG, kCapCuu);
        giuong("G-0601", "Giường sản", TrangThaiGiuong.TRONG, kSan);

        // ===== Bác sĩ =====
        BacSi bs1 = bacSi("BS-001", "GS.TS Nguyễn Văn Minh", "Tim mạch", "Giáo sư", "0912-345-678", "minhnv@bvdktw.vn", kNoi);
        BacSi bs2 = bacSi("BS-002", "TS Trần Thị Lan", "Nội tiết", "Tiến sĩ", "0913-222-333", "lantt@bvdktw.vn", kNoi);
        BacSi bs3 = bacSi("BS-003", "BS Lê Văn Cường", "Chấn thương chỉnh hình", "Bác sĩ", "0904-555-666", "cuonglv@bvdktw.vn", kNgoai);
        BacSi bs4 = bacSi("BS-004", "BSCKII Phạm Thu Hà", "Nhi khoa", "Bác sĩ chuyên khoa II", "0905-777-888", "hapt@bvdktw.vn", kNhi);
        BacSi bs5 = bacSi("BS-005", "BS Đỗ Quang Hải", "Cấp cứu", "Bác sĩ", "0906-999-000", "haidq@bvdktw.vn", kCapCuu);
        BacSi bs6 = bacSi("BS-006", "ThS Vũ Ngọc Ánh", "Xét nghiệm", "Thạc sĩ", "0907-111-222", "anhvn@bvdktw.vn", kXetNghiem);

        // ===== Y tá =====
        YTa yt1 = yTa("YT-001", "Nguyễn Thị Hoa", "Cao đẳng Y tá", "0981-111-111", "hoant@bvdktw.vn", kNoi);
        YTa yt2 = yTa("YT-002", "Trần Văn Nam", "Đại học Điều dưỡng", "0982-222-222", "namtv@bvdktw.vn", kNoi);
        YTa yt3 = yTa("YT-003", "Lê Thị Mai", "Cao đẳng Điều dưỡng", "0983-333-333", "mailt@bvdktw.vn", kNgoai);
        YTa yt4 = yTa("YT-004", "Phạm Văn Tú", "Trung cấp Y tá", "0984-444-444", "tupv@bvdktw.vn", kNhi);
        YTa yt5 = yTa("YT-005", "Hoàng Thu Trang", "Đại học Điều dưỡng", "0985-555-555", "tranght@bvdktw.vn", kCapCuu);

        // ===== Ca trực =====
        CaTruc ca1 = caTruc("CA-01", "Ca sáng", LocalTime.of(7, 0), LocalTime.of(14, 0));
        CaTruc ca2 = caTruc("CA-02", "Ca chiều", LocalTime.of(14, 0), LocalTime.of(21, 0));
        CaTruc ca3 = caTruc("CA-03", "Ca đêm", LocalTime.of(21, 0), LocalTime.of(7, 0));
        CaTruc ca4 = caTruc("CA-04", "Ca hành chính", LocalTime.of(8, 0), LocalTime.of(17, 0));

        // ===== Lịch làm việc hôm nay và hôm qua =====
        LocalDate homNay = LocalDate.now();
        LocalDate homQua = homNay.minusDays(1);
        LichLamViec lich1 = lich(homNay, bs1, pk2, "Khám tim mạch theo hẹn");
        phanCa(lich1, ca1, ca2);
        LichLamViec lich2 = lich(homNay, bs2, pk1, "Khám nội tiết");
        phanCa(lich2, ca4);
        LichLamViec lich3 = lich(homNay, bs4, pk4, "Khám nhi");
        phanCa(lich3, ca1);
        LichLamViec lich4 = lich(homNay, bs6, phongKhamRepo.findById(7L).orElse(null), "Trực xét nghiệm");
        phanCa(lich4, ca1, ca2, ca3);
        LichLamViec lich5 = lich(homQua, bs3, pk3, "Khám chấn thương");
        phanCa(lich5, ca2);

        // ===== Thuốc =====
        Thuoc t1 = thuoc("TH-0001", "Paracetamol 500mg", "Viên", new BigDecimal("500"), 5000, "Giảm đau, hạ sốt");
        Thuoc t2 = thuoc("TH-0002", "Amoxicillin 500mg", "Viên nang", new BigDecimal("1200"), 2500, "Kháng sinh điều trị nhiễm khuẩn");
        Thuoc t3 = thuoc("TH-0003", "Omeprazole 20mg", "Viên", new BigDecimal("900"), 1800, "Giảm tiết axit dạ dày");
        Thuoc t4 = thuoc("TH-0004", "Salbutamol 2mg", "Viên", new BigDecimal("750"), 1200, "Giãn phế quản, trị hen");
        Thuoc t5 = thuoc("TH-0005", "Vitamin C 1000mg", "Viên sủi", new BigDecimal("300"), 3000, "Bổ sung vitamin C");

        // ===== Bệnh nhân =====
        BenhNhan bn1 = benhNhan("BN-00001", "Ngô Quang Huy", LocalDate.of(1985, 3, 15), "Nam", "0912-888-999", "25 Phố Huế, Hoàn Kiếm, Hà Nội", "Tăng huyết áp nhẹ");
        BenhNhan bn2 = benhNhan("BN-00002", "Đỗ Thị Nhung", LocalDate.of(1990, 7, 22), "Nữ", "0913-777-666", "12 Nguyễn Trãi, Thanh Xuân, Hà Nội", null);
        BenhNhan bn3 = benhNhan("BN-00003", "Trần Gia Bảo", LocalDate.of(2018, 11, 3), "Nam", "0914-555-444", "88 Giải Phóng, Hà Nội", "Dị ứng penicillin");
        BenhNhan bn4 = benhNhan("BN-00004", "Lý Văn Tùng", LocalDate.of(1962, 1, 9), "Nam", "0915-333-222", "5 Láng Hạ, Ba Đình, Hà Nội", "Tiểu đường type 2");
        benhNhan("BN-00005", "Phan Thị Hạnh", LocalDate.of(1995, 5, 30), "Nữ", "0916-222-111", "40 Xã Đàn, Đống Đa, Hà Nội", null);

        // ===== Lần khám bệnh =====
        LanKhamBenh k1 = lanKham(LocalDateTime.now().minusHours(3), "Đau ngực khi gắng sức, khó thở nhẹ", "Thiếu máu cơ tim cục bộ", TrangThaiKham.HOAN_THANH, bn1, lich1);
        LanKhamBenh k2 = lanKham(LocalDateTime.now().minusHours(1), "Mệt mỏi, sụt cân, uống nhiều nước", "Nghi ngờ đái tháo đường", TrangThaiKham.DANG_KHAM, bn4, lich2);
        LanKhamBenh k3 = lanKham(LocalDateTime.now().minusMinutes(30), "Sốt 38.5°C, ho, chảy nước mũi 2 ngày", "Viêm đường hô hấp trên", TrangThaiKham.DANG_KHAM, bn3, lich3);
        LanKhamBenh k4 = lanKham(LocalDateTime.now().minusDays(1), "Đau vai phải, mất ngủ", "Rối loạn tiền đình", TrangThaiKham.HOAN_THANH, bn2, lich5);

        // ===== Lần nhập viện (từ lần khám k1) =====
        LanNhapVien nv1 = new LanNhapVien();
        nv1.setMaNhapVien("NV-00001");
        nv1.setNgayNhap(LocalDateTime.now().minusHours(2));
        nv1.setLyDoNhapVien("Theo dõi thiếu máu cơ tim, tiền giả phẫu");
        nv1.setGhiChu("Bệnh nhân ổn định, tiếp tục theo dõi");
        nv1.setTrangThai(TrangThaiNhapVien.DANG_DIEU_TRI);
        nv1.setGiuongBenh(g1);
        nv1 = lanNhapVienRepo.save(nv1);
        g1.setTrangThai(TrangThaiGiuong.DANG_SU_DUNG);
        giuongRepo.save(g1);
        k1.setLanNhapVien(nv1);
        lanKhamRepo.save(k1);

        // ===== Y tá chăm sóc =====
        chamSoc(yt1, nv1);
        chamSoc(yt2, nv1);

        // ===== Lần khám trong thời gian nhập viện =====
        lanKham(LocalDateTime.now().minusMinutes(10), "Tái khám trong viện: đau ngực giảm", null, TrangThaiKham.DANG_KHAM, bn1, null).setLanNhapVien(nv1);

        // ===== Xét nghiệm =====
        XetNghiem xn1 = xetNghiem("XN-00001", "Công thức máu", "Xét nghiệm máu", LocalDateTime.now().minusHours(2), "Hb 135 g/L, WBC 7.2 K/µL — trong ngưỡng bình thường", TrangThaiXetNghiem.CO_KET_QUA, k1, lich4);
        XetNghiem xn2 = xetNghiem("XN-00002", "Điện tâm đồ", "Cận lâm sàng", LocalDateTime.now().minusHours(1), null, TrangThaiXetNghiem.CHO_KET_QUA, k1, lich4);
        xetNghiem("XN-00003", "Xét nghiệm đường huyết", "Xét nghiệm máu", LocalDateTime.now().minusMinutes(45), null, TrangThaiXetNghiem.CHO_KET_QUA, k2, lich4);

        // ===== Đơn thuốc + chi tiết =====
        DonThuoc dt1 = new DonThuoc();
        dt1.setMaDonThuoc("DT-00001");
        dt1.setNgayKe(LocalDateTime.now().minusHours(2));
        dt1.setGhiChu("Uống sau ăn");
        dt1.setLanKhamBenh(k1);
        dt1 = donThuocRepo.save(dt1);
        chiTiet(dt1, t1, 20, "1 viên/lần, 3 lần/ngày", "Dùng khi sốt trên 38.5°C");
        chiTiet(dt1, t3, 14, "1 viên/lần, 1 lần/ngày sáng", null);

        DonThuoc dt2 = new DonThuoc();
        dt2.setMaDonThuoc("DT-00002");
        dt2.setNgayKe(LocalDateTime.now().minusMinutes(30));
        dt2.setGhiChu(null);
        dt2.setLanKhamBenh(k3);
        dt2 = donThuocRepo.save(dt2);
        chiTiet(dt2, t1, 6, "1 viên/lần khi sốt, cách 6 tiếng", "Trẻ em: giảm liều theo cân nặng");
        chiTiet(dt2, t5, 10, "1 viên/ngày pha nước ấm", null);
    }

    // ===== helper =====

    private Khoa khoa(String ma, String ten, String moTa, BenhVien bv) {
        Khoa k = new Khoa();
        k.setMaKhoa(ma);
        k.setTenKhoa(ten);
        k.setMoTa(moTa);
        k.setBenhVien(bv);
        return khoaRepo.save(k);
    }

    private PhongKham phongKham(String ma, String ten, Khoa khoa) {
        PhongKham p = new PhongKham();
        p.setMaPhong(ma);
        p.setTenPhong(ten);
        p.setKhoa(khoa);
        return phongKhamRepo.save(p);
    }

    private GiuongBenh giuong(String ma, String loai, TrangThaiGiuong tt, Khoa khoa) {
        GiuongBenh g = new GiuongBenh();
        g.setMaGiuong(ma);
        g.setLoaiGiuong(loai);
        g.setTrangThai(tt);
        g.setKhoa(khoa);
        return giuongRepo.save(g);
    }

    private BacSi bacSi(String ma, String hoTen, String chuyenMon, String hocHam, String sdt, String email, Khoa khoa) {
        BacSi b = new BacSi();
        b.setMaBacSi(ma);
        b.setHoTen(hoTen);
        b.setChuyenMon(chuyenMon);
        b.setHocHam(hocHam);
        b.setSoDienThoai(sdt);
        b.setEmail(email);
        b.setKhoa(khoa);
        return bacSiRepo.save(b);
    }

    private YTa yTa(String ma, String hoTen, String trinhDo, String sdt, String email, Khoa khoa) {
        YTa y = new YTa();
        y.setMaYTa(ma);
        y.setHoTen(hoTen);
        y.setTrinhDo(trinhDo);
        y.setSoDienThoai(sdt);
        y.setEmail(email);
        y.setKhoa(khoa);
        return yTaRepo.save(y);
    }

    private CaTruc caTruc(String ma, String ten, LocalTime bd, LocalTime kt) {
        CaTruc c = new CaTruc();
        c.setMaCa(ma);
        c.setTenCa(ten);
        c.setGioBatDau(bd);
        c.setGioKetThuc(kt);
        return caTrucRepo.save(c);
    }

    private LichLamViec lich(LocalDate ngay, BacSi bs, PhongKham pk, String ghiChu) {
        LichLamViec l = new LichLamViec();
        l.setNgay(ngay);
        l.setBacSi(bs);
        l.setPhongKham(pk);
        l.setGhiChu(ghiChu);
        return lichRepo.save(l);
    }

    private void phanCa(LichLamViec lich, CaTruc... cas) {
        for (CaTruc ca : cas) {
            LichCaTruc lc = new LichCaTruc();
            lc.setLichLamViec(lich);
            lc.setCaTruc(ca);
            lichCaTrucRepo.save(lc);
        }
    }

    private Thuoc thuoc(String ma, String ten, String donVi, BigDecimal gia, Integer ton, String congDung) {
        Thuoc t = new Thuoc();
        t.setMaThuoc(ma);
        t.setTenThuoc(ten);
        t.setDonViTinh(donVi);
        t.setDonGia(gia);
        t.setSoLuongTon(ton);
        t.setCongDung(congDung);
        return thuocRepo.save(t);
    }

    private BenhNhan benhNhan(String ma, String hoTen, LocalDate ns, String gt, String sdt, String dc, String tienSu) {
        BenhNhan b = new BenhNhan();
        b.setMaBenhNhan(ma);
        b.setHoTen(hoTen);
        b.setNgaySinh(ns);
        b.setGioiTinh(gt);
        b.setSoDienThoai(sdt);
        b.setDiaChi(dc);
        b.setTienSuBenhAn(tienSu);
        return benhNhanRepo.save(b);
    }

    private LanKhamBenh lanKham(LocalDateTime tg, String trieuChung, String chanDoan, TrangThaiKham tt, BenhNhan bn, LichLamViec lich) {
        LanKhamBenh k = new LanKhamBenh();
        k.setThoiGianKham(tg);
        k.setTrieuChung(trieuChung);
        k.setChanDoan(chanDoan);
        k.setTrangThai(tt);
        k.setBenhNhan(bn);
        k.setLichLamViec(lich);
        k = lanKhamRepo.save(k);
        k.setMaLanKham("LK-" + String.format("%05d", k.getId()));
        return lanKhamRepo.save(k);
    }

    private void chamSoc(YTa yt, LanNhapVien nv) {
        ChamSoc c = new ChamSoc();
        c.setYTa(yt);
        c.setLanNhapVien(nv);
        chamSocRepo.save(c);
    }

    private XetNghiem xetNghiem(String ma, String ten, String loai, LocalDateTime tg, String ketQua, TrangThaiXetNghiem tt, LanKhamBenh k, LichLamViec lich) {
        XetNghiem x = new XetNghiem();
        x.setMaXetNghiem(ma);
        x.setTenXetNghiem(ten);
        x.setLoaiXetNghiem(loai);
        x.setThoiGian(tg);
        x.setKetQua(ketQua);
        x.setTrangThai(tt);
        x.setLanKhamBenh(k);
        x.setLichLamViec(lich);
        return xetNghiemRepo.save(x);
    }

    private void chiTiet(DonThuoc dt, Thuoc t, Integer soLuong, String lieuDung, String ghiChu) {
        ChiTietThuoc c = new ChiTietThuoc();
        c.setDonThuoc(dt);
        c.setThuoc(t);
        c.setSoLuong(soLuong);
        c.setLieuDung(lieuDung);
        c.setGhiChu(ghiChu);
        chiTietThuocRepo.save(c);
    }
}
