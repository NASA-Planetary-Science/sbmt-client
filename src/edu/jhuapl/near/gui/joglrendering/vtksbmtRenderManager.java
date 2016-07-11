package edu.jhuapl.near.gui.joglrendering;

import java.util.List;

import javax.swing.JFrame;

import com.google.common.collect.Lists;

import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;

import edu.jhuapl.near.util.NativeLibraryLoader;

public class vtksbmtRenderManager
{

    List<vtksbmtCanvas> canvasList=Lists.newArrayList();

    public void register(vtksbmtCanvas canvas)
    {
        canvasList.add(canvas);
    }

    public void addProp(vtkProp prop)
    {
        for (vtksbmtCanvas canvas : canvasList)
            canvas.addProp(prop);
    }

    public static void main(String[] args)
    {
        NativeLibraryLoader.loadVtkLibraries();
        vtksbmtRenderManager renderManager=new vtksbmtRenderManager();

        int w=600;
        int h=600;
        vtksbmtCanvas canvas1=new vtksbmtBasicCanvas();
        vtksbmtCanvas canvas2=new vtksbmtStereoSBSCanvas(false);
        canvas1.getSwingComponent().setSize(w,h);
        canvas2.getSwingComponent().setSize(w,h);
        renderManager.register(canvas1);
        renderManager.register(canvas2);

        vtkConeSource source=new vtkConeSource();
        source.Update();
        vtkPolyData polyData=source.GetOutput();
        vtkPolyDataMapper mapper=new vtkPolyDataMapper();
        mapper.SetInputData(polyData);
        vtkActor actor=new vtkActor();
        actor.SetMapper(mapper);
        renderManager.addProp(actor);

        JFrame frame1=new JFrame();
        frame1.setSize(w, h);
        frame1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame1.getContentPane().add(canvas1.getSwingComponent());
        frame1.setVisible(true);

        JFrame frame2=new JFrame();
        frame2.setSize(w, h);
        frame2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame2.getContentPane().add(canvas2.getSwingComponent());
        frame2.setVisible(true);

/*        JFrame frame=new JFrame();
        JPanel panel=new JPanel(new GridLayout(1, 2));
        frame.setSize(2*w, h);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.add(canvas1.getComponent());
        panel.add(canvas2.getComponent());
        frame.getContentPane().add(panel);
        frame.setVisible(true);*/
    }

}
