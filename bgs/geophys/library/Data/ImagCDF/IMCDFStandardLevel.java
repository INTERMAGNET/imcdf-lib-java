/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import gsfc.nssdc.cdf.CDFException;

/**
 * An object that represents the standard level of the data
 * 
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class IMCDFStandardLevel 
implements IMCDFPrintEnum
{

    /** code for the standard level:
     *      NONE - does not conform to a standard;
     *      PARTIAL - partially conforms to the relevant standard - details on the level of
     *                conformance are in partial_stand_desc
     *      FULL - fully conforms to the relevant standard */
    public enum StandardLevel {NONE, PARTIAL, FULL};
    

    // the standard represented by this object
    private StandardLevel stand_level;
    
    /** create a standards performance code from the enumeration of the codes
     * @param code - one of the enumeration values for the code */
    public IMCDFStandardLevel (StandardLevel stand_level)
    {
        this.stand_level = stand_level;
    }
    
    /** create a publication state code from a string
     * @param stand_level_string one of "none", "partial" or "full"
     * @throw CDFException if the string could not be recognised */
    public IMCDFStandardLevel (String stand_level_string)
    throws CDFException
    {
        if (stand_level_string.equalsIgnoreCase("none"))
            stand_level = StandardLevel.NONE;
        else if (stand_level_string.equalsIgnoreCase("partial"))
            stand_level = StandardLevel.PARTIAL;
        else if (stand_level_string.equalsIgnoreCase("full"))
            stand_level = StandardLevel.FULL;
        else
            throw new CDFException("Invalid standard level code: " + stand_level_string);
    }
    
    public StandardLevel getStandardLevel () { return stand_level; }
    
    /** get a string representation of the code
     * @return one of "Raw", "Adjusted" or "Definitive" */
    @Override
    public String toString ()
    {
        switch (stand_level)
        {
            case NONE: return "None";
            case PARTIAL: return "Partial";
            case FULL: return "Full";
        }
        return "Unknown";
    }    
    
}
