package org.hsweb.web.mybatis.plgins.pager.dialect;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.mybatis.plgins.pager.PagerHelper;
import org.springframework.stereotype.Component;
import org.webbuilder.utils.common.StringUtils;

/**
 * Created by zhouhao on 16-4-13.
 */
@Component
public class MysqlPagerHelper implements PagerHelper {
    @Override
    public String doPaging(QueryParam param, String sql) {
        StringBuilder builder = new StringBuilder(sql);
        if (param.isPaging())
            builder.append(" limit ")
                    .append(param.getPageSize() * param.getPageIndex())
                    .append(",")
                    .append(param.getPageSize() * (param.getPageIndex() + 1));
        return builder.toString();
    }


    @Override
    public String getDialect() {
        return "mysql";
    }
}
