package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.ReviewDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ReviewDaoImpl extends AbstractDaoImpl implements ReviewDao {
    private final JdbcTemplate jdbcTemplate;

    public ReviewDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review createReview(Review review) {

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");

        Map<String, Object> values = new HashMap<>();

        values.put("content", review.getContent());
        values.put("is_positive", review.getIsPositive());
        values.put("useful", 0);
        values.put("user_id", review.getUserId());
        values.put("film_id", review.getFilmId());

        Long reviewId = simpleJdbcInsert.executeAndReturnKey(values).longValue();
        return getReviewById(reviewId);
    }

    @Override
    public Review getReviewById(Long id) {
        try {
            Review review = jdbcTemplate.queryForObject(readSql("reviews_get_by_id"),
                    this::parseReview, id);
            log.info("Найден Отзыв на фильм пользователя: {} {} {}", review.getReviewId()
                    , review.getFilmId(), review.getUserId());
            return review;
        } catch (Exception e) {
            throw new NotFoundException("Отзыв с идентификатором " + id + " не найден: " + e.getMessage());
        }
    }

    @Override
    public List<Review> getAll() {
        return jdbcTemplate.query(readSql("reviews_get_all"), this::parseReview);
    }

    @Override
    public Review updateReview(Review review) {
        jdbcTemplate.update(readSql("reviews_update"),
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );
        return getReviewById(review.getReviewId());
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId, Integer count) {
        return jdbcTemplate.query(readSql("reviews_get_top_by_filmId"), this::parseReview, filmId, count);
    }

    @Override
    public List<Review> getReviewsTop(Integer count) {
        return jdbcTemplate.query(readSql("reviews_get_top"), this::parseReview, count);
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(readSql("reviews_remove_by_id"), id);
    }

    @Override
    public void addLike(Long id, Long userId) {
        jdbcTemplate.update(readSql("reviews_add_likes_dislikes"), id, userId, 1);
    }

    @Override
    public void addDislike(Long id, Long userId) {
        jdbcTemplate.update(readSql("reviews_add_likes_dislikes"), id, userId, 0);
    }

    @Override
    public void deleteLikeDislike(Long id, Long userId) {
        int removeCount = jdbcTemplate.update(readSql("reviews_remove_likes_dislikes"), id, userId);
        if (removeCount < 1) {
            throw new NotFoundException("Не удалось удалить like отзыва");
        }
    }

    private Review parseReview(ResultSet rs, int rowNum) throws SQLException {
        return new Review(
                rs.getLong("review_id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getInt("useful"),
                rs.getLong("user_id"),
                rs.getLong("film_id")
        );
    }

    @Override
    public void updateUsefulByReview(Review review) {
        jdbcTemplate.update(readSql("reviews_update_useful"),
                review.getUseful(),
                review.getReviewId()
        );
    }
}
