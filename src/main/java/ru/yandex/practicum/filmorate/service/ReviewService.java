package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.ReviewDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

@Service
public class ReviewService implements AbstractService<Review> {

    private final ReviewDao reviewDao;

    @Autowired
    public ReviewService(ReviewDao reviewDao) {
        this.reviewDao = reviewDao;

    }

    @Override
    public Review create(Review review) {
        validate(review);

        return reviewDao.createReview(review);
    }

    @Override
    public Review update(Review review) {
        return reviewDao.updateReview(review);
    }

    @Override
    public Review get(Long id) {
        return reviewDao.getReviewById(id);
    }

    @Override
    public List<Review> getAll() {
        return reviewDao.getAll();
    }

    @Override
    public void delete(Long id) {
        reviewDao.delete(id);
    }

    public List<Review> getReviewsByFilmIdCount(Long filmId, Integer count) {

        if (filmId == null) {
            return reviewDao.getReviewsTop(count);
        } else {
            return reviewDao.getReviewsByFilmId(filmId, count);
        }
    }

    public void addLikeReview(Long id, Long userId) {
        reviewDao.addLike(id, userId);
        updateUseful(1, id);
    }

    public void addDislikeReview(Long id, Long userId) {
        reviewDao.addDislike(id, userId);
        updateUseful(-1, id);
    }

    public void deleteLikeReview(Long id, Long userId) {
        reviewDao.deleteLikeDislike(id, userId);
        updateUseful(-1, id);
    }

    public void deleteDislikeReview(Long id, Long userId) {
        reviewDao.deleteLikeDislike(id, userId);
        updateUseful(1, id);
    }

    public void validate(Review review) {
        if (review.getUserId() < 0) {
            throw new NotFoundException("Некорректный userId для создания отзыва");
        }

        if (review.getFilmId() < 0) {
            throw new NotFoundException("Некорректный filmId для создания отзыва");
        }

        if (review.getIsPositive() == null) {
            throw new NotFoundException("Не передан параметр isPositive");
        }
    }

    public void updateUseful(Integer num, Long id) {
        Review review = get(id);
        review.setUseful(review.getUseful() + num);
        reviewDao.updateUsefulByReview(review);
    }
}
