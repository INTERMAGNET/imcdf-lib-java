package bgs.geophys.library.Data.ImagCDF;

import java.util.ArrayList;
import java.util.List;

/** this is the single exception for all faults in the ImagCDF code. Previously
  * the code used the CDFException from the NASA Java Native CDF library, but
  * the new Intermagnet CDF software uses both NASA's JNI and Pure Java libraries
  * and these two libraries use different CDFException classes. One of the main
  * tasks of this class is to allow both these exceptions to be passed back to
  * clients using this single class
  */
public class IMCDFException extends Exception
{
    private final List<String> errors;
    
    /** create an IMCDFException with an error message]
     * @param message the message */
    public IMCDFException (String message)
    {
        super (message);
        errors = new ArrayList<> ();
    }
    
    /** create an IMCDFException with a root cause
     * @param cause the root cause */
    public IMCDFException (Throwable cause)
    {
        super (cause.getMessage(), cause);
        errors = new ArrayList<> ();
    }
    
    /** create an IMCDFException with an error message] and a root cause
     * @param message the message
     * @param cause  the root cause */
    public IMCDFException (String message, Throwable cause)
    {
        super (message, cause);
        errors = new ArrayList<> ();
    }
    
    /** create an IMCDFException with a list of accumulated errors
     * @param message the message
     * @param errors the list of errors */
    public IMCDFException (String message, List<String> errors)
    {
        super (message);
        if (errors == null)
            this.errors = new ArrayList<> ();
        else
            this.errors = errors;
    }

    /** get the list of errors
     * @return the error list, which may be empty */
    public List<String> getErrorList ()
    {
        return errors;
    }
}
