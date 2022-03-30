package org.hswebframework.web.crud.generator;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.DefaultValue;
import org.hswebframework.ezorm.core.DefaultValueGenerator;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DefaultIdGenerator implements DefaultValueGenerator<RDBColumnMetadata> {

    @Getter
    @Setter
    private String defaultId = Generators.SNOW_FLAKE;

    @Getter
    @Setter
    private Map<String, String> mappings = new HashMap<>();

    @Override
    public String getSortId() {
        return Generators.DEFAULT_ID_GENERATOR;
    }

    @Override
    @SneakyThrows
    public DefaultValue generate(RDBColumnMetadata metadata) {
        String genId = mappings.getOrDefault(metadata.getOwner().getName(), defaultId);
        DefaultValueGenerator<RDBColumnMetadata> generator = metadata.findFeatureNow(DefaultValueGenerator.createId(genId));
        log.debug("use default id generator : {} for column : {}", generator.getSortId(), metadata.getFullName());
        return generator.generate(metadata);
    }

    @Override
    public String getName() {
        return "默认ID生成器";
    }

}
