package com.cooksys.quiz_api.dtos;

import java.util.List;

import com.cooksys.quiz_api.entities.Quiz;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponseDto {

  private Long id;

  private String text;

  public List<AnswerResponseDto> answers;

}
