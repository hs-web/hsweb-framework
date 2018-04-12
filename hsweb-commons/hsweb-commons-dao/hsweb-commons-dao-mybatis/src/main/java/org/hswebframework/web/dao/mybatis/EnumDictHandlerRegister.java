package org.hswebframework.web.dao.mybatis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.hswebframework.web.dict.EnumDict;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.sql.*;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

@Slf4j
public class EnumDictHandlerRegister {

    static TypeHandlerRegistry typeHandlerRegistry;

    private static MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

    private static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();


    public static void register(String packages) {
        register(tokenizeToStringArray(packages,
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    public static void register(String[] packages) {
        if (typeHandlerRegistry == null) {
            log.error("请在spring容器初始化后再调用此方法!");
            return;
        }
        for (String basePackage : packages) {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(basePackage) + "/**/*.class";
            try {
                Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
                for (Resource resource : resources) {
                    try {
                        MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);
                        Class enumType = Class.forName(reader.getClassMetadata().getClassName());
                        if (enumType.isEnum() && EnumDict.class.isAssignableFrom(enumType)) {
                            log.debug("register typeHandler for enum dict:{}", enumType);
                            typeHandlerRegistry.register(enumType, new EnumDictHandler(enumType));
                        }
                    } catch (Exception ignore) {

                    }
                }
            } catch (IOException e) {
                log.warn("register enum dict error", e);
            }
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.BIT,
            JdbcType.BOOLEAN, JdbcType.NUMERIC,
            JdbcType.TINYINT, JdbcType.INTEGER,
            JdbcType.BIGINT, JdbcType.DECIMAL,
            JdbcType.CHAR})
    static class EnumDictHandler<T extends Enum & EnumDict> implements TypeHandler<T> {

        private Class<T> type;

        @Override
        public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
            ps.setObject(i, parameter.getValue());
        }

        @Override
        public T getResult(ResultSet rs, String columnName) throws SQLException {
            Object val = rs.getObject(columnName);
            return EnumDict.findByValue(getType(), val).orElse(null);
        }

        @Override
        public T getResult(ResultSet rs, int columnIndex) throws SQLException {
            Object val = rs.getObject(columnIndex);
            return EnumDict.findByValue(getType(), val).orElse(null);
        }

        @Override
        public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
            Object val = cs.getObject(columnIndex);
            return EnumDict.findByValue(getType(), val).orElse(null);
        }
    }
}
