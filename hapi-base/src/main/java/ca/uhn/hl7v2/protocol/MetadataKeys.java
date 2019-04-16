package ca.uhn.hl7v2.protocol;


public class MetadataKeys {

	private MetadataKeys() {
		// nothing
	}
	
	/**
	 * Contains the raw message as it was received
	 */
	public static final String IN_RAW_MESSAGE = "raw-message";

	/**
	 * Message control ID (MSH-10)
	 */
	public static final String IN_MESSAGE_CONTROL_ID = "/MSH-10";
	
    /**
	 * Provides the IP of the sending system for a given message
	 */
	public static final String IN_SENDING_IP = "SENDING_IP";

    /**
	 * Provides the port of the sending system for a given message
	 */
	public static final String IN_SENDING_PORT = "SENDING_PORT";

}
