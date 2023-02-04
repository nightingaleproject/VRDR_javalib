package edu.gatech.chai.VRDR.messaging.util;

import org.hl7.fhir.r4.model.Bundle;

// a message whose type is unknown, such as during a parsing error, but we still want to inspect and return info on the message
public class UnknownMessage extends BaseMessage {

    public static final String MESSAGE_TYPE = null;

    public UnknownMessage() {
        super(MESSAGE_TYPE);
    }

    public UnknownMessage(Bundle messageBundle) {
        super(messageBundle);
    }

    public UnknownMessage(Bundle messageBundle, boolean ignoreMissingEntries, boolean ignoreBundleType, boolean ignoreEventType) {
        super(messageBundle, ignoreMissingEntries, ignoreBundleType, ignoreEventType);
    }

    public String getIGMessageType() {
        return MESSAGE_TYPE;
    }

}
