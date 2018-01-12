
select

  array_agg(DISTINCT base.date)   || array_agg(DISTINCT one.date) || array_agg(DISTINCT two.date)                     AS dates,
  array_agg(DISTINCT base.hotel_room_availability_id )  || array_agg(DISTINCT one.hotel_room_availability_id )  || array_agg(DISTINCT two.hotel_room_availability_id ) AS ids,
  sum(DISTINCT base.one_person_rate_after_tax)   + sum(DISTINCT one.one_person_rate_after_tax) + sum(DISTINCT two.one_person_rate_after_tax)                         AS estimate,
  array_agg(DISTINCT base.hotel_rate_category)   ||array_agg(DISTINCT one.hotel_rate_category) ||array_agg(DISTINCT two.hotel_rate_category)  AS rates,
  array_agg(DISTINCT base.hotel_room_category)AS rooms

from

  (SELECT *
   FROM v1.hra_upsert
   WHERE hotel_id = '59846'
         --and B.date != C.date
         --and B.hotel_room_category = C.hotel_room_category
         AND date = ANY ((SELECT array_agg(x :: DATE)
                          FROM generate_series('2016-01-10' :: DATE, '2016-01-14' :: DATE, '1 day') AS x) :: DATE [])
         AND NOT (arrival_restriction_status = 'D' AND date = '2016-01-10')
         AND NOT (departure_restriction_status = 'D' AND date = '2016-01-14')


         -- make sure room is sellable in general
         AND master_restriction_status = 'O'

         -- have the booking limit
         AND booking_limit > 1
         AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los < 7) AND
         one_person_rate_after_tax NOTNULL
  ) as base,
  (SELECT *
   FROM v1.hra_upsert
   WHERE hotel_id = '59846'
         --and B.date != C.date
         --and B.hotel_room_category = C.hotel_room_category
         AND date = ANY ((SELECT array_agg(x :: DATE)
                          FROM generate_series('2016-01-10' :: DATE, '2016-01-14' :: DATE, '1 day') AS x) :: DATE [])
         AND NOT (arrival_restriction_status = 'D' AND date = '2016-01-10')
         AND NOT (departure_restriction_status = 'D' AND date = '2016-01-14')


         -- make sure room is sellable in general
         AND master_restriction_status = 'O'

         -- have the booking limit
         AND booking_limit > 1
         AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los < 7) AND
         one_person_rate_after_tax NOTNULL
  ) as one,
  (SELECT *
   FROM v1.hra_upsert
   WHERE hotel_id = '59846'
         --and B.date != C.date
         --and B.hotel_room_category = C.hotel_room_category
         AND date = ANY ((SELECT array_agg(x :: DATE)
                          FROM generate_series('2016-01-10' :: DATE, '2016-01-14' :: DATE, '1 day') AS x) :: DATE [])
         AND NOT (arrival_restriction_status = 'D' AND date = '2016-01-10')
         AND NOT (departure_restriction_status = 'D' AND date = '2016-01-14')


         -- make sure room is sellable in general
         AND master_restriction_status = 'O'

         -- have the booking limit
         AND booking_limit > 1
         AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los < 7) AND
         one_person_rate_after_tax NOTNULL
  ) as two
  
where one.hotel_room_category = base.hotel_room_category and two.hotel_room_category = base.hotel_room_category
AND base.date != one.date
  AND base.date != two.date AND two.date != one.date


GROUP BY (base.hotel_room_category, (base.hotel_rate_category, one.hotel_rate_category, two.hotel_rate_category)) 

;


select
  base.hotel_room_availability_id, base.hotel_rate_category, base.hotel_room_category, base.date,
  one.hotel_room_availability_id, one.hotel_rate_category, one.hotel_room_category, one.date,
  two.hotel_room_availability_id, two.hotel_rate_category, two.hotel_room_category, two.date
from

      (SELECT *
              FROM v1.hra_upsert
              WHERE hotel_id = '59846'
              --and B.date != C.date
              --and B.hotel_room_category = C.hotel_room_category
              AND date = ANY ((SELECT array_agg(x :: DATE)
      FROM generate_series('2016-01-10' :: DATE, '2016-01-14' :: DATE, '1 day') AS x) :: DATE [])
      AND NOT (arrival_restriction_status = 'D' AND date = '2016-01-10')
      AND NOT (departure_restriction_status = 'D' AND date = '2016-01-14')


      -- make sure room is sellable in general
      AND master_restriction_status = 'O'

      -- have the booking limit
      AND booking_limit > 1
      AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los < 7) AND
       one_person_rate_after_tax NOTNULL
      ) as base,
  (SELECT *
   FROM v1.hra_upsert
   WHERE hotel_id = '59846'
         --and B.date != C.date
         --and B.hotel_room_category = C.hotel_room_category
         AND date = ANY ((SELECT array_agg(x :: DATE)
                          FROM generate_series('2016-01-10' :: DATE, '2016-01-14' :: DATE, '1 day') AS x) :: DATE [])
         AND NOT (arrival_restriction_status = 'D' AND date = '2016-01-10')
         AND NOT (departure_restriction_status = 'D' AND date = '2016-01-14')


         -- make sure room is sellable in general
         AND master_restriction_status = 'O'

         -- have the booking limit
         AND booking_limit > 1
         AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los < 7) AND
         one_person_rate_after_tax NOTNULL
  ) as one,
  (SELECT *
   FROM v1.hra_upsert
   WHERE hotel_id = '59846'
         --and B.date != C.date
         --and B.hotel_room_category = C.hotel_room_category
         AND date = ANY ((SELECT array_agg(x :: DATE)
                          FROM generate_series('2016-01-10' :: DATE, '2016-01-14' :: DATE, '1 day') AS x) :: DATE [])
         AND NOT (arrival_restriction_status = 'D' AND date = '2016-01-10')
         AND NOT (departure_restriction_status = 'D' AND date = '2016-01-14')


         -- make sure room is sellable in general
         AND master_restriction_status = 'O'

         -- have the booking limit
         AND booking_limit > 1
         AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los < 7) AND
         one_person_rate_after_tax NOTNULL
  ) as two

where one.hotel_room_category = base.hotel_room_category and two.hotel_room_category = base.hotel_room_category
      AND base.date != one.date
      AND base.date != two.date AND two.date != one.date
;