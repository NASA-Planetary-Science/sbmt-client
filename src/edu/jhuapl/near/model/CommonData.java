package edu.jhuapl.near.model;

import edu.jhuapl.near.util.Preferences;

public class CommonData
{
        private int[] selectionColor;

        public CommonData()
        {
            selectionColor = Preferences.getInstance().getAsIntArray(Preferences.SELECTION_COLOR, new int[]{0, 0, 255});
        }

        public int[] getSelectionColor()
        {
            return selectionColor;
        }

        public void setSelectionColor(int[] selectionColor)
        {
            this.selectionColor = selectionColor.clone();
        }
}
