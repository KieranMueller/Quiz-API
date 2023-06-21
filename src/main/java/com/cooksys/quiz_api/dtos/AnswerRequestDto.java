package com.cooksys.quiz_api.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AnswerRequestDto {

    private Long id;

    private String text;

    private boolean correct = false;

    private QuestionRequestDto question;
}
