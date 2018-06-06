package org.hswebframework.web.service.organizational.simple.terms;

import org.hswebframework.web.service.organizational.DepartmentService;
import org.hswebframework.web.service.organizational.DistrictService;
import org.hswebframework.web.service.organizational.OrganizationalService;
import org.hswebframework.web.service.organizational.PositionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Configuration
public class CustomSqlTermConfiguration {

    @Bean
    public InServiceTreeInSqlTerm<String> distInSqlTerm(DistrictService districtService) {
        return new InServiceTreeInSqlTerm<>(districtService, "dist", "s_district", false);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> distNotInSqlTerm(DistrictService districtService) {
        return new InServiceTreeInSqlTerm<>(districtService, "dist", "s_district", true);
    }


    @Bean
    public InServiceTreeInSqlTerm<String> orgInSqlTerm(OrganizationalService organizationalService) {
        return new InServiceTreeInSqlTerm<>(organizationalService, "org", "s_organization", false);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> orgNotInSqlTerm(OrganizationalService organizationalService) {
        return new InServiceTreeInSqlTerm<>(organizationalService, "org", "s_organization", true);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> departmentInSqlTerm(DepartmentService departmentService) {
        return new InServiceTreeInSqlTerm<>(departmentService, "dept", "s_department", false);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> departmentNotInSqlTerm(DepartmentService departmentService) {
        return new InServiceTreeInSqlTerm<>(departmentService, "dept", "s_department", true);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> positionInSqlTerm(PositionService positionService) {
        return new InServiceTreeInSqlTerm<>(positionService, "pos", "s_position", false);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> positionNotInSqlTerm(PositionService positionService) {
        return new InServiceTreeInSqlTerm<>(positionService, "pos", "s_position", true);
    }

    @Bean
    public PersonInPositionSqlTerm personInPositionSqlTerm() {
        return new PersonInPositionSqlTerm(false);
    }

    @Bean
    public PersonInPositionSqlTerm personNotInPositionSqlTerm() {
        return new PersonInPositionSqlTerm(true);
    }

    @Bean
    public UserInPositionSqlTerm userInPositionSqlTerm() {
        return new UserInPositionSqlTerm(false);
    }

    @Bean
    public UserInPositionSqlTerm userNotInPositionSqlTerm() {
        return new UserInPositionSqlTerm(true);
    }


}
