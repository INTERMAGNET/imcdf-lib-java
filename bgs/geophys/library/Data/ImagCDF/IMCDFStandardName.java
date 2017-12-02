/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import gsfc.nssdc.cdf.CDFException;

/**
 * An object that represents the standard name of the data
 * 
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class IMCDFStandardName 
implements IMCDFPrintEnum
{

    /** code for the standard name:
     *      INTERMAGNET_1_SECOND - conforms to the INTERMAGNET 1-second standard
     *      INTERMAGNET_1_MINUTE - conforms to the INTERMAGNET minute mean standard
     *      INTERMAGNET_1_MINUTE_QD - conforms to the INTERMAGNET minute mean standard, modified for quasi-definitive data
     *      INTERMAGNET_1_SECOND_QD - conforms to the INTERMAGNET 1-second standard, modified for quasi-definitive data */
    public enum StandardName {INTERMAGNET_1_SECOND, INTERMAGNET_1_MINUTE, 
                              INTERMAGNET_1_MINUTE_QD, INTERMAGNET_1_SECOND_QD};
    

    // the standard represented by this object
    private StandardName stand_name;
    
    /** create a standards performance code from the enumeration of the codes
     * @param code - one of the enumeration values for the code */
    public IMCDFStandardName (StandardName stand_name)
    {
        this.stand_name = stand_name;
    }
    
    /** create a publication state code from a string
     * @param stand_level_string one of "none", "partial" or "full"
     * @throws CDFException if the string could not be recognised */
    public IMCDFStandardName (String stand_name_string)
    throws CDFException
    {
        if (stand_name_string.equalsIgnoreCase("INTERMAGNET_1-Second"))
            stand_name = StandardName.INTERMAGNET_1_SECOND;
        else if (stand_name_string.equalsIgnoreCase("INTERMAGNET_1-Minute"))
            stand_name = StandardName.INTERMAGNET_1_MINUTE;
        else if (stand_name_string.equalsIgnoreCase("INTERMAGNET_1-Minute_QD"))
            stand_name = StandardName.INTERMAGNET_1_MINUTE_QD;
        else if (stand_name_string.equalsIgnoreCase("INTERMAGNET_1-Second_QD"))
            stand_name = StandardName.INTERMAGNET_1_SECOND_QD;
        else
            throw new CDFException("Invalid standard level code: " + stand_name_string);
    }
    
    public StandardName getStandardName () { return stand_name; }
    
    /** get a string representation of the code
     * @return one of "Raw", "Adjusted" or "Definitive" */
    @Override
    public String toString ()
    {
        switch (stand_name)
        {
            case INTERMAGNET_1_SECOND: return "INTERMAGNET_1-Second";
            case INTERMAGNET_1_MINUTE: return "INTERMAGNET_1-Minute";
            case INTERMAGNET_1_MINUTE_QD: return "INTERMAGNET_1-Minute_QD";
            case INTERMAGNET_1_SECOND_QD: return "INTERMAGNET_1-Second_QD";
        }
        return "Unknown";
    }    
    
}
