package org.neo4j.sample.unmanaged;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.server.database.CypherExecutor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.*;

/**
 * sample for Neo4j unmanaged extensions
 * <p/>
 * provides a REST interface for maintaining users.
 */
@Path("/user")
public class SampleUserExtension {

    @Context
    protected GraphDatabaseService graphDatabaseService;

    @Context
    protected CypherExecutor cypherExecutor;

    @GET
    public List<Map<String, Object>> getUsernames() {

        IndexHits<Node> indexResult = graphDatabaseService.index().forNodes("users").query("username:*");

        List<Map<String, Object>> result = new ArrayList<>(indexResult.size());

        for (Node userNode: indexResult) {
            Map<String, Object> nodeMap = new HashMap<>(2);
            nodeMap.put("username", userNode.getProperty("username", "<N/A>"));
            nodeMap.put("id", userNode.getId());
            result.add(nodeMap);
        }
        return result;
    }

    /**
     * to be used from unit tests
     *
     * @param graphDatabaseService
     */
    public void setGraphDatabaseService(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    /**
     * to be used from unit tests
     *
     * @param cypherExecutor
     */
    public void setCypherExecutor(CypherExecutor cypherExecutor) {
        this.cypherExecutor = cypherExecutor;
    }
}
