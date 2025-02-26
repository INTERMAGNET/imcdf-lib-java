
package bgs.geophys.library.Data.ImagCDF;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** the base class for ImagCDFVariable implementations */
public abstract class ImagCDFVariable
{
    /** only allow creation by subclasses */
    protected ImagCDFVariable ()
    {
        write_progress_listeners = new ArrayList<> ();
    }
    /** a list of listeners who will recieve "percent complete" notification during writing of data */
    protected List<IMCDFWriteProgressListener> write_progress_listeners;
    
    /** private member data - the type of data that this object holds */
    protected IMCDFVariableType variable_type;
    
        /** the CDF variable name **/
    protected String var_name;
    /** CDF variable attributes: the free text name of this variable */
    protected String field_nam;
    /** CDF variable attributes: valid minimum */
    protected Double valid_min;
    /** CDF variable attributes: valid maximum */
    protected Double valid_max;
    /** CDF variable attributes: units the data is recorded in */
    protected String units;
    /** CDF variable attributes: value indicating missing data */
    protected Double fill_val;
    /** CDF variable attributes: the element that was recorded */
    protected String elem_rec;
    /** the name of the related time stamp array */
    protected String depend_0;
    
    /** the data array */
    protected double data [];
    /** index to the start of the data in the array */
    protected int data_offset;
    /** the length of the data */
    protected int data_length;

    /** add a listener for progress updates
     * @param listener the listener */
    public void addWriteProgressListener (IMCDFWriteProgressListener listener)
    {
        write_progress_listeners.add (listener);
    }
    /** remove a listener for progress updates
     * @param listener the listener */
    public void removeWriteProgressListener (IMCDFWriteProgressListener listener)
    {
        write_progress_listeners.remove(listener);
    }
    /** call the progress listeners
     * @param var_write_count the number of CDF variables written
     * @param n_vars the total number of CDF variables
     * @return true to continue writing, false to stop */
    protected boolean callWriteProgressListeners (int var_write_count, int n_vars)
    {
        Iterator<IMCDFWriteProgressListener> i;
        int percent;
        boolean continue_writing;
        
        i = write_progress_listeners.iterator();
        percent = (var_write_count * 100) / n_vars;
        continue_writing = true;
        while (i.hasNext()) continue_writing &= i.next().percentComplete(percent);
        return continue_writing;
    }

    /** get the name of the variable in the CDF file
     * @return the variable name */
    public String getVarName () { return var_name; }
    /** get the type of variable
     * @return the variable type */
    public IMCDFVariableType getVariableType () { return variable_type; }
    /** get the name of this variable
     * @return the name */
    public String getFieldName () { return field_nam; }
    /** get the valid minimum value for this variable
     * @return the valid minimum */
    public Double getValidMinimum () { return valid_min; }
    /** get the valid maximum value for this variable
     * @return the valid maximum */
    public Double getValidMaximum () { return valid_max; }
    /** get the name of the units that this data is recorded in
     * @return the unit name */
    public String getUnits () { return units; }
    /** get the value corresponding to a missing data sample
     * @return the missing data value */
    public Double getFillValue () { return fill_val; }
    /** get the name of the variable used to hold time stamps for the data
     * @return the variable name */
    public String getDepend0 () { return depend_0; }
    /** get the list of geomagnetic elements recorded
     * @return the orientation */
    public String getElementRecorded () { return elem_rec; }
    
    /** get the data for this variable
     * @return the data */
    public double [] getData () 
    {
        int count;
        double data_subset [];
        
        if (data_offset == 0 && data_length == data.length)
            return data;
        
        data_subset = new double [data_length];
        for (count=0; count<data_length; count++)
            data_subset [count] = data [count + data_offset];
        return data_subset;
    }
    /** get the length of the data for this variable
     * @return the length of data */
    public int getDataLength () { return data_length; }

    /** return true if this variable is a geomagnetic vector data set
     * @return true or false */
    public boolean isVectorGeomagneticData ()
    {
        if (variable_type.getCode() != IMCDFVariableType.VariableTypeCode.GeomagneticFieldElement)
            return false;
        switch (elem_rec.toUpperCase().charAt(0))
        {
            case 'X':
            case 'Y':
            case 'Z':
            case 'H':
            case 'D':
            case 'E':
            case 'V':
            case 'I':
            case 'F':
                return true;
        }
        return false;
    }
    
    /** return true if this variable is a geomagnetic scalar data set
     * @return true or false */
    public boolean isScalarGeomagneticData ()
    {
        if (variable_type.getCode() != IMCDFVariableType.VariableTypeCode.GeomagneticFieldElement)
            return false;
        switch (elem_rec.toUpperCase().charAt(0))
        {
            case 'S':
            case 'G':
                return true;
        }
        return false;
    }

    /** check the metadata for this variable is OK
     * @param accumulated_errors a list of error messages to add to */
    protected void checkMetadata (List<String> accumulated_errors)
    {
        if (variable_type.getCode() == IMCDFVariableType.VariableTypeCode.GeomagneticFieldElement)
        {
            if ((elem_rec.length() != 1) || (! "XYZHDEVIFSG".contains (elem_rec)))
                accumulated_errors.add ("Data array '" + field_nam + "' contains an invalid element code: " + elem_rec);
        }
    }
    
}
