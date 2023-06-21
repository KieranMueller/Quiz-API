package com.cooksys.quiz_api.services.impl;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.cooksys.quiz_api.dtos.*;
import com.cooksys.quiz_api.entities.Answer;
import com.cooksys.quiz_api.entities.Question;
import com.cooksys.quiz_api.entities.Quiz;
import com.cooksys.quiz_api.mappers.AnswerMapper;
import com.cooksys.quiz_api.mappers.QuestionMapper;
import com.cooksys.quiz_api.mappers.QuizMapper;
import com.cooksys.quiz_api.repositories.AnswerRepository;
import com.cooksys.quiz_api.repositories.QuestionRepository;
import com.cooksys.quiz_api.repositories.QuizRepository;
import com.cooksys.quiz_api.services.QuizService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<QuizResponseDto>> getAllQuizzes() {
        return new ResponseEntity<>(quizMapper.entitiesToDtos(quizRepository.findAll()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<QuizResponseDto> createQuiz(QuizRequestDto quizRequestDto) {
        // Implemented: If name for quiz sent in is blank, return bad request, quiz can be sent with just name.
        // quiz can be sent with name, question, and empty array answers/no answers. If question is sent with
        // answers, returns bad request if there is no correct/true answer

        // Ask about condensing this. Seems tough to do since I need to check if getQuestions and getAnswers
        // are null before seeing if any of their elements are empty, throws null pointer exception

        // Add message to go with bad request errors
        if (quizRequestDto == null || quizRequestDto.getName() == null || quizRequestDto.getQuestions() == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (quizRequestDto.getName().isBlank() || quizRequestDto.getQuestions().size() == 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        List<QuestionRequestDto> questions = quizRequestDto.getQuestions();
        for (var question : questions) {
            if (question.getAnswers() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if(question.getText().isBlank() || question.getText() == null || question.getAnswers().size() == 0) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        for (var question : questions) {
            var answers = question.getAnswers();
            int length = question.getAnswers().size();
            int i = 0;
            for (var answer : answers) {
                if(answer.getText() == null)
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                if(answer.getText().isBlank())
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                if (!answer.isCorrect())
                    i++;
            }
            if (i == length && i > 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // public ResponseEntity<QuizResponseDto> createQuiz(QuizRequestDto quizRequestDto)
        Quiz quiz = quizRepository.saveAndFlush(quizMapper.requestDtoToEntity(quizRequestDto));
        for(var q : quiz.getQuestions()) {
            q.setQuiz(quiz);
            questionRepository.saveAndFlush(q);
            for(var a : q.getAnswers()) {
                a.setQuestion(q);
                answerRepository.saveAndFlush(a);
            }
        }
        return new ResponseEntity<>(quizMapper.entityToDto(quiz), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<QuizResponseDto> deleteQuizById(Long id) {
        // Add custom body/message to go with 404 not found
        Optional<Quiz> quizToReturn = quizRepository.findById(id);
        if (quizToReturn.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        quizRepository.deleteById(id);
        return new ResponseEntity<>(quizMapper.entityToDto(quizToReturn.get()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<QuizResponseDto> renameQuiz(Long id, String newName) {
        // Implemented: Throws bad request if name passed is blank, throws Not found if id invalid
        // Add messages to go with statuses
        if (newName.isBlank())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Optional<Quiz> quiz = quizRepository.findById(id);
        if (quiz.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        quiz.map(q -> {
            q.setName(newName);
            return quizRepository.saveAndFlush(q);
        });
        return new ResponseEntity<>(quizMapper.entityToDto(quiz.get()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<QuestionResponseDto> getRandomQuestion(Long id) {
        // Add custom message to 404 not found
        Optional<Quiz> quiz = quizRepository.findById(id);
        // Leaving this in as a joke, it worked! This could be the last line, but jeeze is that ugly...
//        return quiz.map(value -> new ResponseEntity<>(questionMapper.entityToDto(value.getQuestions().get(new Random().nextInt(value.getQuestions().size()))), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        if (quiz.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(
                questionMapper.entityToDto(
                        quiz.get().getQuestions().get(
                                new Random().nextInt(
                                        quiz.get().getQuestions().size()
                                )
                        )
                ), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<QuizResponseDto> addQuestion(Long id, QuestionRequestDto questionRequestDto) {
        // Implemented: If quiz ID does not exist, 404 Not Found.
        // If question coming in is null OR the text is null OR only blank spaces OR there
        // are NO answers (to include empty answer array), bad request.
        // If ANY answer is null or blank, bad request. If there
        // is no correct answer, bad request.

        // Add messages to errors
        if (questionRequestDto == null || questionRequestDto.getText() == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (questionRequestDto.getText().isBlank())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Optional<Quiz> quizToAddTo = quizRepository.findById(id);

        if (quizToAddTo.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if (questionRequestDto.getAnswers() == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (questionRequestDto.getAnswers().size() == 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        int i = 0;
        for (var answer : questionRequestDto.getAnswers()) {
            if (answer.getText() == null)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            if (answer.getText().isBlank())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            if (answer.isCorrect())
                i++;
        }
        if (i == 0 && questionRequestDto.getAnswers().size() > 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        // Reference this for help with children that have null foreign keys, this seems to work
        Question question = questionMapper.requestDtoToEntity(questionRequestDto);
        List<Answer> answers = question.getAnswers();
        answers.forEach(a -> a.setQuestion(question));
        question.setQuiz(quizToAddTo.get());
        questionRepository.saveAndFlush(question);
        return new ResponseEntity<>(quizMapper.entityToDto(quizRepository.saveAndFlush(quizToAddTo.get())), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<QuestionResponseDto> deleteQuestion(Long id, Long questionId) {
        // Implemented: If invalid ID passed for quiz OR question, not found.
        // If question does not belong to the quiz (with the quiz ID passed) return not found
        // Deleting all children, removing all orphan answers
        // Add custom error messages
        Optional<Quiz> opQuiz = quizRepository.findById(id);
        Optional<Question> opQuestion = questionRepository.findById(questionId);
        if (opQuiz.isEmpty() || opQuestion.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Quiz quiz = opQuiz.get();
        Question question = opQuestion.get();
        if (quiz.getQuestions().contains(question)) {
            quiz.removeQuestion(question);
            questionRepository.delete(question);
            quizRepository.saveAndFlush(quiz);
            return new ResponseEntity<>(questionMapper.entityToDto(question), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
