package at.uibk.dps.ta.evaluator;

import at.uibk.dps.di.properties.PropertyServiceScheduler;
import at.uibk.dps.di.scheduler.Cut;
import at.uibk.dps.ee.io.resources.ResourceGraphProviderFile;
import at.uibk.dps.ee.io.spec.SpecificationProviderFile;
import at.uibk.dps.ee.model.graph.*;
import at.uibk.dps.ta.constants.AnnotatedEnactmentSpecifications;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the evaluator.
 *
 * @author Stefan Pedtascher
 */
public class EvaluatorTest {

    private final String localResourceName = "Enactment Engine (Local Machine)";
    private final String cloudResourceName = "https://fkwvdybi0a.execute-api.us-east-1.amazonaws.com/default/functino_noop_pub";

    /**
     * Reduce multiple mappings to a single one.
     *
     * @param mappings the mappings to reduce.
     * @param onLocalResource the tasks that should run on the local resource.
     * @param onCloudResource the tasks that should run on the cloud resource.
     */
    private void reduceMapping(MappingsConcurrent mappings, List<Task> onLocalResource, List<Task> onCloudResource) {
        for(Task task: onLocalResource) {
            for (Mapping<Task, Resource> mapping : mappings.getMappings(task)) {
                if (!mapping.getTarget().getId().equals(localResourceName)) {
                    mappings.removeMapping(mapping);
                }
            }
        }
        for(Task task: onCloudResource) {
            for (Mapping<Task, Resource> mapping : mappings.getMappings(task)) {
                if (!mapping.getTarget().getId().equals(cloudResourceName)) {
                    mappings.removeMapping(mapping);
                }
            }
        }
    }

    /**
     * Setup the specification and add function durations and resource latency.
     *
     * @param eGraph the graph to generate the specification from.
     *
     * @return the generated specification.dd
     */
    /*private EnactmentSpecification setupSpecification(EnactmentGraph eGraph) {

        // Generate the specification
        final EnactmentGraphProvider eGraphProvider = () -> eGraph;
        String mappingsPath = Objects.requireNonNull(getClass().getClassLoader().getResource("mapping.json")).getPath();
        final ResourceGraphProvider rGraphProv = new ResourceGraphProviderFile(mappingsPath);
        final SpecificationProviderFile specProv = new SpecificationProviderFile(eGraphProvider, rGraphProv, mappingsPath);
        final EnactmentSpecification specification = specProv.getSpecification();

        // Set up resource instances and latency
        Resource local = specification.getResourceGraph().getVertex(localResourceName);
        PropertyServiceScheduler.setLatencyLocal(local, 0.0);
        PropertyServiceScheduler.setLatencyGlobal(local, 0.0);
        PropertyServiceScheduler.setInstances(local, 2);
        Resource noop = specification.getResourceGraph().getVertex(cloudResourceName);
        PropertyServiceScheduler.setLatencyLocal(noop, 200.0);
        PropertyServiceScheduler.setLatencyGlobal(noop, 500.0);
        PropertyServiceScheduler.setInstances(noop, 1000);

        // Set up function durations
        MappingsConcurrent mappings = specification.getMappings();
        mappings.mappingStream().forEach((map) -> PropertyServiceScheduler.setDuration(map, 2000.0));

        return specification;
    }

    @Test
    void testEvaluateMediumSizedEnactmentGraph(){

        // Generate specification
        EnactmentGraph eGraph = AnnotatedEnactmentSpecifications.getMediumSizedEnactmentGraph();
        EnactmentSpecification specification = setupSpecification(eGraph);

        // Remove not needed mappings (one resource mapping per task)
        List<Task> onLocalResource = new ArrayList<>();
        onLocalResource.add(eGraph.getVertex("taskNode1"));
        onLocalResource.add(eGraph.getVertex("taskNode2"));
        onLocalResource.add(eGraph.getVertex("taskNode4"));
        onLocalResource.add(eGraph.getVertex("taskNode6"));

        List<Task> onCloudResource = new ArrayList<>();
        onCloudResource.add(eGraph.getVertex("taskNode3"));
        onCloudResource.add(eGraph.getVertex("taskNode5"));
        reduceMapping(specification.getMappings(), onLocalResource, onCloudResource);

        // Generates cuts to check
        List<Cut> cuts = new ArrayList<>();
        Set<Task> topCut = new HashSet<>();
        topCut.add(eGraph.getVertex("commNode3"));
        Set<Task> bottomCut = new HashSet<>();
        bottomCut.add(eGraph.getVertex("commNode7"));
        cuts.add(new Cut(topCut, bottomCut));

        Evaluator evaluator = new Evaluator();
        double duration = evaluator.evaluate(specification, cuts);

        assertEquals(8700.0, duration);
    }

    @Test
    void testEvaluateMediumSizedEnactmentGraphSeparateCut(){

        // Generate specification
        EnactmentGraph eGraph = AnnotatedEnactmentSpecifications.getMediumSizedEnactmentGraph();
        EnactmentSpecification specification = setupSpecification(eGraph);

        // Remove not needed mappings (one resource mapping per task)
        List<Task> onLocalResource = new ArrayList<>();
        onLocalResource.add(eGraph.getVertex("taskNode1"));
        onLocalResource.add(eGraph.getVertex("taskNode2"));
        onLocalResource.add(eGraph.getVertex("taskNode4"));
        onLocalResource.add(eGraph.getVertex("taskNode6"));

        List<Task> onCloudResource = new ArrayList<>();
        onCloudResource.add(eGraph.getVertex("taskNode3"));
        onCloudResource.add(eGraph.getVertex("taskNode5"));
        reduceMapping(specification.getMappings(), onLocalResource, onCloudResource);

        // Generates cuts to check
        List<Cut> cuts = new ArrayList<>();
        Set<Task> topCut = new HashSet<>();
        topCut.add(eGraph.getVertex("commNode3"));
        Set<Task> bottomCut = new HashSet<>();
        bottomCut.add(eGraph.getVertex("commNode5"));
        cuts.add(new Cut(topCut, bottomCut));
        Set<Task> topCut2 = new HashSet<>();
        topCut2.add(eGraph.getVertex("commNode5"));
        Set<Task> bottomCut2 = new HashSet<>();
        bottomCut2.add(eGraph.getVertex("commNode7"));
        cuts.add(new Cut(topCut2, bottomCut2));

        Evaluator evaluator = new Evaluator();
        double duration = evaluator.evaluate(specification, cuts);

        assertEquals(9000.0, duration);
    }

    @Test
    void testEvaluateMediumSizedEnactmentGraphAllLocal(){

        // Generate specification
        EnactmentGraph eGraph = AnnotatedEnactmentSpecifications.getMediumSizedEnactmentGraph();
        EnactmentSpecification specification = setupSpecification(eGraph);

        // Remove not needed mappings (one resource mapping per task)
        List<Task> onLocalResource = new ArrayList<>();
        onLocalResource.add(eGraph.getVertex("taskNode1"));
        onLocalResource.add(eGraph.getVertex("taskNode2"));
        onLocalResource.add(eGraph.getVertex("taskNode4"));
        onLocalResource.add(eGraph.getVertex("taskNode6"));
        onLocalResource.add(eGraph.getVertex("taskNode3"));
        onLocalResource.add(eGraph.getVertex("taskNode5"));

        reduceMapping(specification.getMappings(), onLocalResource, new ArrayList<>());

        List<Cut> cuts = new ArrayList<>();
        Set<Task> topCut = new HashSet<>();
        topCut.add(eGraph.getVertex("commNode1"));
        Set<Task> bottomCut = new HashSet<>();
        bottomCut.add(eGraph.getVertex("commNode8"));
        cuts.add(new Cut(topCut, bottomCut));

        Evaluator evaluator = new Evaluator();
        double duration = evaluator.evaluate(specification, cuts);

        assertEquals(8000.0, duration);
    }

    @Test
    void testEvaluateAllCloud(){

        // Generate specification
        EnactmentGraph eGraph = AnnotatedEnactmentSpecifications.getEnactmentGraphOverlappingDataFlow();
        EnactmentSpecification specification = setupSpecification(eGraph);

        // Remove not needed mappings (one resource mapping per task)
        List<Task> onCloudResource = new ArrayList<>();
        onCloudResource.add(eGraph.getVertex("taskNode1"));
        onCloudResource.add(eGraph.getVertex("taskNode2"));
        onCloudResource.add(eGraph.getVertex("taskNode3"));
        onCloudResource.add(eGraph.getVertex("taskNode4"));
        onCloudResource.add(eGraph.getVertex("taskNode5"));
        onCloudResource.add(eGraph.getVertex("taskNode6"));
        onCloudResource.add(eGraph.getVertex("taskNode7"));

        reduceMapping(specification.getMappings(), new ArrayList<>(), onCloudResource);

        List<Cut> cuts = new ArrayList<>();
        Set<Task> topCut = new HashSet<>();
        topCut.add(eGraph.getVertex("commNode1"));
        Set<Task> bottomCut = new HashSet<>();
        bottomCut.add(eGraph.getVertex("commNode8"));
        cuts.add(new Cut(topCut, bottomCut));

        Evaluator evaluator = new Evaluator();
        double duration = evaluator.evaluate(specification, cuts);

        assertEquals(15700.0, duration);
    }

    @Test
    void testEvaluateComplexEnactmentGraph(){

        // Generate specification
        EnactmentGraph eGraph = AnnotatedEnactmentSpecifications.getComplexEnactmentGraph();
        EnactmentSpecification specification = setupSpecification(eGraph);

        // Remove not needed mappings (one resource mapping per task)
        List<Task> onLocalResource = new ArrayList<>();
        for(int i = 1; i <= 18; i++) {
            onLocalResource.add(eGraph.getVertex("taskNode" + i));
        }
        onLocalResource.remove(eGraph.getVertex("taskNode7"));
        onLocalResource.remove(eGraph.getVertex("taskNode11"));
        onLocalResource.remove(eGraph.getVertex("taskNode10"));
        onLocalResource.remove(eGraph.getVertex("taskNode15"));

        List<Task> onCloudResource = new ArrayList<>();
        onCloudResource.add(eGraph.getVertex("taskNode7"));
        onCloudResource.add(eGraph.getVertex("taskNode11"));
        onCloudResource.add(eGraph.getVertex("taskNode10"));
        onCloudResource.add(eGraph.getVertex("taskNode15"));

        reduceMapping(specification.getMappings(), onLocalResource, onCloudResource);

        // Generates cuts to check
        List<Cut> cuts = new ArrayList<>();
        Set<Task> topCut = new HashSet<>();
        topCut.add(eGraph.getVertex("commNode8"));
        Set<Task> bottomCut = new HashSet<>();
        bottomCut.add(eGraph.getVertex("commNode19"));
        cuts.add(new Cut(topCut, bottomCut));
        Set<Task> topCut2 = new HashSet<>();
        topCut2.add(eGraph.getVertex("commNode11"));
        Set<Task> bottomCut2 = new HashSet<>();
        bottomCut2.add(eGraph.getVertex("commNode18"));
        cuts.add(new Cut(topCut2, bottomCut2));
        Set<Task> topCut3 = new HashSet<>();
        topCut3.add(eGraph.getVertex("commNode16"));
        topCut3.add(eGraph.getVertex("commNode17"));
        Set<Task> bottomCut3 = new HashSet<>();
        bottomCut3.add(eGraph.getVertex("commNode22"));
        cuts.add(new Cut(topCut3, bottomCut3));

        Evaluator evaluator = new Evaluator();
        double duration = evaluator.evaluate(specification, cuts);

        assertEquals(18500.0, duration);
    }*/
}
