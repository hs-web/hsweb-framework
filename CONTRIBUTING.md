# 贡献你的代码
1. fork 本仓库
2. 修改,增加代码
3. 执行`mvn test`通过
4. 提交`pull request`
5. 坐等审查
6. 合并

## 通用树集成测试

树 CRUD 的测试使用真实 JDBC/R2DBC 仓储、实体事件和原事务。期望关系根据测试输入的
`parentId` 独立推导，不调用生产树准备 helper 生成期望值；回滚比较完整持久化快照，
取消执行后等待事务 `afterCompletion` 再查询数据库。

实体事件失败与调用方失败使用独立分支。事件失败分支必须由真实
`EntityCreatedEvent`、`EntitySavedEvent` 或 `EntityDeletedEvent` 的监听器拒绝触发，
不能额外抛出调用方异常使测试误通过。同步删除的 after 监听器还核对原事务内 SQL
已执行，再验证事件拒绝使整棵子树及调用方先前写入一起恢复。

用例入口：

1. `SyncTreeCrudRegressionTest`：乱序多根 `insert(Collection)`、小批次 `save(List)`
   深层移动与遗漏后代、后批实体事件/调用方失败，以及重叠 seed 的 `deleteById(Collection)`。
2. `ReactiveTreeCrudCoverageTest`：两种 `updateById` 的现有缺父级/循环拒绝规则；
   同步与响应式共用 forest 夹具逐层验证查询 children、明确指定的排序、默认排序索引。
3. `ReactiveTreeCrudCoverageTest`：buffer=2 子树删除在后批 SQL/事件开始后取消，
   确认整棵树与调用方写入一起回滚。

这些用例维持现有入口差异：同步树准备仅覆盖 `insert(Collection)`/`save(List)`，
不把普通同步 `updateById` 定义为结构修图入口；没有指定排序的查询不保证顺序，
同步删除不要求叶先执行。路径容量边界按实际实体 schema 验证，不扩大 schema。

先完成一批连贯用例，再集中运行 `hsweb-commons-crud` 相关集成测试。定向验证命令：
`mvn -pl hsweb-commons/hsweb-commons-crud -am -Dtest=SyncTreeCrudRegressionTest,ReactiveTreeCrudCoverageTest -Dsurefire.failIfNoSpecifiedTests=false test`。
同步删除的正式实体事件依赖 easy-orm 同步 DSL 的默认 mapping/repository 上下文和执行级
before/after/error 共享上下文。该上游修复位于 easy-orm `4.2`、`4.2.2-SNAPSHOT`，
须先发布可消费依赖，或在干净 CI 中从上游源码正式构建后再验收本仓库；开发机本地安装
不代表上游已发布。集中运行结果记录在对应 PR 的验证证据中。

# BUG
如果知道导致bug的位置,你可以直接修改后`pull request`,也可以提交[issues](https://github.com/hs-web/hsweb-framework/issues/new).我们会尽快解决.

# 需求&优化
你可以通过issues提交你希望`hsweb`增加的特性以及功能优化,并可以在 [projects](https://github.com/hs-web/hsweb-framework/projects)中查看`hsweb`的开发进展以及计划.

# 社区&交流
你可以通过提交`issues`或者加入官方QQ群:[515649185](http://shang.qq.com/wpa/qunwpa?idkey=3d66b5dd14991d7645af694e7649b373f3a9ce1216131094c78cb2348d542c41)
以及发送邮件和我们取得联系.
