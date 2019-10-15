package org.hswebframework.web.crud.generator;

import org.hswebframework.ezorm.core.DefaultValueGenerator;
import org.hswebframework.ezorm.core.RuntimeDefaultValue;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.web.id.IDGenerator;

public class MD5Generator implements DefaultValueGenerator<RDBColumnMetadata> {
    @Override
    public String getSortId() {
        return Generators.MD5;
    }

    @Override
    public RuntimeDefaultValue generate(RDBColumnMetadata metadata) {
        return IDGenerator.MD5::generate;
    }

    @Override
    public String getName() {
        return "MD5";
    }
}
