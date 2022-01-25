package org.hswebframework.web.authorization.define;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@AllArgsConstructor
public class CompositeAuthorizeDefinitionCustomizer implements AuthorizeDefinitionCustomizer{

    private final List<AuthorizeDefinitionCustomizer> customizers;

    public CompositeAuthorizeDefinitionCustomizer(Iterable<AuthorizeDefinitionCustomizer> customizers){
        this(StreamSupport.stream(customizers.spliterator(),false).collect(Collectors.toList()));
    }

    @Override
    public void custom(AuthorizeDefinitionContext context) {
        for (AuthorizeDefinitionCustomizer customizer : customizers) {
            customizer.custom(context);
        }
    }
}
