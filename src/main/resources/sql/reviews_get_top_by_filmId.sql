SELECT *
FROM REVIEWS
WHERE FILM_ID = ?
ORDER BY USEFUL DESC
LIMIT ?;