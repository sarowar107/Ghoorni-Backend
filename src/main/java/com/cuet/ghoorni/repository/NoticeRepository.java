package com.cuet.ghoorni.repository;

import com.cuet.ghoorni.model.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findAllByOrderByCreatedAtDesc();

    List<Notice> findByToDeptAndToBatchOrderByCreatedAtDesc(String toDept, String toBatch);

    List<Notice> findByToDeptAndToBatchInOrderByCreatedAtDesc(String toDept, List<String> toBatchList);

    List<Notice> findByToDeptInAndToBatchInOrderByCreatedAtDesc(List<String> toDeptList, List<String> toBatchList);

    List<Notice> findByCreatedByUserIdOrderByCreatedAtDesc(String userId);
}