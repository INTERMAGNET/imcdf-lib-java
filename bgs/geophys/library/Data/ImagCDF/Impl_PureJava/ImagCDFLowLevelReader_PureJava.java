/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF.Impl_PureJava;

import bgs.geophys.library.Data.ImagCDF.IMCDFException;
import bgs.geophys.library.Data.ImagCDF.ImagCDFFactory;
import gov.nasa.gsfc.spdf.cdfj.AttributeEntry;
import gov.nasa.gsfc.spdf.cdfj.CDFReader;
import gov.nasa.gsfc.spdf.cdfj.CDFException;
import static gov.nasa.gsfc.spdf.cdfj.TimeUtil.TT_JANUARY_1_1970;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * Error handling strategy: errors in individual reading routines are
 * add to a list, which allows multiple errors to be recorded for a single
 * file (rather than just the first error found). Once reading has completed,
 * call getAccumulatedErrors() to get the list of errors and check whether it
 * is empty of not.
 * 
 * @author smf
 */
public class ImagCDFLowLevelReader_PureJava 
{
    // private member data for this class
    private CDFReader cdf_reader;
    private List<String> accumulated_errors;

    // static initialisers - creation of formatting objects
    private static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("gmt");
    
    
    /** ------------------------------------------------------------------------
     *  --------------------- Opening and closing CDF files --------------------
     *  ------------------------------------------------------------------------*/
    
    /** open a CDF for reading
     * @param filename the file to open
     * @throws IMCDFException if there is an error */
    public ImagCDFLowLevelReader_PureJava (String filename)
    throws IMCDFException
    {
        try
        {
            cdf_reader = new CDFReader (filename);
        }
        catch (CDFException.ReaderError e)
        {
            throw new IMCDFException (e);
        }
        accumulated_errors = new ArrayList<> ();
    }

    /** open a CDF for reading
     * @param file the file to open
     * @throws IMCDFException if there is an error */
    public ImagCDFLowLevelReader_PureJava (File file)
    throws IMCDFException
    {
        this (file.getAbsolutePath());
    }
    
    /** close a CDF - this doesn't do anything for a reader */
    public void close ()
    {
        /* intentionally empty */ 
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
        try
        {
            Vector<AttributeEntry> entries = cdf_reader.getAttributeEntries (name);
            AttributeEntry entry = null;
            if (entry_no < entries.size())
                entry = entries.get(entry_no);
            if (entry == null) 
            {
                if (mandatory)
                    accumulated_errors.add ("Missing global attribute name/entry: " + name + "/" + Integer.toString (entry_no));
                return null;
            }
            if (! name.equals(entry.getAttributeName())) 
            {
                accumulated_errors.add ("Incorrect attribute entry name:" + name + " / " + entry.getAttributeName());
                return null;
            }
            if (entry.getValue() == null)
            {
                accumulated_errors.add ("Global attribute \"" + name + "\" missing entry number " + Integer.toString (entry_no));
                return null;
            }
            if (entry.getValue() instanceof String)
                return (String) entry.getValue();
            accumulated_errors.add ("Incorrect global attribute \"" + name + "\", data type should be String");
            return null;
        }
        catch (CDFException.ReaderError e)
        {
            if (mandatory)
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
    public Double getGlobalAttributeDouble (String name, int entry_no, boolean mandatory)
    {
        try
        {
            Vector<AttributeEntry> entries = cdf_reader.getAttributeEntries (name);
            AttributeEntry entry = null;
            if (entry_no < entries.size())
                entry = entries.get(entry_no);
            if (entry == null) 
            {
                if (mandatory)
                    accumulated_errors.add ("Missing global attribute name/entry: " + name + "/" + Integer.toString (entry_no));
                return null;
            }
            if (! name.equals(entry.getAttributeName()))
            {
                accumulated_errors.add ("Incorrect attribute entry name:" + name + " / " + entry.getAttributeName());
                return null;
            }
            if (entry.getValue() == null)
            {
                accumulated_errors.add ("Global attribute \"" + name + "\" missing entry number " + Integer.toString (entry_no));
                return null;
            }
            if (entry.getValue() instanceof double [])
                return ((double []) entry.getValue()) [0];
            accumulated_errors.add ("Incorrect global attribute \"" + name + "\", data type should be Double");
            return null;
        }
        catch (CDFException.ReaderError e)
        {
            if (mandatory)
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
        try
        {
            Vector<AttributeEntry> entries = cdf_reader.getAttributeEntries (name);
            AttributeEntry entry = null;
            if (entry_no < entries.size())
                entry = entries.get(entry_no);
            if (entry == null) 
            {
                if (mandatory)
                    accumulated_errors.add ("Missing global attribute name/entry: " + name + "/" + Integer.toString (entry_no));
                return null;
            }
            if (! name.equals(entry.getAttributeName()))
            {
                accumulated_errors.add ("Incorrect attribute entry name:" + name + " / " + entry.getAttributeName());
                return null;
            }
            if (entry.getValue() == null)
            {
                accumulated_errors.add ("Global attribute \"" + name + "\" missing entry number " + Integer.toString (entry_no));
                return null;
            }
            if (entry.getValue() instanceof String)
            {
                String string = ((String) entry.getValue()).replace('T', ' ').replaceAll("Z", "");
                SimpleDateFormat formatter = new SimpleDateFormat ("YYYY-mm-dd hh:mm:ss");
                ImagCDFFactory.fixSimpleDateFormat(formatter);
                try 
                { 
                    return formatter.parse (string); 
                }
                catch (ParseException e) 
                {
                    accumulated_errors.add ("Incorrect global attribute data \"" + name + "\", contents should be ISO 8601 format string");
                    return null;
                }
            }
            try
            {
                if (entry.getValue() instanceof long []) 
                {
                    Date ret_val = TT2000ToDate (((long []) entry.getValue())[0]);
                    return ret_val;
                }
            }
            catch (IMCDFException e)
            {
                process_error ("Error decoding global attribute \"" + name + "\"", e);
                return null;
            }
            accumulated_errors.add ("Incorrect global attribute \"" + name + "\", data type should be Long/TT2000 or ISO 8601 format string");
            return null;
        }
        catch (CDFException.ReaderError e)
        {
            if (mandatory)
                process_error ("Error reading global attribute name/entry: " + name + "/" + Integer.toString (entry_no), e);
            return null;
        }
    }
    
    /** get the contents of a variable attribute
     * @param name the name of the attribute
     * @param var_name the name of the variable that this entry applies to
     * @return the value of the attribute's entry */
    public String getVariableAttributeString (String name, String var_name)
    {
        try
        {
            Vector<AttributeEntry> var_entries = cdf_reader.getAttributeEntries(var_name, name);
            if (var_entries == null)
                return null;
            AttributeEntry var_entry = var_entries.get(0);
            if (var_entry.getValue() instanceof String) 
                return (String) var_entry.getValue();
            accumulated_errors.add ("Incorrect variable \"" + var_name + "\", attribute \"" + name + "\", data type (should be String)");
            return null;
        }
        catch (CDFException.ReaderError e)
        {
            process_error ("Error reading variable \"" + var_name + "\", attribute \"" + name + "\"", e);
            return null;
        }
    }
    
    /** get the contents of a variable attribute
     * @param name the name of the attribute
     * @param var_name the name of the variable that this entry applies to
     * @return the value of the attribute's entry */
    public Double getVariableAttributeDouble (String name, String var_name)
    {
        try
        {
            Vector<AttributeEntry> var_entries = cdf_reader.getAttributeEntries(var_name, name);
            if (var_entries == null)
                return null;
            AttributeEntry var_entry = var_entries.get(0);
            if (var_entry.getValue() instanceof double []) 
                return ((double []) var_entry.getValue()) [0];
            accumulated_errors.add ("Incorrect variable \"" + var_name + "\", attribute \"" + name + "\", data type (should be Double)");
            return null;
        }
        catch (CDFException.ReaderError e)
        {
            process_error ("Error reading variable \"" + var_name + "\", attribute \"" + name + "\"", e);
            return null;
        }
    }
    
    /** does the given variable exist in the CDF file
     * @param name the name of the variable to test for
     * @return true if it exists, false otherwise */
    public boolean isVariableExist (String name)
    {
        return cdf_reader.existsVariable(name);
    }
    
    /** get data from a variable
     * @param var_name the name of the variable 
     * @return the data */
    public double [] getDataArray (String var_name)
    {
        try
        {
            Object data = cdf_reader.get(var_name);
            if (data == null) {
                accumulated_errors.add ("Missing data: " + var_name);
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
            accumulated_errors.add ("Unexpected data type in data array: " + var_name);
            return null;
        }
        catch (CDFException.ReaderError e)
        {
            process_error ("Error reading data variable " + var_name, e);
            return null;
        }
    }
    
    /** get TT2000 time stamps from a variable
     * @param var_name the name of the variable 
     * @return the data */
    public long [] getTimeStampArray (String var_name)
    {
        try
        {
            Object data = cdf_reader.get(var_name);
            if (data == null) {
                accumulated_errors.add ("Missing time stamp: " + var_name);
                return null;
            }
            if (data instanceof long [])
                return (long []) data;
            accumulated_errors.add ("Unexpected data type in time stamp array: " + var_name);
            return null;
        }
        catch (CDFException.ReaderError e)
        {
            process_error ("Error reading time stamp variable " + var_name, e);
            return null;
        }
    }
    
    /** get the list of accumulated errors
     * @return the list */
    public List<String> getAccumulatedErrors ()
    {
        return accumulated_errors;
    }
    
    /** ------------------------------------------------------------------------
     *  ---------------------------- Useful utilities --------------------------
     *  ------------------------------------------------------------------------*/

    /** create a Java Date from a CDF TT2000 date
     * @param tt2000 the CDF TT2000 date (8 byte integer)
     * @return the Java Date
     * @throws IMCDFException  */
    public static Date TT2000ToDate (long tt2000)
    throws IMCDFException
    {
        // I can't find code to do this in NASA's CDF library, so have adapted
        // code from the library that converts in the opposite direction
        // get the date, ignoring leap seconds
        Date date = new Date ((tt2000 - TT_JANUARY_1_1970) / 1000000);
        // now convert back to TT2000 to find the number of leap seconds
        long diff = ImagCDFLowLevelWriter_PureJava.DateToTT2000(date) - tt2000;
        date = new Date (date.getTime() - (diff / 1000000));
        return date;
    }
    
    public void process_error (String msg, Exception e)
    {
        if (e.getMessage() != null)
            msg += ": " + e.getMessage();
        accumulated_errors.add (msg);
    }
}
