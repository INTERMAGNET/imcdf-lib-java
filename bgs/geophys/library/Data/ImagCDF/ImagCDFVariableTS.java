
package bgs.geophys.library.Data.ImagCDF;

import bgs.geophys.library.Data.ImagCDF.Impl_PureJava.ImagCDFLowLevelReader_PureJava;
import bgs.geophys.library.Misc.Utils;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/** the base class for ImagCDFVariableTS implementations */
public abstract class ImagCDFVariableTS
{
    // a list of listeners who will recieve "percent complete" notification during writing of data
    private final List<IMCDFWriteProgressListener> write_progress_listeners;

    /** the name of this variable */
    protected String var_name;
    /** the array of time stamps in CDF_TT2000 = nanoseconds since 20000101T000000Z */
    protected long time_stamps [];
    /** the sample period in seconds, set to -1 until calculated */
    protected double sample_period;

    /** create a new ImagCDFVariableTS */
    protected ImagCDFVariableTS ()
    {
        write_progress_listeners = new ArrayList<> ();
        
    }

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
    /** call the listeners for progress updates
     * @param var_write_count the number of variables written
     * @param n_vars the total number of variables written
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

    /** get the array of time stamps that comprise this time stamp variable
     * @return the time stamps
     * @throws IMCDFException if there was an error reading the time stamps */
    public Date [] getTimeStamps () 
    throws IMCDFException
    {
        int count;
        Date dates [];
        
        dates = new Date [time_stamps.length];
        for (count=0; count<dates.length; count++)
            dates [count] = ImagCDFLowLevelReader_PureJava.TT2000ToDate(time_stamps[count]);
        return dates;
    }

    /** get the period between samples
     * @return the sample period
     * @throws IMCDFException if there was an error reading the time stamp data */
    public double getSamplePeriod ()
    throws IMCDFException
    {
        int count;
        long diff, test_diff;

        if (sample_period > 0.0) return sample_period;
        
        if (time_stamps.length < 2) throw new IMCDFException ("Not enough time stamps");
        
        // work through the time stamps checking that the difference between them is the same
        diff = time_stamps [1] - time_stamps [0];
        for (count=2; count<time_stamps.length; count++)
        {
            test_diff = time_stamps [count] - time_stamps [count -1];
            if (test_diff != diff) 
                // leap seconds could occasionally cause this difference to be one second more than expected
                test_diff = (time_stamps [count] - time_stamps [count -1]) - 1000000000;
            if (test_diff != diff) 
                throw new IMCDFException ("Time difference not constant (1st = " + diff + "mS, " + Utils.make_ordinal_number(count) + " = " + test_diff + "mS)");
        }
        sample_period = (double) diff / 1000000000.0;
        
        return sample_period;
    }

    /** get the name of this variable
     * @return the variable name */
    public String getVarName () { return var_name; }
    /** get the first time stamp
     * @return the time stamp
     * @throws IMCDFException if there was an error reading the time stamps */
    public Date getStartDate () throws IMCDFException { return ImagCDFLowLevelReader_PureJava.TT2000ToDate(time_stamps [0]); }
    /** get the number of time stamps
     * @return the number of time stamps */
    public int getNSamples () { return time_stamps.length; }
            
}
