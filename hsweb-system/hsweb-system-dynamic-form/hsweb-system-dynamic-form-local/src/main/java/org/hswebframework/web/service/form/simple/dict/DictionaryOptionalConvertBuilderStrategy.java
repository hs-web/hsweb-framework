package org.hswebframework.web.service.form.simple.dict;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.ezorm.core.OptionConverter;
import org.hswebframework.web.dict.DictDefineRepository;
import org.hswebframework.web.entity.form.DictConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author zhouhao
 * @since 3.0
 */
@Component
public class DictionaryOptionalConvertBuilderStrategy implements OptionalConvertBuilderStrategy {
    @Autowired(required = false)
    private DictDefineRepository dictDefineRepository;

    @Override
    public boolean support(String type) {
        return "dict".equals(type) && dictDefineRepository != null;
    }


    @Override
    public OptionConverter build(DictConfig dictConfig) {
        JSONObject conf = JSON.parseObject(dictConfig.getConfig());
        String dictType = conf.getString("dictType");



        return null;
    }
}
