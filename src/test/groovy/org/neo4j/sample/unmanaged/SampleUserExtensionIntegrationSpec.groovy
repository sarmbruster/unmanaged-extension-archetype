package org.neo4j.sample.unmanaged

import org.neo4j.graphdb.Node
import org.neo4j.graphdb.index.Index
import org.neo4j.test.Neo4jServerSpecification
import org.neo4j.test.Neo4jSpecification
import spock.lang.Ignore
import spock.lang.Shared

/**
 * short example for a unit test using http://www.spockframework.org
 *
 * N.B. this spawns an ImpermanentGraphDatabase
 */
class SampleUserExtensionIntegrationSpec extends Neo4jServerSpecification {

    @Shared
    Index<Node> userIndex

    //String MOUNTPOINT = "/db"

    @Override
    Map<String, String> jaxRsPackagesAndMountpoints() {
        ["org.neo4j.sample.unmanaged": "/db", ]
    }

    def setupSpec() {
        userIndex = graphDatabaseService.index().forNodes("users")
    }

    def "should return empty collection for empty database"() {
        when:
        def response = request.get("user")

        then:
        response.status==200
        response.entity=="null"
    }

    def "should return a valid user list"() {

        given:
        def stefanNode = createUser("Stefan")
        def thomasNode = createUser("Thomas")

        when:
        def response = request.get("user")

        then:
        response.status==200
        response.entity=='{"user":[{"username":"Stefan"},{"username":"Thomas"}]}'

    }

    Node createUser(String userName) {
        withTransaction {
            Node node = graphDatabaseService.createNode()
            node.setProperty("username", userName)
            userIndex.add(node, "username", node.getProperty("username"))
            node
        }
    }
}
