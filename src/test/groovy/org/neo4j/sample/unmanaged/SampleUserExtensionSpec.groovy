package org.neo4j.sample.unmanaged

import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.Node
import org.neo4j.test.Neo4jSpecification

/**
 * short example for a unit test using http://www.spockframework.org
 *
 * N.B. this spawns an ImpermanentGraphDatabase
 */
class SampleUserExtensionSpec extends Neo4jSpecification {

    SampleUserExtension cut
    Index<org.neo4j.graphdb.Node> userIndex

    def setup() {
        cut = new SampleUserExtension(graphDatabaseService: graphDatabaseService, cypherExecutor: cypherExecutor)
        userIndex = graphDatabaseService.index().forNodes("users")
    }

    def "should return empty collection for empty database"() {
        when:
        def result = cut.getUsernames()

        then:
        result.empty
    }

    def "should return a valid user list"() {

        given:
        def stefanNode = createUser("Stefan")
        def thomasNode = createUser("Thomas")

        when:
        def result = cut.getUsernames()

        then:
        result.size() == 2
        result.collect {it.username}.containsAll("Stefan", "Thomas")
//        result.collect {it.id}.containsAll(stefanNode.id, thomasNode.id)

    }

    Node createUser(String userName) {
        Node node = graphDatabaseService.createNode()
        node.setProperty("username", userName)
        userIndex.add(node, "username", node.getProperty("username"))
        node
    }
}
