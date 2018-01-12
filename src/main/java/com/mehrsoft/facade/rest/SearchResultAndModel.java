package com.mehrsoft.facade.rest;

import java.util.List;

/**
 * Created by daryoush on 11/18/16.
 */
public class SearchResultAndModel {

    LinearModel model = new LinearModel();
    List<? extends AggregateHotelRoomAvailability> aggs;

    public SearchResultAndModel(List<? extends AggregateHotelRoomAvailability> aggs) {
        this.aggs = aggs;
    }

    public LinearModel getModel() {
        return model;
    }

    public void setModel(LinearModel model) {
        this.model = model;
    }

    public List<? extends AggregateHotelRoomAvailability> getAggs() {
        return aggs;
    }
}
