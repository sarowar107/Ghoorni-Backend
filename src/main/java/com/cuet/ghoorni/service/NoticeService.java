package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.Notice;
import com.cuet.ghoorni.model.Notification;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.model.Notification;
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

    @Autowired
    private NotificationService notificationService;

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

        Notice savedNotice = noticeRepository.save(notice);

        // Create notifications for targeted users
        createNoticeNotifications(savedNotice);

        return savedNotice;
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

        // Delete associated notifications before deleting the notice
        notificationService.deleteNotificationsByReferenceId(id.toString(),
                Notification.NotificationType.NOTICE_CREATED);

        noticeRepository.delete(notice);
    }

    private void createNoticeNotifications(Notice notice) {
        try {
            // Find all users who should receive this notice
            List<User> targetUsers = findTargetUsersForNotice(notice);

            for (User targetUser : targetUsers) {
                // Don't notify the creator of their own notice
                if (!targetUser.getUserId().equals(notice.getCreatedBy().getUserId())) {
                    String title = "New Notice: " + notice.getTitle();
                    String message = "A new notice has been posted by " + notice.getCreatedBy().getName();

                    notificationService.createNotification(
                            targetUser,
                            title,
                            message,
                            Notification.NotificationType.NOTICE_CREATED,
                            notice.getNoticeId().toString());
                }
            }
        } catch (Exception e) {
            // Log the error but don't fail the notice creation
            System.err.println("Failed to create notice notifications: " + e.getMessage());
        }
    }

    private List<User> findTargetUsersForNotice(Notice notice) {
        List<User> targetUsers = new ArrayList<>();

        // If notice is for ALL departments
        if ("ALL".equals(notice.getToDept())) {
            if ("1".equals(notice.getToBatch())) {
                // All users
                targetUsers = userRepository.findAll();
            } else {
                // All users in specific batch
                targetUsers = userRepository.findByBatch(notice.getToBatch());
            }
        } else {
            // Specific department
            if ("1".equals(notice.getToBatch())) {
                // All users in specific department
                targetUsers = userRepository.findByDeptName(notice.getToDept());
            } else {
                // Users in specific department and batch
                targetUsers = userRepository.findByDeptNameAndBatch(notice.getToDept(), notice.getToBatch());
            }
        }

        return targetUsers;
    }
}