/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.Variable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A class that holds the time stamps for the individual data points in an ImagCDFVariable
 * object
 * 
 * @author smf
 */
public class ImagCDFVariableTS 
{

    // a list of listeners who will recieve "percent complete" notification during writing of data
    private List<IMCDFWriteProgressListener> write_progress_listeners;
    
    private String var_name;
    private long time_stamps [];        // in CDF_TT2000 = nanoseconds since 20000101T000000Z
    private double sample_period;       // set to -1 until calculated - in seconds
    
    /** create an ImagCDF variable time stamp series from the contents of a CDF file
     * @param cdf the open CDF file encapsulated in an ImagCDFLowLevel object
     * @param var_name the CDF variable name
     * @throws CDFException if there is a problem reading the data */
    public ImagCDFVariableTS (ImagCDFLowLevel cdf, String var_name)
    throws CDFException
    {
        Variable var;

        write_progress_listeners = new ArrayList<> ();
        
        this.var_name = var_name;
        var = cdf.getVariable (var_name);
        time_stamps = ImagCDFLowLevel.getTimeStampArray (var);
        
        sample_period = -1.0;
    }

    /** create an ImagCDF variable time stamp series from a start date, sample period
     * and duration
     * @param start_date the date/time stamp of the first sample
     * @param samp_per the sample period (time between samples) in seconds
     * @param n_samples the number of samples
     * @param var_name the CDF variable name */
    public ImagCDFVariableTS (Date start_date, double samp_per, int n_samples, String var_name)
    throws CDFException
    {
        int count;
        long time_in_millis;
        
        write_progress_listeners = new ArrayList<> ();
        
        this.var_name = var_name;
        time_stamps = new long [n_samples];
        
        for (count=0; count<n_samples; count++)
        {
            time_in_millis = start_date.getTime() + (long) ((samp_per * 1000.0 * (double) count) + 0.5);
            time_stamps [count] = ImagCDFLowLevel.DateToTT2000(time_in_millis);
        }
        
        sample_period = -1.0;
    }
    
    /** create an ImagCDF variable time stamp series from a set of dates
     * @param dates an array of dates corresponding to each time stamp
     * @param var_name the CDF variable name */
    public ImagCDFVariableTS (Date dates [], String var_name)
    throws CDFException
    {
        int count;
        
        this.var_name = var_name;
        time_stamps = new long [dates.length];
        for (count=0; count<time_stamps.length; count++)
            time_stamps [count] = ImagCDFLowLevel.DateToTT2000 (dates[count]);

        sample_period = -1.0;
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
     * @return true if the write completed, false if it was interrupted 
     * @throws CDFException if there is an error */
    public boolean write (ImagCDFLowLevel cdf)
    throws CDFException
    {
        int count;
        Variable var;
        
        var = cdf.createDataVariable (var_name, ImagCDFLowLevel.CDFVariableType.TT2000);

        if (! callWriteProgressListeners(0, time_stamps.length)) return false;
        cdf.addTimeStamp (var, 0, time_stamps);
//        for (count=0; count<time_stamps.length; count++)
//        {
//            cdf.addTimeStamp (var, count, time_stamps[count]);
//            // don't send progress every sample or we'll slow right down!
//            if ((count % 500) == 0)
//            {
//                if (! callWriteProgressListeners(count, time_stamps.length)) 
//                {
//                    return false;
//                }
//            }
//        }
        if (! callWriteProgressListeners(time_stamps.length, time_stamps.length)) return false;
        return true;
    }   
    
    public Date [] getTimeStamps ()
    {
        int count;
        Date dates [];
        
        dates = new Date [time_stamps.length];
        for (count=0; count<dates.length; count++)
            dates [count] = ImagCDFLowLevel.TT2000ToDate(time_stamps[count]);
        return dates;
    }
    
    public double getSamplePeriod ()
    throws CDFException
    {
        int count;
        long diff, test_diff;

        if (sample_period > 0.0) return sample_period;
        
        if (time_stamps.length < 2) throw new CDFException ("Not enough time stamps");
        
        // work through the time stamps checking that the difference between them is the same
        diff = time_stamps [1] - time_stamps [0];
        for (count=2; count<time_stamps.length; count++)
        {
            test_diff = time_stamps [count] - time_stamps [count -1];
            if (test_diff != diff) throw new CDFException ("Time difference not constant");
        }
        sample_period = (double) diff / 1000000000.0;
        
        return sample_period;
    }

    public String getVarName () { return var_name; }
    public Date getStartDate () { return ImagCDFLowLevel.TT2000ToDate(time_stamps [0]); }
    public int getNSamples () { return time_stamps.length; }
            
    
}
