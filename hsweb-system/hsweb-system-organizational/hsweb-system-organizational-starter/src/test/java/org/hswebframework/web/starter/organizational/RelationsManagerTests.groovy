package org.hswebframework.web.starter.organizational

import org.hswebframework.web.bean.FastBeanCopier
import org.hswebframework.web.entity.organizational.SimpleDepartmentEntity
import org.hswebframework.web.entity.organizational.SimpleOrganizationalEntity
import org.hswebframework.web.entity.organizational.SimplePersonAuthBindEntity
import org.hswebframework.web.entity.organizational.SimplePositionEntity
import org.hswebframework.web.organizational.authorization.relation.PersonRelations
import org.hswebframework.web.organizational.authorization.relation.Relation
import org.hswebframework.web.organizational.authorization.relation.RelationsManager
import org.hswebframework.web.service.organizational.simple.relations.ServiceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import spock.lang.Specification

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@WebAppConfiguration
@ContextConfiguration
@SpringBootTest(classes = [TestApplication.class], properties = ["classpath:application.yml"])
class RelationsManagerTests extends Specification {

    @Autowired
    RelationsManager relationsManager;

    @Autowired
    ServiceContext serviceContext;

    def createData() {
        def orgData = [
                id      : "10001",
                name    : "总公司",
                code    : "000001",
                children: [
                        [id: "10002", name: "重庆分公司", code: "0000011"],
                        [id: "10003", name: "北京分公司", code: "0000012"]
                ]
        ]
        def departmentData = [
                [
                        id      : "100010001",
                        name    : "人事部",
                        code    : "100010001",
                        orgId   : "10001",
                        position: [

                                [
                                        id      : "1",
                                        name    : "总监",
                                        children:
                                                [[
                                                         id          : "10001",
                                                         name        : "助理",
                                                         departmentId: "100010001"
                                                 ]]
                                ], [
                                        id  : "2",
                                        name: "人事专员"
                                ]

                        ]
                ],
                [
                        id      : "100020001",
                        name    : "研发部",
                        code    : "100020001",
                        orgId   : "10002",
                        position: [
                                [
                                        id      : "3",
                                        name    : "经理",
                                        children:
                                                [[
                                                         id          : "3001",
                                                         name        : "助理",
                                                         departmentId: "100020001"
                                                 ]]
                                ], [
                                        id  : "4",
                                        name: "技术人员"
                                ]

                        ]
                ],
                [
                        id      : "100030001",
                        name    : "研发部",
                        code    : "100030001",
                        orgId   : "10003",
                        position: [
                                [
                                        id      : "5",
                                        name    : "经理",
                                        children:
                                                [[
                                                         id          : "5001",
                                                         name        : "助理",
                                                         departmentId: "100030001"
                                                 ]]
                                ], [
                                        id  : "6",
                                        name: "技术人员"
                                ]

                        ]
                ]
        ]

        def personData = [
                [
                        id         : "1",
                        name       : "张三",
                        positionIds: ["1"]
                ],
                [
                        id         : "2",
                        name       : "李四",
                        positionIds: ["10001"]
                ],
                [
                        id         : "3",
                        name       : "王五",
                        positionIds: ["3"]
                ],
                [
                        id         : "4",
                        name       : "赵六",
                        positionIds: ["4"]
                ],
                [
                        id         : "5",
                        name       : "周七",
                        positionIds: ["2"]
                ],
                [
                        id         : "6",
                        name       : "宋九",
                        positionIds: ["2"]
                ]
        ]
        serviceContext.getOrganizationalService()
                .insert(FastBeanCopier.copy(orgData, new SimpleOrganizationalEntity()));
        departmentData.forEach({ department ->
            serviceContext.getDepartmentService().insert(FastBeanCopier.copy(department, new SimpleDepartmentEntity()));
            department.position.forEach({ position ->
                position.departmentId = department.id;
                serviceContext.getPositionService().insert(FastBeanCopier.copy(position, new SimplePositionEntity()));
            })
        })
        personData.forEach({ person ->
            serviceContext.getPersonService().insert(FastBeanCopier.copy(person, new SimplePersonAuthBindEntity()))
        })
    }

    def setup() {
        expect:
        relationsManager != null

    }

    def "Test"() {
        setup:
        createData()
        and:
        def me = relationsManager.getPersonRelationsByPersonId("2")
        def pre = relationsManager.getPersonRelationsByPersonId("4")

        def relationList = me
                .department()
                .relations("总监")
                .all()

        def orgRelationList = pre
                .org()
                .andParents()
                .department()
                .relations("人事专员")
                .all()

        expect:
        relationList != null
        !relationList.isEmpty()
        orgRelationList != null
        !orgRelationList.isEmpty()
        println relationList
        println orgRelationList
    }

}
