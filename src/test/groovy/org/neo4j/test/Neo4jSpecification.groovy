package org.neo4j.test

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Transaction
import org.neo4j.kernel.impl.util.TestLogger
import org.neo4j.server.database.CypherExecutor
import org.neo4j.server.database.WrappingDatabase
import spock.lang.Specification

/**
 * provides some infrastructure and transactional context for testing with Neo4j
 */
abstract class Neo4jSpecification extends Specification {

    GraphDatabaseService graphDatabaseService
    CypherExecutor cypherExecutor
    Transaction transaction

    def setup() {
        graphDatabaseService = new TestGraphDatabaseFactory().newImpermanentDatabase()
        cypherExecutor = new CypherExecutor(new WrappingDatabase(graphDatabaseService), new TestLogger())
        transaction = graphDatabaseService.beginTx()
    }

    def cleanup() {
        transaction.failure()
        transaction.finish()
    }

}
