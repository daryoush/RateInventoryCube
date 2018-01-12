package com.mehrsoft.facade.rest;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by daryoush on 11/15/16.
 */
public class LinearModel {

        Map<String, Map<String, Object>> variables = new HashMap<>();
        Map<String, Map<String, ? extends Object>> constraints = new HashMap<>();
        String optimize;
        String opType;

        public LinearModel() {
//             optimize = "capacity";
//             opType = "max";

//            addVariable("brit", ImmutableMap.of("capacity", 20000,
//                    "plane", 1,
//                    "person", 8,
//                    "cost", 5000));
//            addVariable("yank",  ImmutableMap.of("capacity", 30000,
//                    "plane", 1,
//                    "person", 16,
//                    "cost", 9000));
//
//            addConstraints("plane", ImmutableMap.of("max", 44));
//            addConstraints("person", ImmutableMap.of("max", 512));
//            addConstraints("cost", ImmutableMap.of("max", 300000));

        }

        public Map<String, Map<String, Object>> getVariables() {
            return variables;
        }

        public void addVariable(
                String name, Map<String, Object> attributes) {
             variables.put(name, attributes);
        }

        public Map<String, Map<String, ? extends Object>> getConstraints() {
            return constraints;
        }

        public void addConstraints(
                String name, Map<String, ? extends Object> attributes) {
            constraints.put(name, attributes);
        }

        public String getOptimize() {
            return optimize;
        }

        public void setOptimize(String optimize) {
            this.optimize = optimize;
        }

        public String getOpType() {
            return opType;
        }

        public void setOpType(String opType) {
            this.opType = opType;
        }
}
