package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.Question;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.repository.QuestionRepository;
import com.cuet.ghoorni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    public Question askQuestion(Question question, String userId) {
        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found!");
        }
        question.setAskedBy(user);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());

        // Set department and batch based on user role
        if ("teacher".equalsIgnoreCase(user.getRole())) {
            question.setToDept(user.getDeptName());
            // toBatch should be set by the frontend for teachers
            if (question.getToBatch() == null || question.getToBatch().isEmpty()) {
                throw new RuntimeException("Teachers must specify a target batch");
            }
        } else if ("cr".equalsIgnoreCase(user.getRole()) || "student".equalsIgnoreCase(user.getRole())) {
            question.setToDept(user.getDeptName());
            question.setToBatch(user.getBatch());
        } else if ("admin".equalsIgnoreCase(user.getRole())) {
            // Use frontend values or defaults for admin
            question.setToDept(question.getToDept() != null ? question.getToDept() : "ALL");
            question.setToBatch(question.getToBatch() != null ? question.getToBatch() : "1");
        }
        return questionRepository.save(question);
    }

    public List<Question> findAllQuestions(String userId) {
        User currentUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Question> allQuestions = questionRepository.findAll();

        return allQuestions.stream()
                .filter(question -> {
                    // Question creator can always see their own questions
                    if (question.getAskedBy().getUserId().equals(userId)) {
                        return true;
                    }

                    // Admin can see all questions
                    if ("admin".equalsIgnoreCase(currentUser.getRole())) {
                        return true;
                    }

                    // If question is targeted to specific department/batch
                    boolean isDeptMatch = question.getToDept().equals("ALL") ||
                            question.getToDept().equals(currentUser.getDeptName());
                    boolean isBatchMatch = question.getToBatch().equals("1") ||
                            question.getToBatch().equals(currentUser.getBatch());

                    return isDeptMatch && isBatchMatch;
                })
                .collect(Collectors.toList());
    }

    public Question findQuestionById(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));
    }

    public void deleteQuestion(Long questionId, String userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        // Check if the user is the creator or an admin
        if (!question.getAskedBy().getUserId().equals(userId) && !user.getRole().equals("admin")) {
            throw new RuntimeException("You don't have permission to delete this question");
        }

        questionRepository.delete(question);
    }
}