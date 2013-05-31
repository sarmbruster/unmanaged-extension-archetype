package org.neo4j.test

import com.github.goldin.spock.extensions.tempdir.TempDir
import com.sun.jersey.api.client.Client
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.server.NeoServer
import org.neo4j.server.helpers.ServerBuilder
import org.neo4j.server.rest.RestRequest
import org.neo4j.server.rest.domain.GraphDbHelper
import spock.lang.Specification

/**
 * provide a running Neo4j server for integration tests
 */
abstract class Neo4jServerSpecification extends Specification {

    @TempDir File tempDir

    GraphDatabaseService graphDatabaseService
    ExecutionEngine executionEngine
    NeoServer server
    Client client = Client.create()
    GraphDbHelper helper
    RestRequest request

    def setup() {
        ServerBuilder serverBuilder = ServerBuilder.server().usingDatabaseDir(tempDir.absolutePath).onPort(7477)
        jaxRsPackagesAndMountpoints().each { packageName, mountPoint ->
            serverBuilder.withThirdPartyJaxRsPackage(packageName, mountPoint)
        }
        server = serverBuilder.build();
        server.start()
        graphDatabaseService = server.database.graph
        executionEngine = new ExecutionEngine(graphDatabaseService)
        helper = new GraphDbHelper(server.getDatabase())
        request = new RestRequest(server.baseUri().resolve(basePath + "/"), client)
    }

    abstract Map<String, String> jaxRsPackagesAndMountpoints()
    abstract String getBasePath()

    def cleanup() {
        server.stop()
    }

    def withTransaction(Closure closure) {
        def tx = graphDatabaseService.beginTx()
        try {
            def result = closure.call()
            tx.success()
            return result
        } finally {
            tx.finish()
        }
    }

}
