/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bgs.geophys.library.Data.ImagCDF;

/**
 * A class, separate from the other Imag CDF classes, that gives information
 * about a CDF file's contents. The class should be overridden with a sub-class
 * that fills the class variables.
 * 
 * @author smf
 */
public class ImagCDFInfo {
    
    /** class used to hold information about an individual variable in a CDF files */
    public class CDFVariableInfo
    {
        private final String var_name;
        private final int data_length;
        private final String depend_0;
        public CDFVariableInfo (String var_name, int data_length, String depend_0)
        {
            this.var_name = var_name;
            this.data_length = data_length;
            this.depend_0 = depend_0;
        }
        public String getVarName () { return var_name; }
        public int getDataLength () { return data_length; }
        public String getDepend0 () { return depend_0; }
    }

    /** an array of information on the variables in the CDF file */
    protected CDFVariableInfo cdf_var_info [];
    /** contents of the global attribute "ElementsRecorded" */
    protected String elements_recorded;
    
    /** create an empty ImagCDFInfo */
    public ImagCDFInfo () {
        cdf_var_info = new CDFVariableInfo [0];
        elements_recorded = "";
    }
    
    public boolean isCDFVarInfoEmpty () { return (cdf_var_info.length == 0); }
    public int getNCDFVars () { return cdf_var_info.length; }
    public CDFVariableInfo getCDFVar (int index) { return cdf_var_info[index]; }
    public String getElementsRecorded () { return elements_recorded; }
    
}
