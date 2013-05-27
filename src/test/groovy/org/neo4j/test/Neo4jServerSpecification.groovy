package org.neo4j.test

import com.sun.jersey.api.client.Client
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.server.NeoServer
import org.neo4j.server.helpers.ServerBuilder
import org.neo4j.server.rest.RestRequest
import org.neo4j.server.rest.domain.GraphDbHelper
import spock.lang.Shared
import spock.lang.Specification

/**
 * provide a running Neo4j server for integration tests
 */
abstract class Neo4jServerSpecification extends Specification {

    String MOUNTPOINT = "/db"
    @Shared GraphDatabaseService graphDatabaseService
    @Shared NeoServer server
    @Shared Client client = Client.create()
    @Shared GraphDbHelper helper
    RestRequest request

    def setupSpec() {
        ServerBuilder serverBuilder = ServerBuilder.server()
        jaxRsPackagesAndMountpoints().each { packageName, mountPoint ->
            serverBuilder.withThirdPartyJaxRsPackage(packageName, mountPoint)
        }
        server = serverBuilder.build();
        server.start()
        graphDatabaseService = server.database.graph
        helper = new GraphDbHelper(server.getDatabase())
    }

    abstract Map<String, String> jaxRsPackagesAndMountpoints()

    def setup() {
        request = new RestRequest(server.baseUri().resolve(MOUNTPOINT + "/"), client)
    }

    def cleanupSpec() {
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
