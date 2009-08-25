package edu.jhuapl.near;

import com.trolltech.qt.gui.*;


public class ErosLineamentViewer extends QWidget
{
    public static boolean checkJoglSupport() {
        try {
            Class.forName("javax.media.opengl.GL");
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static void main(String args[])
    {
        QApplication.initialize(args);

        if (!checkJoglSupport()) {
            QMessageBox.critical(null, "OpenGL Missing", "This Example requires OpenGL for Java\nAvalable at: <i>https://jogl.dev.java.net/</i>");
            return;
        }

        MainWindow window = new MainWindow();
        window.show();
        QApplication.exec();
        //window.dispose();
    }
}

