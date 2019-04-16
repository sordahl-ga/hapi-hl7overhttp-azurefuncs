
/*
 The contents of this file are subject to the Mozilla Public License Version 1.1
 (the "License"); you may not use this file except in compliance with the License.
 You may obtain a copy of the License at http://www.mozilla.org/MPL/
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 specific language governing rights and limitations under the License.

 The Original Code is "AbstractHL7Exception.java".  Description:
 "Exception containing a Location and Error Code"

 The Initial Developer of the Original Code is University Health Network. Copyright (C)
 2012.  All Rights Reserved.

 Contributor(s): ______________________________________.

 Alternatively, the contents of this file may be used under the terms of the
 GNU General Public License (the "GPL"), in which case the provisions of the GPL are
 applicable instead of those above.  If you wish to allow use of your version of this
 file only under the terms of the GPL and not to allow others to use your version
 of this file under the MPL, indicate your decision by deleting  the provisions above
 and replace  them with the notice and other provisions required by the GPL License.
 If you do not delete the provisions above, a recipient may use your version of
 this file under either the MPL or the GPL.
 */

package ca.uhn.hl7v2;

import static ca.uhn.hl7v2.Version.V25;
import static ca.uhn.hl7v2.Version.versionOf;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.util.Terser;

/**
 * Abstract base class for Exceptions that are able to create acknowledgement
 * messages from their error code and location information
 *
 * @author Christian Ohr
 */
@SuppressWarnings("serial")
public abstract class AbstractHL7Exception extends Exception {

    private Location location;
    private ErrorCode errorCode = ErrorCode.APPLICATION_INTERNAL_ERROR;
    private Severity severity = Severity.ERROR;
    private Message responseMessage;


    public AbstractHL7Exception() {
        super();
    }

    public AbstractHL7Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public AbstractHL7Exception(String message) {
        super(message);
    }

    public AbstractHL7Exception(Throwable cause) {
        super(cause);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setFieldPosition(int pos) {
        if (location == null)
            location = new Location();
        location.withField(pos);
    }

    public void setSegmentName(String segmentName) {
        if (location == null)
            location = new Location();
        location.withSegmentName(segmentName);
    }

    public void setSegmentRepetition(int segmentRepetition) {
        if (location == null)
            location = new Location();
        location.withSegmentRepetition(segmentRepetition);
    }

    public int getErrorCode() {
        return errorCode.getCode();
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = ErrorCode.errorCodeFor(errorCode);
    }

    public ErrorCode getError() {
        return errorCode;
    }

    public void setError(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Message getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(Message responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * Populates the generated response based on this exception.
     *
     * @param emptyResponse empty response message
     * @param acknowledgmentCode the acknowledgement code
     * @return response message
     * @throws HL7Exception if populating the response fails
     */
    public Message populateResponse(Message emptyResponse, AcknowledgmentCode acknowledgmentCode, int repetition)
            throws HL7Exception {
        if (responseMessage == null) {
            if (acknowledgmentCode == null) acknowledgmentCode = AcknowledgmentCode.AA;
            if (V25.isGreaterThan(versionOf(emptyResponse.getVersion()))) {
                responseMessage = populateResponseBefore25(emptyResponse, acknowledgmentCode, repetition);
            } else {
                responseMessage = populateResponseAsOf25(emptyResponse, acknowledgmentCode, repetition);
            }
        }
        return responseMessage;
    }

    /**
     * Fill segments for HL7 versions 2.5 or newer.
     * <p/>
     * HL7 versions before 2.5 require to set MSA-1. The ERR segment has various fields (ERR-2,
     * ERR-3) containing details about the exception. ERR-1 is marked as obsolete.
     *
     * @param response           the raw response message
     * @param acknowledgmentCode acknowledgmentCode
     * @param repetition         repetition of the ERR segment that shall be populated
     * @throws HL7Exception
     */
    private Message populateResponseAsOf25(Message response, AcknowledgmentCode acknowledgmentCode,
                                           int repetition) throws HL7Exception {
        // TODO define what should happen if there is no MSA or ERR
        Segment msa = (Segment) response.get("MSA");
        Terser.set(msa, 1, 0, 1, 1, acknowledgmentCode.name());
        Segment err = (Segment) response.get("ERR", repetition);
        if (location != null) {
            if (location.getSegmentName() != null)
                Terser.set(err, 2, 0, 1, 1, location.getSegmentName());
            if (location.getSegmentRepetition() > 0)
                Terser.set(err, 2, 0, 2, 1, Integer.toString(location.getSegmentRepetition()));
            if (location.getField() > 0)
                Terser.set(err, 2, 0, 3, 1, Integer.toString(location.getField()));
            if (location.getFieldRepetition() > 0)
                Terser.set(err, 2, 0, 4, 1, Integer.toString(location.getFieldRepetition()));
            if (location.getComponent() > 0)
                Terser.set(err, 2, 0, 5, 1, Integer.toString(location.getComponent()));
            if (location.getSubcomponent() > 0)
                Terser.set(err, 2, 0, 6, 1, Integer.toString(location.getSubcomponent()));
        }
        Terser.set(err, 3, 0, 1, 1, Integer.toString(errorCode.getCode()));
        Terser.set(err, 3, 0, 2, 1, errorCode.getMessage());
        Terser.set(err, 3, 0, 3, 1, ErrorCode.codeTable());
        Terser.set(err, 3, 0, 9, 1, getMessage());
        Terser.set(err, 4, 0, 1, 1, "E");
        return response;
    }

    /**
     * Fill segments for HL7 versions before 2.5.
     * <p/>
     * HL7 versions before 2.5 require to set MSA-1 and MSA-3. The ERR segment only has one
     * repeatable field (ERR-1) with components containing details about the exception.
     *
     * @param response           the raw response message
     * @param acknowledgmentCode acknowledgment code
     * @param repetition         repetition of the ERR segment that shall be popualted
     * @throws HL7Exception
     */
    private Message populateResponseBefore25(Message response, AcknowledgmentCode acknowledgmentCode,
                                             int repetition) throws HL7Exception {
        // TODO define what should happen if there is no MSA or ERR
        Segment msa = (Segment) response.get("MSA");
        Terser.set(msa, 1, 0, 1, 1, acknowledgmentCode.name());
        Terser.set(msa, 3, 0, 1, 1, errorCode.getMessage());
        Segment err = (Segment) response.get("ERR");
        if (location != null) {
            if (location.getSegmentName() != null)
                Terser.set(err, 1, repetition, 1, 1, location.getSegmentName());
            if (location.getField() > 0)
                Terser.set(err, 1, repetition, 3, 1, Integer.toString(location.getField()));
        }
        Terser.set(err, 1, repetition, 4, 1, Integer.toString(errorCode.getCode()));
        Terser.set(err, 1, repetition, 4, 2, errorCode.getMessage());
        Terser.set(err, 1, repetition, 4, 3, ErrorCode.codeTable());
        Terser.set(err, 1, repetition, 4, 5, getMessage());
        return response;
    }

    public String getMessage() {
        String message = getMessageWithoutLocation();
        StringBuilder msg = new StringBuilder(message);
        if (getLocation() != null && !getLocation().isUnknown()) {
            msg.append(" at ").append(getLocation().toString());
        }
        return msg.toString();
    }

    public String getMessageWithoutLocation() {
        String message = super.getMessage();
        if (message == null) message = "Exception";
        return message;
    }

}
