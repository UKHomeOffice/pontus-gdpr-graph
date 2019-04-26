package uk.gov.homeoffice.cdp;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.schema.JanusGraphManagement;

/**
 * JavaBean for propertyKey
 */
public class PropertyKeyBean
{
  /**
   * Create the propertyKey
   *
   * @param mgmt
   */
  public void make(JanusGraphManagement mgmt)
  {
    if (name == null)
    {
      DefaultGroovyMethods.println(this, "need \"name\" property to define a propertyKey");
    }
    else if (mgmt.containsPropertyKey(name))
    {
      DefaultGroovyMethods.println(this, "property: " + getName() + " exists");
    }
    else
    {
      try
      {
        mgmt.makePropertyKey(name).dataType(TypeMap.MAP.get(dataType)).cardinality(Cardinality.valueOf(cardinality))
            .make();
        DefaultGroovyMethods.println(this, "propertyKey:" + getName() + " creation is done");
      }
      catch (Exception e)
      {
        DefaultGroovyMethods.println(this, "can\'t create property:" + getName() + ", " + e.getMessage());
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

  public String getDataType()
  {
    return dataType;
  }

  public void setDataType(String dataType)
  {
    this.dataType = dataType;
  }

  public String getCardinality()
  {
    return cardinality;
  }

  public void setCardinality(String cardinality)
  {
    this.cardinality = cardinality;
  }

  private String name        = null;
  private String dataType    = null;
  private String cardinality = null;
}
