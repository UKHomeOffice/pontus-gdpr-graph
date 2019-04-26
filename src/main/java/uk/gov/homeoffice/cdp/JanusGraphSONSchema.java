package uk.gov.homeoffice.cdp;

import groovy.lang.Closure;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.shaded.jackson.databind.node.ObjectNode;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphTransaction;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.graphdb.database.StandardJanusGraph;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A utility class to read graph schema definition and write to JanusGraph
 */
public class JanusGraphSONSchema
{
  /**
   * Constructor of JanusGraphSONSchema object with the {@code graph}
   *
   * @param graph a JanusGraph and write graph schema into it
   */
  public JanusGraphSONSchema(JanusGraph graph) throws Exception
  {
    if (!DefaultGroovyMethods.asBoolean(graph))
    {
      throw new Exception("JanusGraph is null");
    }

    this.graph = ((StandardJanusGraph) (graph));
  }

  /**
   * Read the graph schema definition from {@code schemaFile}
   * and write to the JanusGraph
   *
   * @param schemaFile grah schema definition and using
   *                   IBM graph schema format.
   */
  public void readFile(String schemaFile)
  {
    JanusGraphManagement mgmt = graph.openManagement();

    try
    {
      parse(schemaFile).make(mgmt);
      rollbackTxs(graph);
    }
    catch (Exception e)
    {
      DefaultGroovyMethods.print(this, "parse GSON failed: " + e.getMessage());
    }

  }

  /**
   * Commit all running transactions upon the graph
   *
   * @param graph
   * @return
   */
  public static boolean commitTxs(JanusGraph graph)
  {
    StandardJanusGraph sgraph = (StandardJanusGraph) graph;
    try
    {
      Set<JanusGraphTransaction> txs = (Set<JanusGraphTransaction>) sgraph.getOpenTransactions();
      //commit all running transactions
      Iterator<JanusGraphTransaction> iter = (Iterator<JanusGraphTransaction>) txs.iterator();
      while (iter.hasNext())
      {
        iter.next().commit();
      }

    }
    catch (Exception e)
    {
      //ignore
      return false;
    }

    return true;
  }

  /**
   * Rollback back all running transaction upon the graph
   *
   * @param graph
   * @return
   */
  public static boolean rollbackTxs(JanusGraph graph)
  {
    StandardJanusGraph sgraph = (StandardJanusGraph) graph;
    try
    {
      Set<JanusGraphTransaction> txs = (Set<JanusGraphTransaction>) sgraph.getOpenTransactions();
      //commit all running transactions
      Iterator<JanusGraphTransaction> iter = (Iterator<JanusGraphTransaction>) txs.iterator();
      while (iter.hasNext())
      {
        iter.next().rollback();
      }

    }
    catch (Exception e)
    {
      //ignore
      return false;
    }

    return true;
  }

  /**
   * Parse the graph schema definition and return a GraphSchema object
   * if parse successes
   *
   * @param schemaFile name of the json file to parse
   * @return the parsed Graph Schema class
   */
  public static GraphSchema parse(String schemaFile) throws Exception
  {
    File gsonFile = new File(schemaFile);

    if (!gsonFile.exists())
    {
      throw new Exception("file not found:" + schemaFile);
    }

    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(gsonFile, GraphSchema.class);
  }

  public void make(List<ObjectNode> nodes, String name, Closure check, Closure exist, Closure create)
  {
    for (ObjectNode node : nodes)
    {
      String nameStr = node.get(name).asText();
      if (DefaultGroovyMethods.asBoolean(check.call(nameStr)))
      {
        exist.call(nameStr);
      }
      else
      {
        create.call(nameStr, node);
      }

    }

  }

  public StandardJanusGraph getGraph()
  {
    return graph;
  }

  public void setGraph(StandardJanusGraph graph)
  {
    this.graph = graph;
  }

  private StandardJanusGraph graph;
}
