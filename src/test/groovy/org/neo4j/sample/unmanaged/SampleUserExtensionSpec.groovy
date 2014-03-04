package org.neo4j.sample.unmanaged

import org.junit.Rule
import org.neo4j.extension.spock.Neo4jResource
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Transaction
import org.neo4j.graphdb.index.Index
import org.neo4j.kernel.impl.util.TestLogger
import org.neo4j.server.database.CypherExecutor
import org.neo4j.server.database.WrappedDatabase
import spock.lang.Specification

/**
 * short example for a unit test using http://www.spockframework.org
 *
 * N.B. this spawns an ImpermanentGraphDatabase
 */
class SampleUserExtensionSpec extends Specification
{

    @Rule
    @Delegate
    Neo4jResource neo4jResource = new Neo4jResource()

    SampleUserExtension cut
    Index<org.neo4j.graphdb.Node> userIndex
    Transaction transaction

    def setup( )
    {
        cut = new SampleUserExtension(
                graphDatabaseService: graphDatabaseService,
                cypherExecutor: new CypherExecutor(
                        new WrappedDatabase(graphDatabaseService), new TestLogger()
                ))

        transaction = graphDatabaseService.beginTx()
        userIndex = graphDatabaseService.index().forNodes( "users" )
    }

    def cleanup() {
        transaction.close()
    }

    def "should return empty collection for empty database"( )
    {
        when:
        def result = cut.getUsernames()

        then:
        result.empty
    }

    def "should return a valid user list"( )
    {

        given:
        def stefanNode = createUser( "Stefan" )
        def thomasNode = createUser( "Thomas" )

        when:
        def result = cut.getUsernames()

        then:
        result.size() == 2
        result.collect { it.username }.containsAll( "Stefan", "Thomas" )
//        result.collect {it.id}.containsAll(stefanNode.id, thomasNode.id)

    }


    def "sample test with table data"( )
    {

        when:
        def node = graphDatabaseService.createNode()
        props.each { k,v -> node.setProperty(k,v)}

        then:
        node.propertyKeys.size() == expectation


        where:
        props | expectation
        [ name: "abc" ] | 1
        [ name:"abc", phone: 112] | 2
    }

    Node createUser( String userName )
    {
        Node node = graphDatabaseService.createNode()
        node.setProperty( "username", userName )
        userIndex.add( node, "username", node.getProperty( "username" ) )
        node
    }
}
