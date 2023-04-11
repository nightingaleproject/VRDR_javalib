package edu.gatech.chai.VRDR.messaging.util;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.LenientErrorHandler;
import edu.gatech.chai.VRDR.context.VRDRFhirContext;
import edu.gatech.chai.VRDR.messaging.DeathRecordSubmissionMessage;
import edu.gatech.chai.VRDR.model.DeathCertificateDocument;
import edu.gatech.chai.VRDR.model.DeathDate;
import edu.gatech.chai.VRDR.model.DeathLocation;
import edu.gatech.chai.VRDR.model.util.AddressUtil;
import edu.gatech.chai.VRDR.model.util.CommonUtil;
import edu.gatech.chai.VRDR.model.util.DeathCertificateDocumentUtil;
import org.hl7.fhir.r4.model.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class BaseMessage extends Bundle {

    protected Bundle messageBundle;
    protected Parameters record;
    protected MessageHeader header;

    protected BaseMessage(Bundle messageBundle) {
        this(messageBundle, false, false, false);
    }

    protected BaseMessage(Bundle messageBundle, boolean ignoreMissingEntries, boolean ignoreBundleType, boolean ignoreEventType) {
        this.messageBundle = messageBundle;
        BundleType bundleType = messageBundle == null ? BundleType.NULL : messageBundle.getType();
        if (!ignoreBundleType && bundleType != BundleType.MESSAGE) {
            throw new MessageParseException("The FHIR Bundle must be of type message, not " + bundleType.toString(), messageBundle);
        }
        header = CommonUtil.findEntry(messageBundle, MessageHeader.class, ignoreMissingEntries);
        record = CommonUtil.findEntry(messageBundle, Parameters.class, ignoreMissingEntries);
        if (!ignoreEventType && !isMessageEventMatchingMessageType(getIGMessageType())) {
            throw new MessageParseException(getMessageEventTypeMismatchErrorMessage(getIGMessageType()), messageBundle);
        }
    }

    protected <T extends Bundle> void updateDocumentBundle(Class<T> tClass, DocumentBundler<T> documentBundler) {
        try {
            Bundle bundle = CommonUtil.findEntry(messageBundle, tClass);
            if (bundle == null) {
                throw new IllegalArgumentException("No bundle found in the passed messageBundle");
            }
            if (!(tClass.isAssignableFrom(bundle.getClass()))) {
                throw new IllegalArgumentException("Bundle found in the passed messageBundle is of type "
                        + bundle.getClass().getCanonicalName() + " not of expected type " + tClass.getCanonicalName());
            }
            documentBundler.setDocumentBundle((T)bundle);
        } catch (IllegalArgumentException ex) {
            throw new MessageParseException("Error processing entry in the message: " + ex.getMessage(), messageBundle);
        }
    }

    protected abstract String getIGMessageType(); // override for specific IG message types in subclass

    protected String getMessageEventType() {
        return header != null && header.hasEventUriType() ? header.getEventUriType().getValue() : null;
    }

    protected boolean isMessageEventMatchingMessageType(String messageType) {
        String messageEventType = getMessageEventType();
        return messageEventType != null && messageEventType.equals(messageType);
    }

    protected String getMessageEventTypeMismatchErrorMessage(String messageType) {
        return "Message event uri type " + getMessageEventType() + " does not match the expected message type " + messageType;
    }

    protected BaseMessage(String messageType) {
       this(messageType, true);
    }

    // to support jurisdictions who want to bypass adding meta profile by default
    protected BaseMessage(String messageType, boolean includeMetaProfiles) {
        // start with a bundle
        messageBundle = new Bundle();
        messageBundle.setId(UUID.randomUUID().toString());
        messageBundle.setType(Bundle.BundleType.MESSAGE);
        messageBundle.setTimestamp(new Date());
        messageBundle.getMeta().setLastUpdated(new Date());

        // Start with a message header
        header = new MessageHeader();
        if (includeMetaProfiles) {
            header.getMeta().addProfile("http://cdc.gov/nchs/nvss/fhir/vital-records-messaging/StructureDefinition/VRM-SubmissionHeader");
        }
        header.setId(UUID.randomUUID().toString());
        header.setEvent(new UriType(messageType));

        // add message header source
        MessageHeader.MessageSourceComponent source = new MessageHeader.MessageSourceComponent();
        header.setSource(source);

        // add message header destination
        MessageHeader.MessageDestinationComponent destination = new MessageHeader.MessageDestinationComponent();
        destination.setEndpoint(DeathRecordSubmissionMessage.MESSAGE_TYPE);
        List<MessageHeader.MessageDestinationComponent> destinationComponents = new ArrayList<>();
        destinationComponents.add(destination);
        header.setDestination(destinationComponents);

        // setup parameters and reference in header
        record = new Parameters();
        if (includeMetaProfiles) {
            record.getMeta().addProfile("http://cdc.gov/nchs/nvss/fhir/vital-records-messaging/StructureDefinition/VRM-MessageParameters");
        }
        record.setId(UUID.randomUUID().toString());
        header.addFocus(new Reference("urn:uuid:" + record.getId()));

        // add message header to bundle
        Bundle.BundleEntryComponent headerBundleComponent = new Bundle.BundleEntryComponent();
        headerBundleComponent.setFullUrl("urn:uuid:" + header.getId());
        headerBundleComponent.setResource(header);
        messageBundle.addEntry(headerBundleComponent);

        // add parameters resource to bundle
        Bundle.BundleEntryComponent parametersBundleComponent = new Bundle.BundleEntryComponent();
        parametersBundleComponent.setFullUrl("urn:uuid:" + record.getId());
        parametersBundleComponent.setResource(record);
        messageBundle.addEntry(parametersBundleComponent);
    }

    protected void extractBusinessIdentifiers(DeathCertificateDocument from) {
        extractCertNo(from);
        extractStateAuxiliaryId(from);
        extractDeathYear(from);
        extractJurisdictionId(from);
    }

    private void extractCertNo(DeathCertificateDocument from) {
        setCertNo(null);
        if (from != null && from.hasIdentifier() && from.getIdentifier().hasExtension(DeathCertificateDocumentUtil.certificateNumberUrl)) {
            Extension extension = from.getIdentifier().getExtensionByUrl(DeathCertificateDocumentUtil.certificateNumberUrl);
            if (extension.hasValue() && extension.getValue() instanceof IntegerType) {
                setCertNo(((IntegerType) extension.getValue()).getValue());
            }
            else if (extension.hasValue() && extension.getValue() instanceof StringType) {
                setCertNo(Integer.parseInt(((StringType) extension.getValue()).getValue()));
            }
            else {
                setCertNo(null);
            }
        }
        else {
            setCertNo(null);
        }
    }

    private void extractStateAuxiliaryId(DeathCertificateDocument from) {
        if (from != null && from.hasIdentifier() && from.getIdentifier().hasExtension(DeathCertificateDocumentUtil.auxillaryStateIndentifierUrl)) {
            Extension extension = from.getIdentifier().getExtensionByUrl(DeathCertificateDocumentUtil.auxillaryStateIndentifierUrl);
            if (extension.hasValue() && extension.getValue() instanceof StringType) {
                setStateAuxiliaryId(((StringType) extension.getValue()).getValue());
            }
            else {
                setStateAuxiliaryId(null);
            }
        } else {
            setStateAuxiliaryId(null);
        }
    }

    private void extractDeathYear(DeathCertificateDocument from) {
        if (from != null && from.getDeathDate() != null && from.getDeathDate().size() > 0) {
            DeathDate deathDate = from.getDeathDate().get(0);
            if (deathDate.getEffective() instanceof DateTimeType) {
                DateTimeType dateTimeType = (DateTimeType) deathDate.getEffective();
                this.setDeathYear(dateTimeType.getYear());
            } else if (deathDate.getEffective() instanceof DateType) {
                DateType dateType = (DateType) deathDate.getEffective();
                setDeathYear(dateType.getYear());
            } else if (deathDate.getEffective() instanceof StringType) {
                StringType stringType = (StringType) deathDate.getEffective();
                setDeathYear(Integer.parseInt(stringType.getValue()));
            } else {
                setDeathYear(null);
            }
        } else {
            setDeathYear(null);
        }
    }

    private void extractJurisdictionId(DeathCertificateDocument from) {
        setJurisdictionId(getDeathLocationJurisdiction(from));
    }

    public String getDeathLocationJurisdiction(DeathCertificateDocument from) {
        if (from == null) {
            return null;
        }
        for (DeathLocation deathLocation : from.getDeathLocation()) {
            if (deathLocation.hasAddress()) {
                Address address = deathLocation.getAddress();
                StringType stateElement = address.getStateElement();
                if (stateElement.hasExtension(AddressUtil.locationJurisdictionIdUrl)) {
                    Extension extension = stateElement.getExtensionByUrl(AddressUtil.locationJurisdictionIdUrl);
                    if (extension.hasValue() && extension.getValue() instanceof StringType) {
                        return ((StringType) extension.getValue()).getValue();
                    }
                }
                return address.getState();
            }
        }
        return null;
    }

    protected void setSingleStringValue(String key, String value) {
        record.setParameter(key, (String)null);
        if (value != null && !value.trim().isEmpty()) {
            record.setParameter(key, value);
        }
    }

    public Integer getCertNo() {
        if (record.hasParameter("cert_no") && record.getParameter("cert_no") instanceof IntegerType) {
            IntegerType certNoIntegerType = (IntegerType)record.getParameter("cert_no");
            if (certNoIntegerType.hasValue()) {
                return certNoIntegerType.getValue();
            } else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    public void setCertNo(Integer value) {
        record.setParameter("cert_no", (UnsignedIntType)null);
        if (value != null) {
            if (value > 999999) {
                throw new IllegalArgumentException("Certificate number must be a maximum of six digits");
            }
            record.addParameter("cert_no", new UnsignedIntType(value));
        }
    }

    public String getStateAuxiliaryId() {
        if (record.hasParameter("state_auxiliary_id") && record.getParameter("state_auxiliary_id") instanceof StringType) {
            StringType stateAuxiliaryIdStringType = (StringType)record.getParameter("state_auxiliary_id");
            if (stateAuxiliaryIdStringType.hasValue()) {
                return stateAuxiliaryIdStringType.getValue();
            } else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    public void setStateAuxiliaryId(String value) {
        setSingleStringValue("state_auxiliary_id", value);
    }

    public Integer getDeathYear() {
        if (record.hasParameter() && record.getParameter("death_year") instanceof IntegerType) {
            IntegerType deathYearIntegerType = (IntegerType)record.getParameter("death_year");
            if (deathYearIntegerType.hasValue()) {
                return deathYearIntegerType.getValue();
            } else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    public void setDeathYear(Integer value) {
        record.setParameter("death_year", (UnsignedIntType)null);
        if (value != null) {
            if (value < 1000 || value > 9999) {
                throw new IllegalArgumentException("Year of death must be specified using four digits");
            }
            record.addParameter("death_year", new UnsignedIntType(value));
        }
    }

    public String getJurisdictionId() {
        if (record.hasParameter("jurisdiction_id") && record.getParameter("jurisdiction_id") instanceof StringType) {
            StringType jurisdictionIdStringType = (StringType)record.getParameter("jurisdiction_id");
            if (jurisdictionIdStringType.hasValue()) {
                return jurisdictionIdStringType.getValue();
            } else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    public void setJurisdictionId(String value) {
        record.setParameter("jurisdiction_id", (StringType)null);
        if (value != null) {
            if (value.length() != 2) {
                throw new IllegalArgumentException("Jurisdiction ID must be a two character string");
            }
            setSingleStringValue("jurisdiction_id", value);
        }
    }

    protected Bundle getBundle() {
        return messageBundle;
    }

    protected Bundle getMessageBundleRecord() {
        return null; // can be overridden in some cases
    }

    protected void updateMessageBundleRecord() {
        messageBundle.getEntry().removeIf(entry -> entry.getResource() instanceof Bundle);
        header.setFocus(null);
        Bundle newBundle = getMessageBundleRecord();
        if (newBundle != null) {
            Bundle.BundleEntryComponent newEntry = new Bundle.BundleEntryComponent();
            newEntry.setFullUrl("urn:uuid:" + newBundle.getId());
            newEntry.setResource(newBundle);
            messageBundle.addEntry(newEntry);
            header.addFocus(new Reference("urn:uuid:" + newBundle.getId()));
        }
    }

    public String toJson(VRDRFhirContext ctx, boolean prettyPrint) {
        updateMessageBundleRecord();
        return ctx.getCtx().newJsonParser().setPrettyPrint(prettyPrint).encodeResourceToString(messageBundle);
    }

    public String toJson(VRDRFhirContext ctx) {
        return toJson(ctx, false);
    }

    public String toXML(VRDRFhirContext ctx, boolean prettyPrint) {
        updateMessageBundleRecord();
        return ctx.getCtx().newXmlParser().setPrettyPrint(prettyPrint).encodeResourceToString(messageBundle);
    }

    public String toXML(VRDRFhirContext ctx) {
        return toXML(ctx, false);
    }

    public static String bundleToJson(VRDRFhirContext ctx, Bundle bundle) {
        return bundleToJson(ctx, bundle, false);
    }

    public static String bundleToJson(VRDRFhirContext ctx, Bundle bundle, boolean prettyPrint) {
        return ctx.getCtx().newJsonParser().setPrettyPrint(prettyPrint).encodeResourceToString(bundle);
    }

    public static String bundleToXml(VRDRFhirContext ctx, Bundle bundle, boolean prettyPrint) {
        return ctx.getCtx().newXmlParser().setPrettyPrint(prettyPrint).encodeResourceToString(bundle);
    }

    public Date getMessageTimestamp() {
        return messageBundle.getTimestamp();
    }

    public void setMessageTimestamp(Date value) {
        messageBundle.setTimestamp(value);
    }

    public String getMessageId() {
        if (header != null) {
            return header.getId();
        } else {
            return null;
        }
    }

    public void setMessageId(String value) {
        header.setId(value);
        Bundle.BundleEntryComponent messageEntry = messageBundle.getEntry().stream().filter(entry -> entry.getResource() instanceof MessageHeader).findFirst().orElse(null);
        if (messageEntry == null) {
            throw new IllegalArgumentException("MessageHeader not found in message bundle");
        }
        messageEntry.setFullUrl("urn:uuid:" + header.getId());
        messageEntry.setResource(header);
    }

    public String getMessageType() {
        if (header != null && header.getEvent() != null && header.getEvent() instanceof UriType) {
            return ((UriType)header.getEvent()).getValue();
        } else {
            return null;
        }
    }

    public void setMessageType(String value) {
        header.setEvent(new UriType(value));
    }

    public String getMessageSource() {
        if (header != null && header.getSource() != null) {
            return header.getSource().getEndpoint();
        } else {
            return null;
        }
    }

    public void setMessageSource(String value) {
        if (header.getSource() == null) {
            header.setSource(new MessageHeader.MessageSourceComponent());
        }
        header.getSource().setEndpoint(value);
    }

    public String getMessageDestination() {
        if (header != null && header.getDestination().size() > 0) {
            return header.getDestination().get(0).getEndpoint();
        } else {
            return null;
        }
    }

    public void setMessageDestination(String value) {
        header.getDestination().clear();
        MessageHeader.MessageDestinationComponent dest = new MessageHeader.MessageDestinationComponent();
        dest.setEndpoint(value);
        header.getDestination().add(dest);
    }

    public String getNCHSIdentifier() {
        Integer deathYear = getDeathYear();
        String jurisdictionId = getJurisdictionId();
        Integer certNo = getCertNo();
        if (deathYear == null || jurisdictionId == null || certNo == null) {
            return null;
        }
        return String.format("%04d", deathYear) + jurisdictionId + String.format("%06d", certNo);
    }

    public Bundle getMessageBundle() {
        return messageBundle;
    }

    public static String getAbsoluteFilePath(String filePath) {
        File file = new File(filePath);
        if (file.isAbsolute()) {
            return filePath;
        } else {
            return new File(System.getProperty("user.dir"), filePath).getPath();
        }
    }

    public static InputStream getInputStream(String filePath) {
        String path = getAbsoluteFilePath(filePath);
        try {
            return new FileInputStream(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T extends Bundle> T parseXML(Class<T> tClass, VRDRFhirContext ctx, String xmlString) {
        return parse(tClass, ctx.getCtx().newXmlParser(), null, xmlString);
    }

    public static <T extends Bundle> T parseJson(Class<T> tClass, VRDRFhirContext ctx, String jsonString) {
        return parse(tClass, ctx.getCtx().newJsonParser(), null, jsonString);
    }

    public static <T extends Bundle> T parseXMLFile(Class<T> tClass, VRDRFhirContext ctx, String filePath) {
        return parse(tClass, ctx.getCtx().newXmlParser(), getInputStream(filePath), null);
    }

    public static <T extends Bundle> T parseJsonFile(Class<T> tClass, VRDRFhirContext ctx, String filePath) {
        return parse(tClass, ctx.getCtx().newJsonParser(), getInputStream(filePath), null);
    }

    public static <T extends Bundle> T parse(Class<T> tClass, IParser parser, InputStream stream, String bundleString) {
        if (stream != null && bundleString != null) {
            throw new IllegalArgumentException("Cannot parse from both a stream and a string, one must be null");
        }
        try (InputStreamReader reader = stream == null ? null : new InputStreamReader(stream)) {
            parser.setParserErrorHandler(new LenientErrorHandler());
            Bundle bundle = reader != null
                    ? parser.setParserErrorHandler(new LenientErrorHandler()).parseResource(Bundle.class, reader)
                    : parser.setParserErrorHandler(new LenientErrorHandler()).parseResource(Bundle.class, bundleString);
            if (BaseMessage.class.isAssignableFrom(tClass)) {
                return (T) tClass.getConstructor(Bundle.class).newInstance(bundle);
            } else if (Bundle.class.isAssignableFrom(tClass)) {
                return (T) bundle;
            } else {
                throw new IllegalArgumentException("Cannot parse to class " + tClass.getName());
            }
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof MessageParseException) {
                throw (MessageParseException)e.getTargetException();
            } else {
                throw new IllegalArgumentException("Unable to instantiate class with bundle parameter, exception: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse bundle, exception: " + e);
        }
    }

}
