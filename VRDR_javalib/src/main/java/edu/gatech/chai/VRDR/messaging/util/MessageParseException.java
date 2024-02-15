package edu.gatech.chai.VRDR.messaging.util;

import edu.gatech.chai.VRDR.messaging.BaseMessage;
import edu.gatech.chai.VRDR.messaging.ExtractionErrorMessage;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;

public class MessageParseException extends IllegalArgumentException {

    private BaseMessage sourceMessage;

    public MessageParseException() {
        super();
    }

    public MessageParseException(String errorMessage, Bundle sourceBundle) {
        super(errorMessage);
        this.sourceMessage = new UnknownMessage(sourceBundle);
    }

    public MessageParseException(String errorMessage, BaseMessage sourceMessage) {
        super(errorMessage);
        this.sourceMessage = sourceMessage;
    }

    public ExtractionErrorMessage createExtractionErrorMessage() {
        ExtractionErrorMessage errorMessage = new ExtractionErrorMessage(sourceMessage);
        errorMessage.addIssue(OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION, getMessage());
        return errorMessage;
    }
}
