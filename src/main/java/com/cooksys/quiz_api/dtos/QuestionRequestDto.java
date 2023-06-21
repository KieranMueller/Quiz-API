package com.cooksys.quiz_api.dtos;

import com.cooksys.quiz_api.entities.Answer;
import com.cooksys.quiz_api.entities.Quiz;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class QuestionRequestDto {

    private Long id;

    private String text;

    private List<AnswerRequestDto> answers;

    private QuizRequestDto quiz;
}
