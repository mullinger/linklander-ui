/**
 *
 */
package de.lander.persistence.daos;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.test.TestGraphDatabaseFactory;


/**
 * -> http://neo4j.com/docs/stable/tutorials-java-unit-testing.html
 *
 * @author mvogel
 *
 */
public class BasicNeo4jUnitTest {

    GraphDatabaseService graphDb;

    @Before
    public void prepareTestDatabase() {
        graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
        // OR
        // graphDb =
        // new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder()
        // .setConfig(GraphDatabaseSettings.nodestore_mapped_memory_size, "10M")
        // .setConfig(GraphDatabaseSettings.string_block_size, "60")
        // .setConfig(GraphDatabaseSettings.array_block_size, "300").newGraphDatabase();
    }

    @After
    public void destroyTestDatabase() {
        graphDb.shutdown();
    }

    /**
     * Relevant input: checks for node creation and reading
     *
     * @author mvogel
     */
    @Test
    public void shouldCreateAndRequestOneNode() throws Exception {
        Node n = null;
        try (Transaction tx = graphDb.beginTx()) {
            n = graphDb.createNode();
            n.setProperty("name", "Nancy");
            tx.success();
        }

        // The node should have a valid id
        assertThat(n.getId(), is(greaterThan(-1L)));

        // Retrieve a node by using the id of the created node. The id's and
        // property should match.
        try (Transaction tx = graphDb.beginTx()) {
            Node foundNode = graphDb.getNodeById(n.getId());
            assertThat(foundNode.getId(), is(n.getId()));
            assertThat((String) foundNode.getProperty("name"), is("Nancy"));
        }
    }

    /**
     * Demonstrates the use of the {@link ExecutionEngine} with a cypher statement
     *
     * @author mvogel
     */
    @Test
    public void shouldCreateAndRequestNodeViaExecutionEngine() throws Exception {
        // == prepare ==
        StringLogger logger = StringLogger.SYSTEM;
        ExecutionEngine cypher = new ExecutionEngine(graphDb, logger);

        String insertSql =
                "CREATE (Hugo:Person {name:'Hugo Weaving', born:1960}) "
                        + "CREATE (AndyW:Person {name:'Andy Wachowski', born:1967}) "
                        + "CREATE (LanaW:Person {name:'Lana Wachowski', born:1965})";
        // == go ==
        try (Transaction tx = graphDb.beginTx()) {
            ExecutionResult result = cypher.execute(insertSql);
            // == verify ==
            assertThat(result.queryStatistics().nodesCreated(), is(3));
            tx.success();
        }
    }
}
