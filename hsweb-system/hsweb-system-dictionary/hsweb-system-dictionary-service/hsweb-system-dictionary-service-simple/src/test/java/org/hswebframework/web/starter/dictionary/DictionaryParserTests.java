/*
 *  Copyright 2016 http://www.hswebframework.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.hswebframework.web.starter.dictionary;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.entity.dictionary.DictionaryItemEntity;
import org.hswebframework.web.entity.dictionary.SimpleDictionaryEntity;
import org.hswebframework.web.entity.dictionary.SimpleDictionaryItemEntity;
import org.hswebframework.web.service.dictionary.DictionaryParser;
import org.hswebframework.web.service.dictionary.simple.SimpleDictionaryParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 */
public class DictionaryParserTests {

    DictionaryParser<String> parser;

    @Before
    public void init() {
        SimpleDictionaryEntity dictionaryEntity = new SimpleDictionaryEntity();
        String json = "[" +
                "{'value':'1','text':'水果','children':" +
                "[" +
                "{'value':'101','text':'苹果'," +
                "'children':[" +
                "{'value':'10102','text':'红富士'}" +
                ",{'value':'10103','text':'青苹果'}" +
                //使用表达式进行解析
                ",{'id':'10105','value':'10105','text':'其他苹果'}" +
                "]}" +
                ",{'value':'102','text':'梨子'}]" +
                "}" +
                ",{'value':'2','text':'蔬菜'}" +
                "]";

        List<DictionaryItemEntity> itemEntities = JSON.parseArray(json, DictionaryItemEntity.class);
        dictionaryEntity.setItems(itemEntities);
        this.parser = new SimpleDictionaryParser<String>()
                .addToTextExpression("10105", "${#value}[${#context[otherApple]}]")
                .addToValueExpression("10105", "${(#context.put('otherApple',#pattern.split(\"[ \\[ \\]]\")[1])==null)?#value:#value}")
                .setDict(dictionaryEntity);
    }

    //支持表达式
    @Test
    public void testParseExpression() {

        String val = "1,2,101,10102,10105";

        Map<String, Object> data = new HashMap<>();
        data.put("otherApple", "其他苹果1号");

        String text = parser
                .valueToText(val, data)
                .get();

        System.out.println(text);
        data.clear();
        String parseVal = parser.textToValue(text, data)
                .get();
        System.out.println(parseVal);
        System.out.println(data);

    }

    //普通的解析
    @Test
    public void testParseText() throws Exception {
        String val = "1,101,10102,10103,2";

        String text = parser.valueToText(val).get();

        System.out.println(text);
        String parsedVal = parser.textToValue(text).get();

        System.out.println(parsedVal);
        Assert.assertEquals(val, parsedVal);

    }
}
