package edu.gatech.chai.VRDR.messaging;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import edu.gatech.chai.VRDR.messaging.util.BaseMessage;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;

@ResourceDef(name = "StatusMessage", profile = "http://cdc.gov/nchs/nvss/fhir/vital-records-messaging/StructureDefinition/VRM-StatusMessage")
public class StatusMessage extends BaseMessage {

    public static final String MESSAGE_TYPE = "http://nchs.cdc.gov/vrdr_status";

    public StatusMessage() {
        super(MESSAGE_TYPE);
    }

    public StatusMessage(Bundle messageBundle) {
        super(messageBundle);
    }

    public StatusMessage(BaseMessage messageToStatus) {
        this(messageToStatus == null ? null : messageToStatus.getMessageId(),
                messageToStatus == null ? null : messageToStatus.getMessageSource(),
                messageToStatus == null ? null : messageToStatus.getMessageDestination());
        setCertNo(messageToStatus == null ? null : messageToStatus.getCertNo());
        setStateAuxiliaryId(messageToStatus == null ? null : messageToStatus.getStateAuxiliaryId());
        setDeathYear(messageToStatus == null ? null : messageToStatus.getDeathYear());
        setJurisdictionId(messageToStatus == null ? null : messageToStatus.getJurisdictionId());
    }

    public StatusMessage(BaseMessage messageToStatus, String status) {
        this(messageToStatus);
        setStatus(status);
    }

    public StatusMessage(String messageId, String destination, String source) {
        super(MESSAGE_TYPE);
        header.getSource().setEndpoint(source);
        setMessageDestination(destination);
        MessageHeader.MessageHeaderResponseComponent resp = new MessageHeader.MessageHeaderResponseComponent();
        resp.setIdentifier(messageId);
        resp.setCode(MessageHeader.ResponseType.OK);
        header.setResponse(resp);
    }

    public StatusMessage(String messageId, String destination) {
        this(messageId, destination, DeathRecordSubmissionMessage.MESSAGE_TYPE);
    }

    public String getIGMessageType() {
        return MESSAGE_TYPE;
    }

    public String getStatusedMessageId() {
        return header != null && header.getResponse() != null ? header.getResponse().getIdentifier() : null;
    }

    public void setStatusedMessageId(String value) {
        if (header.getResponse() == null) {
            header.setResponse(new MessageHeader.MessageHeaderResponseComponent());
            header.getResponse().setCode(MessageHeader.ResponseType.OK);
        }
        header.getResponse().setIdentifier(value);
    }

    public String getStatus() {
        Type type = record.getParameter("status");
        if (type instanceof StringType) {
            return ((StringType) type).getValue();
        }
        return null;
    }

    public void setStatus(String status) {
        setSingleStringValue("status", status);
    }
}
