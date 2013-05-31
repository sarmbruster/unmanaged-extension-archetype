package org.neo4j.test

import com.github.goldin.spock.extensions.tempdir.TempDir
import org.neo4j.cypher.javacompat.ExecutionEngine
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

    @TempDir File storeDir
    GraphDatabaseService graphDatabaseService
    CypherExecutor cypherExecutor
    Transaction transaction
    ExecutionEngine executionEngine

    def setup() {
        graphDatabaseService = new TestGraphDatabaseFactory().newImpermanentDatabase(storeDir.absolutePath)
        cypherExecutor = new CypherExecutor(new WrappingDatabase(graphDatabaseService), new TestLogger())
        cypherExecutor.start()
        executionEngine = cypherExecutor.executionEngine
        transaction = graphDatabaseService.beginTx()
    }

    def cleanup() {
        transaction.failure()
        transaction.finish()
        graphDatabaseService.shutdown()
    }

}
