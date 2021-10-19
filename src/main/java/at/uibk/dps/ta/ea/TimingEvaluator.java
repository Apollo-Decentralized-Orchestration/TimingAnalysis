package at.uibk.dps.ta.ea;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import org.opt4j.core.Objective;
import org.opt4j.core.Objective.Sign;
import org.opt4j.core.start.Constant;
import org.opt4j.core.Objectives;
import com.google.inject.Inject;
import at.uibk.dps.di.scheduler.Cut;
import at.uibk.dps.ee.model.graph.EnactmentSpecification;
import at.uibk.dps.ee.model.graph.MappingsConcurrent;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping.EnactmentMode;
import at.uibk.dps.ta.evaluator.Evaluator;
import at.uibk.dps.ta.properties.PsGraphCut;
import at.uibk.dps.ta.utils.CutDecoding;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;

/**
 * Evaluator class used for optmization by odse.
 * 
 * @author Fedor Smirnov
 *
 */
public class TimingEvaluator implements org.opt4j.core.problem.Evaluator<EnactmentSpecification> {

  protected final Objective applicationLatency = new Objective("Latency", Sign.MIN);
  protected final int maxCutNum;

  @Inject
  public TimingEvaluator(
      @Constant(namespace = TimingCreatorDecoder.class, value = "maxCutNumber") int maxCutNum) {
    this.maxCutNum = maxCutNum;
  }

  @Override
  public Objectives evaluate(EnactmentSpecification spec) {
    Evaluator timingEval = new Evaluator();
    removeMappings(spec.getMappings());
    Set<Cut> cuts = CutDecoding.decodeCuts(spec.getEnactmentGraph(), maxCutNum);
    double result = timingEval.evaluate(spec, new ArrayList<>(cuts));
    Objectives objectives = new Objectives();
    objectives.add(applicationLatency, result);
    return objectives;
  }

  /**
   * Removes the mappings which are not necessary, given the provided cut
   * assignment.
   * 
   * @param mappings all mappings
   */
  protected void removeMappings(MappingsConcurrent mappings) {
    Set<Mapping<Task, Resource>> toRemove = mappings.mappingStream()
        .filter(mapping -> !isCorrectMapping(mapping)).collect(Collectors.toSet());
    toRemove.forEach(map -> mappings.removeMapping(map));
  }

  protected boolean isCorrectMapping(Mapping<Task, Resource> mapping) {
    int cutIdx = PsGraphCut.getCut(mapping.getSource());
    boolean isOnCloudRes =
        PropertyServiceMapping.getEnactmentMode(mapping).equals(EnactmentMode.Serverless);
    boolean cutIsOnCloud = cutIdx != 0;
    boolean correctCloud = isOnCloudRes && cutIsOnCloud;
    boolean correctLocal = !isOnCloudRes && !cutIsOnCloud;
    return correctCloud || correctLocal;
  }
}
