/**
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License.
 *
 * The Original Code is "CommonDTTest.java".  Description:
 * "Unit test class for ca.uhn.hl7v2.model.primitive.tests.CommonDT"
 *
 * The Initial Developer of the Original Code is Leslie Mann. Copyright (C)
 * 2002.  All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * GNU General Public License (the  �GPL�), in which case the provisions of the GPL are
 * applicable instead of those above.  If you wish to allow use of your version of this
 * file only under the terms of the GPL and not to allow others to use your version
 * of this file under the MPL, indicate your decision by deleting  the provisions above
 * and replace  them with the notice and other provisions required by the GPL License.
 * If you do not delete the provisions above, a recipient may use your version of
 * this file under either the MPL or the GPL.
 *
 */
package ca.uhn.hl7v2.model.primitive.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.frameworks;
import static org.ops4j.pax.exam.CoreOptions.knopflerfish;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.logProfile;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.primitive.CommonDT;

/**
 * Unit test class for ca.uhn.hl7v2.model.primitive.tests.CommonDT
 * 
 * @author Leslie Mann
 * @author Niranjan Sharma niranjan.sharma@med.ge.com This testcase has been
 *         extended for OSGI environment using Junit4 and PAX-Exam.
 */
@RunWith(JUnit4TestRunner.class)
public class CommonDTTest {
    
    // you get that because you "installed" a log profile in configuration.
    public Log logger = LogFactory.getLog(CommonDTTest.class);
    
    @Inject
    BundleContext bundleContext;
    
    @Configuration
    public static Option[] configure() {
	return options(frameworks(equinox(), felix(), knopflerfish())
		, logProfile()
		, systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO")
		, mavenBundle().groupId("org.ops4j.pax.url").artifactId("pax-url-mvn").version("0.4.0")
		, wrappedBundle(mavenBundle().groupId("org.ops4j.base").artifactId("ops4j-base-util").version("0.5.3"))
		, mavenBundle().groupId("ca.uhn.hapi").artifactId("hapi-osgi-base").version("1.0-beta1")
//		, vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5006" )


	);
    } 
    
    private int year;
    private int month;
    private int day;
    private String dateString;
    private CommonDT commonDT;
    
    @Before
    public void BeforeTheTest() {
	year = 2002;
	month = 2;
	day = 24;
	dateString = "20020224";
	commonDT = new CommonDT();
    }
    
    /*
     * ********************************************************* Test Cases
     * *********************************************************
     */

    /**
     * Testing default constructor
     */
    @Test
    public void testConstructor() {
	assertNotNull("Should have a valid CommonDT object", commonDT);
    }
    
    /**
     * Testing string constructor with a HL7 date string.
     */
    @Test
    public void testStringConstructor() throws DataTypeException {
	commonDT = new CommonDT(dateString);
	assertNotNull("Should have a valid CommonDT object", commonDT);
    }
    
    /**
     * Testing string constructor with delete value "".
     */
    @Test
    public void testStringConstructor2() throws DataTypeException {
	commonDT = new CommonDT("\"\"");
	assertNotNull("Should have a valid CommonDT object", commonDT);
    }
    
    /**
     * Testing set/getValue() with various date strings
     */
    @Test
    public void testSetGetValue() throws DataTypeException {
	class TestSpec {
	    String dateString;
	    Object outcome;
	    Exception exception = null;
	    
	    TestSpec(String dateString, Object outcome) {
		this.dateString = dateString;
		this.outcome = outcome;
	    }
	    
	    public String toString() {
		return "[ " + dateString + " : " + (outcome != null ? outcome : "null") + (exception != null ? " [ " + exception.toString() + " ]" : " ]");
	    }
	    
	    public boolean executeTest() {
		CommonDT dt = new CommonDT();
		try {
		    dt.setValue(dateString);
		    String retval = dt.getValue();
		    if (retval != null) {
			return retval.equals(outcome);
		    } else {
			return outcome == null;
		    }
		} catch (Exception e) {
		    if (e.getClass().equals(outcome)) {
			return true;
		    } else {
			this.exception = e;
			return (e.getClass().equals(outcome));
		    }
		}
	    }
	}// inner class
	
	TestSpec[] tests = { new TestSpec(null, null), new TestSpec("", ""), new TestSpec("\"\"", "\"\""), new TestSpec(" ", DataTypeException.class), new TestSpec("2", DataTypeException.class), new TestSpec("20", DataTypeException.class), new TestSpec("200", DataTypeException.class), new TestSpec("999", DataTypeException.class),
	// year
		new TestSpec("1000", "1000"), new TestSpec("1987", "1987"), new TestSpec("2002", "2002"), new TestSpec("9999", "9999"), new TestSpec("10000", DataTypeException.class),
		// month
		new TestSpec("20021", DataTypeException.class), new TestSpec("200200", DataTypeException.class), new TestSpec("200201", "200201"), new TestSpec("200207", "200207"), new TestSpec("200211", "200211"), new TestSpec("200212", "200212"), new TestSpec("200213", DataTypeException.class),
		// day
		new TestSpec("2002010", DataTypeException.class), new TestSpec("20020100", DataTypeException.class), new TestSpec("20020101", "20020101"), new TestSpec("20020107", "20020107"), new TestSpec("20020121", "20020121"), new TestSpec("20020131", "20020131"), new TestSpec("20020132", DataTypeException.class), };
	
	ArrayList failedTests = new ArrayList();
	
	for (int i = 0; i < tests.length; i++) {
	    if (!tests[i].executeTest())
		failedTests.add(tests[i]);
	}
	
	assertEquals("Failures: " + failedTests, 0, failedTests.size());
    }
    
    /**
     * Testing setYearPrecision() with various year values
     */
    @Test
    public void testSetYearPrecision() throws DataTypeException {
	class TestSpec {
	    int year;
	    Object outcome;
	    Exception exception = null;
	    
	    TestSpec(int year, Object outcome) {
		this.year = year;
		this.outcome = outcome;
	    }
	    
	    public String toString() {
		return "[ " + Integer.toString(year) + " : " + (outcome != null ? outcome : "null") + (exception != null ? " [ " + exception.toString() + " ]" : " ]");
	    }
	    
	    public boolean executeTest() {
		CommonDT dt = new CommonDT();
		try {
		    dt.setYearPrecision(year);
		    String retval = dt.getValue();
		    if (retval != null) {
			return retval.equals(outcome);
		    } else {
			return outcome == null;
		    }
		} catch (Exception e) {
		    if (e.getClass().equals(outcome)) {
			return true;
		    } else {
			this.exception = e;
			return (e.getClass().equals(outcome));
		    }
		}
	    }
	}// inner class
	
	TestSpec[] tests = { new TestSpec(-2000, DataTypeException.class), new TestSpec(9, DataTypeException.class), new TestSpec(99, DataTypeException.class), new TestSpec(999, DataTypeException.class), new TestSpec(1000, "1000"), new TestSpec(1987, "1987"), new TestSpec(2001, "2001"), new TestSpec(9999, "9999"), new TestSpec(10000, DataTypeException.class), };
	
	ArrayList failedTests = new ArrayList();
	
	for (int i = 0; i < tests.length; i++) {
	    if (!tests[i].executeTest())
		failedTests.add(tests[i]);
	}
	
	assertEquals("Failures: " + failedTests, 0, failedTests.size());
    }
    
    /**
     * Testing setYearMonthPrecision() with various year, month values
     */
    @Test
    public void testSetYearMonthPrecision() throws DataTypeException {
	class TestSpec {
	    int year;
	    int month;
	    Object outcome;
	    Exception exception = null;
	    
	    TestSpec(int year, int month, Object outcome) {
		this.year = year;
		this.month = month;
		this.outcome = outcome;
	    }
	    
	    public String toString() {
		return "[ " + Integer.toString(year) + " " + Integer.toString(month) + " : " + (outcome != null ? outcome : "null") + (exception != null ? " [ " + exception.toString() + " ]" : " ]");
	    }
	    
	    public boolean executeTest() {
		CommonDT dt = new CommonDT();
		try {
		    dt.setYearMonthPrecision(year, month);
		    String retval = dt.getValue();
		    if (retval != null) {
			return retval.equals(outcome);
		    } else {
			return outcome == null;
		    }
		} catch (Exception e) {
		    if (e.getClass().equals(outcome)) {
			return true;
		    } else {
			this.exception = e;
			return (e.getClass().equals(outcome));
		    }
		}
	    }
	}// inner class
	
	TestSpec[] tests = { new TestSpec(2001, -1, DataTypeException.class), new TestSpec(9, 1, DataTypeException.class), new TestSpec(99, 1, DataTypeException.class), new TestSpec(999, 1, DataTypeException.class), new TestSpec(2001, 0, DataTypeException.class), new TestSpec(2001, 1, "200101"), new TestSpec(1000, 1, "100001"), new TestSpec(1987, 1, "198701"), new TestSpec(2001, 1, "200101"), new TestSpec(2001, 7, "200107"), new TestSpec(2001, 12, "200112"), new TestSpec(2001, 13, DataTypeException.class), new TestSpec(9999, 1, "999901"), new TestSpec(10000, 1, DataTypeException.class), };
	
	ArrayList failedTests = new ArrayList();
	
	for (int i = 0; i < tests.length; i++) {
	    if (!tests[i].executeTest())
		failedTests.add(tests[i]);
	}
	
	assertEquals("Failures: " + failedTests, 0, failedTests.size());
    }
    
    /**
     * Testing setYearMonthDayPrecision() with various year, month, day values
     */
    @Test
    public void testSetYearMonthDayPrecision() throws DataTypeException {
	class TestSpec {
	    int year;
	    int month;
	    int day;
	    Object outcome;
	    Exception exception = null;
	    
	    TestSpec(int year, int month, int day, Object outcome) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.outcome = outcome;
	    }
	    
	    public String toString() {
		return "[ " + Integer.toString(year) + " " + Integer.toString(month) + " : " + Integer.toString(day) + " : " + (outcome != null ? outcome : "null") + (exception != null ? " [ " + exception.toString() + " ]" : " ]");
	    }
	    
	    public boolean executeTest() {
		CommonDT dt = new CommonDT();
		try {
		    dt.setYearMonthDayPrecision(year, month, day);
		    String retval = dt.getValue();
		    if (retval != null) {
			return retval.equals(outcome);
		    } else {
			return outcome == null;
		    }
		} catch (Exception e) {
		    if (e.getClass().equals(outcome)) {
			return true;
		    } else {
			this.exception = e;
			return (e.getClass().equals(outcome));
		    }
		}
	    }
	}// inner class
	
	TestSpec[] tests = { new TestSpec(2001, 1, -1, DataTypeException.class), new TestSpec(9, 1, 1, DataTypeException.class), new TestSpec(99, 1, 1, DataTypeException.class), new TestSpec(999, 1, 1, DataTypeException.class), new TestSpec(2001, 1, 0, DataTypeException.class), new TestSpec(2001, 1, 1, "20010101"), new TestSpec(1000, 1, 1, "10000101"), new TestSpec(1987, 1, 1, "19870101"), new TestSpec(2001, 1, 1, "20010101"), new TestSpec(2001, 1, 7, "20010107"), new TestSpec(2001, 1, 12, "20010112"), new TestSpec(2001, 1, 25, "20010125"), new TestSpec(2001, 1, 31, "20010131"), new TestSpec(2001, 1, 32, DataTypeException.class), new TestSpec(9999, 1, 1, "99990101"), new TestSpec(10000, 1, 1, DataTypeException.class), new TestSpec(2001, 1, 1, "20010101"),
		new TestSpec(2001, -1, 21, DataTypeException.class), };
	
	ArrayList failedTests = new ArrayList();
	
	for (int i = 0; i < tests.length; i++) {
	    if (!tests[i].executeTest())
		failedTests.add(tests[i]);
	}
	
	assertEquals("Failures: " + failedTests, 0, failedTests.size());
    }
    
    /**
     * Testing toHl7DTFormat() with a calendar
     */
    @Test
    public void testConvertCalToHL7DateFormat() throws DataTypeException {
	GregorianCalendar cal = new GregorianCalendar();
	cal.clear();
	cal.setLenient(false);
	cal.set(2002, 5, 24);
	String convertedDate = CommonDT.toHl7DTFormat(cal);
	assertEquals("20020624", convertedDate);
    }
    
    /**
     * Testing ability to retrieve year value
     */
    @Test
    public void testGetYear() throws DataTypeException {
	commonDT = new CommonDT(dateString);
	assertEquals("Should get year back", year, commonDT.getYear());
    }
    
    /**
     * Testing ability to retrieve month value
     */
    @Test
    public void testGetMonth() throws DataTypeException {
	commonDT = new CommonDT(dateString);
	assertEquals("Should get month back", month, commonDT.getMonth());
    }
    
    /**
     * Testing ability to retrieve day value
     */
    @Test
    public void testGetDay() throws DataTypeException {
	commonDT = new CommonDT(dateString);
	assertEquals("Should get day back", day, commonDT.getDay());
    }
}
