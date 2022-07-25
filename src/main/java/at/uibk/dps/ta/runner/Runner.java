package at.uibk.dps.ta.runner;

import at.uibk.dps.di.incision.Incision;
import at.uibk.dps.di.incision.Utility;
import at.uibk.dps.di.scheduler.Cut;
import at.uibk.dps.di.scheduler.Scheduler;
import at.uibk.dps.ee.deploy.run.ImplementationRunBare;
import at.uibk.dps.ee.model.graph.EnactmentSpecification;
import at.uibk.dps.ta.modules.ApplicationTimingModule;
import com.google.gson.JsonObject;
import org.opt4j.core.Individual;
import org.opt4j.core.optimizer.Archive;
import org.opt4j.core.problem.ProblemModule;
import org.opt4j.core.start.Opt4JTask;
import org.opt4j.optimizers.ea.EvolutionaryAlgorithmModule;
import java.util.List;

public class Runner {

    private EnactmentSpecification runEA(ProblemModule module){

        // Setup EA configuration
        EvolutionaryAlgorithmModule ea = new EvolutionaryAlgorithmModule();
        ea.setGenerations(1000);
        ea.setPopulationSize(100);

        // Create Opt4J task
        Opt4JTask task = new Opt4JTask(false);
        task.init(ea, module);

        // Try to execute the Opt4J task
        try {
            task.execute();

            // Get the resulting single and optimal individual
            Individual individual = task.getInstance(Archive.class).iterator().next();
            if (individual.getPhenotype() instanceof EnactmentSpecification) {
                EnactmentSpecification spec = (EnactmentSpecification) individual.getPhenotype();
                return spec;
            } else {
                throw new IllegalStateException("Unexpected type of phenotype");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            task.close();
        }

        throw new IllegalStateException("Unexpected error while running EA");
    }

    /*private JsonObject run(ProblemModule module) {

        // Run the EA and generate the enactment specification
        EnactmentSpecification spec = new Runner().runEA(module);

        // Extract the cuts generated from the EA
        Scheduler scheduler = new Scheduler();
        scheduler.setResources(spec);
        List<Cut> cuts = scheduler.extractCuts(spec.getEnactmentGraph(), scheduler.rankAndSort(spec));

        // Cut the workflow and adapt the specification
        for(Cut cut: cuts) {
            new Incision().cut(spec, cut.getTopCut(), cut.getBottomCut());
        }
        String specificationAdapted = Utility.fromEnactmentSpecificationToString(spec);

        // Run the workflow
        return new ImplementationRunBare().implement("{'input': 1}", specificationAdapted, Utility.DE_CONFIGURATION);
    }

    public static void main(String[] args) {
        new Runner().run(new ApplicationTimingModule());
    }*/
}
