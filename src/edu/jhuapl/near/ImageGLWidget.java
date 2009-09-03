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
    private int object = 0;
    private int xRot = 0;
    private int yRot = 0;
    private int zRot = 0;
    private QPoint lastPos = new QPoint();
    private GL gl = null;
    private GLContext ctx = null;
    private GLU glu = new GLU();
    
    private IntBuffer textureNames;
    private int totalNumTextures = 0;
    private int numTexturesWidth = 0;
    private int numTexturesHeight = 0;
    
    private static final int TEXTURE_SIZE = 64;

    private NearImage nearImage = null;
    
    public Signal1<Integer> xRotationChanged = new Signal1<Integer>();
    public Signal1<Integer> yRotationChanged = new Signal1<Integer>();
    public Signal1<Integer> zRotationChanged = new Signal1<Integer>();

    ArrayList<TextureInfo> textureInfo = new ArrayList<TextureInfo>();

    private static class TextureInfo
    {
    	int x;
    	int y;
    }
    
    
    public ImageGLWidget(QWidget parent, String filename) throws FitsException, IOException 
    {
        super(parent);
        
        nearImage = new NearImage(filename);
        
//        QLabel imageLabel = new QLabel();
//        imageLabel.setBackgroundRole(QPalette.ColorRole.Base);
//        imageLabel.setSizePolicy(QSizePolicy.Policy.Ignored, QSizePolicy.Policy.Ignored);
//        imageLabel.setScaledContents(true);
//        //imageLabel.setPixmap(QPixmap.fromImage(image));
//
//        QScrollArea scrollArea = new QScrollArea(this);
//        scrollArea.setBackgroundRole(QPalette.ColorRole.Dark);
//        scrollArea.setWidget(imageLabel);
        
        
    }

    @Override
    protected void initializeGL()
    {
        GLDrawableFactory factory = GLDrawableFactory.getFactory();
        ctx = factory.createExternalGLContext();
        gl = ctx.getGL();
        
        
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glShadeModel(GL.GL_FLAT);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL.GL_CULL_FACE);

        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);

        numTexturesWidth = (int)Math.ceil((double)(NearImage.IMAGE_WIDTH-1) / (double)(TEXTURE_SIZE-1));
        numTexturesHeight = (int)Math.ceil((double)(NearImage.IMAGE_HEIGHT-1) / (double)(TEXTURE_SIZE-1));
        totalNumTextures = numTexturesWidth * numTexturesHeight;
        
        textureInfo.clear();
        
        textureNames = IntBuffer.allocate(totalNumTextures);
        gl.glGenTextures(totalNumTextures, textureNames);
        
        int c = 0;
        for (int i=0; i<numTexturesHeight; ++i)
        	for (int j=0; j<numTexturesWidth; ++j)
        	{
        		System.out.println(c);
                gl.glBindTexture(GL.GL_TEXTURE_2D, textureNames.get(c));

                int xcorner = i*(TEXTURE_SIZE-1);
                int ycorner = j*(TEXTURE_SIZE-1);
        
                TextureInfo ti = new TextureInfo();
                ti.x = xcorner;
                ti.y = ycorner;
                textureInfo.add(ti);
                
                ByteBuffer buffer = nearImage.getSubImage(TEXTURE_SIZE, xcorner, ycorner);
                byte [] b = new byte[TEXTURE_SIZE*TEXTURE_SIZE*4];
        		factory = GLDrawableFactory.getFactory();
                ctx = factory.createExternalGLContext();
                gl = ctx.getGL();
                gl.glTexImage2D(
                		GL.GL_TEXTURE_2D, 
                		0, 
                		//GL.GL_LUMINANCE, 
                		GL.GL_RGBA, 
                		TEXTURE_SIZE, 
                		TEXTURE_SIZE, 
                		0, 
                		//GL.GL_LUMINANCE, 
                		GL.GL_RGBA, 
                		GL.GL_UNSIGNED_BYTE, 
                		0);
                
                ++c;
        	}
    }

    @Override
    protected void paintGL()
    {
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);

        int c = 0;
        for (int i=0; i<numTexturesHeight; ++i)
        	for (int j=0; j<numTexturesWidth; ++j)
        	{
                gl.glBindTexture(GL.GL_TEXTURE_2D, textureNames.get(c));

                int xcorner = i*(TEXTURE_SIZE-1);
                int ycorner = j*(TEXTURE_SIZE-1);
        
                
                for (int m=0; m<TEXTURE_SIZE-1; ++m)
                {
                	gl.glBegin(GL.GL_TRIANGLE_STRIP);

                	double x = nearImage.getX(xcorner, ycorner+m);
    				double y = nearImage.getY(xcorner, ycorner+m);
    				double z = nearImage.getZ(xcorner, ycorner+m);
    				
                	gl.glTexCoord2d((double)m/(double)(TEXTURE_SIZE-1), 0.0);
                	gl.glVertex3d(x, y, z);
                	
                	x = nearImage.getX(xcorner, ycorner+m+1);
    				y = nearImage.getY(xcorner, ycorner+m+1);
    				z = nearImage.getZ(xcorner, ycorner+m+1);
    				
                	gl.glTexCoord2d(0.0, 0.0);
                	gl.glVertex3d(x, y, z);
                	
        			for (int n=0; n<TEXTURE_SIZE-1; ++n)
        			{
        				x = nearImage.getX(xcorner+m, ycorner+n);
        				y = nearImage.getY(xcorner+m, ycorner+n);
        				z = nearImage.getZ(xcorner+m, ycorner+n);
        				
                    	gl.glTexCoord2d(0.0, 0.0);
                    	gl.glVertex3d(x, y, z);
                    	
        				x = nearImage.getX(xcorner+m, ycorner+n);
        				y = nearImage.getY(xcorner+m, ycorner+n);
        				z = nearImage.getZ(xcorner+m, ycorner+n);
        				
                    	gl.glTexCoord2d(0.0, 0.0);
                    	gl.glVertex3d(x, y, z);
        			}

        			gl.glEnd();
                }
                ++c;
        	}
        
        gl.glFlush();
        gl.glDisable(GL.GL_TEXTURE_2D);
    	/*
        func.glLoadIdentity();
        func.glTranslated(0.0, 0.0, -10.0);
        func.glRotated(xRot / 16.0, 1.0, 0.0, 0.0);
        func.glRotated(yRot / 16.0, 0.0, 1.0, 0.0);
        func.glRotated(zRot / 16.0, 0.0, 0.0, 1.0);
        func.glCallList(object);
        */
    }

    @Override
    protected void resizeGL(int width, int height)
    {
    	gl.glViewport(0, 0, width, height);
    	gl.glMatrixMode(GL.GL_PROJECTION);
    	gl.glLoadIdentity();
    	//glu.gluPerspective(60.0, (double)width/(double)height, 1.0, 30.0);
    	gl.glMatrixMode(GL.GL_MODELVIEW);
    	gl.glLoadIdentity();
    	gl.glTranslated(0.0, 0.0, -100.0);
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

        if (event.buttons().isSet(Qt.MouseButton.LeftButton)) {
            setXRotation(xRot + 8 * dy);
            setYRotation(yRot + 8 * dx);
        } else if (event.buttons().isSet(Qt.MouseButton.RightButton)) {
            setXRotation(xRot + 8 * dy);
            setZRotation(zRot + 8 * dx);
        }
        lastPos = event.pos();
    }


    private void normalizeAngle(int angle[])
    {
        while (angle[0] < 0)
            angle[0] += 360 * 16;
        while (angle[0] > 360 * 16)
            angle[0] -= 360 * 16;
    }
}

