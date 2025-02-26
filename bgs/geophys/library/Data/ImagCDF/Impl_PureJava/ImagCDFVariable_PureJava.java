/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF.Impl_PureJava;

import bgs.geophys.library.Data.ImagCDF.IMCDFException;
import bgs.geophys.library.Data.ImagCDF.IMCDFVariableType;
import bgs.geophys.library.Data.ImagCDF.ImagCDFVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that holds an ImagCDF variable along with it's metadata
 * 
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class ImagCDFVariable_PureJava  extends ImagCDFVariable
{
    
    /** create a ImagCDF variable from the contents of a CDF file
     * @param cdf the open CDF file encapsulated in an ImagCDFLowLevel object
     * @param variable_type the type of data this object should look for in the CDF file
     * @param suffix the suffix for the element name (numbers starting at '1' for temperature elements,
     *               geomagnetic element codes for geomagnetic elements) */
    public ImagCDFVariable_PureJava (ImagCDFLowLevelReader_PureJava cdf, IMCDFVariableType variable_type, String suffix)
    {
        super ();
        
        this.variable_type = variable_type;
        var_name = variable_type.getCDFFileVariableName(suffix);
 
        field_nam = cdf.getVariableAttributeString("FIELDNAM",  var_name);
        valid_min = cdf.getVariableAttributeDouble("VALIDMIN",  var_name);
        valid_max = cdf.getVariableAttributeDouble("VALIDMAX",  var_name);
        units =     cdf.getVariableAttributeString("UNITS",     var_name);
        fill_val =  cdf.getVariableAttributeDouble("FILLVAL",   var_name);
        depend_0 =  cdf.getVariableAttributeString("DEPEND_0",  var_name);
        
        elem_rec = suffix;
        
        data = cdf.getDataArray (var_name);
        data_offset = 0;
        if (data == null)
            cdf.getAccumulatedErrors().add ("Error reading variable " + var_name);
        else
            data_length = data.length;

        super.checkMetadata (cdf.getAccumulatedErrors());
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
     * @throws IMCDFException if the elem_rec or samp_per are invalid */
    public ImagCDFVariable_PureJava (IMCDFVariableType variable_type, String field_nam,
                            double valid_min, double valid_max,
                            String units, double fill_val, String depend_0,
                            String elem_rec, double data [])
    throws IMCDFException
    {
        this (variable_type, field_nam, valid_min, valid_max, units, fill_val, depend_0, elem_rec, data, 0, data.length);
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
     * @throws IMCDFException if the elem_rec or samp_per are invalid */
    public ImagCDFVariable_PureJava (IMCDFVariableType variable_type, String field_nam,
                            double valid_min, double valid_max,
                            String units, double fill_val, String depend_0,
                            String elem_rec, double data [], int data_offset, int data_length)
    throws IMCDFException
    {
        super ();
        
        this.var_name = "";     // var_name is not know yet
        this.variable_type = variable_type;
        this.field_nam = field_nam;
        this.valid_min = valid_min;
        this.valid_max = valid_max;
        this.units = units;
        this.fill_val = fill_val;
        this.depend_0 = depend_0;
        this.elem_rec = elem_rec;
        this.data = data;
        this.data_offset = data_offset;
        this.data_length = data_length;
        
        if (data_offset + data_length > data.length) throw new IllegalArgumentException ("Data length + offset exceed length of data array");
        
        List<String> errors = new ArrayList<> ();
        super.checkMetadata (errors);
        if (! errors.isEmpty())
            throw new IMCDFException (errors.get(0), errors);
    }

    /** write this data to a CDF file
     * @param cdf the CDF file to write into
     * @param suffix the suffix for the element name (numbers starting at '1' for temperature elements,
     *               geomagnetic element codes for geomagnetic elements)
     * @return true if the write completed, false if it was interrupted 
     * @throws IMCDFException if there is an error */
    public boolean write (ImagCDFLowLevelWriter_PureJava cdf, String suffix)
    throws IMCDFException
    {
        var_name = variable_type.getCDFFileVariableName(suffix);

        cdf.createDataVariable(var_name, ImagCDFLowLevelWriter_PureJava.CDFVariableType.Double, fill_val);
        
        cdf.addVariableAttribute ("FIELDNAM",      var_name, field_nam);
        cdf.addVariableAttribute ("VALIDMIN",      var_name, valid_min);
        cdf.addVariableAttribute ("VALIDMAX",      var_name, valid_max);
        cdf.addVariableAttribute ("UNITS",         var_name, units);
        cdf.addVariableAttribute ("FILLVAL",       var_name, fill_val);
        cdf.addVariableAttribute ("DEPEND_0",      var_name, depend_0);
        cdf.addVariableAttribute ("DISPLAY_TYPE",  var_name, "time_series");
        if (isScalarGeomagneticData() || isVectorGeomagneticData())
            cdf.addVariableAttribute ("LABLAXIS",  var_name, suffix);
        else
            cdf.addVariableAttribute ("LABLAXIS",  var_name, "Temperature " + suffix);

        if (! callWriteProgressListeners(0, data_length)) return false;
        cdf.addData (var_name, 0, data, data_offset, data_length);
        if (! callWriteProgressListeners(data_length, data_length)) return false;
        return true;
    }
    

}
