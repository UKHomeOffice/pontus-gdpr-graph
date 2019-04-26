package uk.gov.homeoffice.cdp;

import groovy.lang.Binding;
import groovy.lang.Script;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.SchemaAction;

public class JanusGraphSchemaImporter extends Script
{
  public static void main(String[] args)
  {
    new JanusGraphSchemaImporter(new Binding(args)).run();
  }

  public Object run()
  {
    /*******************************************************************************
     *   Copyright 2017 IBM Corp. All Rights Reserved.
     *
     *   Licensed under the Apache License, Version 2.0 (the "License");
     *   you may not use this file except in compliance with the License.
     *   You may obtain a copy of the License at
     *
     *        http://www.apache.org/licenses/LICENSE-2.0
     *
     *    Unless required by applicable law or agreed to in writing, software
     *    distributed under the License is distributed on an "AS IS" BASIS,
     *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *    See the License for the specific language governing permissions and
     *    limitations under the License.
     *******************************************************************************/

    /**
     * Janusgraph datatype mapping table
     */

    /**
     * JavaBean for propertyKey
     */

    /**
     * Javabean for vertexLabel
     *
     */

    /**
     * JavaBean for EdgeLabel
     *
     */

    /**
     * JavaBean for Index, both vertex and edge index
     *
     */

    /**
     * represent the individual vertex-centric index object in the "vertexCentricIndexes"
     * list
     */

    /**
     * Represents the whole graph schema definition. It contains:
     * - propertyKeys
     * - vertexLabels
     * - edgeLabels
     * - vertexIndexes
     * - edgeIndexes
     * - vertexCentricIndexes
     */

    /**
     * A utility class to read graph schema definition and write to JanusGraph
     */

    /**
     * parse the graph schema in {@code schema} and write to
     * {@code graph}
     * @param graph a valid JanusGraph instance
     * @param schema graph schema definition location
     * @return
     */

    return null;

  }

  /**
   * parse the graph schema in {@code schema} and write to
   * {@code graph}
   *
   * @param graph  a valid JanusGraph instance
   * @param schema graph schema definition location
   * @return
   */
  public void writeGraphSONSchema(JanusGraph graph, String schema) throws Exception
  {
    JanusGraphSONSchema importer = new JanusGraphSONSchema(graph);
    importer.readFile(schema);
  }

  public boolean updateCompositeIndexState(JanusGraph graph, final String name, SchemaAction newState)
  {
    JanusGraphManagement mgmt  = graph.openManagement();
    JanusGraphIndex      index = mgmt.getGraphIndex(name);
    if (index == null)
    {
      print(name + " index doesn\'t exist");
      return false;

    }

    mgmt.updateIndex(index, newState);
    mgmt.commit();
    return JanusGraphSONSchema.rollbackTxs(graph);
  }

  public JanusGraphSchemaImporter(Binding binding)
  {
    super(binding);
  }

  public JanusGraphSchemaImporter()
  {
    super();
  }
}
