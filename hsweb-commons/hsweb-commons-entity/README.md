# 通用实体类模块
集成系统通用的实体类,如 树形结构实体,排序实体,创建信息实体

# 常用实体类

| 类名       | 说明          | 
| ------------- |:-------------:| 
| [`Entity`](src/main/java/org/hswebframework/web/commons/entity/Entity.java)    | 实体类的总接口,用来标识为一个实体类 | 
| [`GenericEntity`](src/main/java/org/hswebframework/web/commons/entity/GenericEntity.java)    | 提供基本属性的实体类 | 
| [`RecordCreationEntity`](src/main/java/org/hswebframework/web/commons/entity/RecordCreationEntity.java)    | 可记录创建信息的实体类 | 
| [`TreeSortSupportEntity`](src/main/java/org/hswebframework/web/commons/entity/TreeSortSupportEntity.java)    | 可排序树形结构实体类 | 

# 实体类工厂
作用: 为了增加拓展性,各个地方依赖的实体均为接口,实体实例应该调用[EntityFactory](src/main/java/org/hswebframework/web/commons/entity/factory/EntityFactory.java)
进行实例化。如: `UserEntity user=entityFactory.newInstance(UserEntity.class);`

目标: controller,service 不再依赖具体实体实现类。实现类由 dao和springMvc进行提供

默认工厂实现: [MapperEntityFactory](src/main/java/org/hswebframework/web/commons/entity/factory/MapperEntityFactory.java)
该工厂可注册接口和实现类的映射关系，以及提供默认的实现类创建。
默认的实现类创建逻辑为。`Class.forName("Simple"+interfaceName);`
如:`UserEntity user=entityFactory.newInstance(UserEntity.class)` 
如果未注册`UserEntity`对应的实现类,则将尝试创建`UserEntity`同包下的`SimpleUserEntity`类实例

注册接口和实现类映射关系:

方式1: 调用 mapperEntityFactory进行注册

```java
    @javax.annotation.Resource
    private MapperEntityFactory mapperEntityFactory;

    @javax.annotation.PostConstruct
    public void init(){
        mapperEntityFactory.addMapping(UserEntity.class,new Mapper(CustomUserEntity.class,CustomUserEntity::new));
    }

```

方式2: application.yml 配置文件描述

```yaml
entity:
      mappings:
          -  source-base-package: org.hswebframework.web.entity.authorization
             target-base-package: com.company.authorization
             mapping:
                UserEntity: CustomUserEntity
```