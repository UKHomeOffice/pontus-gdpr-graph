package com.pontusvision.gdpr;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.specimpl.BuiltResponse;
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
import java.util.concurrent.TimeUnit;

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

//      LOGGER.info(
//          " livelinessRestCall() JWT Token: {}",
//          token == null || token.isEmpty() ? "No Token" : "acquired");
      // curl works in Jenkins
      // curl -X POST 'https://10.54.94.140:8444/gateway/default/pvgdpr_graph' -H 'Origin:
      // https://10.54.94.140:8444' -H "Authorization: Bearer $TOKEN" -H 'Content-Type:
      // application/json' -H 'Accept: application/json' -H 'Cache-Control: no-cache' -H
      // 'Connection: keep-alive' -H 'Referer: https://10.54.93.13:12844/gateway/default/pvgdpr_gui'
      // -H 'DNT: 1' --data-binary $'{"gremlin":"g.V().has(\'id\',
      // \'b113f0014245a18a7ab198fae12b240d90dc63f056b5f796c41444b0b1667b77\')"}' --compressed
      // --insecure
      String                  populatedGremlinQuery = "{\"gremlin\":\"" + gremlinQuery + "\"}";


      final ResteasyClient    client                = new ResteasyClientBuilder().connectTimeout(3, TimeUnit.SECONDS).build();
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
      LOGGER.error("Graph raw gremlin API call to "+ endpointGraph +"; error: " + ex.getMessage());
    }
    finally
    {
      if (response != null)
        response.close();
    }
    return response;
  }

  public static Response graphRestCallFinalResponse(String graphURL, String gremlinQuery, String successMsg, String failureMsg, String source)
  {
    Response retVal = new BuiltResponse();

    try {
      Response resp = graphRestCall(graphURL, gremlinQuery);
      if (resp != null && resp.getStatus() == 200)
      {
        ((BuiltResponse) retVal).setStatus(200);
        ((BuiltResponse) retVal).setReasonPhrase(successMsg);
        ((BuiltResponse) retVal).setEntity(successMsg);

      }
      else if (resp != null)
      {
        ((BuiltResponse) retVal).setStatus(resp.getStatus());
        ((BuiltResponse) retVal).setReasonPhrase(failureMsg);
        ((BuiltResponse) retVal).setEntity(failureMsg +": " + resp.readEntity(String.class));

      }
      else
      {
        ((BuiltResponse) retVal).setStatus(500);
        ((BuiltResponse) retVal).setReasonPhrase(failureMsg);
        ((BuiltResponse) retVal).setEntity(failureMsg);
      }

    }
    catch(Exception e)
    {
      ((BuiltResponse) retVal).setStatus(500);
      ((BuiltResponse) retVal).setReasonPhrase(failureMsg);
      ((BuiltResponse) retVal).setEntity(failureMsg +": " + e.getMessage());
    }


    LOGGER.info(source + " response: " +((BuiltResponse) retVal).getReasonPhrase());

    return retVal;
  }


  @GET @Path("readiness") @Produces(MediaType.TEXT_PLAIN) public Response getReadiness()
  {

    return graphRestCallFinalResponse(getGraphURL(), "1+1", "ready", "Not Ready Yet", "Readiness");

  }

  @GET @Path("liveliness") @Produces(MediaType.TEXT_PLAIN) public Response getLiveliness()
  {
    return graphRestCallFinalResponse(getGraphURL(), "1+1", "alive", "Not Alive Yet", "Liveliness");

  }

}