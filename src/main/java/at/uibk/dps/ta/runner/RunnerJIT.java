package at.uibk.dps.ta.runner;

import at.uibk.dps.di.JIT_C.JIT;
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

import java.util.Iterator;
import java.util.Set;

public class RunnerJIT {

    private void setDur(EnactmentSpecification specification, EnactmentGraph eGraph, String task, double Vs, double Vm, double Vl){

        MappingsConcurrent mappings = specification.getMappings();
        Set<Mapping<Task, Resource>> n = mappings.getMappings(eGraph.getVertex(task));

        for (Mapping<Task, Resource> map : n) {
            String id = map.getTarget().getId();
            if ("Vs".equals(id)) {
                PropertyServiceScheduler.setDuration(map, Vs);
            } else if ("Vm".equals(id)) {
                PropertyServiceScheduler.setDuration(map, Vm);
            } else if ("Vl".equals(id)) {
                PropertyServiceScheduler.setDuration(map, Vl);
            }
        }
    }

    private EnactmentSpecification setupSpecification(EnactmentGraph eGraph, String mappingsPath) {

        // Generate the specification
        final EnactmentGraphProvider eGraphProvider = () -> eGraph;
        final ResourceGraphProvider rGraphProv = new ResourceGraphProviderFile(mappingsPath);
        final SpecificationProviderFile
            specProv = new SpecificationProviderFile(eGraphProvider, rGraphProv, mappingsPath);
        final EnactmentSpecification specification = specProv.getSpecification();


        // Set up function durations on resources
        setDur(specification, eGraph, "taskNode1", 4.0, 2.0, 1.0);
        setDur(specification, eGraph, "taskNode2", 6.0, 4.0, 2.0);
        setDur(specification, eGraph, "taskNode3", 16.0, 9.0, 6.0);
        setDur(specification, eGraph, "taskNode4", 12.0, 7.0, 4.0);
        setDur(specification, eGraph, "taskNode5", 11.0, 8.0, 5.0);
        setDur(specification, eGraph, "taskNode6", 7.0, 3.0, 2.0);
        setDur(specification, eGraph, "taskNode7", 18.0, 12.0, 8.0);
        setDur(specification, eGraph, "taskNode8", 13.0, 9.0, 5.0);
        setDur(specification, eGraph, "taskNode9", 15.0, 12.0, 9.0);

        return specification;
    }

    private void run(){
        EnactmentSpecification specification = setupSpecification(eGraphs.getJITWF(), "src/test/resources/mapping_jit.json");

        new JIT().schedule(specification, 50.0);

        /*
        EnactmentGraphViewer.view(specification.getEnactmentGraph());
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
    }

    public static void main(String[] args) {
        new RunnerJIT().run();
    }
}
