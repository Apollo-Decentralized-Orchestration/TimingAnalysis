package at.uibk.dps.ta.tmp;

import org.opt4j.core.start.Constant;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentSpecification;
import at.uibk.dps.ee.model.graph.MappingsConcurrent;
import at.uibk.dps.ee.model.graph.ResourceGraph;
import at.uibk.dps.ee.model.graph.SpecificationProvider;

/**
 * This one is to be deleted. Just for me to have sth to play with while I build
 * the EA optimizer.
 * 
 * @author Fedor Smirnov
 *
 */
@Singleton
public class SpecificationProviderTest implements SpecificationProvider {

  protected final EnactmentSpecification eSpec;

  @Inject
  public SpecificationProviderTest(
      @Constant(namespace = SpecificationProviderTest.class, value = "durCloud") double durCloud,
      @Constant(namespace = SpecificationProviderTest.class, value = "durLocal") double durLoc,
      @Constant(namespace = SpecificationProviderTest.class,
          value = "transLocalLocal") double latLocLoc,
      @Constant(namespace = SpecificationProviderTest.class,
          value = "transLocalGlobal") double latLocGlo,
      @Constant(namespace = SpecificationProviderTest.class,
          value = "transCloudLocal") double latCloLoc,
      @Constant(namespace = SpecificationProviderTest.class,
          value = "transCloudGlobal") double latCloGlo,
      @Constant(namespace = SpecificationProviderTest.class,
          value = "numInstancesLocal") int numInstancesLocal,
      @Constant(namespace = SpecificationProviderTest.class,
          value = "numInstancesCloud") int numInstancesCloud) {
    this.eSpec = generateSpec(durCloud, durLoc, latLocLoc, latLocGlo, latCloLoc, latCloGlo, numInstancesLocal, numInstancesCloud);
  }

  public EnactmentSpecification generateSpec(double durCloud, double durLoc, double latLocLoc,
      double latLocGlo, double latCloLoc, double latCloGlo, int numInstancesLocal, int numInstancesCloud) {
    EnactmentGraph eGraph = Graphs.getEGraph();
    ResourceGraph rGraph = Graphs.getRGraph(latLocLoc, latLocGlo, latCloLoc, latCloGlo, numInstancesLocal, numInstancesCloud);
    MappingsConcurrent mappings = Graphs.getMappings(eGraph, rGraph, durCloud, durLoc);
    return new EnactmentSpecification(eGraph, rGraph, mappings, "test");
  }

  @Override
  public ResourceGraph getResourceGraph() {
    return eSpec.getResourceGraph();
  }

  @Override
  public EnactmentGraph getEnactmentGraph() {
    return eSpec.getEnactmentGraph();
  }

  @Override
  public MappingsConcurrent getMappings() {
    return eSpec.getMappings();
  }

  @Override
  public EnactmentSpecification getSpecification() {
    return eSpec;
  }
}
