/*
 The contents of this file are subject to the Mozilla Public License Version 1.1
 (the "License"); you may not use this file except in compliance with the License.
 You may obtain a copy of the License at http://www.mozilla.org/MPL/
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 specific language governing rights and limitations under the License.

 The Original Code is "VisitorMessageRule.java".  Description:
 "A message rule that runs a ValidatingMessageVisitor over the message"

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

package ca.uhn.hl7v2.validation.builder.support;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.MessageVisitorFactory;
import ca.uhn.hl7v2.model.MessageVisitors;
import ca.uhn.hl7v2.validation.ValidationException;
import ca.uhn.hl7v2.validation.impl.AbstractMessageRule;

/**
 * A message rule that runs a {@link ValidatingMessageVisitor} over the message,
 * collecting exceptions while traversing
 */
public class VisitorMessageRule extends AbstractMessageRule {

    private final MessageVisitorFactory<? extends ValidatingMessageVisitor> visitorFactory;

    public VisitorMessageRule(MessageVisitorFactory<? extends ValidatingMessageVisitor> visitorFactory) {
        if (visitorFactory == null) throw new IllegalArgumentException("Visitor factory must not be null");
        this.visitorFactory = visitorFactory;
    }

    public ValidationException[] apply(Message message) {
        try {
            ValidatingMessageVisitor visitor = visitorFactory.create();
            MessageVisitors.visit(message, visitor);
            return visitor.getValidationExceptions();
        } catch (HL7Exception e) {
            return new ValidationException[] {
                new ValidationException(e)
            };
        }
    }
}
