package at.uibk.dps.ta.properties;

import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.AbstractPropertyService;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * Property service for annotating graph cuts (used by the ea-based scheduler).
 * 
 * @author Fedor Smirnov
 *
 */
public class PsGraphCut extends AbstractPropertyService{

  /**
   * No constructor.
   */
  private PsGraphCut() {
  }
  
  private static String psNameCut = Property.CUT.name();
  
  protected enum Property{
    /**
     * The cut a node corresponds to
     */
    CUT
  }
  
  /**
   * Sets the cut for the given node
   * 
   * @param task the given node
   * @param cut the cut to set
   */
  public static void setCut(Task task, int cut) {
    checkTask(task);
    task.setAttribute(psNameCut, cut);
  }
  
  /**
   * Returns the cut assignment of the given task
   * 
   * @param task the given task
   * @return the cut assignment
   */
  public static int getCut(Task task) {
    checkAttribute(task, psNameCut);
    return (int) getAttribute(task, psNameCut);
  }
  
  static void checkTask(Task task) {
    if (!TaskPropertyService.isProcess(task)) {
      throw new IllegalArgumentException("Task " + task + " is not a process.");
    }
  }
  
  
}
