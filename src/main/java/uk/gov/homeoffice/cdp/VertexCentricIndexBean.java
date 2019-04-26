package uk.gov.homeoffice.cdp;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.janusgraph.core.EdgeLabel;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphManagement;

import java.util.List;

/**
 * represent the individual vertex-centric index object in the "vertexCentricIndexes"
 * list
 */
public class VertexCentricIndexBean
{
  /**
   * Create vertex-centric index
   * <p>
   * {
   * "name": "indexName",
   * "edge": "edgeLebel",
   * "propertyKeys": [ "propertyKey1", "propertyKey2" ],
   * "order": "incr|decr",
   * "direction": "BOTH|IN|OUT"
   * }
   */
  public void make(JanusGraphManagement mgmt)
  {
    if (name == null)
    {
      DefaultGroovyMethods.println(this, "missing 'name' property, not able to create a vertex-centric index");
      return;

    }

    if (edge == null)
    {
      DefaultGroovyMethods.println(this, "vertex-centric index needs 'edge' property to specify a edge label");
      return;

    }

    EdgeLabel elabel = mgmt.getEdgeLabel(edge);

    if (elabel == null)
    {
      DefaultGroovyMethods.println(this, "edge: " + getEdge() + " doesn\'t exist");
      return;

    }

    if (mgmt.containsRelationIndex(elabel, name))
    {
      DefaultGroovyMethods.println(this, "vertex-centric index: " + getName() + " exists");
      return;

    }

    if (propertyKeys == null || propertyKeys.size() == 0)
    {
      DefaultGroovyMethods.println(this, "missing 'propertyKeys property, not able to create an index");
      return;

    }

    PropertyKey[] keys    = new PropertyKey[propertyKeys.size()];
    int           counter = 0;
    for (String property : propertyKeys)
    {
      PropertyKey key = mgmt.getPropertyKey(property);
      if (key == null)
      {
        DefaultGroovyMethods.println(this,
            "propertyKey:" + property + " doesn\'t exist, can\'t create " + getName() + " vertex-centric index");
        return;

      }

      keys[counter++] = mgmt.getPropertyKey(property);
    }

    mgmt.buildEdgeIndex(elabel, name, Direction.valueOf(direction), Order.valueOf(order), keys);

    DefaultGroovyMethods.println(this, "vertex-centric index: " + getName() + " creation is done");
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getEdge()
  {
    return edge;
  }

  public void setEdge(String edge)
  {
    this.edge = edge;
  }

  public List<String> getPropertyKeys()
  {
    return propertyKeys;
  }

  public void setPropertyKeys(List<String> propertyKeys)
  {
    this.propertyKeys = propertyKeys;
  }

  public String getOrder()
  {
    return order;
  }

  public void setOrder(String order)
  {
    this.order = order;
  }

  public String getDirection()
  {
    return direction;
  }

  public void setDirection(String direction)
  {
    this.direction = direction;
  }

  private String       name      = null;
  private String       edge      = null;
  private List<String> propertyKeys;
  private String       order     = "incr";
  private String       direction = "BOTH";
}
