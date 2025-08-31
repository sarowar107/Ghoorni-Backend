package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.model.Notice;
import com.cuet.ghoorni.payload.NoticeResponse;
import com.cuet.ghoorni.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/api/notices")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @PostMapping("/create")
    public ResponseEntity<Notice> createNotice(@RequestBody Notice notice, Authentication authentication) {
        Notice newNotice = noticeService.createNotice(notice, authentication.getName());
        return new ResponseEntity<>(newNotice, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<NoticeResponse>> getAllNotices() {
        List<Notice> notices = noticeService.findAllNotices();
        List<NoticeResponse> responses = notices.stream()
                .map(NoticeResponse::fromEntity)
                .toList();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getNoticeById(@PathVariable Long id) {
        Notice notice = noticeService.findNoticeById(id);
        NoticeResponse response = NoticeResponse.fromEntity(notice);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotice(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            noticeService.deleteNotice(id, authentication.getName());
            return ResponseEntity.ok().body("Notice deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}