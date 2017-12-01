package com.baidu.ueditor.define;

import java.util.HashMap;
import java.util.Map;

public final class AppInfo {

	public static final int SUCCESS = 0;
	public static final int MAX_SIZE = 1;
	public static final int PERMISSION_DENIED = 2;
	public static final int FAILED_CREATE_FILE = 3;
	public static final int IO_ERROR = 4;
	public static final int NOT_MULTIPART_CONTENT = 5;
	public static final int PARSE_REQUEST_ERROR = 6;
	public static final int NOTFOUND_UPLOAD_DATA = 7;
	public static final int NOT_ALLOW_FILE_TYPE = 8;

	public static final int INVALID_ACTION = 101;
	public static final int CONFIG_ERROR = 102;

	public static final int PREVENT_HOST = 201;
	public static final int CONNECTION_ERROR = 202;
	public static final int REMOTE_FAIL = 203;

	public static final int NOT_DIRECTORY = 301;
	public static final int NOT_EXIST = 302;

	public static final int ILLEGAL = 401;

	public static Map<Integer, String> info = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 7957693488024072535L;

		{

			put(AppInfo.SUCCESS, "SUCCESS");

			// 无效的Action
			put(AppInfo.INVALID_ACTION, "\u65E0\u6548\u7684Action");
			// 配置文件初始化失败
			put(AppInfo.CONFIG_ERROR, "\u914D\u7F6E\u6587\u4EF6\u521D\u59CB\u5316\u5931\u8D25");
			// 抓取远程图片失败
			put(AppInfo.REMOTE_FAIL, "\u6293\u53D6\u8FDC\u7A0B\u56FE\u7247\u5931\u8D25");

			// 被阻止的远程主机
			put(AppInfo.PREVENT_HOST, "\u88AB\u963B\u6B62\u7684\u8FDC\u7A0B\u4E3B\u673A");
			// 远程连接出错
			put(AppInfo.CONNECTION_ERROR, "\u8FDC\u7A0B\u8FDE\u63A5\u51FA\u9519");

			// "文件大小超出限制"
			put(AppInfo.MAX_SIZE, "\u6587\u4ef6\u5927\u5c0f\u8d85\u51fa\u9650\u5236");
			// 权限不足， 多指写权限
			put(AppInfo.PERMISSION_DENIED, "\u6743\u9650\u4E0D\u8DB3");
			// 创建文件失败
			put(AppInfo.FAILED_CREATE_FILE, "\u521B\u5EFA\u6587\u4EF6\u5931\u8D25");
			// IO错误
			put(AppInfo.IO_ERROR, "IO\u9519\u8BEF");
			// 上传表单不是multipart/form-data类型
			put(AppInfo.NOT_MULTIPART_CONTENT, "\u4E0A\u4F20\u8868\u5355\u4E0D\u662Fmultipart/form-data\u7C7B\u578B");
			// 解析上传表单错误
			put(AppInfo.PARSE_REQUEST_ERROR, "\u89E3\u6790\u4E0A\u4F20\u8868\u5355\u9519\u8BEF");
			// 未找到上传数据
			put(AppInfo.NOTFOUND_UPLOAD_DATA, "\u672A\u627E\u5230\u4E0A\u4F20\u6570\u636E");
			// 不允许的文件类型
			put(AppInfo.NOT_ALLOW_FILE_TYPE, "\u4E0D\u5141\u8BB8\u7684\u6587\u4EF6\u7C7B\u578B");

			// 指定路径不是目录
			put(AppInfo.NOT_DIRECTORY, "\u6307\u5B9A\u8DEF\u5F84\u4E0D\u662F\u76EE\u5F55");
			// 指定路径并不存在
			put(AppInfo.NOT_EXIST, "\u6307\u5B9A\u8DEF\u5F84\u5E76\u4E0D\u5B58\u5728");

			// callback参数名不合法
			put(AppInfo.ILLEGAL, "Callback\u53C2\u6570\u540D\u4E0D\u5408\u6CD5");

		}
	};

	public static String getStateInfo(int key) {
		return AppInfo.info.get(key);
	}

}
