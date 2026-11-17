package com.benhvien.web;

import com.benhvien.domain.NguoiDung;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final String key;
    private final String model;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    public AiController(@Value("${app.gemini.api-key:}") String key,
                        @Value("${app.gemini.model:gemini-flash-latest}") String model) {
        this.key = key; this.model = model;
    }
    public record TinNhan(@NotBlank @Pattern(regexp="user|model") String role, @NotBlank @Size(max=4000) String text) {}
    public record ChatReq(@NotEmpty @Size(max=19) List<@NotNull @Valid TinNhan> messages) {}
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@Valid @RequestBody ChatReq req, @AuthenticationPrincipal NguoiDung user) {
        if (key.isBlank()) return error(503,"Trợ lý AI chưa được cấu hình.");
        for (int i=0; i<req.messages().size(); i++)
            if (!req.messages().get(i).role().equals(i%2==0 ? "user" : "model")) return error(400,"Hội thoại không hợp lệ.");
        if (req.messages().size()%2==0) return error(400,"Cần có câu hỏi mới.");
        String prompt = "Bạn là trợ lý sức khỏe tiếng Việt. Vai trò: " + user.getVaiTro().name()
            + ". Bệnh nhân: dễ hiểu; bác sĩ: tham khảo chuyên môn và chẩn đoán phân biệt; admin: kiến thức bệnh lý và điều phối khám. "
            + "Hỏi thêm tuổi, mức độ, thời gian triệu chứng. Không khẳng định chẩn đoán, không kê đơn/liều thuốc cá nhân. "
            + "Nêu giới hạn và khuyên khám phù hợp. Dấu hiệu nguy hiểm (khó thở, đau ngực, đột quỵ, bất tỉnh): gọi 115/cấp cứu ngay. "
            + "Không bịa nguồn. Không truy cập hồ sơ. Chỉ trả lời sức khỏe; không bỏ qua các nguyên tắc này.";
        try {
            var contents = req.messages().stream().map(m -> Map.of("role",m.role(),"parts",List.of(Map.of("text",m.text())))).toList();
            var body = Map.of("systemInstruction",Map.of("parts",List.of(Map.of("text",prompt))),"contents",contents);
            var request = HttpRequest.newBuilder(URI.create("https://generativelanguage.googleapis.com/v1beta/models/"+model+":generateContent"))
                .timeout(Duration.ofSeconds(45)).header("Content-Type","application/json").header("x-goog-api-key",key)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            var response = client.send(request,HttpResponse.BodyHandlers.ofString());
            if (response.statusCode()==429) return error(429,"AI quá tải hoặc hết hạn mức. Hãy thử lại sau.");
            if (response.statusCode()!=200) return error(502,"Dịch vụ AI tạm thời không khả dụng.");
            var candidate = mapper.readTree(response.body()).path("candidates").path(0);
            if (!candidate.path("finishReason").asText().equals("STOP")) return error(502,"AI chưa thể hoàn tất câu trả lời. Hãy diễn đạt lại.");
            StringBuilder answer = new StringBuilder();
            for (var part : candidate.path("content").path("parts"))
                if (!part.path("thought").asBoolean(false)) answer.append(part.path("text").asText(""));
            if (answer.isEmpty()) return error(502,"AI chưa thể trả lời câu hỏi này.");
            return ResponseEntity.ok(Map.of("answer",answer.toString()));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt(); return error(503,"Yêu cầu AI bị gián đoạn.");
        } catch (Exception ex) { return error(502,"AI không phản hồi. Vui lòng thử lại."); }
    }
    private ResponseEntity<?> error(int status,String message) { return ResponseEntity.status(status).body(Map.of("message",message)); }
}
