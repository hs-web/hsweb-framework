package org.hswebframework.web.service.form.simple.dict;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.ezorm.core.OptionConverter;
import org.hswebframework.ezorm.core.ValueConverter;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.web.dao.mybatis.mapper.dict.DictInTermTypeMapper;
import org.hswebframework.web.dict.DictDefineRepository;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.entity.form.DictConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.JDBCType;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;


/**
 * @author zhouhao
 * @since 3.0
 */
@Component
@SuppressWarnings("all")
public class DictionaryOptionalConvertBuilderStrategy implements OptionalConvertBuilderStrategy {
    @Autowired(required = false)
    private DictDefineRepository dictDefineRepository;

    @Override
    public boolean support(String type) {
        return "dict".equals(type) && dictDefineRepository != null;
    }


    @Override
    public OptionConverter build(DictConfig dictConfig) {
        JSONObject conf = new JSONObject(dictConfig.getConfig());
        String dictId = conf.getString("dictId");
        String fieldName = dictConfig.getToField();
        String sppliter = conf.getString("spliter");
        String writeObject = conf.getString("writeObject");
        EnumDictOptionConverter<EnumDict<Object>> converter = new EnumDictOptionConverter<>(() -> dictDefineRepository.getDefine(dictId).getItems(), fieldName);

        converter.setWriteObject(!"false".equalsIgnoreCase(writeObject));

        if (!StringUtils.isEmpty(sppliter)) {
            converter.setSplitter(str -> Arrays.asList(str.split(sppliter)));
        }

        return converter;
    }

    @Override
    public ValueConverter buildValueConverter(DictConfig dictConfig) {
        JSONObject conf = new JSONObject(dictConfig.getConfig());
        String dictId = conf.getString("dictId");
        boolean multi = !"false".equalsIgnoreCase(conf.getString("multi"));

        Supplier<List<EnumDict<Object>>> supplier = () -> dictDefineRepository.getDefine(dictId).getItems();

        EnumDictValueConverter<EnumDict<Object>> converter = new EnumDictValueConverter<>(supplier);
        converter.setMulti(multi);

        RDBColumnMetaData column = dictConfig.getColumn();
        if (multi && (column.getJdbcType() == JDBCType.NUMERIC || column.getJdbcType() == JDBCType.BIGINT)) {
            if (supplier.get().size() < 64) {
                column.setProperty(DictInTermTypeMapper.USE_DICT_MASK_FLAG, true);
                converter.setDataToMask(true);
            } else {
                throw new UnsupportedOperationException("数据类型为数字,并且数据字典选项数量超过64个,不支持多选!");
            }
        }

        return converter;
    }
}
