package at.uibk.dps.ta.properties;

import at.uibk.dps.ee.model.properties.PropertyServiceMapping;
import at.uibk.dps.ee.model.properties.PropertyServiceResource;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyServiceTimingTest {
/*
  @Test
  public void testMappingDuration() {
    Mapping<Task, Resource> mapping = PropertyServiceMapping.createMapping(new Task("task"),
        new Resource("res"), PropertyServiceMapping.EnactmentMode.Local, "impl1");

    double duration = 12;
    PropertyServiceTiming.setDuration(mapping, duration);
    assertEquals(PropertyServiceTiming.getDuration(mapping), duration);
  }

  @Test
  public void testLatency() {
    String id = "resId";
    double latencyGlobal = 500.0;
    double latencyLocal = 200.0;

    Resource result = PropertyServiceResource.createResource(id);
    assertEquals(-1.0, PropertyServiceTiming.getLatencyGlobal(result));
    assertEquals(-1.0, PropertyServiceTiming.getLatencyLocal(result));
    PropertyServiceTiming.setLatencyGlobal(result, latencyGlobal);
    PropertyServiceTiming.setLatencyLocal(result, latencyLocal);
    assertEquals(latencyGlobal, PropertyServiceTiming.getLatencyGlobal(result));
    assertEquals(latencyLocal, PropertyServiceTiming.getLatencyLocal(result));
  }

  @Test
  public void testInstances() {
    String id = "resId";
    Resource result = PropertyServiceResource.createResource(id);
    assertEquals(-1, PropertyServiceTiming.getInstances(result));
    PropertyServiceTiming.setInstances(result, 5);
    assertEquals(5, PropertyServiceTiming.getInstances(result));
  }
  */
}
