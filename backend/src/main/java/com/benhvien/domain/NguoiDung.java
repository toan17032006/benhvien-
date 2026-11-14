package com.benhvien.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Người Dùng — tài khoản đăng nhập hệ thống (quản trị viên, bác sĩ, bệnh nhân). */
@Entity
@Table(name = "nguoi_dung", uniqueConstraints = {
        @UniqueConstraint(name = "uk_nguoi_dung_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_nguoi_dung_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    /** Mật khẩu đã mã hóa BCrypt — null với tài khoản đăng nhập bằng Google. */
    private String matKhau;

    private String hoTen;

    private String email;

    private String soDienThoai;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VaiTro vaiTro;

    /** Ảnh đại diện (lấy từ Google khi đăng nhập bằng Google). */
    private String anhDaiDien;

    private boolean active = true;
}
