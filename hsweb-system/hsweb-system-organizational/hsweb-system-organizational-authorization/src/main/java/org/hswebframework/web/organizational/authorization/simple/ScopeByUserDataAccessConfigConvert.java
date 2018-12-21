package org.hswebframework.web.organizational.authorization.simple;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.simple.AbstractDataAccessConfig;
import org.hswebframework.web.authorization.simple.builder.DataAccessConfigConvert;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author zhouhao
 * @since 3.0.5
 */
@Getter
@Setter
public class ScopeByUserDataAccessConfigConvert implements DataAccessConfigConvert {

    @Override
    public boolean isSupport(String type, String action, String config) {
        return "SCOPE_BY_USER".equalsIgnoreCase(type);
    }

    @Override
    public DataAccessConfig convert(String type, String action, String config) {
        if (StringUtils.isEmpty(config)) {
            config = "{}";
        }
        ScopeByUserDataAccessConfig dataAccessConfig = JSON.parseObject(config, ScopeByUserDataAccessConfig.class);
        dataAccessConfig.setAction(action);
        return dataAccessConfig;
    }
}
