package org.hsweb.web.bean.param;

import java.util.*;

/**
 * Created by 浩 on 2016-01-16 0016.
 */
public class QueryParam extends HashMap<String, Object> {
    private static final long serialVersionUID = 7941767360194797891L;

    /**
     * 是否进行分页，默认为true
     */
    private boolean paging = true;

    /**
     * 第几页 从0开始
     */
    private int pageIndex = 0;

    /**
     * 每页显示记录条数
     */
    private int pageSize = 25;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式 DESC 反序 ASC 正序
     */
    private String sortOrder;

    /**
     * 指定要查询的字段
     */
    private Set<String> includes = new LinkedHashSet<>();

    /**
     * 指定不查询的字段
     */
    private Set<String> excludes = new LinkedHashSet<>();

    /**
     * 指定查询的字段列表，如传入 username,name，在sql里就只会执行 select username,name from table。
     *
     * @param fields 查询的字段列表
     * @return this 引用
     */
    public QueryParam includes(String... fields) {
        includes.addAll(Arrays.asList(fields));
        return this;
    }

    /**
     * 指定不需要查询的的字段列表
     *
     * @param fields 不需要查询的字段列表
     * @return this 引用
     */
    public QueryParam excludes(String... fields) {
        excludes.addAll(Arrays.asList(fields));
        includes.removeAll(Arrays.asList(fields));
        return this;
    }

    public QueryParam where(String key, Object value) {
        this.put(key, value);
        return this;
    }

    public QueryParam where(Map<String, Object> conditions) {
        this.putAll(conditions);
        return this;
    }

    public boolean isPaging() {
        return paging;
    }

    public void setPaging(boolean paging) {
        this.paging = paging;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Set<String> getIncludes() {
        return includes;
    }

    public void setIncludes(Set<String> includes) {
        this.includes = includes;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }
}
