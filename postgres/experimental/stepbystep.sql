DROP FUNCTION if EXISTS  findcubes(date[],date,date,integer,integer,character varying[],character varying[]);

CREATE OR REPLACE FUNCTION findcubes(daterow     DATE [][], checkIn DATE, Checkout DATE,
                                                          bookinglimit INTEGER,
                                                          hotelId      INTEGER, rateChoices VARCHAR[], roomChoices VARCHAR[])
  RETURNS
    TABLE(dates DATE [], ids INTEGER [], estimate NUMERIC, rates VARCHAR[], rooms VARCHAR[]) AS

$BODY$
BEGIN
  RETURN QUERY
  SELECT
    array_agg(date ORDER BY date ASC)                       AS dates,
    array_agg(hotel_room_availability_id ORDER BY date ASC) AS ids,
    sum(one_person_rate_after_tax)                          AS estimate,
    array_agg(DISTINCT hotel_rate_category) AS rates,
    array_agg(DISTINCT hotel_room_category) AS rooms

  FROM v1.hra_upsert,
    (select x as datelist , row_number() over () as rn  from ( select * from reduce_dim ( daterow)) as x) as xx
  WHERE hra_upsert.hotel_id = hotelId
        AND hra_upsert.date = ANY (xx.datelist::date[])
        AND hra_upsert.hotel_room_category = ANY(coalesce(roomChoices, ARRAY [hra_upsert.hotel_room_category]            ))
        AND hra_upsert.hotel_rate_category = ANY(coalesce( rateChoices, ARRAY [hra_upsert.hotel_rate_category]))

        -- make sure room can be checked in and out on this rate for checkin and checkout days
        AND NOT (hra_upsert.arrival_restriction_status = 'D' AND hra_upsert.date = checkIn)
        AND NOT (hra_upsert.departure_restriction_status = 'D' AND hra_upsert.date = Checkout)


        -- make sure room is sellable in general
        AND hra_upsert.master_restriction_status = 'O'

        -- have the booking limit
        AND hra_upsert.booking_limit > bookinglimit

        -- Validate arrival MIN LOS for the arrival day otherwise this room/rate is now a possible option
        -- note that we may exceed the MIN LOS in this check, but still needs to validate this at the end
        -- to make sure with the rest of rates this is still valid
        AND NOT (hra_upsert.arrival_min_los NOTNULL AND date = checkIn AND arrival_min_los > array_length(x, 1))

        -- make sure room can be viable.
        AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los > array_length(x, 1)) AND
        one_person_rate_after_tax NOTNULL


        -- FOR NOW ASSUME we are only doing two person adult.
        -- CHange this so that the prices are separate table (day, hotel, room, rate, type, cnt, price) and
        -- left join the rest of the information so that it would ot be present if price for the particular
        -- request is not available.
        AND two_person_rate_after_tax NOTNULL AND one_person_rate_before_tax NOTNULL
        AND two_person_rate_before_tax NOTNULL


  GROUP BY xx.rn, hotel_id, hotel_room_category, hotel_rate_category;


END;

$BODY$ LANGUAGE plpgsql;



SELECT *
FROM findcubes(Array[Array['2016-08-19','2016-08-20'], ARRAY['2016-08-21','2016-08-22'], ARRAY['2016-01-19','2016-01-20']]:: date[][],
               '2016-08-19', '2016-08-25', 30, '59846', null, null);
select reduce_dim(Array[Array['2016-08-19','2016-08-20'], ARRAY['2016-08-21','2016-08-22'], ARRAY['2016-01-19','2016-01-20']]:: date[][]);

CREATE OR REPLACE FUNCTION public.reduce_dim(anyarray)
  RETURNS SETOF anyarray AS
$function$
DECLARE
  s $1%TYPE;
BEGIN
  FOREACH s SLICE 1  IN ARRAY $1 LOOP
    RETURN NEXT s;
  END LOOP;
  RETURN;
END;
$function$
LANGUAGE plpgsql IMMUTABLE;

select *, row_number() over ()  from (
select *  from reduce_dim(Array[Array['2016-08-19','2016-08-20'], ARRAY['2016-08-19','2016-08-20'], ARRAY['2016-08-19','2016-08-20']])) as x;


SELECT PG_typeof(ARRAY[ARRAY['2016-08-19','2016-08-20'],ARRAY ['2016-08-21','2016-08-22'],ARRAY['2016-08-23','2016-08-24','2016-08-25']] );

select * from public.reduce_dim(ARRAY[ ARRAY['2016-08-19','2016-08-20'],ARRAY ['2016-08-21','2016-08-22'],ARRAY['2016-08-23','2016-08-24','2016-08-25']] :: date[][]);





CREATE OR REPLACE FUNCTION solve_5_ZERO_JOIN(days     DATE [], checkIn DATE, Checkout DATE, bookinglimit INTEGER,
                                   hotelId  INTEGER, rateChoices VARCHAR [], roomChoices VARCHAR [], mixRoom BOOLEAN,
                                   maxLevel INTEGER)
  RETURNS
    TABLE(remaingDays DATE [], ids INTEGER []) AS
$BODY$
BEGIN

  RETURN QUERY
  select zero.remaingDays,  zero.ids from
   partialSolution_2(days, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS zero;

END
$BODY$ LANGUAGE plpgsql;






CREATE OR REPLACE FUNCTION solve_5_ONE_JOIN(days     DATE [], checkIn DATE, Checkout DATE, bookinglimit INTEGER,
                                   hotelId  INTEGER, rateChoices VARCHAR [], roomChoices VARCHAR [], mixRoom BOOLEAN,
                                   maxLevel INTEGER)
  RETURNS
    TABLE(remaingDays DATE [], ids INTEGER []) AS
$BODY$
BEGIN

  RETURN QUERY
  select one.remaingDays, one.ids || zero.ids  from
   partialSolution_2(days, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS zero,
   partialSolution_2(zero.remaingDays, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS one
      where zero.rooms && one.rooms;  -- && array overlap https://www.postgresql.org/docs/9.1/static/functions-array.html

END
$BODY$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION solve_5_TWOJOIN(days     DATE [], checkIn DATE, Checkout DATE, bookinglimit INTEGER,
                                   hotelId  INTEGER, rateChoices VARCHAR [], roomChoices VARCHAR [], mixRoom BOOLEAN,
                                   maxLevel INTEGER)
  RETURNS
    TABLE(remaingDays DATE [], ids INTEGER []) AS
$BODY$
BEGIN

  RETURN QUERY
  select two.remaingDays, two.ids || one.ids || zero.ids  from
   partialSolution_2(days, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS zero,
   partialSolution_2(zero.remaingDays, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS one,
   partialSolution_2(one.remaingDays, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS two
   where zero.rooms && one.rooms AND zero.rooms  && two.rooms;  -- && array overlap https://www.postgresql.org/docs/9.1/static/functions-array.html

END
$BODY$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION solve_5_TWOJOIN(days     DATE [], checkIn DATE, Checkout DATE, bookinglimit INTEGER,
                                   hotelId  INTEGER, rateChoices VARCHAR [], roomChoices VARCHAR [], mixRoom BOOLEAN,
                                   maxLevel INTEGER)
  RETURNS
    TABLE(remaingDays DATE [], ids INTEGER []) AS
$BODY$
BEGIN

  RETURN QUERY
  select two.remaingDays, two.ids || one.ids || zero.ids  from



   partialSolution_2(days, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS zero,
   partialSolution_2(zero.remaingDays, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS one,
   partialSolution_2(one.remaingDays, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS two
   where zero.rooms && one.rooms AND zero.rooms  && two.rooms;  -- && array overlap https://www.postgresql.org/docs/9.1/static/functions-array.html

END
$BODY$ LANGUAGE plpgsql;






CREATE OR REPLACE FUNCTION solve_6(days     DATE [], checkIn DATE, Checkout DATE, bookinglimit INTEGER,
                                   hotelId  INTEGER, rateChoices VARCHAR [], roomChoices VARCHAR [], mixRoom BOOLEAN,
                                   maxLevel INTEGER)
  RETURNS
    TABLE(remaingDays DATE [], ids INTEGER []) AS
$BODY$
BEGIN

  RETURN QUERY
    SELECT  * from (
  select
  array(SELECT unnest(days) EXCEPT SELECT unnest(array_agg(base.date ORDER BY base.date ASC))) AS remaingDays,
  array_agg(base.hotel_room_availability_id ORDER BY base.date ASC) AS ids
 from
        v1.hra_upsert as base

  WHERE

      base.hotel_id = hotelId
      AND base.date = ANY (days)   -- just get the inital subset of days
      AND base.hotel_rate_category = ANY (coalesce(rateChoices, ARRAY [base.hotel_rate_category]))
      -- make sure room can be checked in and out on this rate for checkin and checkout r.remaingDays
      AND NOT (base.arrival_restriction_status = 'D' AND base.date = checkIn)
      AND NOT (base.departure_restriction_status = 'D' AND base.date = Checkout)
      -- make sure room is sellable in general
      AND base.master_restriction_status = 'O'
      -- have the booking limit
      AND base.booking_limit > bookinglimit




  GROUP BY base.hotel_id, base.hotel_room_category,  base.hotel_rate_category) as rootcube;



---  DO LOS TESTING OUTSIDE      AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los > array_length(r.remaingDays, 1))
--      -- Validate arrival MIN LOS for the arrival day otherwise this room/rate is now a possible option
--       -- note that we may exceed the MIN LOS in this check, but still needs to validate this at the end
--       -- to make sure with the rest of rates this is still valid
--       AND NOT (base.arrival_min_los NOTNULL AND date = checkIn AND arrival_min_los > array_length(r.remaingDays, 1))

--   DO ROOMS AT THE OUTSIDE     AND base.hotel_room_category = ANY (coalesce((CASE mixRoom
--                                                           WHEN TRUE
--                                                             THEN roomChoices
--                                                          ELSE r.rooms END), ARRAY [hra_upsert.hotel_room_category]))

--    partialSolution_2(days, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS zero,
--    partialSolution_2(zero.remaingDays, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS one,
--    partialSolution_2(one.remaingDays, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS two,
--    partialSolution_2(two.remaingDays, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS three;
  -- where zero.rooms && one.rooms AND zero.rooms  && two.rooms AND  zero.rooms  && three.rooms;  -- && array overlap https://www.postgresql.org/docs/9.1/static/functions-array.html

END
$BODY$ LANGUAGE plpgsql;

select * from (
select x.remaingDays, row_number() over () as rn from

(SELECT DISTINCT remaingDays
FROM testroomSearch('2016-01-10', '2016-01-17', 1,
                      59846, NULL, NULL, FAlse, 1)) as x) as a;


--
--    UNION
--    (SELECT
--       array(SELECT unnest(days) EXCEPT SELECT unnest(array_agg(date
--                                                      ORDER BY date ASC))) AS remaingDays,
--       r.ids || array_agg(hotel_room_availability_id ORDER BY date ASC),
--       array_agg(DISTINCT hotel_rate_category)                             AS rates,
--       array_agg(DISTINCT hotel_room_category)                             AS rooms,
--       r.level + 1
--     FROM
--       results AS r,
--       v1.hra_upsert
--
--     WHERE
--       r.level < maxLevel AND
--       hra_upsert.hotel_id = hotelId
--       AND hra_upsert.date = ANY (r.remaingDays)
--       AND hra_upsert.hotel_room_category = ANY (coalesce((CASE mixRoom
--                                                           WHEN TRUE
--                                                             THEN roomChoices
--                                                           ELSE r.rooms END), ARRAY [hra_upsert.hotel_room_category]))
--       AND hra_upsert.hotel_rate_category = ANY (coalesce(rateChoices, ARRAY [hra_upsert.hotel_rate_category]))
--
--       -- make sure room can be checked in and out on this rate for checkin and checkout r.remaingDays
--       AND NOT (hra_upsert.arrival_restriction_status = 'D' AND hra_upsert.date = checkIn)
--       AND NOT (hra_upsert.departure_restriction_status = 'D' AND hra_upsert.date = Checkout)
--
--
--       -- make sure room is sellable in general
--       AND hra_upsert.master_restriction_status = 'O'
--
--       -- have the booking limit
--       AND hra_upsert.booking_limit > bookinglimit
--
--       -- Validate arrival MIN LOS for the arrival day otherwise this room/rate is now a possible option
--       -- note that we may exceed the MIN LOS in this check, but still needs to validate this at the end
--       -- to make sure with the rest of rates this is still valid
--       AND
--       NOT (hra_upsert.arrival_min_los NOTNULL AND date = checkIn AND arrival_min_los > array_length(r.remaingDays, 1))
--
--       -- make sure room can be viable.
--       AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los > array_length(r.remaingDays, 1)) AND
--       one_person_rate_after_tax NOTNULL
--
--
--       -- FOR NOW ASSUME we are only doing two person adult.
--       -- CHange this so that the prices are separate table (day, hotel, room, rate, type, cnt, price) and
--       -- left join the rest of the information so that it would ot be present if price for the particular
--       -- request is not available.
--       AND two_person_rate_after_tax NOTNULL AND one_person_rate_before_tax NOTNULL
--       AND two_person_rate_before_tax NOTNULL
--
--
--     GROUP BY hotel_id, hotel_room_category, hotel_rate_category))
--
--
--   SELECT
--     results.remaingDays,
--     results.ids
--
--   FROM results;












DROP FUNCTION IF EXISTS testroomSearch(date,date,integer,integer,character varying[],character varying[],boolean,integer);
CREATE OR REPLACE FUNCTION testroomSearch(checkIn  DATE, checkOut DATE, bookinglimit INTEGER,
                                            hotelId  INTEGER, rateChoices VARCHAR [], roomChoices VARCHAR [],
                                            mixRooms BOOLEAN, maxLevel INTEGER)
  RETURNS
    TABLE(remaingDays DATE [], ids INTEGER []) AS
$BODY$
BEGIN

  RETURN QUERY
  SELECT *
  FROM solve_6((SELECT array_agg(x :: DATE)
                FROM generate_series(checkIn, checkOut, '1 day') AS x), checkIn, checkOut, bookinglimit, hotelId,
               rateChoices, roomChoices, mixRooms, maxLevel) AS s;




END
$BODY$ LANGUAGE plpgsql;

