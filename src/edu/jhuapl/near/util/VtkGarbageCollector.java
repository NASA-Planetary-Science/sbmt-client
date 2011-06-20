package edu.jhuapl.near.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import vtk.vtkGlobalJavaHash;
import vtk.vtkObjectBase;

/**
 * This class helps to provide a solution in concurrent garbage collection issue
 * with VTK. This class allow automatic garbage collection done in a specific
 * thread such as the EDT.
 *
 * @author sebastien jourdain - sebastien.jourdain@kitware.com
 */
public class VtkGarbageCollector {

  private ScheduledExecutorService executor;
  private Runnable deleteRunnable;
  private Runnable deferredEdtRunnable;
  private long periodTime;
  private TimeUnit timeUnit;
  private boolean autoCollectionRunning;
  private boolean debug;

  /**
   * Build a garbage collector which is configured to garbage collect every
   * seconds but has not been started yet. The user has to call
   * SetAutoGarbageCollection(true) to make it start.
   */
  public VtkGarbageCollector() {
    // Default settings
    debug = true;
    periodTime = 1;
    timeUnit = TimeUnit.SECONDS;
    autoCollectionRunning = false;
    //
    executor = Executors.newSingleThreadScheduledExecutor();
    deleteRunnable = new Runnable() {

      public void run() {
        // Do the delete here
          System.gc();
        int num = vtkGlobalJavaHash.GC();
        if (debug) {
          System.out.println("vtkJavaGarbageCollector deleted " + num + " references.");
          System.out.println("vtk alive objects:");

          TreeMap<String, Integer> aliveMap = new TreeMap<String, Integer>();
          int count = 0;
          Set entries = vtkGlobalJavaHash.PointerToReference.entrySet();
          Iterator iter = entries.iterator();
          System.out.println("/-----------------");
          while (iter.hasNext())
          {
            Map.Entry entry = (Map.Entry)iter.next();
            vtkObjectBase obj = (vtkObjectBase)((WeakReference)entry.getValue()).get();
            if (obj != null)
            {
                if (aliveMap.containsKey(obj.GetClassName()))
                    aliveMap.put(obj.GetClassName(), 1+aliveMap.get(obj.GetClassName()));
                else
                    aliveMap.put(obj.GetClassName(), 1);
                //System.out.println(obj.GetClassName());
                //if (obj instanceof vtkPolyData)
                //    System.out.println(obj);
                ++count;
            }
          }
          for (String s : aliveMap.keySet())
          {
              System.out.println(s + " : " + aliveMap.get(s));
          }
          System.out.println("-----------------/");
          System.out.println("number of objects alive: " + count);
        }
      }
    };
    deferredEdtRunnable = new Runnable() {

      public void run() {
        SwingUtilities.invokeLater(deleteRunnable);
      }
    };
  }

  /**
   * Set the schedule time that should be used to send a garbage collection
   * request to the EDT.
   *
   * @param period
   * @param timeUnit
   */
  public void SetScheduleTime(long period, TimeUnit timeUnit) {
    this.periodTime = period;
    this.timeUnit = timeUnit;
    SetAutoGarbageCollection(autoCollectionRunning);
  }

  /**
   * Whether to print out when garbage collection is run.
   *
   * @param debug
   */
  public void SetDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   * Start or stop the automatic garbage collection in the EDT.
   *
   * @param doGarbageCollectionInEDT
   */
  public void SetAutoGarbageCollection(boolean doGarbageCollectionInEDT) {
    autoCollectionRunning = doGarbageCollectionInEDT;
    executor.shutdown();
    if (doGarbageCollectionInEDT) {
      executor = Executors.newSingleThreadScheduledExecutor();
      executor.scheduleAtFixedRate(deferredEdtRunnable, periodTime, periodTime, timeUnit);
    }
  }

  /**
   * @return the runnable that do the garbage collection. This could be used
   * if you want to execute the garbage collection in another thread than
   * the EDT.
   */
  public Runnable GetDeleteRunnable() {
    return deleteRunnable;
  }

  private static List<Field> getAllFields(Class<?> type)
  {
      List<Field> fields = new ArrayList<Field>();
      for (Class<?> c = type; c != null; c = c.getSuperclass())
      {
          fields.addAll(Arrays.asList(c.getDeclaredFields()));
      }
      return fields;
  }

  public static void setFieldsToNull(Object o, boolean includeInherited)
  {
        try
        {
            List<Field> fields = null;
            if (includeInherited)
                fields = getAllFields(o.getClass());
            else
                fields = Arrays.asList(o.getClass().getDeclaredFields());

            for (Field f : fields)
            {
                if (!Modifier.isStatic(f.getModifiers()))
                {
                    //System.out.println(f.getName());
                    f.setAccessible(true);
                    try
                    {
                        //setFieldsToNull(o, includeInherited);
                        if (f.get(o) instanceof vtkObjectBase)
                            f.set(o, null);
                    }
                    catch (IllegalArgumentException e)
                    {

                    }
                }
            }
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

  }
}
