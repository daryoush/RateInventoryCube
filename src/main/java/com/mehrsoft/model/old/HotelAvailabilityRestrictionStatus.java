package com.mehrsoft.model.old;

/**
 * Created by ijet on 8/24/16.
 */

import com.fasterxml.jackson.annotation.JsonProperty;

//SEE   http://stackoverflow.com/questions/12468764/jackson-enum-serializing-and-deserializer
//http://stackoverflow.com/questions/9300191/how-to-annotate-enum-fields-for-deserialization-using-jackson-json
public enum HotelAvailabilityRestrictionStatus  {
  O,//open
    D //closed

}

