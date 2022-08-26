SELECT DISTINCT F.ID,
                F.NAME,
                F.DESCRIPTION,
                F.RELEASE_DATE,
                F.DURATION,
                CASE
                    WHEN (M.PROGNOSTIC_MARK > 10) THEN 10
                    WHEN (M.PROGNOSTIC_MARK < 1) THEN 1
                    ELSE ROUND(M.PROGNOSTIC_MARK, 1)
                    END                                                                       AS RATE,
                F.MPA_ID,
                M.MPA_NAME,
                STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID)    AS GENRES,
                STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') over (PARTITION BY F.ID) AS DIRECTORS
FROM FILMS AS F
         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID
         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID
         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID
         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID
         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID
         INNER JOIN

         (SELECT MARKS.FILM_ID,
                 SUM((MARK_VALUE + OPINION_DELTA) * OPINION_WEIGHT) / SUM(OPINION_WEIGHT) AS PROGNOSTIC_MARK

          FROM

              (SELECT L1.USER_ID,
                      AVG((l2.MARK_VALUE - l1.MARK_VALUE)) as OPINION_DELTA,
                      COUNT(L1.USER_ID)                    as OPINION_WEIGHT
               FROM LIKES AS L1
                        INNER JOIN LIKES AS L2 ON L2.FILM_ID = L1.FILM_ID
               WHERE L2.USER_ID = ?
                 AND L1.USER_ID != ?
               GROUP BY L1.USER_ID)
                  AS OPINIONS

                  INNER JOIN

              (SELECT L3.*
               FROM LIKES AS L3
               WHERE L3.FILM_ID IN
                     (SELECT DISTINCT L4.FILM_ID FROM LIKES AS L4 WHERE L4.USER_ID = ?))
                  AS MARKS
              ON OPINIONS.USER_ID = MARKS.USER_ID

          GROUP BY FILM_ID)
         AS M on F.ID = M.FILM_ID
GROUP BY F.ID, FG.GENRE_ID, FD.DIRECTOR_ID
ORDER BY F.ID;