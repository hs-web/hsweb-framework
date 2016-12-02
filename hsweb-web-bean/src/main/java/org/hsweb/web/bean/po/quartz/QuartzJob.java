/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.bean.po.quartz;

import org.hsweb.web.bean.po.GenericPo;

/**
 * 定时调度任务
 * Created by hsweb-generator Sep 27, 2016 1:55:18 AM
 */
public class QuartzJob extends GenericPo<String> {
    //任务名称
    private String name;
    //备注
    private String remark;
    //cron表达式
    private String cron;
    //执行脚本
    private String script = "groovy";
    //脚本语言
    private String  language;
    //是否启用
    private boolean enabled;
    //启动参数
    private String  parameters;
    //任务类型
    private byte    type;

    /**
     * 获取 任务名称
     *
     * @return String 任务名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置 任务名称
     *
     * @param name 任务名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取 备注
     *
     * @return String 备注
     */
    public String getRemark() {
        return this.remark;
    }

    /**
     * 设置 备注
     *
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取 cron表达式
     *
     * @return String cron表达式
     */
    public String getCron() {
        return this.cron;
    }

    /**
     * 设置 cron表达式
     *
     * @param cron cron表达式
     */
    public void setCron(String cron) {
        this.cron = cron;
    }

    /**
     * 获取 执行脚本
     *
     * @return String 执行脚本
     */
    public String getScript() {
        return this.script;
    }

    /**
     * 设置 执行脚本
     *
     * @param script 执行脚本
     */
    public void setScript(String script) {
        this.script = script;
    }

    /**
     * 获取 脚本语言
     *
     * @return String 脚本语言
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * 设置 脚本语言
     *
     * @param language 脚本语言
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * 获取 是否启用
     *
     * @return long 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置 是否启用
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取 启动参数
     *
     * @return String 启动参数
     */
    public String getParameters() {
        return this.parameters;
    }

    /**
     * 设置 启动参数
     *
     * @param parameters 启动参数
     */
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    /**
     * 获取 任务类型
     *
     * @return long 任务类型
     */
    public byte getType() {
        return this.type;
    }

    /**
     * 设置 任务类型
     *
     * @param type 任务类型
     */
    public void setType(byte type) {
        this.type = type;
    }


    public interface Property extends GenericPo.Property {
        /**
         * @see QuartzJob#name
         */
        String name       = "name";
        /**
         * @see QuartzJob#remark
         */
        String remark     = "remark";
        /**
         * @see QuartzJob#cron
         */
        String cron       = "cron";
        /**
         * @see QuartzJob#script
         */
        String script     = "script";
        /**
         * @see QuartzJob#language
         */
        String language   = "language";
        /**
         * @see QuartzJob#enabled
         */
        String enabled    = "enabled";
        /**
         * @see QuartzJob#parameters
         */
        String parameters = "parameters";
        /**
         * @see QuartzJob#type
         */
        String type       = "type";
    }
}