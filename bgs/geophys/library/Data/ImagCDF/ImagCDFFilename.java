/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import bgs.geophys.library.Data.ImagCDF.ImagCDFFactory;

/** 
 * ImagCDFFilename can be used for both creating and parsing valid ImagCDF filenames.
 */
public class ImagCDFFilename 
{

    // Date formatting objects
    private static final SimpleDateFormat YYYY;
    private static final SimpleDateFormat YYYYMM;
    private static final SimpleDateFormat YYYYMMDD;
    private static final SimpleDateFormat YYYYMMDD_HH;
    private static final SimpleDateFormat YYYYMMDD_HHMM;
    private static final SimpleDateFormat YYYYMMDD_HHMMSS;
    
    static {
        YYYY = new SimpleDateFormat("yyyy");
        ImagCDFFactory.fixSimpleDateFormat(YYYY);
        YYYYMM = new SimpleDateFormat("yyyyMM");
        ImagCDFFactory.fixSimpleDateFormat(YYYYMM);
        YYYYMMDD = new SimpleDateFormat("yyyyMMdd");
        ImagCDFFactory.fixSimpleDateFormat(YYYYMMDD);
        YYYYMMDD_HH = new SimpleDateFormat("yyyyMMdd_HH");
        ImagCDFFactory.fixSimpleDateFormat(YYYYMMDD_HH);
        YYYYMMDD_HHMM = new SimpleDateFormat("yyyyMMdd_HHmm");
        ImagCDFFactory.fixSimpleDateFormat(YYYYMMDD_HHMM);
        YYYYMMDD_HHMMSS = new SimpleDateFormat("yyyyMMdd_HHmmss");
        ImagCDFFactory.fixSimpleDateFormat(YYYYMMDD_HHMMSS);
    }
    
    private String filename;
    private String observatoryCode;
    private Date date;
    private IMCDFPublicationLevel publicationLevel;
    private Interval cadence;
    private Interval coverage;

    /** an enum describing both the cadence and the coverage of the data */
    public enum Interval {
        /** not known */
        UNKNOWN, 
        /** annual mean */
        ANNUAL, 
        /** monthly mean */
        MONTHLY, 
        /** daily mean */
        DAILY, 
        /** hourly mean */
        HOURLY, 
        /** minute mean */
        MINUTE, 
        /** 1-second data */
        SECOND
    }
    
    /** an enum listing the possible cases for the filename */
    public enum Case {
        /** upper case */
        UPPER, 
        /** lower case */
        LOWER
    }
    
    /** Default constructor
    */
    public ImagCDFFilename() {
        filename = "";
        observatoryCode = "";
        date = new Date();
        publicationLevel = new IMCDFPublicationLevel (IMCDFPublicationLevel.PublicationLevel.LEVEL_1);    
        coverage = cadence = Interval.UNKNOWN;
    }
    
    /** Creates a new instance of ImagCDFFilename
     * 
     * @param observatoryCode the IAGA code
     * @param date date/time for the first data sample
     * @param publicationLevel the data type (reported, adjusted, ...)
     * @param cadence the cadence
     * @param coverage the coverage
     * @param characterCase upper or lower
     */
    public ImagCDFFilename (String observatoryCode, Date date, IMCDFPublicationLevel publicationLevel,
                            Interval cadence, Interval coverage, Case characterCase)
    {
        constructFilename (observatoryCode, date, publicationLevel, cadence, coverage, characterCase);
    }
    
    /** Creates a new ImagCDFFilename instance by asking the sub class to parse a filename
     * 
     * @param filename the filename to copy from
     * @throws ParseException if the filename could not be parsed
     */
    public ImagCDFFilename(String filename) throws ParseException {
        parseFilename(filename);
    }
    
    /** Creates a new instance of ImagCDFFilename
     * 
     * @param imag_cdf the data to construct a filename for
     * @param characterCase upper or lower
     * @throws ParseException if there was a problem with the data
     */
    public ImagCDFFilename (ImagCDF imag_cdf, Case characterCase) throws ParseException {
        try { 
            String obsy_code = imag_cdf.getIagaCode();
            ImagCDFVariableTS ts = imag_cdf.findVectorTimeStamps();
            Date dt = ts.getStartDate();
            GregorianCalendar cal = new GregorianCalendar (ImagCDFFactory.gmtTimeZone);
            cal.setTime (dt);
            IMCDFPublicationLevel pub_level = imag_cdf.getPublicationLevel();
            double samp_per = ts.getSamplePeriod(); 
            int n_samples = -1;
            int vector_indices [] = imag_cdf.findVectorElements();
            if (vector_indices != null) {
                ImagCDFVariable var = imag_cdf.getElement (vector_indices[0]);
                n_samples = var.getDataLength();
            }
            coverage = cadence = Interval.UNKNOWN;
            int samples_in_year = -1, samples_in_month = -1, samples_in_day = -1, samples_in_hour = -1, samples_in_minute = -1, samples_in_second = -1;
            switch ((int) samp_per) {
                case 1:
                    samples_in_year = ImagCDFFactory.daysInYear (cal.get (GregorianCalendar.YEAR)) * ImagCDFFactory.SECONDS_PER_DAY;
                    samples_in_month = ImagCDFFactory.daysInMonth (cal.get (GregorianCalendar.MONTH), cal.get (GregorianCalendar.YEAR)) * ImagCDFFactory.SECONDS_PER_DAY;
                    samples_in_day = ImagCDFFactory.SECONDS_PER_DAY;
                    samples_in_hour = ImagCDFFactory.SECONDS_PER_HOUR;
                    samples_in_minute = ImagCDFFactory.SECONDS_PER_MINUTE;
                    samples_in_second = 1;
                    cadence = Interval.SECOND;
                    break;
                case 60:
                    samples_in_year = ImagCDFFactory.daysInYear (cal.get (GregorianCalendar.YEAR)) * ImagCDFFactory.MINUTES_PER_DAY;
                    samples_in_month = ImagCDFFactory.daysInMonth (cal.get (GregorianCalendar.MONTH), cal.get (GregorianCalendar.YEAR)) * ImagCDFFactory.MINUTES_PER_DAY;
                    samples_in_day = ImagCDFFactory.MINUTES_PER_DAY;
                    samples_in_hour = ImagCDFFactory.MINUTES_PER_HOUR;
                    samples_in_minute = 1;
                    samples_in_second = -1;
                    cadence = Interval.MINUTE;
                    break;
                case 3600:
                    samples_in_year = ImagCDFFactory.daysInYear (cal.get (GregorianCalendar.YEAR)) * ImagCDFFactory.HOURS_PER_DAY;
                    samples_in_month = ImagCDFFactory.daysInMonth (cal.get (GregorianCalendar.MONTH), cal.get (GregorianCalendar.YEAR)) * ImagCDFFactory.HOURS_PER_DAY;
                    samples_in_day = ImagCDFFactory.HOURS_PER_DAY;
                    samples_in_hour = 1;
                    samples_in_minute = -1;
                    samples_in_second = -1;
                    cadence = Interval.HOURLY;
                    break;
                case 86400:
                    samples_in_year = ImagCDFFactory.daysInYear (cal.get (GregorianCalendar.YEAR));
                    samples_in_month = ImagCDFFactory.daysInMonth (cal.get (GregorianCalendar.MONTH), cal.get (GregorianCalendar.YEAR));
                    samples_in_day = 1;
                    samples_in_hour = -1;
                    samples_in_minute = -1;
                    samples_in_second = -1;
                    cadence = Interval.DAILY;
                    break;
            }
            if (cadence != Interval.UNKNOWN) {
                if (cal.get (GregorianCalendar.MONTH) == 0 && cal.get (GregorianCalendar.DAY_OF_MONTH) == 1 &&
                    cal.get (GregorianCalendar.HOUR) == 0 && cal.get (GregorianCalendar.MINUTE) == 0 &&
                    cal.get (GregorianCalendar.SECOND) == 0 && cal.get (GregorianCalendar.MILLISECOND) == 0 &&
                    n_samples == samples_in_year)
                    coverage = Interval.ANNUAL;
                else if (cal.get (GregorianCalendar.DAY_OF_MONTH) == 1 &&
                         cal.get (GregorianCalendar.HOUR) == 0 && cal.get (GregorianCalendar.MINUTE) == 0 &&
                         cal.get (GregorianCalendar.SECOND) == 0 && cal.get (GregorianCalendar.MILLISECOND) == 0 &&
                         n_samples == samples_in_month)
                    coverage = Interval.MONTHLY;
                else if (cal.get (GregorianCalendar.HOUR) == 0 && cal.get (GregorianCalendar.MINUTE) == 0 &&
                         cal.get (GregorianCalendar.SECOND) == 0 && cal.get (GregorianCalendar.MILLISECOND) == 0 &&
                         n_samples == samples_in_day)
                    coverage = Interval.DAILY;
                else if (cal.get (GregorianCalendar.MINUTE) == 0 &&
                         cal.get (GregorianCalendar.SECOND) == 0 && cal.get (GregorianCalendar.MILLISECOND) == 0 &&
                         n_samples == samples_in_hour)
                    coverage = Interval.HOURLY;
                else if (cal.get (GregorianCalendar.SECOND) == 0 && cal.get (GregorianCalendar.MILLISECOND) == 0 &&
                         n_samples == samples_in_minute)
                    coverage = Interval.MINUTE;
                else if (cal.get (GregorianCalendar.MILLISECOND) == 0 &&
                         n_samples == samples_in_second)
                    coverage = Interval.SECOND;
            }
            constructFilename(obsy_code, dt, pub_level, cadence, coverage, characterCase);
        }
        catch (IMCDFException e) 
        {
            if (e.getMessage() != null)
                throw new ParseException ("Error creating ImagCDF filename: " + e.getMessage(), -1);
            throw new ParseException ("Error creating ImagCDF filename", -1); 
        }
    }
    
    /** get the filename
     * @return the filename */
    public String getFilename()                         { return filename; }
    /** get the IAGA observatory code
     * @return the IAGA code */
    public String getObservatoryCode()                  { return observatoryCode; }
    /** get the date of the start of the data
     * @return the date */
    public Date getDate()                               { return date; }
    /** get the publication level (AKA data type)
     * @return the publication level */
    public IMCDFPublicationLevel getPublicationLevel()  { return publicationLevel; }
    /** get the data cadence
     * @return the cadence */
    public Interval getCadence()                        { return cadence; }
    /** get the data coverage
     * @return the coverage */
    public Interval getCoverage()                       { return coverage; }

    /** convert this filename to a string
     * @return the filename */
    @Override
    public String toString() 
    {
        return getFilename();
    }
    
    

    
    private void constructFilename (String observatoryCode, Date date, IMCDFPublicationLevel publication_level,
                                    Interval cadence, Interval coverage, Case characterCase)
    {
         // Pass the arguments to the superclass. Exception will be passed up if any validation fails
        this.observatoryCode = validateObservatoryCode(observatoryCode).toUpperCase();
        this.date = date;
        this.publicationLevel = publication_level;
        this.cadence = cadence;
        this.coverage = coverage;
        this.filename = generateFilename(characterCase);
    }
    
    /** Parses a filename, updating the properties accordingly. */
    private void parseFilename(String filename) throws ParseException {
        String obsy_code;
        Date dt;
        IMCDFPublicationLevel pub_level;
        Interval local_coverage, local_cadence;
        String parts [] = filename.split ("_");
        try {
            switch (parts.length) {
                case 4:
                    switch (parts[1].length()) {
                        case 4:
                            dt = YYYY.parse (parts[1]);
                            local_coverage = Interval.ANNUAL;
                            break;
                        case 6:
                            dt = YYYYMM.parse (parts[1]);
                            local_coverage = Interval.MONTHLY;
                            break;
                        case 8:
                            dt = YYYYMMDD.parse (parts[1]);
                            local_coverage = Interval.DAILY;
                            break;
                        default:
                            throw new ParseException ("", 0);
                    }
                    break;
                case 5:
                    String date_time = parts[1] + "_" + parts[2];
                    switch (date_time.length()) {
                        case 11:
                            dt = YYYYMMDD_HH.parse (date_time);
                            local_coverage = Interval.HOURLY;
                            break;
                        case 13:
                            dt = YYYYMMDD_HHMM.parse (date_time);
                            local_coverage = Interval.MINUTE;
                            break;
                        case 15:
                            dt = YYYYMMDD_HHMMSS.parse (date_time);
                            local_coverage = Interval.SECOND;
                            break;
                        default:
                            throw new ParseException ("", 0);
                    }
                    break;
                default:
                    throw new ParseException ("", 0);
            }
            obsy_code = parts[0];
            local_cadence = parseCadence(parts[parts.length -2]);
            pub_level = new IMCDFPublicationLevel (parts[parts.length -1].substring (0,1));
            if (! filename.substring(filename.length() -4).equalsIgnoreCase(".cdf"))
                throw new ParseException("", 0);
        } catch (ParseException | IllegalArgumentException | IMCDFException | IndexOutOfBoundsException e) {
            throw new ParseException("Filename: " + filename + " malformatted.", 0);
        }
        
        constructFilename(obsy_code, dt, pub_level, local_cadence, local_coverage, Case.LOWER);

    }
    
    
    /** Creates a valid ImagCDF filename based on the properties of the class */
    private String generateFilename(Case characterCase) {
        SimpleDateFormat dateFormat;
        String local_cadence, fname;
        
        switch(this.getCadence()){
            case ANNUAL:  local_cadence = "P1Y"; break;
            case MONTHLY: local_cadence = "P1M"; break;
            case DAILY:   local_cadence = "P1D"; break;
            case HOURLY:  local_cadence = "PT1H"; break;
            case MINUTE:  local_cadence = "PT1M"; break;
            case SECOND:  local_cadence = "PT1S"; break;
            default:      local_cadence = "UNKN"; break;
        }

        switch(this.getCoverage()){
            case ANNUAL:  dateFormat = YYYY; break;
            case MONTHLY: dateFormat = YYYYMM; break;
            case DAILY:   dateFormat = YYYYMMDD; break;
            case HOURLY:  dateFormat = YYYYMMDD_HH; break;
            case MINUTE:  dateFormat = YYYYMMDD_HHMM; break;
            case SECOND:  dateFormat = YYYYMMDD_HHMMSS; break;
            default:      dateFormat = YYYYMMDD_HHMMSS; break;
        }
        
        fname = this.getObservatoryCode()
                    + "_"
                    + dateFormat.format(this.getDate())
                    + "_"
                    + local_cadence
                    + "_"
                    + publicationLevel.toString()
                    + ".cdf";
        
        switch(characterCase) {
            case UPPER: return fname.toUpperCase();
            case LOWER: return fname.toLowerCase();
        }
        return fname;
    }

    // Constructor argument validation
    private String validateObservatoryCode(String observatoryCode) {
        if (observatoryCode.length() != 3) {
            throw new IllegalArgumentException("Invalid observatoryCode argument. Must be exactly three characters.");
        }
        return observatoryCode;
    }
    
    private Interval parseCadence (String cadence_str) {
        if ("P1Y".equalsIgnoreCase(cadence_str)) return Interval.ANNUAL;
        if ("P1M".equalsIgnoreCase(cadence_str)) return Interval.MONTHLY;
        if ("P1D".equalsIgnoreCase(cadence_str)) return Interval.DAILY;
        if ("PT1H".equalsIgnoreCase(cadence_str)) return Interval.HOURLY;
        if ("PT1M".equalsIgnoreCase(cadence_str)) return Interval.MINUTE;
        if ("PT1S".equalsIgnoreCase(cadence_str)) return Interval.SECOND;
        return Interval.UNKNOWN;
    }

}
