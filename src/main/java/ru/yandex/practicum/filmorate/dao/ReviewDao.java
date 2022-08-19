package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewDao {
    Review createReview(Review review);

    Review getReviewById(Long id);

    List<Review> getAll();

    Review updateReview(Review review);

    List<Review> getReviewsByFilmId(Long filmId, Integer count);

    List<Review> getReviewsTop(Integer count);

    void delete(Long id);

    void addLike(Long id, Long userId);

    void addDislike(Long id, Long userId);

    void deleteLikeDislike(Long id, Long userId);

    void updateUsefulByReview(Review review);

}
