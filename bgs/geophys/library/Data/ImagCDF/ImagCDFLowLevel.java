/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import gsfc.nssdc.cdf.*;
import gsfc.nssdc.cdf.util.CDFTT2000;
import java.io.File;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class ImagCDFLowLevel 
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

    // static initialisers - creation of formatting objects
    private static final SimpleDateFormat dataDateFormat;
    private static final TimeZone gmtTimeZone;
    private static final GregorianCalendar greg_cal;
    static
    {
        DateFormatSymbols english_date_format_symbols;
        
        gmtTimeZone = TimeZone.getTimeZone("gmt");
        greg_cal = new GregorianCalendar(gmtTimeZone);

        try { english_date_format_symbols = new DateFormatSymbols (Locale.UK); }
        catch (MissingResourceException e) { english_date_format_symbols = null; }
        if (english_date_format_symbols == null)
        {
            try { english_date_format_symbols = new DateFormatSymbols (Locale.US); }
            catch (MissingResourceException e) { english_date_format_symbols = null; }
        }
        if (english_date_format_symbols == null) 
            english_date_format_symbols = new DateFormatSymbols ();
            
        dataDateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS zzz", english_date_format_symbols);
        dataDateFormat.setTimeZone(gmtTimeZone);
    }
    
    
    /** ------------------------------------------------------------------------
     *  --------------------- Opening and closing CDF files --------------------
     *  ------------------------------------------------------------------------*/
    
    /** open a CDF for reading or writing
     * @param filename the file to open
     * @param open_type how to open the file
     * @param compress_type the type of compression to apply to the file
     * @throws CDFException if there is an error */
    public ImagCDFLowLevel (String filename, CDFOpenType open_type,
                            CDFCompressType compress_type)
    throws CDFException
    {
        long params [];
        File file;
        
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

    /** open a CDF for reading or writing
     * @param file the file to open
     * @param open_type how to open the file
     * @param compress_type the type of compression to apply to the file
     * @throws CDFException if there is an error */
    public ImagCDFLowLevel (File file, CDFOpenType open_type,
                            CDFCompressType compress_type)
    throws CDFException
    {
        this (file.getAbsolutePath(), open_type, compress_type);
    }
    
    /** close and CDF - you MUST call this after writing to the CDF otherwise it will be corrupt */
    public void close ()
    throws CDFException
    {
        if (cdf != null) cdf.close();
        cdf = null;
    }

    
    /** ------------------------------------------------------------------------
     *  ------------------------- Writing to CDF files -------------------------
     *  ------------------------------------------------------------------------*/
    
    /** add a global attribute to the CDF file and make an entry in it
     * @param name the attribute name (must be unique)
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the value is null
     * @param value the contents of the entry
     * @throws CDFException if there is an error */
    public void addGlobalAttribute (String name, int entry_no, boolean mandatory, String value)
    throws CDFException
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
            Entry.create (Attribute.create (cdf, name, CDF.GLOBAL_SCOPE), 0, CDF.CDF_CHAR, value);        
    }
    
    /** add a global attribute to the CDF file and make an entry in it
     * @param name the attribute name (must be unique)
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the value is null
     * @param value the contents of the entry
     * @throws CDFException if there is an error */
    public void addGlobalAttribute (String name, int entry_no, boolean mandatory, IMCDFPrintEnum value)
    throws CDFException
    {
        if (value == null)
        {
            if (mandatory) throw new CDFException ("Missing value for mandatory attribute " + name);
        }
        else
            Entry.create (Attribute.create (cdf, name, CDF.GLOBAL_SCOPE), 0, CDF.CDF_CHAR, value.toString());
    }
    
    /** add a global attribute to the CDF file and make an entry in it
     * @param name the attribute name (must be unique)
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the value is null
     * @param value the contents of the entry
     * @throws CDFException if there is an error */
    public void addGlobalAttribute (String name, int entry_no, boolean mandatory, Double value)
    throws CDFException
    {
        if (value == null)
        {
            if (mandatory) throw new CDFException ("Missing value for mandatory attribute " + name);
        }
        else
            Entry.create (Attribute.create (cdf, name, CDF.GLOBAL_SCOPE), 0, CDF.CDF_DOUBLE, new Double (value));
    }

    /** add a global attribute to the CDF file and make an entry in it
     * @param name the attribute name (must be unique)
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the value is null
     * @param value the contents of the entry
     * @throws CDFException if there is an error */
    public void addGlobalAttribute (String name, int entry_no, boolean mandatory, Date value)
    throws CDFException
    {
        if (value == null)
        {
            if (mandatory) throw new CDFException ("Missing value for mandatory attribute " + name);
        }
        else
            Entry.create (Attribute.create (cdf, name, CDF.GLOBAL_SCOPE), 0, CDF.CDF_TIME_TT2000, new Long (ImagCDFLowLevel.DateToTT2000(value)));
    }
    
    /** add a variable attribute to the CDF file and make an entry in it
     * @param name the attribute name
     * @param var the variable that this entry applies to
     * @param value the contents of the entry
     * @throws CDFException if there is an error */
    public void addVariableAttribute (String name, Variable var, String value)
    throws CDFException
    {
        Attribute attr;
        
        attr = findVariableAttribute (name);
        var.putEntry (attr, CDF.CDF_CHAR, value);
    }
    
    /** add a variable attribute to the CDF file and make an entry in it
     * @param name the attribute name
     * @param var the variable that this entry applies to
     * @param value the contents of the entry
     * @throws CDFException if there is an error */
    public void addVariableAttribute (String name, Variable var, double value)
    throws CDFException
    {
        Attribute attr;
        
        attr = findVariableAttribute (name);
        var.putEntry (attr, CDF.CDF_DOUBLE, value);
    }
    
    /** create a 0 dimensional data array in the CDF file
     * @param name the name of the variable
     * @param var_type the type of variable to create (Double or Long))
     * @return the variable that is used to hold the data
     * @throws CDFException if there is an error */
    public Variable createDataVariable (String name, CDFVariableType var_type)
    throws CDFException
    {
        Variable var;

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
        
        return var;
    }
       
    /** put a data sample into a record in the CDF file 
     * @param var the variable to write to
     * @param rec_no the record to write to
     * @param data the data to write
     * @throws CDFException if there is an error */
    public void addData (Variable var, int rec_no, double data)
    throws CDFException
    {
        var.putRecord (rec_no, data);
    }
    
    /** put multiple data samples into consecutive records in the CDF file 
     * @param var the variable to write to
     * @param rec_no the 1st record to write to
     * @param data the data to write
     * @throws CDFException if there is an error */
    public void addData (Variable var, int rec_no, double data [])
    throws CDFException
    {
        addData (var, rec_no, data, 0, data.length);
    }
    
    /** put multiple data samples into consecutive records in the CDF file 
     * @param var the variable to write to
     * @param rec_no the 1st record to write to
     * @param data the data to write
     * @param data_offset offset in the data array to the first data point to write
     * @param data_length number of samples to write
     * @throws CDFException if there is an error */
    public void addData (Variable var, int rec_no, double data [], int data_offset, int data_length)
    throws CDFException
    {
        var.putHyperData (rec_no, data_length, 1, new long [] {data_offset}, new long [] {data_length}, new long [] {0}, data);
    }
    
    /** put a time stamp into a record in the CDF file 
     * @param var the variable to write to
     * @param rec_no the record to write to
     * @param data the data to write
     * @throws CDFException if there is an error */
    public void addTimeStamp (Variable var, int rec_no, long data)
    throws CDFException
    {
        var.putRecord (rec_no, data);
    }

    /** put multiple time stamps into consecutive records in the CDF file 
     * @param var the variable to write to
     * @param rec_no the 1st record to write to
     * @param data the data to write
     * @throws CDFException if there is an error */
    public void addTimeStamp (Variable var, int rec_no, long data [])
    throws CDFException
    {
        addTimeStamp (var, rec_no, data, 0, data.length);
    }
    
    /** put multiple time stamps into consecutive records in the CDF file 
     * @param var the variable to write to
     * @param rec_no the 1st record to write to
     * @param data the data to write
     * @param data_offset offset in the data array to the first data point to write
     * @param data_length number of samples to write
     * @throws CDFException if there is an error */
    public void addTimeStamp (Variable var, int rec_no, long data [], int data_offset, int data_length)
    throws CDFException
    {
        var.putHyperData (rec_no, data_length, 1, new long [] {data_offset}, new long [] {data_length}, new long [] {0}, data);
    }

    
    /** ------------------------------------------------------------------------
     *  ----------------------- Reading from CDF files -------------------------
     *  ------------------------------------------------------------------------*/

    /** get the contents of a global attribute
     * @param name the name of the attribute
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the attribute entry
     *                  does not exist, otherwise return null
     * @return the value of the attribute
     * @throws CDFException if there is an error */
    public String getGlobalAttributeString (String name, int entry_no, boolean mandatory)
    throws CDFException
    {
        Entry entry;
        
        entry = getAttributeEntry(name, entry_no, mandatory);
        if (entry == null) return null;
        return (String) entry.getData();
    }

    /** get the contents of a global attribute
     * @param name the name of the attribute
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the attribute entry
     *                  does not exist, otherwise return null
     * @return the value of the attribute
     * @throws CDFException if there is an error */
    public Double getGlobalAttributeDouble (String name, int entry_no, boolean mandatory)
    throws CDFException
    {
        Entry entry;
        
        entry = getAttributeEntry(name, entry_no, mandatory);
        if (entry == null) return null;
        return ((Double) entry.getData());
    }
    
    /** get the contents of a global attribute
     * @param name the name of the attribute
     * @param entry_no the entry number (zero based)
     * @param mandatory if true, throw an exception if the attribute entry
     *                  does not exist, otherwise return null
     * @return the value of the attribute
     * @throws CDFException if there is an error */
    public Date getGlobalAttributeDate (String name, int entry_no, boolean mandatory)
    throws CDFException
    {
        Entry entry;
        
        entry = getAttributeEntry(name, entry_no, mandatory);
        if (entry == null) return null;
        return TT2000ToDate ((Long) entry.getData());
    }
    
    /** get the contents of a variable attribute
     * @param name the name of the attribute
     * @param var the variable that is expected to have an entry in this attribute
     * @return the value of the attribute's entry
     * @throws CDFException if there is an error */
    public String getVariableAttributeString (String name, Variable var)
    throws CDFException
    {
        return (String) cdf.getAttribute (name).getEntry(var).getData();
    }
    
    /** get the contents of a variable attribute
     * @param name the name of the attribute
     * @param var the variable that is expected to have an entry in this attribute
     * @return the value of the attribute's entry
     * @throws CDFException if there is an error */
    public double getVariableAttributeDouble (String name, Variable var)
    throws CDFException
    {
        return ((Double) cdf.getAttribute (name).getEntry(var).getData()).doubleValue();
    }
    
    /** get data from a variable
     * @param name the name of the variable
     * @return the variable
     * @throws CDFException  if there is an error */
    public Variable getVariable (String name)
    throws CDFException
    {
        return cdf.getVariable(name);
    }
    
    /** does the given variable exist in the CDF file
     * @param name the name of the variable to test for
     * @return true if it exists, false otherwise */
    public boolean isVariableExist (String name)
    {
        if (cdf.getVariableID(name) == -1) return false;
        return true;
    }
    
    
    /** ------------------------------------------------------------------------
     *  ---------------------------- Useful utilities --------------------------
     *  ------------------------------------------------------------------------*/

    /** get an attribute entry, optionally throwing an exception if it doesn't exit */
    public Entry getAttributeEntry (String name, int entry_no, boolean mandatory)
    throws CDFException
    {
        try
        {
            return cdf.getAttribute (name).getEntry(entry_no);
        }
        catch (CDFException ex)
        {
            if (mandatory) throw ex;
        }
        return null;
    }
    
    /** a utility call that allows an application to check whether the CDF native library
     * is available and return gracefully if not.
     * @param prefix a program name (or other) prefix to put infront of the help
     *        text - may be null or empty
     * @return a help string describing how to get the native library (if it's missing)
     *         or null if the library is present and correct */
    public static String checkNativeLib (String prefix)
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
        catch (CDFException e)
        {
            if (e.getMessage() == null) errmsg = "CDF exception (no message in exception)"; 
            else if (e.getMessage().length() <= 0) errmsg = "CDF exception (no message in exception)"; 
            else errmsg = e.getMessage();
        }        

        // successful return
        if (errmsg == null) return null;

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
     * @param name the name of the variable
     * @return the data
     * @throws CDFException  if there is an error */
    public static double [] getDataArray (Variable var)
    throws CDFException
    {
        CDFData cdf_data;

        cdf_data = var.getRecordsObject(0l, var.getNumWrittenRecords());
        return (double []) cdf_data.getData();
    }
    
    /** get TT2000 time stamps from a variable
     * @param name the name of the variable
     * @return the data
     * @throws CDFException  if there is an error */
    public static long [] getTimeStampArray (Variable var)
    throws CDFException
    {
        CDFData cdf_data;

        cdf_data = var.getRecordsObject(0l, var.getNumWrittenRecords());
        return (long []) cdf_data.getData();
    }
    
    /** find the maximum or minimum value that a particualr geomagnetic element
     * may legally take
     * @param element string containing the single element character, e.g. H, D, Z, X, Y, ...
     * @param max true to return the maximum legal value, false to return the minimum
     * @return the maximum of minimum value that the element may take */
    public static double getValidMaxMin (String element, boolean max)
    {
        if ("XYZHEV".indexOf(element.toUpperCase()) >= 0)
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
        if ("XYZHEVFSG".indexOf(element.toUpperCase()) >= 0)
            return "nT";
        else if ("DI".indexOf(element.toUpperCase()) >= 0)
            return "Degrees of arc";
        return "Unknown";
    }
   
    public static long DateToTT2000 (Date date)
    throws CDFException
    {
        greg_cal.setTime(date);
        return CDFTT2000.fromGregorianTime(greg_cal);
    }
    
    public static long DateToTT2000 (long date)
    throws CDFException
    {
        greg_cal.setTimeInMillis(date);
        return CDFTT2000.fromGregorianTime(greg_cal);
    }
    
    public static Date TT2000ToDate (long tt2000)
    {
        GregorianCalendar cal;
        
        cal = CDFTT2000.toGregorianTime(tt2000);
        cal.setTimeZone(gmtTimeZone);
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
    
}
