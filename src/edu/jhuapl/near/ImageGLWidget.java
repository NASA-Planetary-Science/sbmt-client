package edu.jhuapl.near;

import java.io.*;
import java.nio.*;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;

import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.opengl.*;
import nom.tam.fits.*;

class ImageGLWidget extends QGLWidget
{
    private int object = 0;
    private int xRot = 0;
    private int yRot = 0;
    private int zRot = 0;
    private QPoint lastPos = new QPoint();
    private GL func = null;
    private GLContext ctx = null;

    private IntBuffer textureName = IntBuffer.allocate(63);
    private static final int TEXTURE_SIZE = 64;
    
    public Signal1<Integer> xRotationChanged = new Signal1<Integer>();
    public Signal1<Integer> yRotationChanged = new Signal1<Integer>();
    public Signal1<Integer> zRotationChanged = new Signal1<Integer>();

    public ImageGLWidget(QWidget parent, String filename) throws FitsException, IOException 
    {
        super(parent);
        
        new NearImage(filename);
        
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
    protected void disposed()
    {
        func.glDeleteLists(object, 1);
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
    protected void initializeGL()
    {
        GLDrawableFactory factory = GLDrawableFactory.getFactory();
        ctx = factory.createExternalGLContext();
        func = ctx.getGL();

        func.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        func.glShadeModel(GL.GL_FLAT);
        func.glEnable(GL.GL_DEPTH_TEST);
        func.glEnable(GL.GL_CULL_FACE);

        func.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);

        func.glGenTextures(63, textureName);
        func.glBindTexture(GL.GL_TEXTURE_2D, textureName.get(0));
        
        //func.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_LUMINANCE, arg3, 0, arg5, arg6, arg7, arg8)
    }

    @Override
    protected void paintGL()
    {
        func.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        func.glEnable(GL.GL_TEXTURE_2D);
        func.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
        for (int i=0; i<TEXTURE_SIZE; ++i)
        	for (int j=0; j<TEXTURE_SIZE; ++j)
        	{
        		
        	}
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
    	/*
        int side = Math.min(width, height);
        func.glViewport((width - side) / 2, (height - side) / 2, side, side);

        func.glMatrixMode(GL.GL_PROJECTION);
        func.glLoadIdentity();
        func.glOrtho(-0.5, +0.5, +0.5, -0.5, 4.0, 15.0);
        func.glMatrixMode(GL.GL_MODELVIEW);
        */
    }

    @Override
    protected void mousePressEvent(QMouseEvent event)
    {
        lastPos = event.pos();
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

