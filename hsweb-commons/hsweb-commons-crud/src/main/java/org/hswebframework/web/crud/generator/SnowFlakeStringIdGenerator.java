package org.hswebframework.web.crud.generator;

import org.hswebframework.ezorm.core.DefaultValue;
import org.hswebframework.ezorm.core.DefaultValueGenerator;
import org.hswebframework.ezorm.core.RuntimeDefaultValue;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.stereotype.Component;

public class SnowFlakeStringIdGenerator implements DefaultValueGenerator {
    @Override
    public String getSortId() {
        return "snow_flake";
    }

    @Override
    public DefaultValue generate() {
        return (RuntimeDefaultValue) IDGenerator.SNOW_FLAKE_STRING::generate;
    }

    @Override
    public String getName() {
        return "SnowFlake";
    }
}
