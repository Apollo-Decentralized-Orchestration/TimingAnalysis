package at.uibk.dps.ta.runner;

import at.uibk.dps.di.properties.PropertyServiceScheduler;
import at.uibk.dps.di.schedulerV2.LatencyMapping;
import at.uibk.dps.di.schedulerV2.Scheduler;
import at.uibk.dps.ee.io.resources.ResourceGraphProviderFile;
import at.uibk.dps.ee.io.spec.SpecificationProviderFile;
import at.uibk.dps.ee.model.graph.*;
import at.uibk.dps.ee.visualization.model.EnactmentGraphViewer;
import at.uibk.dps.ta.tmp.eGraphs;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;

import java.util.ArrayList;
import java.util.List;


public class RunnerSchedulerV2 {

    private final String localResourceName = "Enactment Engine (Local Machine)";
    private final String cloudResourceName = "https://fkwvdybi0a.execute-api.us-east-1.amazonaws.com/default/functino_noop_pub";
    List<LatencyMapping> latencyMappings = new ArrayList<>();
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
        PropertyServiceScheduler.setLatencyGlobal(noop, 200.0);
        PropertyServiceScheduler.setInstances(noop, 1000);

        latencyMappings.add(new LatencyMapping(localResourceName, "https://fkwvdybi0a.execute-api.us-east-1.amazonaws.com/default/functino_noop_pub", 500.0));
        latencyMappings.add(new LatencyMapping("https://fkwvdybi0a.execute-api.us-east-1.amazonaws.com/default/functino_noop_pub", "https://fkwvdybi0a.execute-api.us-east-1.amazonaws.com/default/functino_noop_pub", 200.0));
        latencyMappings.add(new LatencyMapping(localResourceName, localResourceName, 0.0));

        // Set up function durations
        MappingsConcurrent mappings = specification.getMappings();
        mappings.mappingStream().forEach((map) -> PropertyServiceScheduler.setDuration(map, 2000.0));

        for(Mapping<Task, Resource> map: mappings) {
            if(map.getTarget().getId().contains("http")) {
                map.setAttribute("Cost", 2.0);
            } else {
                map.setAttribute("Cost", 5.0);
            }
        }

        return specification;
    }

    private void run() {

        // Get the eGraph and specification (including function durations, task mappings, latencies)EnactmentSpecification s = setupSpecification(eGraphs.getMediumSizedEnactmentGraph(), "src/test/resources/mapping.json");
        //EnactmentSpecification specification = setupSpecification(eGraphs.getMediumSizedEnactmentGraph(), "src/test/resources/mapping.json");

        EnactmentSpecification specification = setupSpecification(eGraphs.getEnactmentGraphParallel(4,2), "src/test/resources/mapping_simple.json");

        // --> Scheduling

            // This is the adapted HEFT scheduler
            new Scheduler(latencyMappings).schedule(specification, 36.0);

            // TODO check why it is not in parallel Local-Cloud, then Cloud-Local, then Local-Cloud, then Cloud-Local, ...

            // After scheduling two things should be done:
            /*
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
            */
        // <-- Scheduling

        EnactmentGraphViewer.view(specification.getEnactmentGraph());
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new RunnerSchedulerV2().run();
    }
}
