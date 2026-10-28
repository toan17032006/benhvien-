package com.benhvien.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.NoSuchElementException;

/** Chuyển các lỗi Java thành phản hồi JSON thân thiện tiếng Việt cho frontend. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> khongTimThay(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> duLieuKhongHopLe(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> validateSai(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> "Trường '" + f.getField() + "' " + f.getDefaultMessage())
                .orElse("Dữ liệu gửi lên chưa hợp lệ.");
        return ResponseEntity.badRequest().body(java.util.Map.of("message", msg));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> jsonSai(Exception ex) {
        return ResponseEntity.badRequest().body(java.util.Map.of(
                "message", "Định dạng dữ liệu gửi lên không hợp lệ (kiểm tra ngày giờ, số liệu, enum)."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> xungDotKhoaNgoai(DataIntegrityViolationException ex) {
        return ResponseEntity.badRequest().body(java.util.Map.of(
                "message", "Dữ liệu đang được sử dụng ở bảng khác (ràng buộc khóa ngoại) nên không thể xóa/sửa."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> khongCoTaiNguyen(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", "Không tìm thấy tài nguyên."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> loiChung(Exception ex) {
        return ResponseEntity.internalServerError().body(java.util.Map.of(
                "message", "Lỗi máy chủ: " + ex.getClass().getSimpleName()));
    }
}
