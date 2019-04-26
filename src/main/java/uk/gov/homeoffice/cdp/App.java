/*
 * Code derived from
 *  https://github.com/pontusvision/pontus-gdpr-graph/blob/master/src/main/java/com/pontusvision/gdpr/App.java
 * used here under the Apache license.
 */

package uk.gov.homeoffice.cdp;


import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.server.GraphManager;
import org.apache.tinkerpop.gremlin.server.GremlinServer;
import org.apache.tinkerpop.gremlin.server.Settings;
import org.apache.tinkerpop.gremlin.server.util.ServerGremlinExecutor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.janusgraph.core.JanusGraph;
import org.jhades.JHades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class App
{
  private static final Logger               logger = LoggerFactory.getLogger(App.class);
  public static        JanusGraph           graph;
  //    public static JanusGraphManagement graphMgmt;
  public static        GremlinServer        gserver;
  public static        GraphTraversalSource g;
  public static        Settings             settings;

  static
  {
    // hook slf4j up to netty internal logging
    //        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
  }

  public static Server startHealthcheckRest(int port) throws Exception
  {
    Server server = new Server(port);

    ResourceConfig config = new ResourceConfig();
    config.packages("uk.gov.homeoffice.cdp");
    ServletHolder servlet = new ServletHolder(new ServletContainer(config));

    ServletContextHandler context = new ServletContextHandler(server, "/*");
    context.addServlet(servlet, "/*");

    server.start();

    return server;
  }

  public static CompletableFuture<ServerGremlinExecutor> startGraphServer(String file) throws Exception
  {

    try
    {
      settings = Settings.read(file);
    }
    catch (Exception ex)
    {
      logger.error("Configuration file at {} could not be found or parsed properly. [{}]", file, ex.getMessage());
      ex.printStackTrace();
      return null;
    }

    logger.info("Configuring Gremlin Server from {}", file);
    gserver = new GremlinServer(settings);
    CompletableFuture<ServerGremlinExecutor> completableFuture = gserver.start().exceptionally(t -> {
      logger.error("Gremlin Server was unable to start and will now begin shutdown: {}", t.getMessage());
      gserver.stop().join();
      return null;
    });

    ServerGremlinExecutor sge      = completableFuture.get();
    ServerGremlinExecutor executor = gserver.getServerGremlinExecutor();
    executor.getGremlinExecutor().eval("graph = TinkerGraph.open()\n" +
        "g = graph.traversal()\n");

    GraphManager graphMgr   = sge.getGraphManager();
    Set<String>  graphNames = graphMgr.getGraphNames();

    for (String graphName : graphNames)
    {
      logger.debug("Found Graph: " + graphName);
      graph = (JanusGraph) graphMgr.getGraph(graphName);
      g = graph.traversal();
    }

    return completableFuture;

  }

  public static void main(String[] args)
  {
    new JHades().overlappingJarsReport();
    CompletableFuture<ServerGremlinExecutor> graphServer = null;
    Server server = null;
    try
    {
      String portStr = System.getenv("GRAPHDB_REST_PORT");
      int    port    = 3001;
      if (portStr != null)
      {
        port = Integer.parseInt(portStr);
      }

      server = startHealthcheckRest(port);

      String file = args.length == 0 ? "conf/gremlin-server.yml" : args[0];
      graphServer = startGraphServer(file);

      server.join();
      if (graphServer != null)
      {
        graphServer.join();
      }
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }
    finally
    {
      if (server != null)
      {
        server.destroy();
      }
      if (graphServer != null)
      {
        graphServer.cancel(true);
      }
    }
  }
}
