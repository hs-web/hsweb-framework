package org.hswebframework.web.service.organizational.simple.relations;

import lombok.Getter;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.service.organizational.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ServiceContext {

    @Autowired
    private PersonService personService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private OrganizationalService organizationalService;

    @Autowired
    private RelationInfoService relationInfoService;

    @Autowired
    private RelationDefineService relationDefineService;

}
