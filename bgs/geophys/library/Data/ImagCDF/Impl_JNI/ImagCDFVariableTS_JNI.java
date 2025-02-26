/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF.Impl_JNI;

import bgs.geophys.library.Data.ImagCDF.IMCDFException;
import bgs.geophys.library.Data.ImagCDF.ImagCDFVariableTS;
import gsfc.nssdc.cdf.Variable;
import java.util.Date;

/**
 * A class that holds the time stamps for the individual data points in an ImagCDFVariable
 * object
 * 
 * @author smf
 */
public class ImagCDFVariableTS_JNI extends ImagCDFVariableTS
{

    /** create an ImagCDF variable time stamp series from the contents of a CDF file
     * @param cdf the open CDF file encapsulated in an ImagCDFLowLevel object
     * @param var_name the CDF variable name */
    public ImagCDFVariableTS_JNI (ImagCDFLowLevel_JNI cdf, String var_name)
    {
        super ();
        
        this.var_name = var_name;
        Variable var = cdf.getVariable (var_name);

        time_stamps = cdf.getTimeStampArray (var);
        sample_period = -1.0;
    }

    /** create an ImagCDF variable time stamp series from a start date, sample period
     * and duration
     * @param start_date the date/time stamp of the first sample
     * @param samp_per the sample period (time between samples) in seconds
     * @param n_samples the number of samples
     * @param var_name the CDF variable name
     * @throws IMCDFException if there was an error with the CDF */
    public ImagCDFVariableTS_JNI (Date start_date, double samp_per, int n_samples, String var_name)
    throws IMCDFException
    {
        super ();
        
        this.var_name = var_name;
        time_stamps = new long [n_samples];
        
        for (int count=0; count<n_samples; count++)
        {
            long time_in_millis = start_date.getTime() + (long) ((samp_per * 1000.0 * (double) count) + 0.5);
            time_stamps [count] = ImagCDFLowLevel_JNI.DateToTT2000(time_in_millis);
        }
        
        sample_period = -1.0;
    }
    
    /** create an ImagCDF variable time stamp series from a set of dates
     * @param dates an array of dates corresponding to each time stamp
     * @param var_name the CDF variable name
     * @throws IMCDFException if there was an error with the CDF */
    public ImagCDFVariableTS_JNI (Date dates [], String var_name)
    throws IMCDFException
    {
        int count;
        
        this.var_name = var_name;
        time_stamps = new long [dates.length];
        for (count=0; count<time_stamps.length; count++)
            time_stamps [count] = ImagCDFLowLevel_JNI.DateToTT2000 (dates[count]);

        sample_period = -1.0;
    }
    
    /** write this data to a CDF file
     * @param cdf the CDF file to write into
     * @return true if the write completed, false if it was interrupted 
     * @throws IMCDFException if there is an error */
    public boolean write (ImagCDFLowLevel_JNI cdf)
    throws IMCDFException
    {
        int count;
        Variable var;
        
        var = cdf.createDataVariable (var_name, ImagCDFLowLevel_JNI.CDFVariableType.TT2000);

        if (! callWriteProgressListeners(0, time_stamps.length)) return false;
        cdf.addTimeStamp (var, 0, time_stamps);
        if (! callWriteProgressListeners(time_stamps.length, time_stamps.length)) return false;
        return true;
    }   
    
}
