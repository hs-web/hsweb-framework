package org.hsweb.web.mybatis.plgins.pager.dialect;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.mybatis.plgins.pager.PagerHelper;
import org.springframework.stereotype.Component;
import org.webbuilder.utils.common.StringUtils;

/**
 * Created by zhouhao on 16-4-13.
 */
@Component
public class OraclePagerHelper implements PagerHelper {
    @Override
    public String doPaging(QueryParam param, String sql) {
        if (!param.isPaging()) {
            return buildSortSql(param, sql);
        }
        int startWith = param.getPageSize() * (param.getPageIndex() + 1);
        StringBuilder builder = new StringBuilder()
                .append("select * from ( select row_.*, rownum rownum_ from (")
                .append(buildSortSql(param, sql))
                .append(") row_ )")
                .append("where rownum_ <= ")
                .append(startWith)
                .append(" and rownum_ > ")
                .append(param.getPageSize() * param.getPageIndex());
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
        return "oracle";
    }
}
