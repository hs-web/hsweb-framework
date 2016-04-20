package org.hsweb.web.mybatis.plgins.pager.dialect;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.mybatis.plgins.pager.PagerHelper;
import org.springframework.stereotype.Component;
import org.webbuilder.utils.common.StringUtils;

/**
 * Created by zhouhao on 16-4-13.
 */
@Component
public class H2PagerHelper implements PagerHelper {
    @Override
    public String doPaging(QueryParam param, String sql) {
        StringBuilder builder = new StringBuilder();
        builder.append(buildSortSql(param, sql)); //sql格式化
        if (param.isPaging())
            builder.append(" limit ")
                    .append(param.getPageSize() * param.getPageIndex())
                    .append(",")
                    .append(param.getPageSize() * (param.getPageIndex() + 1));
        return builder.toString();
    }

    protected String buildSortSql(QueryParam param, String sql) {
        StringBuilder builder = new StringBuilder(sql);
        if (!StringUtils.isNullOrEmpty(param.getSortField())) {
            builder.append(" order by ").append(param.getSortField());
            if (!StringUtils.isNullOrEmpty(param.getSortOrder())) {
                builder.append(" ").append(param.getSortOrder());
            }
        }
        return builder.toString();
    }

    @Override
    public String getDialect() {
        return "h2";
    }
}
