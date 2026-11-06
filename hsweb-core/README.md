# 系统核心,通用工具等

### 国际化

`LocaleUtils.getSupportLocales()` 默认包含中文（`zh`）、英文（`en`）和日文（`ja`），可通过 JVM 参数 `-Dhsweb.locale.supports=...` 追加语言。该集合用于生成多语言数据，请求使用哪种语言仍由语言上下文决定。

框架的 `core`、`commons`、`authentication`、`authentication-default`、`dictionary` 五组消息资源均在对应模块的 `src/main/resources/i18n/{模块}/messages_ja.properties` 提供日语翻译，覆盖已有中英文键名的并集，并保留消息参数占位符。资源编码沿用各模块惯例，由 `hsweb-starter` 的 `I18nConfiguration` 使用 UTF-8 加载；`ja-JP` 通过语言回退使用 `ja` 资源。


### bean 复制工具
`FastBeanCopier`类. 提供高效的bean复制.支持复杂结构,类型转换,集合泛型,支持bean到map,map到bean的复制.

原理: 使用工具类`Proxy`,通过`javassist`去动态构造一个类,通过原生的方式调用get set方法.而不是通过低效的反射.

```java
 //将source对象中的属性复制到target中.
 FastBeanCopier.copy(source,target);

 //将source对象中的属性复制到target中.不复制id字段
 FastBeanCopier.copy(source,target,"id");

```

#### 访问器支持

`FastBeanCopier` 支持标准 JavaBean 访问器、链式 setter（如 `getName()` / `Bean setName(String)`），以及同名的 fluent 方法对（如 `name()` / `Bean name(String)`，包括继承的方法）。扩展识别仅匹配公开实例方法，getter 返回类型必须与 setter 参数类型一致，避免重载选择依赖反射枚举顺序；已有标准 JavaBean 属性优先。源属性只需可读，只有标准 getter 的只读属性也可复制到 Map、可写 Bean 或 record；目标 Bean 属性仍需可写，不尝试修改只读属性。单独的 fluent 读取方法仍不作为属性识别，静态访问器及参数类型不匹配的方法对不参与复制。

实现位于 `hsweb-core/src/main/java/org/hswebframework/web/bean/FastBeanCopier.java`，使用独立的 `PropertyUtilsBean` 和内省扩展点，不修改全局 BeanUtils 配置，复用现有代码生成、类型转换和缓存。

访问器回归由 `FastBeanCopierBeanTest`、`FastBeanCopierReadOnlyTest` 和 `FastBeanCopierTest` 覆盖，包括只读源属性、目标可写边界、不同访问器风格、继承泛型、忽略属性、record、全局配置隔离及静态访问器排除。

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
