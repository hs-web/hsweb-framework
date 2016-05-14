package org.hsweb.web.bean.po.module;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.hibernate.validator.constraints.NotEmpty;
import org.hsweb.web.bean.po.GenericPo;

import javax.validation.constraints.NotNull;
import java.util.*;


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
    private String pId = "-1";

    //备注
    private String remark;

    //状态
    private int status = 1;

    //模块操作选项
    private String mOption;

    //排序
    private long sortIndex;

    private String oldId;

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
    public String getPId() {
        if (this.pId == null)
            return "1";
        return this.pId;
    }

    /**
     * 设置 父级模块主键
     */
    public void setPId(String pId) {
        this.pId = pId;
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
    public String getMOption() {
        return this.mOption;
    }

    public Map<String, Object> getMOptionMap() {
        try {
            List<Map<String, Object>> opt = JSON.parseObject(getMOption(),new TypeReference<LinkedList<Map<String, Object>>>(){});
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

    /**
     * 设置 模块操作选项
     */
    public void setMOption(String mOption) {
        this.mOption = mOption;
    }

    @Override
    public int compareTo(Module o) {
        return getSortIndex() > o.getSortIndex() ? 1 : 1;
    }

    public String getOldId() {
        if (oldId == null)
            oldId = getUId();
        return oldId;
    }

    public void setOldId(String oldId) {
        this.oldId = oldId;
    }

}
