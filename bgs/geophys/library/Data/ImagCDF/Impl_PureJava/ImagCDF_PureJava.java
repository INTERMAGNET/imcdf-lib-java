/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF.Impl_PureJava;

import bgs.geophys.library.Data.ImagCDF.IMCDFException;
import bgs.geophys.library.Data.ImagCDF.IMCDFPublicationLevel;
import bgs.geophys.library.Data.ImagCDF.IMCDFStandardLevel;
import bgs.geophys.library.Data.ImagCDF.IMCDFStandardName;
import bgs.geophys.library.Data.ImagCDF.IMCDFVariableType;
import bgs.geophys.library.Data.ImagCDF.ImagCDF;
import bgs.geophys.library.Data.ImagCDF.ImagCDFFactory;
import bgs.geophys.library.Data.ImagCDF.ImagCDFVariable;
import bgs.geophys.library.Data.ImagCDF.ImagCDFVariableTS;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
public class ImagCDF_PureJava extends ImagCDF
{
    /** read an ImagCDF file
     * @param file the CDF file
     * @throws IMCDFException if there is an error */
    public ImagCDF_PureJava (File file)
    throws IMCDFException
    {
        this (file, false);
    }
    
    /** read an ImagCDF file
     * @param file the CDF file
     * @throws IMCDFException if there is an error */
    public ImagCDF_PureJava (File file, boolean headerOnly)
    throws IMCDFException
    {
        super ();
        
        // open the CDF file
        ImagCDFLowLevelReader_PureJava cdf = null;
        try
        {
            cdf = new ImagCDFLowLevelReader_PureJava (file);

            // get global metadata
            List<String> links = new ArrayList<> ();
            List<String> pids = new ArrayList<> ();
            format_description =           cdf.getGlobalAttributeString("FormatDescription", 0, true);
            format_version =               cdf.getGlobalAttributeString("FormatVersion",     0, true);
            title =                        cdf.getGlobalAttributeString("Title",             0, true);
            iaga_code =                    cdf.getGlobalAttributeString("IagaCode",          0, true);
            elements_recorded =            cdf.getGlobalAttributeString("ElementsRecorded",  0, true);
            String pub_level_string =      cdf.getGlobalAttributeString("PublicationLevel",  0, true);
            pub_date =                     cdf.getGlobalAttributeDate  ("PublicationDate",   0, true);
            observatory_name =             cdf.getGlobalAttributeString("ObservatoryName",   0, true);
            latitude =                     cdf.getGlobalAttributeDouble("Latitude",          0, true);
            longitude =                    cdf.getGlobalAttributeDouble("Longitude",         0, true);
            elevation =                    cdf.getGlobalAttributeDouble("Elevation",         0, true);
            institution =                  cdf.getGlobalAttributeString("Institution",       0, true);
            vector_sens_orient =           cdf.getGlobalAttributeString("VectorSensOrient",  0, false);
            String standard_level_string = cdf.getGlobalAttributeString("StandardLevel",     0, true);
            String standard_name_string =  cdf.getGlobalAttributeString("StandardName",      0, false);
            standard_version =             cdf.getGlobalAttributeString("StandardVersion",   0, false);
            partial_stand_desc =           cdf.getGlobalAttributeString("PartialStandDesc",  0, false);
            source =                       cdf.getGlobalAttributeString("Source",            0, true);
            terms_of_use =                 cdf.getGlobalAttributeString("TermsOfUse",        0, false);
            unique_identifier =            cdf.getGlobalAttributeString("UniqueIdentifier",  0, false);
            if (pub_level_string == null) pub_level = null;
            else pub_level = new IMCDFPublicationLevel (pub_level_string);
            if (standard_level_string == null) standard_level = null;
            else standard_level = new IMCDFStandardLevel (standard_level_string);
            if (standard_name_string == null) standard_name = null;
            else standard_name = new IMCDFStandardName (standard_name_string); 
            String string = "";
            for (int count=0; string != null; count ++)
            {
                string = cdf.getGlobalAttributeString("ParentIdentifiers", count, false);
                if (string != null) pids.add (string);
            }
            string = "";
            for (int count=0; string != null; count ++)
            {
                string = cdf.getGlobalAttributeString("ReferenceLinks", count, false);
                if (string != null) links.add (string);
            }

            // sort out the array of reference links
            reference_links = new URL [links.size()];
            int count = 0;
            try
            {   
                for (String s : links)
                {
                    reference_links [count ++] = new URL (s);
                }
            }
            catch (MalformedURLException ex)
            {
                cdf.process_error ("Badly formed URL in reference link " + (count +1), ex);
            }

            // sort out the array of parent identifiers
            parent_identifiers = new String [pids.size()];
            count = 0;
            for (String s : pids)
            {
                parent_identifiers [count ++] = s;
            }
        
            // set those object fields to empty values that won't be used when
            // only headers are being read
            elements = new ImagCDFVariable[0];
            temperatures = new ImagCDFVariable[0];
            time_stamps = new ImagCDFVariableTS[0];
            
            // read the data
            if (! headerOnly)
            {
                // get geomagnetic field data - find variable names based on elements recorded
                if (elements_recorded != null)
                {
                    IMCDFVariableType field_var_type = new IMCDFVariableType (IMCDFVariableType.VariableTypeCode.GeomagneticFieldElement);
                    int n_elements = elements_recorded.length();
                    elements = new ImagCDFVariable_PureJava [n_elements];
                    for (count=0; count<n_elements; count++)
                        elements [count] = new ImagCDFVariable_PureJava(cdf, field_var_type, elements_recorded.substring(count, count +1));
                }

                // find the number of temperature variables and get temperature data
                IMCDFVariableType temperature_var_type = new IMCDFVariableType (IMCDFVariableType.VariableTypeCode.Temperature);
                int n_temperatures = 0;
                while (cdf.isVariableExist (temperature_var_type.getCDFFileVariableName(Integer.toString (n_temperatures +1)))) n_temperatures ++;
                temperatures = new ImagCDFVariable_PureJava [n_temperatures];
                for (count=0; count<n_temperatures; count++)
                    temperatures [count] = new ImagCDFVariable_PureJava(cdf, temperature_var_type, Integer.toString (count +1));

                // work out the names of the time stamp arrays
                List<String> unique_ts_names = new ArrayList<> ();
                for (count=0; count<elements.length; count++)
                {
                    String depend_0 = elements[count].getDepend0();
                    if (depend_0 != null)
                    {
                        boolean found = false;
                        for (String ts_name : unique_ts_names)
                        {
                            if (ts_name.equals(depend_0)) found = true;
                        }
                        if (! found) unique_ts_names.add (depend_0);
                    }
                }
                for (count=0; count<temperatures.length; count++)
                {
                    String depend_0 = temperatures[count].getDepend0();
                    if (depend_0 != null)
                    {
                        boolean found = false;
                        for (String ts_name : unique_ts_names)
                        {
                            if (ts_name.equals(depend_0)) found = true;
                        }
                        if (! found) unique_ts_names.add (depend_0);
                    }
                }

                // read the time stamp arrays
                time_stamps = new ImagCDFVariableTS_PureJava [unique_ts_names.size()];
                for (count=0; count<time_stamps.length; count++)
                    time_stamps [count] = new ImagCDFVariableTS_PureJava (cdf, unique_ts_names.get(count));

            }
        }
        finally
        {
            // close the file
            if (cdf != null) cdf.close ();
        }
        
        // check the metadata
        if (cdf == null)
            throw new IMCDFException ("Internal software errors");
        List<String> errors = cdf.getAccumulatedErrors();
        super.checkMetadata (errors);
        
        // check for errors
        if (! errors.isEmpty())
            throw new IMCDFException (errors.get(0), errors);
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
     * @throws IMCDFException if the vector data elements don't have the same sample period or start date */
    public ImagCDF_PureJava (String iaga_code, IMCDFPublicationLevel pub_level,
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
        super ();
        
        // global metadata
        this.format_description = "INTERMAGNET CDF Format";
        this.format_version = FORMAT_VERSIONS_SUPPORTED[FORMAT_VERSIONS_SUPPORTED.length -1];
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
        this.terms_of_use = ImagCDFFactory.getINTERMAGNETTermsOfUse();
        this.unique_identifier = unique_identifier == null ? "" : unique_identifier;
        this.parent_identifiers = parent_identifiers;
        this.reference_links = reference_links;
        
        // data arrays
        this.elements = elements;
        if (temperatures == null) 
            this.temperatures = new ImagCDFVariable_PureJava [0];
        else 
            this.temperatures = temperatures;
        
        // record time stamps
        this.time_stamps = time_stamps;

        // construct elements recorded from variables
        this.elements_recorded = "";
        for (int count=0; count<elements.length; count++)
            elements_recorded += this.elements[count].getElementRecorded().toUpperCase();
        
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
                    if (elements_recorded.contains("G")) elements_recorded = "HDZG";
                    else if (elements_recorded.contains("S")) elements_recorded = "HDZS";
                    else if (elements_recorded.contains("F")) elements_recorded = "HDZF";
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
                    if (elements_recorded.contains("G")) elements_recorded = "XYZG";
                    else if (elements_recorded.contains("S")) elements_recorded = "XYZS";
                    else if (elements_recorded.contains("F")) elements_recorded = "XYZF";
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
                    if (elements_recorded.contains("G")) elements_recorded = "DIFG";
                    else if (elements_recorded.contains("S")) elements_recorded = "DIFS";
                    else if (elements_recorded.contains("F")) elements_recorded = "DIFF";
                    break;
            }
        }
        
        List<String> errors = new ArrayList<> ();
        super.checkMetadata(errors);
        if (! errors.isEmpty())
            throw new IMCDFException (errors.get(0), errors);
    }
    
    /** write this data to a CDF file
     * @param cdf_file the CDF file to write into
     * @param compress true to compress the CDF file, FALSE not to compress
     * @param overwrite_existing true to overwrite any existing file, false to throw exception if file exists
     * @throws IMCDFException if there is an error, including user abort */
    @Override
    public void write (File cdf_file, boolean compress, boolean overwrite_existing)
    throws IMCDFException
    {
        int count;
        boolean abort;
        String string;
        ImagCDFLowLevelWriter_PureJava cdf;
        List <Integer> lengths;
        IMCDFException stored_close_exception;
        
        abort = false;
        cdf = null;
        stored_close_exception = null;
        try
        {
            cdf = new ImagCDFLowLevelWriter_PureJava (cdf_file, overwrite_existing, compress);

            cdf.addGlobalAttribute ("FormatDescription",         0, true,  format_description);
            cdf.addGlobalAttribute ("FormatVersion",             0, true,  format_version);
            cdf.addGlobalAttribute ("Title",                     0, true,  title);
            cdf.addGlobalAttribute ("IagaCode",                  0, true,  iaga_code);
            cdf.addGlobalAttribute ("ElementsRecorded",          0, true,  elements_recorded);
            cdf.addGlobalAttribute ("PublicationLevel",          0, true,  pub_level);
            cdf.addGlobalAttribute ("PublicationDate",           0, true,  pub_date);
            cdf.addGlobalAttribute ("ObservatoryName",           0, true,  observatory_name);
            cdf.addGlobalAttribute ("Latitude",                  0, true,  latitude);
            cdf.addGlobalAttribute ("Longitude",                 0, true,  longitude);
            cdf.addGlobalAttribute ("Elevation",                 0, true,  elevation);
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
                cdf.addGlobalAttribute ("ReferenceLinks",    count, true,  reference_links [count].toString());
        
            // set up variables for monitoring progress - the array containing the length of each sample must correspond to the
            // order in which the data is written to file
            lengths = new ArrayList <> ();
            for (count=0; count<elements.length; count++)
                lengths.add (elements[count].getDataLength());
            for (count=0; count<temperatures.length; count++)
                lengths.add (temperatures[count].getDataLength());
            for (count=0; count<time_stamps.length; count++)
                lengths.add (time_stamps[count].getNSamples());
            n_samples_per_variable = new int [lengths.size()];
            n_data_points_total = 0;
            for (count=0; count<n_samples_per_variable.length; count++)
            {
                n_samples_per_variable [count] = lengths.get(count);
                n_data_points_total += lengths.get(count);
            }
            variable_being_written_index = -1;
            if (! callWriteProgressListeners (-1)) 
            {
                abort = true;
            }
        
            // write the geomagnetic data to file
            for (count=0; (count<elements.length) && (! abort); count++)
            {
                if (elements[count] instanceof ImagCDFVariable_PureJava)
                {
                    variable_being_written_index ++;
                    elements[count].addWriteProgressListener(this);
                    if (! ((ImagCDFVariable_PureJava) elements[count]).write (cdf, elements[count].getElementRecorded())) 
                        abort = true;
                    elements[count].removeWriteProgressListener(this);
                }
                else
                    throw new IMCDFException ("Internal software error");
            }
         
            // write the temperature data to file
            for (count=0; (count<temperatures.length) && (! abort); count++)
            {
                if (elements[count] instanceof ImagCDFVariable_PureJava)
                {
                    variable_being_written_index ++;
                    temperatures[count].addWriteProgressListener(this);
                    if (! ((ImagCDFVariable_PureJava) temperatures[count]).write(cdf, Integer.toString (count +1))) 
                        abort = true;
                    temperatures[count].removeWriteProgressListener(this);
                }
                else
                    throw new IMCDFException ("Internal software error");
            }
            
            // write the time stamps to file
            for (count=0; (count<time_stamps.length) && (! abort); count++)
            {
                if (time_stamps[count] instanceof ImagCDFVariableTS_PureJava)
                {
                    variable_being_written_index ++;
                    time_stamps[count].addWriteProgressListener(this);
                    if (! ((ImagCDFVariableTS_PureJava) time_stamps[count]).write (cdf)) 
                        abort = true;
                    time_stamps[count].removeWriteProgressListener(this);
                }
                else
                    throw new IMCDFException ("Internal software error");
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
                    stored_close_exception = new IMCDFException ("User aborted write operation, " + cdf_file.getName() + " deleted");
                }
            }
            catch (IMCDFException e)
            {
                stored_close_exception = e;
            }
        }
        
        // process any problems when the file was closed
        if (stored_close_exception != null) throw stored_close_exception;
    }
}
