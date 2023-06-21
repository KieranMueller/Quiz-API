package com.cooksys.quiz_api.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseDto {

  private Long id;

  private String name;

  private List<QuestionResponseDto> questions;

}
