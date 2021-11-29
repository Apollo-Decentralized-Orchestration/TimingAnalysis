package at.uibk.dps.ta.modules;

import org.opt4j.core.config.annotations.Info;
import org.opt4j.core.config.annotations.Order;
import org.opt4j.core.problem.ProblemModule;
import org.opt4j.core.start.Constant;
import at.uibk.dps.ee.model.graph.SpecificationProvider;
import at.uibk.dps.ta.ea.ResultViewer;
import at.uibk.dps.ta.ea.TimingCreatorDecoder;
import at.uibk.dps.ta.ea.TimingEvaluator;
import at.uibk.dps.ta.tmp.SpecificationProviderTest;

public class ApplicationTimingModule extends ProblemModule {

  @Info("Maximum number of cuts")
  @Order(1)
  @Constant(namespace = TimingCreatorDecoder.class, value = "maxCutNumber")
  public int maxCutNumber = 1;

  @Info("Duration of tasks on the cloud")
  @Order(2)
  @Constant(namespace = SpecificationProviderTest.class, value = "durCloud")
  public double durCloud = 2000;

  @Info("Duration of tasks on the local resource")
  @Order(3)
  @Constant(namespace = SpecificationProviderTest.class, value = "durLocal")
  public double durLocal = 2000;

  @Info("Transmission time between local tasks")
  @Order(4)
  @Constant(namespace = SpecificationProviderTest.class, value = "transLocalLocal")
  public double transLocalLocal = 0;

  @Info("Transmission time to the local machine")
  @Order(5)
  @Constant(namespace = SpecificationProviderTest.class, value = "transLocalGlobal")
  public double transLocalGlobal = 0;

  @Info("Transmission time between tasks in the cloud")
  @Order(6)
  @Constant(namespace = SpecificationProviderTest.class, value = "transCloudLocal")
  public double transCloudLocal = 200;

  @Info("Transmission time to the cloud")
  @Order(7)
  @Constant(namespace = SpecificationProviderTest.class, value = "transCloudGlobal")
  public double transCloudGlobal = 500;

  @Info("Number of available instances on local resource")
  @Order(8)
  @Constant(namespace = SpecificationProviderTest.class, value = "numInstancesLocal")
  public int numInstancesLocal = 1;

  @Info("Number of available instances on cloud resource")
  @Order(9)
  @Constant(namespace = SpecificationProviderTest.class, value = "numInstancesCloud")
  public int numInstancesCloud = 1000;

  @Override
  protected void config() {
    bindProblem(TimingCreatorDecoder.class, TimingCreatorDecoder.class, TimingEvaluator.class);
    bind(SpecificationProvider.class).to(SpecificationProviderTest.class);
    addOptimizerStateListener(ResultViewer.class);
  }

  public int getMaxCutNumber() {
    return maxCutNumber;
  }

  public void setMaxCutNumber(int maxCutNumber) {
    this.maxCutNumber = maxCutNumber;
  }

  public double getDurCloud() {
    return durCloud;
  }

  public void setDurCloud(double durCloud) {
    this.durCloud = durCloud;
  }

  public double getDurLocal() {
    return durLocal;
  }

  public void setDurLocal(double durLocal) {
    this.durLocal = durLocal;
  }

  public double getTransLocalLocal() {
    return transLocalLocal;
  }

  public void setTransLocalLocal(double transLocalLocal) {
    this.transLocalLocal = transLocalLocal;
  }

  public double getTransLocalGlobal() {
    return transLocalGlobal;
  }

  public void setTransLocalGlobal(double transLocalGlobal) {
    this.transLocalGlobal = transLocalGlobal;
  }

  public double getTransCloudLocal() {
    return transCloudLocal;
  }

  public void setTransCloudLocal(double transCloudLocal) {
    this.transCloudLocal = transCloudLocal;
  }

  public double getTransCloudGlobal() {
    return transCloudGlobal;
  }

  public void setTransCloudGlobal(double transCloudGlobal) {
    this.transCloudGlobal = transCloudGlobal;
  }

  public int getNumInstancesLocal() {
    return numInstancesLocal;
  }

  public void setNumInstancesLocal(int numInstancesLocal) {
    this.numInstancesLocal = numInstancesLocal;
  }

  public int getNumInstancesCloud() {
    return numInstancesCloud;
  }

  public void setNumInstancesCloud(int numInstancesCloud) {
    this.numInstancesCloud = numInstancesCloud;
  }
}
