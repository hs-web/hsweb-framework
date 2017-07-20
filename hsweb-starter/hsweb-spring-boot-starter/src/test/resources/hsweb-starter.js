/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
//组件信息
var info = {
    groupId: "org.hswebframework",
    artifactId: "hsweb-starter-test",
    version: "3.0.2",
    configClass: "",
    website: "http://github.com/hs-web",
    comment: "测试"
};

//版本更新信息
var versions = [
    {
        version: "3.0.0",
        upgrade: function (context) {
            java.lang.System.out.println("更新到3.0.2了");
        }
    },
    {
        version: "3.0.1",
        upgrade: function (context) {
            java.lang.System.out.println("更新到3.0.2了");
        }
    },
    {
        version: "3.0.2",
        upgrade: function (context) {
            java.lang.System.out.println("更新到3.0.1了");
        }
    }
];

function install(context) {
    var database = context.database;
    java.lang.System.out.println("安装了");
}


//设置依赖
dependency.setup(info)
    .onInstall(install)
    .onUpgrade(function (context) { //更新时执行
        var upgrader = context.upgrader;
        upgrader.filter(versions)
            .upgrade(function (newVer) {
                newVer.upgrade(context);
            });
    })
    .onUninstall(function (context) { //卸载时执行

    }).onInitialize(function (context) {
    java.lang.System.out.println("初始化啦");
});