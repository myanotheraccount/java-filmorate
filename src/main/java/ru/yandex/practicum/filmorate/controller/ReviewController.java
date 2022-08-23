package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review item) {
        return reviewService.create(item);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review item) {
        return reviewService.update(item);
    }

    @GetMapping("/{id}")
    public Review get(@PathVariable("id") Long id) {
        return reviewService.get(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        reviewService.delete(id);
    }

    //Получение всех отзывов по идентификатору фильма, если фильм не указан, то по всем.
    @GetMapping()
    public List<Review> getAll(@RequestParam(required = false) Long filmId
            , @RequestParam(required = false, defaultValue = "10") Integer count) {

        if (filmId == null) {
            return reviewService.getAll();
        } else {
            return reviewService.getReviewsByFilmIdCount(filmId, count);
        }
    }

    //пользователь ставит лайк отзыву
    @PutMapping("/{id}/like/{userId}")
    public void addLikeReviewById(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addLikeReview(id, userId);
    }

    //пользователь ставит дизлайк отзыву
    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeReviewById(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addDislikeReview(id, userId);
    }

    //пользователь удаляет лайк отзыву
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeReviewById(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteLikeReview(id, userId);
    }

    //пользователь удаляет дизлайк отзыву
    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeReviewById(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteDislikeReview(id, userId);
    }
}
