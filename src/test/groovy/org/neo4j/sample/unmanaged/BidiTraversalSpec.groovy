package org.neo4j.sample.unmanaged

import org.junit.Rule
import org.neo4j.extension.spock.Neo4jResource
import org.neo4j.graphalgo.GraphAlgoFactory
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.DynamicRelationshipType
import org.neo4j.graphdb.Path
import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.traversal.BranchState
import org.neo4j.graphdb.traversal.Evaluation
import org.neo4j.graphdb.traversal.Evaluators
import org.neo4j.graphdb.traversal.PathEvaluator
import org.neo4j.kernel.Uniqueness
import org.neo4j.tooling.GlobalGraphOperations
import spock.lang.Specification

import static org.neo4j.helpers.collection.IteratorUtil.count
import static org.neo4j.kernel.Traversal.*

class BidiTraversalSpec extends Specification
{

    @Rule
    @Delegate
    Neo4jResource neo4j = new Neo4jResource()

    RelationshipType contact = DynamicRelationshipType.withName( "contact" )
    RelationshipType employee = DynamicRelationshipType.withName( "employee" )

    def shouldShortestPath( )
    {
        setup:
        createGraph( "me contact c00", "c00 contact c10", "c10 contact co",
                "c00 contact c11", "c11 contact co",
                "c00 contact c12",
                "me contact c01", "c01 contact c13", "c13 contact co",
                "me contact c02", "c02 contact c20", "c20 contact co2",
        );

        when:
        def finder = GraphAlgoFactory.shortestPath( pathExpanderForTypes( contact ), maxDepth )
        def all = finder.findAllPaths( getNodeWithName( "me" ), getNodeWithName( "co" ) )

//        println all.collect { it.nodes()*.getProperty("name") }

        then:
        all.size() == expectedResults

        where:
        maxDepth | expectedResults
        10       | 3
        3        | 3
        2        | 0
    }

    def "path with fixed length"( )
        {
            setup:
            createGraph( "me contact c00", "c00 contact c10", "c10 contact co",
                    "c00 contact c11", "c11 contact co",
                    "c00 contact c12",
                    "me contact c01", "c01 contact c13", "c13 contact co",
                    "me contact c02", "c02 contact c20", "c20 contact co2",
            );

            when:
            def finder = GraphAlgoFactory.pathsWithLength( pathExpanderForTypes( contact ), depth )
            def all = finder.findAllPaths( getNodeWithName( "me" ), getNodeWithName( "co" ) )

    //        println all.collect { it.nodes()*.getProperty("name") }

            then:
            all.size() == expectedResults

            where:
            depth | expectedResults
            3     | 3
            2     | 0
        }


    def shouldSimpleBidiWork( )
    {
        setup:
        createGraph( "me contact c00", "c00 contact c10", "c10 employee co",
                "c00 contact c11", "c11 employee co",
                "c00 contact c12",
                "me contact c01", "c01 contact c13", "c13 employee co",
                "me contact c02", "c02 contact c20", "c20 employee co2",
        );

        //dumpGraph()

        when:

        org.neo4j.graphdb.Node user = null;

        def traverser = bidirectionalTraversal()
                .startSide( traversal().expand( pathExpanderForTypes( contact, Direction.OUTGOING ) ).uniqueness(
                Uniqueness.NODE_PATH ) )
                .endSide( traversal().expand( pathExpanderForTypes( employee, Direction.INCOMING ) ).uniqueness(
                Uniqueness.NODE_PATH ) )
                .traverse( getNodeWithName( "me" ), getNodeWithName( "co" ) )
        def result = [:].withDefault { 0 }
        for ( path in traverser )
        {

            result[path.nodes()[1].getProperty( "name" )]++
        }
        result.sort { it.value }

        then:
        result.size() == 2
        result.c00 == 2
        result.c01 == 1

    }

    private dumpGraph( )
    {
        for ( node in GlobalGraphOperations.at( graphDatabaseService ).allNodes )
        {
            def rels = node.getRelationships().collect { "$it.startNode.id -[:${it.type.name()}]-> $it.endNode.id" }
            println "node $node.id : ${node.getProperty( "name", null )} : $rels"
        }
    }
}
