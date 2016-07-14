/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import gsfc.nssdc.cdf.CDFException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * An ImagCDF object contains metadata that applies to an entire CDF file. 
 * It also contains ImagCDFVariable objects
 * 
 * An ImagCDFVariable object contains metadata and data that apply to an individual
 * geomagnetic field element or a temperature time series. It also contains a reference
 * to an ImagCDFVariableTS object
 * 
 * An ImagCDFVariableTS object contains time stamps for the individual data points
 * in an ImagCDFVariable object
 * 
 * @author smf
 */
public class ImagCDF 
implements IMCDFWriteProgressListener
{

    /** an enumeration that defines possible sample period values for the
     * filename */
    public enum FilenameSamplePeriod {ANNUAL, MONTHLY, DAILY, HOURLY, MINUTE, SECOND}

    /** constant value used to indicate missing data */
    public static final double MISSING_DATA_VALUE = 99999.0;

    /** the version of the data format that this software supports */
    public static final String FORMAT_VERSION_SUPPORTED = "1.1";
    
    // a list of listeners who will recieve "percent complete" notification during writing of data
    private List<IMCDFWriteProgressListener> write_progress_listeners;
    
    // an array that holds the number of points in each variable to be written out and a pointer that
    // shows which variable from this array is currently being written - the pointer can be set to
    // both < 0 (before the start of writing) and >= the array length (after the end of writing)
    // n_data_points_total is the total number of data points to write to file
    private int n_samples_per_variable [];
    private int variable_being_written_index;
    private int n_data_points_total;
    
    // private member data - the CDF global attributes
    private String format_description;
    private String format_version;
    private String title;
    private String iaga_code;
    private String elements_recorded;
    private IMCDFPublicationLevel pub_level;
    private Date pub_date;
    private String observatory_name;
    private double latitude;
    private double longitude;
    private double elevation;
    private String institution;
    private String vector_sens_orient;
    private IMCDFStandardLevel standard_level;
    private IMCDFStandardName standard_name;
    private String standard_version;
    private String partial_stand_desc;
    private String source;
    private String terms_of_use;
    private String unique_identifier;
    private String parent_identifiers [];
    private URL reference_links [];

    // private member data - the field element and temperature data arrays and their time stamps
    private ImagCDFVariable elements [];
    private ImagCDFVariableTS vector_time_stamps, scalar_time_stamps;
    private ImagCDFVariable temperatures [];
    private ImagCDFVariableTS temperature_time_stamps [];
    
    /** read an ImagCDF file
     * @param file the CDF file
     * @throws CDFException if there is an error */
    public ImagCDF (File file)
    throws CDFException
    {
        int count, n_elements, n_temperatures;
        boolean need_scalar_ts;
        String string, standard_name_string;
        ImagCDFLowLevel cdf;
        IMCDFVariableType field_var_type, temperature_var_type;
        List<String> links, pids;
        CDFException stored_close_exception;
        
        write_progress_listeners = new ArrayList<> ();
        
        // check that the CDF libraries are available
        string = ImagCDFLowLevel.checkNativeLib("");
        if (string != null) throw new CDFException (string);
        
        // open the CDF file
        cdf = null;
        stored_close_exception = null;
        try
        {
            cdf = new ImagCDFLowLevel (file, ImagCDFLowLevel.CDFOpenType.CDFOpen, ImagCDFLowLevel.CDFCompressType.None);

            // get global metadata
            links = new ArrayList<String> ();
            pids = new ArrayList<String> ();
            format_description =                                  cdf.getGlobalAttributeString("FormatDescription", 0, true);
            format_version =                                      cdf.getGlobalAttributeString("FormatVersion",     0, true);
            title =                                               cdf.getGlobalAttributeString("Title",             0, true);
            iaga_code =                                           cdf.getGlobalAttributeString("IagaCode",          0, true);
            elements_recorded =                                   cdf.getGlobalAttributeString("ElementsRecorded",  0, true);
            pub_level =                new IMCDFPublicationLevel (cdf.getGlobalAttributeString("PublicationLevel",  0, true));
            pub_date =                                            cdf.getGlobalAttributeDate  ("PublicationDate",   0, true);
            observatory_name =                                    cdf.getGlobalAttributeString("ObservatoryName",   0, true);
            latitude =                                            cdf.getGlobalAttributeDouble("Latitude",          0, true);
            longitude =                                           cdf.getGlobalAttributeDouble("Longitude",         0, true);
            elevation =                                           cdf.getGlobalAttributeDouble("Elevation",         0, true);
            institution =                                         cdf.getGlobalAttributeString("Institution",       0, true);
            vector_sens_orient =                                  cdf.getGlobalAttributeString("VectorSensOrient",  0, false);
            standard_level =              new IMCDFStandardLevel (cdf.getGlobalAttributeString("StandardLevel",     0, true));
            standard_name_string =                                cdf.getGlobalAttributeString("StandardName",      0, false);
            standard_version =                                    cdf.getGlobalAttributeString("StandardVersion",   0, false);
            partial_stand_desc =                                  cdf.getGlobalAttributeString("PartialStandDesc",  0, false);
            source =                                              cdf.getGlobalAttributeString("Source",            0, true);
            terms_of_use =                                        cdf.getGlobalAttributeString("TermsOfUse",        0, false);
            unique_identifier =                                   cdf.getGlobalAttributeString("UniqueIdentifier",  0, false);
            if (standard_name_string == null) standard_name = null;
            else standard_name = new IMCDFStandardName (standard_name_string);  
            for (count=0, string=""; string != null; count ++)
            {
                string =                                          cdf.getGlobalAttributeString("ParentIdentifiers", count, false);
                if (string != null) pids.add (string);
            }
            for (count=0, string=""; string != null; count ++)
            {
                string =                                          cdf.getGlobalAttributeString("ReferenceLinks", count, false);
                if (string != null) links.add (string);
            }

            // sort out the array of reference links
            reference_links = new URL [links.size()];
            count = 0;
            try
            {   
                for (String s : links)
                {
                    reference_links [count ++] = new URL (s);
                }
        }
            catch (MalformedURLException ex)
            {
                throw new CDFException ("Badly formed URL in reference link " + (count +1));
            }

            // sort out the array of parent identifiers
            parent_identifiers = new String [links.size()];
            count = 0;
            for (String s : pids)
            {
                parent_identifiers [count ++] = s;
            }
        
            // get geomagnetic field data - find variable names based on elements recorded
            field_var_type = new IMCDFVariableType (IMCDFVariableType.VariableTypeCode.GeomagneticFieldElement);
            n_elements = elements_recorded.length();
            elements = new ImagCDFVariable [n_elements];
            need_scalar_ts = false;
            for (count=0; count<n_elements; count++)
            {
                elements [count] = new ImagCDFVariable(cdf, field_var_type, elements_recorded.substring(count, count +1));
                if (elements[count].isScalarGeomagneticData())
                    need_scalar_ts = true;
            }
            vector_time_stamps = new ImagCDFVariableTS (cdf, IMCDFVariableType.VectorTimeStampsVarName);
            if (need_scalar_ts)
                scalar_time_stamps = new ImagCDFVariableTS (cdf, IMCDFVariableType.ScalarTimeStampsVarName);
        
            // find the number of temperature variables and get temperature data
            temperature_var_type = new IMCDFVariableType (IMCDFVariableType.VariableTypeCode.Temperature);
            n_temperatures = 0;
            while (cdf.isVariableExist (temperature_var_type.getCDFFileVariableName(Integer.toString (n_temperatures +1)))) n_temperatures ++;
            temperatures = new ImagCDFVariable [n_temperatures];
            temperature_time_stamps = new ImagCDFVariableTS [n_temperatures];
            for (count=0; count<n_temperatures; count++)
            {
                temperatures [count] = new ImagCDFVariable(cdf, temperature_var_type, Integer.toString (count +1));
                temperature_time_stamps [count] = new ImagCDFVariableTS (cdf, IMCDFVariableType.getTemperatureTimeStampsVarName (Integer.toString (count +1)));
            }
        }
        finally
        {
            try
            {
                // close the file
                if (cdf != null) cdf.close ();
            }
            catch (CDFException e)
            {
                stored_close_exception = e;
            }
        }
        
        // process any problems when the file was closed
        if (stored_close_exception != null) throw stored_close_exception;
        
        checkMetadata ();
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
     * @param elements the geomagnetic data
     * @param vector_time_stamps time stamps for the vector data
     * @param scalar_time_stamps time stamps for the scalar data or NULL if there is no scalar data
     * @param temperatures the temperature data (may be null)
     * @param temperature_time_stamps the corresponding temperature time stamps, one for each temperature variable
     * @throws CDFException if the vector data elements don't have the same sample period or start date */
    public ImagCDF (String iaga_code, IMCDFPublicationLevel pub_level,
                    Date pub_date, String observatory_name, 
                    double latitude, double longitude, double elevation, 
                    String institution, String vector_sens_orient,
                    IMCDFStandardLevel standard_level, IMCDFStandardName standard_name,
                    String standard_version, String partial_stand_desc,
                    String source, String unique_identifier, String parent_identifiers [],
                    URL reference_links [],
                    ImagCDFVariable elements [], 
                    ImagCDFVariableTS vector_time_stamps, ImagCDFVariableTS scalar_time_stamps,
                    ImagCDFVariable temperatures [], ImagCDFVariableTS temperature_time_stamps [])
    throws CDFException
    {
        int count;
        
        write_progress_listeners = new ArrayList<> ();
        
        // global metadata
        this.format_description = "INTERMAGNET CDF Format";
        this.format_version = FORMAT_VERSION_SUPPORTED;
        this.title = "Geomagnetic time series data";
        this.iaga_code = iaga_code;
        this.pub_level = pub_level;
        this.pub_date = pub_date;
        this.observatory_name = observatory_name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.institution = institution;
        this.vector_sens_orient = vector_sens_orient == null ? "" : vector_sens_orient;
        this.standard_level = standard_level;
        this.standard_name = standard_name;
        this.standard_version = standard_version == null ? "" : standard_version;
        this.partial_stand_desc = partial_stand_desc == null ? "" : partial_stand_desc;
        this.source = source;
        this.terms_of_use = getINTERMAGNETTermsOfUse();
        this.unique_identifier = unique_identifier == null ? "" : unique_identifier;
        this.parent_identifiers = parent_identifiers;
        this.reference_links = reference_links;
        
        // data arrays
        this.elements = elements;
        this.vector_time_stamps = vector_time_stamps;
        this.scalar_time_stamps = scalar_time_stamps;
        if (temperatures == null) 
        {
            this.temperatures = new ImagCDFVariable [0];
            this.temperature_time_stamps = new ImagCDFVariableTS [0];
        }
        else 
        {
            if (temperature_time_stamps.length < temperatures.length)
                throw new CDFException ("Not enough temperature time stamp variables");
            this.temperatures = temperatures;
            this.temperature_time_stamps = temperature_time_stamps;
        }

        // construct elements recorded from variables
        this.elements_recorded = "";
        for (count=0; count<elements.length; count++)
            elements_recorded += elements[count].getElementRecorded().toUpperCase();
        
        // fix the ordering of some standard orientation codes
        if (elements_recorded.contains("H") &&
            elements_recorded.contains("D") &&
            elements_recorded.contains("Z")) 
        {
            switch (elements_recorded.length())
            {
                case 3:
                    elements_recorded = "HDZ";
                    break;
                case 4:
                    if (elements_recorded.contains("F")) elements_recorded = "HDZF";
                    if (elements_recorded.contains("G")) elements_recorded = "HDZG";
                    if (elements_recorded.contains("S")) elements_recorded = "HDZS";
                    break;
            }
        }
        if (elements_recorded.contains("X") &&
            elements_recorded.contains("Y") &&
            elements_recorded.contains("Z")) 
        {
            switch (elements_recorded.length())
            {
                case 3:
                    elements_recorded = "XYZ";
                    break;
                case 4:
                    if (elements_recorded.contains("F")) elements_recorded = "XYZF";
                    if (elements_recorded.contains("G")) elements_recorded = "XYZG";
                    if (elements_recorded.contains("S")) elements_recorded = "XYZS";
                    break;
            }
        }
        if (elements_recorded.contains("D") &&
            elements_recorded.contains("I") &&
            elements_recorded.contains("F")) 
        {
            switch (elements_recorded.length())
            {
                case 3:
                    elements_recorded = "DIF";
                    break;
                case 4:
                    if (elements_recorded.contains("F")) elements_recorded = "DIFF";
                    if (elements_recorded.contains("G")) elements_recorded = "DIFG";
                    if (elements_recorded.contains("S")) elements_recorded = "DIFS";
                    break;
            }
        }
        
        checkMetadata();
    }
    
    public void addWriteProgressListener (IMCDFWriteProgressListener listener)
    {
        write_progress_listeners.add (listener);
    }
    public void removeWriteProgressListener (IMCDFWriteProgressListener listener)
    {
        write_progress_listeners.remove(listener);
    }
    private boolean callWriteProgressListeners (int current_data_set_percent)
    {
        Iterator<IMCDFWriteProgressListener> i;
        int n_data_points_written, percent, count;
        boolean continue_writing;
        
        // convert the percentage through the current data set to the number of data points written
        // then work out the percentage of all data points written
        if (variable_being_written_index < 0 || current_data_set_percent < 0) percent = 0;
        else if (variable_being_written_index >= n_samples_per_variable.length || current_data_set_percent > 100) percent = 100;
        else
        {
            n_data_points_written = 0;
            for (count=0; count<variable_being_written_index; count++)
                n_data_points_written += n_samples_per_variable [count];
            n_data_points_written += (n_samples_per_variable [variable_being_written_index] * current_data_set_percent) / 100;
            percent = (n_data_points_written * 100) / n_data_points_total;
        }
        
        i = write_progress_listeners.iterator();
        continue_writing = true;
        while (i.hasNext()) continue_writing &= i.next().percentComplete(percent);
        return continue_writing;
    }

    /** write this data to a CDF file
     * @param cdf_file the CDF file to write into
     * @param compress true to compress the CDF file, FALSE not to compress
     * @param overwrite_existing true to overwrite any existing file, false to throw exception if file exists
     * @throws CDFException if there is an error, including user abort */
    public void write (File cdf_file, boolean compress, boolean overwrite_existing)
    throws CDFException
    {
        int count;
        boolean abort;
        String string;
        ImagCDFLowLevel cdf;
        List <Integer> lengths;
        CDFException stored_close_exception;
        
        // check that the CDF libraries are available
        string = ImagCDFLowLevel.checkNativeLib("");
        if (string != null) throw new CDFException (string);
        
        abort = false;
        cdf = null;
        stored_close_exception = null;
        try
        {
            cdf = new ImagCDFLowLevel (cdf_file, 
                                       overwrite_existing ? ImagCDFLowLevel.CDFOpenType.CDFForceCreate : ImagCDFLowLevel.CDFOpenType.CDFCreate,
                                       compress ? ImagCDFLowLevel.CDFCompressType.GZip6 : ImagCDFLowLevel.CDFCompressType.None);

            cdf.addGlobalAttribute ("FormatDescription",         0, true,  format_description);
            cdf.addGlobalAttribute ("FormatVersion",             0, true,  format_version);
            cdf.addGlobalAttribute ("Title",                     0, true,  title);
            cdf.addGlobalAttribute ("IagaCode",                  0, true,  iaga_code);
            cdf.addGlobalAttribute ("ElementsRecorded",          0, true,  elements_recorded);
            cdf.addGlobalAttribute ("PublicationLevel",          0, true,  pub_level);
            cdf.addGlobalAttribute ("PublicationDate",           0, true,  pub_date);
            cdf.addGlobalAttribute ("ObservatoryName",           0, true,  observatory_name);
            cdf.addGlobalAttribute ("Latitude",                  0, true,  new Double (latitude));
            cdf.addGlobalAttribute ("Longitude",                 0, true,  new Double (longitude));
            cdf.addGlobalAttribute ("Elevation",                 0, true,  new Double (elevation));
            cdf.addGlobalAttribute ("Institution",               0, true,  institution);
            cdf.addGlobalAttribute ("VectorSensOrient",          0, false, vector_sens_orient);
            cdf.addGlobalAttribute ("StandardLevel",             0, true,  standard_level);
            cdf.addGlobalAttribute ("StandardName",              0, false, standard_name);
            cdf.addGlobalAttribute ("StandardVersion",           0, false, standard_version);
            cdf.addGlobalAttribute ("PartialStandDesc",          0, false, partial_stand_desc);
            cdf.addGlobalAttribute ("Source",                    0, true,  source);
            cdf.addGlobalAttribute ("TermsOfUse",                0, false, terms_of_use);
            cdf.addGlobalAttribute ("UniqueIdentifier",          0, false, unique_identifier);
            for (count=0; count<parent_identifiers.length; count++)
                cdf.addGlobalAttribute ("ParentIdentifiers", count, true, parent_identifiers [count]);
            for (count=0; count<reference_links.length; count++)
                cdf.addGlobalAttribute ("References",        count, true, reference_links [count].toString());
        
            // check temperature time stamps array is the same length as the temperature array
            if (temperatures == null) temperatures = new ImagCDFVariable[0];
            if (temperature_time_stamps == null) temperature_time_stamps = new ImagCDFVariableTS[0];
            if (temperatures.length != temperature_time_stamps.length)
                throw new CDFException ("Temperature time stamps array was be the same length as array of temperatures");
        
            // set up variables for minitoring progress - the array containing the length of each sample must correspond to the
            // order in which the data is written to file
            lengths = new ArrayList <> ();
            for (count=0; count<elements.length; count++)
                lengths.add (new Integer(elements[count].getDataLength()));
            lengths.add (new Integer (vector_time_stamps.getNSamples()));
            if (scalar_time_stamps != null)
                lengths.add (new Integer (scalar_time_stamps.getNSamples()));
            for (count=0; count<temperatures.length; count++)
            {
                lengths.add (new Integer (temperatures[count].getDataLength()));
                lengths.add (new Integer (temperature_time_stamps[count].getNSamples()));
            }
            n_samples_per_variable = new int [lengths.size()];
            n_data_points_total = 0;
            for (count=0; count<n_samples_per_variable.length; count++)
            {
                n_samples_per_variable [count] = lengths.get(count).intValue();
                n_data_points_total += lengths.get(count).intValue();
            }
            variable_being_written_index = -1;
            if (! callWriteProgressListeners (-1)) 
            {
                abort = true;
            }
        
            // write the individual variables to the file
            for (count=0; (count<elements.length) && (! abort); count++)
            {
                variable_being_written_index ++;
                elements[count].addWriteProgressListener(this);
                if (! elements[count].write (cdf, elements[count].getElementRecorded())) 
                {
                    abort = true;
                }
                elements[count].removeWriteProgressListener(this);
            }
            variable_being_written_index ++;
            if (! abort)
            {
                vector_time_stamps.addWriteProgressListener(this);
                if (! vector_time_stamps.write (cdf)) 
                {
                    abort = true;
                }
                vector_time_stamps.removeWriteProgressListener(this);
            }
            if (scalar_time_stamps != null)
            {
                variable_being_written_index ++;
                if (! abort)
                {
                    scalar_time_stamps.addWriteProgressListener(this);
                    if (! scalar_time_stamps.write (cdf)) 
                    {
                        abort = true;
                    }
                    scalar_time_stamps.removeWriteProgressListener(this);
                }
            }
         
            for (count=0; (count<temperatures.length) && (! abort); count++)
            {
                variable_being_written_index ++;
                temperatures[count].addWriteProgressListener(this);
                if (! temperatures[count].write(cdf, Integer.toString (count))) abort = true;
                temperatures[count].removeWriteProgressListener(this);
                variable_being_written_index ++;
                if (! abort)
                {
                    temperature_time_stamps[count].addWriteProgressListener(this);
                    if (! temperature_time_stamps[count].write (cdf)) 
                    {
                        abort = true;
                    }
                    temperature_time_stamps[count].removeWriteProgressListener(this);
                }
            }

            variable_being_written_index ++;
            if (! callWriteProgressListeners (101)) abort = true;
        }
        finally
        {
            try
            {
                // finalise the file
                if (cdf != null) cdf.close ();

                // remove the file if the operation was aborted
                if (abort)
                {
                    cdf_file.delete();
                    stored_close_exception = new CDFException ("User aborted write operation, " + cdf_file.getName() + " deleted");
                }
            }
            catch (CDFException e)
            {
                stored_close_exception = e;
            }
        }
        
        // process any problems when the file was closed
        if (stored_close_exception != null) throw stored_close_exception;
    }
    
    public String getFormatDescription() { return format_description; }
    public String getFormatVersion() { return format_version; }
    public String getTitle() { return title; }
    public String getIagaCode() { return iaga_code; }
    public String getElementsRecorded() { return elements_recorded; }
    public IMCDFPublicationLevel getPublicationLevel() {  return pub_level; }
    public Date getPublicationDate() { return pub_date; }
    public String getObservatoryName() { return observatory_name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getElevation() { return elevation; }
    public String getInstitution() { return institution; }
    public String getVectorSensorOrientation() { return vector_sens_orient; }
    public IMCDFStandardLevel getStandardLevel() { return standard_level; }
    public IMCDFStandardName getStandardName() { return standard_name; }
    public String getStandardVersion() { return standard_version; }
    public String getPartialStandDesc() { return partial_stand_desc; }
    public String getSource() { return source; }
    public String getTermsOfUse() { return terms_of_use; }
    public String getUniqueIdentifier() { return unique_identifier; }
    public String [] getParentIdentifiers() { return parent_identifiers; }
    public URL [] getReferenceLinks() { return reference_links; }

    public int getNElements () { return elements.length; }
    public ImagCDFVariable getElement (int index) { return elements [index]; }
    public ImagCDFVariableTS getVectorTimeStamps () { return vector_time_stamps; }
    public ImagCDFVariableTS getScalarTimeStamps () { return scalar_time_stamps; }
    
    /** finds the index of the first three vector elements
     * @return an array containing indices of elements, or null if there are less than thee vector elements  */
    public int [] findVectorElements ()
    {
        int count, indexes [], found_count;

        indexes = new int [3];
        for (count=found_count=0; count<elements.length; count++)
        {
            if (elements[count].isVectorGeomagneticData())
            {
                indexes [found_count ++] = count;
                if (found_count == indexes.length) return indexes;
            }
        }
        return null;
    }
    
    /** find the first scalar element
     * @return the element index or a -ve number if there is no scalar element in the file */
    public int findScalarElement ()
    {
        int count;

        for (count=0; count<elements.length; count++)
        {
            if (elements[count].isScalarGeomagneticData())
                return count;
        }
        return -1;
    }
    
    /** get the element code string corresponding to the given vector and scalar elements
     * @param vector_indices the indexes to the vector elements
     * @param scalar_index  the index to the scalar element OR -ve for no scalar index
     * @return the concatenated element code */
    public String getElementCodes (int vector_indices [], int scalar_index)
    {
        String codes;
        int count;

        codes = "";
        for (count=0; count<vector_indices.length; count++)
            codes += elements[vector_indices[count]].getElementRecorded();
        if (scalar_index >= 0)
            codes += elements [scalar_index].getElementRecorded();
        return codes;
    }
    
    public int getNTemperatures () { return temperatures.length; }
    public ImagCDFVariable getTemperature (int index) { return temperatures [index]; }
    public ImagCDFVariableTS getTemperatureTimeStamps (int index) { return temperature_time_stamps [index]; }
    
    /** generate an IMAG CDF filename 
     * @param prefix the prefix for the name (including any directory)
     * @param station_code the IAGA station code
     * @param sample_period the sample period of the data
     * @param start_date the start date for the data
     * @param pub_level the publication level
     * @param force_lower_case set true to force the filename to lower case
     *        (as demanded by the IAGA 2002 format description)
     * @return the file OR null if there is an error */
    public static String makeFilename (String prefix, String station_code, 
                                       FilenameSamplePeriod sample_period, Date start_date,
                                       IMCDFPublicationLevel pub_level)
    {
        ImagCDFFilename.Interval cdf_interval;
        switch (sample_period)
        {
            case ANNUAL: cdf_interval = ImagCDFFilename.Interval.ANNUAL; break;
            case MONTHLY: cdf_interval = ImagCDFFilename.Interval.MONTHLY; break;
            case DAILY: cdf_interval = ImagCDFFilename.Interval.DAILY; break;
            case HOURLY: cdf_interval = ImagCDFFilename.Interval.HOURLY; break;
            case MINUTE: cdf_interval = ImagCDFFilename.Interval.MINUTE; break;
            case SECOND: cdf_interval = ImagCDFFilename.Interval.SECOND; break;
            default: cdf_interval = ImagCDFFilename.Interval.UNKNOWN; break;
        }
        return new ImagCDFFilename (station_code, start_date, pub_level, cdf_interval, ImagCDFFilename.Case.LOWER).getFilename ();
    }

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
    
    private void checkMetadata ()
    throws CDFException
    {
        if (! title.equalsIgnoreCase             ("Geomagnetic time series data")) throw new CDFException ("Format Error");
        if (! format_description.equalsIgnoreCase("INTERMAGNET CDF Format"))       throw new CDFException ("Format Error");
        if (! format_version.equalsIgnoreCase    (FORMAT_VERSION_SUPPORTED))       throw new CDFException ("Format Error");

        switch (standard_level.getStandardLevel())
        {
            case FULL:
                if (standard_name == null) throw new CDFException ("Missing StandardName attribute");
                break;
            case PARTIAL:
                if (standard_name == null) throw new CDFException ("Missing StandardName attribute");
                if (partial_stand_desc == null) throw new CDFException ("Missing PartialStandDesc attribute");
                break;
        }
        
        if (parent_identifiers == null) parent_identifiers = new String [0];
        if (reference_links == null) reference_links = new URL [0];
    }

    // used to receive progress reports from ImagCDFVariable objects when they are writing data
    @Override
    public boolean percentComplete(int percent_complete) 
    {
        return callWriteProgressListeners(percent_complete);
    }
    
}
