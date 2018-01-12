alter table hra_upsert drop COLUMN  rates  ;
alter table hra_upsert add COLUMN  rates  JSONB;

update hra_upsert as t set rates = json_build_object('currency', (CASE WHEN t.currency_code IS NULL
  THEN 'Dollar'
                                                                  ELSE t.currency_code END)
, 'base', json_build_object('before', t.base_rate_before_tax, 'after', t.base_rate_after_tax)
, 'adults', json_build_array(
                                                         json_build_object('before', t.one_person_rate_before_tax,
                                                                           'after',
                                                                           t.one_person_rate_after_tax),
                                                         json_build_object('before', t.two_person_rate_before_tax,
                                                                           'after',
                                                                           t.two_person_rate_after_tax),
                                                         json_build_object('before', t.three_person_rate_before_tax,
                                                                           'after',
                                                                           t.three_person_rate_after_tax),
                                                         json_build_object('before', t.four_person_rate_before_tax,
                                                                           'after',
                                                                           t.four_person_rate_after_tax)


                                                     )
, 'extras', json_build_array(
                                                         json_build_object('code', t.age_rate_code,
                                                                           'rates', json_build_array(
                                                                               json_build_object('before',
                                                                                                 t.age_qualified_code_before_tax,
                                                                                                 'after',
                                                                                                 t.age_qualified_code_after_tax)

                                                                           )
                                                         )
                                                     )
);



-- JUST TO View the data in JSON

select json_build_object('currency', currency_code
, 'base', json_build_object('before', base_rate_before_tax, 'after', base_rate_after_tax)
, 'adults', json_build_array(
                                   json_build_object('before', one_person_rate_before_tax, 'after',
                                                     one_person_rate_after_tax),
                                   json_build_object('before', two_person_rate_before_tax, 'after',
                                                     two_person_rate_after_tax),
                                   json_build_object('before', three_person_rate_before_tax, 'after',
                                                     three_person_rate_after_tax),
                                   json_build_object('before', four_person_rate_before_tax, 'after',
                                                     four_person_rate_after_tax)


                               ),
                               'extras', json_build_array(
                                   json_build_object('code', age_rate_code,
                                                     'rates', json_build_array(
                                                         json_build_object('before', age_qualified_code_before_tax,
                                                                           'after',
                                                                           age_qualified_code_after_tax)
                                                     )
                                   )
                               )
             )

from (
       SELECT
         h.currency_code,

         h.base_rate_before_tax,
         h.base_rate_after_tax,

         h.one_person_rate_before_tax,
         h.one_person_rate_after_tax,
         h.two_person_rate_before_tax,
         h.two_person_rate_after_tax,
         h.three_person_rate_before_tax,
         h.three_person_rate_after_tax,
         h.four_person_rate_before_tax,
         h.four_person_rate_after_tax,
         h.age_rate_code,
         h.age_qualified_code_before_tax,
         h.age_qualified_code_after_tax

       FROM hra_upsert AS h
     ) as t
;