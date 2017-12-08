/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import gsfc.nssdc.cdf.CDFException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/** 
 * ImagCDFFilename can be used for both creating and parsing valid ImagCDF filenames.
 */
public class ImagCDFFilename 
{

    // Date formatting objects
    private static SimpleDateFormat yyyy;
    private static SimpleDateFormat yyyyMM;
    private static SimpleDateFormat yyyyMMdd;
    private static SimpleDateFormat yyyyMMdd_HH;
    private static SimpleDateFormat yyyyMMdd_HHmm;
    private static SimpleDateFormat yyyyMMdd_HHmmss;
    
    static {
        TimeZone gmtTimeZone = TimeZone.getTimeZone("gmt");

        yyyy = new SimpleDateFormat("yyyy");
        yyyy.setTimeZone(gmtTimeZone);
        yyyyMM = new SimpleDateFormat("yyyyMM");
        yyyyMM.setTimeZone(gmtTimeZone);
        yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
        yyyyMMdd.setTimeZone(gmtTimeZone);
        yyyyMMdd_HH = new SimpleDateFormat("yyyyMMdd_HH");
        yyyyMMdd_HH.setTimeZone(gmtTimeZone);
        yyyyMMdd_HHmm = new SimpleDateFormat("yyyyMMdd_HHmm");
        yyyyMMdd_HHmm.setTimeZone(gmtTimeZone);
        yyyyMMdd_HHmmss = new SimpleDateFormat("yyyyMMdd_HHmmss");
        yyyyMMdd_HHmmss.setTimeZone(gmtTimeZone);
    }
    
    private String filename;
    private String observatoryCode;
    private Date date;
    private IMCDFPublicationLevel publicationLevel;
    private Interval interval;

    public enum Interval {
        UNKNOWN, ANNUAL, MONTHLY, DAILY, HOURLY, MINUTE, SECOND
    }
    
    public enum Case {
        UPPER, LOWER
    }
    
    /** Default constructor
    */
    ImagCDFFilename() {
        filename = "";
        observatoryCode = "";
        date = new Date();
        publicationLevel = new IMCDFPublicationLevel (IMCDFPublicationLevel.PublicationLevel.LEVEL_1);    
        interval = Interval.UNKNOWN;
    }
    
    /** Creates a new instance of ImagCDFFilename */
    public ImagCDFFilename (String observatoryCode, Date date, IMCDFPublicationLevel publicationLevel,
                            Interval interval, Case characterCase)
    {
        constructFilename (observatoryCode, date, publicationLevel, interval, characterCase);
    }
    
    /** Creates a new ImagCDFFilename instance by asking the sub class to parse a filename */
    public ImagCDFFilename(String filename) throws ParseException {
        parseFilename(filename);
    }
    
    /** Creates a new instance of Iaga2002Filename */
    public ImagCDFFilename (ImagCDF imag_cdf, Case characterCase) throws ParseException {
        try { 
            String observatoryCode = imag_cdf.getIagaCode();
            ImagCDFVariableTS ts = imag_cdf.findVectorTimeStamps();
            Date date = ts.getStartDate();
            IMCDFPublicationLevel publicationLevel = imag_cdf.getPublicationLevel();
            double sample_period = ts.getSamplePeriod(); 
            if (sample_period == 1.0) interval = Interval.SECOND;
            else if (sample_period == 60.0) interval = Interval.MINUTE;
            else if (sample_period == 3600.0) interval = Interval.HOURLY;
            else if (sample_period == 86400.0) interval = Interval.DAILY;
            else if (sample_period == 2419400.0 || sample_period == 2505600.0 || sample_period == 2592000) interval = Interval.MONTHLY;
            else if (sample_period == 31536000.0 || sample_period == 31622400.0) interval = Interval.ANNUAL;
            else interval = Interval.UNKNOWN;
            constructFilename(observatoryCode, date, publicationLevel, interval, characterCase);
        }
        catch (CDFException e) { throw new ParseException ("Can't find sample period from ImagCDF data", -1); }
    }
    
    // Property accessors
    public String getFilename()                         { return filename; }
    public String getObservatoryCode()                  { return observatoryCode; }
    public Date getDate()                               { return date; }
    public IMCDFPublicationLevel getPublicationLevel()  { return publicationLevel; }
    public Interval getInterval()                       { return interval; }

    @Override
    public String toString() 
    {
        return getFilename();
    }
    
    

    
    private void constructFilename (String observatoryCode, Date date, IMCDFPublicationLevel publication_level,
                                            Interval interval, Case characterCase)
    {
         // Pass the arguments to the superclass. Exception will be passed up if any validation fails
        this.observatoryCode = validateObservatoryCode(observatoryCode).toUpperCase();
        this.date = date;
        this.publicationLevel = publication_level;
        this.interval = interval;
        this.filename = generateFilename(characterCase);
    }
    
    /** Parses a filename, updating the properties accordingly. */
    private void parseFilename(String filename) throws ParseException {
        String observatoryCode;
        Date date;
        IMCDFPublicationLevel publicationLevel;
        Interval interval;
        try {
            switch (filename.length())
            {
                case 18:
                    date = yyyy.parse (filename.substring(4, 8));
                    interval = Interval.ANNUAL;
                    publicationLevel = new IMCDFPublicationLevel (filename.substring (14, 15));
                    break;
                case 20:
                    date = yyyyMM.parse (filename.substring(4, 10));
                    interval = Interval.MONTHLY;
                    publicationLevel = new IMCDFPublicationLevel (filename.substring (16, 17));
                    break;
                case 22:
                    date = yyyyMMdd.parse (filename.substring(4, 12));
                    interval = Interval.DAILY;
                    publicationLevel = new IMCDFPublicationLevel (filename.substring (18, 19));
                    break;
                case 26:
                    date = yyyyMMdd_HH.parse (filename.substring(4, 15));
                    interval = Interval.HOURLY;
                    publicationLevel = new IMCDFPublicationLevel (filename.substring (21, 22));
                    break;
                case 28:
                    date = yyyyMMdd_HHmm.parse (filename.substring(4, 17));
                    interval = Interval.MINUTE;
                    publicationLevel = new IMCDFPublicationLevel (filename.substring (23, 24));
                    break;
                case 30:
                    date = yyyyMMdd_HHmmss.parse (filename.substring(4, 19));
                    interval = Interval.SECOND;
                    publicationLevel = new IMCDFPublicationLevel (filename.substring (25, 26));
                    break;
                default:
                    throw new ParseException("", 0);
            }
            if (! filename.substring(filename.length() -4).equalsIgnoreCase(".cdf"))
                throw new ParseException("", 0);
            observatoryCode = filename.substring(0,3);
        } catch (ParseException e) {
            throw new ParseException("Filename: " + filename + " malformatted.", 0);
        } catch (IllegalArgumentException e) {
            throw new ParseException("Filename: " + filename + " malformatted.", 0);
        } catch (CDFException e) {
            throw new ParseException("Filename: " + filename + " malformatted.", 0);
        }
        
        constructFilename(observatoryCode, date, publicationLevel, interval, Case.LOWER);

    }
    
    
    /** Creates a valid ImagCDF filename based on the properties of the class */
    private String generateFilename(Case characterCase) {
        SimpleDateFormat dateFormat;
        String cadence, filename;
        
        switch(this.getInterval()){
            case ANNUAL:  dateFormat = yyyy; cadence = "P1Y"; break;
            case MONTHLY: dateFormat = yyyyMM; cadence = "P1M"; break;
            case DAILY:   dateFormat = yyyyMMdd; cadence = "P1D"; break;
            case HOURLY:  dateFormat = yyyyMMdd_HH; cadence = "PT1H"; break;
            case MINUTE:  dateFormat = yyyyMMdd_HHmm; cadence = "PT1M"; break;
            case SECOND:  dateFormat = yyyyMMdd_HHmmss; cadence = "PT1S"; break;
            default:      dateFormat = yyyyMMdd_HHmmss; cadence = "UNKN"; break;
        }
        
        filename = this.getObservatoryCode()
                    + "_"
                    + dateFormat.format(this.getDate())
                    + "_"
                    + cadence
                    + "_"
                    + publicationLevel.toString()
                    + ".cdf";
        
        switch(characterCase) {
            case UPPER: return filename.toUpperCase();
            case LOWER: return filename.toLowerCase();
        }
        return filename;
    }

    // Constructor argument validation
    private String validateObservatoryCode(String observatoryCode) {
        if (observatoryCode.length() != 3) {
            throw new IllegalArgumentException("Invalid observatoryCode argument. Must be exactly three characters.");
        }
        return observatoryCode;
    }
    
}

