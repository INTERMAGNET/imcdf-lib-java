/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.Variable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A class that holds an ImagCDF variable along with it's metadata
 * 
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class ImagCDFVariable 
{
    
    // a list of listeners who will recieve "percent complete" notification during writing of data
    private List<IMCDFWriteProgressListener> write_progress_listeners;
    
    // private member data - the type of data that this object holds
    private IMCDFVariableType variable_type;
    
    // private member data - the CDF variable attributes
    private String field_nam;
    private double valid_min;
    private double valid_max;
    private String units;
    private double fill_val;
    private String elem_rec;
    
    // private member data - the data array
    private double data [];
    private int data_offset;
    private int data_length;
    
    /** create a ImagCDF variable from the contents of a CDF file
     * @param cdf the open CDF file encapsulated in an ImagCDFLowLevel object
     * @param variable_type the type of data this object should look for in the CDF file
     * @param suffix the suffix for the element name (numbers starting at '1' for temperature elements,
     *               geomagnetic element codes for geomagnetic elements)
     * @throws CDFException if there is a problem reading the data */
    public ImagCDFVariable (ImagCDFLowLevel cdf, IMCDFVariableType variable_type, String suffix)
    throws CDFException
    {
        Variable var;

        write_progress_listeners = new ArrayList<> ();
        
        this.variable_type = variable_type;
        var = cdf.getVariable (variable_type.getCDFFileVariableName(suffix));
        
        field_nam =                             cdf.getVariableAttributeString("FIELDNAM",  var);
        valid_min =                             cdf.getVariableAttributeDouble("VALIDMIN",  var);
        valid_max =                             cdf.getVariableAttributeDouble("VALIDMAX",  var);
        units =                                 cdf.getVariableAttributeString("UNITS",     var);
        fill_val =                              cdf.getVariableAttributeDouble("FILLVAL",   var);
        
        elem_rec = suffix;
        
        data = ImagCDFLowLevel.getDataArray (var);
        data_offset = 0;
        data_length = data.length;

        checkMetadata ();
    }

    /** create an ImagCDFVariable from data and metadata for subsequent writing to a file
     * @param variable_type the type of variable - geomagnetic element or temperature
     * @param field_nam set the "Geomagnetic Field Element " and a number or
     *                  "Temperature" and a number
     * @param valid_min the smallest possible value that the data can take
     * @param valid_max the largest possible value that the data can take
     * @param units name of the units that the data is in
     * @param fill_val the value that, when present in the data, shows that the data point was not recorded
     * @param elem_rec for geomagnetic data the element that this data represents.
     *                 for temperature data the name of the location where temperature was recorded
     * @param data the data as an array
     * @throws CDFException if the elem_rec or samp_per are invalid */
    public ImagCDFVariable (IMCDFVariableType variable_type, String field_nam,
                            double valid_min, double valid_max,
                            String units, double fill_val,
                            String elem_rec, double data [])
    throws CDFException
    {
        this (variable_type, field_nam, valid_min, valid_max, units, fill_val, elem_rec, data, 0, data.length);
    }
    
    /** create an ImagCDFVariable from data and metadata for subsequent writing to a file
     * @param variable_type the type of variable - geomagnetic element or temperature
     * @param field_nam set the "Geomagnetic Field Element " and a number or
     *                  "Temperature" and a number
     * @param valid_min the smallest possible value that the data can take
     * @param valid_max the largest possible value that the data can take
     * @param units name of the units that the data is in
     * @param fill_val the value that, when present in the data, shows that the data point was not recorded
     * @param elem_rec for geomagnetic data the element that this data represents.
     *                 for temperature data the name of the location where temperature was recorded
     * @param data the data as an array
     * @param data_offset index into the data array to start writing from
     * @param data_length number of samples of data to write
     * @throws CDFException if the elem_rec or samp_per are invalid */
    public ImagCDFVariable (IMCDFVariableType variable_type, String field_nam,
                            double valid_min, double valid_max,
                            String units, double fill_val,
                            String elem_rec, double data [], int data_offset, int data_length)
    throws CDFException
    {
        write_progress_listeners = new ArrayList<> ();
        
        this.variable_type = variable_type;
        this.field_nam = field_nam;
        this.valid_min = valid_min;
        this.valid_max = valid_max;
        this.units = units;
        this.fill_val = fill_val;
        this.elem_rec = elem_rec;
        this.data = data;
        this.data_offset = data_offset;
        this.data_length = data_length;
        
        if (data_offset + data_length > data.length) throw new IllegalArgumentException ("Data length + offset exceed length of data array");
        
        checkMetadata ();
    }

    public void addWriteProgressListener (IMCDFWriteProgressListener listener)
    {
        write_progress_listeners.add (listener);
    }
    public void removeWriteProgressListener (IMCDFWriteProgressListener listener)
    {
        write_progress_listeners.remove(listener);
    }
    private boolean callWriteProgressListeners (int var_write_count, int n_vars)
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

    /** write this data to a CDF file
     * @param cdf the CDF file to write into
     * @param suffix the suffix for the element name (numbers starting at '1' for temperature elements,
     *               geomagnetic element codes for geomagnetic elements)
     * @return true if the write completed, false if it was interrupted 
     * @throws CDFException if there is an error */
    public boolean write (ImagCDFLowLevel cdf, String suffix)
    throws CDFException
    {
        int count;
        Variable var;
        
        var = cdf.createDataVariable (variable_type.getCDFFileVariableName(suffix), ImagCDFLowLevel.CDFVariableType.Double);

        cdf.addVariableAttribute ("FIELDNAM",      var, field_nam);
        cdf.addVariableAttribute ("VALIDMIN",      var, new Double (valid_min));
        cdf.addVariableAttribute ("VALIDMAX",      var, new Double (valid_max));
        cdf.addVariableAttribute ("UNITS",         var, units);
        cdf.addVariableAttribute ("FILLVAL",       var, new Double (fill_val));
        if (isVectorGeomagneticData())
            cdf.addVariableAttribute("DEPEND_0",   var, IMCDFVariableType.VectorTimeStampsVarName);
        else if (isScalarGeomagneticData())
            cdf.addVariableAttribute("DEPEND_0",   var, IMCDFVariableType.ScalarTimeStampsVarName);
        else
            cdf.addVariableAttribute("DEPEND_0",   var, IMCDFVariableType.getTemperatureTimeStampsVarName(suffix));
        cdf.addVariableAttribute ("DISPLAY_TYPE",  var, "time_series");
        if (isScalarGeomagneticData() || isVectorGeomagneticData())
            cdf.addVariableAttribute ("LABLAXIS",  var, suffix);
        else
            cdf.addVariableAttribute ("LABLAXIS",  var, "Temperature " + suffix);
        
        if (! callWriteProgressListeners(0, data_length)) return false;
        cdf.addData (var, 0, data, data_offset, data_length);
//        for (count=0; count<data_length; count++)
//        {
//            cdf.addData (var, count, data[count + data_offset]);
//            // don't send progress every sample or we'll slow right down!
//            if ((count % 500) == 0)
//            {
//                if (! callWriteProgressListeners(count, data_length)) 
//                {
//                    return false;
//                }
//            }
//        }
        if (! callWriteProgressListeners(data_length, data_length)) return false;
        return true;
    }
    
    public IMCDFVariableType getVariableType () { return variable_type; }
    public String getFieldName () { return field_nam; }
    public double getValidMinimum () { return valid_min; }
    public double getValidMaximum () { return valid_max; }
    public String getUnits () { return units; }
    public double getFillValue () { return fill_val; }
    public String getElementRecorded () { return elem_rec; }
    
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
    public int getDataLength () { return data_length; }

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
    
    private void checkMetadata ()
    throws CDFException
    {
        if (variable_type.getCode() == IMCDFVariableType.VariableTypeCode.GeomagneticFieldElement)
        {
            if (elem_rec.length() != 1 || "XYZHDEVIFSG".indexOf (elem_rec) < 0)
                throw new CDFException ("Data array '" + field_nam + "' contains an invalid element code: " + elem_rec);
        }
    }
    
}
