package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.Question;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.repository.QuestionRepository;
import com.cuet.ghoorni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

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
        return questionRepository.save(question);
    }

    public List<Question> findAllQuestions() {
        return questionRepository.findAll();
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