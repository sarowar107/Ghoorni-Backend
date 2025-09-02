package com.cuet.ghoorni.repository;

import com.cuet.ghoorni.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByToDeptAndToBatchOrderByCreatedAtDesc(String toDept, String toBatch);

    List<Question> findByToDeptAndToBatchInOrderByCreatedAtDesc(String toDept, List<String> toBatchList);

    List<Question> findByToDeptInAndToBatchInOrderByCreatedAtDesc(List<String> toDeptList, List<String> toBatchList);
}