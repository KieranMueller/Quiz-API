package com.cooksys.quiz_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequestDto {

    private Long id;

    private String name;

    private List<QuestionRequestDto> questions;

}
