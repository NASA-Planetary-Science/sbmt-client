package edu.jhuapl.near.gui.joglrendering;

import java.awt.Dimension;
import java.util.Map;

import javax.swing.JFrame;

import com.google.common.collect.Maps;

import vtk.vtkActor;
import vtk.vtkCamera;
import vtk.vtkConeSource;
import vtk.vtkInteractorStyle;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkRenderWindowInteractor;

import edu.jhuapl.near.util.NativeLibraryLoader;

public class vtksbmtInteractorStyle extends vtkInteractorStyle
{

    /*  vtkInteractorStyle events
     * vtkCommand::EnterEvent
     * vtkCommand::LeaveEvent
     * vtkCommand::LeftButtonPressEvent
     * vtkCommand::MiddleButtonPressEvent
     * vtkCommand::RightButtonPressEvent
     * vtkCommand::LeftButtonReleaseEvent
     * vtkCommand::MiddleButtonReleaseEvent
     * vtkCommand::RightButtonReleaseEvent
     * vtkCommand::MouseMoveEvent
     * vtkCommand::MouseWheelForwardEvent
     * vtkCommand::MouseWheelBackwardEvent
     * vtkCommand::KeyPressEvent
     * vtkCommand::KeyReleaseEvent
     * vtkCommand::CharEvent
     * vtkCommand::TimerEvent
     * vtkCommand::ConfigureEvent
     * vtkCommand::EnableEvent
     * vtkCommand::DisableEvent
     * vtkCommand::ExposeEvent
     * vtkCommand::StartInteractionEvent
     * vtkCommand::EndInteractionEvent
     */

    public enum StyleEvent
    {
        ENTER("EnterEvent","OnEnter"),
        LEAVE("LeaveEvent","OnLeave"),
        KEYDOWN("KeyDownEvent","OnKeyDown"),
        KEYUP("KeyUpEvent","OnKeyUp"),
        KEYPRESS("KeyPressEvent","OnKeyPress"),
        KEYRELEASE("KeyReleaseEvent","OnKeyRelease"),
        LEFTBUTTONDOWN("LeftButtonPressEvent","OnLeftButtonDown"),
        MIDDLEBUTTONDOWN("MiddleButtonPressEvent","OnMiddleButtonDown"),
        RIGHTBUTTONDOWN("RightButtonPressEvent","OnRightButtonDown"),
        LEFTBUTTONUP("LeftButtonReleaseEvent","OnLeftButtonUp"),
        MIDDLEBUTTONUP("MiddleButtonReleaseEvent","OnMiddleButtonUp"),
        RIGHTBUTTONUP("RightButtonReleaseEvent","OnRightButtonUp"),
        MOUSEMOVE("MouseMoveEvent","OnMouseMove"),
        MOUSEWHEELFORWARD("MouseWheelForwardEvent","OnMouseWheelForward"),
        MOUSEWHEELBACKWARD("MouseWheelBackwardEvent","OnMouseWheelBackward");

        String vtkEventString,localMethodName;

        private StyleEvent(String vtkEventString, String localMethodName)
        {
            this.vtkEventString=vtkEventString;
            this.localMethodName=localMethodName;
        }

        public String getLocalMethodName()
        {
            return localMethodName;
        }

        public String getVtkEventString()
        {
            return vtkEventString;
        }
    }

    public enum ModifierKey
    {
        LCTRL,LALT,LCMD,LSHIFT,
        RCTRL,RALT,RCMD,RSHIFT;
    }

    int storedMouseX=-1;
    int storedMouseY=-1;
    double motionFactor=10.;
    double mouseWheelMotionFactor=1.;

    vtksbmtJoglCanvas canvas;   // unfortunately it is necessary to keep a reference to the canvas itself so that mouse coordinates can be computed appropriately
    vtkRenderWindowInteractor interactor;
    Map<Integer,StyleEvent> eventObserverTags=Maps.newConcurrentMap();  // this keeps track of the (int) id of the observer assigned to listen for each respective event, so one could delete it (by id) later if necessary
    int interactionObserverTag=-1; // this is only used during interaction to watch for mouse moves
    Map<ModifierKey,Boolean> modifiersPressed=Maps.newConcurrentMap();

    public vtksbmtInteractorStyle(vtksbmtJoglCanvas canvas)
    {
        this.canvas=canvas;
        this.interactor=canvas.getRenderWindowInteractor();
        interactor.RemoveAllObservers();
        interactor.SetInteractorStyle(this);
        for (StyleEvent e : StyleEvent.values())
            eventObserverTags.put(interactor.AddObserver(e.getVtkEventString(), this, e.getLocalMethodName()),e);
        interactionObserverTag=interactor.CreateRepeatingTimer(1000/30);   // try to update at about 30 frames per second during interaction
        for (ModifierKey m : ModifierKey.values())
            modifiersPressed.put(m, false);
    }

    protected int[] convertEventCoordsToComponentCoords(int[] eventCoords)
    {
        Dimension componentSize=canvas.getComponent().getSize();
        return new int[]{eventCoords[0]-componentSize.width/2,eventCoords[1]-componentSize.height-componentSize.height/2};
    }

    protected void storeMousePosition()
    {
        int[] coords=convertEventCoordsToComponentCoords(interactor.GetEventPosition());
        storedMouseX=coords[0];
        storedMouseY=coords[1];//componentLocation.y-(screenSize.height-coords[1]);//-screenLocation.y;//(size[1]-screenLocation.y);
        FindPokedRenderer(coords[0],coords[1]); // this sets CurrentRenderer in the C++ VTK code
    }

    protected void discardMousePosition()
    {
        storedMouseX=-1;
        storedMouseY=-1;
    }

    protected void endInteraction()
    {
        discardMousePosition();
        if (interactionObserverTag!=-1)
        {
            interactor.RemoveObserver(interactionObserverTag);
            interactionObserverTag=-1;
        }
    }

    @Override
    public void OnEnter()
    {
        System.out.println("Enter");
    }

    @Override
    public void OnLeave()
    {
        System.out.println("Leave");
    }

    @Override
    public void OnKeyDown()
    {
        System.out.println("KeyDown");
    }

    @Override
    public void OnKeyUp()
    {
        System.out.println("KeyUp");
    }

    @Override
    public void OnKeyPress()
    {
        System.out.println("KeyPress");
    }

    @Override
    public void OnKeyRelease()
    {
        System.out.println("KeyRelease");
    }

    @Override
    public void OnLeftButtonDown()
    {
        System.out.println("LeftButtonDown");
        if (interactor.GetShiftKey()==1)
        {
            if (interactor.GetControlKey()==1)
                StartDolly();
            else
                StartSpin();
        }
        else if (interactor.GetControlKey()==1)
            StartPan();
        else
            StartRotate();
    }

    @Override
    public void OnMiddleButtonDown()
    {
        System.out.println("MiddleButtonDown");
        StartPan();
    }

    @Override
    public void OnRightButtonDown()
    {
        System.out.println("RightButtonDown");
        StartSpin();
    }

    @Override
    public void OnLeftButtonUp()
    {
        System.out.println("LeftButtonUp");
        endInteraction();
    }

    @Override
    public void OnMiddleButtonUp()
    {
        System.out.println("MiddleButtonUp");
        endInteraction();
    }

    @Override
    public void OnRightButtonUp()
    {
        System.out.println("RightButtonUp");
        endInteraction();
    }

    @Override
    public void OnMouseMove()
    {
        System.out.println("MouseMove");
    }

    @Override
    public void OnMouseWheelForward()
    {
        System.out.println("MouseWheelForward");
        storeMousePosition();   // need to call this to commit further actions to the correct renderer (via FindPokedRenderer)
        if (GetCurrentRenderer()==null)
            return;
        double factor=motionFactor*0.2*mouseWheelMotionFactor;
        dolly(Math.pow(1.1,factor));
    }

    @Override
    public void OnMouseWheelBackward()
    {
        System.out.println("MouseWheelBackward");
        storeMousePosition();   // need to call this to commit further actions to the correct renderer (via FindPokedRenderer)
        if (GetCurrentRenderer()==null)
            return;
        double factor=motionFactor*0.2*mouseWheelMotionFactor;
        dolly(Math.pow(1.1,-factor));
    }

    @Override
    public void OnTimer()
    {
        System.out.println("Timer");
    }

    @Override
    public void StartRotate()
    {
        System.out.println("StartRotate");
        storeMousePosition();
        interactionObserverTag=interactor.AddObserver("MouseMoveEvent", this, "Rotate");
    }

    @Override
    public void StartSpin()
    {
        System.out.println("StartSpin");
        storeMousePosition();
        interactionObserverTag=interactor.AddObserver("MouseMoveEvent", this, "Spin");
    }

    @Override
    public void StartDolly()
    {
        System.out.println("StartDolly");
        storeMousePosition();
        interactionObserverTag=interactor.AddObserver("MouseMoveEvent", this, "Dolly");
    }

    @Override
    public void StartPan()
    {
        System.out.println("StartPan");
        storeMousePosition();
        interactionObserverTag=interactor.AddObserver("MouseMoveEvent", this, "Pan");
    }

    @Override
    public void EndRotate()
    {
        System.out.println("EndRotate");
    }

    @Override
    public void EndSpin()
    {
        System.out.println("EndSpin");
    }

    @Override
    public void EndDolly()
    {
        System.out.println("EndDolly");
    }

    @Override
    public void EndPan()
    {
        System.out.println("EndPan");
    }

    @Override
    public void Rotate()
    {
        System.out.println("Rotate");

        if (GetCurrentRenderer()==null)
            return;

        int[] coords=convertEventCoordsToComponentCoords(interactor.GetEventPosition());
        int dx=coords[0]-storedMouseX;
        int dy=coords[1]-storedMouseY;
        storeMousePosition();

        int[] size=GetCurrentRenderer().GetRenderWindow().GetSize();
        double delev=-20./(double)size[1];
        double dazim=-20./(double)size[0];
        double rxf=dx*dazim*motionFactor;
        double ryf=dy*delev*motionFactor;

        vtkCamera cam=GetCurrentRenderer().GetActiveCamera();
        cam.Azimuth(rxf);
        cam.Elevation(ryf);
        cam.OrthogonalizeViewUp();

        if (GetAutoAdjustCameraClippingRange()==1)
            GetCurrentRenderer().ResetCameraClippingRange();

        if (GetCurrentRenderer().GetLightFollowCamera()==1)
            GetCurrentRenderer().UpdateLightsGeometryToFollowCamera();

        GetCurrentRenderer().GetRenderWindow().Render();
    }

    @Override
    public void Spin()
    {
        System.out.println("Spin");

        if (GetCurrentRenderer()==null)
            return;

        int[] coords=convertEventCoordsToComponentCoords(interactor.GetEventPosition());
        double dxNew=coords[0];
        double dyNew=coords[1];
        double dxOld=storedMouseX;
        double dyOld=storedMouseY;
        double newAngle=Math.toDegrees(Math.atan2(dyNew,dxNew));
        double oldAngle=Math.toDegrees(Math.atan2(dyOld,dxOld));
        storeMousePosition();

        vtkCamera cam=GetCurrentRenderer().GetActiveCamera();
        cam.Roll(newAngle-oldAngle);
        cam.OrthogonalizeViewUp();

        GetCurrentRenderer().GetRenderWindow().Render();
    }

    @Override
    public void Pan()
    {
        System.out.println("Pan");

        if (GetCurrentRenderer()==null)
            return;

        double[] viewFocus=new double[4];
        double focalDepth;
        double[] viewPoint=new double[3];
        double[] newPickPoint=new double[4];
        double[] oldPickPoint=new double[4];
        double[] motionVector=new double[3];

        vtkCamera cam=GetCurrentRenderer().GetActiveCamera();
        viewFocus=cam.GetFocalPoint();
        ComputeWorldToDisplay(GetCurrentRenderer(), viewFocus[0], viewFocus[1], viewFocus[2], viewFocus);
        focalDepth=viewFocus[2];
        ComputeDisplayToWorld(GetCurrentRenderer(), storedMouseX, storedMouseY, focalDepth, newPickPoint);

        int[] coords=convertEventCoordsToComponentCoords(interactor.GetEventPosition());
        double xNew=coords[0];
        double yNew=coords[1];
        ComputeDisplayToWorld(GetCurrentRenderer(), xNew, yNew, focalDepth, oldPickPoint);
        storeMousePosition();

        motionVector[0]=-(oldPickPoint[0]-newPickPoint[0]);
        motionVector[1]=-(oldPickPoint[1]-newPickPoint[1]);
        motionVector[2]=-(oldPickPoint[2]-newPickPoint[2]);

        viewFocus=cam.GetFocalPoint();
        viewPoint=cam.GetPosition();
        cam.SetFocalPoint(motionVector[0]+viewFocus[0],motionVector[1]+viewFocus[1],motionVector[2]+viewFocus[2]);
        cam.SetPosition(motionVector[0]+viewPoint[0],motionVector[1]+viewPoint[1],motionVector[2]+viewPoint[2]);

        if (interactor.GetLightFollowCamera()==1)
            GetCurrentRenderer().UpdateLightsGeometryToFollowCamera();

        GetCurrentRenderer().GetRenderWindow().Render();
    }

    @Override
    public void Dolly()
    {
        System.out.println("Dolly");

        if (GetCurrentRenderer()==null)
            return;

        int[] coords=convertEventCoordsToComponentCoords(interactor.GetEventPosition());
        double factor=Math.pow(1.1,(double)(coords[1]-storedMouseY)/GetCurrentRenderer().GetSize()[1]*motionFactor);
        storeMousePosition();

        dolly(factor);
    }

    protected void dolly(double factor)
    {
        vtkCamera cam=GetCurrentRenderer().GetActiveCamera();
        if (cam.GetParallelProjection()==1)
            cam.SetParallelScale(cam.GetParallelScale()/factor);
        else
        {
            cam.Dolly(factor);
            if (GetAutoAdjustCameraClippingRange()==1)
                GetCurrentRenderer().ResetCameraClippingRange();
            if (interactor.GetLightFollowCamera()==1)
                GetCurrentRenderer().UpdateLightsGeometryToFollowCamera();
        }

        GetCurrentRenderer().GetRenderWindow().Render();
    }

    public static void main(String[] args)
    {
        NativeLibraryLoader.loadVtkLibraries();
        vtksbmtJoglCanvas canvas=new vtksbmtJoglCanvas();
        canvas.setInteractorStyle(new vtksbmtInteractorStyle(canvas));

        JFrame frame=new JFrame();
        frame.setSize(600, 600);
        canvas.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(canvas.getComponent());
        frame.setVisible(true);

        vtkConeSource source=new vtkConeSource();
        source.Update();
        vtkPolyData polyData=source.GetOutput();
        vtkPolyDataMapper mapper=new vtkPolyDataMapper();
        mapper.SetInputData(polyData);
        vtkActor actor=new vtkActor();
        actor.SetMapper(mapper);
        canvas.getRenderer().AddActor(actor);
    }

}
