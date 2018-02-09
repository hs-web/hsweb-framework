package org.hswebframework.web.organizational.authorization.simple;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.simple.builder.DataAccessConfigConvert;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

import static org.hswebframework.web.organizational.authorization.access.DataAccessType.*;

/**
 * @author zhouhao
 */
public class CustomScopeDataAccessConfigConvert implements DataAccessConfigConvert {
    private static final List<String> supportTypes = Arrays.asList(
            DataAccessType.SCOPE_TYPE_CUSTOM
    );

    @Override
    public boolean isSupport(String type, String action, String config) {
        return supportTypes.contains(type);
    }

    @Override
    public DataAccessConfig convert(String type, String action, String config) {
        if (StringUtils.isEmpty(config)) {
            config = "{}";
        }
        SimpleCustomScopeDataAccessConfig accessConfig = JSON.parseObject(config, SimpleCustomScopeDataAccessConfig.class);
        accessConfig.setAction(action);

        return accessConfig;
    }
}
