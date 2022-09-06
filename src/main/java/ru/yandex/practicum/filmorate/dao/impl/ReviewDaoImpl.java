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
public class ReviewDaoImpl implements ReviewDao {
    private final JdbcTemplate jdbcTemplate;
    private static final String REVIEWS_ADD_LIKES_DISLIKES = "INSERT INTO REVIEWS_LIKES(REVIEW_ID, USER_ID, IS_POSITIVE) VALUES (?, ?, ?);";
    private static final String REVIEWS_GET_ALL = "SELECT * FROM REVIEWS ORDER BY USEFUL DESC;";
    private static final String REVIEWS_GET_BY_ID = "SELECT * FROM REVIEWS WHERE REVIEW_ID = ?";
    private static final String REVIEWS_GET_TOP = "SELECT * FROM REVIEWS ORDER BY USEFUL DESC LIMIT ?;";
    private static final String REVIEWS_GET_TOP_BY_FILMID = "SELECT * FROM REVIEWS WHERE FILM_ID = ? ORDER BY USEFUL DESC LIMIT ?;";
    private static final String REVIEWS_REMOVE_BY_ID = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?;";
    private static final String REVIEWS_REMOVE_LIKES_DISLIKES = "DELETE FROM REVIEWS_LIKES WHERE REVIEW_ID = ? AND USER_ID = ?;";
    private static final String REVIEWS_UPDATE = "UPDATE REVIEWS SET CONTENT = ?, IS_POSITIVE = ? WHERE REVIEW_ID = ?;";
    private static final String REVIEWS_UPDATE_USEFUL = "UPDATE REVIEWS SET USEFUL = ? WHERE REVIEW_ID = ?;";

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
        log.info("Добавлен отзыв id = {}", reviewId);
        return getReviewById(reviewId);
    }

    @Override
    public Review getReviewById(Long id) {
        try {
            Review review = jdbcTemplate.queryForObject(REVIEWS_GET_BY_ID,
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
        return jdbcTemplate.query(REVIEWS_GET_ALL, this::parseReview);
    }

    @Override
    public Review updateReview(Review review) {
        jdbcTemplate.update(REVIEWS_UPDATE,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );
        log.info("Обновлен отзыв id = {}", review.getReviewId());
        return getReviewById(review.getReviewId());
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId, Integer count) {
        List<Review> reviews = jdbcTemplate.query(REVIEWS_GET_TOP_BY_FILMID, this::parseReview, filmId, count);
        log.info("Найдены отзывы по фильму {}", filmId);
        return reviews;
    }

    @Override
    public List<Review> getReviewsTop(Integer count) {
        List<Review> reviews = jdbcTemplate.query(REVIEWS_GET_TOP, this::parseReview, count);
        log.info("Найден топ отзывов");
        return reviews;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(REVIEWS_REMOVE_BY_ID, id);
        log.info("Удален отзыв {}", id);
    }

    @Override
    public void addLike(Long id, Long userId) {
        jdbcTemplate.update(REVIEWS_ADD_LIKES_DISLIKES, id, userId, 1);
        log.info("Пользователь {} добавил лайк на отзыв {}", userId, id);
    }

    @Override
    public void addDislike(Long id, Long userId) {
        jdbcTemplate.update(REVIEWS_ADD_LIKES_DISLIKES, id, userId, 0);
        log.info("Пользователь {} добавил дизлайк на отзыв {}", userId, id);
    }

    @Override
    public void deleteLikeDislike(Long id, Long userId) {
        int removeCount = jdbcTemplate.update(REVIEWS_REMOVE_LIKES_DISLIKES, id, userId);
        if (removeCount < 1) {
            throw new NotFoundException("Не удалось удалить like отзыва");
        }
        log.info("Пользователь {} удалена реакция на отзыв {}", userId, id);
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
        jdbcTemplate.update(REVIEWS_UPDATE_USEFUL,
                review.getUseful(),
                review.getReviewId()
        );
    }
}
