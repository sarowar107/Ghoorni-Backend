package com.cuet.ghoorni.repository;

import com.cuet.ghoorni.model.NotificationSettings;
import com.cuet.ghoorni.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {

    Optional<NotificationSettings> findByUser(User user);

    boolean existsByUser(User user);
}
