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
ALTER TABLE `s_config`
  COMMENT '系统配置文件表';
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
ALTER TABLE `s_form`
  COMMENT '动态表单';
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
ALTER TABLE `s_template`
  COMMENT '模板';
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
ALTER TABLE `s_modules`
  COMMENT '系统模块';
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
ALTER TABLE `s_modules`
  COMMENT '系统模块配置';
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
  `size`        LONG          NULL
  COMMENT '资源大小',
  `create_date` DATETIME      NOT NULL
  COMMENT '创建时间',
  `creator_id`  VARCHAR(256)  NOT NULL
  COMMENT '创建人'
);
ALTER TABLE `s_resources`
  COMMENT '资源表';
CREATE TABLE `s_classified` (
  `u_id`       VARCHAR(32)   NOT NULL PRIMARY KEY
  COMMENT 'uid',
  `name`       VARCHAR(256)  NOT NULL
  COMMENT '名称',
  `remark`     VARCHAR(1024) NULL
  COMMENT '备注',
  `type`       VARCHAR(256)  NULL
  COMMENT '类型',
  `parent_id`  VARCHAR(32)   NOT NULL
  COMMENT '父级分类',
  `icon`       VARCHAR(256)  NULL
  COMMENT '图标',
  `config`     TEXT          NULL
  COMMENT '配置',
  `sort_index` INT           NULL
  COMMENT '排序'
);
ALTER TABLE `s_classified`
  COMMENT '分类表';
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
ALTER TABLE `s_role`
  COMMENT '角色表';
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
ALTER TABLE `s_role_modules`
  COMMENT '角色模块绑定表';
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
ALTER TABLE `s_script`
  COMMENT '脚本';
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
ALTER TABLE `s_user`
  COMMENT '用户表';
CREATE TABLE `s_user_role` (
  `u_id`    VARCHAR(32)  NOT NULL PRIMARY KEY
  COMMENT 'UID',
  `user_id` VARCHAR(256) NOT NULL
  COMMENT '用户ID',
  `role_id` VARCHAR(256) NOT NULL
  COMMENT '角色ID'
);
ALTER TABLE `s_user_role`
  COMMENT '用户角色关联表';
CREATE TABLE s_history
(
  `u_id`              VARCHAR(32) NOT NULL PRIMARY KEY
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
ALTER TABLE `s_history`
  COMMENT '操作记录表';

CREATE TABLE s_query_plan
(
  `u_id`        VARCHAR(32)  NOT NULL PRIMARY KEY
  COMMENT 'UID',
  `name`        VARCHAR(256) NOT NULL
  COMMENT '名称',
  `type`        VARCHAR(256) NOT NULL
  COMMENT '类型',
  `CONFIG`      TEXT COMMENT '方案配置内容',
  `SHARING`     TINYINT COMMENT '是否共享',
  `CREATOR_ID`  VARCHAR(32)  NOT NULL
  COMMENT '创建人ID',
  `CREATE_DATE` DATETIME     NOT NULL
  COMMENT '创建日期'
);
ALTER TABLE `s_query_plan`
  COMMENT '查询方案';

CREATE TABLE s_data_source
(
  u_id        VARCHAR(32) PRIMARY KEY NOT NULL
  COMMENT 'ID',
  name        VARCHAR(64)             NOT NULL
  COMMENT '名称',
  driver      VARCHAR(128)            NOT NULL
  COMMENT 'driver',
  url         VARCHAR(512)            NOT NULL
  COMMENT 'url',
  username    VARCHAR(128)            NOT NULL
  COMMENT '用户名',
  password    VARCHAR(128)            NOT NULL
  COMMENT '密码',
  enabled     TINYINT                 NOT NULL
  COMMENT '是否启用',
  create_date DATETIME                NOT NULL
  COMMENT '创建日期',
  properties  TEXT COMMENT '其他配置',
  comment     VARCHAR(512) COMMENT '备注',
  test_sql    VARCHAR(512) COMMENT '测试sql'
);
ALTER TABLE s_data_source
  COMMENT '数据源';


CREATE TABLE S_QUARTZ_JOB_HIS (
  u_id       VARCHAR(32) NOT NULL  PRIMARY KEY
  COMMENT '主键',
  job_id     VARCHAR(32) NOT NULL
  COMMENT '任务ID',
  start_time DATETIME    NOT NULL
  COMMENT '开始时间',
  end_time   DATETIME COMMENT '结束时间',
  result     TEXT COMMENT '执行结果',
  status     TINYINT COMMENT '状态'
);

CREATE TABLE S_QUARTZ_JOB (
  u_id       VARCHAR(32)  NOT NULL PRIMARY KEY
  COMMENT '主键',
  name       VARCHAR(128) NOT NULL
  COMMENT '主键',
  remark     VARCHAR(512) COMMENT '主键',
  cron       VARCHAR(512) NOT NULL
  COMMENT '主键',
  script     TEXT         NOT NULL
  COMMENT '主键',
  language   VARCHAR(32)  NOT NULL
  COMMENT '主键',
  enabled    TINYINT COMMENT '主键',
  parameters TEXT COMMENT '主键',
  type       TINYINT
);


CREATE TABLE QRTZ_JOB_DETAILS
(
  SCHED_NAME        VARCHAR(120) NOT NULL,
  JOB_NAME          VARCHAR(200) NOT NULL,
  JOB_GROUP         VARCHAR(200) NOT NULL,
  DESCRIPTION       VARCHAR(250) NULL,
  JOB_CLASS_NAME    VARCHAR(250) NOT NULL,
  IS_DURABLE        VARCHAR(1)   NOT NULL,
  IS_NONCONCURRENT  VARCHAR(1)   NOT NULL,
  IS_UPDATE_DATA    VARCHAR(1)   NOT NULL,
  REQUESTS_RECOVERY VARCHAR(1)   NOT NULL,
  JOB_DATA          BLOB         NULL,
  PRIMARY KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
);

CREATE TABLE QRTZ_TRIGGERS
(
  SCHED_NAME     VARCHAR(120) NOT NULL,
  TRIGGER_NAME   VARCHAR(200) NOT NULL,
  TRIGGER_GROUP  VARCHAR(200) NOT NULL,
  JOB_NAME       VARCHAR(200) NOT NULL,
  JOB_GROUP      VARCHAR(200) NOT NULL,
  DESCRIPTION    VARCHAR(250) NULL,
  NEXT_FIRE_TIME BIGINT(13)   NULL,
  PREV_FIRE_TIME BIGINT(13)   NULL,
  PRIORITY       INTEGER      NULL,
  TRIGGER_STATE  VARCHAR(16)  NOT NULL,
  TRIGGER_TYPE   VARCHAR(8)   NOT NULL,
  START_TIME     BIGINT(13)   NOT NULL,
  END_TIME       BIGINT(13)   NULL,
  CALENDAR_NAME  VARCHAR(200) NULL,
  MISFIRE_INSTR  SMALLINT(2)  NULL,
  JOB_DATA       BLOB         NULL,
  PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
  REFERENCES QRTZ_JOB_DETAILS (SCHED_NAME, JOB_NAME, JOB_GROUP)
);

CREATE TABLE QRTZ_SIMPLE_TRIGGERS
(
  SCHED_NAME      VARCHAR(120) NOT NULL,
  TRIGGER_NAME    VARCHAR(200) NOT NULL,
  TRIGGER_GROUP   VARCHAR(200) NOT NULL,
  REPEAT_COUNT    BIGINT(7)    NOT NULL,
  REPEAT_INTERVAL BIGINT(12)   NOT NULL,
  TIMES_TRIGGERED BIGINT(10)   NOT NULL,
  PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
  REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

CREATE TABLE QRTZ_CRON_TRIGGERS
(
  SCHED_NAME      VARCHAR(120) NOT NULL,
  TRIGGER_NAME    VARCHAR(200) NOT NULL,
  TRIGGER_GROUP   VARCHAR(200) NOT NULL,
  CRON_EXPRESSION VARCHAR(200) NOT NULL,
  TIME_ZONE_ID    VARCHAR(80),
  PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
  REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

CREATE TABLE QRTZ_SIMPROP_TRIGGERS
(
  SCHED_NAME    VARCHAR(120)   NOT NULL,
  TRIGGER_NAME  VARCHAR(200)   NOT NULL,
  TRIGGER_GROUP VARCHAR(200)   NOT NULL,
  STR_PROP_1    VARCHAR(512)   NULL,
  STR_PROP_2    VARCHAR(512)   NULL,
  STR_PROP_3    VARCHAR(512)   NULL,
  INT_PROP_1    INT            NULL,
  INT_PROP_2    INT            NULL,
  LONG_PROP_1   BIGINT         NULL,
  LONG_PROP_2   BIGINT         NULL,
  DEC_PROP_1    NUMERIC(13, 4) NULL,
  DEC_PROP_2    NUMERIC(13, 4) NULL,
  BOOL_PROP_1   VARCHAR(1)     NULL,
  BOOL_PROP_2   VARCHAR(1)     NULL,
  PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
  REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

CREATE TABLE QRTZ_BLOB_TRIGGERS
(
  SCHED_NAME    VARCHAR(120) NOT NULL,
  TRIGGER_NAME  VARCHAR(200) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  BLOB_DATA     BLOB         NULL,
  PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
  FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
  REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

CREATE TABLE QRTZ_CALENDARS
(
  SCHED_NAME    VARCHAR(120) NOT NULL,
  CALENDAR_NAME VARCHAR(200) NOT NULL,
  CALENDAR      BLOB         NOT NULL,
  PRIMARY KEY (SCHED_NAME, CALENDAR_NAME)
);

CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS
(
  SCHED_NAME    VARCHAR(120) NOT NULL,
  TRIGGER_GROUP VARCHAR(200) NOT NULL,
  PRIMARY KEY (SCHED_NAME, TRIGGER_GROUP)
);

CREATE TABLE QRTZ_FIRED_TRIGGERS
(
  SCHED_NAME        VARCHAR(120) NOT NULL,
  ENTRY_ID          VARCHAR(95)  NOT NULL,
  TRIGGER_NAME      VARCHAR(200) NOT NULL,
  TRIGGER_GROUP     VARCHAR(200) NOT NULL,
  INSTANCE_NAME     VARCHAR(200) NOT NULL,
  FIRED_TIME        BIGINT(13)   NOT NULL,
  SCHED_TIME        BIGINT(13)   NOT NULL,
  PRIORITY          INTEGER      NOT NULL,
  STATE             VARCHAR(16)  NOT NULL,
  JOB_NAME          VARCHAR(200) NULL,
  JOB_GROUP         VARCHAR(200) NULL,
  IS_NONCONCURRENT  VARCHAR(1)   NULL,
  REQUESTS_RECOVERY VARCHAR(1)   NULL,
  PRIMARY KEY (SCHED_NAME, ENTRY_ID)
);

CREATE TABLE QRTZ_SCHEDULER_STATE
(
  SCHED_NAME        VARCHAR(120) NOT NULL,
  INSTANCE_NAME     VARCHAR(200) NOT NULL,
  LAST_CHECKIN_TIME BIGINT(13)   NOT NULL,
  CHECKIN_INTERVAL  BIGINT(13)   NOT NULL,
  PRIMARY KEY (SCHED_NAME, INSTANCE_NAME)
);

CREATE TABLE QRTZ_LOCKS
(
  SCHED_NAME VARCHAR(120) NOT NULL,
  LOCK_NAME  VARCHAR(40)  NOT NULL,
  PRIMARY KEY (SCHED_NAME, LOCK_NAME)
);

