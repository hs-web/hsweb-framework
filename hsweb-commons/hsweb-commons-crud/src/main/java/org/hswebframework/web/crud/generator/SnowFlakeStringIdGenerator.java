package org.hswebframework.web.crud.generator;

import org.hswebframework.ezorm.core.DefaultValueGenerator;
import org.hswebframework.ezorm.core.RuntimeDefaultValue;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.web.id.IDGenerator;

public class SnowFlakeStringIdGenerator implements DefaultValueGenerator<RDBColumnMetadata> {
    @Override
    public String getSortId() {
        return Generators.SNOW_FLAKE;
    }

    @Override
    public RuntimeDefaultValue generate(RDBColumnMetadata metadata) {
        return IDGenerator.SNOW_FLAKE_STRING::generate;
    }

    @Override
    public String getName() {
        return "SnowFlake";
    }
}
