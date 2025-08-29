package com.cuet.ghoorni.controller;

import com.cuet.ghoorni.model.Question;
import com.cuet.ghoorni.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping("/ask")
    public ResponseEntity<Question> askQuestion(@RequestBody Question question, Authentication authentication) {
        Question newQuestion = questionService.askQuestion(question, authentication.getName());
        return new ResponseEntity<>(newQuestion, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() {
        List<Question> questions = questionService.findAllQuestions();
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }
}