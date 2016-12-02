package org.hsweb.web.bean.po.module;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.hsweb.web.bean.po.GenericPo;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 系统模块
 * Created by generator
 */
public class Module extends GenericPo<String> implements Comparable<Module> {
    private static final long serialVersionUID = 8910856253780046561L;

    //模块名称
    @NotNull(message = "名称不能为空")
    @NotEmpty(message = "名称不能为空")
    private String name;

    //模块路径
    private String uri;

    //模块图标
    private String icon;

    //父级模块主键
    private String parentId = "-1";

    //备注
    private String remark;

    //状态
    private int status = 1;

    //模块操作选项
    private String optional;

    //排序
    private long sortIndex;

    /**
     * 获取 模块名称
     *
     * @return String 模块名称
     */
    public String getName() {
        if (this.name == null)
            return "";
        return this.name;
    }

    /**
     * 设置 模块名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取 模块路径
     *
     * @return String 模块路径
     */
    public String getUri() {
        if (this.uri == null)
            return "";
        return this.uri;
    }

    /**
     * 设置 模块路径
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * 获取 模块图标
     *
     * @return String 模块图标
     */
    public String getIcon() {
        if (this.icon == null)
            return "";
        return this.icon;
    }

    /**
     * 设置 模块图标
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 获取 父级模块主键
     *
     * @return String 父级模块主键
     */
    public String getParentId() {
        if (this.parentId == null)
            return "1";
        return this.parentId;
    }

    /**
     * 设置 父级模块主键
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * 获取 备注
     *
     * @return String 备注
     */
    public String getRemark() {
        if (this.remark == null)
            return "";
        return this.remark;
    }

    /**
     * 设置 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取 状态
     *
     * @return int 状态
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * 设置 状态
     */
    public void setStatus(int status) {
        this.status = status;
    }

    public long getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(long sortIndex) {
        this.sortIndex = sortIndex;
    }

    /**
     * 获取 模块操作选项
     *
     * @return String 模块操作选项
     */
    public String getOptional() {
        return this.optional;
    }

    public Map<String, Object> getOptionalMap() {
        try {
            List<Map<String, Object>> opt = JSON.parseObject(getOptional(), new TypeReference<LinkedList<Map<String, Object>>>() {
            });
            if (opt == null) return new HashMap<>();
            Map<String, Object> all = new LinkedHashMap<>();
            for (Map<String, Object> map : opt) {
                all.put(String.valueOf(map.get("id")), map);
            }
            return all;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public void setOptional(String optional) {
        this.optional = optional;
    }

    /**
     * 设置 模块操作选项
     */

    @Override
    public int compareTo(Module o) {
        return ((Long) getSortIndex()).compareTo(o.getSortIndex());
    }


    public interface Property extends GenericPo.Property {
        /**
         * @see Module#name
         */
        String name      = "name";
        /**
         * @see Module#uri
         */
        String uri       = "uri";
        /**
         * @see Module#icon
         */
        String icon      = "icon";
        /**
         * @see Module#parentId
         */
        String parentId  = "parentId";
        /**
         * @see Module#remark
         */
        String remark    = "remark";
        /**
         * @see Module#status
         */
        String status    = "status";
        /**
         * @see Module#optional
         */
        String optional  = "optional";
        /**
         * @see Module#sortIndex
         */
        String sortIndex = "sortIndex";
    }

}