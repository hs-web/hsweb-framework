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
        return new InServiceTreeInSqlTerm<>(districtService, "dist", "s_district", false, false);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> distInSqlTermParent(DistrictService districtService) {
        return new InServiceTreeInSqlTerm<>(districtService, "dist", "s_district", false, true);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> distNotInSqlTerm(DistrictService districtService) {
        return new InServiceTreeInSqlTerm<>(districtService, "dist", "s_district", true, false);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> distNotInSqlTermParent(DistrictService districtService) {
        return new InServiceTreeInSqlTerm<>(districtService, "dist", "s_district", true, true);
    }

    //=======================================================================
    @Bean
    public InServiceTreeInSqlTerm<String> orgInSqlTerm(OrganizationalService organizationalService) {
        return new InServiceTreeInSqlTerm<>(organizationalService, "org", "s_organization", false, false);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> orgNotInSqlTerm(OrganizationalService organizationalService) {
        return new InServiceTreeInSqlTerm<>(organizationalService, "org", "s_organization", true, false);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> orgInSqlTermParent(OrganizationalService organizationalService) {
        return new InServiceTreeInSqlTerm<>(organizationalService, "org", "s_organization", false, true);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> orgNotInSqlTermParent(OrganizationalService organizationalService) {
        return new InServiceTreeInSqlTerm<>(organizationalService, "org", "s_organization", true, true);
    }

    //=======================================================================
    @Bean
    public InServiceTreeInSqlTerm<String> departmentInSqlTerm(DepartmentService departmentService) {
        return new InServiceTreeInSqlTerm<>(departmentService, "dept", "s_department", false, false);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> departmentNotInSqlTerm(DepartmentService departmentService) {
        return new InServiceTreeInSqlTerm<>(departmentService, "dept", "s_department", true, false);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> departmentInSqlTermParent(DepartmentService departmentService) {
        return new InServiceTreeInSqlTerm<>(departmentService, "dept", "s_department", false, true);
    }

    @Bean
    public InServiceTreeInSqlTerm<String> departmentNotInSqlTermParent(DepartmentService departmentService) {
        return new InServiceTreeInSqlTerm<>(departmentService, "dept", "s_department", true, true);
    }


    /*====================================================================================*/

    @Bean
    public UserInSqlTerm userInPositionSqlTerm(PositionService positionService) {

        return new UserInPositionSqlTerm(false, false, "user-in-position", positionService);
    }

    @Bean
    public UserInSqlTerm userNotInPositionSqlTerm(PositionService positionService) {

        return new UserInPositionSqlTerm(true, false, "user-not-in-position", positionService);
    }

    @Bean
    public UserInSqlTerm userInPositionChildSqlTerm(PositionService positionService) {

        return new UserInPositionSqlTerm(false, true, "user-in-position-child", positionService).forChild();
    }

    @Bean
    public UserInSqlTerm userNotInPositionChildSqlTerm(PositionService positionService) {

        return new UserInPositionSqlTerm(true, true, "user-not-in-position-child", positionService).forChild();
    }

    @Bean
    public UserInSqlTerm userInPositionParentSqlTerm(PositionService positionService) {

        return new UserInPositionSqlTerm(false, true, "user-in-position-parent", positionService).forParent();
    }

    @Bean
    public UserInSqlTerm userNotInPositionParentSqlTerm(PositionService positionService) {
        return new UserInPositionSqlTerm(true, true, "user-not-in-position-parent", positionService).forParent();
    }
 /*====================================================================================*/

    @Bean
    public UserInSqlTerm personInPositionSqlTerm(PositionService positionService) {

        return new UserInPositionSqlTerm(false, false, "person-in-position", positionService).forPerson();
    }

    @Bean
    public UserInSqlTerm personNotInPositionSqlTerm(PositionService positionService) {

        return new UserInPositionSqlTerm(true, false, "person-not-in-position", positionService).forPerson();
    }

    @Bean
    public UserInSqlTerm personInPositionChildSqlTerm(PositionService positionService) {

        return new UserInPositionSqlTerm(false, true, "person-in-position-child", positionService).forPerson();
    }

    @Bean
    public UserInSqlTerm personNotInPositionChildSqlTerm(PositionService positionService) {

        return new UserInPositionSqlTerm(true, true, "person-not-in-position-child", positionService).forPerson();
    }

    @Bean
    public UserInSqlTerm personInPositionParentSqlTerm(PositionService positionService) {

        return new UserInPositionSqlTerm(false, true, "person-in-position-parent", positionService).forPerson().forParent();
    }

    @Bean
    public UserInSqlTerm personNotInPositionParentSqlTerm(PositionService positionService) {

        return new UserInPositionSqlTerm(true, true, "person-not-in-position-parent", positionService).forPerson().forParent();
    }

    /*====================================================================================*/
    @Bean
    public UserInSqlTerm userInDepartmentSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm(false, false, "user-in-department", departmentService);
    }

    @Bean
    public UserInSqlTerm userNotInDepartmentSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm(true, false, "user-not-in-department", departmentService);
    }

    @Bean
    public UserInSqlTerm userInDepartmentChildSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm(false, true, "user-in-department-child", departmentService).forChild();
    }

    @Bean
    public UserInSqlTerm userNotInDepartmentChildSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm(true, true, "user-not-in-department-child", departmentService).forChild();
    }


    @Bean
    public UserInSqlTerm userInDepartmentParentSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm(false, true, "user-in-department-parent", departmentService).forParent();
    }

    @Bean
    public UserInSqlTerm userNotInDepartmentParentSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm(true, true, "user-not-in-department-parent", departmentService).forParent();
    }

    /*====================================================================================*/
    @Bean
    public UserInSqlTerm personInDepartmentSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm(false, false, "person-in-department", departmentService).forPerson();
    }

    @Bean
    public UserInSqlTerm personNotInDepartmentSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm(true, false, "person-not-in-department", departmentService).forPerson();
    }

    @Bean
    public UserInSqlTerm personInDepartmentChildSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm(false, true, "person-in-department-child", departmentService).forPerson();
    }

    @Bean
    public UserInSqlTerm personNotInDepartmentChildSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm(true, true, "person-not-in-department-child", departmentService).forPerson();
    }

    @Bean
    public UserInSqlTerm personInDepartmentParentSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm(false, true, "person-in-department-parent", departmentService)
                .forPerson()
                .forParent();
    }

    @Bean
    public UserInSqlTerm personNotInDepartmentParentSqlTerm(DepartmentService departmentService) {

        return new UserInDepartmentSqlTerm(true, true, "person-not-in-department-parent", departmentService)
                .forPerson()
                .forParent();
    }


    /*====================================================================================*/
    @Bean
    public UserInSqlTerm userInOrgSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm(false, false, "user-in-org", organizationalService);
    }

    @Bean
    public UserInSqlTerm userNotInOrgSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm(true, false, "user-not-in-org", organizationalService);
    }

    @Bean
    public UserInSqlTerm userInOrgChildSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm(false, true, "user-in-org-child", organizationalService);
    }

    @Bean
    public UserInSqlTerm userNotInOrgChildSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm(true, true, "user-not-in-org-child", organizationalService);
    }


    @Bean
    public UserInSqlTerm userInOrgParentSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm(false, true, "user-in-org-parent", organizationalService).forParent();
    }

    @Bean
    public UserInSqlTerm userNotInOrgParentSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm(true, true, "user-not-in-org-parent", organizationalService).forParent();
    }


    /*====================================================================================*/
    @Bean
    public UserInSqlTerm personInOrgSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm(false, false, "person-in-org", organizationalService).forPerson();
    }

    @Bean
    public UserInSqlTerm personNotInOrgSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm(true, false, "person-not-in-org", organizationalService).forPerson();
    }

    @Bean
    public UserInSqlTerm personInOrgChildSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm(false, true, "person-in-org-child", organizationalService).forPerson();
    }

    @Bean
    public UserInSqlTerm personNotInOrgChildSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm(true, true, "person-not-in-org-child", organizationalService).forPerson();
    }

    @Bean
    public UserInSqlTerm personInOrgParentSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm(false, true, "person-in-org-parent", organizationalService).forPerson().forParent();
    }

    @Bean
    public UserInSqlTerm personNotInOrgParentSqlTerm(OrganizationalService organizationalService) {

        return new UserInOrgSqlTerm(true, true, "person-not-in-org-parent", organizationalService).forPerson().forParent();
    }

    /*====================================================================================*/
    @Bean
    public UserInSqlTerm userInDistSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm(false, false, "user-in-dist", districtService);
    }

    @Bean
    public UserInSqlTerm userNotInDistSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm(true, false, "user-not-in-dist", districtService);
    }

    @Bean
    public UserInSqlTerm userInDistChildSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm(false, true, "user-in-dist-child", districtService);
    }

    @Bean
    public UserInSqlTerm userNotInDistChildSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm(true, true, "user-not-in-dist-child", districtService);
    }

    @Bean
    public UserInSqlTerm userInDistParentSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm(false, true, "user-in-dist-parent", districtService).forParent();
    }

    @Bean
    public UserInSqlTerm userNotInDistParentSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm(true, true, "user-not-in-dist-parent", districtService).forParent();
    }

    /*====================================================================================*/
    @Bean
    public UserInSqlTerm personInDistSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm(false, false, "person-in-dist", districtService).forPerson();
    }

    @Bean
    public UserInSqlTerm personNotInDistSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm(true, false, "person-not-in-dist", districtService).forPerson();
    }

    @Bean
    public UserInSqlTerm personInDistChildSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm(false, true, "person-in-dist-child", districtService).forPerson();
    }

    @Bean
    public UserInSqlTerm personNotInDistChildSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm(true, true, "person-not-in-dist-child", districtService).forPerson();
    }

    @Bean
    public UserInSqlTerm personInDistParentSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm(false, true, "person-in-dist-parent", districtService).forPerson().forParent();
    }

    @Bean
    public UserInSqlTerm personNotInDistParentSqlTerm(DistrictService districtService) {

        return new UserInDistSqlTerm(true, true, "person-not-in-dist-parent", districtService).forPerson().forParent();
    }


}
