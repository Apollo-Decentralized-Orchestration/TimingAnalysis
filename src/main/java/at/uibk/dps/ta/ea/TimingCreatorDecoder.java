package at.uibk.dps.ta.ea;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.opt4j.core.genotype.IntegerGenotype;
import org.opt4j.core.problem.Creator;
import org.opt4j.core.problem.Decoder;
import org.opt4j.core.start.Constant;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import at.uibk.dps.ee.model.graph.EnactmentSpecification;
import at.uibk.dps.ee.model.graph.SpecificationProvider;
import at.uibk.dps.ee.model.utils.UtilsCopy;
import at.uibk.dps.ta.properties.PsGraphCut;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * Used to en- and decode the genotype optimizing the cuts to improve the
 * application latency.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class TimingCreatorDecoder
    implements Creator<IntegerGenotype>, Decoder<IntegerGenotype, EnactmentSpecification> {

  protected final EnactmentSpecification spec;
  protected final int maxCutNum;
  protected final List<Task> taskOrderList;
  protected final Random rand;

  @Inject
  public TimingCreatorDecoder(SpecificationProvider specProvider,
      @Constant(namespace = TimingCreatorDecoder.class, value = "maxCutNumber") int maxCutNum,
      Random rand) {
    this.spec = specProvider.getSpecification();
    this.maxCutNum = maxCutNum;
    this.taskOrderList = spec.getEnactmentGraph().getVertices().stream()
        .filter(task -> TaskPropertyService.isProcess(task)).collect(Collectors.toList());
    this.rand = rand;
  }

  @Override
  public EnactmentSpecification decode(IntegerGenotype intGeno) {
    EnactmentSpecification specCopy = UtilsCopy.deepCopySpec(spec, "copy");
    for (int idx = 0; idx < taskOrderList.size(); idx++) {
      Task task = taskOrderList.get(idx);
      int cutIdx = intGeno.get(idx);
      Task copyTask = specCopy.getEnactmentGraph().getVertex(task.getId());
      PsGraphCut.setCut(copyTask, cutIdx);
    }
    return specCopy;
  }

  @Override
  public IntegerGenotype create() {
    IntegerGenotype result = new IntegerGenotype(0, maxCutNum);
    result.init(rand, taskOrderList.size());
    return result;
  }
}
