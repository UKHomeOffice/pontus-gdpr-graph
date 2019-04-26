package uk.gov.homeoffice.cdp;

import org.janusgraph.core.schema.JanusGraphManagement;

import java.util.List;

/**
 * Represents the whole graph schema definition. It contains:
 * - propertyKeys
 * - vertexLabels
 * - edgeLabels
 * - vertexIndexes
 * - edgeIndexes
 * - vertexCentricIndexes
 */
public class GraphSchema
{
  /**
   * use the {@code mgmt} to create the schema
   *
   * @param mgmt
   */
  public void make(JanusGraphManagement mgmt)
  {
    //create properties
    for (PropertyKeyBean property : propertyKeys)
    {
      property.make(mgmt);
    }

    //create vertex labels
    for (VertexLabelBean vertex : vertexLabels)
    {
      vertex.make(mgmt);
    }

    //create edge labels

    for (EdgeLabelBean edge : edgeLabels)
    {
      edge.make(mgmt);
    }

    //create v indexes
    for (IndexBean vindex : vertexIndexes)
    {
      vindex.make(mgmt, true);
    }

    //create e indexes
    for (IndexBean eindex : edgeIndexes)
    {
      eindex.make(mgmt, false);
    }

    //create vc indexes
    for (VertexCentricIndexBean vcindex : vertexCentricIndexes)
    {
      vcindex.make(mgmt);
    }

    mgmt.commit();
  }

  public List<PropertyKeyBean> getPropertyKeys()
  {
    return propertyKeys;
  }

  public void setPropertyKeys(List<PropertyKeyBean> propertyKeys)
  {
    this.propertyKeys = propertyKeys;
  }

  public List<VertexLabelBean> getVertexLabels()
  {
    return vertexLabels;
  }

  public void setVertexLabels(List<VertexLabelBean> vertexLabels)
  {
    this.vertexLabels = vertexLabels;
  }

  public List<EdgeLabelBean> getEdgeLabels()
  {
    return edgeLabels;
  }

  public void setEdgeLabels(List<EdgeLabelBean> edgeLabels)
  {
    this.edgeLabels = edgeLabels;
  }

  public List<IndexBean> getVertexIndexes()
  {
    return vertexIndexes;
  }

  public void setVertexIndexes(List<IndexBean> vertexIndexes)
  {
    this.vertexIndexes = vertexIndexes;
  }

  public List<IndexBean> getEdgeIndexes()
  {
    return edgeIndexes;
  }

  public void setEdgeIndexes(List<IndexBean> edgeIndexes)
  {
    this.edgeIndexes = edgeIndexes;
  }

  public List<VertexCentricIndexBean> getVertexCentricIndexes()
  {
    return vertexCentricIndexes;
  }

  public void setVertexCentricIndexes(List<VertexCentricIndexBean> vertexCentricIndexes)
  {
    this.vertexCentricIndexes = vertexCentricIndexes;
  }

  private List<PropertyKeyBean>        propertyKeys;
  private List<VertexLabelBean>        vertexLabels;
  private List<EdgeLabelBean>          edgeLabels;
  private List<IndexBean>              vertexIndexes;
  private List<IndexBean>              edgeIndexes;
  private List<VertexCentricIndexBean> vertexCentricIndexes;
}
