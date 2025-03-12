package org.hswebframework.web.crud.generator;

import org.hswebframework.ezorm.core.DefaultValueGenerator;
import org.hswebframework.ezorm.core.RuntimeDefaultValue;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.web.id.IDGenerator;

public class RandomIdGenerator implements DefaultValueGenerator<RDBColumnMetadata> {
    @Override
    public String getSortId() {
        return Generators.RANDOM;
    }

    @Override
    public RuntimeDefaultValue generate(RDBColumnMetadata metadata) {
        return IDGenerator.RANDOM::generate;
    }

    @Override
    public String getName() {
        return "Random";
    }
}
