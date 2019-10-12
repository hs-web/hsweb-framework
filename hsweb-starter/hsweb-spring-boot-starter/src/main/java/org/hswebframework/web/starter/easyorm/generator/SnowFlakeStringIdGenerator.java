package org.hswebframework.web.starter.easyorm.generator;

import org.hswebframework.ezorm.core.DefaultValueGenerator;
import org.hswebframework.ezorm.core.RuntimeDefaultValue;
import org.hswebframework.web.id.IDGenerator;

public class SnowFlakeStringIdGenerator implements DefaultValueGenerator {
    @Override
    public String getSortId() {
        return "snow_flake";
    }

    @Override
    public RuntimeDefaultValue generate() {
        return IDGenerator.SNOW_FLAKE_STRING::generate;
    }

    @Override
    public String getName() {
        return "SnowFlake";
    }
}
