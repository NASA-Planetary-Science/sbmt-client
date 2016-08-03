package edu.jhuapl.near.gui.multitexturing;

import org.apache.commons.math3.geometry.Space;

public interface Chart<X extends Space, Y extends Space>
{
    Y getImage(X x);
    X getInverse(Y y);
}
