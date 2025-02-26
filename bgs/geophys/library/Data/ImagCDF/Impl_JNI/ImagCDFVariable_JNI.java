/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF.Impl_JNI;

import bgs.geophys.library.Data.ImagCDF.IMCDFException;
import bgs.geophys.library.Data.ImagCDF.IMCDFVariableType;
import bgs.geophys.library.Data.ImagCDF.ImagCDFVariable;
import gsfc.nssdc.cdf.Variable;
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
public class ImagCDFVariable_JNI extends ImagCDFVariable
{
    
    /** create a ImagCDF variable from the contents of a CDF file
     * @param cdf the open CDF file encapsulated in an ImagCDFLowLevel object
     * @param variable_type the type of data this object should look for in the CDF file
     * @param suffix the suffix for the element name (numbers starting at '1' for temperature elements,
     *               geomagnetic element codes for geomagnetic elements) */
    public ImagCDFVariable_JNI (ImagCDFLowLevel_JNI cdf, IMCDFVariableType variable_type, String suffix)
    {
        super ();
        
        this.variable_type = variable_type;
        Variable var = cdf.getVariable (variable_type.getCDFFileVariableName(suffix));
        
        if (var == null)
            var_name = "";
        else
            var_name = var.getName();
        field_nam = cdf.getVariableAttributeString("FIELDNAM",  var);
        valid_min = cdf.getVariableAttributeDouble("VALIDMIN",  var);
        valid_max = cdf.getVariableAttributeDouble("VALIDMAX",  var);
        units =     cdf.getVariableAttributeString("UNITS",     var);
        fill_val =  cdf.getVariableAttributeDouble("FILLVAL",   var);
        depend_0 =  cdf.getVariableAttributeString("DEPEND_0",  var);
        
        elem_rec = suffix;
        
        data = cdf.getDataArray (var);
        data_offset = 0;
        if (data == null)
            data_length = 0;
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
    public ImagCDFVariable_JNI (IMCDFVariableType variable_type, String field_nam,
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
    public ImagCDFVariable_JNI (IMCDFVariableType variable_type, String field_nam,
                                double valid_min, double valid_max,
                                String units, double fill_val, String depend_0,
                                String elem_rec, double data [], int data_offset, int data_length)
    throws IMCDFException
    {
        super ();
        
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
    public boolean write (ImagCDFLowLevel_JNI cdf, String suffix)
    throws IMCDFException
    {
        Variable var;
        
        var = cdf.createDataVariable (variable_type.getCDFFileVariableName(suffix), ImagCDFLowLevel_JNI.CDFVariableType.Double);

        cdf.addVariableAttribute ("FIELDNAM",      var, field_nam);
        cdf.addVariableAttribute ("VALIDMIN",      var, valid_min);
        cdf.addVariableAttribute ("VALIDMAX",      var, valid_max);
        cdf.addVariableAttribute ("UNITS",         var, units);
        cdf.addVariableAttribute ("FILLVAL",       var, fill_val);
        cdf.addVariableAttribute ("DEPEND_0",      var, depend_0);
        cdf.addVariableAttribute ("DISPLAY_TYPE",  var, "time_series");
        if (isScalarGeomagneticData() || isVectorGeomagneticData())
            cdf.addVariableAttribute ("LABLAXIS",  var, suffix);
        else
            cdf.addVariableAttribute ("LABLAXIS",  var, "Temperature " + suffix);
        
        if (! callWriteProgressListeners(0, data_length)) return false;
        cdf.addData (var, 0, data, data_offset, data_length);
        if (! callWriteProgressListeners(data_length, data_length)) return false;
        return true;
    }
    
    
}
