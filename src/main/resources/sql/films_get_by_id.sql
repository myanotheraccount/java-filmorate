SELECT F.ID,
       F.NAME,
       F.DESCRIPTION,
       F.RELEASE_DATE,
       F.DURATION,
       F.RATE,
       F.MPA_ID,
       M.MPA_NAME,
       STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',')    AS GENRES,
       STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') AS DIRECTORS
FROM FILMS AS F
         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID
         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID
         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID
         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID
         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID
WHERE F.ID = ?
GROUP BY F.ID;
