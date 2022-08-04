package at.uibk.dps.ta.runner;

import at.uibk.dps.di.properties.PropertyServiceScheduler;
import at.uibk.dps.di.schedulerV2.LatencyMapping;
import at.uibk.dps.di.schedulerV2.Scheduler;
import at.uibk.dps.ee.io.afcl.AfclReader;
import at.uibk.dps.ee.io.resources.ResourceGraphProviderFile;
import at.uibk.dps.ee.io.spec.SpecificationProviderFile;
import at.uibk.dps.ee.model.graph.*;
import at.uibk.dps.ee.visualization.model.EnactmentGraphViewer;
import net.sf.opendse.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class RunnerSchedulerV2AFCL {

    List<LatencyMapping> latencyMappings = new ArrayList<>();
    private final String localResourceName = "Enactment Engine (Local Machine)";

    private void setLocalCloudResources(String local, String cloud, EnactmentSpecification specification){


        Resource r_cloud = specification.getResourceGraph().getVertex(cloud);
        PropertyServiceScheduler.setInstances(r_cloud, 1000);
        Resource r_local = specification.getResourceGraph().getVertex(local);
        PropertyServiceScheduler.setInstances(r_local, 1);
    }

    private EnactmentSpecification setupSpecification(String eGraphPath, String mappingsPath) {

        // Generate the specification
        final EnactmentGraphProvider eGraphProvider = () -> new AfclReader(eGraphPath).getEnactmentGraph();
        final ResourceGraphProvider rGraphProv = new ResourceGraphProviderFile(mappingsPath);
        final SpecificationProviderFile specProv = new SpecificationProviderFile(eGraphProvider, rGraphProv, mappingsPath);
        final EnactmentSpecification specification = specProv.getSpecification();

        latencyMappings.add(new LatencyMapping(localResourceName, "lambda-url.us-east-1.on.aws", 500.0));
        latencyMappings.add(new LatencyMapping("lambda-url.us-east-1.on.aws", "lambda-url.us-east-1.on.aws", 200.0));
        latencyMappings.add(new LatencyMapping(localResourceName, localResourceName, 0.0));

        // Set up resource instances and latencies
        setLocalCloudResources(localResourceName, "https://nvpl2u7eejdrscdll55nvomp3a0qumpv.lambda-url.us-east-1.on.aws/", specification);
        setLocalCloudResources(localResourceName, "https://abzuwbac2m7wzlgpm6zfrnj5c40hexln.lambda-url.us-east-1.on.aws/", specification);
        setLocalCloudResources(localResourceName, "https://xektuf6xq7mlscz3ddcxlmhf2y0szkes.lambda-url.us-east-1.on.aws/", specification);
        setLocalCloudResources(localResourceName, "https://kbqlngc543plsvzrvjtjuqt3aq0mirwu.lambda-url.us-east-1.on.aws/", specification);
        setLocalCloudResources(localResourceName, "https://ezbibug3jxjej53rtc4v7hybca0wfqui.lambda-url.us-east-1.on.aws/", specification);
        setLocalCloudResources(localResourceName, "https://p5fc3q4iczkxolkwl6zkwloy3q0ncrbs.lambda-url.us-east-1.on.aws/", specification);
        setLocalCloudResources(localResourceName, "https://io3e6ekvvhy4cum7t2qiaip2eu0mnxxy.lambda-url.us-east-1.on.aws/", specification);
        setLocalCloudResources(localResourceName, "https://iytgcfvg6yomsrzvn7jhud57j40adryk.lambda-url.us-east-1.on.aws/", specification);
        setLocalCloudResources(localResourceName, "https://pqzui26yyjprgn3fmbego4d7wy0vrciw.lambda-url.us-east-1.on.aws/", specification);
        setLocalCloudResources(localResourceName, "https://uz2ukwa2hcko3xxvisfxcs7lke0vgtci.lambda-url.us-east-1.on.aws/", specification);
        setLocalCloudResources(localResourceName, "https://kg4jhyqz2ta6r5lmrwuumyatfi0mvenc.lambda-url.us-east-1.on.aws/", specification);
        setLocalCloudResources(localResourceName, "https://hmx2mia3vamexdlv5bshhjtqha0lmpcj.lambda-url.us-east-1.on.aws/", specification);
        setLocalCloudResources(localResourceName, "https://l7o363noduuirrq4dbv5elfh4q0mdbrq.lambda-url.us-east-1.on.aws/", specification);
        setLocalCloudResources(localResourceName, "https://34wj2qkwxkmuxjmn3j6hm5fil40giysj.lambda-url.us-east-1.on.aws/", specification);

        // Set up function durations
        MappingsConcurrent mappings = specification.getMappings();
        mappings.mappingStream().forEach((map) -> PropertyServiceScheduler.setDuration(map, 2000.0));

        return specification;
    }

    private void run() {

        // Get the eGraph and specification (including function durations, task mappings, latencies)EnactmentSpecification s = setupSpecification(eGraphs.getMediumSizedEnactmentGraph(), "src/test/resources/mapping.json");
        //EnactmentSpecification specification = setupSpecification(eGraphs.getMediumSizedEnactmentGraph(), "src/test/resources/mapping.json");

        EnactmentSpecification specification = setupSpecification("src/test/resources/soykb/workflow.yaml", "src/test/resources/mapping_soykb.json");

        // --> Scheduling

            // This is the adapted HEFT scheduler
            new Scheduler(latencyMappings).schedule(specification);


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
        new RunnerSchedulerV2AFCL().run();
    }
}
