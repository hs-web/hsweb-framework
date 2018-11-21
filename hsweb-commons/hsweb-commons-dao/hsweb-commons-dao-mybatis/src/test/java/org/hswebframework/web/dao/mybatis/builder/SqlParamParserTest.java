package org.hswebframework.web.dao.mybatis.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.*;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.web.commons.entity.QueryEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.asm.ClassReader;

import java.lang.invoke.LambdaMetafactory;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * @author zhouhao
 * @since 1.0
 */
public class SqlParamParserTest {
    @SneakyThrows
    public static <T> void test(Function<T, Object> function) {
        Class t=function.getClass();

        System.out.println(t);
    }

    public static void main(String[] args) {
        test(TestQueryEntity::getName$like);
    }


    @Test
    public void testParseQueryParam() {
        Map<String, Object> queryParam = new LinkedHashMap<>();
        queryParam.put("name", "张三");
        queryParam.put("name$like$or", "王五");
        queryParam.put("and", TestQueryEntity
                .builder()
                .name$like("李四%").age$gt(1)
                .or(TestQueryEntity.builder().name$like("王五").age$gt(10).build())
                .build());

        QueryParamEntity entity = SqlParamParser.parseQueryParam(queryParam);

        Assert.assertTrue(!entity.getTerms().isEmpty());
        Assert.assertEquals(entity.getTerms().get(0).getColumn(), "name");
        Assert.assertEquals(entity.getTerms().get(0).getType(), Term.Type.and);

        Assert.assertEquals(entity.getTerms().get(1).getColumn(), "name");
        Assert.assertEquals(entity.getTerms().get(1).getTermType(), "like");
        Assert.assertEquals(entity.getTerms().get(1).getType(), Term.Type.or);


        Assert.assertEquals(entity.getTerms().get(2).getType(), Term.Type.and);
        Assert.assertTrue(!entity.getTerms().get(2).getTerms().isEmpty());
        Assert.assertEquals(entity.getTerms().get(2).getTerms().get(0).getTermType(), "like");

        Assert.assertEquals(entity.getTerms().get(2).getTerms().get(1).getTermType(), "gt");

        Assert.assertTrue(!entity.getTerms().get(2).getTerms().get(2).getTerms().isEmpty());
        Assert.assertEquals(entity.getTerms().get(2).getTerms().get(2).getTerms().get(0).getTermType(), "like");
        Assert.assertEquals(entity.getTerms().get(2).getTerms().get(2).getTerms().get(1).getTermType(), "gt");

        System.out.println(JSON.toJSONString(entity, SerializerFeature.PrettyFormat));
    }


}