# 贡献你的代码
1. fork 本仓库
2. 修改,增加代码
3. 执行`mvn test`通过
4. 提交`pull request`
5. 坐等审查
6. 合并

## 事务边界与通用树集成测试

普通 `ReactiveCrudService.createUpdate/createDelete` 返回仓储 DSL，不隐式开启事务。
需要将 SQL、实体事件及其他写入一起回滚时，业务 Service 应对完整业务 Publisher 明确声明
事务；命名 CRUD 方法保持其事务注解。基础树 insert/save/update/delete 的复合操作由
程序式事务保持完整原子性，直接调用非代理树 Service 也应在后批失败或取消时整体回滚。
普通缓存 DSL 保持既有 hook 行为：修改数量大于零时清除缓存，零修改时不清除。

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
   确认整棵树与调用方写入一起回滚；非代理树复合操作在后批失败或取消时恢复完整快照，
   追加 hook 不重复执行原 seed SQL。
4. `PlainCrudTransactionTest`：Spring 代理业务事务内的真实事件失败与 SQL 后取消；
   无调用方事务的普通 DSL 不承诺隐式回滚，追加 hook 和 SQL 执行次数保持仓储行为。
5. `GenericReactiveCacheSupportCrudServiceTest`：有无调用方事务时，普通缓存 DSL 的
   正修改/零修改结果、缓存清除和追加 hook 顺序；`ScopedReactiveTreeSortEntityServiceTest`
   验证普通更新的调用方事务与树删除自身事务。

这些用例维持现有入口差异：同步树准备仅覆盖 `insert(Collection)`/`save(List)`，
不把普通同步 `updateById` 定义为结构修图入口；没有指定排序的查询不保证顺序，
同步删除不要求叶先执行。路径容量边界按实际实体 schema 验证，不扩大 schema。

`hsweb-commons-crud` 相关集成测试的定向验证命令：
`mvn -pl hsweb-commons/hsweb-commons-crud -am -Dtest=PlainCrudTransactionTest,GenericReactiveCacheSupportCrudServiceTest,ScopedReactiveTreeSortEntityServiceTest,SyncTreeCrudRegressionTest,ReactiveTreeCrudCoverageTest -Dsurefire.failIfNoSpecifiedTests=false test`。
同步删除的正式实体事件依赖 easy-orm 同步 DSL 的默认 mapping/repository 上下文和执行级
before/after/error 共享上下文。干净 CI 使用 easy-orm `4.2` 中包含该通用修复的
`4.2.2-SNAPSHOT`。运行结果记录在对应 PR 的验证证据中。

## 树 Save 与部分更新的契约

树 save 按提交实体的完整树关系保存：已有节点的 `parentId=null` 表示移根，
共用 helper 维护 path/level 和原有后代；父级清空 SQL 与正式 Modify/Save 事件参与同一
事务。遗漏 children 不自动删除后代。业务列的 null 仍遵循现有 ORM 忽略 null、生成
默认值等规则，这一契约不提供整实体的全量 Replace。

公开 `PATCH /资源` 是批量 Save/upsert，已有 ID 保存、不存在的 ID 新增；
`PUT /资源/{id}` 是正式 `updateById` 部分更新入口，null 沿用持久父级和业务字段。
既有节点的名称等稀疏修改应使用更新入口，完整 Save 必须表达本次需要的树关系。
同步树准备覆盖 `insert(Collection)` / `save(List)`，普通同步 `updateById` 不提供
结构修图；响应式树 `updateById` 维护显式关系变化及遗漏后代的必要树字段。

实现位于 `hsweb-commons/hsweb-commons-crud` 的 `TreeSortServiceHelper`、
`TreeSortEntityService`、`ReactiveTreeSortEntityService`；公开路由由
`ReactiveServiceSaveController` 提供。`ExplicitTreeUpdateRegressionTest` 通过真实
R2DBC H2 / HTTP 和正式实体事件验证 PUT 遗漏或传 null 父级时保留关系，及 PATCH
完整 null 父级移根、保留遗漏后代和同批不存在 ID 新增。

## 树 updateById 的业务校验边界

响应式树 `updateById` 对目标及显式既有 child 保持纯 UPDATE，使用正式 Modify 的
完整 before/after 事件；目标已被另一事务删除时返回零，不能以 Save/UPSERT 重新
创建目标或继续创建 child。显式新 child 经过服务 Save；自动补齐的后代只维护必要树字段。

`save(Publisher)` 覆写只拥有 Save 入口。原来在该覆写中校验既有节点更新的业务，需要
将更新规则迁移到 `EntityBeforeModifyEvent.getAfter()`；新增和 Save 分别监听
`EntityBeforeCreateEvent` / `EntityBeforeSaveEvent`，共同调用业务模块自己的校验函数。
既有 Save 覆写不会自动成为 UPDATE 的校验入口。

`TreeEntityEventValidationRegressionTest` 的 8 个真实 Spring / R2DBC H2 用例已通过：
两种 updateById overload 的目标拒绝、显式既有 child 拒绝和新 child 拒绝，以及直接
Create/Save 共用校验。正式 Modify after 包含持久业务列和完整树字段，child 拒绝前
读取实际已写父节点，拒绝后原事务恢复整个持久快照。

框架本次定向集成验证共 15 组、109 个用例：108 通过、1 个既有跳过、0 失败、0 错误；
跳过项为 `EntityEventListenerTest.testInsertError`。验证期间输入、依赖和执行入口保持
稳定，结果检查通过。这一结果只覆盖框架选定范围；普通树并发边界见下节。
以上契约的定向复验入口：
`mvn -pl hsweb-commons/hsweb-commons-crud -am -Dtest=ExplicitTreeUpdateRegressionTest,SyncTreeCrudRegressionTest,ScopedReactiveTreeSortEntityServiceTest,TreeEntityEventValidationRegressionTest -Dsurefire.failIfNoSpecifiedTests=false test`。

## 普通树删除的并发边界

响应式普通树在子树展开后按 ID 快照执行叶先分批删除。没有 parent 外键或共同领域
约束时，另一事务在展开与删除 SQL 之间提交的新 child 可能不在删除快照内，并留下
失去父节点的记录；框架当前不保证普通树并发新增/删除的关系完整性。

当前方案不新增通用锁，也不扩大已有领域锁策略。业务需要该并发保证时，应让关联
写入和删除遵守同一领域约束；已有 `prepareTreeDelete` 领域锁的保证仅适用于共同
遵守该约束的操作。框架已验证的复合事务、完整实体事件、分组隔离、叶先分批、取消
与回滚，不能外推为所有普通树的并发关系保证。

## 成员 ID 兼容性验证

`DimensionUserEntity.generateId()` 保持原有三字段以 `-` 拼接后的 MD5 规则；已提供的
ID 保持不变。`DimensionUserIdentityTest` 固定验证旧 ID，
`DimensionUserIdentityRegressionTest` 使用真实数据库和绑定事件验证按已知 ID 保存、
重复保存更新、查询及解绑；完整认证绑定行为同时由 `DefaultDimensionUserServiceTest` 验证。
定向验证命令：
`mvn -pl hsweb-system/hsweb-system-authorization/hsweb-system-authorization-default -am -Dtest=DimensionUserIdentityTest,DimensionUserIdentityRegressionTest,DefaultDimensionUserServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`。

# BUG
如果知道导致bug的位置,你可以直接修改后`pull request`,也可以提交[issues](https://github.com/hs-web/hsweb-framework/issues/new).我们会尽快解决.

# 需求&优化
你可以通过issues提交你希望`hsweb`增加的特性以及功能优化,并可以在 [projects](https://github.com/hs-web/hsweb-framework/projects)中查看`hsweb`的开发进展以及计划.

# 社区&交流
你可以通过提交`issues`或者加入官方QQ群:[515649185](http://shang.qq.com/wpa/qunwpa?idkey=3d66b5dd14991d7645af694e7649b373f3a9ce1216131094c78cb2348d542c41)
以及发送邮件和我们取得联系.
