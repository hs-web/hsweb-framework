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

