package org.hswebframework.web.authorization.annotation;


import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.define.Phased;

import java.lang.annotation.*;

/**
 * 接口资源声明注解,声明Controller的资源相关信息,用于进行权限控制。
 * <br>
 * 在Controller进行注解,表示此接口需要有对应的权限{@link Permission#getId()}才能进行访问.
 * 具体的操作权限控制,需要在方法上注解{@link ResourceAction}.
 * <br>
 *
 *
 * <pre>{@code
 * @RestController
 * //声明资源
 * @Resource(id = "test", name = "测试功能")
 * public class TestController implements ReactiveCrudController<TestEntity, String> {
 *
 *     //声明操作,需要有 test:query 权限才能访问此接口
 *     @QueryAction
 *     public Mono<User> getUser() {
 *         return Authentication.currentReactive()
 *                 .switchIfEmpty(Mono.error(new UnAuthorizedException()))
 *                 .map(Authentication::getUser);
 *     }
 *
 * }
 * }
 * </pre>
 * 如果接口不需要进行权限控制,可注解{@link Authorize#ignore()}来标识此接口不需要权限控制.
 * 或者通过监听 {@link org.hswebframework.web.authorization.events.AuthorizingHandleBeforeEvent}来进行自定义处理
 * <pre>{@code
 *   @EventListener
 *   public void handleAuthEvent(AuthorizingHandleBeforeEvent e) {
 *      //admin用户可以访问全部操作
 *      if ("admin".equals(e.getContext().getAuthentication().getUser().getUsername())) {
 *         e.setAllow(true);
 *       }
 *    }
 * }</pre>
 *
 * @author zhouhao
 * @see ResourceAction
 * @see Authorize
 * @see org.hswebframework.web.authorization.events.AuthorizingHandleBeforeEvent
 * @since 4.0
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD,ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Resource {

    /**
     * 资源ID
     *
     * @return 资源ID
     */
    String id();

    /**
     * @return 资源名称
     */
    String name();

    /**
     * @return 资源操作定义
     */
    ResourceAction[] actions() default {};

    /**
     * @return 多个操作控制逻辑
     */
    Logical logical() default Logical.DEFAULT;

    /**
     * @return 权限控制阶段
     */
    Phased phased() default Phased.before;

    /**
     * @return 资源描述
     */
    String[] description() default {};

    /**
     * @return 资源分组
     */
    String[] group() default {};

    /**
     * 如果在方法上设置此属性，表示是否合并类上注解的属性
     *
     * @return 是否合并
     */
    boolean merge() default true;
}
