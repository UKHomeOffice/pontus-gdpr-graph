package com.pontusvision.gdpr;

import org.apache.tinkerpop.gremlin.server.Settings;
import org.apache.tinkerpop.gremlin.server.util.ServerGremlinExecutor;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.eclipse.jetty.server.Server;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ResourceTest
{

  public static Response callGetRestAPI(String healthcheckURL, MediaType mediaType)
  {
    Response                response = null;
    final ResteasyClient    client   = new ResteasyClientBuilder().build();
    final ResteasyWebTarget target   = client.target(healthcheckURL);
    MultivaluedMap          headers  = new MultivaluedMapImpl();
    headers.add("Origin", "localhost");
    headers.add("Content-Type", mediaType);
    headers.add("Accept", mediaType);

    headers.add("Cache-Control", "no-cache");
    // headers.add("Accept-Encoding", "gzip, deflate, br");
    headers.add("Connection", "keep-alive");
    headers.add("DNT", "1");
    headers.add("Accept-Encoding", "gzip, deflate, br");

    response =
        target
            .request()
            .headers(headers)
            .get();

    return response;

  }

  static int healthcheckPortNum = 3332;
  Server server = null;

  @Before public void setup() throws Exception
  {
    server = App.startHealthcheckRest(healthcheckPortNum);
  }

  @After public void teardown()
  {
    if (server != null)
    {
      try
      {
        server.stop();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  public static void shutdownGraph(CompletableFuture<ServerGremlinExecutor> graphServer)
      throws Exception
  {
    if (graphServer != null)
    {
      ServerGremlinExecutor sge = graphServer.get();

      Graph graph = sge.getGraphManager().getGraph("graph");

      graph.close();

      sge.getGremlinExecutor().close();

      sge.getGremlinExecutorService().shutdownNow();
      sge.getGremlinExecutorService().shutdown();

      graphServer.cancel(true);

      App.gserver.stop();

    }
  }

  @Test
  public void getGraphURL() throws Exception
  {
    App.settings = Settings.read("target/test-classes/graphdb-conf/gremlin-mem-no-elastic.yml");

    assertEquals(Resource.getGraphURL(), "http://0.0.0.0:8182");

    App.settings = Settings.read("target/test-classes/graphdb-conf/gremlin-url-test.yml");

    assertEquals(Resource.getGraphURL(), "https://10.20.30.40:9999");

  }

  @Test
  public void getReadinessSuccess() throws Exception
  {
    CompletableFuture<ServerGremlinExecutor> graphServer = null;
    try
    {
      graphServer = App.startGraphServer("target/test-classes/graphdb-conf/gremlin-mem-no-elastic.yml");

      Response resp = callGetRestAPI("http://localhost:" + healthcheckPortNum

          + "/healthcheck/readiness", MediaType.TEXT_PLAIN_TYPE);

      assertEquals(resp.getStatus(), 200);
      assertEquals("ready",resp.readEntity(String.class));

    }
    finally
    {

      shutdownGraph(graphServer);

    }

  }

  @Test
  public void getLivelinessSuccess() throws Exception
  {
    CompletableFuture<ServerGremlinExecutor> graphServer = null;
    try
    {
      graphServer = App.startGraphServer("target/test-classes/graphdb-conf/gremlin-mem-no-elastic.yml");

      Response resp = callGetRestAPI("http://localhost:" + healthcheckPortNum

          + "/healthcheck/liveliness", MediaType.TEXT_PLAIN_TYPE);

      assertEquals(resp.getStatus(), 200);
      assertEquals("alive",resp.readEntity(String.class) );

    }
    finally
    {
      shutdownGraph(graphServer);
    }

  }

  @Test
  public void getLivelinessFailureNoGraph() throws Exception
  {

    Response resp = callGetRestAPI("http://localhost:" + healthcheckPortNum

        + "/healthcheck/liveliness", MediaType.TEXT_PLAIN_TYPE);

    assertNotEquals(resp.getStatus(), 200);

  }

  @Test
  public void getLivelinessFailureGraphInvalidHostAndPort() throws Exception
  {
    CompletableFuture<ServerGremlinExecutor> graphServer = null;
    try
    {
      try
      {
        graphServer = App.startGraphServer("target/test-classes/graphdb-conf/gremlin-url-test.yml");
      }
      catch (Exception e)
      {
        // ignore.
      }
      Response resp = callGetRestAPI("http://localhost:" + healthcheckPortNum

          + "/healthcheck/liveliness", MediaType.TEXT_PLAIN_TYPE);

      assertNotEquals(resp.getStatus(), 200);

    }
    finally
    {
      if (graphServer != null)
      {
        graphServer.get().getGremlinExecutor().close();
        graphServer.cancel(true);
      }

    }

  }


  @Test
  public void getReadinessFailureGraphInvalidHostAndPort() throws Exception
  {
    CompletableFuture<ServerGremlinExecutor> graphServer = null;
    try
    {
      try
      {
        graphServer = App.startGraphServer("target/test-classes/graphdb-conf/gremlin-url-test.yml");
      }
      catch (Exception e)
      {
        // ignore.
      }
      Response resp = callGetRestAPI("http://localhost:" + healthcheckPortNum

          + "/healthcheck/readiness", MediaType.TEXT_PLAIN_TYPE);

      assertNotEquals(resp.getStatus(), 200);

    }
    finally
    {
      if (graphServer != null)
      {
        graphServer.get().getGremlinExecutor().close();
        graphServer.cancel(true);
      }

    }

  }
}