package org.hswebframework.web.workflow.terms;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Configuration
public class WorkflowTermsAutoConfiguration {

    @Bean
    public ClaimSqlTerm claimSqlTerm() {
        return new ClaimSqlTerm("user-wf-claim");
    }

    @Bean
    public CompletedSqlTerm completedSqlTerm() {
        return new CompletedSqlTerm("user-wf-completed");
    }

    @Bean
    public ProcessParticipateSqlTerm participateSqlTerm() {
        return new ProcessParticipateSqlTerm("user-wf-part");
    }

    @Bean
    public TodoSqlTerm todoSqlTerm() {
        return new TodoSqlTerm("user-wf-todo");
    }

}
