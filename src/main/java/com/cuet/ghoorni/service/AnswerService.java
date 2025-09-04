package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.Answer;
import com.cuet.ghoorni.model.Notification;
import com.cuet.ghoorni.model.Question;
import com.cuet.ghoorni.model.User;
import com.cuet.ghoorni.model.Notification;
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

    @Autowired
    private NotificationService notificationService;

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

        Answer savedAnswer = answerRepository.save(answer);

        // Create notification for the question author
        createAnswerNotification(savedAnswer, question);

        return savedAnswer;
    }

    public List<Answer> findAnswersByQuestionId(Long questionId) {
        return answerRepository.findByQuestionQuestionId(questionId);
    }

    public void deleteAnswer(Long answerId, String userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the user is the answer creator or an admin
        if (!answer.getAnsweredBy().getUserId().equals(userId) && !user.getRole().equals("admin")) {
            throw new RuntimeException("You don't have permission to delete this answer");
        }

        // Delete associated notifications before deleting the answer
        notificationService.deleteNotificationsByReferenceId(answerId.toString(),
                Notification.NotificationType.QUESTION_ANSWERED);

        answerRepository.delete(answer);
    }

    private void createAnswerNotification(Answer answer, Question question) {
        try {
            User questionAuthor = question.getAskedBy();
            User answerAuthor = answer.getAnsweredBy();

            // Don't notify if the person is answering their own question
            if (!questionAuthor.getUserId().equals(answerAuthor.getUserId())) {
                String title = "New Answer to Your Question";
                String message = answerAuthor.getName() + " has answered your question: " + question.getTitle();

                notificationService.createNotification(
                        questionAuthor,
                        title,
                        message,
                        Notification.NotificationType.QUESTION_ANSWERED,
                        answer.getAnsId().toString());
            }
        } catch (Exception e) {
            // Log the error but don't fail the answer creation
            System.err.println("Failed to create answer notification: " + e.getMessage());
        }
    }
}