package at.uibk.dps.ta.runner;

import at.uibk.dps.di.incision.Incision;
import at.uibk.dps.di.incision.Utility;
import at.uibk.dps.di.properties.PropertyServiceScheduler;
import at.uibk.dps.di.scheduler.Cut;
import at.uibk.dps.di.scheduler.Scheduler;
import at.uibk.dps.ee.deploy.run.ImplementationRunBare;
import at.uibk.dps.ee.io.resources.ResourceGraphProviderFile;
import at.uibk.dps.ee.io.spec.SpecificationProviderFile;
import at.uibk.dps.ee.model.graph.*;
import at.uibk.dps.ee.visualization.model.EnactmentGraphViewer;
import at.uibk.dps.ta.tmp.eGraphs;
import io.vertx.core.Vertx;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import org.w3c.dom.ls.LSOutput;

import java.util.*;

public class RunnerFile2 {

    private final String localResourceName = "Enactment Engine (Local Machine)";
    private final String cloudResourceName = "https://fkwvdybi0a.execute-api.us-east-1.amazonaws.com/default/functino_noop_pub";

    private EnactmentSpecification setupSpecification(EnactmentGraph eGraph, String mappingsPath) {

        // Generate the specification
        final EnactmentGraphProvider eGraphProvider = () -> eGraph;
        final ResourceGraphProvider rGraphProv = new ResourceGraphProviderFile(mappingsPath);
        final SpecificationProviderFile specProv = new SpecificationProviderFile(eGraphProvider, rGraphProv, mappingsPath);
        final EnactmentSpecification specification = specProv.getSpecification();

        // Set up resource instances and latencies
        Resource local = specification.getResourceGraph().getVertex(localResourceName);
        PropertyServiceScheduler.setLatencyLocal(local, 0.0);
        PropertyServiceScheduler.setLatencyGlobal(local, 0.0);
        PropertyServiceScheduler.setInstances(local, 1);
        Resource noop = specification.getResourceGraph().getVertex(cloudResourceName);
        PropertyServiceScheduler.setLatencyLocal(noop, 200.0);
        PropertyServiceScheduler.setLatencyGlobal(noop, 500.0);
        PropertyServiceScheduler.setInstances(noop, 1000);

        // Set up function durations
        MappingsConcurrent mappings = specification.getMappings();
        mappings.mappingStream().forEach((map) -> PropertyServiceScheduler.setDuration(map, 2000.0));

        return specification;
    }

    private void run() {

        // Get the eGraph and specification (including function durations, task mappings, latencies)
        EnactmentSpecification specification = setupSpecification(eGraphs.getMediumSizedEnactmentGraph(), "src/test/resources/mapping.json");

        // --> Scheduling

        // This is the adapted HEFT scheduler
        //List<Cut> cuts = new JIT().schedule(specification);

        // After scheduling two things should be done:

        // 1. Each task should have exactly one mapping at the end of the scheduling (here only cloud functions are considered)
        MappingsConcurrent mappings = specification.getMappings();
        for(Mapping mapping: mappings) {
            if(!mapping.getId().endsWith(cloudResourceName)) {
                specification.getMappings().removeMapping(mapping);
            }
        }

        // 2. A list of cuts expresses where the workflow should be cut (EnactmentGraphViewer.view(specification.getEnactmentGraph()); for better understanding)
        List<Cut> cuts = new ArrayList<>();

        Set<Task> topCut = new HashSet<>();
        topCut.add(specification.getEnactmentGraph().getVertex("commNode3"));

        Set<Task> bottomCut = new HashSet<>();
        bottomCut.add(specification.getEnactmentGraph().getVertex("commNode7"));

        cuts.add(new Cut(topCut, bottomCut));
        // <-- Scheduling

        //EnactmentGraphViewer.view(specification.getEnactmentGraph());

        // Cut the workflow at the given position
        for(Cut cut: cuts) {
            new Incision().cut(specification, cut.getTopCut(), cut.getBottomCut());
        }

        // Get the adapted specification as string
        String specificationAdapted = Utility.fromEnactmentSpecificationToString(specification);

        // Run the workflow
        new ImplementationRunBare(Vertx.vertx()).implement("{'input': 1}", specificationAdapted, Utility.DE_CONFIGURATION);
    }

    public static void main(String[] args) {
        new RunnerFile2().run();
    }
}
