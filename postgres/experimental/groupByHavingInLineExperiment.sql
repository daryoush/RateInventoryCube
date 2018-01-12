DROP FUNCTION IF EXISTS solve_4( DATE [], DATE, DATE, INTEGER, INTEGER, CHARACTER VARYING [], CHARACTER VARYING [], BOOLEAN, INTEGER );

CREATE OR REPLACE FUNCTION solve_4(days     DATE [], checkIn DATE, Checkout DATE, bookinglimit INTEGER,
                                   hotelId  INTEGER, rateChoices VARCHAR [], roomChoices VARCHAR [], mixRoom BOOLEAN,
                                   maxLevel INTEGER)
  RETURNS
    TABLE(remaingDays DATE [], ids INTEGER []) AS
$BODY$
BEGIN

  RETURN QUERY
  WITH RECURSIVE results(remaingDays, ids, rates, rooms, level) AS
  (SELECT p.remaingDays,p.ids, p.rates,p.rooms, 1
   FROM partialSolution_2(days, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999) AS p

   UNION
   (SELECT
      array(SELECT unnest(days) EXCEPT SELECT unnest(array_agg(date
                                                     ORDER BY date ASC))) AS remaingDays,
      r.ids || array_agg(hotel_room_availability_id ORDER BY date ASC),
      array_agg(DISTINCT hotel_rate_category)                             AS rates,
      array_agg(DISTINCT hotel_room_category)                             AS rooms,
      r.level + 1
    FROM
      results AS r,
      v1.hra_upsert

    WHERE
      r.level < maxLevel AND
      hra_upsert.hotel_id = hotelId
      AND hra_upsert.date = ANY (r.remaingDays)
      AND hra_upsert.hotel_room_category = ANY (coalesce((CASE mixRoom
                                                          WHEN TRUE
                                                            THEN roomChoices
                                                          ELSE r.rooms END), ARRAY [hra_upsert.hotel_room_category]))
      AND hra_upsert.hotel_rate_category = ANY (coalesce(rateChoices, ARRAY [hra_upsert.hotel_rate_category]))

      -- make sure room can be checked in and out on this rate for checkin and checkout r.remaingDays
      AND NOT (hra_upsert.arrival_restriction_status = 'D' AND hra_upsert.date = checkIn)
      AND NOT (hra_upsert.departure_restriction_status = 'D' AND hra_upsert.date = Checkout)


      -- make sure room is sellable in general
      AND hra_upsert.master_restriction_status = 'O'

      -- have the booking limit
      AND hra_upsert.booking_limit > bookinglimit

      -- Validate arrival MIN LOS for the arrival day otherwise this room/rate is now a possible option
      -- note that we may exceed the MIN LOS in this check, but still needs to validate this at the end
      -- to make sure with the rest of rates this is still valid
      AND
      NOT (hra_upsert.arrival_min_los NOTNULL AND date = checkIn AND arrival_min_los > array_length(r.remaingDays, 1))

      -- make sure room can be viable.
      AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los > array_length(r.remaingDays, 1)) AND
      one_person_rate_after_tax NOTNULL


      -- FOR NOW ASSUME we are only doing two person adult.
      -- CHange this so that the prices are separate table (day, hotel, room, rate, type, cnt, price) and
      -- left join the rest of the information so that it would ot be present if price for the particular
      -- request is not available.
      AND two_person_rate_after_tax NOTNULL AND one_person_rate_before_tax NOTNULL
      AND two_person_rate_before_tax NOTNULL


    GROUP BY hotel_id, hotel_room_category, hotel_rate_category))


  SELECT
    results.remaingDays,
    results.ids

  FROM results;

END
$BODY$ LANGUAGE plpgsql;


DROP FUNCTION IF EXISTS searchforrooms_4(date,date,integer,integer,character varying[],character varying[],boolean,integer)
CREATE OR REPLACE FUNCTION searchForRooms_4(checkIn  DATE, checkOut DATE, bookinglimit INTEGER,
                                            hotelId  INTEGER, rateChoices VARCHAR [], roomChoices VARCHAR [],
                                            mixRooms BOOLEAN, maxLevel INTEGER)
  RETURNS
    TABLE(remaingDays DATE [], ids INTEGER []) AS
$BODY$
BEGIN

  RETURN QUERY
  SELECT *
  FROM solve_4((SELECT array_agg(x :: DATE)
                FROM generate_series(checkIn, checkOut, '1 day') AS x), checkIn, checkOut, bookinglimit, hotelId,
               rateChoices, roomChoices, mixRooms, maxLevel) AS s
  WHERE
    s.remaingDays = '{}';



END
$BODY$ LANGUAGE plpgsql;


SELECT *
FROM searchForRooms_4('2016-08-10', '2016-08-15', 1,
                      59846, NULL, NULL, FAlse, 1);








--
--
--
--
--
--
-- DROP FUNCTION IF EXISTS findrateforroomondatewithinv_2( DATE [], DATE, DATE, INTEGER, INTEGER, VARCHAR [], VARCHAR [] );
-- CREATE OR REPLACE FUNCTION FindRateForRoomOnDateWithInv_2(r.remaingDays         DATE [], checkIn DATE, Checkout DATE,
--                                                           bookinglimit INTEGER,
--                                                           hotelId      INTEGER, rateChoices VARCHAR [],
--                                                           roomChoices  VARCHAR [])
--   RETURNS
--     TABLE(dates DATE [], ids INTEGER [], estimate NUMERIC, rates VARCHAR [], rooms VARCHAR []) AS
--
-- $BODY$
-- BEGIN
--   RETURN QUERY
--   SELECT
--     array_agg(date
--     ORDER BY date ASC)                      AS dates,
--     array_agg(hotel_room_availability_id
--     ORDER BY date ASC)                      AS ids,
--     sum(one_person_rate_after_tax)          AS estimate,
--     array_agg(DISTINCT hotel_rate_category) AS rates,
--     array_agg(DISTINCT hotel_room_category) AS rooms
--
--   FROM v1.hra_upsert
--   WHERE hra_upsert.hotel_id = hotelId
--         AND hra_upsert.date = ANY (r.remaingDays)
--         AND hra_upsert.hotel_room_category = ANY (coalesce(roomChoices, ARRAY [hra_upsert.hotel_room_category]))
--         AND hra_upsert.hotel_rate_category = ANY (coalesce(rateChoices, ARRAY [hra_upsert.hotel_rate_category]))
--
--         -- make sure room can be checked in and out on this rate for checkin and checkout r.remaingDays
--         AND NOT (hra_upsert.arrival_restriction_status = 'D' AND hra_upsert.date = checkIn)
--         AND NOT (hra_upsert.departure_restriction_status = 'D' AND hra_upsert.date = Checkout)
--
--
--         -- make sure room is sellable in general
--         AND hra_upsert.master_restriction_status = 'O'
--
--         -- have the booking limit
--         AND hra_upsert.booking_limit > bookinglimit
--
--         -- Validate arrival MIN LOS for the arrival day otherwise this room/rate is now a possible option
--         -- note that we may exceed the MIN LOS in this check, but still needs to validate this at the end
--         -- to make sure with the rest of rates this is still valid
--         AND NOT (hra_upsert.arrival_min_los NOTNULL AND date = checkIn AND arrival_min_los > array_length(r.remaingDays, 1))
--
--         -- make sure room can be viable.
--         AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los > array_length(r.remaingDays, 1)) AND
--         one_person_rate_after_tax NOTNULL
--
--
--         -- FOR NOW ASSUME we are only doing two person adult.
--         -- CHange this so that the prices are separate table (day, hotel, room, rate, type, cnt, price) and
--         -- left join the rest of the information so that it would ot be present if price for the particular
--         -- request is not available.
--         AND two_person_rate_after_tax NOTNULL AND one_person_rate_before_tax NOTNULL
--         AND two_person_rate_before_tax NOTNULL
--
--
--   GROUP BY hotel_id, hotel_room_category, hotel_rate_category;
--
--
-- END;
--
-- $BODY$ LANGUAGE plpgsql;
--
-- SELECT *
-- FROM FindRateForRoomOnDateWithInv_2('{2016-08-19,2016-08-20,2016-08-21,2016-08-22,2016-08-23,2016-08-24,2016-08-25}',
--                                     '2016-08-19', '2016-08-25', 30, '59846', NULL, NULL); -- all rooms all rates
--
-- SELECT *
-- FROM FindRateForRoomOnDateWithInv_2('{2016-08-19,2016-08-20,2016-08-21,2016-08-22,2016-08-23,2016-08-24,2016-08-25}',
--                                     '2016-08-19', '2016-08-25', 30, '59846', NULL,
--                                     '{B2V}'); -- all rates, only B2v rooms (can be a list too)
--
--
-- CREATE OR REPLACE FUNCTION partialSolution_3(r.remaingDays       DATE [], checkIn DATE, Checkout DATE, bookinglimit INTEGER,
--                                              hotelId    INTEGER, rateChoices VARCHAR [], roomChoices VARCHAR []
--                                              )
--   RETURNS
--     TABLE(remaingDays DATE [], ids INTEGER [], rates VARCHAR [], rooms VARCHAR []) AS
--
-- $BODY$
-- BEGIN
--   RETURN QUERY
--
--
--   WITH xxx AS (
--       SELECT
--         array(SELECT unnest(r.remaingDays) EXCEPT SELECT unnest(Partial.dates)) AS remaingDays,
--         Partial.ids                                                    AS IDS,
--         Partial.rates                                                  AS rates,
--         Partial.rooms                                                  AS rooms
--
--       FROM
--         (SELECT *
--           AS Partial
--       )
--   SELECT
--     xxx.remaingDays,
--     xxx.ids,
--     xxx.rates,
--     xxx.rooms
--      FROM xxx
--
--
--
-- END
-- $BODY$ LANGUAGE plpgsql;

--
-- SELECT *
-- FROM partialSolution_2('{2016-08-19,2016-08-20,2016-08-21,2016-08-22,2016-08-23,2016-08-24,2016-08-25}', '2016-08-19',
--                        '2016-08-25', 30, 59846, NULL, NULL, 0, 900);
--
--
-- SELECT *
-- FROM partialSolution_2('{2016-08-19,2016-08-20,2016-08-21,2016-08-22,2016-08-23,2016-08-24,2016-08-25}', '2016-08-19',
--                        '2016-08-25', 30, 59846, '{A2V, A3V, A5VBP}', '{B2V}', 0, 900);
--
--
-- SELECT *
-- FROM
--       solve_2('{2016-08-19,2016-08-20,2016-08-21,2016-08-22,2016-08-23,2016-08-24,2016-08-25}', '2016-08-19',
--               '2016-08-25', 2,
--               59846, '{A2V, A3V, A5VBP}', '{B2V}', TRUE) AS s
-- WHERE
--   s.remaingDays = '{}'
-- ORDER BY
--   s.estimate;
--
--
-- SELECT *
-- FROM
--       solve_2('{2016-08-19,2016-08-20,2016-08-21,2016-08-22,2016-08-23,2016-08-24,2016-08-25}', '2016-08-19',
--               '2016-08-25', 2,
--               59846, NULL, NULL, TRUE) AS s
-- WHERE
--   s.remaingDays = '{}'
-- ORDER BY
--   s.estimate;
--
--
--
--
--
-- SELECT count(*)
-- FROM searchForRooms_2('2016-01-01', '2016-01-10', 1,
--                       59846, NULL, NULL, FALSE); -- always just return the same room type
--
-- SELECT *
-- FROM searchForRooms_2('2016-08-19', '2016-08-25', 2,
--                       59846, '{A2V, A3V, A5VBP}', '{B2V}', TRUE) AS s
--
