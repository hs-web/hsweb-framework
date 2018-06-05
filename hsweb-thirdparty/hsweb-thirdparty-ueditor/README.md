# 百度UEditor

对[百度UEditor](http://ueditor.baidu.com/website/)后台服务的封装

# 使用:

在pom.xml引入模块:

```xml
<dependency>
   <dependency>
        <groupId>org.hswebframework.web</groupId>
        <artifactId>hsweb-thirdparty-ueditor</artifactId>
        <version>${hsweb.framework.version}</version>
    </dependency>
</dependency>
```

⚠️ 注意: 此模块使用`hsweb-system-file-api`模块进行文件管理. 
所以在使用此模块之前需要提供`hsweb-system-file-api`的实现模块.
你可以查看[hsweb-system-file](../../hsweb-system/hsweb-system-file)的文档进行引入.

### 后端配置

在`src/main/resources` 下建立`ueditor-config.json`文件,此文件为ueditor的配置文件.例如:
[ueditor-config.json](https://github.com/hs-web/hsweb3-demo/blob/master/src/main/resources/ueditor-config.json)
你也可以参照[官方的配置](http://fex.baidu.com/ueditor/#server-config)

### 前端配置
修改`ueditor.config.js`中的配置项: `window.UEDITOR_CONFIG.serverUrl`. 例如:

```js
 /**
     * 配置项主体。注意，此处所有涉及到路径的配置别遗漏URL变量。
     */
    window.UEDITOR_CONFIG = {

        // 服务器统一请求接口路径
          serverUrl: "/ueditor"
        //** 更多配置项
        }
```