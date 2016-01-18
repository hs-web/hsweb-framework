package org.hsweb.web.bean.po.form;

import org.hsweb.web.bean.po.GenericPo;

/**
* 自定义表单
* Created by generator 
*/
public class Form extends GenericPo<String> {
    private static final long serialVersionUID = 8910856253780046561L;
    //主键
    private String u_id;

    //名称
    private String name;

    //定义内容
    private String content;

    //数据库表名
    private String table_name;

    //表关联信息
    private String foreigns;

    //备注
    private String remark;

    //自动维护
    private boolean auto_alter;

    //创建时间
    private java.util.Date create_date;

    //最后一次修改时间
    private java.util.Date update_date;

    //数据库名称
    private String db_name;

    /**
    * 获取 主键
    * @return String 主键
    */
    public String getU_id(){
           if(this.u_id==null)
              return "";
        return this.u_id;
    }

    /**
    * 设置 主键
    */
    public void setU_id(String u_id){
        this.u_id=u_id;
    }

    /**
    * 获取 名称
    * @return String 名称
    */
    public String getName(){
           if(this.name==null)
              return "";
        return this.name;
    }

    /**
    * 设置 名称
    */
    public void setName(String name){
        this.name=name;
    }

    /**
    * 获取 定义内容
    * @return String 定义内容
    */
    public String getContent(){
           if(this.content==null)
              return "";
        return this.content;
    }

    /**
    * 设置 定义内容
    */
    public void setContent(String content){
        this.content=content;
    }

    /**
    * 获取 数据库表名
    * @return String 数据库表名
    */
    public String getTable_name(){
           if(this.table_name==null)
              return "";
        return this.table_name;
    }

    /**
    * 设置 数据库表名
    */
    public void setTable_name(String table_name){
        this.table_name=table_name;
    }

    /**
    * 获取 表关联信息
    * @return String 表关联信息
    */
    public String getForeigns(){
           if(this.foreigns==null)
              return "";
        return this.foreigns;
    }

    /**
    * 设置 表关联信息
    */
    public void setForeigns(String foreigns){
        this.foreigns=foreigns;
    }

    /**
    * 获取 备注
    * @return String 备注
    */
    public String getRemark(){
           if(this.remark==null)
              return "";
        return this.remark;
    }

    /**
    * 设置 备注
    */
    public void setRemark(String remark){
        this.remark=remark;
    }

    /**
    * 获取 自动维护
    * @return boolean 自动维护
    */
    public boolean isAuto_alter(){
        return this.auto_alter;
    }

    /**
    * 设置 自动维护
    */
    public void setAuto_alter(boolean auto_alter){
        this.auto_alter=auto_alter;
    }

    /**
    * 获取 创建时间
    * @return java.util.Date 创建时间
    */
    public java.util.Date getCreate_date(){
        return this.create_date;
    }

    /**
    * 设置 创建时间
    */
    public void setCreate_date(java.util.Date create_date){
        this.create_date=create_date;
    }

    /**
    * 获取 最后一次修改时间
    * @return java.util.Date 最后一次修改时间
    */
    public java.util.Date getUpdate_date(){
        return this.update_date;
    }

    /**
    * 设置 最后一次修改时间
    */
    public void setUpdate_date(java.util.Date update_date){
        this.update_date=update_date;
    }

    /**
    * 获取 数据库名称
    * @return String 数据库名称
    */
    public String getDb_name(){
           if(this.db_name==null)
              return "orcl";
        return this.db_name;
    }

    /**
    * 设置 数据库名称
    */
    public void setDb_name(String db_name){
        this.db_name=db_name;
    }

}
