/*
SQLyog Ultimate v11.11 (64 bit)
MySQL - 5.5.27 : Database - wb
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`wb` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `wb`;

/*Table structure for table `area` */

DROP TABLE IF EXISTS `area`;

CREATE TABLE `area` (
  `id` varchar(32) NOT NULL DEFAULT '',
  `name` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `area` */

insert  into `area`(`id`,`name`) values ('1','万州'),('2','江北');

/*Table structure for table `b_publicity` */

DROP TABLE IF EXISTS `b_publicity`;

CREATE TABLE `b_publicity` (
  `u_id` varchar(64) NOT NULL DEFAULT '' COMMENT '主键',
  `creator_id` varchar(256) DEFAULT NULL COMMENT '发布人',
  `create_date` datetime NOT NULL COMMENT '发布时间',
  `update_date` datetime DEFAULT NULL COMMENT '修改时间',
  `title` varchar(512) DEFAULT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `is_urgent` int(11) DEFAULT NULL COMMENT '是否紧急',
  `file_list` text COMMENT '附件列表',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '状态',
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='公告,更新于:2016-01-21 09:52:38。新增字段0,删除字段0,变更字段9';

/*Data for the table `b_publicity` */

insert  into `b_publicity`(`u_id`,`creator_id`,`create_date`,`update_date`,`title`,`content`,`is_urgent`,`file_list`,`status`) values ('HxQf6khi','admin','2015-11-26 23:12:47','2015-12-27 16:17:01','测试22222','<h3>姓名:{{name}}</h3>',1,'[]',1);

/*Table structure for table `b_publicity_reader` */

DROP TABLE IF EXISTS `b_publicity_reader`;

CREATE TABLE `b_publicity_reader` (
  `u_id` varchar(64) NOT NULL DEFAULT '' COMMENT '主键',
  `publicity_id` varchar(256) DEFAULT NULL COMMENT '公告ID',
  `reader_id` varchar(256) DEFAULT NULL COMMENT '阅读人',
  `read_time` datetime NOT NULL COMMENT '阅读时间',
  `download_file` int(11) DEFAULT NULL COMMENT '是否了下载附件',
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='公告阅读情况,更新于:2016-01-21 09:52:39。新增字段0,删除字段0,变更字段5';

/*Data for the table `b_publicity_reader` */

insert  into `b_publicity_reader`(`u_id`,`publicity_id`,`reader_id`,`read_time`,`download_file`) values ('834TAaz3','HxQf6khi','admin','2015-12-01 16:46:45',NULL),('9UAFZkRn','HxQf6khi','admin','2015-12-10 14:48:51',NULL),('bMGc076D','HxQf6khi','admin','2015-12-27 13:41:37',NULL),('jxX6735y','HxQf6khi','WA1Rd5','2015-12-01 17:08:09',NULL),('X75eWLkm','HxQf6khi','admin','2015-12-02 15:22:24',NULL);

/*Table structure for table `s_config` */

DROP TABLE IF EXISTS `s_config`;

CREATE TABLE `s_config` (
  `u_id` varchar(40) NOT NULL DEFAULT '' COMMENT '主键',
  `content` text NOT NULL COMMENT '资源地址',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `create_date` datetime NOT NULL COMMENT '创建日期',
  `update_date` datetime DEFAULT NULL COMMENT '修改日期',
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='配置管理表,更新于:2016-01-21 09:52:40。新增字段0,删除字段0,变更字段5';

/*Data for the table `s_config` */

/*Table structure for table `s_form` */

DROP TABLE IF EXISTS `s_form`;

CREATE TABLE `s_form` (
  `u_id` varchar(40) NOT NULL DEFAULT '' COMMENT '主键',
  `name` varchar(256) NOT NULL COMMENT '表单名称',
  `content` text NOT NULL COMMENT '定义内容',
  `db_name` varchar(50) NOT NULL COMMENT '数据库名称',
  `table_name` varchar(256) NOT NULL COMMENT '数据库表名',
  `foreigns` text COMMENT '关联信息配置',
  `auto_alter` int(11) DEFAULT NULL COMMENT '是否自动维护',
  `status` int(11) NOT NULL COMMENT '状态',
  `create_date` datetime NOT NULL COMMENT '添加日期',
  `update_date` datetime DEFAULT NULL COMMENT '修改日期',
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='自定义表单,更新于:2016-01-21 09:52:40。新增字段0,删除字段0,变更字段10';

/*Data for the table `s_form` */

/*Table structure for table `s_logger` */

DROP TABLE IF EXISTS `s_logger`;

CREATE TABLE `s_logger` (
  `u_id` varchar(40) NOT NULL DEFAULT '' COMMENT '主键',
  `client_ip` varchar(64) NOT NULL COMMENT '请求者ip',
  `request_uri` varchar(64) NOT NULL COMMENT 'uri',
  `request_url` varchar(64) NOT NULL COMMENT 'url',
  `request_method` varchar(64) NOT NULL COMMENT '请求方法',
  `response_content` text NOT NULL COMMENT '响应结果',
  `user_id` varchar(64) NOT NULL COMMENT '操作用户主键',
  `request_time` bigint(20) NOT NULL COMMENT '请求时间',
  `response_time` bigint(20) NOT NULL COMMENT '请求时间',
  `user_agent` text COMMENT '客户端标识',
  `referer` text NOT NULL COMMENT 'referer',
  `response_code` text COMMENT '响应码',
  `request_header` text COMMENT '请求头',
  `class_name` varchar(64) DEFAULT NULL COMMENT '类名',
  `module_desc` varchar(64) DEFAULT NULL COMMENT '功能摘要',
  `request_param` text COMMENT '请求参数',
  `exception_info` text COMMENT '异常',
  `cache_key` text COMMENT '命中缓存',
  `server_name` varchar(256) DEFAULT NULL COMMENT '服务器名',
  `server_ip` varchar(64) DEFAULT NULL COMMENT '服务器ip',
  `app_name` varchar(128) DEFAULT NULL COMMENT '应用名',
  `use_time` int(11) DEFAULT NULL COMMENT '请求耗时',
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统日志表,更新于:2016-01-21 09:52:39。新增字段0,删除字段0,变更字段22';

/*Data for the table `s_logger` */

/*Table structure for table `s_modules` */

DROP TABLE IF EXISTS `s_modules`;

CREATE TABLE `s_modules` (
  `u_id` varchar(40) NOT NULL DEFAULT '' COMMENT '主键',
  `name` varchar(256) NOT NULL COMMENT '资源名称',
  `uri` varchar(1024) DEFAULT NULL COMMENT '模块地址',
  `icon` varchar(256) DEFAULT NULL COMMENT '图标',
  `p_id` varchar(256) NOT NULL COMMENT '上级菜单',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `status` int(11) DEFAULT NULL COMMENT '状态',
  `m_option` text NOT NULL COMMENT '可操作选项',
  `sort_index` int(11) NOT NULL COMMENT '排序',
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统模块,更新于:2016-01-21 09:52:40。新增字段0,删除字段0,变更字段9';

/*Data for the table `s_modules` */

insert  into `s_modules`(`u_id`,`name`,`uri`,`icon`,`p_id`,`remark`,`status`,`m_option`,`sort_index`) values ('b_publicity','公示公告','','','index','初始数据',1,'[{\"id\":\"C\",\"text\":\"新增\",\"checked\":true},{\"id\":\"R\",\"text\":\"查询\",\"checked\":false},{\"id\":\"U\",\"text\":\"修改\",\"checked\":false},{\"id\":\"D\",\"text\":\"删除\",\"checked\":false}]',1100),('b_publicity_reader','公示公告查看','','','index','初始数据',1,'[{\"id\":\"C\",\"text\":\"新增\",\"checked\":false},{\"id\":\"R\",\"text\":\"查询\",\"checked\":false},{\"id\":\"U\",\"text\":\"修改\",\"checked\":false},{\"id\":\"D\",\"text\":\"删除\",\"checked\":false}]',1200),('developer','系统开发','','','-1','',1,'[{\"id\":\"M\",\"text\":\"菜单可见\",\"checked\":true}]',1000000),('index','首页','',NULL,'-1','首页',1,'[{\"id\":\"M\", \"text\":\"菜单可见\", \"uri\":\"\"},{\"id\":\"C\", \"text\":\"新增\", \"uri\":\"\"},{\"id\":\"R\", \"text\":\"查询\", \"uri\":\"\"},{\"id\":\"U\", \"text\":\"修改\", \"uri\":\"\"},{\"id\":\"D\", \"text\":\"删除\", \"uri\":\"\"}]',1000),('module','权限管理','page/module/list.html',NULL,'sys','初始数据',1,'[{\"id\":\"M\", \"text\":\"菜单可见\", \"uri\":\"\"},{\"id\":\"C\", \"text\":\"新增\", \"uri\":\"\"},{\"id\":\"R\", \"text\":\"查询\", \"uri\":\"\"},{\"id\":\"U\", \"text\":\"修改\", \"uri\":\"\"},{\"id\":\"D\", \"text\":\"删除\", \"uri\":\"\"}]',110000),('query_plan_test','通用查询测试','page/query/common.html?module=b_test','','developer','',1,'[{\"id\":\"M\",\"text\":\"菜单可见\",\"checked\":true},{\"id\":\"C\",\"text\":\"新增\",\"checked\":false},{\"id\":\"R\",\"text\":\"查询\",\"checked\":false},{\"id\":\"U\",\"text\":\"修改\",\"checked\":false},{\"id\":\"D\",\"text\":\"删除\",\"checked\":false}]',1200000),('role','角色管理','page/role/list.html',NULL,'sys','初始数据',1,'[{\"id\":\"M\", \"text\":\"菜单可见\", \"uri\":\"\"},{\"id\":\"C\", \"text\":\"新增\", \"uri\":\"\"},{\"id\":\"R\", \"text\":\"查询\", \"uri\":\"\"},{\"id\":\"U\", \"text\":\"修改\", \"uri\":\"\"},{\"id\":\"D\", \"text\":\"删除\", \"uri\":\"\"}]',120000),('sys','系统维护','','','-1','初始数据',1,'[{\"id\":\"M\",\"text\":\"菜单可见\",\"checked\":false},{\"id\":\"C\",\"text\":\"新增\",\"checked\":false},{\"id\":\"R\",\"text\":\"查询\",\"checked\":false},{\"id\":\"U\",\"text\":\"修改\",\"checked\":false},{\"id\":\"D\",\"text\":\"删除\",\"checked\":false}]',100000),('s_logger','日志管理','page/logger/list.html',NULL,'sys','初始数据',1,'[{\"id\":\"M\", \"text\":\"菜单可见\", \"uri\":\"\"},{\"id\":\"C\", \"text\":\"新增\", \"uri\":\"\"},{\"id\":\"R\", \"text\":\"查询\", \"uri\":\"\"},{\"id\":\"U\", \"text\":\"修改\", \"uri\":\"\"},{\"id\":\"D\", \"text\":\"删除\", \"uri\":\"\"}]',130000),('s_query_plan','查询方案管理','page/queryPlan/list.html','','developer','',1,'[{\"id\":\"M\",\"text\":\"菜单可见\",\"checked\":true},{\"id\":\"C\",\"text\":\"新增\",\"checked\":false},{\"id\":\"R\",\"text\":\"查询\",\"checked\":false},{\"id\":\"U\",\"text\":\"修改\",\"checked\":false},{\"id\":\"D\",\"text\":\"删除\",\"checked\":false}]',1100000),('s_query_plan_role','查询方案角色管理','','','developer','此为管理权限，无需显示到菜单',1,'[{\"id\":\"R\",\"text\":\"查询\",\"checked\":true}]',1110000),('s_template','模板管理','page/template/list.html','','developer','',1,'[{\"id\":\"M\",\"text\":\"菜单可见\",\"checked\":true},{\"id\":\"C\",\"text\":\"新增\",\"checked\":true},{\"id\":\"R\",\"text\":\"查询\",\"checked\":false},{\"id\":\"U\",\"text\":\"修改\",\"checked\":false},{\"id\":\"D\",\"text\":\"删除\",\"checked\":false}]',1500000),('user','用户管理','page/user/list.html',NULL,'sys','初始数据',1,'[{\"id\":\"M\", \"text\":\"菜单可见\", \"uri\":\"\"},{\"id\":\"C\", \"text\":\"新增\", \"uri\":\"\"},{\"id\":\"R\", \"text\":\"查询\", \"uri\":\"\"},{\"id\":\"U\", \"text\":\"修改\", \"uri\":\"\"},{\"id\":\"D\", \"text\":\"删除\", \"uri\":\"\"}]',130000);

/*Table structure for table `s_query_plan` */

DROP TABLE IF EXISTS `s_query_plan`;

CREATE TABLE `s_query_plan` (
  `u_id` varchar(64) DEFAULT NULL COMMENT '主键',
  `type` varchar(64) NOT NULL COMMENT '方案类型',
  `name` varchar(128) NOT NULL COMMENT '方案名称',
  `query_condition` text COMMENT '可选查询条件',
  `show_fields` text COMMENT '列表显示字段',
  `info_template_id` varchar(256) DEFAULT NULL COMMENT '查看详情模板id',
  `creator_id` varchar(256) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `status` int(11) DEFAULT NULL COMMENT '状态',
  `module_id` varchar(512) NOT NULL COMMENT '模块ID',
  `data_api` varchar(1024) NOT NULL COMMENT '数据接口'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='查询方案,更新于:2016-01-21 09:52:39。新增字段0,删除字段0,变更字段12';

/*Data for the table `s_query_plan` */

insert  into `s_query_plan`(`u_id`,`type`,`name`,`query_condition`,`show_fields`,`info_template_id`,`creator_id`,`create_time`,`update_time`,`status`,`module_id`,`data_api`) values ('test','测试','公示公告','[{\"id\":\"title$LIKE\",\"text\":\"标题\",\"type\":\"text\"},{\"id\":\"create_date$START\",\"text\":\"发布时间\",\"type\":\"date\"}]','[{\"field\":\"u_id\",\"header\":\"ID\",\"mapper\":\"\"},{\"field\":\"title\",\"header\":\"标题\",\"mapper\":\"\"},{\"field\":\"create_date\",\"header\":\"发布日期\",\"mapper\":\"\"}]','t_test',NULL,'2015-11-30 16:41:19',NULL,NULL,'b_test','cf/b_publicity'),('jBCMLH1b','测试','测试方案2','[{\"id\":\"username$LIKE\",\"text\":\"用户名\",\"type\":\"text\"}]','[{\"field\":\"username\",\"header\":\"用户名\",\"mapper\":\"\"},{\"field\":\"name\",\"header\":\"姓名\",\"mapper\":\"\"},{\"field\":\"phone\",\"header\":\"联系电话\",\"mapper\":\"\"}]','t_user','admin',NULL,NULL,1,'b_test','user');

/*Table structure for table `s_query_plan_role` */

DROP TABLE IF EXISTS `s_query_plan_role`;

CREATE TABLE `s_query_plan_role` (
  `u_id` varchar(64) DEFAULT NULL COMMENT '主键',
  `plan_id` varchar(64) NOT NULL COMMENT '方案主键',
  `role_id` varchar(64) NOT NULL COMMENT '角色主键',
  `is_default` int(11) NOT NULL COMMENT '是否为默认方案'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='null,更新于:2016-01-21 09:52:39。新增字段0,删除字段0,变更字段4';

/*Data for the table `s_query_plan_role` */

insert  into `s_query_plan_role`(`u_id`,`plan_id`,`role_id`,`is_default`) values ('xDiXuJEk','test','admin',0),('f6DtYJoK','jBCMLH1b','admin',1);

/*Table structure for table `s_resources` */

DROP TABLE IF EXISTS `s_resources`;

CREATE TABLE `s_resources` (
  `u_id` varchar(40) NOT NULL DEFAULT '' COMMENT '主键',
  `name` varchar(256) NOT NULL COMMENT '资源名称',
  `path` varchar(1024) NOT NULL COMMENT '资源地址',
  `type` varchar(256) NOT NULL COMMENT '脚本类型',
  `md5` varchar(256) NOT NULL COMMENT 'md5值',
  `status` int(11) DEFAULT NULL COMMENT '状态',
  `create_date` datetime NOT NULL COMMENT '创建日期',
  `creator_id` varchar(256) NOT NULL COMMENT '创建人主键',
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='资源,更新于:2016-01-21 09:52:40。新增字段0,删除字段0,变更字段8';

/*Data for the table `s_resources` */

insert  into `s_resources`(`u_id`,`name`,`path`,`type`,`md5`,`status`,`create_date`,`creator_id`) values ('1qcMJR','blob.png','/file/2015-12-02','file','6673b9564f059fe95f16be9d0d1b6d23',1,'2015-12-02 16:43:38','-1'),('3m67hW','d833c895d143ad4bb664f0c880025aafa40f066b.jpg','/file/2015-11-29','file','20c48a3347957a48f2631a79d72b5000',1,'2015-11-29 16:04:02','admin'),('63CcTA','78914edfc030f71dbb30441679c6e8bd.jpg','/file/2015-11-29','file','e559668a7c38434c81dad97300ec0bef',1,'2015-11-29 16:04:02','admin'),('AzCxjB','UltraISO.exe','/file/2015-11-26','file','9773c12edcd5d3d62db95843f0f6349f',1,'2015-11-26 23:12:38','admin'),('BZqjvo','1d88b38ff4f4111861c17513dce3ca31.jpg','/file/2015-11-29','file','b32bb760325f54d22d35d853fb0ed592',1,'2015-11-29 14:54:47','admin'),('dbRy9k','834e267281ac3aa4c7403ba0a9152d5d.jpg','/file/2015-11-29','file','52334b33bd6efc14d81086db2eda432d',1,'2015-11-29 16:04:02','admin'),('GON565','default.jpg','/file/2015-11-29','file','759e263dbf96f41d05dc3c247daca45b',1,'2015-11-29 16:04:02','admin'),('HDzhrc','1d88b38ff4f4111861c17513dce3ca31.jpg','/file/2015-11-29','file','548386296a39527c84793c0e29dfa31b',1,'2015-11-29 15:35:04','admin'),('j3hRG4','20121120133354156.jpg','/file/2015-11-29','file','59795210c6c5b364fa493b2e18079e8a',1,'2015-11-29 16:04:02','admin'),('J6SQjX','aa.docx','/file/2015-12-09','file','c089631517fc2625ea6577617b269605',1,'2015-12-09 11:50:52','admin'),('lVR89C','fswerwr.jpg','/file/2015-11-29','file','89de82e788018ffa427cb4a177626d96',1,'2015-11-29 16:04:02','admin'),('mzyiBD','6b9e8f28cc8c5927d7485a2777fe60b9.jpg','/file/2015-11-29','file','2df28fe500963ca4d8824051412f83f5',1,'2015-11-29 16:04:01','admin'),('qQGE5N','97f53e7eebe0a3c0b2bebfc7cebea454.jpg','/file/2015-11-29','file','b3007537eb42f63c47c854f41b294067',1,'2015-11-29 16:04:01','admin'),('qZzMZS','blob.png','/file/2015-12-27','file','ed31e9754506a83aaaefa6675e58c0df',1,'2015-12-27 14:15:09','-1'),('RG5y3Q','111.jpg','/file/2015-11-29','file','3b021057be85bb3d47c22a26dcb992a4',1,'2015-11-29 16:04:01','admin'),('Rit09B','35ec343ad74703c3931f1339ffff31e6.jpg','/file/2015-11-29','file','b8d9ecf4e0bd2e19d695c9d94157dc58',1,'2015-11-29 16:04:01','admin'),('S5rZaB','9358d109b3de9c82973f93916e81800a19d84351.jpg','/file/2015-11-29','file','0b42e840b4140c4fd11bc362c1d9e74b',1,'2015-11-29 16:04:01','admin'),('U1WE62','blob.png','/file/2015-12-02','file','79f169d9710186946d08fa7b37bdd0c7',1,'2015-12-02 17:28:46','-1'),('uMnp96','1430285866114322.png','/file/2015-12-02','file','a8f49b0ebcd6b5eb8e7b995aedd14e3a',1,'2015-12-02 17:52:50','-1'),('UPhh3H','blob.png','/file/2015-12-02','file','4b27b163cf5a2cbf2f1d3449433a5f9b',1,'2015-12-02 16:43:20','-1'),('WGNTXH','1d88b38ff4f4111861c17513dce3ca31.jpg','/file/2015-11-29','file','d41d8cd98f00b204e9800998ecf8427e',1,'2015-11-29 15:27:42','admin'),('wVZEe4','M9FPKCMT@N8]3GVKMIYZB7C.jpg','/file/2015-11-29','file','c6b8be525032ec5ae35aa936c7cac758',1,'2015-11-29 16:04:02','admin');

/*Table structure for table `s_role` */

DROP TABLE IF EXISTS `s_role`;

CREATE TABLE `s_role` (
  `u_id` varchar(40) NOT NULL DEFAULT '' COMMENT '主键',
  `name` varchar(256) NOT NULL COMMENT '角色名称',
  `type` varchar(50) DEFAULT NULL COMMENT '角色类型',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色管理,更新于:2016-01-21 09:52:40。新增字段0,删除字段0,变更字段4';

/*Data for the table `s_role` */

insert  into `s_role`(`u_id`,`name`,`type`,`remark`) values ('admin','超级管理员','','初始数据');

/*Table structure for table `s_role_modules` */

DROP TABLE IF EXISTS `s_role_modules`;

CREATE TABLE `s_role_modules` (
  `u_id` varchar(40) NOT NULL DEFAULT '' COMMENT '主键',
  `module_id` varchar(256) NOT NULL COMMENT '模块主键',
  `role_id` varchar(256) NOT NULL COMMENT '角色主键',
  `o_level` text COMMENT '操作权限',
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色模块分配,更新于:2016-01-21 09:52:40。新增字段0,删除字段0,变更字段4';

/*Data for the table `s_role_modules` */

insert  into `s_role_modules`(`u_id`,`module_id`,`role_id`,`o_level`) values ('4hDa58','query_plan_test','admin','[\"M\",\"R\"]'),('6z5wjJ','s_query_plan','admin','[\"M\",\"R\"]'),('ayqe2O','s_logger','admin','[\"M\",\"R\"]'),('Cbp5O9','developer','admin','[\"M\"]'),('CVNBwe','b_publicity_reader','admin','[\"C\",\"R\",\"U\"]'),('GTR8dV','sys','admin','[\"M\"]'),('ikreyE','b_publicity','admin','[\"R\"]'),('JKkdus','module','admin','[\"M\",\"R\"]'),('LcgQCG','user','admin','[\"M\",\"R\"]'),('lsMYIX','role','admin','[\"M\",\"R\"]'),('O7YaVI','s_template','admin','[\"M\",\"R\"]'),('vyxI7Z','index','admin','[\"M\"]');

/*Table structure for table `s_script` */

DROP TABLE IF EXISTS `s_script`;

CREATE TABLE `s_script` (
  `u_id` varchar(40) NOT NULL DEFAULT '' COMMENT '主键',
  `name` varchar(256) NOT NULL COMMENT '脚本名称',
  `path` varchar(1024) NOT NULL COMMENT '路径',
  `type` varchar(256) NOT NULL COMMENT '脚本类型',
  `content` text NOT NULL COMMENT '脚本内容',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `status` int(11) DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户角色分配,更新于:2016-01-21 09:52:40。新增字段0,删除字段0,变更字段7';

/*Data for the table `s_script` */

/*Table structure for table `s_template` */

DROP TABLE IF EXISTS `s_template`;

CREATE TABLE `s_template` (
  `u_id` varchar(64) DEFAULT NULL COMMENT '主键',
  `type` varchar(64) NOT NULL COMMENT '模板类型',
  `name` varchar(128) NOT NULL COMMENT '模板名称',
  `content` text COMMENT '模板内容',
  `creator_id` varchar(256) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='模板管理,更新于:2016-01-21 09:52:39。新增字段0,删除字段0,变更字段7';

/*Data for the table `s_template` */

insert  into `s_template`(`u_id`,`type`,`name`,`content`,`creator_id`,`create_time`,`update_time`) values ('t_test','测试','测试模板','<p style=\"margin: 0px 0px 20px; padding: 0px 4px 0px 0px; text-align: center; font-size: 32px; font-weight: bold; border-bottom-color: rgb(204, 204, 204); border-bottom-width: 2px; border-bottom-style: solid;\">{{title}}</p><p style=\"text-align: right;\">{{create_date}}</p><table align=\"center\"><tbody><tr class=\"firstRow\"><td width=\"750\" align=\"right\" valign=\"middle\" style=\"-ms-word-break: break-all;\">标题</td><td width=\"750\" valign=\"top\" style=\"-ms-word-break: break-all;\">{{title}}</td></tr><tr><td width=\"750\" align=\"right\" valign=\"middle\" style=\"-ms-word-break: break-all;\">发布</td><td width=\"750\" valign=\"top\"><br/></td></tr><tr><td width=\"750\" valign=\"top\"><br/></td><td width=\"750\" valign=\"top\"><br/></td></tr><tr><td width=\"750\" valign=\"top\"><br/></td><td width=\"750\" valign=\"top\"><br/></td></tr></tbody></table><p><br/></p><img width=\"1919\" height=\"1335\" title=\"1430285866114322.png\" style=\"width: 659px; height: 437px;\" alt=\"1430285866114322.png\" src=\"/wb/api/file/download/uMnp96\"/>','admin',NULL,'2015-12-02 17:53:14'),('t_user','测试','用户详情模板','<p style=\"text-align: center;\"><span style=\"font-size: 36px;\">{{username}}<br/></span></p><p style=\"text-align: center;\"><span style=\"font-size: 36px;\">{{name}}</span></p>','admin','2015-12-02 18:24:47',NULL);

/*Table structure for table `s_user` */

DROP TABLE IF EXISTS `s_user`;

CREATE TABLE `s_user` (
  `u_id` varchar(40) NOT NULL DEFAULT '' COMMENT '主键',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(64) NOT NULL COMMENT '密码',
  `name` varchar(64) DEFAULT NULL COMMENT '姓名',
  `email` varchar(512) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(64) DEFAULT NULL COMMENT '联系电话',
  `status` int(11) DEFAULT NULL COMMENT '状态',
  `create_date` datetime NOT NULL COMMENT '创建日期',
  `update_date` datetime DEFAULT NULL COMMENT '修改日期',
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表,更新于:2016-01-21 09:52:40。新增字段0,删除字段0,变更字段9';

/*Data for the table `s_user` */

insert  into `s_user`(`u_id`,`username`,`password`,`name`,`email`,`phone`,`status`,`create_date`,`update_date`) values ('admin','admin','23ec59e119da971084cbd0ba72d230a0',NULL,NULL,NULL,NULL,'2015-11-26 23:04:49',NULL),('WA1Rd5','test','49ef565bc8a7cddfccf9608f51898359','测试用户','','',0,'2015-12-01 17:07:22','2016-01-18 19:24:39');

/*Table structure for table `s_user_02` */

DROP TABLE IF EXISTS `s_user_02`;

CREATE TABLE `s_user_02` (
  `id` varchar(50) NOT NULL DEFAULT '',
  `username` varchar(256) NOT NULL COMMENT '用户名',
  `area_id` int(11) DEFAULT NULL COMMENT '地区主键',
  `s_area_id` int(11) DEFAULT NULL COMMENT '第二地区主键',
  `test_f` varchar(256) DEFAULT NULL COMMENT '测试字段',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='测试表,更新于:2015-12-21 13:48:35。新增字段1,删除字段0,变更字段4';

/*Data for the table `s_user_02` */

insert  into `s_user_02`(`id`,`username`,`area_id`,`s_area_id`,`test_f`) values ('111','admin',1,2,NULL);

/*Table structure for table `s_user_role` */

DROP TABLE IF EXISTS `s_user_role`;

CREATE TABLE `s_user_role` (
  `u_id` varchar(40) NOT NULL DEFAULT '' COMMENT '主键',
  `user_id` varchar(256) NOT NULL COMMENT '用户主键',
  `role_id` varchar(256) NOT NULL COMMENT '角色主键',
  PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户角色分配,更新于:2016-01-21 09:52:40。新增字段0,删除字段0,变更字段3';

/*Data for the table `s_user_role` */

insert  into `s_user_role`(`u_id`,`user_id`,`role_id`) values ('admin_admin','admin','admin'),('x9Ww9Kof','WA1Rd5','admin');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
