package edu.gatech.chai.VRDR.messaging;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import edu.gatech.chai.VRDR.messaging.util.BaseMessage;
import edu.gatech.chai.VRDR.messaging.util.DocumentBundler;
import edu.gatech.chai.VRDR.model.CauseOfDeathCodedContentBundle;
import edu.gatech.chai.VRDR.model.DeathCertificateDocument;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MessageHeader;

@ResourceDef(name = "CauseOfDeathCodingMessage", profile = "http://cdc.gov/nchs/nvss/fhir/vital-records-messaging/StructureDefinition/VRM-CauseOfDeathCodingMessage")
public class CauseOfDeathCodingMessage extends BaseMessage implements DocumentBundler<CauseOfDeathCodedContentBundle> {

    // from the VRDR Messaging IG http://build.fhir.org/ig/nightingaleproject/vital_records_fhir_messaging_ig/branches/main/StructureDefinition-VRM-CauseOfDeathCodingMessage.html
    public static final String MESSAGE_TYPE = "http://cdc.gov/nchs/nvss/fhir/vital-records-messaging/StructureDefinition/VRM-CauseOfDeathCodingMessage";

    private CauseOfDeathCodedContentBundle causeOfDeathCodedContentBundle;

    public CauseOfDeathCodingMessage() {
        super(MESSAGE_TYPE);
    }

    public CauseOfDeathCodingMessage(Bundle messageBundle) {
        super(messageBundle);
        updateDocumentBundle(CauseOfDeathCodedContentBundle.class, this);
    }

    public CauseOfDeathCodedContentBundle getDocumentBundle() {
        return getCauseOfDeathCodedContentBundle();
    }

    public void setDocumentBundle(CauseOfDeathCodedContentBundle causeOfDeathCodedContentBundle) {
        setCauseOfDeathCodedContentBundle(causeOfDeathCodedContentBundle);
    }

    public CauseOfDeathCodedContentBundle getCauseOfDeathCodedContentBundle() {
        return causeOfDeathCodedContentBundle;
    }

    public void setCauseOfDeathCodedContentBundle(CauseOfDeathCodedContentBundle causeOfDeathCodedContentBundle) {
        this.causeOfDeathCodedContentBundle = causeOfDeathCodedContentBundle;
    }

    public CauseOfDeathCodingMessage(BaseMessage messageToCode) {
        this(messageToCode == null ? null : messageToCode.getMessageId(),
                messageToCode == null ? null : messageToCode.getMessageDestination(),
                messageToCode == null ? null : messageToCode.getMessageSource());
        setCertNo(messageToCode == null ? null : messageToCode.getCertNo());
        setStateAuxiliaryId(messageToCode == null ? null : messageToCode.getStateAuxiliaryId());
        setDeathYear(messageToCode == null ? null : messageToCode.getDeathYear());
        setJurisdictionId(messageToCode == null ? null : messageToCode.getJurisdictionId());
    }

    public CauseOfDeathCodingMessage(String messageId, String source, String destination) {
        super(MESSAGE_TYPE);
        header.getSource().setEndpoint(source == null ? DeathRecordSubmissionMessage.MESSAGE_TYPE : source);
        setMessageDestination(destination);
        MessageHeader.MessageHeaderResponseComponent resp = new MessageHeader.MessageHeaderResponseComponent();
        resp.setIdentifier(messageId);
        resp.setCode(MessageHeader.ResponseType.OK);
        header.setResponse(resp);
    }

    public String getIGMessageType() {
        return MESSAGE_TYPE;
    }

    protected Bundle getMessageBundleRecord() {
        return causeOfDeathCodedContentBundle;
    }

    public String getCodedMessageId() {
        return header != null && header.getResponse() != null ? header.getResponse().getIdentifier() : null;
    }

    public void setCodedMessageId(String value) {
        if (header.getResponse() == null) {
            header.setResponse(new MessageHeader.MessageHeaderResponseComponent());
            header.getResponse().setCode(MessageHeader.ResponseType.OK);
        }
        header.getResponse().setIdentifier(value);
    }

}
