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

Bean 属性复制每次只读取一次实际参与复制的源属性，空值保持现有跳过写入的语义；include/ignore 排除的属性不读取，getter 抛出的异常继续传播。Bean 转普通 Map 保持浅复制，嵌套的 Throwable 保留原引用，不自动展开、丢弃或转成字符串。

Bean/record 转 Map 时排除 `transient` 字段（包含继承字段）及 JavaBeans `java.beans.Transient` 标记的属性，排除发生在调用 getter 前，显式 include 也不会导出这些属性。`@Transient(false)` 不排除属性，但不能覆盖字段上的 `transient`。此规则不影响 Bean→Bean、Map→Bean、Bean→record、Map→Map 和 `getProperty` 按名称取值。

直接将 Throwable 转 Map（包括 `DEFAULT_CONVERT.convert(error, Map.class, ...)`）使用固定描述字段：`type` 为完整类名，`message` 为异常消息，`detail` 为含 cause/suppressed 的完整堆栈文本；`I18nSupportException` 另提供 `i18nCode`、`args`，`BusinessException` 另提供业务 `code`、`status`。参数保持浅复制及原类型；空值跳过，不再枚举异常的其他 getter 或携带原始 cause/suppressed/stackTrace 对象。include/ignore 在取值前应用，排除 detail 时不生成堆栈，值转换仍使用调用方的 Converter。Map 用于错误描述，异常的真实类型还原不依赖类名反射实例化。

目标声明为 Throwable 体系时，兼容的现成异常直接保留原引用；异常描述 Map 通过 `ExceptionConverter.fromMap` 受控恢复。默认实现要求非空字符串 type；识别 hsweb-core 中 I18nSupportException、BusinessException、NotFoundException、ValidationException 及其 NoStackTrace 类名，恢复为相应 NoStackTrace 实现。未知类型使用 `RemoteException`（BusinessException.NoStackTrace 子类）安全包装；通过 `RemoteExceptionMetadata` 保留远端 type/detail、发送端 message、国际化及业务字段，不按 type 任意加载类或解析 detail。恢复结果必须可赋值给目标声明；其余具体异常或 Error 声明需由 SPI 提供对应结果，否则明确报错。Object/Map 目标不因存在 type 字段而主动恢复。

ValidationException 的 details 以 property/message/detail 字段 Map 列表导出，恢复时保留字段错误及详情；details 中的业务值保持浅复制。已知异常固定的业务 code/status 若与描述冲突会明确拒绝，不静默丢失。鉴权等上层模块的异常由其 SPI 实现扩展，hsweb-core 不反向依赖这些模块。

还原支持本地 Object[] 与 JSON List 形式的 args，复制参数容器并保留参数值类型；status 必须是 int 范围内的整数。接收端无对应译文时使用发送端 message 回退。恢复后的异常再次转 Map 时沿用原远端 type/detail，不生成本地替代栈；缺省字段保持缺省。`copy(map, Throwable.class)` 与 `copy(map, existingThrowable)` 采用同一恢复路径，后者返回新异常，调用方必须使用返回值；复制到普通 Bean/record 的异常字段也使用该转换规则。

可在 `META-INF/services/org.hswebframework.web.bean.ExceptionConverter` 注册自定义双向实现（`toMap` / `fromMap`）。首次异常转换时使用线程上下文 ClassLoader 发现的首个实现，每个运行环境应只注册一个；无实现时回退 `DefaultExceptionConverter`。实例按 copier 的 ClassLoader 复用，自定义实现应无状态或线程安全。toMap 按字段提供延迟 Supplier，由 copier 统一过滤、读取及转换；继承默认实现并覆盖 `getDetail(Throwable)` 可精简原始异常的栈，自定义栈处理需保留循环引用处理。携带 RemoteExceptionMetadata 的异常直接沿用远端 detail，不再调用此栈渲染方法。fromMap 应返回与目标声明兼容的异常，copier 拒绝 null 或不兼容的 SPI 返回值。

访问器回归由 `FastBeanCopierBeanTest`、`FastBeanCopierReadOnlyTest`、`FastBeanCopierReadOnceTest`、`FastBeanCopierTransientTest` 和 `FastBeanCopierTest` 覆盖，包括只读源属性、单次取值、导出排除规则、目标可写边界、不同访问器风格、继承泛型、忽略属性、record、全局配置隔离及静态访问器排除。`FastBeanCopierExceptionTest`、`FastBeanCopierExceptionRestoreTest`、`FastBeanCopierExceptionSpiTest` 及 `RemoteExceptionI18nTest` 覆盖异常描述、循环 cause、受控还原、JSON 往返、国际化回退和重译、值转换及双向 SPI 发现。

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

`EnumDict.isEnabled()` 表示选项是否允许新选择，静态枚举默认 true。动态字典项通过已有 status（1 启用、0 禁用）实现；可通过 DictDefineRepository 统一读取，无需依赖字典内部 Service。该标志不改变字典值、ordinal、已有序列化或历史值解码，也不会自动在所有消费者中新增过滤。采用字段级稳定编码的消费者应自行保存映射，不将可变化的字典 ordinal 当作持久化位位置。

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
