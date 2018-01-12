DROP FUNCTION IF EXISTS findrateforroomondatewithinv_2( DATE [], DATE, DATE, INTEGER, INTEGER, VARCHAR[], VARCHAR[]);
CREATE OR REPLACE FUNCTION FindRateForRoomOnDateWithInv_2(days         DATE [], checkIn DATE, Checkout DATE,
                                                          bookinglimit INTEGER,
                                                          hotelId      INTEGER, rateChoices VARCHAR[], roomChoices VARCHAR[])
  RETURNS
    TABLE(dates DATE [], ids INTEGER [], estimate NUMERIC, rates VARCHAR[], rooms VARCHAR[])  STABLE  AS

$BODY$
BEGIN
  raise notice 'days in the find : % ', days;

  RETURN QUERY
  SELECT
    array_agg(date ORDER BY date ASC)                       AS dates,
    array_agg(hotel_room_availability_id ORDER BY date ASC) AS ids,
    sum(one_person_rate_after_tax)                          AS estimate,
    array_agg(DISTINCT hotel_rate_category) AS rates,
    array_agg(DISTINCT hotel_room_category) AS rooms

  FROM v1.hra_upsert
  WHERE hra_upsert.hotel_id = hotelId
        AND hra_upsert.date = ANY (days)
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
        AND NOT (hra_upsert.arrival_min_los NOTNULL AND date = checkIn AND arrival_min_los > array_length(days, 1))

        -- make sure room can be viable.
        AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los > array_length(days, 1)) AND
        one_person_rate_after_tax NOTNULL


        -- FOR NOW ASSUME we are only doing two person adult.
        -- CHange this so that the prices are separate table (day, hotel, room, rate, type, cnt, price) and
        -- left join the rest of the information so that it would ot be present if price for the particular
        -- request is not available.
        AND two_person_rate_after_tax NOTNULL AND one_person_rate_before_tax NOTNULL
        AND two_person_rate_before_tax NOTNULL


  GROUP BY hotel_id, hotel_room_category, hotel_rate_category;


END;

$BODY$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION partialSolution_2(days DATE [], checkIn DATE, Checkout DATE, bookinglimit INTEGER,
                                             hotelId INTEGER, rateChoices VARCHAR[], roomChoices VARCHAR[], sum_before NUMERIC, max_estimate NUMERIC)
  RETURNS
TABLE(remaingDays DATE [], ids INTEGER [], rates VARCHAR[], rooms VARCHAR[], estimate NUMERIC, minEstimate NUMERIC) STABLE AS


$BODY$
BEGIN
  RETURN QUERY


  WITH xxx AS (
      SELECT
        array(SELECT unnest(days) EXCEPT SELECT unnest(Partial.dates)) AS remaingDays,
        Partial.ids                                                    AS IDS,
        Partial.rates AS rates,
        Partial.rooms AS rooms,
        --TODO  IMPROVE ESTIMATE... .BE VERY CONSERVATIVE --- APPLY FREE NIGHT OR USE SOME AVG NIGHT
        sum_before + Partial.estimate                                  AS estimate

      FROM
        (SELECT *
         FROM FindRateForRoomOnDateWithInv_2(days, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices)) AS Partial
      WHERE Partial.estimate < max_estimate)
  SELECT
    xxx.remaingDays,
    xxx.ids,
    xxx.rates,
    xxx.rooms,
    xxx.estimate,
    --- use 99999999999999 as largest value
    (SELECT coalesce( min(xxx.estimate), 99999999999999)
     FROM xxx
     WHERE xxx.remaingDays = '{}') AS minEstimate
  FROM xxx;

END
$BODY$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION solve_2(days    DATE [], checkIn DATE, Checkout DATE, bookinglimit INTEGER,
                                   hotelId INTEGER,rateChoices VARCHAR[], roomChoices VARCHAR[], mixRoom BOOLEAN)
  RETURNS
    TABLE(remaingDays DATE [], ids INTEGER [], estimate NUMERIC, minEstimate NUMERIC) STABLE AS
$BODY$
BEGIN

  RETURN QUERY
  WITH RECURSIVE results(remaingDays, ids,  rates, rooms, estimate, minEstimate) AS
  (SELECT
     p.remaingDays,
     p.ids,
     p.rates,
     p.rooms,
     p.estimate,
     p.minEstimate
   --- use 99999999999999 as largest value
   FROM partialSolution_2(days, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999) AS p

   UNION
   SELECT
     y.remaingDays,
     r.ids || y.ids,
     y.rates,
     y.rooms,
     y.estimate,
     y.minEstimate
   FROM
     results AS r,
     --  TODO note if mix and match, send the room and rate choices from argument,
     -- if not mix and match only send the room that are already on the record
     -- but always send rates that are from argument
         partialSolution_2(r.remaingDays, checkIn, Checkout, bookinglimit, hotelId, rateChoices,
                           (CASE mixRoom WHEN true  then  roomChoices else r.rooms END),                           r.estimate,
                           r.minEstimate) AS y   -- minestimate is min vALUE OF GROUP
    WHERE r.remaingDays != '{}'

  )
  SELECT
    results.remaingDays,
    results.ids,
    results.estimate,
    results.minEstimate
  FROM results;


END
$BODY$ LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION searchForRooms_2( checkIn DATE, checkOut DATE, bookinglimit INTEGER,
                                             hotelId INTEGER, rateChoices  VARCHAR[], roomChoices VARCHAR[], mixRooms BOOLEAN )
  RETURNS
    TABLE(remaingDays DATE [], ids INTEGER [], estimate NUMERIC, minEstimate NUMERIC) AS
$BODY$
BEGIN

  RETURN QUERY
  SELECT  * FROM solve_2((select array_agg(x::date) from generate_series(checkIn, checkOut, '1 day') as x), checkIn, checkOut, bookinglimit, hotelId, rateChoices, roomChoices, mixRooms) as s
  WHERE
    s.remaingDays = '{}'
  ORDER BY
    s.estimate;


END
$BODY$ LANGUAGE plpgsql;





---------------------------------------------------------------------------------------------

SELECT *
FROM FindRateForRoomOnDateWithInv_2('{2016-08-19,2016-08-20,2016-08-21,2016-08-22,2016-08-23,2016-08-24,2016-08-25}',
                                    '2016-08-19', '2016-08-25', 30, '59846', null, null);     -- all rooms all rates

SELECT *
FROM FindRateForRoomOnDateWithInv_2('{2016-08-19,2016-08-20,2016-08-21,2016-08-22,2016-08-23,2016-08-24,2016-08-25}',
                                    '2016-08-19', '2016-08-25', 30, '59846', null, '{B2V}');      -- all rates, only B2v rooms (can be a list too)


SELECT *
FROM FindRateForRoomOnDateWithInv_2('{2016-01-22}',


                                 '2016-08-19', '2016-08-25', 1, '59846', null, null);





SELECT *
FROM partialSolution_2('{2016-08-19,2016-08-20,2016-08-21,2016-08-22,2016-08-23,2016-08-24,2016-08-25}', '2016-08-19',
                       '2016-08-25', 30, 59846, null, null, 0, 900);


SELECT *
FROM partialSolution_2('{2016-08-19,2016-08-20,2016-08-21,2016-08-22,2016-08-23,2016-08-24,2016-08-25}', '2016-08-19',
                       '2016-08-25', 30, 59846, '{A2V, A3V, A5VBP}', '{B2V}', 0, 900);



SELECT *
FROM
      solve_2('{2016-08-19,2016-08-20,2016-08-21,2016-08-22,2016-08-23,2016-08-24,2016-08-25}', '2016-08-19', '2016-08-25', 2,
              59846, '{A2V, A3V, A5VBP}', '{B2V}', true) AS s
WHERE
  s.remaingDays = '{}'
ORDER BY
  s.estimate;


SELECT *
FROM
      solve_2('{2016-08-19,2016-08-20,2016-08-21,2016-08-22,2016-08-23,2016-08-24,2016-08-25}', '2016-08-19', '2016-08-25', 2,
              59846, null, null, true) AS s
WHERE
  s.remaingDays = '{}'
ORDER BY
  s.estimate;



select * from searchForRooms_2('2016-01-10', '2016-01-15', 1,
                                      59846, null, null, false);


select * from searchForRooms_2('2016-02-01', '2016-02-10', 1,
                                      59846, null, null, false);     -- always just return the same room type

select * from searchForRooms_2('2016-08-19', '2016-08-25', 2,
                               59846, '{A2V, A3V, A5VBP}', '{B2V}', true) AS s

