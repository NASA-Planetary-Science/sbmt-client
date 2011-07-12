package edu.jhuapl.near.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

  private TreeMap<String, ArrayList<Integer>> liveObjectHistoryMap =
      new TreeMap<String, ArrayList<Integer>>();

  /**
   * Build a garbage collector which is configured to garbage collect every
   * seconds but has not been started yet. The user has to call
   * SetAutoGarbageCollection(true) to make it start.
   */
  public VtkGarbageCollector() {
    // Default settings
    debug = false;
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
          updateLiveObjectHistory();
          saveLiveObjectHistory();

          System.out.println("vtkJavaGarbageCollector deleted " + num + " references.");
          System.out.println("vtk live objects: " + getNumberOfLiveObjects());
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

  public TreeMap<String, Integer> getLiveObjectMap()
  {
      TreeMap<String, Integer> liveMap = new TreeMap<String, Integer>();
      Set entries = vtkGlobalJavaHash.PointerToReference.entrySet();
      Iterator iter = entries.iterator();
      while (iter.hasNext())
      {
          Map.Entry entry = (Map.Entry)iter.next();
          vtkObjectBase obj = (vtkObjectBase)((WeakReference)entry.getValue()).get();
          if (obj != null)
          {
              if (liveMap.containsKey(obj.GetClassName()))
                  liveMap.put(obj.GetClassName(), 1+liveMap.get(obj.GetClassName()));
              else
                  liveMap.put(obj.GetClassName(), 1);
          }
      }

      return liveMap;
  }

  public void updateLiveObjectHistory()
  {
      int numberStepsSoFar = 1;

      // First go through all existing items and append a zero to each value
      for (String s : liveObjectHistoryMap.keySet())
      {
          ArrayList<Integer> objectHistory = liveObjectHistoryMap.get(s);
          objectHistory.add(0);
          numberStepsSoFar = objectHistory.size();
      }

      // Get the current live object map and update the history map
      TreeMap<String, Integer> liveMap = getLiveObjectMap();
      for (String s : liveMap.keySet())
      {
          if (!liveObjectHistoryMap.containsKey(s))
          {
              ArrayList<Integer> objectHistory = new ArrayList<Integer>();
              for (int i=0;i<numberStepsSoFar; ++i)
                  objectHistory.add(0);
              liveObjectHistoryMap.put(s, objectHistory);
          }
          ArrayList<Integer> objectHistory = liveObjectHistoryMap.get(s);
          objectHistory.set(objectHistory.size()-1, liveMap.get(s));
      }
  }

  public int getNumberOfLiveObjects()
  {
      TreeMap<String, Integer> liveMap = getLiveObjectMap();
      int count = 0;
      for (String s : liveMap.keySet())
          count = count + liveMap.get(s);

      return count;
  }

  public void saveLiveObjectHistory()
  {
    try
    {
        FileWriter fstream = new FileWriter("/tmp/vtkliveobjects.csv");
        BufferedWriter out = new BufferedWriter(fstream);

        int historyLength = 0;
        for (String s : liveObjectHistoryMap.keySet())
        {
            out.write(s + ",");

            ArrayList<Integer> objectHistory = liveObjectHistoryMap.get(s);
            for (Integer num : objectHistory)
                out.write(num + ",");
            out.write("\n");

            historyLength = objectHistory.size();
        }

        // Write out totals for each object on last line
        out.write("Total,");
        for (int i=0; i<historyLength; ++i)
        {
            int total = 0;
            for (String s : liveObjectHistoryMap.keySet())
            {
                ArrayList<Integer> objectHistory = liveObjectHistoryMap.get(s);
                total += objectHistory.get(i);
            }
            out.write(total + ",");
        }
        out.write("\n");

        out.close();
    }
    catch (IOException e)
    {
        e.printStackTrace();
    }
  }
}
