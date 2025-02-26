/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

/**
 * An object that represents the level of publication that a piece of geomagnetic data
 * has reached and converts to and from string representations of the code
 * 
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class IMCDFPublicationLevel 
implements IMCDFPrintEnum
{

    /** code for the publication level */
    public enum PublicationLevel {
        /** raw */
        LEVEL_1, 
        /** provisional */
        LEVEL_2, 
        /** quasi-definitive */
        LEVEL_3, 
        /** definitive */
        LEVEL_4
    }

    // the baseline code represented by this object
    private PublicationLevel level;

    /** create a baseline type code from the enumeration of the codes
     * @param level - one of the enumeration values for the code */
    public IMCDFPublicationLevel (PublicationLevel level)
    {
        this.level = level;
    }

    /** create a baseline type code from a string
     * @param level_string one of "1", "2", "3" or "4"
     * @throws IMCDFException if the string could not be recognised */
    public IMCDFPublicationLevel (String level_string)
    throws IMCDFException
    {
        if (level_string.equalsIgnoreCase("1"))
            level = PublicationLevel.LEVEL_1;
        else if (level_string.equalsIgnoreCase("2"))
            level = PublicationLevel.LEVEL_2;
        else if (level_string.equalsIgnoreCase("3"))
            level = PublicationLevel.LEVEL_3;
        else if (level_string.equalsIgnoreCase("4"))
            level = PublicationLevel.LEVEL_4;
        else
            throw new IMCDFException("Invalid baseline type code: " + level_string);
    }

    /** get the publication level (aka data type)
     * @return the publication level */
    public PublicationLevel getLevel () { return level; }

    /** get a string representation of the code
     * @return one of "1", "2", "3" or "4" */
    @Override
    public String toString ()
    {
        switch (level)
        {
            case LEVEL_1: return "1";
            case LEVEL_2: return "2";
            case LEVEL_3: return "3";
            case LEVEL_4: return "4";
        }
        return "0";
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // This section deals with translation between the ImagCDF publication level and
    // IMF and IAGA 2002 data types
    /////////////////////////////////////////////////////////////////////////////////////
    
    /** get an IMF data type
     * @param long_form true for string, false for single letter code
     * @return the data type code */
    public String getIMFDataType (boolean long_form)
    {
        switch (level)
        {
            case LEVEL_4:
                if (long_form) return "Definitive";
                return "D";
            case LEVEL_3:
                if (long_form) return "Quasi-definitive";
                return "Q";
            case LEVEL_2:
                if (long_form) return "Adjusted";
                return "A";
        }
        if (long_form) return "Reported";
        return "R";
    }
    
    /** get an IAGA-2002 data type
     * @param long_form true for string, false for single letter code
     * @return the data type code */
    public String getIAGA2002DataType (boolean long_form)
    {
        switch (level)
        {
            case LEVEL_4:
                if (long_form) return "Definitive";
                return "D";
            case LEVEL_3:
                if (long_form) return "Quasi-definitive";
                return "Q";
            case LEVEL_2:
                if (long_form) return "Provisional";
                return "P";
        }
        if (long_form) return "Variation";
        return "V";
    }
    /** convert an IMF or IAGA-2002 data type code to an IMCDF publication level
     * @param type_code one of the IMF or IAGA-2002 data type codes - only
     *        the first letter is used to decode the code
     * @return the baseline type */
    public static IMCDFPublicationLevel getPublicationLevel (String type_code)
    {
        if (type_code.length() > 0)
        {
            switch (type_code.charAt(0))
            {
                case 'A': return new IMCDFPublicationLevel (IMCDFPublicationLevel.PublicationLevel.LEVEL_2);
                case 'R': return new IMCDFPublicationLevel (IMCDFPublicationLevel.PublicationLevel.LEVEL_1);
                case 'Q': return new IMCDFPublicationLevel (IMCDFPublicationLevel.PublicationLevel.LEVEL_3);
                case 'D': return new IMCDFPublicationLevel (IMCDFPublicationLevel.PublicationLevel.LEVEL_4);
                case 'T': return new IMCDFPublicationLevel (IMCDFPublicationLevel.PublicationLevel.LEVEL_1);
                case 'Z': return new IMCDFPublicationLevel (IMCDFPublicationLevel.PublicationLevel.LEVEL_4);
            }
        }
        return new IMCDFPublicationLevel (IMCDFPublicationLevel.PublicationLevel.LEVEL_1);
    }
    
    
}
