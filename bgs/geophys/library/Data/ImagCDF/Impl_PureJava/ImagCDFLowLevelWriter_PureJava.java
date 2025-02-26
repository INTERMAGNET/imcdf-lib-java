/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF.Impl_PureJava;

import bgs.geophys.library.Data.ImagCDF.IMCDFException;
import bgs.geophys.library.Data.ImagCDF.IMCDFPrintEnum;
import gov.nasa.gsfc.spdf.cdfj.CDFDataType;
import gov.nasa.gsfc.spdf.cdfj.CDFWriter;
import gov.nasa.gsfc.spdf.cdfj.CDFException;
import gov.nasa.gsfc.spdf.cdfj.TimeUtil;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class ImagCDFLowLevelWriter_PureJava 
{
    /** an enumeration listing types of variable that can be created */
    public enum CDFVariableType {Double, TT2000}
    
    // private member data for this class
    private CDFWriter cdf_writer;
    private boolean force_create;
    private boolean compress;
    private String cdf_filename;

    
    /** ------------------------------------------------------------------------
     *  --------------------- Opening and closing CDF files --------------------
     *  ------------------------------------------------------------------------*/
    
    /** open a CDF for writing
     * @param filename the file to open
     * @param force_create if true, overwrite existing file
     * @param compress whether to compress variables in the file */
    public ImagCDFLowLevelWriter_PureJava (String filename, boolean force_create, boolean compress)
    {
        this.compress = compress;
        this.force_create = force_create;
        this.cdf_filename = filename;
        
        cdf_writer = new CDFWriter (true);
    }

    /** open a CDF for reading or writing
     * @param file the file to open
     * @param force_create if true, overwrite existing file
     * @param compress whether to compress variables in the file */
    public ImagCDFLowLevelWriter_PureJava (File file, boolean force_create, boolean compress)
    {
        this (file.getAbsolutePath(), force_create, compress);
    }
    
    /** close a CDF - you MUST call this after writing to the CDF otherwise the
     *  CDF file won't be written
     * @throws IMCDFException if there was an error with the CDF */
    public void close ()
    throws IMCDFException
    {
        try
        {
            if (! cdf_writer.write(cdf_filename, force_create))
                throw new IMCDFException ("Error writing CDF");
        }
        catch (IOException e)
        {
            throw new IMCDFException ("IO Error", e);
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
                if (mandatory) throw new IMCDFException ("Missing value for mandatory attribute " + name);
            }
            else if (value.length() <= 0)
            {
                if (mandatory) throw new IMCDFException ("Missing value for mandatory attribute " + name);
            }
            else
                cdf_writer.addGlobalAttributeEntry (name, CDFDataType.CHAR, value);
        }
        catch (CDFException.WriterError e)
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
                if (mandatory) throw new IMCDFException ("Missing value for mandatory attribute " + name);
            }
            else
                cdf_writer.addGlobalAttributeEntry (name, CDFDataType.CHAR, value.toString());
        }
        catch (CDFException.WriterError e)
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
                if (mandatory) throw new IMCDFException ("Missing value for mandatory attribute " + name);
            }
            else
                cdf_writer.addGlobalAttributeEntry (name, CDFDataType.DOUBLE, new double [] {value});
        }
        catch (CDFException.WriterError e)
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
                if (mandatory) throw new IMCDFException ("Missing value for mandatory attribute " + name);
            }
            else
                cdf_writer.addGlobalAttributeEntry (name, CDFDataType.TT2000, new long [] {ImagCDFLowLevelWriter_PureJava.DateToTT2000(value)});
        }
        catch (CDFException.WriterError e)
        {
            throw new IMCDFException (e);
        }
    }
    
    /** add a variable attribute to the CDF file and make an entry in it
     * @param name the attribute name
     * @param var_name the name of the variable that this entry applies to
     * @param value the contents of the entry
     * @throws IMCDFException if there is an error */
    public void addVariableAttribute (String name, String var_name, String value)
    throws IMCDFException
    {
        try
        {
            cdf_writer.addVariableAttributeEntry(var_name, name, CDFDataType.CHAR, value);
        }
        catch (CDFException.WriterError e)
        {
            throw new IMCDFException (e);
        }
    }
    
    /** add a variable attribute to the CDF file and make an entry in it
     * @param name the attribute name
     * @param var_name the name of the variable that this entry applies to
     * @param value the contents of the entry
     * @throws IMCDFException if there is an error */
    public void addVariableAttribute (String name, String var_name, double value)
    throws IMCDFException
    {
        try
        {
            cdf_writer.addVariableAttributeEntry(var_name, name, CDFDataType.DOUBLE, new double [] {value});
        }
        catch (CDFException.WriterError e)
        {
            throw new IMCDFException (e);
        }
    }
    
    /** create a 0 dimensional data array in the CDF file
     * @param var_name the name of the variable
     * @param var_type the type of variable to create (Double or Long))
     * @param missing_val the value to use as a missing value
     *                    for var_type of Double, this should be a double
     *                    for var_type of TT2000, this should be a long
     * @throws IMCDFException if there is an error */
    public void createDataVariable (String var_name, CDFVariableType var_type, Object missing_val)
    throws IMCDFException
    {
        try
        {
            switch (var_type)
            {
                case Double:
                    cdf_writer.defineVariable (var_name, CDFDataType.DOUBLE, new int [] {}, new boolean [] {}, true, compress, new double [] {(double) missing_val});
                    break;
                case TT2000:
                    cdf_writer.defineVariable (var_name, CDFDataType.TT2000, new int [] {}, new boolean [] {}, true, compress, new long [] {(long) missing_val});
                    break;
                default:
                    throw new IMCDFException ("Unrecongnised data type");
            }
        }
        catch (CDFException.WriterError e)
        {
            throw new IMCDFException (e);
        }
    }
       
    /** put a data sample into a record in the CDF file 
     * @param var_name the name of the variable that this entry applies to
     * @param rec_no the record to write to
     * @param data the data to write
     * @throws IMCDFException if there is an error */
    public void addData (String var_name, int rec_no, double data)
    throws IMCDFException
    {
        try
        {
            int record_limits [] = new int [] {rec_no, rec_no};
            cdf_writer.addData (var_name, data, record_limits);
        }
        catch (CDFException.WriterError e)
        {
            throw new IMCDFException (e);
        }
    }
    
    /** put multiple data samples into consecutive records in the CDF file 
     * @param var_name the name of the variable that this entry applies to
     * @param rec_no the 1st record to write to
     * @param data the data to write
     * @throws IMCDFException if there is an error */
    public void addData (String var_name, int rec_no, double data [])
    throws IMCDFException
    {
        try
        {
            int record_limits [] = new int [] {rec_no, rec_no + data.length -1};
            cdf_writer.addData (var_name, data, record_limits);
        }
        catch (CDFException.WriterError e)
        {
            throw new IMCDFException (e);
        }
    }
    
    /** put multiple data samples into consecutive records in the CDF file 
     * @param var_name the name of the variable that this entry applies to
     * @param rec_no the 1st record to write to
     * @param data the data to write
     * @param data_offset offset in the data array to the first data point to write
     * @param data_length number of samples to write
     * @throws IMCDFException if there is an error */
    public void addData (String var_name, int rec_no, double data [], int data_offset, int data_length)
    throws IMCDFException
    {
        double data_slice [];
        if (data_offset != 0 || data_length != data.length)
        {
            // we need to slice the data - I can't see a way to do this with the Java CDF library
            data_slice = new double [data_length];
            System.arraycopy(data, data_offset, data_slice, 0, data_length);
        }
        else data_slice = data;
        addData (var_name, rec_no, data_slice);
    }
    
    /** put a time stamp into a record in the CDF file 
     * @param var_name the name of the variable that this entry applies to
     * @param rec_no the record to write to
     * @param data the data to write
     * @throws IMCDFException if there is an error */
    public void addTimeStamp (String var_name, int rec_no, long data)
    throws IMCDFException
    {
        try
        {
            int record_limits [] = new int [] {rec_no, rec_no};
            cdf_writer.addData (var_name, data, record_limits);
        }
        catch (CDFException.WriterError e)
        {
            throw new IMCDFException (e);
        }
    }

    /** put multiple time stamps into consecutive records in the CDF file 
     * @param var_name the name of the variable that this entry applies to
     * @param rec_no the 1st record to write to
     * @param data the data to write
     * @throws IMCDFException if there is an error */
    public void addTimeStamp (String var_name, int rec_no, long data [])
    throws IMCDFException
    {
        try
        {
            int record_limits [] = new int [] {rec_no, rec_no + data.length -1};
            cdf_writer.addData (var_name, data, record_limits);
        }
        catch (CDFException.WriterError e)
        {
            throw new IMCDFException (e);
        }
    }
    
    /** put multiple time stamps into consecutive records in the CDF file 
     * @param var_name the name of the variable that this entry applies to
     * @param rec_no the 1st record to write to
     * @param data the data to write
     * @param data_offset offset in the data array to the first data point to write
     * @param data_length number of samples to write
     * @throws IMCDFException if there is an error */
    public void addTimeStamp (String var_name, int rec_no, long data [], int data_offset, int data_length)
    throws IMCDFException
    {
        long data_slice [];
        if (data_offset != 0 || data_length != data.length)
        {
            // we need to slice the data - I can't see a way to do this with the Java CDF library
            data_slice = new long [data_length];
            System.arraycopy(data, data_offset, data_slice, 0, data_length);
        }
        else data_slice = data;
        addTimeStamp (var_name, rec_no, data_slice);
    }

    
    
    /** ------------------------------------------------------------------------
     *  ---------------------------- Useful utilities --------------------------
     *  ------------------------------------------------------------------------*/

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
    throws IMCDFException
    {
        try {
            return TimeUtil.tt2000(date);
        } catch (Throwable ex) {
            throw new IMCDFException (ex.getMessage());
        }
    }
    
    public static long DateToTT2000 (long date)
    throws IMCDFException
    {
        try {
            return TimeUtil.tt2000(date);
        } catch (Throwable ex) {
            throw new IMCDFException (ex.getMessage());
        }
    }    
}
