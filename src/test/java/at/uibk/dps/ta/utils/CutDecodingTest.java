package at.uibk.dps.ta.utils;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import at.uibk.dps.di.scheduler.Cut;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUser;
import at.uibk.dps.ta.properties.PsGraphCut;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Task;

class CutDecodingTest {

  EnactmentGraph input;
  Task[] processes;
  Task[] comms;
  
  @Test
  void test() {
    Set<Cut> result = CutDecoding.decodeCuts(input);
    
    assertEquals(2, result.size());
    
    Iterator<Cut> iter = result.iterator();
    Cut cutFirst = iter.next();
    Cut cutSecond = iter.next();
    
    boolean isUpperCut = cutFirst.getTopCut().contains(comms[0]);
    Cut cut1 = isUpperCut ? cutFirst : cutSecond;
    Cut cut2 = isUpperCut ? cutSecond : cutFirst;
    
    assertTrue(cut1.getTopCut().contains(comms[0]));
    assertTrue(cut1.getTopCut().contains(comms[1]));
    assertTrue(cut1.getBottomCut().contains(comms[2]));
    assertTrue(cut1.getBottomCut().contains(comms[3]));
    
    assertTrue(cut2.getTopCut().contains(comms[2]));
    assertTrue(cut2.getTopCut().contains(comms[3]));
    assertTrue(cut2.getBottomCut().contains(comms[4]));
    assertTrue(cut2.getBottomCut().contains(comms[5]));   
  }
  
  @BeforeEach
  void setup() {
    
    input = new EnactmentGraph();
    
    processes = new Task[6];
    comms = new Task[6];
    
    for (int i = 0; i < 6; i++) {
      processes[i] = PropertyServiceFunctionUser.createUserTask("task" + i, "type");
      comms[i] = new Communication("comm" + i);
    }
    
    PropertyServiceDependency.addDataDependency(processes[0], comms[0], "key", input);
    PropertyServiceDependency.addDataDependency(processes[0], comms[1], "key", input);
    PropertyServiceDependency.addDataDependency(processes[1], comms[2], "key", input);
    PropertyServiceDependency.addDataDependency(processes[3], comms[3], "key", input);
    PropertyServiceDependency.addDataDependency(processes[2], comms[4], "key", input);
    PropertyServiceDependency.addDataDependency(processes[4], comms[5], "key", input);
    
    PropertyServiceDependency.addDataDependency(comms[0], processes[1], "key", input);
    PropertyServiceDependency.addDataDependency(comms[1], processes[3], "key", input);
    PropertyServiceDependency.addDataDependency(comms[2], processes[2], "key", input);
    PropertyServiceDependency.addDataDependency(comms[3], processes[4], "key", input);
    PropertyServiceDependency.addDataDependency(comms[4], processes[5], "key", input);
    PropertyServiceDependency.addDataDependency(comms[5], processes[5], "key", input);
    
    PsGraphCut.setCut(processes[0], 0);
    PsGraphCut.setCut(processes[5], 0);
    PsGraphCut.setCut(processes[1], 1);
    PsGraphCut.setCut(processes[3], 1);
    PsGraphCut.setCut(processes[2], 2);
    PsGraphCut.setCut(processes[4], 2);
  }
}
