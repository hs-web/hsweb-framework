package org.hswebframework.web.tests;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.test.web.servlet.ResultActions;

/**
 * @author zhouhao
 * @TODO
 */
public interface TestResult {
    ResultActions getResultActions() throws Exception;

    default String resultAsString() {
        try {
            return getResultActions().andReturn().getResponse().getContentAsString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default JSONObject resultAsJson() {
        return JSON.parseObject(resultAsString());
    }

    default JSONArray resultAsJsonArray() {
        return JSON.parseArray(resultAsString());
    }
}
