SELECT *
FROM USERS
WHERE ID IN (
    SELECT FRIEND_ID as ID
    FROM FRIENDSHIP
    WHERE USER_ID = ?);