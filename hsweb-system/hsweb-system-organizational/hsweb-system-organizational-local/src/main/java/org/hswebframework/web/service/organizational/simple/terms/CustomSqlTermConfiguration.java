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

    //=======================================================================
    @Bean
    public InServiceTreeInSqlTerm<String> distInSqlTerm(DistrictService districtService) {
        return new InServiceTreeInSqlTerm<>(districtService, "按行政区划查询", "dist", "s_district");
    }


    //=======================================================================
    @Bean
    public InServiceTreeInSqlTerm<String> orgInSqlTerm(OrganizationalService organizationalService) {
        return new InServiceTreeInSqlTerm<>(organizationalService, "按机构查询", "org", "s_organization");
    }

    //=======================================================================
    @Bean
    public InServiceTreeInSqlTerm<String> departmentInSqlTerm(DepartmentService departmentService) {
        return new InServiceTreeInSqlTerm<>(departmentService, "按部门查询","dept", "s_department");
    }

    /*====================================================================================*/

    @Bean
    public UserInSqlTerm userInPositionSqlTerm(PositionService positionService) {

        return new UserInPositionSqlTerm("user-in-position", positionService);
    }

    /*====================================================================================*/

    @Bean
    public UserInSqlTerm personInPositionSqlTerm(PositionService positionService) {

        return new UserInPositionSqlTerm("person-in-position", positionService).forPerson();
    }


    /*====================================================================================*/
    @Bean
    public UserInSqlTerm userInDepartmentSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm( "user-in-department", departmentService);
    }

    /*====================================================================================*/
    @Bean
    public UserInSqlTerm personInDepartmentSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm( "person-in-department", departmentService).forPerson();
    }


    /*====================================================================================*/
    @Bean
    public UserInSqlTerm userInOrgSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm("user-in-org", organizationalService);
    }


    /*====================================================================================*/
    @Bean
    public UserInSqlTerm personInOrgSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm( "person-in-org", organizationalService).forPerson();
    }

    /*====================================================================================*/
    @Bean
    public UserInSqlTerm userInDistSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm( "user-in-dist", districtService);
    }

    /*====================================================================================*/
    @Bean
    public UserInSqlTerm personInDistSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm( "person-in-dist", districtService).forPerson();
    }



}
