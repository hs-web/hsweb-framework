# 系统核心,通用工具等


### bean 复制工具
`FastBeanCopier`类. 提供高效的bean复制.支持复杂结构,类型转换,集合泛型,支持bean到map,map到bean的复制.

原理: 使用工具类`Proxy`,通过`javassist`去动态构造一个类,通过原生的方式调用get set方法.而不是通过低效的反射.

```java
 //将source对象中的属性复制到target中.
 FastBeanCopier.copy(source,target);

 //将source对象中的属性复制到target中.不复制id字段
 FastBeanCopier.copy(source,target,"id");

```
类型转换可通过 `BeanUtilsBean.getInstance().getConvertUtils().register(converter, targetType)`
扩展。嵌套 Map 转为接口、抽象类或需要工厂创建的对象时，先使用注册的转换器，再回退到
默认 Bean 创建与复制逻辑；通过 `FastBeanCopier.copy` 传入的 `Converter` 同样适用于嵌套属性。
转换器注册、替换和注销应在下一次转换时生效，无需清理 Bean 复制缓存。

转换计划按目标类型和泛型结构缓存；调用方提供的泛型数组不会被缓存直接持有。
共享计划仅保存类型元数据，不持有局部 Converter 或 BeanFactory；每次转换使用当前工厂。
默认 BeanFactory 和以 Class 为目标的复制缓存无参构造器元数据，每次仍创建新实例，
保留构造器访问检查与异常传播。自定义 BeanFactory 在转换时使用，不受元数据缓存影响。
动态类加载器卸载前应调用 `FastBeanCopier.clearCache(classLoader)`，同时释放复制器、
转换计划和构造器缓存。

默认 List / Set 和普通 Map 复制按已知元素数分配容量，保持容器类型、遍历顺序与独立复制。
自定义集合仍通过无参构造器创建。复制器、record 和后端的类型对缓存命中无需分配组合键，
按源类型组织目标缓存；类加载器清理同时匹配源和目标类型，保留无关缓存。

`FastBeanCopierConverterTest` 覆盖四种后端的接口、抽象类、工厂创建对象、集合、数组和 record
属性转换，以及转换器动态注册、返回 null 和异常传播。`FastBeanCopierSupportTest`
覆盖泛型隔离、工厂替换与引用释放、容器复制、构造器异常与类加载器清理；
`ClassPairCacheTest` 覆盖并发创建、递归依赖、稀疏 / 密集目标及清理中的创建行为。
验证命令为 `mvn -o -pl hsweb-core -am test`。

兼容修复与缓存优化的测试结果及基线性能对照见
[FastBeanCopier Benchmark Summary](src/test/java/org/hswebframework/web/bean/FastBeanCopierBenchmarkSummary.md)。

约定: 如果属性类实现了`Cloneable`接口,在复制的时候将调用`clone`方法.所以如果你实现了`Cloneable`接口,就必须重写`clone`方法并且为`public`修饰的.

### 数据字典

可通过枚举来定义数据字典,定义一个枚举,并实现`EnumDict`接口:
```java
@AllArgsConstructor
@Getter
@Dict(id="data-status") //定义一个id,默认为 DataStatusEnum.class.getSimpleName();
public enum DataStatusEnum implements EnumDict<Byte> {
    ENABLED((byte) 1, "正常"),
    DISABLED((byte) 0, "禁用"),
    LOCK((byte) -1, "锁定"),
    DELETED((byte) -10, "删除");

    private Byte value;

    private String text;
}
```

在实体类中使用:
```java
@Data
public class User  {
    private String id;
    
    //单选
    private DataStatusEnum status;
    
    //多选
    private DataStatusEnum[] statusArr;
}
```

作用: 
1. 当值为单选,在持久化到数据库时,将自动存储字典的value值. 因此数据库字段的类型应该与value字段的类型一致.
2. 当值为多选,并且枚举选项数量小于`64`个,则会将值进行位运算(`EnumDict.toBit`)后存储.在查询的时候也使用位运算进行查询.
因此数据库字段的类型应该为数字类型。
如: `where().in("statusArr",0,-1);` 则将生成sql : `where status_arr & {bit} != {bit}` 。
在java中可以通过`EnumDict`中的静态方法进行判断,如 `in` 和 `anyIn`. 
3. 当枚举选项数量大于等于`64`个的时候,需要自行实现存储和查询逻辑,可以使用中间表的方式,也可以使用hsweb自带的实现,模块:`hsweb-system/hsweb-system-dictionary`。

注意: 1,2的功能由`hsweb-commons-dao`模块去实现,如果你不没有使用hsweb自带的dao实现,可能无法使用此功能.

所有的字典都会注册到:`DictDefineRepository`,可通过此类去获取字典,以提供给前端或者其他地方使用.

## ToString
``org.hswebframework.web.bean.ToString``提供了对Bean转为String的功能.包括字段脱敏(打码).

```java

@lombok.Getter
@lombok.Setter
public class MyEntity{
    
    //敏感字段,在ToString的时候会给字段打码.比如: 185*****234
    @org.hswebframework.web.bean.ToString.Ignore
    private String userPhone;
    
    public String toString(){
        return org.hswebframework.web.bean.ToString.toString(this);
    }
}

```
