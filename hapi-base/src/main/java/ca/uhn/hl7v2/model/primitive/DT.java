/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "DT.java".  Description:
 * "Note: The class description below has been excerpted from the Hl7 2.3.0 documentation"
 *
 * The Initial Developer of the Original Code is University Health Network. Copyright (C)
 * 2005.  All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * GNU General Public License (the "GPL"), in which case the provisions of the GPL are
 * applicable instead of those above.  If you wish to allow use of your version of this
 * file only under the terms of the GPL and not to allow others to use your version
 * of this file under the MPL, indicate your decision by deleting  the provisions above
 * and replace  them with the notice and other provisions required by the GPL License.
 * If you do not delete the provisions above, a recipient may use your version of
 * this file under either the MPL or the GPL.
 */
package ca.uhn.hl7v2.model.primitive;

import ca.uhn.hl7v2.model.AbstractPrimitive;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;

/**
 * Represents an HL7 DT (date) datatype.   
 * 
 * @author <a href="mailto:neal.acharya@uhn.on.ca">Neal Acharya</a>
 * @author <a href="mailto:bryan.tripp@uhn.on.ca">Bryan Tripp</a>
 * @version $Revision: 1.1 $ updated on $Date: 2007-02-19 02:24:51 $ by $Author: jamesagnew $
 */
@SuppressWarnings("serial")
public abstract class DT extends AbstractPrimitive {

    private CommonDT myDetail;
    
    /**
     * @param theMessage message to which this Type belongs
     */
    public DT(Message theMessage) {
        super(theMessage);
    }
    
    private CommonDT getDetail() throws DataTypeException {
        if (myDetail == null) {
            myDetail = new CommonDT(getValue());
        }
        return myDetail;
    }
    
    /**
     * @see AbstractPrimitive#setValue(java.lang.String)
     * @throws DataTypeException if the value is incorrectly formatted and either validation is 
     *      enabled for this primitive or detail setters / getters have been called, forcing further
     *      parsing.   
     */
    public void setValue(String theValue) throws DataTypeException {
        super.setValue(theValue);
        
        if (myDetail != null) {
            myDetail.setValue(theValue);
        }
    }
    
    /**
     * @see AbstractPrimitive#getValue
     */
    public String getValue() {
        String result = super.getValue();
        
        if (myDetail != null) {
            result = myDetail.getValue();
        }
        
        return result;
    }

    /**
     * @see CommonDT#setYearPrecision(int)
     * @throws DataTypeException if the value is incorrectly formatted.  If validation is enabled, this 
     *      exception should be thrown at setValue(), but if not, detailed parsing may be deferred until 
     *      this method is called.  
     */
    public void setYearPrecision(int yr) throws DataTypeException {
        getDetail().setYearPrecision(yr);       
    }
    
    /**
     * @see CommonDT#setYearMonthPrecision(int, int)
     * @throws DataTypeException if the value is incorrectly formatted.  If validation is enabled, this 
     *      exception should be thrown at setValue(), but if not, detailed parsing may be deferred until 
     *      this method is called.  
     */
    public void setYearMonthPrecision(int yr, int mnth) throws DataTypeException {
        getDetail().setYearMonthPrecision(yr,mnth);         
    }
    
    /**
     * @see CommonDT#setYearMonthDayPrecision(int, int, int)
     * @throws DataTypeException if the value is incorrectly formatted.  If validation is enabled, this 
     *      exception should be thrown at setValue(), but if not, detailed parsing may be deferred until 
     *      this method is called.  
     */
    public void setYearMonthDayPrecision(int yr, int mnth, int dy) throws DataTypeException {
        getDetail().setYearMonthDayPrecision(yr,mnth,dy);        
    }
    
    /**
     * Returns the year as an integer.
     * @throws DataTypeException if the value is incorrectly formatted.  If validation is enabled, this 
     *      exception should be thrown at setValue(), but if not, detailed parsing may be deferred until 
     *      this method is called.  
     */
    public int getYear() throws DataTypeException {
        return getDetail().getYear();
    }
    
    /**
     * Returns the month as an integer.
     * @throws DataTypeException if the value is incorrectly formatted.  If validation is enabled, this 
     *      exception should be thrown at setValue(), but if not, detailed parsing may be deferred until 
     *      this method is called.  
     */
    public int getMonth() throws DataTypeException {
        return getDetail().getMonth();
    }
    
    /**
     * Returns the day as an integer.
     * @throws DataTypeException if the value is incorrectly formatted.  If validation is enabled, this 
     *      exception should be thrown at setValue(), but if not, detailed parsing may be deferred until 
     *      this method is called.  
     */
    public int getDay() throws DataTypeException {
        return getDetail().getDay();
    }
}
