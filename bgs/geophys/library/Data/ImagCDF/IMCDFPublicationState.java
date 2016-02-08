/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import java.text.ParseException;

/**
 * An object that represents the publication state of the data, that is how much editing
 * has been done, and converts to and from string representations of the code
 * 
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class IMCDFPublicationState 
{

    /** code for the publication state - the state of editing the data has been through:
     *      RAW - no edits made to the data;
     *      ADJUSTED - some edits made to the data;
     *      DEFINTIVE - final edits made to the data - no further editing will be done */
    public enum PubState {RAW, ADJUSTED, DEFINITIVE};

    // the publication state represented by this object
    private PubState pub_state;
    
    /** create a publication state code from the enumeration of the codes
     * @param code - one of the enumeration values for the code */
    public IMCDFPublicationState (PubState pub_state)
    {
        this.pub_state = pub_state;
    }
    
    /** create a publication state code from a string
     * @param code_string one of "raw", "adjusted" or "definitive"
     * @throws ParseException if the string could not be recognised */
    public IMCDFPublicationState (String pub_state_string)
    throws ParseException
    {
        if (pub_state_string.equalsIgnoreCase("raw"))
            pub_state = PubState.RAW;
        else if (pub_state_string.equalsIgnoreCase("adjusted"))
            pub_state = PubState.ADJUSTED;
        else if (pub_state_string.equalsIgnoreCase("definitive"))
            pub_state = PubState.DEFINITIVE;
        else
            throw new ParseException("Invalid publication state code: " + pub_state_string, 0);
    }
    
    public PubState getCode () { return pub_state; }
    
    /** get a string representation of the code
     * @return one of "Raw", "Adjusted" or "Definitive" */
    @Override
    public String toString ()
    {
        return toString (true);
    }    

    /** get a string representation of the code
     * @return one of "Raw", "Adjusted" or "Definitive" or the short form of one of these */
    public String toString (boolean long_form)
    {
        switch (pub_state)
        {
            case RAW: if (long_form) return "Raw"; return "R";
            case ADJUSTED: if (long_form) return "Adjusted"; return "A";
            case DEFINITIVE: if (long_form) return "Definitive"; return "D";
        }
        if (long_form) return "Unknown";
        return "U";
    }    
    
    /** convert an IMF or IAGA-2002 data type code to an IMCDF publication state
     * @param type_code one of the IMF or IAGA-2002 data type codes - only
     *        the first letter is used to decode the code
     * @return the publication state */
    public static IMCDFPublicationState getPublicationState (String type_code)
    {
        if (type_code.length() > 0)
        {
            switch (type_code.charAt(0))
            {
                case 'A': return new IMCDFPublicationState (IMCDFPublicationState.PubState.ADJUSTED);
                case 'R': return new IMCDFPublicationState (IMCDFPublicationState.PubState.RAW);
                case 'Q': return new IMCDFPublicationState (IMCDFPublicationState.PubState.ADJUSTED);
                case 'D': return new IMCDFPublicationState (IMCDFPublicationState.PubState.DEFINITIVE);
                case 'T': return new IMCDFPublicationState (IMCDFPublicationState.PubState.RAW);
                case 'Z': return new IMCDFPublicationState (IMCDFPublicationState.PubState.DEFINITIVE);
            }
        }
        return new IMCDFPublicationState (IMCDFPublicationState.PubState.RAW);
    }

}
