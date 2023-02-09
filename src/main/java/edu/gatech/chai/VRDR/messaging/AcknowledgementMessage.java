package edu.gatech.chai.VRDR.messaging;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import edu.gatech.chai.VRDR.messaging.util.BaseMessage;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.MessageHeader;

@ResourceDef(name = "AcknowledgementMessage", profile = "http://cdc.gov/nchs/nvss/fhir/vital-records-messaging/StructureDefinition/VRM-AcknowledgementMessage")
public class AcknowledgementMessage extends BaseMessage {

    public static final String MESSAGE_TYPE = "http://nchs.cdc.gov/vrdr_acknowledgement";

    public AcknowledgementMessage() {
        super(MESSAGE_TYPE);
    }

    public AcknowledgementMessage(Bundle messageBundle) {
        super(messageBundle);
    }

    public AcknowledgementMessage(BaseMessage messageToAck) {
        this(messageToAck == null ? null : messageToAck.getMessageId(),
                messageToAck == null ? null : messageToAck.getMessageSource(),
                messageToAck == null ? null : messageToAck.getMessageDestination());
        setCertNo(messageToAck == null ? null : messageToAck.getCertNo());
        setStateAuxiliaryId(messageToAck == null ? null : messageToAck.getStateAuxiliaryId());
        setDeathYear(messageToAck == null ? null : messageToAck.getDeathYear());
        setJurisdictionId(messageToAck == null ? null : messageToAck.getJurisdictionId());
        if (messageToAck instanceof DeathRecordVoidMessage) {
            DeathRecordVoidMessage voidMessageToAck = (DeathRecordVoidMessage) messageToAck;
            Integer blockCount = voidMessageToAck != null ? voidMessageToAck.getBlockCount() : null;
            setBlockCount(blockCount);
        }
    }

    public AcknowledgementMessage(String messageId, String destination, String source) {
        super(MESSAGE_TYPE);
        header.getSource().setEndpoint(source);
        setMessageDestination(destination);
        MessageHeader.MessageHeaderResponseComponent resp = new MessageHeader.MessageHeaderResponseComponent();
        resp.setIdentifier(messageId);
        resp.setCode(MessageHeader.ResponseType.OK);
        header.setResponse(resp);
    }

    public AcknowledgementMessage(String messageId, String destination) {
        this(messageId, destination, DeathRecordSubmissionMessage.MESSAGE_TYPE);
    }

    public String getIGMessageType() {
        return MESSAGE_TYPE;
    }

    public String getAckedMessageId() {
        return header != null && header.getResponse() != null ? header.getResponse().getIdentifier() : null;
    }

    public void setAckedMessageId(String value) {
        if (header.getResponse() == null) {
            header.setResponse(new MessageHeader.MessageHeaderResponseComponent());
            header.getResponse().setCode(MessageHeader.ResponseType.OK);
        }
        header.getResponse().setIdentifier(value);
    }

    public Integer getBlockCount() {
        if (record == null) {
            return null;
        }
        if (!record.hasParameter("block_count")) {
            return null;
        }
        if (!(record.getParameter("block_count") instanceof IntegerType)) {
            return null;
        }
        IntegerType blockCountType = (IntegerType) record.getParameter("block_count");
        if (blockCountType == null) {
            return null;
        }
        return blockCountType.getValue();
    }

    public void setBlockCount(Integer value) {
        if (value != null && value > 1) {
            record.setParameter("block_count", new IntegerType(value));
        }
        else {
            record.setParameter("block_count", (IntegerType)null);
        }
    }

}
