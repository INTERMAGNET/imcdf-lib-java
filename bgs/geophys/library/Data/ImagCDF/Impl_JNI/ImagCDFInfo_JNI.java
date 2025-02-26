/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bgs.geophys.library.Data.ImagCDF.Impl_JNI;

import bgs.geophys.library.Data.ImagCDF.ImagCDFInfo;
import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.Entry;
import gsfc.nssdc.cdf.Variable;
import java.io.File;
import java.util.Vector;

/**
 * A class, separate from the other Imag CDF classes, that gives information
 * about a CDF file's contents. This class directly accesses the CDF reading
 * routines, rather than using the JNI low level reader, so that it completely
 * disconnected from the standard reading process.
 *
 * @author smf
 */
public class ImagCDFInfo_JNI extends ImagCDFInfo {
    
    public ImagCDFInfo_JNI (File file) {
        // open the CDF file
        Vector vars;
        CDF cdf;
        try {
            cdf = CDF.open (file.getAbsolutePath());
            vars = cdf.getVariables();
        } catch (CDFException e) {
            cdf = null;
            vars = new Vector ();
        }
        
        // gather information on its variables
        cdf_var_info = new CDFVariableInfo [vars.size()];
        for (int count=0; count<vars.size(); count++) {
            Variable var = (Variable) vars.get(count);

            String name = var.getName();
            
            int data_length;
            try {
                data_length = (int) var.getNumAllocatedRecords();
            } catch (CDFException e) {
                data_length = -1;
            }
            
            String depend_0;
            try {
                if (cdf == null) {
                    depend_0 = "";
                } else {
                    Object data = cdf.getAttribute ("DEPEND_0").getEntry(var).getData();
                    if (data instanceof String) 
                        depend_0 = (String) data;
                    else
                        depend_0 = "";
                }
            } catch (CDFException e) {
                depend_0 = "";
            }

            cdf_var_info [count] = (new CDFVariableInfo (name, data_length, depend_0));
        }
        
        // get the "ElementsRecorded" global attribute
        try {
            if (cdf == null)
                elements_recorded = "";
            else
            {
                Entry entry = cdf.getAttribute("ElementsRecorded").getEntry(0);
                if (entry == null) 
                    elements_recorded = "";
                else {
                    if (entry.getData () instanceof String) 
                        elements_recorded = (String) entry.getData();
                    else
                        elements_recorded = "";
                }
            }
        } catch (CDFException e) {
            elements_recorded = "";
        }
    }

}
