package com.cooksys.quiz_api.services.impl;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Random;

import com.cooksys.quiz_api.dtos.QuestionRequestDto;
import com.cooksys.quiz_api.dtos.QuestionResponseDto;
import com.cooksys.quiz_api.dtos.QuizRequestDto;
import com.cooksys.quiz_api.dtos.QuizResponseDto;
import com.cooksys.quiz_api.entities.Question;
import com.cooksys.quiz_api.entities.Quiz;
import com.cooksys.quiz_api.mappers.AnswerMapper;
import com.cooksys.quiz_api.mappers.QuestionMapper;
import com.cooksys.quiz_api.mappers.QuizMapper;
import com.cooksys.quiz_api.repositories.AnswerRepository;
import com.cooksys.quiz_api.repositories.QuestionRepository;
import com.cooksys.quiz_api.repositories.QuizRepository;
import com.cooksys.quiz_api.services.QuizService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuizMapper quizMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;

    @Override
    public List<QuizResponseDto> getAllQuizzes() {
        return quizMapper.entitiesToDtos(quizRepository.findAll());
    }

    @Override
    public QuizResponseDto createQuiz(QuizRequestDto quizRequestDto) {
        // Any error handling to add?
        var quizToSave = quizRepository.saveAndFlush(quizMapper.requestDtoToEntity(quizRequestDto));
        return quizMapper.entityToDto(quizToSave);
    }

    @Override
    public QuizResponseDto deleteQuizById(Long id) {
        // Add error handling for id passed that doesn't exist
        var quizToReturn = quizRepository.getById(id);
        quizRepository.deleteById(id);
        return quizMapper.entityToDto(quizToReturn);
    }

    @Override
    public QuizResponseDto renameQuiz(Long id, String newName) {
        // Add error handling for an id passed that doesn't exist
        var quizToSave = quizRepository.findById(id).map((q) -> {
            q.setName(newName);
            return quizRepository.saveAndFlush(q);
        }).orElseThrow();
        return quizMapper.entityToDto(quizToSave);
    }

    @Override
    public QuestionResponseDto getRandomQuestion(Long id) {
        // Add error handling
        var quiz = quizRepository.findById(id).orElseThrow();
        return questionMapper.entityToDto(quiz.getQuestions().get(new Random().nextInt(quiz.getQuestions().size())));
    }

    @Override
    public QuizResponseDto addQuestion(Long id, QuestionRequestDto questionRequestDto) {
        Quiz quizToAddTo = quizRepository.findById(id).orElseThrow();
        Question question = questionMapper.requestDtoToEntity(questionRequestDto);
        question.setQuiz(quizToAddTo);
        questionRepository.saveAndFlush(question);
        return quizMapper.entityToDto(quizRepository.saveAndFlush(quizToAddTo));
    }

    @Override
    public QuestionResponseDto deleteQuestion(Long id, Long questionId) {
        // Add Error handling
        Quiz quiz = quizRepository.findById(id).orElseThrow();
        Question question = questionRepository.findById(questionId).orElseThrow();
        if(quiz.getQuestions().contains(question)) {
            quiz.removeQuestion(question);
            questionRepository.deleteById(questionId);
            quizRepository.saveAndFlush(quiz);
            return questionMapper.entityToDto(question);
        }
        throw new RuntimeException();
    }
}
