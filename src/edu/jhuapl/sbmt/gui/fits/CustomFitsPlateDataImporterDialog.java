/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShapeModelImporterDialog.java
 *
 * Created on Jul 21, 2011, 9:00:24 PM
 */
package edu.jhuapl.sbmt.gui.fits;

import edu.jhuapl.saavtk.gui.dialog.CustomPlateDataImporterDialog;
import edu.jhuapl.saavtk.model.PolyhedralModel;

import nom.tam.fits.AsciiTableHDU;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;


public class CustomFitsPlateDataImporterDialog extends CustomPlateDataImporterDialog
{
    private boolean okayPressed = false;
    private int numCells = 0;
    private boolean isEditMode;
    private static final String LEAVE_UNMODIFIED = "<leave unmodified or empty to use existing plate data>";
    private String origColoringFile; // used in Edit mode only to store original filename

    /** Creates new form ShapeModelImporterDialog */
    public CustomFitsPlateDataImporterDialog(java.awt.Window parent, boolean isEditMode)
    {
        super(parent, isEditMode);
    }


    protected String validateFitsFile(String filename)
    {
//        String result = "Ancillary FITS file reading not implemented yet";
        String result = null;

        try {
            Fits fits = new Fits(filename);
            BasicHDU[] hdus = fits.read();
            int nhdus = fits.getNumberOfHDUs();
            if (nhdus != 2)
                return "FITS Ancillary File has improper number of HDUs";

                BasicHDU hdu = fits.getHDU(1);
                 if (hdu instanceof AsciiTableHDU)
                {
                    AsciiTableHDU athdu = (AsciiTableHDU)hdu;
                    int ncols = athdu.getNCols();
                    if (ncols <= PolyhedralModel.FITS_SCALAR_COLUMN_INDEX)
                        return "FITS Ancillary File Has Insufficient Columns";

//                    for (int k=0; k<ncols; k++)
//                        System.out.print(athdu.getColumnName(k) + ", ");
//
//                    String scalarHeader = athdu.getColumnName(SmallBodyModel.FITS_SCALAR_COLUMN_INDEX);
//                    float[] scalars = (float[])athdu.getColumn(FITS_SCALAR_COLUMN_INDEX);
//                    for (int j=0; j<10; j++)
//                    {
//                        System.out.println("Value " + j + ": " + scalars[j]);
//                    }
                }
                else if (hdu instanceof BinaryTableHDU)
                {
                    BinaryTableHDU bthdu = (BinaryTableHDU)hdu;
                    int ncols = bthdu.getNCols();
                    if (ncols <= PolyhedralModel.FITS_SCALAR_COLUMN_INDEX)
                        return "FITS Ancillary File Has Insufficient Columns";
                }
                else
                    return "FITS Ancillary File doesn't have an Ascii Table HDU";

        } catch (Exception e) { return "Error Parsing FITS Ancillary File"; }

        return result;
    }
}

