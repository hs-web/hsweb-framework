package org.hswebframework.web.dict;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.dict.defaults.DefaultClassDictDefine;
import org.hswebframework.web.dict.defaults.DefaultDictDefineRepository;
import org.hswebframework.web.dict.defaults.DefaultDictSupportApi;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.hswebframework.web.dict.EnumDict.*;
import static org.junit.Assert.*;

/**
 * @author zhouhao
 * @since 3.0
 */
public class DictDefineTest {

    private DefaultDictDefineRepository repository = new DefaultDictDefineRepository();

    private DictSupportApi api = new DefaultDictSupportApi(repository);

    @Test
    public void testJson(){

        UserCode code=UserCode.CODE0;

        String json  =JSON.toJSONString(code);
        System.out.println(json);
        Assert.assertNotNull(json);
        Assert.assertNotNull(JSON.parseObject(json,UserCode.class));

    }
    @Test
    public void testEnumDict() {

        Assert.assertEquals(UserCode.SIMPLE, findByValue(UserCode.class, UserCode.SIMPLE.getValue()).orElse(null));

        Assert.assertEquals(UserCode.SIMPLE, findByText(UserCode.class, UserCode.SIMPLE.getText()).orElse(null));

        Assert.assertEquals(UserCode.SIMPLE, find(UserCode.class, UserCode.SIMPLE.getText()).orElse(null));

        long bit = toMask( UserCode.values());

        System.out.println(maskIn(bit,UserCode.SIMPLE,UserCode.TEST,UserCode.SIMPLE));

        long bit2= toMask(UserCode.SIMPLE,UserCode.CODE0,UserCode.SIMPLE);

        Assert.assertTrue(maskInAny(bit2,UserCode.SIMPLE,UserCode.CODE4,UserCode.CODE0));
        Assert.assertFalse(maskInAny(bit2,UserCode.CODE1,UserCode.CODE4,UserCode.CODE5));

        for (UserCode userCode : UserCode.values()) {
            Assert.assertTrue(userCode.in(bit));
        }

        List<UserCode> codes = getByMask(UserCode.class, bit);


    }

    @Test
    public void testParse() {
//        JSON.toJSONString("",SerializerFeature.PrettyFormat)
        DefaultClassDictDefine define = DefaultClassDictDefine.builder()
                .id("test-code")
                .field("code")
                .build();
        repository.registerDefine(define);


        List<ClassDictDefine> defines = repository.getDefine(UseDictEntity2.class);
        assertFalse(defines.isEmpty());
        assertEquals(defines.size(), 2);

        defines = repository.getDefine(UserCode.class);
        assertFalse(defines.isEmpty());
        assertEquals(defines.size(), 1);

        assertEquals(defines.get(0).getItems().size(), UserCode.values());


    }

    @Test
    public void testWrap() {
        assertNull(api.wrap(null));
        assertNotNull(api.wrap(new HashMap<>()));
        assertNull(api.unwrap(null));
        assertNotNull(api.unwrap(new HashMap<>()));

        UseDictEntity2 entity = new UseDictEntity2();
        entity.setStatus(new Integer(1).byteValue());

        entity = api.wrap(entity);

        assertEquals(entity.getStatusText(), "正常");

        entity.setStatus(null);
        entity = api.unwrap(entity);
        assertEquals(entity.getStatus(), Byte.valueOf((byte) 1));

        entity.setStatus((byte) 2);
        entity = api.unwrap(entity);
        assertEquals(entity.getStatus(), Byte.valueOf((byte) 2));

        entity.setStatus(null);
        entity.setStatusText(null);
        entity.setCode("1");
        api.wrap(entity);

        assertNull(entity.getStatusText());
        assertEquals(entity.getCode(), "1");

        api.unwrap(entity);
        assertEquals(entity.getCode(), "1");
    }
}