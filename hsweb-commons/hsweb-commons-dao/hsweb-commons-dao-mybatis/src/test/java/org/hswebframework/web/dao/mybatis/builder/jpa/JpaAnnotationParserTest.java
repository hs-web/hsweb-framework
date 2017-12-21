package org.hswebframework.web.dao.mybatis.builder.jpa;

import org.hswebframework.ezorm.rdb.meta.RDBTableMetaData;
import org.junit.Test;
import org.springframework.util.Assert;

import static org.junit.Assert.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since
 */
public class JpaAnnotationParserTest {


    @Test
    public void testParse() {
        RDBTableMetaData metaData = JpaAnnotationParser.parseMetaDataFromEntity(TestEntity.class);

        Assert.notNull(metaData, "metadata parse error");
    }
}
