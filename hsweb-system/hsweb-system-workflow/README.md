## 工作流模板
使用flowable(前activiti) 整合组织架构和动态表单。实现灵活的工作流配置和运行。

## SQL条件
`hsweb-system-workflow-local`模块中提供了一些自定义的查询条件,用于对流程的关联查询.可以在动态查询中
进行使用,例如:

```java

//查询id为userId用户的待办任务
createQuery().where("processInstanceId","user-wf-todo",userId).list();

```
注意: 表中需要存在与属性`processInstanceId`关联的字段

1. user-wf-claim : 用户待签收(领取)的流程数据
2. user-wf-todo : 用户待处理的流程数据
3. user-wf-completed : 用户已处理的流程数据
4. user-wf-part : 用户参与的流程数据,通过options来参与的类型: `where("processInstanceId$user-wf-part$starter","admin")`.(参与类型为`starter`)
