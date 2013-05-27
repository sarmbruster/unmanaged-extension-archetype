package org.neo4j.sample.unmanaged;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.sample.domain.User;
import org.neo4j.server.database.CypherExecutor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

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
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUsernames() {

        IndexHits<Node> indexResult = graphDatabaseService.index().forNodes("users").query("username:*");

        List<User> result = new ArrayList<User>(indexResult.size());

        for (Node userNode: indexResult) {
            result.add(new User((String)userNode.getProperty("username")));
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
