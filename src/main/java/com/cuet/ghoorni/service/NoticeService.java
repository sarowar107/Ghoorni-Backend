package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.Notice;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.repository.NoticeRepository;
import com.cuet.ghoorni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

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

        // Set department and batch based on user role
        if ("teacher".equalsIgnoreCase(user.getRole())) {
            notice.setToDept(user.getDeptName());
            // toBatch will be set by the frontend
        } else if ("cr".equalsIgnoreCase(user.getRole()) || "student".equalsIgnoreCase(user.getRole())) {
            notice.setToDept(user.getDeptName());
            notice.setToBatch(user.getBatch());
        } else if ("admin".equalsIgnoreCase(user.getRole())) {
            // toDept and toBatch will be set by the frontend
        }
        return noticeRepository.save(notice);
    }

    public List<Notice> findAllNotices(User user) {
        if ("admin".equalsIgnoreCase(user.getRole())) {
            // Admin can see all notices
            return noticeRepository.findAllByOrderByCreatedAtDesc();
        }

        List<Notice> visibleNotices = new ArrayList<>();

        // Add notices targeted to user's dept/batch
        List<String> deptList = new ArrayList<>();
        deptList.add(user.getDeptName());
        deptList.add("ALL");

        List<String> batchList = new ArrayList<>();
        batchList.add(user.getBatch());
        batchList.add("1"); // 1 means ALL batches

        visibleNotices.addAll(noticeRepository.findByToDeptInAndToBatchInOrderByCreatedAtDesc(deptList, batchList));

        // Add notices created by the user
        visibleNotices.addAll(noticeRepository.findByCreatedByUserIdOrderByCreatedAtDesc(user.getUserId()));

        // Remove duplicates while maintaining order
        return visibleNotices.stream()
                .distinct()
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
                .toList();
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