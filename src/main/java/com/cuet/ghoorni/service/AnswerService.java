package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.Answer;
import com.cuet.ghoorni.model.Question;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.payload.AnswerRequest;
import com.cuet.ghoorni.repository.AnswerRepository;
import com.cuet.ghoorni.repository.QuestionRepository;
import com.cuet.ghoorni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    public Answer submitAnswer(AnswerRequest answerRequest, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        Question question = questionRepository.findById(answerRequest.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found!"));

        Answer answer = new Answer();
        answer.setContent(answerRequest.getContent());
        answer.setAnsweredBy(user);
        answer.setQuestion(question);
        answer.setCreatedAt(LocalDateTime.now());
        answer.setUpdatedAt(LocalDateTime.now());

        return answerRepository.save(answer);
    }

    public List<Answer> findAnswersByQuestionId(Long questionId) {
        return answerRepository.findByQuestionQuestionId(questionId);
    }
}