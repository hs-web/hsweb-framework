package org.hswebframework.web.organizational.authorization.simple;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.simple.builder.DataAccessConfigConvert;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

import static org.hswebframework.web.organizational.authorization.access.DataAccessType.*;

/**
 * @author zhouhao
 */
public class ScopeDataAccessConfigConvert implements DataAccessConfigConvert {
    private static final List<String> supportTypes = Arrays.asList(
            DISTRICT_SCOPE, ORG_SCOPE, DEPARTMENT_SCOPE, POSITION_SCOPE, PERSON_SCOPE
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
        SimpleScopeDataAccessConfig accessConfig = JSON.parseObject(config, SimpleScopeDataAccessConfig.class);
        accessConfig.setAction(action);
        accessConfig.setType(type);
        return accessConfig;
    }
}
