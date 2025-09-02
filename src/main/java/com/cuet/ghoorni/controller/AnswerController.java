package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.model.Answer;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.payload.AnswerRequest;
import com.cuet.ghoorni.payload.AnswerResponse;
import com.cuet.ghoorni.service.AnswerService;
import com.cuet.ghoorni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/submit")
    public ResponseEntity<?> submitAnswer(@RequestBody AnswerRequest answerRequest,
            Authentication authentication) {
        // Check if user's email is verified
        User user = userRepository.findByUserId(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmailVerified() == null || !user.getEmailVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Email verification required to answer questions");
        }

        Answer newAnswer = answerService.submitAnswer(answerRequest, authentication.getName());
        return new ResponseEntity<>(AnswerResponse.fromEntity(newAnswer), HttpStatus.CREATED);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerResponse>> getAnswersByQuestionId(@PathVariable Long questionId) {
        List<Answer> answers = answerService.findAnswersByQuestionId(questionId);
        List<AnswerResponse> responses = answers.stream()
                .map(AnswerResponse::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @DeleteMapping("/{answerId}")
    public ResponseEntity<?> deleteAnswer(@PathVariable Long answerId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            answerService.deleteAnswer(answerId, authentication.getName());
            return ResponseEntity.ok().body("Answer deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}