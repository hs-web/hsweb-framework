package org.hswebframework.web.workflow.dimension

import org.hswebframework.web.workflow.flowable.TestApplication
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
class DefaultCandidateDimensionParserTest extends Specification {

    @Autowired
    private CandidateDimensionParser parser;

    def "Test Parse User"() {
        setup:
        def config = """ {"dimension":"user","idList":["admin"]} """
        and:
        def dimension = parser.parse(null,config)
        expect:
        dimension != null
        dimension.getCandidateUserIdList() != null
        !dimension.getCandidateUserIdList().isEmpty()
        dimension.getCandidateUserIdList().get(0) == "admin"
    }

    def "Test Parse Role"() {
        setup:
        def config = """ {"dimension":"role","idList":["admin"]} """
        and:
        def dimension = parser.parse(null,config)
        expect:
        dimension != null
        dimension.getCandidateUserIdList() != null
        dimension != CandidateDimension.empty
    }


    def "Test Parse Position"() {
        setup:
        def config = """ {"dimension":"position","idList":["test"],"tree":"parent"} """
        and:
        def dimension = parser.parse(null,config)
        expect:
        dimension != null
        dimension.getCandidateUserIdList() != null
        dimension != CandidateDimension.empty
    }

    def "Test Parse Department"() {
        setup:
        def config = """ {"dimension":"department","idList":["test"]} """
        and:
        def dimension = parser.parse(null,config)
        expect:
        dimension != null
        dimension.getCandidateUserIdList() != null
        dimension != CandidateDimension.empty
    }

    def "Test Parse Org"() {
        setup:
        def config = """ {"dimension":"org","idList":["test"]} """
        and:
        def dimension = parser.parse(null,config)
        expect:
        dimension != null
        dimension.getCandidateUserIdList() != null
        dimension != CandidateDimension.empty
    }
}
