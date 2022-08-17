SELECT F.ID,
       F.NAME,
       F.DESCRIPTION,
       F.RELEASE_DATE,
       F.DURATION,
       COUNT(L.FILM_ID)                                                           AS RATE,
       F.MPA_ID,
       M.MPA_NAME,
       STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID) AS GENRES
FROM FILMS AS F
         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID
         LEFT JOIN LIKES L on F.ID = L.FILM_ID
         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID
         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID
GROUP BY F.ID, FG.GENRE_ID
ORDER BY RATE DESC
LIMIT ?;