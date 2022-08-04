package at.uibk.dps.ta.evaluator;

import at.uibk.dps.di.properties.PropertyServiceScheduler;
import at.uibk.dps.di.scheduler.Cut;
import at.uibk.dps.di.scheduler.Scheduler;
import at.uibk.dps.ee.model.graph.*;
import at.uibk.dps.ee.visualization.model.EnactmentGraphViewer;
import at.uibk.dps.ta.tmp.SpecificationProviderTest;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to evaluate a given task - resource mapping.
 *
 * @author Stefan Pedratscher
 */
public class Evaluator {

    /**
     * The amount of needed mappings.
     */
    protected final static int NUM_MAPPINGS = 1;


    /**
     * Check if a task in the {@link EnactmentGraph} has a communication
     * node as predecessor which is a top cut.
     *
     * @param specification the specification.
     * @param current the task to check for predecessor top cuts.
     * @param cuts all available top cuts.
     *
     * @return true if a predecessor has a top cut global latency should be used
     */
    protected boolean prevOnSameResource(final EnactmentSpecification specification, final Task current, final List<Cut> cuts) {

        // Get all predecessor communication nodes
        final Collection<Task> predecessors = specification.getEnactmentGraph().getPredecessors(current);

        // Get task mappings
        MappingsConcurrent mappings = specification.getMappings();

        // Get resource of task
        Resource resource = mappings.getMappings(current).iterator().next().getTarget();

        // Iterate over all predecessor communication nodes
        for (Task predecessor : predecessors) {

            // Check if predecessor communication node is not in top cut
            if(cuts.stream().noneMatch(cut -> cut.getTopCut().contains(predecessor))) {

                // Get the predecessor task nodes
                final Collection<Task> predecessorTask = specification.getEnactmentGraph().getPredecessors(predecessor);

                // Iterate over all predecessor tasks
                for (Task task : predecessorTask){

                    // Check if predecessor task is on same resource
                    if(resource.equals(mappings.getMappings(task).iterator().next().getTarget())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Evaluate.
     *
     * @param specification
     * @param cuts
     * @return
     */
    public double evaluate(final EnactmentSpecification specification, final List<Cut> cuts) {

        // Use scheduler functionality
        /*Scheduler scheduler = new Scheduler();

        // Get the enactment graph
        EnactmentGraph eGraph = specification.getEnactmentGraph();

        // Remember the finish time of the tasks
        Map<Task, Double> mapFinishTime = new HashMap<>();

        // Get the resource mappings
        Map<String, at.uibk.dps.di.scheduler.Resource> mapResource = scheduler.getResources(specification);

        // Rank the tasks based on upward rank (no latency between tasks) and sort them
        ArrayList<Task> rankedTasks = scheduler.rankAndSort(specification);

        // Iterate over all ranked tasks
        for(Task rankedTask: rankedTasks) {

            // Get predecessor task nodes of current ranked task
            Collection<Task> predecessorTaskNodes = scheduler.getPredecessorTaskNodes(eGraph, rankedTask);

            // Find the earliest start time by inspecting the finish times of the predecessor tasks (maximum finish time of predecessor tasks)
            Set<Double> finishTimes = mapFinishTime.entrySet().stream().filter(kv -> predecessorTaskNodes.contains(kv.getKey())).map(Map.Entry::getValue).collect(Collectors.toSet());
            double earliestStartTime = finishTimes.isEmpty() ? 0.0 : Collections.max(finishTimes);

            // Check if previous task was on the same resource
            boolean prevOnSameResource = predecessorTaskNodes.stream().anyMatch(predecessor -> prevOnSameResource(specification, rankedTask, cuts));

            // Get mapping of task and check if only one mapping is present
            Set<Mapping<Task, Resource>> mappings = specification.getMappings().getMappings(rankedTask);

            // Check if only one mapping is present
            if(mappings.size() == NUM_MAPPINGS){

                // Get the only mapping of the task
                Mapping<Task, Resource> map = specification.getMappings().getMappings(rankedTask).iterator().next();

                // Set that the resource is used now
                double finishTime = mapResource.get(map.getTarget().getId()).setResource(earliestStartTime, PropertyServiceScheduler
                    .getAvgDurationOnAllResources(map), prevOnSameResource);

                // Set the finish time of the task and its resource type
                mapFinishTime.put(rankedTask, finishTime);
            } else {
                throw new IllegalArgumentException(mappings.size() + " mappings exist for task " + rankedTask.getId() + ". There should be exactly one mapping.");
            }
        }

        Set<Double> resourceDurations = mapResource.values().stream()
            .map(at.uibk.dps.di.scheduler.Resource::maxDuration)
            .collect(Collectors.toSet());

        return resourceDurations.isEmpty() ? 0.0 : Collections.max(resourceDurations);*/
        return 1.1;
    }

    public static void main(String[] args) {
        SpecificationProviderTest specificationProviderTest = new SpecificationProviderTest(
            2000,2000,0,0,200,500,1,1000
        );
        final EnactmentSpecification specification = specificationProviderTest.generateSpec(
            2000,2000,0,0,200,500,1,1000
        );

        List<Cut> cuts = new Scheduler().schedule(specification);

        EnactmentGraphViewer.view(specification.getEnactmentGraph());

        Evaluator e = new Evaluator();
        System.out.println(e.evaluate(specification, cuts));
    }
}
