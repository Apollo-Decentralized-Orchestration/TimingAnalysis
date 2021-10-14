package at.uibk.dps.ta.evaluator;

import at.uibk.dps.di.scheduler.Cut;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentSpecification;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceTiming;
import net.sf.opendse.model.Communication;
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
     * Get all root nodes (nodes containing attribute Leaf) in
     * an {@link EnactmentGraph}.
     *
     * @param eGraph the graph to check for root nodes.
     *
     * @return a collection of leaf nodes.
     */
    private Collection<Task> getRootNodes(final EnactmentGraph eGraph) {
        return eGraph.getVertices()
            .stream()
            .filter(task -> task instanceof Communication && PropertyServiceData.isRoot(task))
            .collect(Collectors.toList());
    }

    /**
     * Jump over communication nodes and return previous task nodes.
     *
     * @param eGraph the {@link EnactmentGraph} to inspect.
     * @param node to get predecessor tasks from.
     *
     * @return list of predecessor task nodes.
     */
    private Collection<Task> getPredecessorTaskNodes(final EnactmentGraph eGraph, final Task node) {
        if(node instanceof Communication) {

            // The predecessor of a communication node is a task node
            return eGraph.getPredecessors(node);
        } else {

            // The predecessor of a task node is a communication node (which predecessor is a task node)
            final Collection<Task> predecessorTasks = new ArrayList<>();
            eGraph.getPredecessors(node).forEach((pS) -> predecessorTasks.addAll(eGraph.getPredecessors(pS)));
            return predecessorTasks;
        }
    }

    /**
     * Jump over communication nodes and return next task nodes.
     *
     * @param eGraph the {@link EnactmentGraph} to inspect.
     * @param node to get successor tasks from.
     *
     * @return list of successor task nodes.
     */
    private Collection<Task> getSuccessorTaskNodes(EnactmentGraph eGraph, Task node) {
        if(node instanceof Communication) {

            // The successor of a communication node is a task node
            return eGraph.getSuccessors(node);
        } else {

            // The successor of a task node is a communication node (which successor is a task node)
            final Collection<Task> successorTasks = new ArrayList<>();
            eGraph.getSuccessors(node).forEach((pS) -> successorTasks.addAll(eGraph.getSuccessors(pS)));
            return successorTasks;
        }
    }

    /**
     * Get the duration of a task based on the mapping and its resource.
     *
     * @param task the task to get the duration from.
     * @param specification the specification.
     * @param useGlobalLatency true if the global latency should be used.
     *
     * @return the duration of the task on the specified resource.
     */
    private double getDuration(final Task task, final EnactmentSpecification specification, boolean useGlobalLatency) {

        // Get the mappings of the specified task
        final Set<Mapping<Task, Resource>> taskMappings = specification.getMappings().getMappings(task);

        // Only one mapping should be present
        if(taskMappings.size() == 1) {

            final Mapping<Task, Resource> map = taskMappings.iterator().next();

            // Get the duration of the task at the resource
            double duration = PropertyServiceTiming.getDuration(map);

            // Add the latency
            duration += useGlobalLatency ? PropertyServiceTiming.getLatencyGlobal(map.getTarget()) : PropertyServiceTiming.getLatencyLocal(map.getTarget());

            // Return total duration
            return duration;
        } else {
            throw new IllegalArgumentException(taskMappings.size() + " mappings exist for task " + task.getId() + ". There should be exactly one mapping.");
        }
    }

    /**
     * Check if a task in the {@link EnactmentGraph} has a communication node as predecessor which is a top cut.
     *
     * @param eGraph the graph to check.
     * @param current the task to check for predecessor top cuts.
     * @param cuts all available top cuts.
     *
     * @return true if a predecessor has a top cut global latency should be used
     */
    private boolean hasPredecessorTopCutAndUseGlobalLatency(final EnactmentGraph eGraph, final Task current, final List<Cut> cuts) {

        // Get all predecessors
        final Collection<Task> predecessor = eGraph.getPredecessors(current);
        final Queue<Task> queue = new LinkedList<>(predecessor);

        // Iterate over all predecessors
        while(!queue.isEmpty()) {

            // Get first task in the queue
            final Task task = queue.poll();
            boolean continueAbove = true;

            // Iterate over all cuts
            for(Cut cut: cuts) {

                // Check for top cut
                if(cut.getTopCut().contains(task)) {

                    // A predecessor has a top cut. Now check if immediate predecessor is top cut
                    return predecessor.contains(task);
                } else if (cut.getBottomCut().contains(task)) {
                    continueAbove = false;
                }
            }

            // Check predecessor tasks
            if(continueAbove){
                queue.addAll(eGraph.getPredecessors(task));
            }
        }
        return false;
    }

    /**
     * Get the duration of the graph.
     *
     * @param specification contianing the grap, mappings, resources, ...
     * @param cuts the cuts to consider while calulcating the duration.
     *
     * @return the expected duration of the graph.
     */
    public double evaluate(final EnactmentSpecification specification, final List<Cut> cuts) {

        // Get the enactment graph
        final EnactmentGraph eGraph = specification.getEnactmentGraph();

        // Store the durations of the leaf nodes
        final List<Double> durationLeafNodes = new ArrayList<>();

        // Contains tasks to be checked
        final Queue<Task> queue = new LinkedList<>();

        // Keep track of the task durations
        final HashMap<String, Double> taskDurations = new HashMap<>();

        // Add successors of root communication nodes to the queue and calculate their durations
        getRootNodes(eGraph).forEach((rootNodes) -> {
            getSuccessorTaskNodes(eGraph, rootNodes).forEach((successor) -> { queue.add(successor);
                    taskDurations.put(successor.getId(), getDuration(successor, specification, hasPredecessorTopCutAndUseGlobalLatency(eGraph, successor, cuts)));});});

        // Iterate over all task in the queue
        while(!queue.isEmpty()) {

            // Get the first element in the queue
            final Task current = queue.poll();

            // Get the successors of the task to check
            final Collection<Task> successors = getSuccessorTaskNodes(eGraph, current);

            // Iterate over all successors
            for(Task successor: successors) {

                // Get all predecessors of the successor (to check if all durations are set)
                final Collection<Task> predecessors = getPredecessorTaskNodes(eGraph, successor);

                // Keeps track if all predecessor durations are set
                boolean allSet = true;

                // Keep the maximum duration of the predecessor tasks
                double maxDuration = 0;

                // Check if task is in cut and if we should use local latency
                boolean useGlobalLatency = hasPredecessorTopCutAndUseGlobalLatency(eGraph, successor, cuts);

                // Iterate over all predecessors
                for(Task predecessor: predecessors){

                    // Check if all predecessors are set
                    if(!taskDurations.containsKey(predecessor.getId())) {

                        // Not all predecessors are set
                        allSet = false;
                        break;
                    }else {

                        // Get the duration of the predecessor task
                        double duration = taskDurations.get(predecessor.getId()) + getDuration(successor, specification, useGlobalLatency);

                        // Get new maximum duration
                        if(maxDuration < duration){
                            maxDuration = duration;
                        }
                    }
                }

                // If all predecessor durations are set
                if(allSet) {

                    // Add newly calculated duration for successor task
                    taskDurations.put(successor.getId(), maxDuration);

                    // Check if following communication node is leaf node
                    final Collection<Task> commNodeSuccessors = eGraph.getSuccessors(successor);
                    for (Task commNodeSuccessor: commNodeSuccessors){
                        if(PropertyServiceData.isLeaf(commNodeSuccessor)){
                            durationLeafNodes.add(maxDuration);
                        }
                    }

                } else {
                    // Add task to queue (check later if all predecessor durations are set)
                    queue.add(current);
                }
            }

            // Add all successors to check
            queue.addAll(successors);
        }

        // Return the maximum duration of the leaf nodes
        return Collections.max(durationLeafNodes);
    }
}
