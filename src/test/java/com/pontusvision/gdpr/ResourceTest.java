package com.pontusvision.gdpr;

import org.apache.tinkerpop.gremlin.driver.MessageSerializer;
import org.apache.tinkerpop.gremlin.driver.ser.MessageTextSerializer;
import org.apache.tinkerpop.gremlin.server.Settings;
import org.apache.tinkerpop.gremlin.server.util.ServerGremlinExecutor;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.eclipse.jetty.server.Server;
import org.javatuples.Pair;
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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ResourceTest
{
  protected final static Map<String, MessageTextSerializer> serializers = new HashMap<>();

  protected static void configureSerializers(ServerGremlinExecutor embeddedServer)
  {

    // grab some sensible defaults if no serializers are present in the config
    //    final List<Settings.SerializerSettings> serializerSettings =
    //        (null == this.settings.serializers || this.settings.serializers.isEmpty()) ? DEFAULT_SERIALIZERS : settings.serializers;

    App.settings.serializers.stream().map(config -> {
      try
      {
        final Class clazz = Class.forName(config.className);
        if (!MessageSerializer.class.isAssignableFrom(clazz))
        {
          //          logger.warn("The {} serialization class does not implement {} - it will not be available.", config.className, MessageSerializer.class.getCanonicalName());
          return Optional.<MessageSerializer>empty();
        }

        //        if (clazz.getAnnotation(Deprecated.class) != null)
        //          logger.warn("The {} serialization class is deprecated.", config.className);

        final MessageSerializer  serializer             = (MessageSerializer) clazz.newInstance();
        final Map<String, Graph> graphsDefinedAtStartup = new HashMap<>();
        for (String graphName : App.settings.graphs.keySet())
        {
          graphsDefinedAtStartup.put(graphName, embeddedServer.getGraphManager().getGraph(graphName));
        }

        if (config.config != null)
          serializer.configure(config.config, graphsDefinedAtStartup);

        return Optional.ofNullable(serializer);
      }
      catch (ClassNotFoundException cnfe)
      {
        //        logger.warn("Could not find configured serializer class - {} - it will not be available", config.className);
        return Optional.<MessageSerializer>empty();
      }
      catch (Exception ex)
      {
        //        logger.warn("Could not instantiate configured serializer class - {} - it will not be available. {}", config.className, ex.getMessage());
        return Optional.<MessageSerializer>empty();
      }
    }).filter(Optional::isPresent).map(Optional::get)
                            .flatMap(
                                ser -> Stream.of(ser.mimeTypesSupported()).map(mimeType -> Pair.with(mimeType, ser)))
                            .forEach(pair -> {
                              final String mimeType = pair.getValue0();

                              MessageSerializer ser = pair.getValue1();
                              if (ser instanceof MessageTextSerializer)
                              {
                                final MessageTextSerializer serializer = (MessageTextSerializer) ser;

                                if (!serializers.containsKey(mimeType))
                                {
                                  //        logger.info("Configured {} with {}", mimeType, pair.getValue1().getClass().getName());
                                  serializers.put(mimeType, serializer);
                                }
                              }
                            });

    if (serializers.size() == 0)
    {
      //      logger.error("No serializers were successfully configured - server will not start.");
      throw new RuntimeException("Serialization configuration error.");
    }
  }

  public static ServerGremlinExecutor createEmbeddedServer() throws URISyntaxException, IOException
  {

    String gconfFileStr = (String) App.settings.graphs
        .getOrDefault("graph", "target/test-classes/graphdb-conf/janusgraph-inmem.properties");

    ServerGremlinExecutor embeddedServer = new ServerGremlinExecutor(App.settings, null, null);
    //    embeddedServer.getGraphManager().putTraversalSource("g", graph.traversal());
    //    embeddedServer.getGraphManager().putGraph("graph", graph);

    configureSerializers(embeddedServer);

    return embeddedServer;

  }

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
      assertEquals(resp.readEntity(String.class), "ready");

    }
    finally
    {
      if (graphServer != null)
      {
        graphServer.cancel(true);
      }

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
      assertEquals(resp.readEntity(String.class), "alive");

    }
    finally
    {
      if (graphServer != null)
      {
        graphServer.cancel(true);
      }

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
        graphServer.cancel(true);
      }

    }

  }




}