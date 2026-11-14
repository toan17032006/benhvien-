package com.benhvien.auth;

import com.benhvien.domain.NguoiDung;
import com.benhvien.repository.NguoiDungRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** Đọc header "Authorization: Bearer <token>" và nạp Người Dùng vào SecurityContext. */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final NguoiDungRepository nguoiDungRepo;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider, NguoiDungRepository nguoiDungRepo) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.nguoiDungRepo = nguoiDungRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = jwtTokenProvider.xacThuc(header.substring(7));
            if (username != null) {
                NguoiDung nd = nguoiDungRepo.findByUsername(username).orElse(null);
                if (nd != null && nd.isActive()) {
                    var auth = new UsernamePasswordAuthenticationToken(
                            nd, null, List.of(new SimpleGrantedAuthority("ROLE_" + nd.getVaiTro().name())));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
