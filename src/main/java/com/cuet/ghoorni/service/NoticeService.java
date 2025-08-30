package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.Notice;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.repository.NoticeRepository;
import com.cuet.ghoorni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private UserRepository userRepository;

    public Notice createNotice(Notice notice, String userId) {
        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found!");
        }
        notice.setCreatedBy(user);
        notice.setCreatedAt(LocalDateTime.now());
        return noticeRepository.save(notice);
    }

    public List<Notice> findAllNotices() {
        // Sort notices by creation date (newest first)
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    public Notice findNoticeById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found with id: " + id));
    }

    public void deleteNotice(Long id, String userId) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found with id: " + id));

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        // Check if the user is the creator or an admin
        if (!notice.getCreatedBy().getUserId().equals(userId) && !user.getRole().equals("admin")) {
            throw new RuntimeException("You don't have permission to delete this notice");
        }

        noticeRepository.delete(notice);
    }
}