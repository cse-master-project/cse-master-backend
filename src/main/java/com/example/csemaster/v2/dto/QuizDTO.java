package com.example.csemaster.v2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class QuizDTO {
    @Schema(hidden = true)
    private Long quizId;

    @NotBlank
    private String subject;
    @NotBlank
    private String chapter;
    @NotNull
    @Min(value = 1, message = "NOT_VALID_STATE")
    @Max(value = 5, message = "NOT_VALID_STATE")
    private Integer quizType;
    private String jsonContent;
    private Boolean hasImage;

    @Schema(hidden = true)
    private Boolean isDeleted;
    @Schema(hidden = true)
    private String creator;
}
