
package bgs.geophys.library.Data.ImagCDF;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/** the base class for ImagCDF implementations */
public abstract class ImagCDF
implements IMCDFWriteProgressListener
{
    
    /** constant value used to indicate missing data */
    public static final double MISSING_DATA_VALUE = 99999.0;
    /** constant value used to indicate missing time stamp */
    public static final long MISSING_TIME_VALUE = - Long.MAX_VALUE;

    /** the versions of the data format that this software supports */
    public static final String [] FORMAT_VERSIONS_SUPPORTED = {"1.2", "1.3"};
    
    // a list of listeners who will recieve "percent complete" notification during writing of data
    private final List<IMCDFWriteProgressListener> write_progress_listeners;
    
    /** an array that holds the number of points in each variable to be written out */    
    protected int n_samples_per_variable [];
    /** a pointer that shows which variable from the n_samples_per_variable array is currently 
     * being written - the pointer can be set to
     * both &lt; 0 (before the start of writing) and &gt;= the array length (after the end of writing)
     * n_data_points_total is the total number of data points to write to file */
    protected int variable_being_written_index;
    /** total number of data points */
    protected int n_data_points_total;
    
    /** description of the ImagCDF data format */
    protected String format_description;
    /** version of the ImagCDF data format */
    protected String format_version;
    /** title for this data set */
    protected String title;
    /** IAGA code for the observatory */
    protected String iaga_code;
    /** list of the geomagnetism elements in the CDF file, one character per element */
    protected String elements_recorded;
    /** the publication level (AKA data type) */
    protected IMCDFPublicationLevel pub_level;
    /** the date of publication */
    protected Date pub_date;
    /** the name of the observatory */
    protected String observatory_name;
    /** the latitude at the observatory */
    protected Double latitude;
    /** the longitude at the observatory */
    protected Double longitude;
    /** the elevation at the observatory */
    protected Double elevation;
    /** the institute responsible for the observatory */
    protected String institution;
    /** the orientation of the sensor used to record the vector data */
    protected String vector_sens_orient;
    /** the standard to which the data conforms */
    protected IMCDFStandardLevel standard_level;
    /** the name of the standard to which the data conforms */
    protected IMCDFStandardName standard_name;
    /** the version of the standard to which the data conforms */
    protected String standard_version;
    /** for data sets only partially meeting a standard, this describes which
     * parts of the standard the data set meets */
    protected String partial_stand_desc;
    /** the source of the data */
    protected String source;
    /** terms under which users can use the data */
    protected String terms_of_use;
    /** a persistent identifier for the data */
    protected String unique_identifier;
    /** a list of parent identifiers for the data */
    protected String parent_identifiers [];
    /** a list of URLs that describe the data */
    protected URL reference_links [];

    /** the field element data array */
    protected ImagCDFVariable elements [];
    /** the temperature data array */
    protected ImagCDFVariable temperatures [];
    
    /** the time stamps for the field data and the temperature data */
    protected ImagCDFVariableTS time_stamps [];

    /** create a new empty ImagCDF object */
    protected ImagCDF ()
    {
        write_progress_listeners = new ArrayList<> ();
    }
    
     /** write this data to a CDF file - implementation is provided by sub-classes
     * @param cdf_file the CDF file to write into
     * @param compress true to compress the CDF file, FALSE not to compress
     * @param overwrite_existing true to overwrite any existing file, false to throw exception if file exists
     * @throws IMCDFException if there is an error, including user abort */
    public abstract void write (File cdf_file, boolean compress, boolean overwrite_existing)
    throws IMCDFException;

    /** add a listener that receives notification of the progress of write operations
     * @param listener the listener */
    public void addWriteProgressListener (IMCDFWriteProgressListener listener)
    {
        write_progress_listeners.add (listener);
    }
    /** remove a listener that receives notification of the progress of write operations
     * @param listener the listener */
    public void removeWriteProgressListener (IMCDFWriteProgressListener listener)
    {
        write_progress_listeners.remove(listener);
    }
    /** call the listeners with notification of progress of writing
     * @param current_data_set_percent how complete the write operation is
     * @return the completeness as a percent
     */
    protected boolean callWriteProgressListeners (int current_data_set_percent)
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

    /** get a description of the ImagCDF data format
     * @return the description */
    public String getFormatDescription() { return format_description; }
    /** get the version of this implementation of the ImagCDF data format
     * @return the version */
    public String getFormatVersion() { return format_version; }
    /** get the title for this data set
     * @return the title */
    public String getTitle() { return title; }
    /** get the IAGA code for the observatory
     * @return the IAGA code */
    public String getIagaCode() { return iaga_code; }
    /** get the orientation of the data - also acts as a list of data arrays in the CDF file
     * @return the orientation */
    public String getElementsRecorded() { return elements_recorded; }
    /** get the publication level (AKA data "type")
     * @return the publication level */
    public IMCDFPublicationLevel getPublicationLevel() {  return pub_level; }
    /** get the date of publication
     * @return the date */
    public Date getPublicationDate() { return pub_date; }
    /** get the name of the observatory
     * @return the name */
    public String getObservatoryName() { return observatory_name; }
    /** get the observatory's latitude
     * @return the latitude */
    public Double getLatitude() { return latitude; }
    /** get the observatory's longitude
     * @return the longitude */
    public Double getLongitude() { return longitude; }
    /** get the observatory's elevation
     * @return the elevation */
    public Double getElevation() { return elevation; }
    /** get the name of the responsible institute
     * @return the institute name */
    public String getInstitution() { return institution; }
    /** get the orientation of the sensor used to record the vector data
     * @return the orientation */
    public String getVectorSensorOrientation() { return vector_sens_orient; }
    /** get the standards to which the data conforms
     * @return the standard */
    public IMCDFStandardLevel getStandardLevel() { return standard_level; }
    /** get the name of the standard to which the data conforms
     * @return the name */
    public IMCDFStandardName getStandardName() { return standard_name; }
    /** get the version of the standard to which the data conforms
     * @return the version */
    public String getStandardVersion() { return standard_version; }
    /** get the description of which parts of the standard the data conforms to
     * (if it does not fully conform)
     * @return the list of partial standards confirmation */
    public String getPartialStandDesc() { return partial_stand_desc; }
    /** get the source of the data
     * @return the source */
    public String getSource() { return source; }
    /** get the terms under which users can use the data
     * @return the terms of use */
    public String getTermsOfUse() { return terms_of_use; }
    /** get any unique (persistent) ID for the data
     * @return the ID */
    public String getUniqueIdentifier() { return unique_identifier; }
    /** get any parent persistent IDs for the data
     * @return the IDs */
    public String [] getParentIdentifiers() { return parent_identifiers; }
    /** get any URLs relating to the data
     * @return the URLs */
    public URL [] getReferenceLinks() { return reference_links; }

    /** get the number of geomagnetic elements recorded
     * @return the number of elements */
    public int getNElements () { return elements.length; }
    /** get a geomagnetic element
     * @param index 0..N-1
     * @return the geomagnetic element */
    public ImagCDFVariable getElement (int index) { return elements [index]; }

    /** find the time stamps associated with a geomagnetic element or a temperature variable
     * @param var the variable
     * @return the time series or null */
    public ImagCDFVariableTS findTimeStamps (ImagCDFVariable var) 
    {
        for (ImagCDFVariableTS ts : time_stamps)
            if (ts.getVarName().equals(var.getDepend0()))
                return ts;
        return null;
    }
    
    /** find timestamps for vector geomagnetic elements
     * @return the time series
     * @throws IMCDFException  if the CDF variable can't be found */
    public ImagCDFVariableTS findVectorTimeStamps ()
    throws IMCDFException
    {
        int indices [] = findVectorElements();
        if (indices == null) throw new IMCDFException ("Can't find Geomagnetic vector data");
        ImagCDFVariableTS ts = findTimeStamps(elements[indices[0]]);
        if (ts == null) throw new IMCDFException ("Can't find Geomagnetic vector data time stamps");
        return ts;
    }
    
    /** finds the index of the first three vector elements
     * @return an array containing indices of elements, or null if there are less than thee vector elements  */
    public int [] findVectorElements ()
    {
        int indexes [] = new int [3];
        int found_count = 0;
        for (int count=0; count<elements.length; count++)
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
        for (int count=0; count<elements.length; count++)
        {
            if (elements[count].isScalarGeomagneticData())
                return count;
        }
        return -1;
    }
    
    /** find the timestamps for scalar data which may be the same as
     * those for vector data, or may not exist
     * @return a time stamp object or null if there is no scalar data
     */
    public ImagCDFVariableTS findScalarTimeStamps ()
    {
        int scalar_index = findScalarElement();
        if (scalar_index < 0) return null;
        ImagCDFVariable scalar_element = getElement(scalar_index);
        return findTimeStamps(scalar_element);
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

    /** get the number of temperature variables recorded in the data
     * @return the number of temperature variables */
    public int getNTemperatures () { return temperatures.length; }
    /** get a temperature variable
     * @param index 0..N-1
     * @return the temperature variable */
    public ImagCDFVariable getTemperature (int index) { return temperatures [index]; }

    /** check the metadata in this CDF and create an error list if there is a fault
     * @param accumulated_errors a list of error messages to add to */
    protected void checkMetadata (List<String> accumulated_errors)
    {
        if (title != null && ! title.equalsIgnoreCase ("Geomagnetic time series data")) 
            accumulated_errors.add ("Format Error in 'Title'");
        if (format_description != null && ! format_description.equalsIgnoreCase("INTERMAGNET CDF Format"))
            accumulated_errors.add ("Format Error in 'FormatDescription'");
        boolean found_version = false;
        String version_list = "";
        for (String version : FORMAT_VERSIONS_SUPPORTED) {
            if (format_version != null && format_version.equalsIgnoreCase (version))
                found_version  = true;
            version_list += version + " ";
        }
        if (! found_version)
            accumulated_errors.add ("Format Error, 'FormatVersion' must be one of " + version_list);

        if (standard_level != null)
        {
            switch (standard_level.getStandardLevel())
            {
                case FULL:
                    if (standard_name == null) accumulated_errors.add ("Missing StandardName attribute");
                    break;
                case PARTIAL:
                    if (standard_name == null) accumulated_errors.add ("Missing StandardName attribute");
                    if (partial_stand_desc == null) accumulated_errors.add ("Missing PartialStandDesc attribute");
                    break;
            }
        }
        
        if (parent_identifiers == null) parent_identifiers = new String [0];
        if (reference_links == null) reference_links = new URL [0];
        
        // check data variables have corresponding time stamps variables and that the
        // time stamp array for a data variable has the same length as its data variable
        ImagCDFVariable vars [] = new ImagCDFVariable[elements.length + temperatures.length];
        System.arraycopy(elements, 0, vars, 0, elements.length);
        System.arraycopy(temperatures, 0, vars, elements.length, temperatures.length);
        for (int count=0; count<vars.length; count++)
        {
            boolean found = false;
            for (int count2=0; count2<time_stamps.length; count2++)
            {
                if (vars[count] != null &&
                    vars[count].getDepend0() != null &&
                    vars[count].getDepend0().equals(time_stamps[count2].getVarName())) {
                    found = true;
                    if (time_stamps[count2] != null &&
                        time_stamps[count2].time_stamps != null &&
                        vars[count].getDataLength() != time_stamps[count2].getNSamples())
                        accumulated_errors.add ("Time stamp variable '" + time_stamps[count2].var_name + 
                                                "' and data variable '" + vars[count].var_name + "' have different lengths");
                }
            }
            if (! found)
                accumulated_errors.add ("Missing time stamp data for element " + vars[count].getElementRecorded());
        }
    }
    
    /** used to receive progress reports from ImagCDFVariable objects when they are writing data
     * @param percent_complete the completeness of the write operation as a percentage
     * @return whether the write operation should continue */
    @Override
    public boolean percentComplete(int percent_complete) 
    {
        return callWriteProgressListeners(percent_complete);
    }
    
}
