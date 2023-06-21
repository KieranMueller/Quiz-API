package com.cooksys.quiz_api.services;

import java.util.List;
import java.util.Optional;

import com.cooksys.quiz_api.dtos.QuestionRequestDto;
import com.cooksys.quiz_api.dtos.QuestionResponseDto;
import com.cooksys.quiz_api.dtos.QuizRequestDto;
import com.cooksys.quiz_api.dtos.QuizResponseDto;
import org.springframework.http.ResponseEntity;

public interface QuizService {

  List<QuizResponseDto> getAllQuizzes();

  QuizResponseDto createQuiz(QuizRequestDto quizRequestDto);

  QuizResponseDto deleteQuizById(Long id);

  QuizResponseDto renameQuiz(Long id, String newName);

  QuestionResponseDto getRandomQuestion(Long id);

  QuizResponseDto addQuestion(Long id, QuestionRequestDto questionRequestDto);

  QuestionResponseDto deleteQuestion(Long id, Long questionId);
}
