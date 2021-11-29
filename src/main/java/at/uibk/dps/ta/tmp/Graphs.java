package at.uibk.dps.ta.tmp;

import at.uibk.dps.di.properties.PropertyServiceScheduler;
import at.uibk.dps.ee.model.graph.*;
import at.uibk.dps.ee.io.resources.ResourceGraphProviderFile;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping.EnactmentMode;
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
  private static final String localResourceName =
      "Enactment Engine (Local Machine)";
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
    Mapping<Task, Resource> result = PropertyServiceMapping.createMapping(task, res, eMode, isCloudRes ? cloudResourceName : localResourceName);
    double mappingDuration = isCloudRes ? durCloud : durLoc;
    PropertyServiceScheduler.setDuration(result, mappingDuration);
    return result;
  }


  public static EnactmentGraph getEGraph() {
    return eGraphs.getMediumSizedEnactmentGraph();
  }
}
