package org.hsweb.web.bean.po.classified;
import org.hsweb.web.bean.po.GenericPo;
/**
* 
* Created by hsweb-generator 2016-5-14 10:18:41
*/
public class Classified extends GenericPo<String>{
        //分类名称
        private String name;
        //备注
        private String remark;
        //分类类型
        private String type;
        //父级分类
        private String p_id;
        //显示图标
        private String icon;
        //其他配置
        private String config;
        //排序
        private int sort_index;

        /**
        * 获取 分类名称
        * @return java.lang.String 分类名称
        */
        public String getName(){
            if(this.name==null)
            return "";
        return this.name;
        }

        /**
        * 设置 分类名称
        */
        public void setName(String name){
        this.name=name;
        }
        /**
        * 获取 备注
        * @return java.lang.String 备注
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
        * 获取 分类类型
        * @return java.lang.String 分类类型
        */
        public String getType(){
            if(this.type==null)
            return "";
        return this.type;
        }

        /**
        * 设置 分类类型
        */
        public void setType(String type){
        this.type=type;
        }
        /**
        * 获取 父级分类
        * @return java.lang.String 父级分类
        */
        public String getP_id(){
            if(this.p_id==null)
            return "";
        return this.p_id;
        }

        /**
        * 设置 父级分类
        */
        public void setP_id(String p_id){
        this.p_id=p_id;
        }
        /**
        * 获取 显示图标
        * @return java.lang.String 显示图标
        */
        public String getIcon(){
            if(this.icon==null)
            return "";
        return this.icon;
        }

        /**
        * 设置 显示图标
        */
        public void setIcon(String icon){
        this.icon=icon;
        }
        /**
        * 获取 其他配置
        * @return java.lang.String 其他配置
        */
        public String getConfig(){
            if(this.config==null)
            return "";
        return this.config;
        }

        /**
        * 设置 其他配置
        */
        public void setConfig(String config){
        this.config=config;
        }
        /**
        * 获取 排序
        * @return int 排序
        */
        public int getSort_index(){
        return this.sort_index;
        }

        /**
        * 设置 排序
        */
        public void setSort_index(int sort_index){
        this.sort_index=sort_index;
        }
    }
