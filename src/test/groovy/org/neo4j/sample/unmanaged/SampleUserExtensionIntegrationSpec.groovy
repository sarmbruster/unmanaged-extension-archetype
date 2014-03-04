package org.neo4j.sample.unmanaged

import org.junit.ClassRule
import org.neo4j.extension.spock.Neo4jServerResource
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.index.Index
import spock.lang.Shared
import spock.lang.Specification

/**
 * short example for a unit test using http://www.spockframework.org
 *
 * N.B. this spawns an ImpermanentGraphDatabase
 */
class SampleUserExtensionIntegrationSpec extends Specification {

    @ClassRule
    @Shared
    Neo4jServerResource neo4j = new Neo4jServerResource(
        thirdPartyJaxRsPackages: ["org.neo4j.sample.unmanaged": "/db"]
    )

    Index<Node> userIndex

    def setup() {
        neo4j.withTransaction {
            userIndex = neo4j.graphDatabaseService.index().forNodes("users")
        }
    }

    def "should trivial test work"() {
        when:
        def response = neo4j.http.GET("db/user/test")

        then:
        response.status()==200
        response.entity=="test"
    }

    def "should return empty collection for empty database"() {
        when:
        def response = neo4j.http.GET("db/user")

        then:
        response.status()==200
        response.entity=="null"
    }

    def "should return a valid user list"() {

        given:
        def stefanNode = createUser("Stefan")
        def thomasNode = createUser("Thomas")

        when:
        def response = neo4j.http.GET("db/user")

        then:
        response.status()==200
        response.entity=='{"user":[{"username":"Stefan"},{"username":"Thomas"}]}'

    }

    Node createUser(String userName) {
        neo4j.withTransaction {
            Node node = neo4j.graphDatabaseService.createNode()
            node.setProperty("username", userName)
            userIndex.add(node, "username", node.getProperty("username"))
            node
        }
    }
}
