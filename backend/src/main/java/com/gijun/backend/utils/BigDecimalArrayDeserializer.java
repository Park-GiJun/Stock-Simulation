package com.gijun.backend.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalArrayDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (p.isExpectedStartArrayToken()) {
            p.nextToken();
            BigDecimal value = p.getDecimalValue();
            p.nextToken();
            return value;
        }
        return p.getDecimalValue();
    }
}
