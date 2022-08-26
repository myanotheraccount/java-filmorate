SELECT DISTINCT F.ID,
                F.NAME,
                F.DESCRIPTION,
                F.RELEASE_DATE,
                F.DURATION,
                ROUND(AVG(L.MARK_VALUE), 1)                                                   AS RATE,
                COUNT(L.FILM_ID)                                                              AS VOTES,
                F.MPA_ID,
                M.MPA_NAME,
                STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID)    AS GENRES,
                STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') over (PARTITION BY F.ID) AS DIRECTORS
FROM (SELECT DISTINCT FILMS.*
      FROM FILMS
               JOIN FILMS_GENRES on FILMS.ID = FILMS_GENRES.FILM_ID
      WHERE FILMS_GENRES.GENRE_ID = ?) AS F
         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID
         LEFT JOIN LIKES L on F.ID = L.FILM_ID
         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID
         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID
         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID
         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID
GROUP BY F.ID, FG.GENRE_ID, FD.DIRECTOR_ID
ORDER BY RATE DESC, VOTES DESC
LIMIT ?;