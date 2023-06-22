package com.cooksys.quiz_api.services.impl;

import com.cooksys.quiz_api.dtos.*;
import com.cooksys.quiz_api.entities.Answer;
import com.cooksys.quiz_api.entities.Question;
import com.cooksys.quiz_api.entities.Quiz;
import com.cooksys.quiz_api.exception.BadRequestException;
import com.cooksys.quiz_api.exception.NotFoundException;
import com.cooksys.quiz_api.mappers.AnswerMapper;
import com.cooksys.quiz_api.mappers.QuestionMapper;
import com.cooksys.quiz_api.mappers.QuizMapper;
import com.cooksys.quiz_api.repositories.AnswerRepository;
import com.cooksys.quiz_api.repositories.QuestionRepository;
import com.cooksys.quiz_api.repositories.QuizRepository;
import com.cooksys.quiz_api.services.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

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
        return quizMapper.entitiesToDtos(quizRepository.findAllByDeletedFalse());
    }

    @Override
    public QuizResponseDto createQuiz(QuizRequestDto quizRequestDto) {
        // Implemented: Body MUST include Quiz with name, with questions, with at least one correct answer
        if (quizRequestDto == null || quizRequestDto.getName() == null || quizRequestDto.getQuestions() == null)
            throw new BadRequestException("Quiz Must Include \"name\" and \"questions\"");
        if (quizRequestDto.getName().isBlank() || quizRequestDto.getQuestions().size() == 0)
            throw new BadRequestException("Quiz Must Include \"name\" and \"questions\"");
        List<QuestionRequestDto> questions = quizRequestDto.getQuestions();
        for (var question : questions) {
            if (question.getAnswers() == null)
                throw new BadRequestException("\"questions\" Must Include \"text\" And \"answers\"");
            if (question.getText().isBlank() || question.getText() == null || question.getAnswers().size() == 0)
                throw new BadRequestException("\"questions\" Must Have \"text\", and \"answers\"");
            var answers = question.getAnswers();
            int i = 0;
            for (var answer : answers) {
                if (answer.getText() == null)
                    throw new BadRequestException("\"answers\" Can Not Be Null");
                if (answer.getText().isBlank())
                    throw new BadRequestException("\"answers\", \"text\" Can Not Be Blank");
                if (answer.isCorrect())
                    i++;
            }
            if (i == 0) throw new BadRequestException("\"questions\" Must Include At Least One \"correct\" Answer");
        }

        Quiz quiz = quizRepository.saveAndFlush(quizMapper.requestDtoToEntity(quizRequestDto));
        for (var q : quiz.getQuestions()) {
            q.setQuiz(quiz);
            questionRepository.saveAndFlush(q);
            for (var a : q.getAnswers()) {
                a.setQuestion(q);
                answerRepository.saveAndFlush(a);
            }
        }
        return quizMapper.entityToDto(quiz);
    }

    @Override
    public QuizResponseDto deleteQuizById(Long id) {
        // Implemented: Soft deletes quiz and children (Questions and Answers)
        Optional<Quiz> opQuiz = quizRepository.findByIdAndDeletedFalse(id);
        if (opQuiz.isEmpty())
            throw new NotFoundException("Unable To Find Quiz With ID " + id);
        Quiz quiz = opQuiz.get();
        quiz.setDeleted(true);
        for(var question : quiz.getQuestions()) {
            question.setDeleted(true);
            questionRepository.saveAndFlush(question);
            for(var answer : question.getAnswers()) {
                answer.setDeleted(true);
                answerRepository.saveAndFlush(answer);
            }
        }
        return quizMapper.entityToDto(quizRepository.saveAndFlush(quiz));
    }

    @Override
    public QuizResponseDto renameQuiz(Long id, String newName) {
        // Implemented: Throws bad request if name passed is null, blank, and throws Not found if id invalid
        if (newName == null)
            throw new BadRequestException("New Name Cannot Be Null");
        if (newName.isBlank())
            throw new BadRequestException("New Name Cannot Be Blank");
        Optional<Quiz> quiz = quizRepository.findByIdAndDeletedFalse(id);
        if (quiz.isEmpty())
            throw new BadRequestException("Unable To Find Quiz With ID " + id);
        quiz.map(q -> {
            q.setName(newName);
            return quizRepository.saveAndFlush(q);
        });
        return quizMapper.entityToDto(quiz.get());
    }

    @Override
    public QuestionResponseDto getRandomQuestion(Long id) {
        Optional<Quiz> opQuiz = quizRepository.findByIdAndDeletedFalse(id);
        if (opQuiz.isEmpty())
            throw new NotFoundException("Unable To Find Quiz With ID " + id);
        Quiz quiz = opQuiz.get();
        return questionMapper.entityToDto(
                quiz.getQuestions().get(
                        new Random().nextInt(
                                quiz.getQuestions().size()
                        )
                )
        );
    }

    @Override
    public QuizResponseDto addQuestion(Long id, QuestionRequestDto questionRequestDto) {
        // Implemented: If quiz ID does not exist, throws Not Found. If question coming in
        // is null OR text is null OR blank Bad Request. If ANY answer is null or blank, bad request.
        // If there is no correct answer, bad request.
        Optional<Quiz> opQuiz = quizRepository.findByIdAndDeletedFalse(id);
        if (opQuiz.isEmpty())
            throw new NotFoundException("Unable To Find Quiz With ID " + id);
        if (questionRequestDto == null || questionRequestDto.getText() == null)
            throw new BadRequestException("Question Must Include Text");
        if (questionRequestDto.getText().isBlank())
            throw new BadRequestException("Question Must Include Text");
        if (questionRequestDto.getAnswers() == null)
            throw new BadRequestException("Question Must Include Answer(s)");
        if (questionRequestDto.getAnswers().size() == 0)
            throw new BadRequestException("Question Must Include Answer(s)");
        int i = 0;
        for (var answer : questionRequestDto.getAnswers()) {
            if (answer.getText() == null)
                throw new BadRequestException("Question Must Include Answer(s)");
            if (answer.getText().isBlank())
                throw new BadRequestException("Answer Cannot Be Blank");
            if (answer.isCorrect())
                i++;
        }
        if (i == 0) throw new BadRequestException("Question Must Include At Least One Correct Answer");

        Quiz quiz = opQuiz.get();
        Question question = questionMapper.requestDtoToEntity(questionRequestDto);
        List<Answer> answers = question.getAnswers();

        answers.forEach(a -> a.setQuestion(question));
        question.setQuiz(quiz);
        questionRepository.saveAndFlush(question);
        return quizMapper.entityToDto(quizRepository.saveAndFlush(quiz));
    }

    @Override
    public QuestionResponseDto deleteQuestion(Long id, Long questionId) {
        // Implemented: If invalid ID passed for quiz OR question, not found.
        // If question does not belong to the quiz (with the quiz ID passed) return not found
        // Sets question deleted flag to true and all children/answers
        Optional<Quiz> opQuiz = quizRepository.findByIdAndDeletedFalse(id);
        Optional<Question> opQuestion = questionRepository.findByIdAndDeletedFalse(questionId);
        if (opQuiz.isEmpty())
            throw new NotFoundException("Unable To Find Quiz With ID " + id);
        if (opQuestion.isEmpty())
            throw new NotFoundException("Unable To Find Question With ID " + questionId);
        Quiz quiz = opQuiz.get();
        Question question = opQuestion.get();
        if (quiz.getQuestions().contains(question)) {
            question.setDeleted(true);
            for(var answer : question.getAnswers()) {
                answer.setDeleted(true);
                answerRepository.saveAndFlush(answer);
            }
            return questionMapper.entityToDto(questionRepository.saveAndFlush(question));
        }
        throw new NotFoundException("Unable To Find Question With ID " + questionId);
    }

    @Override
    public AnswerResponseDto deleteAnswer(Long id, Long qId, Long aId) {
        // Problem, deleted answer still showing up when getting all quizzes
        Optional<Quiz> opQuiz = quizRepository.findByIdAndDeletedFalse(id);
        Optional<Question> opQuestion = questionRepository.findByIdAndDeletedFalse(qId);
        Optional<Answer> opAnswer = answerRepository.findByIdAndDeletedFalse(aId);
        if(opQuiz.isEmpty())
            throw new NotFoundException("Unable To Find Quiz With ID " + id);
        if(opQuestion.isEmpty())
            throw new NotFoundException("Unable To Find Question With ID " + qId);
        if(opAnswer.isEmpty())
            throw new NotFoundException("Unable To Find Answer With ID " + aId);

        Quiz quiz = opQuiz.get();
        Question question = opQuestion.get();
        Answer answer = opAnswer.get();

        if(quiz.getQuestions().contains(question)) {
            if(question.getAnswers().contains(answer)) {
                answer.setDeleted(true);
                answerRepository.saveAndFlush(answer);
                return answerMapper.entityToDto(answer);
            }
            questionRepository.saveAndFlush(question);
            quizRepository.saveAndFlush(quiz);
        }

        throw new NotFoundException("Could Not Find Answer With ID " + aId + " Within Question With ID " + qId + " Within Quiz With ID " + id);
    }
}
