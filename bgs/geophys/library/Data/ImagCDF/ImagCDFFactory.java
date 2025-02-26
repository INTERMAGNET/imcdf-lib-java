/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import bgs.geophys.library.Data.ImagCDF.Impl_JNI.ImagCDFInfo_JNI;
import bgs.geophys.library.Data.ImagCDF.Impl_JNI.ImagCDFLowLevel_JNI;
import bgs.geophys.library.Data.ImagCDF.Impl_JNI.ImagCDFVariableTS_JNI;
import bgs.geophys.library.Data.ImagCDF.Impl_JNI.ImagCDFVariable_JNI;
import bgs.geophys.library.Data.ImagCDF.Impl_JNI.ImagCDF_JNI;
import bgs.geophys.library.Data.ImagCDF.Impl_PureJava.ImagCDFInfo_PureJava;
import bgs.geophys.library.Data.ImagCDF.Impl_PureJava.ImagCDFVariableTS_PureJava;
import bgs.geophys.library.Data.ImagCDF.Impl_PureJava.ImagCDFVariable_PureJava;
import bgs.geophys.library.Data.ImagCDF.Impl_PureJava.ImagCDF_PureJava;
import bgs.geophys.library.Misc.DateUtils;
import gov.nasa.gsfc.spdf.cdfj.CDR;
import gov.nasa.gsfc.spdf.cdfj.TimeUtil;
import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.util.CDFTT2000;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * A factory to create ImagCDF data, either creating ImagCDF files from data
 * in memory, or reading ImagCDF files into memory
 * 
 * Two implementations of NASA's CDF library can be used:
 * - A pure java library is easy to use, but doesn't have an external leap
 *   seconds table and so will go out of date (it also uses static data
 *   extensively and may be problematic in a multi-threaded application)
 * - A JNI library, which requires CDF to be installed correctly before
 *   an application using the library can run
 * By default the pure java version of the library is used. The library to be
 * used can be selected with the IMAG_CDF_LIBRARY system property or environment
 * variable (the system property takes precedence). Set the property / variable
 * to:
 * "JNI" to only use the JNI library (fails if the library is not available)
 * "PURE_JAVA" to use the pure Java library (won't fail, as this library is 
 *             statically linked)
 * "JNI,PURE_JAVA" to use the JNI library if available, otherwise use the
 *                 pure Java library (this is the default)
 * 
 * The code to initialise the correct library also checks that the leap
 * second table is up to date. This code will need updating if a leap
 * second is inserted (and a new CDF leap second table produced) after 2016.
 * 
 * @author smf
 */
public class ImagCDFFactory {
    
    /** an enumeration that lists the libraries that clients can use */
    public enum ImagCDFLibraryType {
        /** the Java Native Interface library - this requires the CDF software to be installed */
        JNI, 
        /** a pure java CDF library that does not require the CDF software to be installed */
        PURE_JAVA
    }

    /** a class used to hold information about the CDF library that 
     * has been initialised */
    public static class CDFLibraryInfo {
        private final String requested_library;
        private ImagCDFLibraryType library_type;
        private final String library_version;
        private final List<String> init_errors;
        private final List<String> init_warnings;
        public CDFLibraryInfo (String requested_library, ImagCDFLibraryType library_type,
                               List<String> init_errors, List<String> init_warnings) {
            this.requested_library = requested_library;
            this.library_type = library_type;
            this.init_errors = init_errors;
            this.init_warnings = init_warnings;

            String ver = null;
            if (library_type != null) {
                if (library_type == ImagCDFLibraryType.JNI) {
                    try {
                        ver = CDF.getLibraryVersion();
                    } catch (CDFException e) {
                        ver = null;
                        this.init_errors.add ("Unable to determine CDF version");
                    }
                } else {
                    ver = CDR.getLibraryVersion ();
                }
            }
            library_version = ver;
        }
        public String getRequestedLibrary () { return requested_library; }
        public ImagCDFLibraryType getLibraryType () { return library_type; }
        public String getLibraryVersion () { return library_version; }
        public int getNWarnings () { return init_warnings.size(); }
        public String getInitWarning (int index, int max_len) { return truncate (init_warnings.get (index), max_len); }
        public int getNErrors () { return init_errors.size(); }
        public String getInitError (int index, int max_len) { return truncate (init_errors.get (index), max_len); }
        private String truncate (String s, int max_len) {
            if (max_len < 0) return s;
            // allow for elipses to be added to a truncated string
            boolean add_elipses = true;
            if (max_len < 5) add_elipses = false;
            else max_len -= 3;
            // check if string is too short to be truncated
            if (s.length() < max_len) return s;
            // truncate the string
            String truncated = s.substring (0, max_len);
            if (add_elipses) truncated += "...";
            return truncated;
        }
    }
    
    /* details of the CDF library initialisation process */
    private static final CDFLibraryInfo cdf_library_info;
    
    static
    {
        /* start with no errors or warnings during initialisation */
        List<String> init_errors = new ArrayList<> ();
        List<String> init_warnings = new ArrayList<> ();
        
        /* work out which library (if any) the user asked for */
        String lib_name = System.getProperty("IMAG_CDF_LIBRARY");
        String requested_lib_name;
        if (lib_name == null)
            lib_name = System.getenv("IMAG_CDF_LIBRARY");
        if (lib_name == null) {
            lib_name="JNI,PURE_JAVA";
            requested_lib_name = "Default (" + lib_name + ")";
        } else {
            requested_lib_name = lib_name;
        }
        
        /* are we trying to use the JNI library */
        ImagCDFLibraryType library_type = null;
        if (lib_name.equalsIgnoreCase("JNI") || lib_name.equalsIgnoreCase("JNI,PURE_JAVA")) {
            String errmsg = ImagCDFLowLevel_JNI.checkNativeLib("", true);
            if (errmsg == null) {
                library_type = ImagCDFLibraryType.JNI;
            } else {
                if (lib_name.equalsIgnoreCase("JNI"))
                    init_errors.add (errmsg);
                else
                    init_warnings.add (errmsg);
            }
        }
        
        /* are we trying to use the Pure Java library */
        if ((library_type == null) && 
            (lib_name.equalsIgnoreCase("PURE_JAVA") || lib_name.equalsIgnoreCase("JNI,PURE_JAVA"))) {
            library_type = ImagCDFLibraryType.PURE_JAVA;
        }
        
        /* check that library was initialised or there was an error - if neither of these 
         * is true, then the environment variable IMAG_CDF_LIBRARY was incorrectly set */
        if (library_type == null) {
            if (init_errors.size() <= 0)
                init_errors.add ("System Property or Environment Variable IMAG_CDF_LIBRARY must be set to one of 'JNI', 'PURE_JAVA' or 'JNI,PURE_JAVA'");
        } else {
            /* check that the leap seconds table is up to date - this code is correct for leap seconds
             * up to the end of 2016. If leap seconds are inserted after this date, this code will need 
             * updating (change the value that "expected_last_leap_sec" is set to - remember that
             * the month field is 0 based (January = 0) */
            GregorianCalendar last_leap_sec = new GregorianCalendar (DateUtils.gmtTimeZone);
            switch (library_type) {
                case JNI:
                    long date_fields [] = CDFTT2000.CDFgetLastDateinLeapSecondsTable();
                    last_leap_sec.set ((int) date_fields[0], (int) date_fields[1] -1, (int) date_fields[2], 0, 0, 0);
                    last_leap_sec.set (GregorianCalendar.MILLISECOND, 0);
                    break;
                case PURE_JAVA:
                    last_leap_sec.setTime(TimeUtil.CDFgetLastDateinLeapSecondsTable ());
                    break;
            }
            GregorianCalendar expected_last_leap_sec = new GregorianCalendar (DateUtils.gmtTimeZone);
            expected_last_leap_sec.set (2016, 11, 31, 0, 0, 0);
            expected_last_leap_sec.set (GregorianCalendar.MILLISECOND, 0);
            if (last_leap_sec.getTimeInMillis() < expected_last_leap_sec.getTimeInMillis()) {
                String msg = String.format ("CDF leap second table's last leap second is at %04d-%02d-%02d, but should be at %04d-%02d-%02d",
                                            last_leap_sec.get (GregorianCalendar.YEAR),
                                            last_leap_sec.get (GregorianCalendar.MONTH) +1,
                                            last_leap_sec.get (GregorianCalendar.DAY_OF_MONTH),
                                            expected_last_leap_sec.get (GregorianCalendar.YEAR),
                                            expected_last_leap_sec.get (GregorianCalendar.MONTH) +1,
                                            expected_last_leap_sec.get (GregorianCalendar.DAY_OF_MONTH));
                init_warnings.add (msg);
            }
        }
        
        // set up the CDF information object that will allow users to understand what happened during initialisation
        cdf_library_info = new CDFLibraryInfo (requested_lib_name, library_type, init_errors, init_warnings);
    }
    
    /** get information about the library initialisation process */
    public static CDFLibraryInfo getCDFLibraryInfo () { return cdf_library_info; }
    
    /** YOU PROBABLY DON'T WANT TO USE THIS METHOD. It's main
     * purpose is to force the CDF library used by the ImagCDF
     * routines during testing. If you do use it, make sure to
     * call it before any other ImagCDF routines.
     * @param library_type JNI or Pure Java
     */
    public static void forceUseLibrary (ImagCDFLibraryType library_type)
    {
        cdf_library_info.library_type = library_type;
    }

    /** check for problems accessing the CDF libraries.
     * @return an error message or NULL if there isn't a problem
     */
    public static String checkCDFLibraryAccess ()
    {
        String ret_val = null;
        if (cdf_library_info.getLibraryType() == ImagCDFLibraryType.JNI)
        {
            ret_val = ImagCDFLowLevel_JNI.checkNativeLib(null);
        }
        return ret_val;
    }
    
    /** read an ImagCDF file
     * @param file the CDF file
     * @return the contents of the CDF file
     * @throws IMCDFException if there is an error */
    public static ImagCDF readImagCDF (File file)
    throws IMCDFException
    {
        return readImagCDF (file, false);
    }
    
    /** read an ImagCDF file
     * @param file the CDF file
     * @param headersOnly only read the global attributes, not the data or variable attributes
     * @return the contents of the CDF file
     * @throws IMCDFException if there is an error */
    public static ImagCDF readImagCDF (File file, boolean headersOnly)
    throws IMCDFException
    {
        checkInitErrors ();
        if (cdf_library_info.getLibraryType() == ImagCDFLibraryType.JNI)
            return new ImagCDF_JNI (file, headersOnly);
        else
            return new ImagCDF_PureJava (file, headersOnly);
    }
    
    /** create an ImagCDF object from data and metadata (prior to writing to a file)
     * @param iaga_code IAGA code of the observatory
     * @param pub_level the amount of editing done on the data
     * @param pub_date the date the data was published
     * @param observatory_name Full name of the observatory
     * @param latitude Geographic latitude of the observing position
     * @param longitude Geographic longitude of the observing position
     * @param elevation Height of the observing position above sea level
     * @param institution name of the institution
     * @param vector_sens_orient the orientation of the vector sensor (which may differ 
     *                           from that of the elements reported in the data)
     * @param standard_level describes whether the data conforms to a standard
     * @param standard_name name for the standard
     * @param standard_version version of the standard
     * @param partial_stand_desc description of the parts of the standard that are applicable to this data
     * @param source set to one of "institute", "intermagnet" or "wdc"
     * @param unique_identifier an identifier such as a DOI
     * @param parent_identifiers the unique identifiers of any parent data sets
     * @param reference_links URLs of relevance, e.g. www.intermagnet.org
     * @param elements the geomagnetic data - depend_0 values must be set to the correct time stamp name
     * @param temperatures the temperature data (may be null) - depend_0 values must be set to the correct time stamp name
     * @param time_stamps the time stamps for the elements and temperature - there must be at least
     *                    one entry for every unique "depend_0" entry in the elements and temperatures
     * @return the CDF data ready for writing
     * @throws IMCDFException if the vector data elements don't have the same sample period or start date */
    public static ImagCDF createImagCDF (String iaga_code, IMCDFPublicationLevel pub_level,
                                         Date pub_date, String observatory_name, 
                                         double latitude, double longitude, double elevation, 
                                         String institution, String vector_sens_orient,
                                         IMCDFStandardLevel standard_level, IMCDFStandardName standard_name,
                                         String standard_version, String partial_stand_desc,
                                         String source, String unique_identifier, String parent_identifiers [],
                                         URL reference_links [], ImagCDFVariable elements [], 
                                         ImagCDFVariable temperatures [], ImagCDFVariableTS time_stamps [])
    throws IMCDFException
    {
        checkInitErrors ();
        if (cdf_library_info.getLibraryType() == ImagCDFLibraryType.JNI)
            return new ImagCDF_JNI (iaga_code, pub_level, pub_date, observatory_name, 
                                    latitude, longitude, elevation, institution, vector_sens_orient,
                                    standard_level, standard_name, standard_version, partial_stand_desc,
                                    source, unique_identifier, parent_identifiers, reference_links,
                                    elements, temperatures, time_stamps);
        else
            return new ImagCDF_PureJava (iaga_code, pub_level, pub_date, observatory_name, 
                                         latitude, longitude, elevation, institution, vector_sens_orient,
                                         standard_level, standard_name, standard_version, partial_stand_desc,
                                         source, unique_identifier, parent_identifiers, reference_links,
                                         elements, temperatures, time_stamps);
    }
    
    /** create an ImagCDFVariable from data and metadata for subsequent writing to a file
     * @param variable_type the type of variable - geomagnetic element or temperature
     * @param field_nam set the "Geomagnetic Field Element " and a number or
     *                  "Temperature" and a number
     * @param valid_min the smallest possible value that the data can take
     * @param valid_max the largest possible value that the data can take
     * @param units name of the units that the data is in
     * @param fill_val the value that, when present in the data, shows that the data point was not recorded
     * @param depend_0 the name of the time stamp variable in the CDF file for this data variable
     * @param elem_rec for geomagnetic data the element that this data represents.
     *                 for temperature data the name of the location where temperature was recorded
     * @param data the data as an array
     * @return the new CDF variable
     * @throws IMCDFException if the elem_rec or samp_per are invalid */
    public static ImagCDFVariable createImagCDFVariable (IMCDFVariableType variable_type, String field_nam,
                                                         double valid_min, double valid_max,
                                                         String units, double fill_val, String depend_0,
                                                         String elem_rec, double data [])
    throws IMCDFException
    {
        if (cdf_library_info.getLibraryType() == ImagCDFLibraryType.JNI)
            return new ImagCDFVariable_JNI (variable_type, field_nam, valid_min, valid_max,
                                            units, fill_val, depend_0, elem_rec, data);
        else      
            return new ImagCDFVariable_PureJava (variable_type, field_nam, valid_min, valid_max,
                                                 units, fill_val, depend_0, elem_rec, data);
    }    

    /** create an ImagCDFVariable from data and metadata for subsequent writing to a file
     * @param variable_type the type of variable - geomagnetic element or temperature
     * @param field_nam set the "Geomagnetic Field Element " and a number or
     *                  "Temperature" and a number
     * @param valid_min the smallest possible value that the data can take
     * @param valid_max the largest possible value that the data can take
     * @param units name of the units that the data is in
     * @param fill_val the value that, when present in the data, shows that the data point was not recorded
     * @param depend_0 the name of the time stamp variable in the CDF file for this data variable
     * @param elem_rec for geomagnetic data the element that this data represents.
     *                 for temperature data the name of the location where temperature was recorded
     * @param data the data as an array
     * @param data_offset index into the data array to start writing from
     * @param data_length number of samples of data to write
     * @return the new CDF variable
     * @throws IMCDFException if the elem_rec or samp_per are invalid */
    public static ImagCDFVariable createImagCDFVariable (IMCDFVariableType variable_type, String field_nam,
                                                         double valid_min, double valid_max,
                                                         String units, double fill_val, String depend_0,
                                                         String elem_rec, double data [], int data_offset, int data_length)
    throws IMCDFException
    {
        checkInitErrors ();
        if (cdf_library_info.getLibraryType() == ImagCDFLibraryType.JNI)
            return new ImagCDFVariable_JNI (variable_type, field_nam, valid_min, valid_max,
                                            units, fill_val, depend_0, elem_rec, data,
                                            data_offset, data_length);
        else      
            return new ImagCDFVariable_PureJava (variable_type, field_nam, valid_min, valid_max,
                                                 units, fill_val, depend_0, elem_rec, data,
                                                 data_offset, data_length);
    }    
    
    /** create an ImagCDF variable time stamp series from a start date, sample period
     * and duration
     * @param start_date the date/time stamp of the first sample
     * @param samp_per the sample period (time between samples) in seconds
     * @param n_samples the number of samples
     * @param var_name the CDF variable name
     * @return the new CDF variable
     * @throws IMCDFException if there was an error with the CDF */
    public static ImagCDFVariableTS createImagCDFVariableTS (Date start_date, double samp_per, int n_samples, String var_name)
    throws IMCDFException
    {
        checkInitErrors ();
        if (cdf_library_info.getLibraryType() == ImagCDFLibraryType.JNI)
            return new ImagCDFVariableTS_JNI (start_date, samp_per, n_samples, var_name);
        else
            return new ImagCDFVariableTS_PureJava (start_date, samp_per, n_samples, var_name);
    }
    
    /** create an ImagCDF variable time stamp series from a set of dates
     * @param dates an array of dates corresponding to each time stamp
     * @param var_name the CDF variable name
     * @return the new CDF variable
     * @throws IMCDFException if there was an error with the CDF */
    public static ImagCDFVariableTS createImagCDFVariableTS (Date dates [], String var_name)
    throws IMCDFException
    {
        checkInitErrors ();
        if (cdf_library_info.getLibraryType() == ImagCDFLibraryType.JNI)
            return new ImagCDFVariableTS_JNI (dates, var_name);
        else
            return new ImagCDFVariableTS_PureJava (dates, var_name);
    }
    
    /** get information about an Intermagnet CDF file
     * @param file the file to read
     * @return information about the contents of the file
     * @throws IMCDFException if there was an initialisation error */
    public static ImagCDFInfo getImagCDFInfo (File file)
    throws IMCDFException
    {
        checkInitErrors ();
        if (cdf_library_info.getLibraryType() == ImagCDFLibraryType.JNI)
            return new ImagCDFInfo_JNI (file);
        else
            return new ImagCDFInfo_PureJava (file);
    }
    
    /** generate an IMAG CDF filename 
     * @param station_code the IAGA station code
     * @param cadence the sample period of the data
     * @param start_date the start date for the data
     * @param pub_level the publication level
     * @param coverage the coverage for the data
     * @return the file OR null if there is an error */
    public static String makeFilename (String station_code, 
                                       ImagCDFFilename.Interval cadence, Date start_date,
                                       IMCDFPublicationLevel pub_level, ImagCDFFilename.Interval coverage)
    {
        return new ImagCDFFilename (station_code, start_date, pub_level, cadence, coverage, ImagCDFFilename.Case.LOWER).getFilename ();
    }

    /** a text listing of Intermagnet's terms under which a user may use Intermagnet data
     * @return the terms of use */
    public static String getINTERMAGNETTermsOfUse ()
    {
        return "CONDITIONS OF USE FOR DATA PROVIDED THROUGH INTERMAGNET:\n" +
               "The data made available through INTERMAGNET are provided for\n" +
               "your use and are not for commercial use or sale or distribution\n" +
               "to third parties without the written permission of the institute\n" +
               "(http://www.intermagnet.org/Institutes_e.html) operating\n" +
               "the observatory. Publications making use of the data\n" +
               "should include an acknowledgment statement of the form given below.\n" +
               "A citation reference should be sent to the INTERMAGNET Secretary\n" +
               "(secretary@intermagnet.org) for inclusion in a publications list\n" +
               "on the INTERMAGNET website.\n" +
               "\n" +
               "     ACKNOWLEDGEMENT OF DATA FROM OBSERVATORIES\n" +
               "     PARTICIPATING IN INTERMAGNET\n" +
               "We offer two acknowledgement templates. The first is for cases\n" +
               "where data from many observatories have been used and it is not\n" + 
               "practical to list them all, or each of their operating institutes.\n" + 
               "The second is for cases where research results have been produced\n" + 
               "using a smaller set of observatories.\n" +
               "\n" +
               "     Suggested Acknowledgement Text (template 1)\n" +
               "The results presented in this paper rely on data collected\n" + 
               "at magnetic observatories. We thank the national institutes that\n" + 
               "support them and INTERMAGNET for promoting high standards of\n" + 
               "magnetic observatory practice (www.intermagnet.org).\n" +
               "\n" +
               "     Suggested Acknowledgement Text (template 2)\n" +
               "The results presented in this paper rely on the data\n" + 
               "collected at <observatory name>. We thank <institute name>,\n" + 
               "for supporting its operation and INTERMAGNET for promoting high\n" + 
               "standards of magnetic observatory practice (www.intermagnet.org).\n";
    }
    
    /** find the maximum or minimum value that a particular geomagnetic element
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

    private static void checkInitErrors ()
    throws IMCDFException {
        // if there were any initialisation errors (rather than warnings) then we can't continue
        if (cdf_library_info.getNErrors() > 0)
            throw new IMCDFException (cdf_library_info.getInitError(0, -1));
    }
}
