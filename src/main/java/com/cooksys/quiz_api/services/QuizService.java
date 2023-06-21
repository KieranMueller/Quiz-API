package com.cooksys.quiz_api.services;

import java.util.List;
import java.util.Optional;

import com.cooksys.quiz_api.dtos.QuestionRequestDto;
import com.cooksys.quiz_api.dtos.QuestionResponseDto;
import com.cooksys.quiz_api.dtos.QuizRequestDto;
import com.cooksys.quiz_api.dtos.QuizResponseDto;
import org.springframework.http.ResponseEntity;

public interface QuizService {

  ResponseEntity<List<QuizResponseDto>> getAllQuizzes();

  ResponseEntity<QuizResponseDto> createQuiz(QuizRequestDto quizRequestDto);

  ResponseEntity<QuizResponseDto> deleteQuizById(Long id);

  ResponseEntity<QuizResponseDto> renameQuiz(Long id, String newName);

  ResponseEntity<QuestionResponseDto> getRandomQuestion(Long id);

  ResponseEntity<QuizResponseDto> addQuestion(Long id, QuestionRequestDto questionRequestDto);

  ResponseEntity<QuestionResponseDto> deleteQuestion(Long id, Long questionId);
}
