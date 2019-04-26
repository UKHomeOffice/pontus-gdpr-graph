import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.janusgraph.core.JanusGraph
import org.janusgraph.core.schema.JanusGraphManagement

LinkedHashMap globals = [:]
globals << [g: ((JanusGraph) graph).traversal() as GraphTraversalSource]
globals << [mgmt: ((JanusGraph) graph).openManagement() as JanusGraphManagement]

