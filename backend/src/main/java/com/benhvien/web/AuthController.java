package com.benhvien.web;

import com.benhvien.auth.GoogleTokenVerifier;
import com.benhvien.auth.JwtTokenProvider;
import com.benhvien.domain.NguoiDung;
import com.benhvien.domain.VaiTro;
import com.benhvien.repository.NguoiDungRepository;
import com.benhvien.web.dto.AuthResponse;
import com.benhvien.web.dto.GoogleAuthReq;
import com.benhvien.web.dto.LoginReq;
import com.benhvien.web.dto.NguoiDungView;
import com.benhvien.web.dto.RegisterReq;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final NguoiDungRepository repo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenVerifier googleVerifier;

    public AuthController(NguoiDungRepository repo, PasswordEncoder encoder,
                          JwtTokenProvider jwtTokenProvider, GoogleTokenVerifier googleVerifier) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleVerifier = googleVerifier;
    }

    /** Đăng ký tài khoản mới (mặc định vai trò BENH_NHAN). */
    @PostMapping("/register")
    @Transactional
    public AuthResponse register(@Valid @RequestBody RegisterReq r) {
        if (repo.existsByUsername(r.username())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác.");
        }
        if (r.email() != null && !r.email().isBlank() && repo.findByEmail(r.email()).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác.");
        }
        NguoiDung nd = new NguoiDung();
        nd.setUsername(r.username());
        nd.setMatKhau(encoder.encode(r.matKhau()));
        nd.setHoTen(r.hoTen());
        nd.setEmail(r.email());
        nd.setSoDienThoai(r.soDienThoai());
        nd.setVaiTro(parseVaiTro(r.vaiTro()));
        nd.setActive(true);
        repo.save(nd);
        return phanHoi(nd);
    }

    /** Đăng nhập bằng tên đăng nhập + mật khẩu. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginReq r) {
        NguoiDung nd = repo.findByUsername(r.username())
                .orElseThrow(() -> new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu."));
        if (nd.getMatKhau() == null || !encoder.matches(r.matKhau(), nd.getMatKhau())) {
            throw new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu.");
        }
        if (!nd.isActive()) {
            throw new IllegalArgumentException("Tài khoản đã bị khóa, vui lòng liên hệ quản trị viên.");
        }
        return phanHoi(nd);
    }

    /** Đăng nhập/đăng ký bằng Google — token hợp lệ thì tự tạo tài khoản nếu chưa có. */
    @PostMapping("/google")
    @Transactional
    public AuthResponse google(@Valid @RequestBody GoogleAuthReq r) {
        GoogleTokenVerifier.GoogleUser g;
        try {
            g = googleVerifier.xacThuc(r.idToken());
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Không xác minh được token Google, vui lòng thử lại.");
        }
        NguoiDung nd = repo.findByEmail(g.email()).orElse(null);
        if (nd == null) {
            nd = new NguoiDung();
            nd.setUsername(g.email()); // dùng email làm tên đăng nhập
            nd.setMatKhau(null);       // không có mật khẩu — đăng nhập qua Google
            nd.setHoTen(g.hoTen());
            nd.setEmail(g.email());
            nd.setAnhDaiDien(g.anhDaiDien());
            nd.setVaiTro(VaiTro.BENH_NHAN);
            nd.setActive(true);
        } else {
            if (!nd.isActive()) {
                throw new IllegalArgumentException("Tài khoản đã bị khóa, vui lòng liên hệ quản trị viên.");
            }
            if (g.anhDaiDien() != null && !g.anhDaiDien().equals(nd.getAnhDaiDien())) {
                nd.setAnhDaiDien(g.anhDaiDien());
            }
            if (nd.getHoTen() == null || nd.getHoTen().isBlank()) {
                nd.setHoTen(g.hoTen());
            }
        }
        repo.save(nd);
        return phanHoi(nd);
    }

    /** Thông tin người đang đăng nhập. */
    @GetMapping("/me")
    public NguoiDungView me() {
        return view(nguoiDungHienTai());
    }

    // ===== Hàm nội bộ =====

    private NguoiDung nguoiDungHienTai() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof NguoiDung nd) {
            return nd;
        }
        throw new IllegalArgumentException("Chưa đăng nhập hoặc phiên đã hết hạn.");
    }

    private VaiTro parseVaiTro(String vaiTro) {
        if (vaiTro == null || vaiTro.isBlank()) {
            return VaiTro.BENH_NHAN;
        }
        try {
            return VaiTro.valueOf(vaiTro.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Vai trò không hợp lệ (chọn ADMIN, BAC_SI hoặc BENH_NHAN).");
        }
    }

    private AuthResponse phanHoi(NguoiDung nd) {
        return new AuthResponse(jwtTokenProvider.taoToken(nd), view(nd));
    }

    private NguoiDungView view(NguoiDung nd) {
        return new NguoiDungView(nd.getId(), nd.getUsername(), nd.getHoTen(), nd.getEmail(),
                nd.getSoDienThoai(), nd.getVaiTro(), nd.getAnhDaiDien());
    }
}
