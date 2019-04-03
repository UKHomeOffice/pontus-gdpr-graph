package com.pontusvision.gdpr;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

@Path("healthcheck") public class Resource
{

  private static final Logger LOGGER = LoggerFactory.getLogger(Resource.class);

  public static String getGraphURL()
  {
    return (App.settings.ssl.enabled ? "https://" : "http://") + App.settings.host + ":" + App.settings.port;

  }

  public static Response graphRestCall(String graphURL, String gremlinQuery)
  {
    final String endpointGraph = graphURL;

    Response response = null;
    try
    {
      String token = System.getenv("TOKEN");

      LOGGER.info(
          " livelinessRestCall() JWT Token: {}",
          token == null || token.isEmpty() ? "No Token" : "acquired");
      // curl works in Jenkins
      // curl -X POST 'https://10.54.94.140:8444/gateway/default/pvgdpr_graph' -H 'Origin:
      // https://10.54.94.140:8444' -H "Authorization: Bearer $TOKEN" -H 'Content-Type:
      // application/json' -H 'Accept: application/json' -H 'Cache-Control: no-cache' -H
      // 'Connection: keep-alive' -H 'Referer: https://10.54.93.13:12844/gateway/default/pvgdpr_gui'
      // -H 'DNT: 1' --data-binary $'{"gremlin":"g.V().has(\'id\',
      // \'b113f0014245a18a7ab198fae12b240d90dc63f056b5f796c41444b0b1667b77\')"}' --compressed
      // --insecure
      String                  populatedGremlinQuery = "{\"gremlin\":\"" + gremlinQuery + "\"}";
      final ResteasyClient    client                = new ResteasyClientBuilder().build();
      final ResteasyWebTarget target                = client.target(endpointGraph);
      MultivaluedMap          headers               = new MultivaluedMapImpl();
      headers.add("Origin", "localhost");
      headers.add("Authorization", "Bearer " + token);
      headers.add("Content-Type", MediaType.APPLICATION_JSON);
      headers.add("Accept", MediaType.APPLICATION_JSON);

      headers.add("Cache-Control", "no-cache");
      // headers.add("Accept-Encoding", "gzip, deflate, br");
      headers.add("Connection", "keep-alive");
      headers.add("DNT", "1");
      headers.add("Accept-Encoding", "gzip, deflate, br");

      response =
          target
              .request()
              .headers(headers)
              .post(Entity.entity(populatedGremlinQuery, MediaType.APPLICATION_JSON));

    }
    catch (Exception ex)
    {
      LOGGER.error("Graph raw gremlin API call to {} ", endpointGraph, ex);
    }
    finally
    {
      if (response != null)
        response.close();
    }
    return response;
  }

  @GET @Path("readiness") @Produces(MediaType.TEXT_PLAIN) public String getReadiness() throws Exception
  {
    Response resp = graphRestCall(getGraphURL(), "1+1");

    if (resp != null && resp.getStatus() == 200)
    {
      return "ready";
    }
    throw new Exception("Not Ready Yet");

  }

  @GET @Path("liveliness") @Produces(MediaType.TEXT_PLAIN) public String getLiveliness() throws Exception
  {
    final String query = "graph.openManagement().getVertexLabels().toString()";

    Response resp = graphRestCall(getGraphURL(), query);

    if (resp != null && resp.getStatus() == 200)
    {
      return "alive";
    }

    throw new Exception("Not Alive");

  }

}