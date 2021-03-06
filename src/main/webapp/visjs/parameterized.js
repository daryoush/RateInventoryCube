

function byPriceParameter() {
    var obj = {
        "getSpecification": function (data) {  // Top level function called from main
            var vlSpec ={
                "parameters": [
                    {
                        "name": "job", "signal": "q", "type": "text",
                        "value": "", "placeholder": "search", "autocomplete": "off"
                    },
                    {
                        "signal": "sex", "type": "radio", "value": "all",
                        "options": ["men", "women", "all"]
                    }
                ],
                "spec": {
                    "width": 800,
                    "height": 500,
                    "padding": {"left": 15, "right": 65, "top": 10, "bottom": 25},
                    "signals": [
                        { "name": "sex", "init": "all" },
                        { "name": "q", "init": "",
                            "streams": [
                                {"type": "area:click", "expr": "datum.job"},
                                {"type": "dblclick", "expr": "''"}
                            ]
                        }
                    ],
                    "data": [
                        {
                            "name": "jobs",
                            "url": "https://vega.github.io/vega-editor/app/data/jobs.json",
                            "transform": [
                                {
                                    "type": "filter",
                                    "test": "(sex === 'all' || datum.sex === sex) && (!q || test(regexp(q,'i'), datum.job))"
                                },
                                {
                                    "type": "stack",
                                    "groupby": ["year"],
                                    "field": "perc",
                                    "sortby": ["-job", "-sex"]
                                }
                            ]
                        },
                        {
                            "name": "series",
                            "source": "jobs",
                            "transform": [
                                {
                                    "type": "facet",
                                    "groupby": ["job", "sex"],
                                    "summarize": [{
                                        "field": "perc",
                                        "ops": ["sum", "argmax"],
                                        "as": ["sum", "argmax"]
                                    }]
                                }
                            ]
                        },
                        {
                            "name": "years",
                            "source": "jobs",
                            "transform": [
                                {
                                    "type": "aggregate",
                                    "groupby": ["year"],
                                    "summarize": [{"field": "perc", "ops": ["sum"], "as": ["sum"]}]
                                }
                            ]
                        }
                    ],
                    "scales": [
                        {
                            "name": "x",
                            "type": "linear",
                            "range": "width",
                            "zero": false, "round": true,
                            "domain": {"data": "jobs", "field": "year"}
                        },
                        {
                            "name": "y",
                            "type": "linear",
                            "range": "height", "round": true,
                            "domain": {"data": "years", "field": "sum"}
                        },
                        {
                            "name": "color",
                            "type": "ordinal",
                            "domain": ["men", "women"],
                            "range": ["#33f", "#f33"]
                        },
                        {
                            "name": "alpha",
                            "type": "linear",
                            "domain": {"data": "series", "field": "sum"},
                            "range": [0.4, 0.8]
                        },
                        {
                            "name": "font",
                            "type": "sqrt",
                            "range": [0, 20], "round": true,
                            "domain": {"data": "series", "field": "argmax.perc"}
                        },
                        {
                            "name": "opacity",
                            "type": "quantile",
                            "range": [0, 0, 0, 0, 0, 0, 0.1, 0.2, 0.4, 0.7, 1.0],
                            "domain": {"data": "series", "field": "argmax.perc"}
                        },
                        {
                            "name": "align",
                            "type": "quantize",
                            "range": ["left", "center", "right"], "zero":false,
                            "domain": [1730, 2130]
                        },
                        {
                            "name": "offset",
                            "type": "quantize",
                            "range": [6, 0, -6], "zero":false,
                            "domain": [1730, 2130]
                        }
                    ],
                    "axes": [
                        {
                            "type": "x", "scale": "x", "format": "d",
                            "ticks": 15, "tickSizeEnd": 0
                        },
                        {
                            "type": "y", "scale": "y", "format": "%", "orient": "right",
                            "grid": true, "layer": "back", "tickSize": 12,
                            "properties": {
                                "axis": {"stroke": {"value":"transparent"}},
                                "grid": {"stroke": {"value": "#ccc"}},
                                "ticks": {"stroke": {"value": "#ccc"}}
                            }
                        }
                    ],
                    "marks": [
                        {
                            "type": "group",
                            "from": {"data": "series"},
                            "marks": [
                                {
                                    "type": "area",
                                    "properties": {
                                        "update": {
                                            "x": {"scale": "x", "field": "year"},
                                            "y": {"scale": "y", "field": "layout_start"},
                                            "y2": {"scale": "y", "field": "layout_end"},
                                            "fill": {"scale": "color", "field": "sex"},
                                            "fillOpacity": {
                                                "scale": "alpha",
                                                "field": {"parent": "sum"}
                                            }
                                        },
                                        "hover": {
                                            "fillOpacity": {"value": 0.2}
                                        }
                                    }
                                }
                            ]
                        },
                        {
                            "type": "text",
                            "from": {"data": "series"},
                            "interactive": false,
                            "properties": {
                                "update": {
                                    "x": {"scale": "x", "field": "argmax.year"},
                                    "dx": {"scale": "offset", "field": "argmax.year"},
                                    "y": {"scale": "y", "field": "argmax.layout_mid"},
                                    "fill": {"value": "#000"},
                                    "fillOpacity": {"scale": "opacity", "field": "argmax.perc"},
                                    "fontSize": {"scale": "font", "field": "argmax.perc", "offset": 5},
                                    "text": {"field": "job"},
                                    "align": {"scale": "align", "field": "argmax.year"},
                                    "baseline": {"value": "middle"}
                                }
                            }
                        }
                    ]
                }
            }


            return vlSpec;
        }
    };
    return obj;



}


