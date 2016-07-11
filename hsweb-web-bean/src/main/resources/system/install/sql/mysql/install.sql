CREATE TABLE `s_user_profile` (
  `u_id`    VARCHAR(32) NOT NULL PRIMARY KEY
  COMMENT 'UID',
  `content` TEXT        NOT NULL
  COMMENT '配置内容',
  `type`    VARCHAR(512)
  COMMENT '类型',
  `user_id` VARCHAR(32)
  COMMENT '用户ID'
);

CREATE TABLE `s_config` (
  `u_id`          VARCHAR(32) NOT NULL PRIMARY KEY
  COMMENT 'UID',
  `content`       TEXT        NOT NULL
  COMMENT '配置内容',
  `remark`        VARCHAR(512)
  COMMENT '备注',
  `classified_id` VARCHAR(32)
  COMMENT '分类ID',
  `create_date`   DATETIME    NOT NULL
  COMMENT '创建日期',
  `update_date`   DATETIME
  COMMENT '修改日期'
);
ALTER TABLE `s_config` COMMENT '系统配置文件表';
CREATE TABLE `s_form` (
  `u_id`          VARCHAR(32)  NOT NULL PRIMARY KEY
  COMMENT 'uid',
  `classified_id` VARCHAR(32) COMMENT '分类ID',
  `name`          VARCHAR(256) NOT NULL
  COMMENT '名称',
  `html`          TEXT COMMENT 'html内容',
  `meta`          TEXT COMMENT '结构定义',
  `config`        TEXT COMMENT '配置',
  `version`       INT COMMENT '版本',
  `revision`      INT COMMENT '修订版',
  `release`       INT COMMENT '发布版',
  `using`         TINYINT COMMENT '是否使用中',
  `create_date`   DATETIME     NOT NULL
  COMMENT '创建日期',
  `update_date`   DATETIME COMMENT '修改日期',
  `remark`        VARCHAR(200)
);
ALTER TABLE `s_form` COMMENT '动态表单';
CREATE TABLE `s_template` (
  `u_id`          VARCHAR(32)  NOT NULL PRIMARY KEY
  COMMENT 'uid',
  `name`          VARCHAR(256) NOT NULL
  COMMENT '名称',
  `template`      TEXT COMMENT '模板内容',
  `classified_id` VARCHAR(32) COMMENT '分类',
  `type`          VARCHAR(64) COMMENT '类型',
  `script`        TEXT COMMENT '脚本',
  `script_links`  TEXT COMMENT '外部脚本',
  `css`           TEXT COMMENT '样式',
  `css_links`     TEXT COMMENT '外部样式',
  `version`       INT COMMENT '版本',
  `revision`      INT COMMENT '修订版',
  `release`       INT COMMENT '发布版',
  `using`         TINYINT COMMENT '是否使用中',
  `remark`        VARCHAR(200)
);
ALTER TABLE `s_template` COMMENT '模板';
CREATE TABLE `s_modules` (
  `u_id`       VARCHAR(32)   NOT NULL PRIMARY KEY
  COMMENT 'uid',
  `name`       VARCHAR(256)  NOT NULL
  COMMENT '模块名称',
  `uri`        VARCHAR(1024) NULL
  COMMENT 'uri',
  `icon`       VARCHAR(256)  NULL
  COMMENT '图标',
  `parent_id`  VARCHAR(256)  NOT NULL
  COMMENT '上级菜单',
  `remark`     VARCHAR(512)  NULL
  COMMENT '备注',
  `status`     INT(4)        NULL
  COMMENT '状态',
  `optional`   TEXT          NOT NULL
  COMMENT '可选权限',
  `sort_index` INT(32)       NOT NULL
  COMMENT '排序'
);
ALTER TABLE `s_modules` COMMENT '系统模块';
CREATE TABLE `s_module_meta` (
  `u_id`      VARCHAR(32)   NOT NULL PRIMARY KEY
  COMMENT 'uid',
  `key`       VARCHAR(256)  NOT NULL
  COMMENT '标识',
  `module_id` VARCHAR(1024) NOT NULL
  COMMENT '模块ID',
  `remark`    VARCHAR(1024) NULL
  COMMENT '备注',
  `role_id`   VARCHAR(256)  NULL
  COMMENT '图标',
  `status`    INT(4)        NULL
  COMMENT '状态',
  `meta`      TEXT          NULL
  COMMENT '定义内容'
);
ALTER TABLE `s_modules` COMMENT '系统模块配置';
CREATE TABLE `s_resources` (
  `u_id`        VARCHAR(32)   NOT NULL PRIMARY KEY
  COMMENT 'uid',
  `name`        VARCHAR(256)  NOT NULL
  COMMENT '资源名称',
  `path`        VARCHAR(1024) NOT NULL
  COMMENT '路径',
  `type`        VARCHAR(256)  NOT NULL
  COMMENT '类型',
  `md5`         VARCHAR(256)  NOT NULL
  COMMENT 'md5校验值',
  `status`      INT(4)        NULL
  COMMENT '状态',
  `create_date` DATETIME      NOT NULL
  COMMENT '创建时间',
  `creator_id`  VARCHAR(256)  NOT NULL
  COMMENT '创建人'
);
ALTER TABLE `s_resources` COMMENT '资源表';
CREATE TABLE `s_classified` (
  `u_id`       VARCHAR(32)   NOT NULL PRIMARY KEY
  COMMENT 'uid',
  `remark`     VARCHAR(1024) NOT NULL
  COMMENT '备注',
  `type`       VARCHAR(256)  NOT NULL
  COMMENT '类型',
  `parent_id`  VARCHAR(32)   NOT NULL
  COMMENT '父级分类',
  `icon`       VARCHAR(256)  NULL
  COMMENT '状态',
  `config`     TEXT          NOT NULL
  COMMENT '创建时间',
  `sort_index` INT           NOT NULL
  COMMENT '排序'
);
ALTER TABLE `s_resources` COMMENT '资源表';
CREATE TABLE `s_role` (
  `u_id`   VARCHAR(32)  NOT NULL PRIMARY KEY
  COMMENT 'uid',
  `name`   VARCHAR(256) NOT NULL
  COMMENT '角色名称',
  `type`   VARCHAR(50)  NULL
  COMMENT '类型',
  `remark` VARCHAR(512) NULL
  COMMENT '备注'
);
ALTER TABLE `s_role` COMMENT '角色表';
CREATE TABLE `s_role_modules` (
  `u_id`      VARCHAR(32)  NOT NULL PRIMARY KEY
  COMMENT 'uid',
  `module_id` VARCHAR(256) NOT NULL
  COMMENT '模块id',
  `role_id`   VARCHAR(256) NOT NULL
  COMMENT '角色id',
  `actions`   TEXT         NULL
  COMMENT '可操作权限'
);
ALTER TABLE `s_role_modules` COMMENT '角色模块绑定表';
CREATE TABLE `s_script` (
  `u_id`          VARCHAR(32)   NOT NULL PRIMARY KEY
  COMMENT 'uid',
  `name`          VARCHAR(256)  NOT NULL
  COMMENT '脚本名称',
  `classified_id` VARCHAR(1024) NOT NULL
  COMMENT '路径',
  `type`          VARCHAR(256)  NOT NULL
  COMMENT '类型',
  `content`       TEXT          NOT NULL
  COMMENT '内容',
  `remark`        VARCHAR(512)  NULL
  COMMENT '备注',
  `status`        INT(4)        NULL
  COMMENT '状态'
);
ALTER TABLE `s_script` COMMENT '脚本';
CREATE TABLE `s_user` (
  `u_id`        VARCHAR(32)  NOT NULL PRIMARY KEY
  COMMENT 'ID',
  `username`    VARCHAR(64)  NOT NULL
  COMMENT '用户名',
  `password`    VARCHAR(64)  NOT NULL
  COMMENT '密码',
  `name`        VARCHAR(64)  NULL
  COMMENT '姓名',
  `email`       VARCHAR(512) NULL
  COMMENT '邮箱',
  `phone`       VARCHAR(64)  NULL
  COMMENT '联系电话',
  `status`      INT(4)       NULL
  COMMENT '状态',
  `create_date` DATETIME     NOT NULL
  COMMENT '创建日期',
  `update_date` DATETIME     NULL
  COMMENT '修改日期'
);
ALTER TABLE `s_user` COMMENT '用户表';
CREATE TABLE `s_user_role` (
  `u_id`    VARCHAR(32)  NOT NULL PRIMARY KEY
  COMMENT 'UID',
  `user_id` VARCHAR(256) NOT NULL
  COMMENT '用户ID',
  `role_id` VARCHAR(256) NOT NULL
  COMMENT '角色ID'
);
ALTER TABLE `s_user_role` COMMENT '用户角色关联表';
CREATE TABLE s_history
(
  `u_id`              VARCHAR(32) NOT NULL
  COMMENT 'UID',
  `type`              VARCHAR(64) NOT NULL
  COMMENT '类型',
  `describe`          VARCHAR(512) COMMENT '描述',
  `primary_key_name`  VARCHAR(32) COMMENT '主键名称',
  `primary_key_value` VARCHAR(64) COMMENT '主键值',
  `change_before`     TEXT COMMENT '修改前的值',
  `change_after`      TEXT COMMENT '修改后的值',
  `create_date`       DATETIME    NOT NULL
  COMMENT '创建日期',
  `creator_id`        VARCHAR(32) COMMENT '创建人'
);
ALTER TABLE `s_history` COMMENT '操作记录表';
