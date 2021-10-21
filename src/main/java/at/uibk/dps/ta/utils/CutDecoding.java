package at.uibk.dps.ta.utils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import at.uibk.dps.di.scheduler.Cut;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ta.properties.PsGraphCut;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * Converts the cut index assignment to the {@link Cut} representation.
 * 
 * @author Fedor Smirnov
 */
public class CutDecoding {

  /**
   * No constructor
   */
  private CutDecoding() {}


  /**
   * Reads the cuts annotated in the given graph.
   * 
   * @param eGraph the enactment graph
   * @param maxCutNumber the maximum number of cuts
   * @return the cuts annotated in the given graph
   */
  public static Set<Cut> decodeCuts(final EnactmentGraph eGraph) {
    Set<Cut> result = new HashSet<>();
    for (int cutIdx = 1; cutIdx <= findMaxCutNum(eGraph); cutIdx++) {
      result.add(findCutForIdx(eGraph, cutIdx));
    }
    return result;
  }

  /**
   * Finds the max cut number in the graph
   * 
   * @param graph the enactment graph
   * @return the largest cut label present in the graph
   */
  static int findMaxCutNum(final EnactmentGraph graph) {
    Task maxCutTask = graph.getVertices().stream().filter(task -> TaskPropertyService.isProcess(task))
        .max(Comparator.comparing(task -> PsGraphCut.getCut(task))).get();
    return PsGraphCut.getCut(maxCutTask);
  }

  /**
   * Finds the cut for the given cut idx.
   * 
   * @param eGraph the enactment graph
   * @param cutIdx the index of the cut
   * @return the cut for the given cut idx
   */
  static Cut findCutForIdx(final EnactmentGraph eGraph, int cutIdx) {
    Set<Task> cutProcesses = eGraph.getVertices().stream()
        .filter(task -> TaskPropertyService.isProcess(task) && PsGraphCut.getCut(task) == cutIdx)
        .collect(Collectors.toSet());
    Set<Task> upperCut = findCutBoundary(cutProcesses, eGraph, true);
    Set<Task> bottomCut = findCutBoundary(cutProcesses, eGraph, false);
    return new Cut(upperCut, bottomCut);
  }

  /**
   * Finds the cut boundary
   * 
   * @param cutProcesses the processes inside the cut
   * @param eGraph the enactment graph
   * @param upper true iff looking for the upper cut
   * @return the cut boundary
   */
  static Set<Task> findCutBoundary(Set<Task> cutProcesses, EnactmentGraph eGraph, boolean upper) {
    Set<Task> result = new HashSet<>();
    for (Task cutTask : cutProcesses) {
      for (Dependency incEdge : upper ? eGraph.getInEdges(cutTask) : eGraph.getOutEdges(cutTask)) {
        Task comm = upper ? eGraph.getSource(incEdge) : eGraph.getDest(incEdge);
        if (!result.contains(comm)) {
          for (Dependency incIncEdge : upper ? eGraph.getInEdges(comm) : eGraph.getOutEdges(comm)) {
            Task otherProcess = upper ? eGraph.getSource(incIncEdge) : eGraph.getDest(incIncEdge);
            if (!cutProcesses.contains(otherProcess)) {
              result.add(comm);
            }
          }
        }
      }
    }
    return result;
  }
}
