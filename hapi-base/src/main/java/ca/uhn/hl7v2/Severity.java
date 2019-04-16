/*
 The contents of this file are subject to the Mozilla Public License Version 1.1
 (the "License"); you may not use this file except in compliance with the License.
 You may obtain a copy of the License at http://www.mozilla.org/MPL/
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 specific language governing rights and limitations under the License.

 The Original Code is "Severity.java ".  Description:
 "Severity enumeration"

 The Initial Developer of the Original Code is University Health Network. Copyright (C)
 2013.  All Rights Reserved.

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

/**
 * Severity code table
 */
public enum Severity {

    INFO("I", "Info"),
    WARNING("W", "Warning"),
    ERROR("E", "Error");

    private static final String HL70516 = "HL70516";
    private final String code;
    private final String message;

    private Severity(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * @return the string severity code
     */
    public String getCode() {
        return code;
    }

    /**
     * @return the severity code message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the Severity for the given string
     * @param code severity error code
     * @return Severity
     */
    public static Severity severityFor(String code) {
        for (Severity severity : Severity.values()) {
            if (severity.code.equals(code)) {
                return severity;
            }
        }
        return null;
    }

    /**
     * @return the HL7 table number
     */
    public static String codeTable() {
        return HL70516;
    }
}
