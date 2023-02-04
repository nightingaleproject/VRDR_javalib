package edu.gatech.chai.VRDR.messaging;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import edu.gatech.chai.VRDR.messaging.util.BaseMessage;
import edu.gatech.chai.VRDR.messaging.util.DocumentBundler;
import edu.gatech.chai.VRDR.model.DeathCertificateDocument;
import edu.gatech.chai.VRDR.model.DemographicCodedContentBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MessageHeader;

@ResourceDef(name = "DemographicsCodingUpdateMessage", profile = "http://cdc.gov/nchs/nvss/fhir/vital-records-messaging/StructureDefinition/VRM-DemographicsCodingUpdateMessage")
public class DemographicsCodingUpdateMessage extends BaseMessage implements DocumentBundler<DemographicCodedContentBundle> {

    public static final String MESSAGE_TYPE = "http://cdc.gov/nchs/nvss/fhir/vital-records-messaging/StructureDefinition/VRM-DemographicsCodingUpdateMessage";

    private DemographicCodedContentBundle demographicCodedContentBundle;

    public DemographicsCodingUpdateMessage() {
        super(MESSAGE_TYPE);
    }

    public DemographicsCodingUpdateMessage(Bundle messageBundle) {
        super(messageBundle);
        updateDocumentBundle(DemographicCodedContentBundle.class, this);
    }

    public DemographicCodedContentBundle getDocumentBundle() {
        return getDemographicCodedContentBundle();
    }

    public void setDocumentBundle(DemographicCodedContentBundle demographicCodedContentBundle) {
        setDemographicCodedContentBundle(demographicCodedContentBundle);
    }

    public DemographicCodedContentBundle getDemographicCodedContentBundle() {
        return demographicCodedContentBundle;
    }

    public void setDemographicCodedContentBundle(DemographicCodedContentBundle demographicCodedContentBundle) {
        this.demographicCodedContentBundle = demographicCodedContentBundle;
    }

    public DemographicsCodingUpdateMessage(BaseMessage messageToCode) {
        this(messageToCode == null ? null : messageToCode.getMessageId(),
                messageToCode == null ? null : messageToCode.getMessageDestination(),
                messageToCode == null ? null : messageToCode.getMessageSource());
        setCertNo(messageToCode == null ? null : messageToCode.getCertNo());
        setStateAuxiliaryId(messageToCode == null ? null : messageToCode.getStateAuxiliaryId());
        setDeathYear(messageToCode == null ? null : messageToCode.getDeathYear());
        setJurisdictionId(messageToCode == null ? null : messageToCode.getJurisdictionId());
    }

    public DemographicsCodingUpdateMessage(String messageId, String source, String destination) {
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
        return demographicCodedContentBundle;
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
