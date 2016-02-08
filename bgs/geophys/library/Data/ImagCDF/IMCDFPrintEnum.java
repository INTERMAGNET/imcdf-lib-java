/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCDF;

/**
 * An interface that allows objects that contain an enumeration that needs
 * to be formatted as a string to declare that this is the case - this
 * allows null objects to be trapped without a NullPinterException:
 *      object.toString() - throws NullPointerException if object is null
 * 
 * There are no methods needed to implement the interface,
 * just add it to an object that wants to be used in this way
 * 
 * @author smf
 */
public interface IMCDFPrintEnum 
{
    
}
