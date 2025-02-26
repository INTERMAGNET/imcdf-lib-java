/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import java.text.ParseException;

/**
 * A class to enumerate the types of data an ImagCDFVariable can hold
 * 
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class IMCDFVariableType 
{
    /** the default name of the CDF variable used for time stamps where the same
     * time stamp variable is used for both vector and scalar data (ie both have the same
     * sample period) */
    public static final String DEFAULT_TIME_STAMPS_VAR_NAME = "DataTimes";
    /** the default name of the CDF variable used for time stamps for vector data, where
     * this differs from the time stamps variable used for scalar data */
    public static final String DEFAULT_VECTOR_TIME_STAMPS_VAR_NAME = "GeomagneticVectorTimes";
    /** the default name of the CDF variable used for time stamps for scalar data, where
     * this differs from the time stamps variable used for vector data */
    public static final String DEFAULT_SCALAR_TIME_STAMPS_VAR_NAME = "GeomagneticScalarTimes";

    /** get a variable name that can be used for temperature data
     * @param suffix a suffix for the name of the temperature time stamp variable, so that
     *        the CDF file can contain more than one temperature time stamp variable if there
     *        are temperature data arrays with different sample periods in the CDF file
     * @return the name of the variable */
    public static String getDefaultTemperatureTimeStampsVarName (String suffix)
    {
        return "Temperature" + suffix + "Times";
    }
    
    /** code for the type of data that can be held */
    public enum VariableTypeCode {
        /** geomagnetic data */
        GeomagneticFieldElement, 
        /** temperature data */
        Temperature
    }

    // the variable type code represented by this object
    private VariableTypeCode code;

    /** create a variable type code from the enumeration of the codes
     * @param code - one of the enumeration values for the code */
    public IMCDFVariableType (VariableTypeCode code)
    {
        this.code = code;
    }

    /** create a baseline type code from a string
     * @param code_string one of "GeomagneticField", or "Temperature"
     * @throws ParseException if the string could not be recognised */
    public IMCDFVariableType (String code_string)
    throws ParseException
    {
        if (code_string.equalsIgnoreCase("GeomagneticFieldElement"))
            code = VariableTypeCode.GeomagneticFieldElement;
        else if (code_string.equalsIgnoreCase("Temperature"))
            code = VariableTypeCode.Temperature;
        else
            throw new ParseException("Invalid variable type code: " + code_string, 0);
    }

    /** get the code for the type of CDF variable
     * @return the code */
    public VariableTypeCode getCode () { return code; }

    /** get a string representation of the code
     * @return one of "GeomagneticFieldElement" or "Temperature" */
    @Override
    public String toString ()
    {
        switch (code)
        {
            case GeomagneticFieldElement: return "GeomagneticField";
            case Temperature: return "Temperature";
        }
        return "Unknown";
    }
    
    /** create the name of the variable that will be used in the CDF file
     * @param suffix the suffix to the base name
     * @return the variable name */
    public String getCDFFileVariableName (String suffix)
    {
        return toString() + suffix;
    }
    
}
