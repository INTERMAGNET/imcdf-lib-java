/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bgs.geophys.library.Data.ImagCDF.Impl_PureJava;

import bgs.geophys.library.Data.ImagCDF.IMCDFException;
import bgs.geophys.library.Data.ImagCDF.ImagCDFInfo;
import gov.nasa.gsfc.spdf.cdfj.AttributeEntry;
import gov.nasa.gsfc.spdf.cdfj.CDFException;
import gov.nasa.gsfc.spdf.cdfj.CDFReader;
import java.io.File;
import java.util.Vector;

/**
 * A class, separate from the other Imag CDF classes, that gives information
 * about a CDF file's contents. This class directly accesses the CDF reading
 * routines, rather than using the Pure Java low level reader, so that it completely
 * disconnected from the standard reading process.

 * @author smf
 */
public class ImagCDFInfo_PureJava extends ImagCDFInfo {
    
    public ImagCDFInfo_PureJava (File file) {
        // open the CDF file
        CDFReader cdf_reader;
        String var_names [];
        try
        {
            cdf_reader = new CDFReader (file.getAbsolutePath());
            var_names = cdf_reader.getVariableNames();
        }
        catch (CDFException.ReaderError e)
        {
            cdf_reader = null;
            var_names = new String [0];
        }
        
        // gather information on its variables
        cdf_var_info = new CDFVariableInfo [var_names.length];
        for (int count=0; count<var_names.length; count++) {
            int data_length;
            try {
                data_length = (int) cdf_reader.getNumberOfValues (var_names[count]);
            } catch (CDFException.ReaderError ex) {
                data_length = -1;
            }

            String depend_0;
            Vector<AttributeEntry> var_entries;
            try {
                var_entries = cdf_reader.getAttributeEntries(var_names[count], "DEPEND_0");
            } catch (CDFException.ReaderError ex) {
                var_entries = null;
            }
            if (var_entries == null)
                depend_0 = "";
            else if (var_entries.isEmpty())
                depend_0 = "";
            else {
                AttributeEntry var_entry = var_entries.get(0);
                if (var_entry.getValue() instanceof String) 
                    depend_0 = (String) var_entry.getValue();
                else
                    depend_0 = "";
            }
            
            cdf_var_info [count] = new CDFVariableInfo (var_names[count], data_length, depend_0);
        }
        
        // get the "ElementsRecorded" global attribute
        try {
            Vector<AttributeEntry> entries;
            if (cdf_reader == null)
                entries = new Vector ();
            else
                entries = cdf_reader.getAttributeEntries ("ElementsRecorded");
            AttributeEntry entry;
            if (entries.size() >= 1)
                entry = entries.get(0);
            else
                entry = null;
            if (entry == null)
                elements_recorded = "";
            else if (! "ElementsRecorded".equals(entry.getAttributeName())) 
                elements_recorded = "";
            else if (entry.getValue() == null)
                elements_recorded = "";
            else if (entry.getValue() instanceof String)
                elements_recorded = (String) entry.getValue();
            else
                elements_recorded = "";
        } catch (CDFException.ReaderError ex) {
            elements_recorded = "";
        }
    }
}
