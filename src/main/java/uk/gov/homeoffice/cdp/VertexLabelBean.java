package uk.gov.homeoffice.cdp;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.VertexLabelMaker;

/**
 * Javabean for vertexLabel
 */
public class VertexLabelBean
{
  /**
   * Create the vertex
   *
   * @param mgmt
   */
  public void make(JanusGraphManagement mgmt)
  {
    if (name == null)
    {
      DefaultGroovyMethods.println(this, "need \"name\" property to define a vertex");
    }
    else if (mgmt.containsVertexLabel(name))
    {
      DefaultGroovyMethods.println(this, "vertex: " + getName() + " exists");
    }
    else
    {
      try
      {
        VertexLabelMaker maker = mgmt.makeVertexLabel(name);
        if (partition)
          maker.partition();
        if (useStatic)
          maker.setStatic();
        maker.make();
        DefaultGroovyMethods.println(this, "vertex:" + getName() + " creation is done");
      }
      catch (Exception e)
      {
        DefaultGroovyMethods.println(this, "can\'t create vertex: " + getName() + ", " + e.getMessage());
      }

    }

  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public boolean getPartition()
  {
    return partition;
  }

  public boolean isPartition()
  {
    return partition;
  }

  public void setPartition(boolean partition)
  {
    this.partition = partition;
  }

  public boolean getUseStatic()
  {
    return useStatic;
  }

  public boolean isUseStatic()
  {
    return useStatic;
  }

  public void setUseStatic(boolean useStatic)
  {
    this.useStatic = useStatic;
  }

  private String  name      = null;
  private boolean partition = false;
  private boolean useStatic = false;
}
