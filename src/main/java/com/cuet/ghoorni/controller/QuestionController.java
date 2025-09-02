package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.model.Question;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.payload.QuestionResponse;
import com.cuet.ghoorni.service.QuestionService;
import com.cuet.ghoorni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody Question question, Authentication authentication) {
        // Check if user's email is verified
        User user = userRepository.findByUserId(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmailVerified() == null || !user.getEmailVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Email verification required to ask questions");
        }

        Question newQuestion = questionService.askQuestion(question, authentication.getName());
        return new ResponseEntity<>(QuestionResponse.fromEntity(newQuestion), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAllQuestions(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Question> questions = questionService.findAllQuestions(authentication.getName());
        List<QuestionResponse> responses = questions.stream()
                .map(QuestionResponse::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Long questionId) {
        try {
            Question question = questionService.findQuestionById(questionId);
            return new ResponseEntity<>(QuestionResponse.fromEntity(question), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            questionService.deleteQuestion(questionId, authentication.getName());
            return ResponseEntity.ok().body("Question deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}