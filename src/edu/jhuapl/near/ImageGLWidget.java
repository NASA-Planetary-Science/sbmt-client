package edu.jhuapl.near;

import java.io.*;
import java.nio.*;
import java.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.glu.*;

import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.opengl.*;
import nom.tam.fits.*;
import java.nio.ByteBuffer;

class ImageGLWidget extends QGLWidget
{
    private int xRot = 0;
    private int yRot = 0;
    private int zRot = 0;
    private QPoint lastPos = new QPoint();
    private GL gl = null;
    private GLContext ctx = null;
    private GLU glu = null;
    
    private IntBuffer textureNames;
    private int totalNumTextures = 0;
    private int numTexturesWidth = 0;
    private int numTexturesHeight = 0;
    
    private static final int TEXTURE_SIZE = 64;

    private NearImage nearImage = null;
    private LineamentModel lineamentModel;
    
    public Signal1<Integer> xRotationChanged = new Signal1<Integer>();
    public Signal1<Integer> yRotationChanged = new Signal1<Integer>();
    public Signal1<Integer> zRotationChanged = new Signal1<Integer>();

    private double eyex;
    private double eyey;
    private double eyez;
    private double centerx;
    private double centery;
    private double centerz;
    private double upx;
    private double upy;
    private double upz;

    
    public ImageGLWidget(String filename, LineamentModel model, QWidget parent) throws FitsException, IOException 
    {
        super(parent);

        lineamentModel = model;
        
        nearImage = new NearImage(filename);
    }

    @Override
    protected void initializeGL()
    {
        GLDrawableFactory factory = GLDrawableFactory.getFactory();
        ctx = factory.createExternalGLContext();
        gl = ctx.getGL();
        ctx.makeCurrent();
    	glu = new GLU();
        
        
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glShadeModel(GL.GL_FLAT);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        gl.glLineWidth(4.0f);
        
        numTexturesWidth = (int)Math.ceil((double)(NearImage.IMAGE_WIDTH-1) / (double)(TEXTURE_SIZE-1));
        numTexturesHeight = (int)Math.ceil((double)(NearImage.IMAGE_HEIGHT-1) / (double)(TEXTURE_SIZE-1));
        totalNumTextures = numTexturesWidth * numTexturesHeight;
     
        textureNames = IntBuffer.allocate(totalNumTextures);
        gl.glGenTextures(totalNumTextures, textureNames);
        
        int c = 0;
        for (int i=0; i<numTexturesHeight; ++i)
        	for (int j=0; j<numTexturesWidth; ++j)
        	{
        		gl.glBindTexture(GL.GL_TEXTURE_2D, textureNames.get(c));

        		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
        		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
        		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);

                int corner1 = i*(TEXTURE_SIZE-1);
                int corner2 = j*(TEXTURE_SIZE-1);
        
                ByteBuffer buffer = nearImage.getSubImage(TEXTURE_SIZE, corner1, corner2);
                gl.glTexImage2D(
                		GL.GL_TEXTURE_2D, 
                		0, 
                		GL.GL_RGBA, 
                		TEXTURE_SIZE, 
                		TEXTURE_SIZE, 
                		0, 
                		GL.GL_RGBA, 
                		GL.GL_UNSIGNED_BYTE, 
                		buffer);
                
                ++c;
        	}
        
    }

    @Override
    protected void paintGL()
    {
    	gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    	//gl.glEnable(GL.GL_TEXTURE_2D);
    	gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
    	
    	gl.glLoadIdentity();
    	gl.glTranslated(0.0, 0.0, -5.0);
    	gl.glRotated(xRot / 16.0, 1.0, 0.0, 0.0);
        gl.glRotated(yRot / 16.0, 0.0, 1.0, 0.0);
        gl.glRotated(zRot / 16.0, 0.0, 0.0, 1.0);

    	Point center = nearImage.getCenter();
    	//System.out.println("Center:  " + center.x + " " + center.y + " " + center.z);
    	//glu.gluLookAt(0, 0, -40, center.x, center.y, center.z, 0, 1, 0);
    	//glu.gluLookAt(250, 200, 500, 250, 200, 0, 0, 1, 0);
    	//glu.gluLookAt(eyex, eyey, eyez, centerx, centery, centerz, upx, upy, upz);
    	//glu.gluLookAt(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
    	//gl.glTranslated(-(double)width/2.0, -(double)height/2.0, -392.48);
    	//gl.glTranslated(0.0, 0.0, -392);

        double x, y, z;
        
        int c = 0;
        for (int i=0; i<numTexturesHeight; ++i)
        	for (int j=0; j<numTexturesWidth; ++j)
        	{
        		//gl.glBindTexture(GL.GL_TEXTURE_2D, textureNames.get(c));

                int corner1 = i*(TEXTURE_SIZE-1);
                int corner2 = j*(TEXTURE_SIZE-1);
        
                int maxM = TEXTURE_SIZE-1;
                if (i==numTexturesHeight-1)
                	maxM = (NearImage.IMAGE_HEIGHT-1) - (numTexturesHeight-1)*(TEXTURE_SIZE-1);
                for (int m=0; m<maxM; ++m)
                {
                	gl.glColor3d(1.0, 0.0, 0.0);

                	gl.glBegin(GL.GL_TRIANGLE_STRIP);

                	x = nearImage.getX(corner1+m, corner2);
    				y = nearImage.getY(corner1+m, corner2);
    				z = nearImage.getZ(corner1+m, corner2);

    				//gl.glTexCoord2d(0.0, (double)(m)/(double)(TEXTURE_SIZE-1));
                	gl.glVertex3d(x, y, z);
                	//System.out.println("1:  " + x + " " + y + " " + z);
    				//System.out.println( 0.0 +" , "+ (double)(m)/(double)(TEXTURE_SIZE-1));
    				
                	x = nearImage.getX(corner1+m+1, corner2);
    				y = nearImage.getY(corner1+m+1, corner2);
    				z = nearImage.getZ(corner1+m+1, corner2);
    				
    				//gl.glTexCoord2d(0.0, (double)(m+1)/(double)(TEXTURE_SIZE-1));
                	gl.glVertex3d(x, y, z);
                	//System.out.println("2:  " + x + " " + y + " " + z);
    				//System.out.println(0.0 + " , " + (double)(m+1)/(double)(TEXTURE_SIZE-1)); 
    						
                    int maxN = TEXTURE_SIZE-1;
                    if (j==numTexturesWidth-1)
                    	maxN = (NearImage.IMAGE_WIDTH-1) - (numTexturesWidth-1)*(TEXTURE_SIZE-1);
        			for (int n=0; n<maxN; ++n)
        			{
        				x = nearImage.getX(corner1+m, corner2+n+1);
        				y = nearImage.getY(corner1+m, corner2+n+1);
        				z = nearImage.getZ(corner1+m, corner2+n+1);
        				
        				//gl.glTexCoord2d((double)(n+1)/(double)(TEXTURE_SIZE-1),
                    	//				(double)(m)/(double)(TEXTURE_SIZE-1));
                    	gl.glVertex3d(x, y, z);
                    	//System.out.println("3:  " + x + " " + y + " " + z);
        				//System.out.println((double)(n+1)/(double)(TEXTURE_SIZE-1) + " , " + 
        				//		(double)(m)/(double)(TEXTURE_SIZE-1));
        				
        				x = nearImage.getX(corner1+m+1, corner2+n+1);
        				y = nearImage.getY(corner1+m+1, corner2+n+1);
        				z = nearImage.getZ(corner1+m+1, corner2+n+1);
        				
                    	//gl.glTexCoord2d((double)(n+1)/(double)(TEXTURE_SIZE-1), 
                    	//				(double)(m+1)/(double)(TEXTURE_SIZE-1));
                    	gl.glVertex3d(x, y, z);
                    	//System.out.println("4:  " + x + " " + y + " " + z);
        				//System.out.println((double)(n+1)/(double)(TEXTURE_SIZE-1) + " , " +
        				//		(double)(m+1)/(double)(TEXTURE_SIZE-1));
        			}

        			gl.glEnd();
                }
                ++c;
        	}
        
        gl.glDisable(GL.GL_TEXTURE_2D);

        drawLineaments();
        
        gl.glFlush();
    }

    @Override
    protected void resizeGL(int width, int height)
    {
    	gl.glViewport(0, 0, width, height);
    	gl.glMatrixMode(GL.GL_PROJECTION);
    	gl.glLoadIdentity();
    	//glu.gluPerspective(60.0, (double)width/(double)height, 10.1, 100000.0);
    	//gl.glOrtho(-100.0, 600.0, -100.0, 500.0, -1.0, 1.0);
    	gl.glOrtho(0.0, 8.0, -7.0, -3.0, 1.0, 100.0);
    	gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    private void drawLineaments()
    {
    	BoundingBox bb = nearImage.getBoundingBox();
    	List<LineamentModel.Lineament> list = lineamentModel.getLineamentsWithinBox(bb);
    	for (LineamentModel.Lineament lin : list)
    	{
    		//System.out.println(lin.name + " " + nearImage.getName());
    		if (lin.name.equals(nearImage.getName()))
    		{
    			gl.glColor3f(1.0f, 0f, 0f);
//    		else
//    			gl.glColor3f(0f, 1.0f, 0f);
    		
    		gl.glBegin(GL.GL_LINE_STRIP);
    		
    		int size = lin.x.size();
    		for (int i=0;i<size;++i)
    		{
    			gl.glVertex3d(lin.x.get(i), lin.y.get(i), lin.z.get(i));
    		}
    		System.out.println("x  " +lin.x);
    		System.out.println("y  " +lin.y);
    		System.out.println("z  " +lin.z);
    		gl.glEnd();
    		}
    	}
    }
    
    @Override
    protected void mousePressEvent(QMouseEvent event)
    {
        lastPos = event.pos();
    }

    @Override
    protected void disposed()
    {
        //gl.glDeleteLists(object, 1);
    }

    @Override
    public QSize minimumSizeHint()
    {
        return new QSize(50, 50);
    }

    @Override
    public QSize sizeHint()
    {
        return new QSize(400, 400);
    }

    private void setXRotation(int _angle)
    {
        int angle[] = { _angle };
        normalizeAngle(angle);

        if (angle[0] != xRot) {
            xRot = angle[0];
            xRotationChanged.emit(xRot);
            updateGL();
        }
    }

    private void setYRotation(int _angle)
    {
        int angle[] = { _angle };
        normalizeAngle(angle);

        if (angle[0] != yRot) {
            yRot = angle[0];
            yRotationChanged.emit(yRot);
            updateGL();
        }
    }

    private void setZRotation(int _angle)
    {
        int angle[] = { _angle };
        normalizeAngle(angle);

        if (angle[0] != zRot) {
            zRot = angle[0];
            zRotationChanged.emit(zRot);
            updateGL();
        }
    }

    @Override
    protected void mouseMoveEvent(QMouseEvent event)
    {
        int dx = event.x() - lastPos.x();
        int dy = event.y() - lastPos.y();

        if (event.buttons().isSet(Qt.MouseButton.LeftButton)) 
        {
        	setXRotation(xRot + 8 * dy);
            setYRotation(yRot + 8 * dx);
        } 
//        else if (event.buttons().isSet(Qt.MouseButton.MidButton)) 
//        {
//            //setXRotation(xRot + 8 * dy);
//            //setZRotation(zRot + 8 * dx);
//        } 
        else if (event.buttons().isSet(Qt.MouseButton.RightButton)) 
        {
        	// Do panning: Move the eye and center points 
        	
            setXRotation(xRot + 8 * dy);
            setZRotation(zRot + 8 * dx);
        }
        lastPos = event.pos();
        updateGL();
    }


    private void normalizeAngle(int angle[])
    {
        while (angle[0] < 0)
            angle[0] += 360 * 16;
        while (angle[0] > 360 * 16)
            angle[0] -= 360 * 16;
    }
}

