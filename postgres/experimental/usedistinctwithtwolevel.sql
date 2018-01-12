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

DROP FUNCTION if EXISTS  findcubes(date[],date,date,integer,integer,character varying[],character varying[]);

CREATE OR REPLACE FUNCTION findcubes(daterow     DATE [][], checkIn DATE, Checkout DATE,
                                     bookinglimit INTEGER,
                                     hotelId      INTEGER, rateChoices VARCHAR[], roomChoices VARCHAR[])
  RETURNS
    TABLE(origDates DATE[],  dates DATE [], ids INTEGER [], estimate NUMERIC, rates VARCHAR[], rooms VARCHAR[]) AS

$BODY$
BEGIN

  raise notice 'daterow in cube: % ', daterow;

  RETURN QUERY
  SELECT
    xx.datelist,
    array_agg(date ORDER BY date ASC)                       AS dates,
    array_agg(hotel_room_availability_id ORDER BY date ASC) AS ids,
    sum(one_person_rate_after_tax)                          AS estimate,
    array_agg(DISTINCT hotel_rate_category) AS rates,
    array_agg(DISTINCT hotel_room_category) AS rooms

  FROM v1.hra_upsert,
    (  select * from reduce_dim ( daterow) as datelist) as xx
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
        AND NOT (hra_upsert.arrival_min_los NOTNULL AND date = checkIn AND arrival_min_los > array_length(xx.datelist, 1))

        -- make sure room can be viable.
        AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los > array_length(xx.datelist, 1)) AND
        one_person_rate_after_tax NOTNULL


        -- FOR NOW ASSUME we are only doing two person adult.
        -- CHange this so that the prices are separate table (day, hotel, room, rate, type, cnt, price) and
        -- left join the rest of the information so that it would ot be present if price for the particular
        -- request is not available.
        AND two_person_rate_after_tax NOTNULL AND one_person_rate_before_tax NOTNULL
        AND two_person_rate_before_tax NOTNULL


  GROUP BY xx.datelist, hotel_id, hotel_room_category, hotel_rate_category;


END;

$BODY$ LANGUAGE plpgsql;



SELECT *
FROM findcubes(Array[Array['2016-08-19','2016-08-20'], ARRAY['2016-08-21','2016-08-22'], ARRAY['2016-01-10','2016-01-20']]:: date[][],
               '2016-08-19', '2016-08-25', 30, '59846', null, null);




CREATE OR REPLACE FUNCTION oneJoinCubeSolve(days     DATE [], checkIn DATE, Checkout DATE, bookinglimit INTEGER,
                                            hotelId  INTEGER, rateChoices VARCHAR [], roomChoices VARCHAR [], mixRoom BOOLEAN,
                                            maxLevel INTEGER)
  RETURNS
    TABLE(remaingDays DATE [], ids INTEGER []) AS
$BODY$
BEGIN

  RETURN QUERY
  select
    array(SELECT unnest(one.origDates) EXCEPT SELECT unnest(one.dates)) AS remaingDays,
    one.ids || zero.ids  from

        partialSolution_2(days, checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices, 0, 99999999999999)  AS zero,
        findcubes((SELECT DISTINCT zero.remaingDays ), checkIn, Checkout, bookinglimit, hotelId, rateChoices, roomChoices)  AS one
  where zero.rooms && one.rooms
  AND zero.remaingDays != '{}'
  AND  zero.remaingDays =  one.origDates;  -- && array overlap https://www.postgresql.org/docs/9.1/static/functions-array.html

END
$BODY$ LANGUAGE plpgsql;



DROP FUNCTION IF EXISTS solveCube(date,date,integer,integer,character varying[],character varying[],boolean,integer);
CREATE OR REPLACE FUNCTION solveCubeA(checkIn  DATE, checkOut DATE, bookinglimit INTEGER,
                                          hotelId  INTEGER, rateChoices VARCHAR [], roomChoices VARCHAR [],
                                          mixRooms BOOLEAN, maxLevel INTEGER)
  RETURNS
    TABLE(remaingDays DATE [], ids INTEGER []) AS
$BODY$
BEGIN

  RETURN QUERY
  SELECT *
  FROM oneJoinCubeSolve((SELECT array_agg(x :: DATE)
                FROM generate_series(checkIn, checkOut, '1 day') AS x), checkIn, checkOut, bookinglimit, hotelId,
               rateChoices, roomChoices, mixRooms, maxLevel) AS s;




END
$BODY$ LANGUAGE plpgsql;


select * from solveCube('2016-01-10', '2016-01-15', 1,
59846, NULL, NULL, FAlse, 1);

SELECT *
FROM partialSolution_2((SELECT array_agg(x :: DATE)
                        FROM generate_series('2016-01-10'::DATE, '2016-01-17'::DATE, '1 day') AS x), '2016-08-19',
                       '2016-08-25', 30, 59846, null, null, 0, 900);



select  DISTINCT aaa.remaingDays from
  (
    SELECT *
    FROM partialSolution_2((SELECT array_agg(x :: DATE)
                            FROM generate_series('2016-01-10' :: DATE, '2016-01-17' :: DATE, '1 day') AS x),
                           '2016-08-19',
                           '2016-08-25', 30, 59846, NULL, NULL, 0, 900)
  ) as aaa;

select * from findcubes((select array_agg(xxxx) from

                  (select DISTINCT aaa.remaingDays from
(
SELECT *
FROM partialSolution_2((SELECT array_agg(x :: DATE)
FROM generate_series('2016-01-10' :: DATE, '2016-01-17' :: DATE, '1 day') AS x),
'2016-08-19',
'2016-08-25', 30, 59846, NULL, NULL, 0, 900)
) as aaa) as xxxx),

                        '2016-08-19',
'2016-08-25', 30, 59846, NULL, NULL) as zz;


    select json_agg(

    (SELECT  remaingdays
      FROM partialSolution_2((SELECT array_agg(x :: DATE)
                              FROM generate_series('2016-01-10' :: DATE, '2016-01-17' :: DATE, '1 day') AS x),
                             '2016-08-19',
                           '2016-08-25', 30, 59846, NULL, NULL, 0, 900) AS z


     group by remaingdays));


select * from findcubes(
    (SELECT DISTINCT  z.remaingdays
FROM partialSolution_2((SELECT array_agg(x :: DATE)
                        FROM generate_series('2016-01-10' :: DATE, '2016-01-17' :: DATE, '1 day') AS x),
                       '2016-08-19',
                '2016-08-25', 30, 59846, NULL, NULL, 0, 900) as z),
    '2016-08-19',
    '2016-08-25', 30, 59846, NULL, NULL, 0, 900)

;
