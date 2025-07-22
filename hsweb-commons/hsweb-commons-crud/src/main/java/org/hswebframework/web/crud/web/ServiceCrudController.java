package org.hswebframework.web.crud.web;

/**
 * 基于{@link org.hswebframework.web.crud.service.CrudService}的通用增删改查Controller模版接口,
 * 通过实现此接口,即可支持对应的增删改查功能.
 *
 * <pre>{@code
 * @RestController
 * @RequestMapping("/example/crud")
 * @AllArgsConstructor
 * @Getter
 * @Resource(id = "example", name = "增删改查演示")
 * @Tag(name = "增删改查演示")
 * public class ExampleController implements ServiceCrudController<ExampleEntity, String> {
 *
 *     private final ExampleService service;
 *
 *
 * }
 * }</pre>
 *
 * @param <E> 实体类型
 * @param <K> 主键类型
 * @author zhouhao
 * @see org.springframework.web.bind.annotation.RestController
 * @see org.springframework.web.bind.annotation.RequestMapping
 * @see ServiceSaveController
 * @see ServiceQueryController
 * @see ServiceDeleteController
 * @since 3.0
 */
public interface ServiceCrudController<E, K> extends
    ServiceSaveController<E, K>,
    ServiceQueryController<E, K>,
    ServiceDeleteController<E, K> {
}
