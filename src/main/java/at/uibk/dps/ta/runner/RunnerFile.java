package at.uibk.dps.ta.runner;

import at.uibk.dps.di.incision.Incision;
import at.uibk.dps.di.incision.Utility;
import at.uibk.dps.di.properties.PropertyServiceScheduler;
import at.uibk.dps.di.scheduler.Cut;
import at.uibk.dps.ee.deploy.run.ImplementationRunBare;
import at.uibk.dps.ee.io.afcl.AfclReader;
import at.uibk.dps.ee.io.resources.ResourceGraphProviderFile;
import at.uibk.dps.ee.io.spec.SpecificationProviderFile;
import at.uibk.dps.ee.model.graph.*;
import at.uibk.dps.ee.visualization.model.EnactmentGraphViewer;
import at.uibk.dps.ta.tmp.eGraphs;
import io.netty.util.internal.StringUtil;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RunnerFile {

    private final String localResourceName = "Enactment Engine (Local Machine)";
    private final String cloudResourceName = "https://gsyhelawyk.execute-api.us-east-1.amazonaws.com/default/dummy";

    private EnactmentSpecification setupSpecification(String eGraphPath, String mappingsPath) {

        // Generate the specification
        final EnactmentGraphProvider eGraphProvider = () -> new AfclReader(eGraphPath).getEnactmentGraph();
        final ResourceGraphProvider rGraphProv = new ResourceGraphProviderFile(mappingsPath);
        final SpecificationProviderFile specProv = new SpecificationProviderFile(eGraphProvider, rGraphProv, mappingsPath);
        final EnactmentSpecification specification = specProv.getSpecification();


        // Set up resource instances and latencies
        /*Resource local = specification.getResourceGraph().getVertex(localResourceName);
        PropertyServiceScheduler.setLatencyLocal(local, 0.0);
        PropertyServiceScheduler.setLatencyGlobal(local, 0.0);
        PropertyServiceScheduler.setInstances(local, 1);
        Resource noop = specification.getResourceGraph().getVertex(cloudResourceName);
        PropertyServiceScheduler.setLatencyLocal(noop, 200.0);
        PropertyServiceScheduler.setLatencyGlobal(noop, 500.0);
        PropertyServiceScheduler.setInstances(noop, 1000);
*/
        // Set up function durations
        MappingsConcurrent mappings = specification.getMappings();
        mappings.mappingStream().forEach((map) -> PropertyServiceScheduler.setDuration(map, 2000.0));

        return specification;
    }

    private void run(String afclPath, String mappingsPath, String input) {

        // Get the eGraph and specification (including function durations, task mappings, latencies)
        EnactmentSpecification specification = setupSpecification(afclPath, mappingsPath);

        // Get the adapted specification as string
        String specificationAdapted = Utility.fromEnactmentSpecificationToString(specification);

        // Run the workflow
        new ImplementationRunBare().implement(input, specificationAdapted, Utility.DE_CONFIGURATION);
    }

    private void run2(String afclPath, String mappingsPath, String input) {

        // Get the eGraph and specification (including function durations, task mappings, latencies)
        EnactmentSpecification specification = setupSpecification(afclPath, mappingsPath);

        // Get the adapted specification as string
        String specificationAdapted = Utility.fromEnactmentSpecificationToString(specification);

        String in = "{'input': '" + input + "', 'configuration': '" + Utility.DE_CONFIGURATION + "', 'specification': '" +
                Utility.fromEnactmentSpecificationToString(setupSpecification("src/test/resources/wf1.yaml", "src/test/resources/wf1.json")) + "'}";

        // Run the workflow
        new ImplementationRunBare().implement(in, specificationAdapted, Utility.DE_CONFIGURATION);
    }

    public static void main(String[] args) {
        String in = StringUtils.repeat("*", 500);

        double start = System.currentTimeMillis();

/*
        new RunnerFile().run(
                    "src/test/resources/wf1.yaml",
                "src/test/resources/wf1.json",
                      "{'input': '" + in + "'}");

*/
        new RunnerFile().run2(
                "src/test/resources/wf2.yaml",
                "src/test/resources/wf2.json",
                in);

        double end = System.currentTimeMillis();
        System.out.println("Duration: " + (end - start));
    }
}
