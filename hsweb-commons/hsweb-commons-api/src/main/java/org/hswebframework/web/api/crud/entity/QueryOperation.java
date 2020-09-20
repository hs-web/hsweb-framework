package org.hswebframework.web.api.crud.entity;


import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;
import org.hswebframework.ezorm.core.param.Term;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;

@Target({METHOD, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation
public @interface QueryOperation {

    /**
     * The HTTP method for this operation.
     *
     * @return the HTTP method of this operation
     **/
    @AliasFor(annotation = Operation.class)
    String method() default "";

    /**
     * Tags can be used for logical grouping of operations by resources or any other qualifier.
     *
     * @return the list of tags associated with this operation
     **/
    @AliasFor(annotation = Operation.class)
    String[] tags() default {};

    /**
     * Provides a brief description of this operation. Should be 120 characters or less for proper visibility in Swagger-UI.
     *
     * @return a summary of this operation
     **/
    @AliasFor(annotation = Operation.class)
    String summary() default "";

    /**
     * A verbose description of the operation.
     *
     * @return a description of this operation
     **/
    @AliasFor(annotation = Operation.class)
    String description() default "";

    /**
     * Request body associated to the operation.
     *
     * @return a request body.
     */
    @AliasFor(annotation = Operation.class)
    RequestBody requestBody() default @RequestBody();

    /**
     * Additional external documentation for this operation.
     *
     * @return additional documentation about this operation
     **/
    @AliasFor(annotation = Operation.class)
    ExternalDocumentation externalDocs() default @ExternalDocumentation();

    /**
     * The operationId is used by third-party tools to uniquely identify this operation.
     *
     * @return the ID of this operation
     **/
    @AliasFor(annotation = Operation.class)
    String operationId() default "";

    /**
     * An optional array of parameters which will be added to any automatically detected parameters in the method itself.
     *
     * @return the list of parameters for this operation
     **/
    @AliasFor(annotation = Operation.class)
    Parameter[] parameters() default {
            @Parameter(name = "pageSize", description = "每页数量", schema = @Schema(implementation = Integer.class), in = ParameterIn.QUERY),
            @Parameter(name = "pageIndex", description = "页码", schema = @Schema(implementation = Integer.class), in = ParameterIn.QUERY),
            @Parameter(name = "total", description = "设置了此值后将不重复执行count查询总数", schema = @Schema(implementation = Integer.class), in = ParameterIn.QUERY),
            @Parameter(name = "where", description = "条件表达式,和terms参数冲突", example = "id = 1", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "orderBy", description = "排序表达式,和sorts参数冲突", example = "id desc", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "includes", description = "指定要查询的列,多列使用逗号分隔", example = "id", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "excludes", description = "指定不查询的列,多列使用逗号分隔",  schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "terms[0].column", description = "指定条件字段", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "terms[0].termType", description = "条件类型", schema = @Schema(implementation = String.class), example = "like", in = ParameterIn.QUERY),
            @Parameter(name = "terms[0].type", description = "多个条件组合方式", schema = @Schema(implementation = Term.Type.class), in = ParameterIn.QUERY),
            @Parameter(name = "terms[0].value", description = "条件值", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "sorts[0].name", description = "排序字段", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
            @Parameter(name = "sorts[0].order", description = "顺序,asc或者desc", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
    };

    /**
     * The list of possible responses as they are returned from executing this operation.
     *
     * @return the list of responses for this operation
     **/
    @AliasFor(annotation = Operation.class)
    ApiResponse[] responses() default {};

    /**
     * Allows an operation to be marked as deprecated.  Alternatively use the @Deprecated annotation
     *
     * @return whether or not this operation is deprecated
     **/
    @AliasFor(annotation = Operation.class)
    boolean deprecated() default false;

    /**
     * A declaration of which security mechanisms can be used for this operation.
     *
     * @return the array of security requirements for this Operation
     */
    @AliasFor(annotation = Operation.class)
    SecurityRequirement[] security() default {};

    /**
     * An alternative server array to service this operation.
     *
     * @return the list of servers hosting this operation
     **/
    @AliasFor(annotation = Operation.class)
    Server[] servers() default {};

    /**
     * The list of optional extensions
     *
     * @return an optional array of extensions
     */
    @AliasFor(annotation = Operation.class)
    Extension[] extensions() default {};

    /**
     * Allows this operation to be marked as hidden
     *
     * @return whether or not this operation is hidden
     */
    @AliasFor(annotation = Operation.class)
    boolean hidden() default false;

    /**
     * Ignores JsonView annotations while resolving operations and types.
     *
     * @return whether or not to ignore JsonView annotations
     */
    @AliasFor(annotation = Operation.class)
    boolean ignoreJsonView() default false;

}
