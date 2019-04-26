package uk.gov.homeoffice.cdp;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.JanusGraphSchemaType;

import java.util.List;

/**
 * JavaBean for Index, both vertex and edge index
 */
public class IndexBean
{
  /**
   * Create vertex/edge index based on the {@code node}. If {@code isVertexIndex} equals true
   * a vertex index is created. Otherwise, create edge index
   *
   * @param mgmt          is used to call the {@code buildIndex()}
   * @param isVertexIndex create a vertex index or edge index
   */
  public void make(JanusGraphManagement mgmt, boolean isVertexIndex)
  {
    if (name == null)
    {
      DefaultGroovyMethods.println(this, "missing the 'name' property, not able to create an index");
      return;

    }

    if (mgmt.containsGraphIndex(name))
    {
      DefaultGroovyMethods.println(this, "index: " + getName() + " exists");
      return;

    }

    if (propertyKeys == null || propertyKeys.size() == 0)
    {
      DefaultGroovyMethods.println(this, "missing the 'propertyKeys property, not able to create an index");
      return;

    }

    JanusGraphManagement.IndexBuilder ib = mgmt.buildIndex(name, isVertexIndex ? Vertex.class : Edge.class);
    for (String property : propertyKeys)
    {
      ib.addKey(mgmt.getPropertyKey(property));
    }

    if (isVertexIndex && unique)
    {
      ib.unique();
    }

    //indexOnly
    if (indexOnly != null)
    {
      JanusGraphSchemaType key = null;
      if (isVertexIndex)
      {
        key = mgmt.getVertexLabel(indexOnly);
      }
      else
      {
        key = mgmt.getEdgeLabel(indexOnly);
      }

      if (key == null)
      {
        DefaultGroovyMethods.println(this, getIndexOnly() + " doesn\'t exist, skip only property");
      }
      else
      {
        ib.indexOnly(key);
      }

    }

    if (composite)
    {
      ib.buildCompositeIndex();
    }

    if (mixedIndex != null)
    {
      ib.buildMixedIndex(mixedIndex);
    }

    DefaultGroovyMethods.println(this, "index: " + getName() + " creation is done");
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public List<String> getPropertyKeys()
  {
    return propertyKeys;
  }

  public void setPropertyKeys(List<String> propertyKeys)
  {
    this.propertyKeys = propertyKeys;
  }

  public boolean getComposite()
  {
    return composite;
  }

  public boolean isComposite()
  {
    return composite;
  }

  public void setComposite(boolean composite)
  {
    this.composite = composite;
  }

  public boolean getUnique()
  {
    return unique;
  }

  public boolean isUnique()
  {
    return unique;
  }

  public void setUnique(boolean unique)
  {
    this.unique = unique;
  }

  public String getIndexOnly()
  {
    return indexOnly;
  }

  public void setIndexOnly(String indexOnly)
  {
    this.indexOnly = indexOnly;
  }

  public String getMixedIndex()
  {
    return mixedIndex;
  }

  public void setMixedIndex(String mixedIndex)
  {
    this.mixedIndex = mixedIndex;
  }

  private String       name       = null;
  private List<String> propertyKeys;
  private boolean      composite  = true;
  private boolean      unique     = false;
  private String       indexOnly  = null;
  private String       mixedIndex = null;
}
