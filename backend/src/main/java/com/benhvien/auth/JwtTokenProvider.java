package com.benhvien.auth;

import com.benhvien.domain.NguoiDung;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sinh và xác thực JWT (HS256) tự triển khai bằng JDK — không thêm thư viện ngoài.
 * Payload gồm: sub (username), vaiTro, hoTen, iat, exp.
 */
@Component
public class JwtTokenProvider {

    private static final Base64.Encoder B64URL = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64DEC = Base64.getUrlDecoder();

    private final byte[] secret;
    private final long expireMs;
    private final ObjectMapper mapper = new ObjectMapper();

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.expire-hours:24}") long expireHours) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expireMs = expireHours * 3_600_000L;
    }

    public String taoToken(NguoiDung nd) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            Instant now = Instant.now();
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("sub", nd.getUsername());
            claims.put("vaiTro", nd.getVaiTro().name());
            claims.put("hoTen", nd.getHoTen());
            claims.put("iat", now.getEpochSecond());
            claims.put("exp", now.plusMillis(expireMs).getEpochSecond());
            String duLieu = B64URL.encodeToString(mapper.writeValueAsBytes(header))
                    + "." + B64URL.encodeToString(mapper.writeValueAsBytes(claims));
            return duLieu + "." + ky(duLieu);
        } catch (Exception ex) {
            throw new IllegalStateException("Không tạo được token đăng nhập.", ex);
        }
    }

    /** Trả về username nếu token hợp lệ và chưa hết hạn, ngược lại trả về null. */
    public String xacThuc(String token) {
        if (token == null) {
            return null;
        }
        String[] part = token.split("\\.");
        if (part.length != 3) {
            return null;
        }
        try {
            String duLieu = part[0] + "." + part[1];
            boolean dungChuKy = MessageDigest.isEqual(
                    ky(duLieu).getBytes(StandardCharsets.US_ASCII),
                    part[2].getBytes(StandardCharsets.US_ASCII));
            if (!dungChuKy) {
                return null;
            }
            Map<?, ?> claims = mapper.readValue(B64DEC.decode(part[1]), Map.class);
            Object exp = claims.get("exp");
            if (!(exp instanceof Number n) || Instant.now().getEpochSecond() > n.longValue()) {
                return null;
            }
            Object sub = claims.get("sub");
            return sub instanceof String s ? s : null;
        } catch (Exception ex) {
            return null;
        }
    }

    /** Ký HMAC-SHA256 rồi mã hóa base64url. */
    private String ky(String duLieu) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return B64URL.encodeToString(mac.doFinal(duLieu.getBytes(StandardCharsets.US_ASCII)));
        } catch (Exception ex) {
            throw new IllegalStateException("Không ký được token.", ex);
        }
    }
}
