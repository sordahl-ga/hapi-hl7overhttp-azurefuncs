package ca.uhn.hl7v2.parser;

/**
 * Defines the behaviour to use when an unexpected
 * segment is discovered while parsing a message.
 * 
 * @see ParserConfiguration#setUnexpectedSegmentBehaviour(UnexpectedSegmentBehaviourEnum)
 */
public enum UnexpectedSegmentBehaviourEnum {

	/**
	 * <p>
	 * Add the segment as a 
	 * {@link ca.uhn.hl7v2.model.Message#addNonstandardSegment(String) nonstandard segment}
	 * at the current location, even if the current location is in a 
	 * child group within the message.
	 * </p>
	 * <p>
	 * This is the default.
	 * </p>
	 */
	ADD_INLINE,

	/**
	 * Return the parser back to the root of the message (even if the last
	 * segment was in a group) and add the unexpected segment as a
	 * {@link ca.uhn.hl7v2.model.Message#addNonstandardSegment(String) nonstandard segment}.
	 */
	DROP_TO_ROOT,

	/**
	 * Throw an {@link ca.uhn.hl7v2.HL7Exception}
	 */
	THROW_HL7_EXCEPTION
}
