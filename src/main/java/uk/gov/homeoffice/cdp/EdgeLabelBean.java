package uk.gov.homeoffice.cdp;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.janusgraph.core.Multiplicity;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.EdgeLabelMaker;
import org.janusgraph.core.schema.JanusGraphManagement;

import java.util.List;

/**
 * JavaBean for EdgeLabel
 */
public class EdgeLabelBean
{
  public void make(JanusGraphManagement mgmt)
  {
    if (name == null)
    {
      DefaultGroovyMethods.println(this, "need \"name\" property to define a label");
    }
    else if (mgmt.containsEdgeLabel(name))
    {
      DefaultGroovyMethods.println(this, "edge: " + getName() + " exists");
    }
    else
    {
      try
      {
        EdgeLabelMaker maker = mgmt.makeEdgeLabel(name).multiplicity(Multiplicity.valueOf(multiplicity));
        if (signatures != null && signatures.size() > 0)
        {
          PropertyKey[] keys    = new PropertyKey[signatures.size()];
          int           counter = 0;
          for (String key : signatures)
          {
            keys[counter++] = mgmt.getPropertyKey(key);
          }

          maker.signature(keys);
        }

        if (unidirected)
        {
          maker.unidirected();
        }

        maker.make();
        DefaultGroovyMethods.println(this, "edge: " + getName() + " creation is done");
      }
      catch (Exception e)
      {
        DefaultGroovyMethods.println(this, "cant\'t create edge: " + getName() + ", " + e.getMessage());
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

  public String getMultiplicity()
  {
    return multiplicity;
  }

  public void setMultiplicity(String multiplicity)
  {
    this.multiplicity = multiplicity;
  }

  public List<String> getSignatures()
  {
    return signatures;
  }

  public void setSignatures(List<String> signatures)
  {
    this.signatures = signatures;
  }

  public boolean getUnidirected()
  {
    return unidirected;
  }

  public boolean isUnidirected()
  {
    return unidirected;
  }

  public void setUnidirected(boolean unidirected)
  {
    this.unidirected = unidirected;
  }

  private String       name         = null;
  private String       multiplicity = "MULTI";
  private List<String> signatures;
  private boolean      unidirected  = false;
}
