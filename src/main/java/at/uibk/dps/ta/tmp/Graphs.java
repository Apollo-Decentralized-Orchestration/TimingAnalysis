package at.uibk.dps.ta.tmp;

import at.uibk.dps.di.properties.PropertyServiceScheduler;
import com.google.gson.JsonPrimitive;
import at.uibk.dps.ee.io.resources.ResourceGraphProviderFile;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.MappingsConcurrent;
import at.uibk.dps.ee.model.graph.ResourceGraph;
import at.uibk.dps.ee.model.graph.ResourceGraphProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUser;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping.EnactmentMode;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * Tmp class to give me graphs to play with.
 * 
 * @author Fedor Smirnov
 */
public final class Graphs {

  private Graphs() {}

  private static final String mappingsPath = "src/test/resources/mapping.json";
  private static final String cloudResourceName =
      "https://fkwvdybi0a.execute-api.us-east-1.amazonaws.com/default/functino_noop_pub";

  public static ResourceGraph getRGraph(double tLatLocLoc, double tLatLocGlo, double tLatCloLoc,
      double tLatCloGlo, int tInstLoc, int tInstClo) {
    final ResourceGraphProvider rGraphProv = new ResourceGraphProviderFile(mappingsPath);
    ResourceGraph result = rGraphProv.getResourceGraph();
    result.forEach(res -> annotateResource(res, tLatLocLoc, tLatLocGlo, tLatCloLoc, tLatCloGlo, tInstLoc, tInstClo));
    return result;
  }

  public static MappingsConcurrent getMappings(EnactmentGraph eGraph, ResourceGraph resGraph,
      double durCloud, double durLoc) {
    MappingsConcurrent mappings = new MappingsConcurrent();
    eGraph.getVertices().stream().filter(task -> TaskPropertyService.isProcess(task))
        .forEach(process -> resGraph.getVertices()
            .forEach(res -> mappings.addMapping(makeMapping(process, res, durCloud, durLoc))));
    return mappings;
  }

  static void annotateResource(Resource res, double tLatLocLoc, double tLatLocGlo,
      double tLatCloLoc, double tLatCloGlo, int tInstLoc, int tInstClo) {
    boolean isCloudRes = res.getId().equals(cloudResourceName);
    double latLoc = isCloudRes ? tLatCloLoc : tLatLocLoc;
    double latGlob = isCloudRes ? tLatCloGlo : tLatLocGlo;
    int instances = isCloudRes ? tInstClo : tInstLoc;
    PropertyServiceScheduler.setLatencyGlobal(res, latGlob);
    PropertyServiceScheduler.setLatencyLocal(res, latLoc);
    PropertyServiceScheduler.setInstances(res, instances);
  }

  static Mapping<Task, Resource> makeMapping(Task task, Resource res, double durCloud,
      double durLoc) {
    boolean isCloudRes = res.getId().equals(cloudResourceName);
    EnactmentMode eMode = isCloudRes ? EnactmentMode.Serverless : EnactmentMode.Local;
    Mapping<Task, Resource> result = PropertyServiceMapping.createMapping(task, res, eMode, "bla");
    double mappingDuration = isCloudRes ? durCloud : durLoc;
    PropertyServiceScheduler.setDuration(result, mappingDuration);
    return result;
  }


  public static EnactmentGraph getEGraph() {
    final Task comm1 = new Communication("commNode1");
    final Task comm2 = new Communication("commNode2");
    final Task comm3 = new Communication("commNode3");
    final Task comm4 = new Communication("commNode4");
    final Task comm5 = new Communication("commNode5");
    final Task comm6 = new Communication("commNode6");
    final Task comm7 = new Communication("commNode7");
    final Task comm8 = new Communication("commNode8");
    final Task task1 = PropertyServiceFunctionUser.createUserTask("taskNode1", "noop");
    final Task task2 = PropertyServiceFunctionUser.createUserTask("taskNode2", "noop");
    final Task task3 = PropertyServiceFunctionUser.createUserTask("taskNode3", "noop");
    final Task task4 = PropertyServiceFunctionUser.createUserTask("taskNode4", "noop");
    final Task task5 = PropertyServiceFunctionUser.createUserTask("taskNode5", "noop");
    final Task task6 = PropertyServiceFunctionUser.createUserTask("taskNode6", "noop");
    EnactmentGraph graph = new EnactmentGraph();
    PropertyServiceData.setContent(comm1, new JsonPrimitive(true));
    PropertyServiceDependency.addDataDependency(comm1, task1, "key1", graph);
    PropertyServiceDependency.addDataDependency(task1, comm2, "key2", graph);
    PropertyServiceDependency.addDataDependency(task1, comm3, "key3", graph);
    PropertyServiceDependency.addDataDependency(comm2, task2, "key4", graph);
    PropertyServiceDependency.addDataDependency(comm3, task3, "key5", graph);
    PropertyServiceDependency.addDataDependency(task2, comm4, "key6", graph);
    PropertyServiceDependency.addDataDependency(task3, comm5, "key7", graph);
    PropertyServiceDependency.addDataDependency(comm4, task4, "key8", graph);
    PropertyServiceDependency.addDataDependency(comm5, task5, "key9", graph);
    PropertyServiceDependency.addDataDependency(task4, comm6, "key10", graph);
    PropertyServiceDependency.addDataDependency(task5, comm7, "key11", graph);
    PropertyServiceDependency.addDataDependency(comm6, task6, "key12", graph);
    PropertyServiceDependency.addDataDependency(comm7, task6, "key13", graph);
    PropertyServiceDependency.addDataDependency(task6, comm8, "key14", graph);
    PropertyServiceData.makeRoot(comm1);
    PropertyServiceData.makeLeaf(comm8);
    return graph;
  }
}
