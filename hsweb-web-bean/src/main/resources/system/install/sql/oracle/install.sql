-- ----------------------------
-- Table structure for S_CONFIG
-- ----------------------------
CREATE TABLE "S_CONFIG" (
  "U_ID"        VARCHAR2(256) NOT NULL,
  "CONTENT"     CLOB          NOT NULL,
  "REMARK"      VARCHAR2(512) NULL,
  "CREATE_DATE" DATE          NOT NULL,
  "UPDATE_DATE" DATE          NULL
);
COMMENT ON TABLE "S_CONFIG" IS '系统配置文件表';
COMMENT ON COLUMN "S_CONFIG"."U_ID" IS 'UID';
COMMENT ON COLUMN "S_CONFIG"."CONTENT" IS '配置内容';
COMMENT ON COLUMN "S_CONFIG"."REMARK" IS '备注';
COMMENT ON COLUMN "S_CONFIG"."CREATE_DATE" IS '创建日期';
COMMENT ON COLUMN "S_CONFIG"."UPDATE_DATE" IS '修改日期';

-- ----------------------------
-- Table structure for S_FORM
-- ----------------------------
CREATE TABLE "S_FORM" (
  "U_ID"        VARCHAR2(256) NOT NULL,
  "NAME"        VARCHAR2(256) NOT NULL,
  "HTML"        CLOB          NULL,
  "META"        CLOB          NULL,
  "CONFIG"      CLOB          NULL,
  "VERSION"     NUMBER(32)    NULL,
  "USING"       NUMBER(4)     NULL,
  "CREATE_DATE" DATE          NOT NULL,
  "UPDATE_DATE" DATE          NULL,
  "REMARK"      VARCHAR2(200) NULL
);
COMMENT ON COLUMN "S_FORM"."U_ID" IS 'UID';
COMMENT ON COLUMN "S_FORM"."NAME" IS '名称';
COMMENT ON COLUMN "S_FORM"."HTML" IS 'HTML内容';
COMMENT ON COLUMN "S_FORM"."META" IS '结构定义';
COMMENT ON COLUMN "S_FORM"."CONFIG" IS '配置';
COMMENT ON COLUMN "S_FORM"."VERSION" IS '版本';
COMMENT ON COLUMN "S_FORM"."USING" IS '是否使用中';
COMMENT ON COLUMN "S_FORM"."CREATE_DATE" IS '创建日期';
COMMENT ON COLUMN "S_FORM"."UPDATE_DATE" IS '修改日期';

-- ----------------------------
-- Table structure for S_LOGGER
-- ----------------------------
CREATE TABLE "S_LOGGER" (
  "U_ID"             VARCHAR2(256)  NOT NULL,
  "CLIENT_IP"        VARCHAR2(256)  NULL,
  "REQUEST_URI"      VARCHAR2(1024) NOT NULL,
  "REQUEST_URL"      VARCHAR2(2048) NOT NULL,
  "REQUEST_METHOD"   VARCHAR2(512)  NOT NULL,
  "RESPONSE_CONTENT" CLOB           NOT NULL,
  "USER_ID"          VARCHAR2(64)   NOT NULL,
  "REQUEST_TIME"     DATE           NULL,
  "RESPONSE_TIME"    DATE           NULL,
  "USER_AGENT"       CLOB           NULL,
  "REFERER"          VARCHAR2(64)   NOT NULL,
  "RESPONSE_CODE"    CLOB           NULL,
  "REQUEST_HEADER"   CLOB           NULL,
  "CLASS_NAME"       VARCHAR2(512)  NULL,
  "MODULE_DESC"      VARCHAR2(256)  NULL,
  "REQUEST_PARAM"    CLOB           NULL,
  "EXCEPTION_INFO"   CLOB           NULL,
  "CACHE_KEY"        CLOB           NULL,
  "SERVER_IP"        VARCHAR2(64)   NULL,
  "APP_NAME"         VARCHAR2(128)  NULL,
  "USE_TIME"         NUMBER(32)     NULL
);
COMMENT ON TABLE "S_LOGGER" IS '日志表';
COMMENT ON COLUMN "S_LOGGER"."U_ID" IS 'UID';
COMMENT ON COLUMN "S_LOGGER"."CLIENT_IP" IS '客户端IP';
COMMENT ON COLUMN "S_LOGGER"."REQUEST_URI" IS 'URI';
COMMENT ON COLUMN "S_LOGGER"."REQUEST_URL" IS 'URL';
COMMENT ON COLUMN "S_LOGGER"."REQUEST_METHOD" IS '请求方法';
COMMENT ON COLUMN "S_LOGGER"."RESPONSE_CONTENT" IS '相应内容';
COMMENT ON COLUMN "S_LOGGER"."USER_ID" IS '操作用户';
COMMENT ON COLUMN "S_LOGGER"."REQUEST_TIME" IS '请求时间';
COMMENT ON COLUMN "S_LOGGER"."RESPONSE_TIME" IS '响应时间';
COMMENT ON COLUMN "S_LOGGER"."USER_AGENT" IS '用户标识';
COMMENT ON COLUMN "S_LOGGER"."REFERER" IS 'referer';
COMMENT ON COLUMN "S_LOGGER"."RESPONSE_CODE" IS '响应码';
COMMENT ON COLUMN "S_LOGGER"."REQUEST_HEADER" IS '请求头';
COMMENT ON COLUMN "S_LOGGER"."CLASS_NAME" IS '对于class名称';
COMMENT ON COLUMN "S_LOGGER"."MODULE_DESC" IS '描述';
COMMENT ON COLUMN "S_LOGGER"."REQUEST_PARAM" IS '请求参数';
COMMENT ON COLUMN "S_LOGGER"."EXCEPTION_INFO" IS '异常';
COMMENT ON COLUMN "S_LOGGER"."CACHE_KEY" IS '缓存';
COMMENT ON COLUMN "S_LOGGER"."SERVER_IP" IS '服务器ID';
COMMENT ON COLUMN "S_LOGGER"."APP_NAME" IS '应用名称';
COMMENT ON COLUMN "S_LOGGER"."USE_TIME" IS '请求耗时';

-- ----------------------------
-- Records of S_LOGGER
-- ----------------------------

-- ----------------------------
-- Table structure for S_MODULES
-- ----------------------------
CREATE TABLE "S_MODULES" (
  "U_ID"       VARCHAR2(256)  NOT NULL,
  "NAME"       VARCHAR2(256)  NOT NULL,
  "URI"        VARCHAR2(1024) NULL,
  "ICON"       VARCHAR2(256)  NULL,
  "P_ID"       VARCHAR2(256)  NOT NULL,
  "REMARK"     VARCHAR2(512)  NULL,
  "STATUS"     NUMBER(4)      NULL,
  "M_OPTION"   CLOB           NOT NULL,
  "SORT_INDEX" NUMBER(32)     NOT NULL
);
COMMENT ON TABLE "S_MODULES" IS '系统模块';
COMMENT ON COLUMN "S_MODULES"."U_ID" IS 'UID';
COMMENT ON COLUMN "S_MODULES"."NAME" IS '模块名称';
COMMENT ON COLUMN "S_MODULES"."URI" IS 'URI';
COMMENT ON COLUMN "S_MODULES"."ICON" IS '图标';
COMMENT ON COLUMN "S_MODULES"."P_ID" IS '上级菜单';
COMMENT ON COLUMN "S_MODULES"."REMARK" IS '备注';
COMMENT ON COLUMN "S_MODULES"."STATUS" IS '状态';
COMMENT ON COLUMN "S_MODULES"."M_OPTION" IS '可选权限';
COMMENT ON COLUMN "S_MODULES"."SORT_INDEX" IS '排序';

-- ----------------------------
-- Records of S_MODULES
-- ----------------------------
INSERT INTO "S_MODULES" VALUES ('sys', '系统设置', NULL, 'fa fa-cog', '-1', '系统权限', '101', '[]', '1');
INSERT INTO "S_MODULES" VALUES ('form', '表单管理', 'admin/form/list.html', 'fa fa-wpforms', 'sys', NULL, '1', '[{"id":"M","text":"菜单可见","checked":true},{"id":"C","text":"新增","checked":false},{"id":"R","text":"查询","checked":false},{"id":"U","text":"修改","checked":false},{"id":"D","text":"删除","checked":false},{"id":"deploy","text":"发布","checked":false}]', '10101');
INSERT INTO "S_MODULES" VALUES ('module', '权限管理', 'admin/module/list.html', 'fa fa-list-alt', 'sys', NULL, '1', '[{"id":"M","text":"菜单可见","checked":true},{"id":"C","text":"新增","checked":false},{"id":"R","text":"查询","checked":false},{"id":"U","text":"修改","checked":false},{"id":"D","text":"删除","checked":false}]', '10102');
INSERT INTO "S_MODULES" VALUES ('role', '角色管理', 'admin/role/list.html', 'fa fa-users', 'sys', '初始数据', '1', '[{"id":"M", "text":"菜单可见", "uri":""},{"id":"C", "text":"新增", "uri":""},{"id":"R", "text":"查询", "uri":""},{"id":"U", "text":"修改", "uri":""},{"id":"D", "text":"删除", "uri":""}]', '10103');
INSERT INTO "S_MODULES" VALUES ('user', '用户管理', 'admin/user/list.html', 'fa fa-user', 'sys', '初始数据', '1', '[{"id":"M", "text":"菜单可见", "uri":""},{"id":"C", "text":"新增", "uri":""},{"id":"R", "text":"查询", "uri":""},{"id":"U", "text":"修改", "uri":""},{"id":"D", "text":"删除", "uri":""}]', '10104');
INSERT INTO "S_MODULES" VALUES ('s_logger', '日志管理', 'admin/logger/list.html', 'fa fa-book', 'sys', NULL, '1', '[{"id":"M","text":"菜单可见","checked":true},{"id":"R","text":"查询","checked":false}]', '10105');

-- ----------------------------
-- Table structure for S_RESOURCES
-- ----------------------------
CREATE TABLE "S_RESOURCES" (
  "U_ID"        VARCHAR2(256)  NOT NULL,
  "NAME"        VARCHAR2(256)  NOT NULL,
  "PATH"        VARCHAR2(1024) NOT NULL,
  "TYPE"        VARCHAR2(256)  NOT NULL,
  "MD5"         VARCHAR2(256)  NOT NULL,
  "STATUS"      NUMBER(4)      NULL,
  "CREATE_DATE" DATE           NOT NULL,
  "CREATOR_ID"  VARCHAR2(256)  NOT NULL
);
COMMENT ON TABLE "S_RESOURCES" IS '资源表';
COMMENT ON COLUMN "S_RESOURCES"."U_ID" IS 'UID';
COMMENT ON COLUMN "S_RESOURCES"."NAME" IS '资源名称';
COMMENT ON COLUMN "S_RESOURCES"."PATH" IS '路径';
COMMENT ON COLUMN "S_RESOURCES"."TYPE" IS '类型';
COMMENT ON COLUMN "S_RESOURCES"."MD5" IS 'MD5校验值';
COMMENT ON COLUMN "S_RESOURCES"."STATUS" IS '状态';
COMMENT ON COLUMN "S_RESOURCES"."CREATE_DATE" IS '创建时间';
COMMENT ON COLUMN "S_RESOURCES"."CREATOR_ID" IS '创建人';

-- ----------------------------
-- Table structure for S_ROLE
-- ----------------------------
CREATE TABLE "S_ROLE" (
  "U_ID"   VARCHAR2(256) NOT NULL,
  "NAME"   VARCHAR2(256) NOT NULL,
  "TYPE"   VARCHAR2(50)  NULL,
  "REMARK" VARCHAR2(512) NULL
);
COMMENT ON TABLE "S_ROLE" IS '角色表';
COMMENT ON COLUMN "S_ROLE"."U_ID" IS 'UID';
COMMENT ON COLUMN "S_ROLE"."NAME" IS '角色名称';
COMMENT ON COLUMN "S_ROLE"."TYPE" IS '类型';
COMMENT ON COLUMN "S_ROLE"."REMARK" IS '备注';

-- ----------------------------
-- Records of S_ROLE
-- ----------------------------
INSERT INTO "S_ROLE" VALUES ('admin', '超级管理员', NULL, '初始数据');
INSERT INTO "S_ROLE" VALUES ('ent', '企业用户', NULL, '企业端用户');
INSERT INTO "S_ROLE" VALUES ('ju', '局端用户', NULL, NULL);

-- ----------------------------
-- Table structure for S_ROLE_MODULES
-- ----------------------------
CREATE TABLE "S_ROLE_MODULES" (
  "U_ID"      VARCHAR2(256) NOT NULL,
  "MODULE_ID" VARCHAR2(256) NOT NULL,
  "ROLE_ID"   VARCHAR2(256) NOT NULL,
  "O_LEVEL"   CLOB          NULL
);
COMMENT ON TABLE "S_ROLE_MODULES" IS '角色模块绑定表';
COMMENT ON COLUMN "S_ROLE_MODULES"."U_ID" IS 'UID';
COMMENT ON COLUMN "S_ROLE_MODULES"."MODULE_ID" IS '模块ID';
COMMENT ON COLUMN "S_ROLE_MODULES"."ROLE_ID" IS '角色ID';
COMMENT ON COLUMN "S_ROLE_MODULES"."O_LEVEL" IS '可操作权限';

-- ----------------------------
-- Table structure for S_SCRIPT
-- ----------------------------
CREATE TABLE "S_SCRIPT" (
  "U_ID"    VARCHAR2(256)  NOT NULL,
  "NAME"    VARCHAR2(256)  NOT NULL,
  "PATH"    VARCHAR2(1024) NOT NULL,
  "TYPE"    VARCHAR2(256)  NOT NULL,
  "CONTENT" CLOB           NOT NULL,
  "REMARK"  VARCHAR2(512)  NULL,
  "STATUS"  NUMBER(4)      NULL
);
COMMENT ON TABLE "S_SCRIPT" IS '脚本';
COMMENT ON COLUMN "S_SCRIPT"."U_ID" IS 'UID';
COMMENT ON COLUMN "S_SCRIPT"."NAME" IS '脚本名称';
COMMENT ON COLUMN "S_SCRIPT"."PATH" IS '路径';
COMMENT ON COLUMN "S_SCRIPT"."TYPE" IS '类型';
COMMENT ON COLUMN "S_SCRIPT"."CONTENT" IS '内容';
COMMENT ON COLUMN "S_SCRIPT"."REMARK" IS '备注';
COMMENT ON COLUMN "S_SCRIPT"."STATUS" IS '状态';

-- ----------------------------
-- Records of S_TEST_2
-- ----------------------------

-- ----------------------------
-- Table structure for S_USER
-- ----------------------------
CREATE TABLE "S_USER" (
  "U_ID"        VARCHAR2(64)  NOT NULL,
  "USERNAME"    VARCHAR2(64)  NOT NULL,
  "PASSWORD"    VARCHAR2(64)  NOT NULL,
  "NAME"        VARCHAR2(64)  NULL,
  "EMAIL"       VARCHAR2(512) NULL,
  "PHONE"       VARCHAR2(64)  NULL,
  "STATUS"      NUMBER(4)     NULL,
  "CREATE_DATE" DATE          NOT NULL,
  "UPDATE_DATE" DATE          NULL,
  "ENT_ID"      VARCHAR2(64)  NULL
);
COMMENT ON TABLE "S_USER" IS '用户表';
COMMENT ON COLUMN "S_USER"."U_ID" IS 'ID';
COMMENT ON COLUMN "S_USER"."USERNAME" IS '用户名';
COMMENT ON COLUMN "S_USER"."PASSWORD" IS '密码';
COMMENT ON COLUMN "S_USER"."NAME" IS '姓名';
COMMENT ON COLUMN "S_USER"."EMAIL" IS '邮箱';
COMMENT ON COLUMN "S_USER"."PHONE" IS '联系电话';
COMMENT ON COLUMN "S_USER"."STATUS" IS '状态';
COMMENT ON COLUMN "S_USER"."CREATE_DATE" IS '创建日期';
COMMENT ON COLUMN "S_USER"."UPDATE_DATE" IS '修改日期';
COMMENT ON COLUMN "S_USER"."ENT_ID" IS '企业ID';

-- ----------------------------
-- Records of S_USER
-- ----------------------------
INSERT INTO "S_USER" VALUES ('admin', 'admin', '23ec59e119da971084cbd0ba72d230a0', '超级管理员', NULL, NULL, '0', TO_DATE('2015-11-19 12:10:36', 'YYYY-MM-DD HH24:MI:SS'), NULL, NULL);
-- ----------------------------
-- Table structure for S_USER_ROLE
-- ----------------------------
CREATE TABLE "S_USER_ROLE" (
  "U_ID"    VARCHAR2(256) NOT NULL,
  "USER_ID" VARCHAR2(256) NOT NULL,
  "ROLE_ID" VARCHAR2(256) NOT NULL
);
-- CREATE TABLE
CREATE TABLE "S_HISTORY"
(
  "U_ID"              VARCHAR2(32) NOT NULL,
  "TYPE"              VARCHAR2(64) NOT NULL,
  "DESCRIBE"          VARCHAR2(512),
  "PRIMARY_KEY_NAME"  VARCHAR2(32),
  "PRIMARY_KEY_VALUE" VARCHAR2(64),
  "CHANGE_BEFORE"     CLOB,
  "CHANGE_AFTER"      CLOB,
  "CREATE_DATE"       DATE         NOT NULL,
  "CREATOR_ID"        VARCHAR2(32)
);

COMMENT ON COLUMN "S_USER_ROLE"."U_ID" IS 'UID';
COMMENT ON COLUMN "S_USER_ROLE"."USER_ID" IS '用户ID';
COMMENT ON COLUMN "S_USER_ROLE"."ROLE_ID" IS '角色ID';


ALTER TABLE "S_CONFIG" ADD PRIMARY KEY ("U_ID");
ALTER TABLE "S_LOGGER" ADD PRIMARY KEY ("U_ID");
ALTER TABLE "S_MODULES" ADD PRIMARY KEY ("U_ID");
ALTER TABLE "S_RESOURCES" ADD PRIMARY KEY ("U_ID");
ALTER TABLE "S_ROLE" ADD PRIMARY KEY ("U_ID");
ALTER TABLE "S_ROLE_MODULES" ADD PRIMARY KEY ("U_ID");
ALTER TABLE "S_SCRIPT" ADD PRIMARY KEY ("U_ID");
ALTER TABLE "S_TEST" ADD PRIMARY KEY ("U_ID");
ALTER TABLE "S_USER" ADD PRIMARY KEY ("U_ID");
ALTER TABLE "S_USER_ROLE" ADD PRIMARY KEY ("U_ID");