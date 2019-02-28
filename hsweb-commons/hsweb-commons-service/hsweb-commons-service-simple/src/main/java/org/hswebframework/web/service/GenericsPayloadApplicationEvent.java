package org.hswebframework.web.service;

import org.springframework.context.PayloadApplicationEvent;
import org.springframework.core.ResolvableType;

/**
 * 动态泛型事件,用于动态发布支持泛型的事件
 * <pre>
 *     //相当于发布事件: EntityModifyEvent&lt;UserEntity&gt;
 *     eventPublisher
 *          .publishEvent(new GenericsPayloadApplicationEvent&lt;&gt;(this, new EntityModifyEvent<>(oldEntity, newEntity), UserEntity.class));
 *
 *      //只监听相同泛型事件
 *      &#064;EventListener
 *      public handleEvent(EntityModifyEvent&lt;UserEntity&gt; event){
 *
 *      }
 * </pre>
 *
 * @author zhouhao
 * @since 3.0.7
 */
public class GenericsPayloadApplicationEvent<E> extends PayloadApplicationEvent<E> {

    private static final long serialVersionUID = 3745888943307798710L;

    //泛型列表
    private transient Class[] generics;

    //事件类型
    private transient Class eventType;

    /**
     * @param source   事件源
     * @param payload  事件,不能使用匿名内部类
     * @param generics 泛型列表
     */
    public GenericsPayloadApplicationEvent(Object source, E payload, Class... generics) {
        super(source, payload);
        this.generics = generics;
        this.eventType = payload.getClass();
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(PayloadApplicationEvent.class
                , ResolvableType.forClassWithGenerics(eventType, generics));
    }
}
