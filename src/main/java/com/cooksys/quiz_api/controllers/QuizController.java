package com.cooksys.quiz_api.controllers;

import java.util.List;

import com.cooksys.quiz_api.dtos.QuestionRequestDto;
import com.cooksys.quiz_api.dtos.QuestionResponseDto;
import com.cooksys.quiz_api.dtos.QuizRequestDto;
import com.cooksys.quiz_api.dtos.QuizResponseDto;
import com.cooksys.quiz_api.services.QuizService;

import com.cooksys.quiz_api.services.impl.QuizServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/quiz")
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<QuizResponseDto> getAllQuizzes() {
        return quizService.getAllQuizzes();
    }

    // TODO: Implement the remaining 6 endpoints from the documentation.

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponseDto createQuiz(@RequestBody QuizRequestDto quizRequestDto) {
        return quizService.createQuiz(quizRequestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public QuizResponseDto deleteQuizById(@PathVariable Long id) {
        return quizService.deleteQuizById(id);
    }

    @PatchMapping("/{id}/rename/{newName}")
    @ResponseStatus(HttpStatus.OK)
    public QuizResponseDto renameQuiz(@PathVariable Long id, @PathVariable String newName) {
        return quizService.renameQuiz(id, newName);
    }

    @GetMapping("/{id}/random")
    @ResponseStatus(HttpStatus.OK)
    public QuestionResponseDto getRandomQuestion(@PathVariable Long id) {
        return quizService.getRandomQuestion(id);
    }

    @PatchMapping("/{id}/add")
    public QuizResponseDto addQuestion(@PathVariable Long id, @RequestBody QuestionRequestDto questionRequestDto) {
        return quizService.addQuestion(id, questionRequestDto);
    }

    @DeleteMapping("/{id}/delete/{questionId}")
    @ResponseStatus(HttpStatus.OK)
    public QuestionResponseDto deleteQuestion(@PathVariable Long id, @PathVariable Long questionId) {
        return quizService.deleteQuestion(id, questionId);
    }

}
