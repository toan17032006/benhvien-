package com.benhvien.auth;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Xác minh Google ID Token (đăng nhập bằng Google) qua endpoint tokeninfo của Google,
 * không cần thêm thư viện ngoài — chỉ dùng HttpClient sẵn có của JDK.
 */
@Component
public class GoogleTokenVerifier {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String clientId;

    public GoogleTokenVerifier(@Value("${app.google.client-id:}") String clientId) {
        this.clientId = clientId;
    }

    public record GoogleUser(String email, String hoTen, String anhDaiDien) {}

    public GoogleUser xacThuc(String idToken) throws Exception {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Máy chủ chưa cấu hình GOOGLE_CLIENT_ID nên chưa dùng được đăng nhập Google.");
        }
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token="
                + URLEncoder.encode(idToken, StandardCharsets.UTF_8);
        HttpResponse<String> res = http.send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalArgumentException("Token Google không hợp lệ hoặc đã hết hạn.");
        }
        JsonNode n = mapper.readTree(res.body());
        if (!clientId.equals(n.path("aud").asText())) {
            throw new IllegalArgumentException("Token Google không được phát hành cho ứng dụng này.");
        }
        String email = n.path("email").asText(null);
        if (email == null || email.isBlank() || !n.path("email_verified").asBoolean(false)) {
            throw new IllegalArgumentException("Tài khoản Google chưa xác nhận địa chỉ email.");
        }
        long exp = n.path("exp").asLong(0);
        if (exp > 0 && Instant.now().getEpochSecond() > exp) {
            throw new IllegalArgumentException("Token Google đã hết hạn.");
        }
        return new GoogleUser(email, n.path("name").asText(email), n.path("picture").asText(null));
    }
}
