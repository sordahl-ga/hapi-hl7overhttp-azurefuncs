package ca.uhn.hl7v2.sourcegen;

import junit.framework.TestCase;

import org.junit.Test;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.Version;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.v23.datatype.XTN;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.model.v23.segment.PID;
import ca.uhn.hl7v2.model.v231.message.ADT_A34;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.model.v26.message.MDM_T02;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * Check particular corner cases of generated classes
 */
public class CheckParticularsTest extends TestCase {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(CheckParticularsTest.class);

	private void failIfExists(String className) {
		try {
			Class.forName(className);
			fail("Shouldn't exist: " + className);
		} catch (ClassNotFoundException e) {
			// pass
			return;
		}
	}

	/**
	 * This segment is invalid and shouldn't be generated.. The DB contains it
	 * for some reason
	 */
	@Test public void testEdSegmentNewDb() {
		String className = "ca.uhn.hl7v2.model.v251.segment.ED";
		failIfExists(className);
	}

	/**
	 * GetVersion seems to be brittle for some reason.. Maven really wants to
	 * override Velocity's "version" variable
	 */
	@Test public void testGetVersion() {
		ADT_A01 msg = new ADT_A01();
		assertEquals("2.5", msg.getVersion());
	}

	/**
	 * https://sourceforge.net/p/hl7api/bugs/95/
	 */
	@Test public void testOrc14() {
		ca.uhn.hl7v2.model.v23.segment.ORC orc = new ORU_R01().getRESPONSE().getORDER_OBSERVATION().getORC();
		assertEquals(XTN.class, orc.getOrc14_CallBackPhoneNumber(0).getClass());
	}

	/**
	 * This segment is invalid and shouldn't be generated.. The DB contains it
	 * for some reason
	 */
	@Test public void testEdSegmentOldDb() {
		try {
			Class.forName("ca.uhn.hl7v2.model.v231.segment.ED");
			fail();
		} catch (ClassNotFoundException e) {
			// pass
			return;
		}
	}

	@Test public void testMdmMessage() {

		MDM_T02 mdm = new MDM_T02();
		mdm.getOBSERVATION();

	}

	/**
	 * Timing is spelled incorrectly a few times in the DB
	 */
	@Test public void testTimingSpelling() {
		failIfExists("ca.uhn.hl7v2.model.v25.group.OML_O21_TIIMING");
		failIfExists("ca.uhn.hl7v2.model.v25.group.OML_O33_TIIMING");
		failIfExists("ca.uhn.hl7v2.model.v25.group.OML_O35_TIIMING");
		failIfExists("ca.uhn.hl7v2.model.v251.group.OML_O21_TIIMING");
		failIfExists("ca.uhn.hl7v2.model.v251.group.OML_O33_TIIMING");
		failIfExists("ca.uhn.hl7v2.model.v251.group.OML_O35_TIIMING");
		failIfExists("ca.uhn.hl7v2.model.v26.group.OML_O21_TIIMING");
		failIfExists("ca.uhn.hl7v2.model.v26.group.OML_O33_TIIMING");
		failIfExists("ca.uhn.hl7v2.model.v26.group.OML_O35_TIIMING");
	}

	/**
	 * See 3454369. MRG-7 in v2.3 should be XPN
	 */
	@Test public void testv23Mrg7TypeIsXpn() {
		new ca.uhn.hl7v2.model.v23.message.ADT_A06().getMRG().getMrg7_PriorPatientName().getXpn1_FamilyName();
	}

	/**
	 * See 2864817. PID-5 in v2.3 should be repeatable
	 */
	@Test public void testv23Pid5IsRepeatable() {
		ca.uhn.hl7v2.model.v23.message.ADT_A01 a01 = new ca.uhn.hl7v2.model.v23.message.ADT_A01();
		PID pid = a01.getPID();
		pid.getPid5_PatientName(0);
		pid.getPid5_PatientName(1);
		pid.getPatientName(0);
		pid.getPatientName(1);

	}

	/**
	 * The first component of TS should be TSComponentOne in older versions of
	 * HL7
	 * 
	 * @throws DataTypeException
	 */
	@Test public void testTSComponentOne22() throws DataTypeException {
		ca.uhn.hl7v2.model.v22.message.ORU_R01 msg = new ca.uhn.hl7v2.model.v22.message.ORU_R01();
		ca.uhn.hl7v2.model.v22.datatype.TS ts = new ca.uhn.hl7v2.model.v22.datatype.TS(msg);
		Type componentOne = ts.getComponent(0);
		assertEquals(ca.uhn.hl7v2.model.v22.datatype.TSComponentOne.class, componentOne.getClass());

	}

	/**
	 * The first component of TS should be TSComponentOne in older versions of
	 * HL7
	 * 
	 * @throws DataTypeException
	 */
	@Test
	public void testTSComponentOne23() throws DataTypeException {
		ca.uhn.hl7v2.model.v23.message.ORU_R01 msg = new ca.uhn.hl7v2.model.v23.message.ORU_R01();
		ca.uhn.hl7v2.model.v23.datatype.TS ts = new ca.uhn.hl7v2.model.v23.datatype.TS(msg);
		Type componentOne = ts.getComponent(0);
		assertEquals(ca.uhn.hl7v2.model.v23.datatype.TSComponentOne.class, componentOne.getClass());

	}

	/**
	 * The first component of TS should be TSComponentOne in older versions of
	 * HL7
	 * 
	 * @throws DataTypeException
	 */
	@Test
	public void testTSComponentOne231() throws DataTypeException {
		ca.uhn.hl7v2.model.v231.message.ORU_R01 msg = new ca.uhn.hl7v2.model.v231.message.ORU_R01();
		ca.uhn.hl7v2.model.v231.datatype.TS ts = new ca.uhn.hl7v2.model.v231.datatype.TS(msg);
		Type componentOne = ts.getComponent(0);
		assertEquals(ca.uhn.hl7v2.model.v231.datatype.TSComponentOne.class, componentOne.getClass());

	}

	/**
	 * The first component of TS should be TSComponentOne in older versions of
	 * HL7
	 * 
	 * @throws DataTypeException
	 */
	@Test
	public void testTSComponentOne24() throws DataTypeException {
		ca.uhn.hl7v2.model.v24.message.ORU_R01 msg = new ca.uhn.hl7v2.model.v24.message.ORU_R01();
		ca.uhn.hl7v2.model.v24.datatype.TS ts = new ca.uhn.hl7v2.model.v24.datatype.TS(msg);
		Type componentOne = ts.getComponent(0);
		assertEquals(ca.uhn.hl7v2.model.v24.datatype.TSComponentOne.class, componentOne.getClass());

	}

	/**
	 * See bug 3520523
	 */
	@Test
	public void testRSP_K21_QueryResponse_isRepeatable() throws HL7Exception {
		new ca.uhn.hl7v2.model.v25.message.RSP_K21().getQUERY_RESPONSEAll();
		new ca.uhn.hl7v2.model.v251.message.RSP_K21().getQUERY_RESPONSEAll();
		new ca.uhn.hl7v2.model.v26.message.RSP_K21().getQUERY_RESPONSEAll();
	}

	/**
	 * See bug 3538074
	 * 
	 * @author Jure Grom
	 */
	@Test
	public void testParseORL_O34() throws Exception {

		// See bug 3373654
		new ca.uhn.hl7v2.model.v25.message.ORL_O34().getRESPONSE().getPATIENT();

		// Fix overall
		for (Version next : Version.values()) {
			if (next.isGreaterThan(Version.V21)) {
				String message = "MSH|^~\\&|LIS|LAB|HIS|HOSP_SYS|20110329142202||ORL^O34^ORL_O34|90|P|" + next.getVersion() + "\r" + "MSA|AA|12201\r" + "PID|1||32XX77^^^^HIS_PAC||SAMPLE^PATIENT^^^^^L||19981126|M\r"
						+ "SPM|25081299|36201^0310170||K^kri-EDTA|||||||||||||20110329142201.267+0200|||||||||1\r" + "ORC|OK|36201|02000110170|40201|||||20110329142201.312+0200||||||||||||44129-A^^^^^^ORG^^^Redna ambulanta\r" + "TQ1|||||||||R\r"
						+ "OBR||36201|02000110170|020001^K-Hemogram^KC_PEK_LIS_PRE||||||||||||12345^TEST^DOCTOR||||||||O\r" + "SPM|25081299|36201^0310170||K^kri-EDTA|||||||||||||20110329142201.267+0200|||||||||1\r";

				ourLog.debug("Testing ORL^O34 parse for version " + next.getVersion());
				new PipeParser().parse(message);
			}
		}
	}

	/**
	 * https://sourceforge.net/p/hl7api/bugs/67/
	 */
	@Test
	public void testEventMapIn231DoesntOverwriteExistingStructures() throws HL7Exception {

		String testMsg = 
				"MSH|^~\\&|SENDER|IIG|RCV1|IIG|20061123090324||ADT^A34|18879123|P|2.3.1|||AL|NE\r" + 
				"EVN|A34|20061123090324\r" + 
				"PID|||21122112^5^M12||TESTNAME^FIRSTNAME||20020709|M|||EWZ 1^^HALLIN TIROL^^6060^A|A|||D||||3657090702|||||||A\r" + 
				"MRG|220064212^3^M12\r";
		PipeParser parse = new PipeParser();
		Message msg = parse.parse(testMsg);
		assertEquals(ADT_A34.class, msg.getClass());

	}

}
