function inventoryChart() {

    var obj = {
        "getSpecification": function (data) {  // Top level function called from main
            var vlSpec = {
                // "width": 800,
                //"height": 800,
                "data": getData(data),
                "mark": getMark(),
                "transform": getTransform(),
                "encoding": getEncodings(),
                //"config": {"cell": {"width": 500,"height": 400}},
            };


            return vlSpec;
        }
    };

    // Local methods
    function getMark() {
        return "point";  //"circle";
    }

    function getData(data) {
        var obj = {"values": data};
        return obj;
    }

    function getTransform() {
        var obj = {
            "calculate": [{"field": "Master Status", "expr": "datum.restrictions[0].status"},
                {"field": "Arrival Status", "expr": "datum.restrictions[1].status"},
                {"field": "Departure Status", "expr": "datum.restrictions[2].status"}],
            // "filter": "datum.bookingLimit >= 0  && datum.roomCategory == 'B2V'"
        };
        return obj;
    }


    function getEncodings() {
        var obj = {
            "row": {"field": "rateCategory", "type": "ordinal"},
            "column": {"field": "roomCategory", "type": "ordinal"},

            "x": {
                "field": "date",
                "type": "temporal",
                // DOESN't seem to work!! "scale": {"bandSize": 30}  // for terllis https://vega.github.io/vega-lite/docs/size.html
            },
            "y": {
                "field": "twoPersonRateBeforeTax",
                "type": "quantitative",
                "title": "Two Person Price",
                //"scale": {"type": "log"}
                "scale": {"domain": [50, 550]},
            },
            // "size": {"field": "bookingLimit", "type": "quantitative"},
            // "color": {"value": "#000"}//  TODO get status to be the color  OOO, OCC
            "color": {"field": "bookingLimit", "type": "quantitative"},
            "shape": {"field": "Master Status", "type": "ordinal"},

        };
        return obj;
    }


    return obj;
}



function invSingleChart() {

    var obj = {
        "getSpecification": function (data) {  // Top level function called from main
            var vlSpec = {
                "description": "",
                "data": getData(data),
                "transform": { "calculate": [{"field": "ydata", "expr": "datum. twoPersonRateBeforeTax"}],
                    //  "filter": "datum.rooms[0] == 'B2V'"
                },
                //  "config": {"cell": {"width": 500,"height": 400}},

                "layers": [
                    {
                        "mark": "rule",
                        "encoding": {
                            "x": {
                                "field": "date",
                                "type": "temporal",
                            },
                            "y": {
                                "aggregate": "min",
                                "field": "ydata",
                                "type": "quantitative",
                                "axis": {"title": "Price"}
                            },
                            "y2": {
                                "aggregate": "max",
                                "field": "ydata",
                                "type": "quantitative"
                            },
                        }
                    },
                    {
                        "mark": "tick",
                        "encoding": {
                            "x": {
                                "field": "date",
                                "type": "temporal",
                            },
                            "y": {
                                "aggregate": "min",
                                "field": "ydata",
                                "type": "quantitative",
                                "axis": {"title": "Price"}
                            },
                            "size": {"value": 5}
                        }
                    },
                    {
                        "mark": "tick",
                        "encoding": {
                            "x": {
                                "field": "date",
                                "type": "temporal",
                            },
                            "y": {
                                "aggregate": "max",
                                "field": "ydata",
                                "type": "quantitative",
                                "axis": {"title": "Price"}
                            },
                            "size": {"value": 5}
                        }
                    },
                    {
                        "mark": "point",
                        "encoding": {
                            "x": {
                                "field": "date",
                                "type": "temporal",
                            },
                            "y": {
                                "aggregate": "mean",
                                "field": "ydata",
                                "type": "quantitative",
                                "axis": {"title": "Price"}
                            },
                            "size": {"value": 2},

                        }
                    }
                ]
            }


            return vlSpec;
        }
    };
    return obj;


    // Local methods
    function getMark() {
        return "point";  //"circle";
    }

    function getData(data) {
        var obj = {"values": data};
        return obj;
    }

    function getTransform() {
        var obj = {
            // calculate adds a new field, transform modifies data
            "calculate": [{"field": "Master Status", "expr": "datum.restrictions[0].status"},
                {"field": "Arrival Status", "expr": "datum.restrictions[0].status"},
                {"field": "Departure Status", "expr": "datum.restrictions[0].status"}],
            // "filter": "datum.bookingLimit >= 0  && datum.roomCategory == 'B2V'"
        };
        return obj;
    }


    function getEncodings() {
        var obj = {
            "row": {"field": "rateCategory", "type": "ordinal"},
            "column": {"field": "roomCategory", "type": "ordinal"},

            "x": {
                "field": "date",
                "type": "temporal",
                // DOESN't seem to work!! "scale": {"bandSize": 30}  // for terllis https://vega.github.io/vega-lite/docs/size.html
            },
            "y": {
                "field": "twoPersonRateBeforeTax",
                "type": "quantitative",
                "title": "Two Person Price",
                //"scale": {"type": "log"}
                "scale": {"domain": [50, 550]},
            },
            // "size": {"field": "bookingLimit", "type": "quantitative"},
            // "color": {"value": "#000"}//  TODO get status to be the color  OOO, OCC
            "color": {"field": "bookingLimit", "type": "quantitative"},
            "shape": {"field": "Master Status", "type": "ordinal"},

        };
        return obj;
    }
}
