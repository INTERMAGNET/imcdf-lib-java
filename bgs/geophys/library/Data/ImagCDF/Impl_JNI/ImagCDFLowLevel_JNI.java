/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF.Impl_JNI;

import bgs.geophys.library.Data.ImagCDF.IMCDFException;
import bgs.geophys.library.Data.ImagCDF.IMCDFPrintEnum;
import bgs.geophys.library.Misc.DateUtils;
import gsfc.nssdc.cdf.*;
import gsfc.nssdc.cdf.util.CDFTT2000;
import java.io.File;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * Error handling strategy for reading CDF files: errors in individual reading 
 * routines are add to a list, which allows multiple errors to be recorded for a 
 * single file (rather than just the first error found). Once reading has completed,
 * call getAccumulatedErrors() to get the list if errors and check whether it
 * is empty of not.
 * 
 * @author smf
 */
public class ImagCDFLowLevel_JNI 
{

    /** CDF creation codes:
     *     CDFForceCreate - create the CDF, deleting any existing file;
     *     CDFForceCreate - create the CDF, but don't delete any existing file -
     *                      an existing file will cause an error;
     *     CDFOpen - open the CDF, which must already exist */
    public enum CDFOpenType { CDFForceCreate, CDFCreate, CDFOpen }

    /** an enumeration used to set the type of compression on new CDF files */
    public enum CDFCompressType {None, RLE, Huff, AHuff, GZip1, GZip2, GZip3, 
                                 GZip4, GZip5, GZip6, GZip7, GZip8, GZip9}

    /** an enumeration listing types of variable that can be created */
    public enum CDFVariableType {Double, TT2000}
    
    // private member data for this class
    private CDF cdf;
    private List<String> accumulated_errors;

    // static initialisers - creation of formatting objects
    private static final SimpleDateFormat DATA_DATE_FORMAT;
    private static final TimeZone GMT_TIME_ZONE;
    private static final GregorianCalendar GREGORIAN_CAL;
    static
    {
        DateFormatSymbols english_date_format_symbols;
        
        GMT_TIME_ZONE = TimeZone.getTimeZone("gmt");
        GREGORIAN_CAL = new GregorianCalendar(GMT_TIME_ZONE);

        try { english_date_format_symbols = new DateFormatSymbols (Locale.UK); }
        catch (MissingResourceException e) { english_date_format_symbols = null; }
        if (english_date_format_symbols == null)
        {
            try { english_date_format_symbols = new DateFormatSymbols (Locale.US); }
            catch (MissingResourceException e) { english_date_format_symbols = null; }
        }
        if (english_date_format_symbols == null) 
            english_date_format_symbols = new DateFormatSymbols ();
            
        DATA_DATE_FORMAT = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS zzz", english_date_format_symbols);
        DateUtils.fixSimpleDateFormat(DATA_DATE_FORMAT);
    }
    
    
    /** ------------------------------------------------------------------------
     *  --------------------- Opening and closing CDF files --------------------
     *  ------------------------------------------------------------------------*/
    
    /** open a CDF for reading or writing
     * @param filename the file to open
     * @param open_type how to open the file
     * @param compress_type the type of compression to apply to the file
     * @throws IMCDFException if there is an error */
    public ImagCDFLowLevel_JNI (String filename, CDFOpenType open_type,
                                CDFCompressType compress_type)
    throws IMCDFException
    {
        long params [];
        File file;

        accumulated_errors = null;
        try
        {
            switch (open_type)
            {
                case CDFForceCreate:
                    file = new File (filename);
                    if (file.exists()) file.delete();
                    cdf = CDF.create (filename);
                    break;
                case CDFCreate:
                    cdf = CDF.create (filename);
                    break;
                case CDFOpen:
                    cdf = CDF.open (filename);
                    compress_type = CDFCompressType.None;
                    accumulated_errors = new ArrayList<String> ();
                    break;
            }

            params = new long [1];
            switch (compress_type)
            {
                case RLE:
                    params [0] = CDF.RLE_OF_ZEROs;
                    cdf.setCompression(CDF.RLE_COMPRESSION, params);
                    break;
                case Huff:
                    params [0] = CDF.OPTIMAL_ENCODING_TREES;
                    cdf.setCompression(CDF.HUFF_COMPRESSION, params);
                    break;
                case AHuff:
                    params [0] = CDF.OPTIMAL_ENCODING_TREES;
                    cdf.setCompression(CDF.AHUFF_COMPRESSION, params);
                    break;
                case GZip1:
                    params [0] = 1;
                    cdf.setCompression(CDF.GZIP_COMPRESSION, params);
                    break;
                case GZip2:
                    params [0] = 2;
                    cdf.setCompression(CDF.GZIP_COMPRESSION, params);
                    break;
                case GZip3:
                    params [0] = 3;
                    cdf.setCompression(CDF.GZIP_COMPRESSION, params);
                    break;
                case GZip4:
                    params [0] = 4;
                    cdf.setCompression(CDF.GZIP_COMPRESSION, params);
                    break;
                case GZip5:
                    params [0] = 5;
                    cdf.setCompression(CDF.GZIP_COMPRESSION, params);
                    break;
                case GZip6:
                    params [0] = 6;
                    cdf.setCompression(CDF.GZIP_COMPRESSION, params);
                    break;
                case GZip7:
                    params [0] = 7;
                    cdf.setCompression(CDF.GZIP_COMPRESSION, params);
                    break;
                case GZip8:
                    params [0] = 8;
                    cdf.setCompression(CDF.GZIP_COMPRESSION, params);
                    break;
                case GZip9:
                    params [0] = 9;
                    cdf.setCompression(CDF.GZIP_COMPRESSION, params);
                    break;
            }
        }
        catch (CDFException e)
        {
            throw new IMCDFException (e);
        }
    }

    /** open a CDF for reading or writing
     * @param file the file to open
     * @param open_type how to open the file
     * @param compress_type the type of compression to apply to the file
     * @throws IMCDFException if there is an error */
    public ImagCDFLowLevel_JNI (File file, CDFOpenType open_type,
                                CDFCompressType compress_type)
    throws IMCDFException
    {
        this (file.getAbsolutePath(), open_type, compress_type);
    }

    
    /** close the CDF - you MUST call this after writing to the CDF otherwise it will be corrupt
     * @throws IMCDFException if there was an error with the CDF */
    public void close ()
    throws IMCDFException
    {
        try
        {
            if (cdf != null) cdf.close();
            cdf = null;
        }
        catch (CDFException e)
        {
            throw new IMCDFException (e);
        }
    }

    
    /** ------------------------------------------------------------------------
     *  ------------------------- Writing to CDF files -------------------------
     *  ------------------------------------------------------------------------*/
    
    /** add a global attribute to the CDF file and make an entry in it
     * @param name the attribute name (must be unique)
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the value is null
     * @param value the contents of the entry
     * @throws IMCDFException if there is an error */
    public void addGlobalAttribute (String name, int entry_no, boolean mandatory, String value)
    throws IMCDFException
    {
        try
        {
            if (value == null)
            {
                if (mandatory) throw new CDFException ("Missing value for mandatory attribute " + name);
            }
            else if (value.length() <= 0)
            {
                if (mandatory) throw new CDFException ("Missing value for mandatory attribute " + name);
            }
            else
                Entry.create (safeCreateGlobalAttribute(name), entry_no, CDF.CDF_CHAR, value);        
        }
        catch (CDFException e)
        {
            throw new IMCDFException (e);
        }
    }
    
    /** add a global attribute to the CDF file and make an entry in it
     * @param name the attribute name (must be unique)
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the value is null
     * @param value the contents of the entry
     * @throws IMCDFException if there is an error */
    public void addGlobalAttribute (String name, int entry_no, boolean mandatory, IMCDFPrintEnum value)
    throws IMCDFException
    {
        try
        {
            if (value == null)
            {
                if (mandatory) throw new CDFException ("Missing value for mandatory attribute " + name);
            }
            else
                Entry.create (safeCreateGlobalAttribute(name), entry_no, CDF.CDF_CHAR, value.toString());
        }
        catch (CDFException e)
        {
            throw new IMCDFException (e);
        }
    }
    
    /** add a global attribute to the CDF file and make an entry in it
     * @param name the attribute name (must be unique)
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the value is null
     * @param value the contents of the entry
     * @throws IMCDFException if there is an error */
    public void addGlobalAttribute (String name, int entry_no, boolean mandatory, Double value)
    throws IMCDFException
    {
        try
        {
            if (value == null)
            {
                if (mandatory) throw new CDFException ("Missing value for mandatory attribute " + name);
            }
            else
                Entry.create (safeCreateGlobalAttribute(name), entry_no, CDF.CDF_DOUBLE, value);
        }
        catch (CDFException e)
        {
            throw new IMCDFException (e);
        }
    }

    /** add a global attribute to the CDF file and make an entry in it
     * @param name the attribute name (must be unique)
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the value is null
     * @param value the contents of the entry
     * @throws IMCDFException if there is an error */
    public void addGlobalAttribute (String name, int entry_no, boolean mandatory, Date value)
    throws IMCDFException
    {
        try
        {
            if (value == null)
            {
                if (mandatory) throw new CDFException ("Missing value for mandatory attribute " + name);
            }
            else
                Entry.create (safeCreateGlobalAttribute(name), entry_no, CDF.CDF_TIME_TT2000, ImagCDFLowLevel_JNI.DateToTT2000(value));
        }
        catch (CDFException e)
        {
            throw new IMCDFException (e);
        }
    }
    
    /** add a variable attribute to the CDF file and make an entry in it
     * @param name the attribute name
     * @param var the variable that this entry applies to
     * @param value the contents of the entry
     * @throws IMCDFException if there is an error */
    public void addVariableAttribute (String name, Variable var, String value)
    throws IMCDFException
    {
        Attribute attr;

        try
        {
            attr = findVariableAttribute (name);
            var.putEntry (attr, CDF.CDF_CHAR, value);
        }
        catch (CDFException e)
        {
            throw new IMCDFException (e);
        }
    }
    
    /** add a variable attribute to the CDF file and make an entry in it
     * @param name the attribute name
     * @param var the variable that this entry applies to
     * @param value the contents of the entry
     * @throws IMCDFException if there is an error */
    public void addVariableAttribute (String name, Variable var, double value)
    throws IMCDFException
    {
        Attribute attr;
        
        try
        {
            attr = findVariableAttribute (name);
            var.putEntry (attr, CDF.CDF_DOUBLE, value);
        }
        catch (CDFException e)
        {
            throw new IMCDFException (e);
        }
    }
    
    /** create a 0 dimensional data array in the CDF file
     * @param name the name of the variable
     * @param var_type the type of variable to create (Double or Long))
     * @return the variable that is used to hold the data
     * @throws IMCDFException if there is an error */
    public Variable createDataVariable (String name, CDFVariableType var_type)
    throws IMCDFException
    {
        Variable var;

        try
        {
            switch (var_type)
            {
                case Double:
                    var = Variable.create (cdf, name, CDF.CDF_DOUBLE, 1, 0, new long [] {1}, CDF.VARY, new long [] {CDF.VARY} );
                    break;
                case TT2000:
                    var = Variable.create (cdf, name, CDF.CDF_TIME_TT2000, 1, 0, new long [] {1}, CDF.VARY, new long [] {CDF.VARY} );
                    break;
                default:
                    throw new CDFException ("Unrecongnised data type");
            }
        }
        catch (CDFException e)
        {
            throw new IMCDFException (e);
        }
        
        return var;
    }
       
    /** put a data sample into a record in the CDF file 
     * @param var the variable to write to
     * @param rec_no the record to write to
     * @param data the data to write
     * @throws IMCDFException if there is an error */
    public void addData (Variable var, int rec_no, double data)
    throws IMCDFException
    {
        try
        {
            var.putRecord (rec_no, data);
        }
        catch (CDFException e)
        {
            throw new IMCDFException (e);
        }
    }
    
    /** put multiple data samples into consecutive records in the CDF file 
     * @param var the variable to write to
     * @param rec_no the 1st record to write to
     * @param data the data to write
     * @throws IMCDFException if there is an error */
    public void addData (Variable var, int rec_no, double data [])
    throws IMCDFException
    {
        addData (var, rec_no, data, 0, data.length);
    }
    
    /** put multiple data samples into consecutive records in the CDF file 
     * @param var the variable to write to
     * @param rec_no the 1st record to write to
     * @param data the data to write
     * @param data_offset offset in the data array to the first data point to write
     * @param data_length number of samples to write
     * @throws IMCDFException if there is an error */
    public void addData (Variable var, int rec_no, double data [], int data_offset, int data_length)
    throws IMCDFException
    {
        double data_slice [];
        
        try
        {
            if (data_offset != 0 || data_length != data.length)
            {
                // we need to slice the data - I can't see a way to do this with the Java CDF library
                data_slice = new double [data_length];
                System.arraycopy(data, data_offset, data_slice, 0, data_length);
            }
            else data_slice = data;
            var.putHyperData (rec_no, data_length, 1, new long [] {0}, new long [] {data_length}, new long [] {0}, data_slice);
        }
        catch (CDFException e)
        {
            throw new IMCDFException (e);
        }
    }
    
    /** put a time stamp into a record in the CDF file 
     * @param var the variable to write to
     * @param rec_no the record to write to
     * @param data the data to write
     * @throws IMCDFException if there is an error */
    public void addTimeStamp (Variable var, int rec_no, long data)
    throws IMCDFException
    {
        try
        {
            var.putRecord (rec_no, data);
        }
        catch (CDFException e)
        {
            throw new IMCDFException (e);
        }
    }

    /** put multiple time stamps into consecutive records in the CDF file 
     * @param var the variable to write to
     * @param rec_no the 1st record to write to
     * @param data the data to write
     * @throws IMCDFException if there is an error */
    public void addTimeStamp (Variable var, int rec_no, long data [])
    throws IMCDFException
    {
        addTimeStamp (var, rec_no, data, 0, data.length);
    }
    
    /** put multiple time stamps into consecutive records in the CDF file 
     * @param var the variable to write to
     * @param rec_no the 1st record to write to
     * @param data the data to write
     * @param data_offset offset in the data array to the first data point to write
     * @param data_length number of samples to write
     * @throws IMCDFException if there is an error */
    public void addTimeStamp (Variable var, int rec_no, long data [], int data_offset, int data_length)
    throws IMCDFException
    {
        long data_slice [];
        
        try
        {
            if (data_offset != 0 || data_length != data.length)
            {
                // we need to slice the data - I can't see a way to do this with the Java CDF library
                data_slice = new long [data_length];
                System.arraycopy(data, data_offset, data_slice, 0, data_length);
            }
            else data_slice = data;
            var.putHyperData (rec_no, data_length, 1, new long [] {0}, new long [] {data_length}, new long [] {0}, data_slice);
        }
        catch (CDFException e)
        {
            throw new IMCDFException (e);
        }
    }

    
    /** ------------------------------------------------------------------------
     *  ----------------------- Reading from CDF files -------------------------
     *  ------------------------------------------------------------------------*/

    /** get the contents of a global attribute
     * @param name the name of the attribute
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the attribute entry
     *                  does not exist, otherwise return null
     * @return the value of the attribute */
    public String getGlobalAttributeString (String name, int entry_no, boolean mandatory)
    {
        Entry entry = getAttributeEntry(name, entry_no, mandatory);
        if (entry == null) return null;
        try {
            if (entry.getData () instanceof String) return (String) entry.getData();
        } catch (CDFException e) {
            process_error ("Error reading global attribute name/entry: " + name + "/" + Integer.toString (entry_no), e);
            return null;
        }
        accumulated_errors.add ("Incorrect global attribute \"" + name + "\", data type should be String");
        return null;
    }

    /** get the contents of a global attribute
     * @param name the name of the attribute
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the attribute entry
     *                  does not exist, otherwise return null
     * @return the value of the attribute */
    public Double getGlobalAttributeDouble (String name, int entry_no, boolean mandatory)
    {
        try
        {
            Entry entry = getAttributeEntry(name, entry_no, mandatory);
            if (entry == null) return null;
            if (entry.getData () instanceof Double) return (Double) entry.getData();
            accumulated_errors.add ("Incorrect global attribute \"" + name + "\", data type should be Double");
            return null;
        }
        catch (CDFException e)
        {
            process_error ("Error reading global attribute name/entry: " + name + "/" + Integer.toString (entry_no), e);
            return null;
        }
    }
    
    /** get the contents of a global attribute
     * @param name the name of the attribute
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the attribute entry
     *                  does not exist, otherwise return null
     * @return the value of the attribute */
    public Date getGlobalAttributeDate (String name, int entry_no, boolean mandatory)
    { 
        Entry entry = getAttributeEntry(name, entry_no, mandatory);
        if (entry == null) return null;
        try {
            if (entry.getData() instanceof Long) return TT2000ToDate ((Long) entry.getData());
            if (entry.getData() instanceof String)
            {
                String string = ((String) entry.getData()).replace('T', ' ').replaceAll("Z", "");
                SimpleDateFormat formatter = new SimpleDateFormat ("YYYY-mm-dd hh:mm:ss");
                DateUtils.fixSimpleDateFormat(formatter);
                try { return formatter.parse (string); }
                catch (ParseException e) {}
            }
        } 
        catch (CDFException e) 
        {
            process_error ("Error reading global attribute name/entry: " + name + "/" + Integer.toString (entry_no), e);
            return null;
        }
        accumulated_errors.add ("Incorrect global attribute \"" + name + "\", should be Long/TT2000 or ISO 8601 format string");
        return null;
    }
    
    /** get the contents of a variable attribute
     * @param name the name of the attribute
     * @param var the variable that is expected to have an entry in this attribute
     * @return the value of the attribute's entry */
    public String getVariableAttributeString (String name, Variable var)
    {
        if (var == null)
        {
            accumulated_errors.add ("Missing data variable");
            return null;
        }
        try
        {
            Object data = cdf.getAttribute (name).getEntry(var).getData();
            if (data instanceof String) return (String) data;
            accumulated_errors.add ("Incorrect variable \"" + var.getName() + "\", attribute \"" + name + "\", data type (should be String)");
            return null;
        }
        catch (CDFException e)
        {
            process_error ("Error reading variable \"" + var.getName() + "\", attribute \"" + name + "\"", e);
            return null;
        }
    }
    
    /** get the contents of a variable attribute
     * @param name the name of the attribute
     * @param var the variable that is expected to have an entry in this attribute
     * @return the value of the attribute's entry */
    public Double getVariableAttributeDouble (String name, Variable var)
    {
        if (var == null)
        {
            accumulated_errors.add ("Missing data variable");
            return null;
        }
        try
        {
            Object data = cdf.getAttribute (name).getEntry(var).getData();
            if (data instanceof Double) return ((Double) data);
            accumulated_errors.add ("Incorrect variable \"" + var.getName() + "\", attribute \"" + name + "\", data type (should be Double)");
            return null;
        }
        catch (CDFException e)
        {
            process_error ("Error reading variable \"" + var.getName() + "\", attribute \"" + name + "\"", e);
            return null;
        }
    }

    /** get data from a variable
     * @param name the name of the variable
     * @return the variable */
    public Variable getVariable (String name)
    {
        try
        {
            return cdf.getVariable(name);
        }
        catch (CDFException e)
        {
            process_error ("Error getting variable \"" + name + "\"", e);
            return null;
        }
    }
    
    /** does the given variable exist in the CDF file
     * @param name the name of the variable to test for
     * @return true if it exists, false otherwise */
    public boolean isVariableExist (String name)
    {
        if (cdf.getVariableID(name) == -1) return false;
        return true;
    }
    
    /** get an attribute entry, optionally throwing an exception if it doesn't exit
     * 
     * @param name the attribute name
     * @param entry_no the index number of the entry
     * @param mandatory if true and the attribute/entry doesn't exist, throw an exception
     * @return the entry */
    private Entry getAttributeEntry (String name, int entry_no, boolean mandatory)
    {
        try
        {
            return cdf.getAttribute (name).getEntry(entry_no);
        }
        catch (CDFException ex)
        {
            if (mandatory)
                process_error ("Error reading global attribute name/entry: " + name + "/" + Integer.toString (entry_no), ex);
        }
        return null;
    }
        
    /** get the list of accumulated errors after reading the CDF file
     * @return the list */
    public List<String> getAccumulatedErrors ()
    {
        return accumulated_errors;
    }

    /** add an exception to accumulated error list
     * @param msg the message for the exception
     * @param e the underlying exception */
    public void process_error (String msg, Exception e)
    {
        if (e.getMessage() != null)
            msg += ": " + e.getMessage();
        accumulated_errors.add (msg);
    }
    
    /** ------------------------------------------------------------------------
     *  ---------------------------- Useful utilities --------------------------
     *  ------------------------------------------------------------------------*/

    /** a utility call that allows an application to check whether the CDF native library
     * is available and return gracefully if not.
     * @param prefix a program name (or other) prefix to put infront of the help
     *        text - may be null or empty
     * @return a help string describing how to get the native library (if it's missing)
     *         or null if the library is present and correct */
    public static String checkNativeLib (String prefix) {
        return checkNativeLib(prefix, false);
    }
    
    /** a utility call that allows an application to check whether the CDF native library
     * is available and return gracefully if not.
     * @param prefix a program name (or other) prefix to put infront of the help
     *        text - may be null or empty
     * @param short_msg true for a short message, false for a long one
     * @return a help string describing how to get the native library (if it's missing)
     *         or null if the library is present and correct */
    public static String checkNativeLib (String prefix, boolean short_msg)
    {
        String p2, errmsg;
        int count;

        // check the library - if it's missing attempt to get an error
        errmsg = null;
        try
        {
            if (CDF.getLibraryVersion() == null) errmsg = "CDF library version call returned no information";
        }
        catch (UnsatisfiedLinkError e) 
        { 
            if (e.getMessage() == null) errmsg = "Unsatisfied link error (no message in exception)"; 
            else if (e.getMessage().length() <= 0) errmsg = "Unsatisfied link error (no message in exception)"; 
            else errmsg = e.getMessage();
        }
        catch (NoClassDefFoundError e)
        {
            if (e.getMessage() == null) errmsg = "No class definition found error (no message in exception)"; 
            else if (e.getMessage().length() <= 0) errmsg = "No class definition found error (no message in exception)"; 
            else errmsg = e.getMessage();
        }
        catch (CDFException e)
        {
            if (e.getMessage() == null) errmsg = "CDF exception (no message in exception)"; 
            else if (e.getMessage().length() <= 0) errmsg = "CDF exception (no message in exception)"; 
            else errmsg = e.getMessage();
        }        

        // successful return
        if (errmsg == null) return null;

        // if caller requested a short message, return
        if (short_msg) return errmsg;

        // provide help
        if (prefix == null) prefix = "";
        p2 = "";
        for (count=0; count<prefix.length(); count++) p2 += " ";
        return prefix + "The CDF libraries cannot be found on this system.\n" +
               p2 +     "When attempting to find the libraries the system\n" +
               p2 +     "returned this error message:\n" +
               p2 +     "  " + errmsg +
               p2 +     "Without these libraries this program cannot run.\n" +
               p2 +     "To obtain these libraries you need to install the\n" +
               p2 +     "CDF program from NASA, you can obtain this from:\n" +
               p2 +     "  http://cdf.gsfc.nasa.gov/\n" +
               p2 +     "Select the 'Download CDF software' link, then download\n" +
               p2 +     "the latest version. For Windows systems a compiled\n" +
               p2 +     "package is available which you can install in the usual\n" +
               p2 +     "way, for other systems you may need to compile CDF.\n" +
               p2 +     "The libraries need to be installed in a directory pointed\n" +
               p2 +     "to by the PATH (Windows) or LD_LIBRARY_PATH (UNIX)\n" +
               p2 +     "environment variable. On Windows the CDF installer does\n" +
               p2 +     "this for you, on UNIX you may need to set the variables\n" +
               p2 +     "yourself.\n";
        
    }

    /** get data from a variable
     * @param var the variable
     * @return the data or null if there was an error */
    public double [] getDataArray (Variable var)
    {
        CDFData cdf_data;

        if (var == null)
        {
            accumulated_errors.add ("Missing data variable");
            return null;
        }
        try
        {
            cdf_data = var.getRecordsObject(0l, var.getNumWrittenRecords());
            Object data = cdf_data.getData();
            if (data == null) {
                accumulated_errors.add ("Missing data: " + var.getName());
                return null;
            }
            if (data instanceof double [])
                return (double []) data;
            if (data instanceof Double []) {
                Double orig_array [] = (Double []) data;
                double [] new_array = new double [orig_array.length];
                for (int count=0; count<orig_array.length; count++)
                    new_array[count] = (double) orig_array[count];
                return new_array;
            }
            if (data instanceof Double) {
                double [] array = new double [1];
                array [0] = (double) data;
                return array;
            }
            accumulated_errors.add ("Unexpected data type in data array: " + var.getName());
            return null;
        }
        catch (CDFException e)
        {
            process_error("Error reading data " + var.getName(), e);
            return null;
        }
    }
    
    /** get TT2000 time stamps from a variable
     * @param var the variable
     * @return the data or null if there was an error */
    public long [] getTimeStampArray (Variable var)
    {
        CDFData cdf_data;

        if (var == null)
        {
            accumulated_errors.add ("Missing time stamp variable");
            return null;
        }
        try
        {
            cdf_data = var.getRecordsObject(0l, var.getNumWrittenRecords());
            Object data = cdf_data.getData();
            if (data == null) {
                accumulated_errors.add ("Missing time stamp data: " + var.getName());
                return null;
            }
            if (data instanceof long [])
                return (long []) data;
            accumulated_errors.add ("Unexpected data type in time stamp array: " + var.getName());
            return null;
        }
        catch (CDFException e)
        {
            process_error ("Error reading time stamp variable " + var.getName(), e);
            return null;
        }
    }
    
    /** find the maximum or minimum value that a particualr geomagnetic element
     * may legally take
     * @param element string containing the single element character, e.g. H, D, Z, X, Y, ...
     * @param max true to return the maximum legal value, false to return the minimum
     * @return the maximum of minimum value that the element may take */
    public static double getValidMaxMin (String element, boolean max)
    {
        if ("XYZHEV".contains(element.toUpperCase()))
        {
            if (max) return 79999.0;
            return -79999.0;
        }
        else if (element.equalsIgnoreCase("D"))
        {
            if (max) return 360.0;
            return -360.0;
        }
        else if (element.equalsIgnoreCase("I"))
        {
            if (max) return 90.0;
            return -90.0;
        }
        else if (element.equalsIgnoreCase("F"))
        {
            if (max) return 79999.0;
            return 0.0;
        }
        else if (element.equalsIgnoreCase("S"))
        {
            if (max) return 79999.0;
            return 0.0;
        }
        else if (element.equalsIgnoreCase("G"))
        {
            if (max) return 79999.0;
            return -79999.0;
        }
        return 0.0;
    }

    /** get the name of the units corresponding to particular geomagnetic elements
     * @param element string containing the single element character, e.g. H, D, Z, X, Y, ...
     * @return the name of the units */
    public static String getUnits (String element)
    {
        if ("XYZHEVFSG".contains(element.toUpperCase()))
            return "nT";
        else if ("DI".contains(element.toUpperCase()))
            return "Degrees of arc";
        return "Unknown";
    }
   
    public static long DateToTT2000 (Date date)
    {
        GREGORIAN_CAL.setTime(date);
        return CDFTT2000.fromGregorianTime(GREGORIAN_CAL);
    }
    
    public static long DateToTT2000 (long date)
    {
        GREGORIAN_CAL.setTimeInMillis(date);
        return CDFTT2000.fromGregorianTime(GREGORIAN_CAL);
    }
    
    public static Date TT2000ToDate (long tt2000)
    {
        GregorianCalendar cal;
        
        cal = CDFTT2000.toGregorianTime(tt2000);
        cal.setTimeZone(GMT_TIME_ZONE);
        return new Date (cal.getTimeInMillis());
        
    }
    
    /** ------------------------------------------------------------------------
     *  ---------------------------- Private code ------------------------------
     *  ------------------------------------------------------------------------*/
    
    /** find a variable attribute - if it doesn't exist create it */
    private Attribute findVariableAttribute (String name)
    throws CDFException
    {
        Attribute attr;
        
        try
        {
            attr = cdf.getAttribute (name);
        }
        catch (CDFException e)
        {
            attr = Attribute.create (cdf, name, CDF.VARIABLE_SCOPE);
        }
        return attr;
    }
    
    private Attribute safeCreateGlobalAttribute (String name)
    throws CDFException
    {
        Attribute attr;
        try
        {
            attr = cdf.getAttribute(name);
        }
        catch (CDFException e)
        {
            attr = Attribute.create (cdf, name, CDF.GLOBAL_SCOPE);
        }
        return attr;
    }
    
}
