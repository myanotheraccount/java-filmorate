package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "reviewId")
public class Review {
    @Positive
    private Long reviewId;
    @NotNull
    private String content;
    @NotNull
    @JsonProperty("isPositive")
    private Boolean isPositive;
    private Integer useful;
    @NotNull
    private Long userId;
    @NotNull
    private Long filmId;
}
