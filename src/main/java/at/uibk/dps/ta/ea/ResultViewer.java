package at.uibk.dps.ta.ea;

import org.opt4j.core.Individual;
import org.opt4j.core.optimizer.Archive;
import org.opt4j.core.optimizer.Optimizer;
import org.opt4j.core.optimizer.OptimizerStateListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import at.uibk.dps.ee.model.graph.EnactmentSpecification;
import at.uibk.dps.ee.model.graph.SpecificationProvider;
import at.uibk.dps.ee.visualization.model.EnactmentGraphViewer;

/**
 * Shows the enactment graph of the best solution at the end of the
 * optimization.
 * 
 * @author Fedor Smirnov
 *
 */
@Singleton
public class ResultViewer implements OptimizerStateListener {

  protected final EnactmentSpecification spec;
  protected final Archive archive;

  @Inject
  public ResultViewer(SpecificationProvider specProv, Archive archive) {
    this.spec = specProv.getSpecification();
    this.archive = archive;
  }

  @Override
  public void optimizationStarted(Optimizer arg0) {
    // do nothing
  }

  @Override
  public void optimizationStopped(Optimizer arg0) {
    // get the best individual
    if (archive.size() != 1) {
      throw new IllegalStateException("We expect exactly one solution");
    }

    Individual bestIndi = archive.iterator().next();
    if (bestIndi.getPhenotype() instanceof EnactmentSpecification) {
      EnactmentSpecification spec = (EnactmentSpecification) bestIndi.getPhenotype();
      EnactmentGraphViewer.view(spec.getEnactmentGraph());
    } else {
      throw new IllegalStateException("Unexpected type of phenotype");
    }
  }
}
