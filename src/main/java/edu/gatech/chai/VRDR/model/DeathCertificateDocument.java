package edu.gatech.chai.VRDR.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import edu.gatech.chai.VRDR.context.VRDRFhirContext;
//import org.hl7.fhir.dstu2016may.model.codesystems.ObservationStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Enumerations.*;
import org.hl7.fhir.r4.model.Composition.CompositionStatus;

import ca.uhn.fhir.model.api.annotation.*;
import edu.gatech.chai.VRDR.model.util.CommonUtil;
import edu.gatech.chai.VRDR.model.util.DeathCertificateDocumentUtil;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Observation;
import edu.gatech.chai.VRDR.model.util.*;
import org.hl7.fhir.r4.model.Coding;

import java.util.UUID;

import edu.gatech.chai.VRDR.model.util.*;

import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.elementmodel.Property.*;
import org.hl7.fhir.r4.model.codesystems.EventStatus;

import static edu.gatech.chai.VRDR.model.util.DeathCertificateDocumentUtil.*;

@ResourceDef(name = "Bundle", profile = "http://hl7.org/fhir/us/vrdr/StructureDefinition/vrdr-death-certificate-document")
public class DeathCertificateDocument extends Bundle {

    public static final String LOINC_CODE_DATE_PRONOUNCED_DEAD = "80616-6";

    private static final long serialVersionUID = -429197004514766374L;

    /// <summary>String to represent a blank value when an empty String is not allowed</summary>
    private static String BlankPlaceholder = "BLANK";

    /// <summary>Mortality data for code translations.</summary>
    private MortalityData MortalityData = MortalityData.Instance;

    /// <summary>Useful for navigating around the FHIR Bundle using FHIRPaths.</summary>
    private ITypedElement Navigator;

    /// <summary>Bundle that contains the death record.</summary>
    private Bundle Bundle;

    /// <summary>Composition that described what the Bundle is, as well as keeping references to its contents.</summary>
    private Composition Composition;

    /// <summary>The Decedent.</summary>
    private Patient Decedent;

    /// <summary>The Decedent's Race and Ethnicity provided by Jurisdiction.</summary>
    private Observation InputRaceAndEthnicityObs;

    /// <summary>The Certifier.</summary>
    private Practitioner Certifier;

    /// <summary>The Manner of Death Observation.</summary>
    private Observation MannerOfDeath;

    /// <summary>Condition Contributing to Death.</summary>
    private Observation ConditionContributingToDeath;

    /// <summary>Cause Of Death Condition Line A (#1).</summary>
    private Observation CauseOfDeathConditionA;

    /// <summary>Cause Of Death Condition Line B (#2).</summary>
    private Observation CauseOfDeathConditionB;

    /// <summary>Cause Of Death Condition Line C (#3).</summary>
    private Observation CauseOfDeathConditionC;

    /// <summary>Cause Of Death Condition Line D (#4).</summary>
    private Observation CauseOfDeathConditionD;

    /// <summary>Examiner Contacted.</summary>
    private Observation ExaminerContactedObs;

    /// <summary>Tobacco Use Contributed To Death.</summary>
    private Observation TobaccoUseObs;

    /// <summary>Injury Location.</summary>
    private Location InjuryLocationLoc;
    /// <summary>Create Injury Location.</summary>

    public DeathCertificateDocument()
    {
        super();
        CommonUtil.initResource(this);
        setType(BundleType.DOCUMENT);
    }

    public DeathCertificateDocument(CompositionStatus status, Decedent decedent, DeathCertificationProcedure deathCertificationProcedure)
    {
        super();
        CommonUtil.initResource(this);
        setType(BundleType.DOCUMENT);
        DeathCertificate deathCertificate = new DeathCertificate(status, decedent, deathCertificationProcedure);
        CommonUtil.initResource(deathCertificate);
        this.addEntry(new BundleEntryComponent().setResource(deathCertificate));
    }

    public void addAuxillaryStateIdentifier(String auxillaryStateIdentifierValue)
    {
        Extension extension = new Extension();
        extension.setUrl(DeathCertificateDocumentUtil.auxillaryStateIndentifierUrl);
        extension.setValue(new StringType(auxillaryStateIdentifierValue));
        this.getIdentifier().addExtension(extension);
    }

    public void addResource(Resource resource)
    {
        DeathCertificate deathCertificate = getDeathCertificate().get(0);
        deathCertificate.addResource(resource);
        this.addEntry(new BundleEntryComponent().setResource(resource));
    }

    //Helper Accessor methods

    private List<Resource> getRecords(Class<? extends Resource> type)
    {
        List<Resource> returnList = new ArrayList<Resource>();
        for (BundleEntryComponent bec : this.getEntry())
        {
            Resource resource = bec.getResource();
            if (type.isInstance(resource))
            {
                returnList.add(resource);
            }
        }
        return returnList;
    }

    private <T extends Resource> List<T> castListOfRecords(List<Resource> inputList)
    {
        List<T> outputList = inputList
                .stream()
                .map(e -> (T) e)
                .collect(Collectors.toList());
        return outputList;
    }

    public List<AutopsyPerformedIndicator> getAutopsyPerformedIndicator()
    {
        List<Resource> resources = getRecords(AutopsyPerformedIndicator.class);
        return castListOfRecords(resources);
    }

    public List<BirthRecordIdentifier> getBirthRecordIdentifier()
    {
        List<Resource> resources = getRecords(BirthRecordIdentifier.class);
        return castListOfRecords(resources);
    }

    public List<CauseOfDeathPart1> getCauseOfDeathCondition()
    {
        List<Resource> resources = getRecords(CauseOfDeathPart1.class);
        return castListOfRecords(resources);
    }

    public List<CauseOfDeathPart2> getConditionContributingToDeath()
    {
        List<Resource> resources = getRecords(CauseOfDeathPart2.class);
        return castListOfRecords(resources);
    }

    public List<Certifier> getCertifier()
    {
        List<Resource> resources = getRecords(Certifier.class);
        return castListOfRecords(resources);
    }

    public List<DeathCertificate> getDeathCertificate()
    {
        List<Resource> resources = getRecords(DeathCertificate.class);
        return castListOfRecords(resources);
    }


    public List<DeathCertificationProcedure> getDeathCertificationProcedure()
    {
        List<Resource> resources = getRecords(DeathCertificationProcedure.class);
        return castListOfRecords(resources);
    }

    public List<DeathDate> getDeathDate()
    {
        List<Resource> resources = getRecords(DeathDate.class);
        return castListOfRecords(resources);
    }

    public List<DeathLocation> getDeathLocation()
    {
        List<Resource> resources = getRecords(DeathLocation.class);
        return castListOfRecords(resources);
    }

    public List<Decedent> getDecedent()
    {
        List<Resource> resources = getRecords(Decedent.class);
        return castListOfRecords(resources);
    }

    public List<DecedentAge> getDecedentAge()
    {
        List<Resource> resources = getRecords(DecedentAge.class);
        return castListOfRecords(resources);
    }

    public List<DecedentDispositionMethod> getDecedentDispositionMethod()
    {
        List<Resource> resources = getRecords(DecedentDispositionMethod.class);
        return castListOfRecords(resources);
    }

    public List<DecedentEducationLevel> getDecedentEducationLevel()
    {
        List<Resource> resources = getRecords(DecedentEducationLevel.class);
        return castListOfRecords(resources);
    }

    public List<DecedentFather> getDecedentFather()
    {
        List<Resource> resources = getRecords(DecedentFather.class);
        return castListOfRecords(resources);
    }

    public List<DecedentMilitaryService> getDecedentMilitaryService()
    {
        List<Resource> resources = getRecords(DecedentMilitaryService.class);
        return castListOfRecords(resources);
    }

    public List<DecedentMother> getDecedentMother() 
    {
        List<Resource> resources = getRecords(DecedentMother.class);
        return castListOfRecords(resources);
    }

    public List<DecedentPregnancyStatus> getDecedentPregnancy()
    {
        List<Resource> resources = getRecords(DecedentPregnancyStatus.class);
        return castListOfRecords(resources);
    }

    public List<DecedentSpouse> getDecedentSpouse()
    {
        List<Resource> resources = getRecords(DecedentSpouse.class);
        return castListOfRecords(resources);
    }

    public List<DecedentUsualWork> getDecedentUsualWork()
    {
        List<Resource> resources = getRecords(DecedentUsualWork.class);
        return castListOfRecords(resources);
    }

    public List<DispositionLocation> getDispositionLocation()
    {
        List<Resource> resources = getRecords(DispositionLocation.class);
        return castListOfRecords(resources);
    }

    public List<FuneralHome> getFuneralHome()
    {
        List<Resource> resources = getRecords(FuneralHome.class);
        return castListOfRecords(resources);
    }

    public List<InjuryIncident> getInjuryIncident()
    {
        List<Resource> resources = getRecords(InjuryIncident.class);
        return castListOfRecords(resources);
    }

    public List<InjuryLocation> getInjuryLocation()
    {
        List<Resource> resources = getRecords(InjuryLocation.class);
        return castListOfRecords(resources);
    }

    public List<MannerOfDeath> getMannerOfDeath()
    {
        List<Resource> resources = getRecords(MannerOfDeath.class);
        return castListOfRecords(resources);
    }

    public List<TobaccoUseContributedToDeath> getTobaccoUseContributedToDeath()
    {
        List<Resource> resources = getRecords(TobaccoUseContributedToDeath.class);
        return castListOfRecords(resources);
    }

    public String getgetDateOfDeathPronouncement()
    {
        if (this == null  ||  getDeathDate() == null  ||  getDeathDate().size() == 0)
        {
            return null;
        }
        for (DeathDate date : getDeathDate())
        {
            for (Observation.ObservationComponentComponent component : date.getComponent())
            {
                for (Coding coding : component.getCode().getCoding())
                {
                    if (coding.getCode().equals(LOINC_CODE_DATE_PRONOUNCED_DEAD))
                    {
                        Type value = component.getValue();
                        if (value instanceof DateTimeType)
                        {
                            return ((DateTimeType) value).getValueAsString();
                        }
                        else if (value instanceof TimeType)
                        {
                            return ((TimeType) value).getValueAsString();
                        }
                    }
                }
            }
        }
        return null;
    }

    public List<InputRaceAndEthnicity> getInputRaceAndEthnicity()
    {
        List<Resource> resources = getRecords(InputRaceAndEthnicity.class);
        return castListOfRecords(resources);
    }

    public String gettoJson(VRDRFhirContext ctx)
    {
        return toJson(ctx, false);
    }

    public String gettoJson(VRDRFhirContext ctx, boolean prettyPrint)
    {
        return ctx.getCtx().newJsonParser().setPrettyPrint(prettyPrint).encodeResourceToString(this);
    }


// DeathRecord_submissionProperties.cs
//    These fields are used primarily for submitting death records to NCHS.  Some are also used in response messages from NCHS to EDRS corresponding to TRX and MRE content.

    /// <summary>Class <c>DeathRecord</c> models a FHIR Vital Records Death Reporting (VRDR) Death
    /// Record. This class was designed to help consume and produce death records that follow the
    /// HL7 FHIR Vital Records Death Reporting Implementation Guide, as described at:
    /// http://hl7.org/fhir/us/vrdr and https://github.com/hl7/vrdr.
    /// </summary>


    /////////////////////////////////////////////////////////////////////////////////
    //
    // Record Properties: Death Certification
    //
    /////////////////////////////////////////////////////////////////////////////////

    /// <summary>Death Record Identifier, Death Certificate Number.</summary>
    /// <value>a record identification String.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.Identifier = "42";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Death Certificate Number: {ExampleDeathRecord.Identifier}");</para>
    /// </example>
    //  [Property("Identifier", Property.Types.String, "Death Certification", "Death Certificate Number.", true, IGURL.DeathCertificate, true, 3)]
    //  [FHIRPath("Bundle", "identifier")]
    private String Identifier;

    public String getIdentifier()
    {
        if (Bundle != null && Bundle.getIdentifier() != null && Bundle.getIdentifier().getExtension() != null)
        {
            Extension ext = Bundle.getIdentifier().getExtension().stream().filter(ex -> ex.getUrl().equals(URL.ExtensionURL.CertificateNumber)).findFirst().get();
            if (ext != null && ext.getValue() != null)
            {
                return (ext.getValue().toString());
            }
        }
        return null;
    }

    public void setIdentifier(String value)
    {
        Bundle.getIdentifier().getExtension().removeIf(ex -> ex.getUrl().equals(URL.ExtensionURL.CertificateNumber));
        if (isNullOrWhiteSpace(value))
        {
            Extension ext = new Extension(URL.ExtensionURL.CertificateNumber, new StringType(value));
            Bundle.getIdentifier().getExtension().add(ext);
            UpdateDeathRecordIdentifier();
        }
    }

    /// <summary>Update the bundle identifier from the component fields.</summary>
    private void UpdateDeathRecordIdentifier()
    {
        Integer certificateNumber = 0;
        if (Identifier != null)
        {
            Integer.parseInt(Identifier);
        }
        Integer deathYear = 0;
        if (getDeathYear() != null)
        {
            deathYear = getDeathYear();
        }
        String jurisdictionId = this.DeathLocationJurisdiction;
        if (jurisdictionId == null  ||  jurisdictionId.trim().length() < 2)
        {
            jurisdictionId = "XX";
        }
        else
        {
            jurisdictionId = jurisdictionId.trim().substring(0, 2).toUpperCase();
        }
        this.DeathRecordIdentifier = deathYear.toString(" D4 ")+ jurisdictionId + certificateNumber.toString(" D6 ");

        //this.DeathRecordIdentifier = $ "{deathYear.toString(" D4 ")}{jurisdictionId}{certificateNumber.toString(" D6 ")}";

    }

    /// <summary>Death Record Bundle Identifier, NCHS identifier.</summary>
    /// <value>a record bundle identification String, e.g., 2022MA000100, derived from year of death, jurisdiction of death, and certificate number</value>
    /// <example>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"NCHS identifier: {ExampleDeathRecord.DeathRecordIdentifier}");</para>
    /// </example>
    //  [Property("Death Record Identifier", Property.Types.String, "Death Certification", "Death Record identifier.", true, IGURL.DeathCertificate, true, 4)]
    //  [FHIRPath("Bundle", "identifier")]
    private String DeathRecordIdentifier;

    public String getDeathRecordIdentifier()
    {
        if (Bundle != null && Bundle.getIdentifier() != null)
        {
            return Bundle.getIdentifier().getValue();
        }
        return null;
    }

    // The setter is private because the value is derived so should never be public void set(String value) directly
    private void setDeathRecordIdentifier(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (Bundle.getIdentifier() == null)
        {
            Bundle.setIdentifier(new Identifier());
        }
        Bundle.getIdentifier().setValue(value);
        Bundle.getIdentifier().setSystem("http://nchs.cdc.gov/vrdr_id");
    }

    /// <summary>State Local Identifier1.</summary>
    /// <para>"value" the String representation of the local identifier</para>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.StateLocalIdentifier1 = "MA";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"State local identifier: {ExampleDeathRecord.StateLocalIdentifier1}");</para>
    /// </example>
    //  [Property("State Local Identifier1", Property.Types.String, "Death Certification", "State Local Identifier.", true, ProfileURL.DeathCertificate, true, 5)]
    //  [FHIRPath("Bundle", "identifier")]
    private String StateLocalIdentifier1;

    public String getStateLocalIdentifier1()
    {
        if (Bundle != null && Bundle.getIdentifier() != null && Bundle.getIdentifier().getExtension() != null)
        {
            Extension ext = Bundle.getIdentifier().getExtension().stream().filter(ex -> ex.getUrl().equals(URL.ExtensionURL.AuxiliaryStateIdentifier1)).findFirst().get();
            if (ext != null && ext.getValue() != null)
            {
                return ext.getValue().toString();
            }
        }
        return null;
    }

    public void setStateLocalIdentifier1(String value)
    {
        Bundle.getIdentifier().getExtension().removeIf(ex -> ex.getUrl().equals(URL.ExtensionURL.AuxiliaryStateIdentifier1));
        if (isNullOrWhiteSpace(value))
        {
            Extension ext = new Extension(URL.ExtensionURL.AuxiliaryStateIdentifier1, new StringType(value));
            Bundle.getIdentifier().getExtension().add(ext);
        }
    }


    /// <summary>State Local Identifier2.</summary>
    /// <para>"value" the String representation of the local identifier</para>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.StateLocalIdentifier2 = "YC";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"State local identifier: {ExampleDeathRecord.StateLocalIdentifier1}");</para>
    /// </example>
    //  [Property("State Local Identifier2", Property.Types.String, "Death Certification", "State Local Identifier.", true, ProfileURL.DeathCertificate, true, 5)]
    //  [FHIRPath("Bundle", "identifier")]
    private String StateLocalIdentifier2;

    public String getStateLocalIdentifier2()
    {
        if (Bundle != null && Bundle.getIdentifier() != null && Bundle.getIdentifier().getExtension() != null) //if (Bundle?.Identifier?.Extension != null)
        {
            Extension ext = Bundle.getIdentifier().getExtension().stream().filter(ex -> ex.getUrl().equals(URL.ExtensionURL.AuxiliaryStateIdentifier2)).findFirst().get();
            if (ext != null && ext.getValue() != null)
            {
                return ext.getValue().toString();
            }
        }
        return null;
    }

    public void setStateLocalIdentifier2(String value)
    {
        Bundle.getIdentifier().getExtension().removeIf(ex -> ex.getUrl().equals(URL.ExtensionURL.AuxiliaryStateIdentifier2));
        if (isNullOrWhiteSpace(value))
        {
            Extension ext = new Extension(URL.ExtensionURL.AuxiliaryStateIdentifier2, new StringType(value));
            Bundle.getIdentifier().getExtension().add(ext);
        }
    }


    /// <summary>Certified time.</summary>
    /// <value>time when the record was certified.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.CertifiedTime = "2019-01-29T16:48:06-05:00";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Certified at: {ExampleDeathRecord.CertifiedTime}");</para>
    /// </example>
    //  [Property("Certified Time", Property.Types.StringDateTime, "Death Certification", "Certified time (i.e. certifier date signed).", true, IGURL.DeathCertification, false, 12)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Procedure).stream().findAny(code.coding.code='308646001')", "")]
    private String CertifiedTime;

    public String getCertifiedTime()
    {
        if (DeathCertification != null && DeathCertification.getPerformed() != null)
        {
            return DeathCertification.getPerformed().toString();
        }
        else if (Composition != null && Composition.getAttester() != null && Composition.getAttester().get(0) != null && Composition.getAttester().get(0).getTime() != null)
        {
            return Composition.getAttester().get(0).getTime().toString();
        }
        return null;
    }

    public void setCertifiedTime(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (DeathCertification == null)
        {
            CreateDeathCertification();
        }
        try {
            Composition.getAttester().get(0).setTime(DateUtils.parseDate(value));
        } catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
        DeathCertification.setPerformed(new DateTimeType(value));
    }

    /// <summary>Filing Format.</summary>
    /// <value>Source flag: paper/electronic.
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>        /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; format = new Map&lt;String, String&gt;();</para>
    /// <para>format.add("code", ValueSets.FilingFormat.electronic);</para>
    /// <para>format.add("system", CodeSystems.FilingFormat);</para>
    /// <para>format.add("display", "Electronic");</para>
    /// <para>ExampleDeathRecord.FilingFormat = format;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Filed method: {ExampleDeathRecord.FilingFormat}");</para>
    /// </example>
    //  [Property("Filing Format", Property.Types.Dictionary, "Death Certification", "Filing format.", true, IGURL.DeathCertificate, true, 13)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Composition).getExtension().stream().findAny(url='http://hl7.org/fhir/us/vrdr/StructureDefinition/FilingFormat')", "")]
    private Map<String, String> FilingFormat;

    public Map<String, String> getFilingFormat()
    {
        if (Composition != null)
        {
            Extension filingFormat = Composition.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.FilingFormat)).findFirst().get();

            if (filingFormat != null && filingFormat.getValue() != null && filingFormat.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept) filingFormat.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setFilingFormat(Map<String, String> value)
    {
        // TODO: Handle case where Composition == null (either create it or throw exception)
        Composition.getExtension().removeIf(ext -> ext.getUrl().equals(URL.ExtensionURL.FilingFormat);
        Extension filingFormat = new Extension();
        filingFormat.setUrl(URL.ExtensionURL.FilingFormat);
        filingFormat.setValue(MapToCodeableConcept(value));
        Composition.getExtension().add(filingFormat);
    }

    /// <summary>Filing Format Helper.</summary>
    /// <value>filing format.
    /// <para>"code" - the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.FilingFormatHelper = ValueSets.FilingFormat.Electronic;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Filing Format: {ExampleDeathRecord.FilingFormatHelper}");</para>
    /// </example>
    //  [Property("Filing Format Helper", Property.Types.String, "Death Certification", "Filing Format.", false, IGURL.DeathCertificate, true, 13)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Composition).getExtension().stream().findAny(url='http://hl7.org/fhir/us/vrdr/StructureDefinition/FilingFormat')", "")]
    private String FilingFormatHelper;

    public String getFilingFormatHelper()
    {
        if (FilingFormat.containsKey("code") && isNullOrWhiteSpace(FilingFormat.get("code")))
        {
            return FilingFormat.get("code");
        }
        return null;
    }

    public void setFilingFormatHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("FilingFormat", value, ValueSets.FilingFormat.Codes);
        }
    }

    /// <summary>Registered time.</summary>
    /// <value>time when the record was registered.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.RegisteredTime = "2019-01-29T16:48:06-05:00";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Registered at: {ExampleDeathRecord.RegisteredTime}");</para>
    /// </example>
    //  [Property("Registered Date/Time", Property.Types.StringDateTime, "Death Certification", "Date/Time of record registration.", true, IGURL.DeathCertificate, true, 13)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Composition)", "date")]
    private String RegisteredTime;

    public String getRegisteredTime()
    {
        if (Composition != null)
        {
            return Composition.getDate().toString();
        }
        return null;
    }

    public void setRegisteredTime(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            try {
                Composition.setDate(new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).parse(value));
            } catch (ParseException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /// <summary>State Specific Data.</summary>
    /// <value>Possible use for future filler unless two neighboring states wish to use for some specific information that they both collect. This would be a non-standard field</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.StateSpecific = "Data";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"State Specific Data: {ExampleDeathRecord.StateSpecific}");</para>
    /// </example>
    //  [Property("State Specific Data", Property.Types.String, "Death Certification", "State Specific Content.", true, IGURL.DeathCertificate, true, 13)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Composition).getExtension().stream().findAny(url='http://hl7.org/fhir/us/vrdr/StructureDefinition/StateSpecificField')", "date")]
    private String StateSpecific;

    public String getStateSpecific()
    {
        if (Composition != null)
        {
            Extension stateSpecificData = Composition.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.StateSpecificField)).findFirst().get();
            if (stateSpecificData != null && stateSpecificData.getValue() != null && stateSpecificData.getValue() instanceof StringType)
            {
                return stateSpecificData.getValue().toString();
            }
        }
        return null;
    }

    public void setStateSpecific(String value)
    {
        // TODO: Handle case where Composition == null (either create it or throw exception)
        Composition.getExtension().removeIf(ext -> ext.getUrl().equals(URL.ExtensionURL.StateSpecificField);
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        Extension stateSpecificData = new Extension();
        stateSpecificData.setUrl(URL.ExtensionURL.StateSpecificField);
        stateSpecificData.setValue(new StringType(value));
        Composition.getExtension().add(stateSpecificData);
    }

    /// <summary>Replace Status.</summary>
    /// <value>Replacement Record – suggested codes.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; replace = new Map&lt;String, String&gt;();</para>
    /// <para>replace.add("code", "original");</para>
    /// <para>replace.add("system", CodeSystems.ReplaceStatus);</para>
    /// <para>replace.add("display", "original");</para>
    /// <para>ExampleDeathRecord.ReplaceStatus = replace;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Filed method: {ExampleDeathRecord.ReplaceStatus}");</para>
    /// </example>
    //  [Property("Replace Status", Property.Types.Dictionary, "Death Certification", "Replace status.", true, IGURL.DeathCertificate, true, 13)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Composition).getExtension().stream().findAny(url='http://hl7.org/fhir/us/vrdr/StructureDefinition/ReplaceStatus')", "")]
    private Map<String, String> ReplaceStatus;

    public Map<String, String> getReplaceStatus()
    {
        if (Composition != null)
        {
            Extension replaceStatus = Composition.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.ReplaceStatus)).findFirst().get();
            if (replaceStatus != null && replaceStatus.getValue() != null && replaceStatus.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept) replaceStatus.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setReplaceStatus(Map<String, String> value)
    {
        // TODO: Handle case where Composition == null (either create it or throw exception)
        Composition.getExtension().removeIf(ext -> ext.getUrl().equals(URL.ExtensionURL.ReplaceStatus);
        Extension replaceStatus = new Extension();
        replaceStatus.setUrl(URL.ExtensionURL.ReplaceStatus);
        replaceStatus.setValue(MapToCodeableConcept(value));
        Composition.getExtension().add(replaceStatus);
    }

    /// <summary>Replace Status Helper.</summary>
    /// <value>replace status.
    /// <para>"code" - the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.ReplaceStatusHelper = ValueSets.ReplaceStatus.Original_Record;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"ReplaceStatus: {ExampleDeathRecord.ReplaceStatusHelper}");</para>
    /// </example>
    //  [Property("Replace Status Helper", Property.Types.String, "Death Certification", "Replace Status.", false, IGURL.DeathCertificate, true, 4)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Composition).getExtension().stream().findAny(url='http://hl7.org/fhir/us/vrdr/StructureDefinition/ReplaceStatus')", "")]
    private String ReplaceStatusHelper;

    public String getReplaceStatusHelper()
    {
        if (ReplaceStatus.containsKey("code") && isNullOrWhiteSpace(ReplaceStatus.get("code")))
        {
            return ReplaceStatus.get("code");
        }
        return null;
    }

    public void setReplaceStatusHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("ReplaceStatus", value, ValueSets.ReplaceStatus.Codes);
        }
    }


    /// <summary>Certification Role.</summary>
    /// <value>the role/qualification of the person who certified the death. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; role = new Map&lt;String, String&gt;();</para>
    /// <para>role.add("code", "76899008");</para>
    /// <para>role.add("system", CodeSystems.SCT);</para>
    /// <para>role.add("display", "Infectious diseases physician");</para>
    /// <para>ExampleDeathRecord.CertificationRole = role;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Certification Role: {ExampleDeathRecord.CertificationRole['display']}");</para>
    /// </example>
    //  [Property("Certification Role", Property.Types.Dictionary, "Death Certification", "Certification Role.", true, IGURL.DeathCertification, true, 4)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Procedure).stream().findAny(code.coding.code='308646001')", "performer")]
    private Map<String, String> CertificationRole;

    public Map<String, String> getCertificationRole()
    {
        if (DeathCertification == null)
        {
            return EmptyCodeableMap();
        }
        Procedure.ProcedurePerformerComponent performer = DeathCertification.getPerformer().get(0);
        if (performer != null && performer.getFunction() != null)
        {
            return CodeableConceptToMap(performer.getFunction());
        }
        return EmptyCodeableMap();
    }

    public void setCertificationRole(Map<String, String> value)
    {
        if (DeathCertification == null)
        {
            CreateDeathCertification();
        }
        Procedure.ProcedurePerformerComponent performer = new Procedure.ProcedurePerformerComponent();
        performer.setFunction(MapToCodeableConcept(value));
        performer.setActor(new Reference("urn:uuid:" + Certifier.getId()));
        DeathCertification.getPerformer().clear();
        DeathCertification.getPerformer().add(performer);
    }

    /// <summary>Certification Role Helper.</summary>
    /// <value>the role/qualification of the person who certified the death.
    /// <para>"code" - the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.CertificationRoleHelper = ValueSets.CertificationRole.InfectiousDiseasesPhysician;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Certification Role: {ExampleDeathRecord.CertificationRoleHelper}");</para>
    /// </example>
    //  [Property("Certification Role Helper", Property.Types.String, "Death Certification", "Certification Role.", false, IGURL.DeathCertification, true, 4)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Procedure).stream().findAny(code.coding.code='308646001')", "performer")]
    private String CertificationRoleHelper;

    public String getCertificationRoleHelper()
    {
        if (CertificationRole.containsKey("code"))
        {
            String code = CertificationRole.get("code");
            if (code == "OTH")
            {
                if (CertificationRole.containsKey("text") && isNullOrWhiteSpace(CertificationRole.get("text")))
                {
                    return (CertificationRole.get("text"));
                }
                return ("Other");
            }
            else if (isNullOrWhiteSpace(code))
            {
                return code;
            }
        }
        return null;
    }

    public void setCertificationRoleHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            // do nothing
            return;
        }
        if (!Mappings.CertifierTypes.FHIRToIJE.containsKey(value))
        { //other
            CertificationRole = CodeableConceptToMap(new CodeableConcept(new Coding(CodeSystems.NullFlavor_HL7_V3, "OTH", "Other")));//, value));
        }
        else
        { // normal path
            SetCodeValue("CertificationRole", value, ValueSets.CertifierTypes.Codes);
        }
    }

    /// <summary>Manner of Death Type.</summary>
    /// <value>the manner of death type. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; manner = new Map&lt;String, String&gt;();</para>
    /// <para>manner.add("code", "7878000");</para>
    /// <para>manner.add("system", "");</para>
    /// <para>manner.add("display", "Accidental death");</para>
    /// <para>ExampleDeathRecord.MannerOfDeathType = manner;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Manner Of Death Type: {ExampleDeathRecord.MannerOfDeathType['display']}");</para>
    /// </example>
    //  [Property("Manner Of Death Type", Property.Types.Dictionary, "Death Certification", "Manner of Death Type.", true, IGURL.MannerOfDeath, true, 49)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='69449-7')", "")]
    private Map<String, String> MannerOfDeathType;

    public Map<String, String> getMannerOfDeathType()
    {
        if (MannerOfDeath != null && MannerOfDeath.getValue() != null && MannerOfDeath.getValue() instanceof CodeableConcept)
        {
            return CodeableConceptToMap((CodeableConcept) MannerOfDeath.getValue());
        }
        return EmptyCodeableMap();
    }

    public void setMannerOfDeathType(Map<String, String> value)
    {
        if (isMapEmptyOrDefault(value) && MannerOfDeath == null)
        {
            return;
        }
        if (MannerOfDeath == null)
        {
            MannerOfDeath = new Observation();
            MannerOfDeath.setId(UUID.randomUUID().toString());
            MannerOfDeath.setMeta(new Meta());
            CanonicalType[] mannerofdeath_profile = {URL.ProfileURL.MannerOfDeath};
            MannerOfDeath.getMeta().setProfile(Arrays.asList(mannerofdeath_profile));
            MannerOfDeath.setStatus(Observation.ObservationStatus.FINAL);
            MannerOfDeath.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "69449-7", "Manner of death")));//, null));
            MannerOfDeath.setSubject(new Reference("urn:uuid:" + Decedent.getId()));
            MannerOfDeath.getPerformer().add(new Reference("urn:uuid:" + Certifier.getId()));
            MannerOfDeath.setValue(MapToCodeableConcept(value));
            AddReferenceToComposition(MannerOfDeath.getId(), "DeathCertification");
            Resource resource = MannerOfDeath;
            resource.setId("urn:uuid:" + MannerOfDeath.getId());
            Bundle.addEntry(new BundleEntryComponent().setResource(resource));
        }
        else
        {
            MannerOfDeath.setValue(MapToCodeableConcept(value));
        }
    }

    /// <summary>Manner of Death Type Helper</summary>
    /// <value>the manner of death type
    /// <para>"code" - the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.MannerOfDeathTypeHelper = MannerOfDeath.Natural;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Manner Of Death Type: {ExampleDeathRecord.MannerOfDeathTypeHelper}");</para>
    /// </example>
    //  [Property("Manner Of Death Type Helper", Property.Types.String, "Death Certification", "Manner of Death Type.", false, IGURL.MannerOfDeath, true, 49)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='69449-7')", "")]

    private String MannerOfDeathTypeHelper;

    public String getMannerOfDeathTypeHelper()
    {
        if (MannerOfDeathType.containsKey("code") && isNullOrWhiteSpace(MannerOfDeathType.get("code")))
        {
            return MannerOfDeathType.get("code");
        }
        return null;
    }

    public void setMannerOfDeathTypeHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("MannerOfDeathType", value, ValueSets.MannerOfDeath.Codes);
        }
    }

    /// <summary>Given name(s) of certifier.</summary>
    /// <value>the certifier's name (first, middle, etc.)</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>String[] names = { "Doctor", "Middle" };</para>
    /// <para>ExampleDeathRecord.CertifierGivenNames = names;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Certifier Given Name(s): {String.Join(", ", ExampleDeathRecord.CertifierGivenNames)}");</para>
    /// </example>

    //	[Property("Certifier Given Names", Property.Types.StringArr, "Death Certification", "Given name(s) of certifier.", true, IGURL.Certifier, true, 5)]
    //	[FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Practitioner).stream().findAny(meta.profile='http://hl7.org/fhir/us/vrdr/StructureDefinition/vrdr-certifier')", "name")]
    private String[] CertifierGivenNames;

    public String[] getCertifierGivenNames()
    {
        if (Certifier != null && Certifier.getName().size() > 0)
        {
            return (String[]) Certifier.getName().get(0).getGiven().toArray();
        }
        return new String[0];
    }

    public void setCertifierGivenNames(String[] value)
    {
        if (Certifier == null)
        {
            CreateCertifier();
        }
        updateGivenHumanName(value, Certifier.getName());
    }

    /// <summary>Family name of certifier.</summary>
    /// <value>the certifier's family name (i.e. last name)</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.CertifierFamilyName = "Last";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Certifier's Last Name: {ExampleDeathRecord.CertifierFamilyName}");</para>
    /// </example>

    //  [Property("Certifier Family Name", Property.Types.String, "Death Certification", "Family name of certifier.", true, IGURL.Certifier, true, 6)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Practitioner).stream().findAny(meta.profile='http://hl7.org/fhir/us/vrdr/StructureDefinition/vrdr-certifier')", "name")]
    private String CertifierFamilyName;

    public String getCertifierFamilyName()
    {
        if (Certifier != null && Certifier.getName().size() > 0)
        {
            return Certifier.getName().get(0).getFamily();
        }
        return null;
    }

    public void setCertifierFamilyName(String value)
    {
        if (Certifier == null)
        {
            CreateCertifier();
        }
        HumanName name = Certifier.getName().get(0);
        if (name != null && isNullOrEmpty(value))
        {
            name.setFamily(value);
        } else if (isNullOrEmpty(value))
        {
            name = new HumanName();
            name.setUse(HumanName.NameUse.OFFICIAL);
            name.setFamily(value);
            Certifier.getName().add(name);
        }
    }

    /// <summary>Certifier's Suffix.</summary>
    /// <value>the certifier's suffix</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.CertifierSuffix = "Jr.";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Certifier Suffix: {ExampleDeathRecord.CertifierSuffix}");</para>
    /// </example>

    //  [Property("Certifier Suffix", Property.Types.String, "Death Certification", "Certifier's Suffix.", true, IGURL.Certifier, true, 7)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Practitioner).stream().findAny(meta.profile='http://hl7.org/fhir/us/vrdr/StructureDefinition/vrdr-certifier')", "name")]
    private String CertifierSuffix;

    public String getCertifierSuffix()
    {
        if (Certifier != null && Certifier.getName().size() > 0 && Certifier.getName().get(0).getSuffix().size() > 0)
        {
            return Certifier.getName().get(0).getSuffix().get(0).toString();
        }
        return null;
    }

    public void setCertifierSuffix(StringType value)
    {
        if (Certifier == null)
        {
            CreateCertifier();
        }
        HumanName name = Certifier.getName().get(0);
        if (name != null && isNullOrEmpty(value.toString()))
        {
            StringType[] suffix = {value};
            name.setSuffix(Arrays.asList(suffix));
        }
        else if (isNullOrEmpty(value.toString()))
        {
            name = new HumanName();
            name.setUse(HumanName.NameUse.OFFICIAL);
            StringType[] suffix = {value};
            name.setSuffix(Arrays.asList(suffix));
            Certifier.setName((List<HumanName>) name); ////
        }
    }

    /// <summary>Certifier's Address.</summary>
    /// <value>the certifier's address. A Map representing an address, containing the following key/value pairs:
    /// <para>"addressLine1" - address, line one</para>
    /// <para>"addressLine2" - address, line two</para>
    /// <para>"addressCity" - address, city</para>
    /// <para>"addressCounty" - address, county</para>
    /// <para>"addressState" - address, state</para>
    /// <para>"addressZip" - address, zip</para>
    /// <para>"addressCountry" - address, country</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; address = new Map&lt;String, String&gt;();</para>
    /// <para>address.add("addressLine1", "123 Test Street");</para>
    /// <para>address.add("addressLine2", "Unit 3");</para>
    /// <para>address.add("addressCity", "Boston");</para>
    /// <para>address.add("addressCounty", "Suffolk");</para>
    /// <para>address.add("addressState", "MA");</para>
    /// <para>address.add("addressZip", "12345");</para>
    /// <para>address.add("addressCountry", "US");</para>
    /// <para>ExampleDeathRecord.CertifierAddress = address;</para>
    /// <para>// Getter:</para>
    /// <para>foreach(var pair in ExampleDeathRecord.CertifierAddress)</para>
    /// <para>{</para>
    /// <para>  Console.WriteLine($"\tCertifierAddress key: {pair.Key}: value: {pair.getValue()}");</para>
    /// <para>};</para>
    /// </example>

    //  [Property("Certifier Address", Property.Types.Dictionary, "Death Certification", "Certifier's Address.", true, IGURL.Certifier, true, 8)]
    //  [PropertyParam("addressLine1", "address, line one")]
    //  [PropertyParam("addressLine2", "address, line two")]
    //  [PropertyParam("addressCity", "address, city")]
    //  [PropertyParam("addressCounty", "address, county")]
    //  [PropertyParam("addressState", "address, state")]
    //  [PropertyParam("addressStnum", "address, stnum")]
    //  [PropertyParam("addressPredir", "address, predir")]
    //  [PropertyParam("addressPostdir", "address, postdir")]
    //  [PropertyParam("addressStname", "address, stname")]
    //  [PropertyParam("addressStrdesig", "address, strdesig")]
    //  [PropertyParam("addressUnitnum", "address, unitnum")]
    //  [PropertyParam("addressZip", "address, zip")]
    //  [PropertyParam("addressCountry", "address, country")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Practitioner).stream().findAny(meta.profile='http://hl7.org/fhir/us/vrdr/StructureDefinition/vrdr-certifier')", "address")]
    private Map<String, String> CertifierAddress;

    public Map<String, StringType> getCertifierAddress()
    {
        if (Certifier == null)
        {
            return (new HashMap<String, StringType>());
        }
        return addressToMap(Certifier.getAddress().get(0));
    }

    public void setCertifierAddress(Map<String, StringType> value)
    {
        if (Certifier == null)
        {
            CreateCertifier();
        }
        Certifier.getAddress().clear();
        Certifier.getAddress().add(mapToAddress(value));
    }

    /// <summary>Certifier Identifier ** not mapped to IJE **.</summary>
    /// <value>the certifier identification. A Map representing a system (e.g. NPI) and a value, containing the following key/value pairs:
    /// <para>"system" - the identifier system, e.g. US NPI</para>
    /// <para>"value" - the idetifier value, e.g. US NPI number</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; identifier = new Map&lt;String, String&gt;();</para>
    /// <para>identifier.add("system", "http://hl7.org/fhir/sid/us-npi");</para>
    /// <para>identifier.add("value", "1234567890");</para>
    /// <para>ExampleDeathRecord.CertifierIdentifier = identifier;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"\tCertifier Identifier: {ExampleDeathRecord.CertifierIdentifier['value']}");</para>
    /// </example>

    //  [Property("Certifier Identifier", Property.Types.Dictionary, "Death Certification", "Certifier Identifier.", true, IGURL.Certifier, false, 10)]
    //  [PropertyParam("system", "The identifier system.")]
    //  [PropertyParam("value", "The identifier value.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Practitioner).stream().findAny(meta.profile='http://hl7.org/fhir/us/vrdr/StructureDefinition/vrdr-certifier')", "identifier")]
    private Map<String, String> CertifierIdentifier;

    public Map<String, String> getCertifierIdentifier()
    {
        if (Certifier == null  ||  Certifier.getIdentifier().get(0) == null)
        {
            return new HashMap<String, String>();
        }
        Identifier identifier = Certifier.getIdentifier().get(0);
        Map result = new HashMap<String, String>();
        result.put("system", identifier.getSystem());
        result.put("value", identifier.getValue());
        return result;
    }

    public void setCertifierIdentifier(Map<String, String> value)
    {
        if (Certifier == null)
        {
            CreateCertifier();
        }
        if (Certifier.getIdentifier().size() > 0)
        {
            Certifier.getIdentifier().clear();
        }
        if (value.containsKey("system") && value.containsKey("value"))
        {
            Identifier identifier = new Identifier();
            identifier.setSystem(value.get("system"));
            identifier.setSystem(value.get("value"));
            Certifier.getIdentifier().add(identifier);
        }
    }

    // /// <summary>Certifier License Number. ** NOT MAPPED TO IJE ** </summary>
    // /// <value>A String containing the certifier license number.</value>
    // /// <example>
    // /// <para>// Setter:</para>
    // /// <para>ExampleDeathRecord.CertifierQualification = qualification;</para>
    // /// <para>// Getter:</para>
    // /// <para>Console.WriteLine($"\tCertifier Qualification: {ExampleDeathRecord.CertifierQualification['display']}");</para>
    // /// </example>
    // [Property("Certifier License Number", Property.Types.String, "Death Certification", "Certifier License Number.", true, "http://build.fhir.org/ig/HL7/vrdr/StructureDefinition-VRDR-Certifier.html", false, 11)]
    // [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Practitioner).stream().findAny(meta.profile='http://hl7.org/fhir/us/vrdr/StructureDefinition/vrdr-certifier')", "qualification")]
    // public String getCertifierLicenseNumber
    // {
    //     get
    //     {
    //         Practitioner.QualificationComponent qualification = Certifier.Qualification.get(0);
    //         if (qualification != null && qualification.getIdentifier().get(0) != null)
    //         {
    //             if (isNullOrWhiteSpace(qualification.getIdentifier().get(0).getValue()))
    //             {
    //                 return qualification.getIdentifier().get(0).getValue();
    //             }
    //             return null;
    //         }
    //         return null;
    //     }
    //     set
    //     {
    //         if (Certifier.Qualification.get(0) == null)
    //         {
    //             Practitioner.QualificationComponent qualification = new Practitioner.QualificationComponent();
    //             Identifier identifier = new Identifier();
    //             identifier.setValue(value;
    //             qualification.getIdentifier().add(identifier);
    //             Certifier.Qualification.add(qualification);
    //         }
    //         else
    //         {
    //             Certifier.Qualification.get(0).getIdentifier().clear();
    //             Identifier identifier = new Identifier();
    //             identifier.setValue(value;
    //             Certifier.Qualification.get(0).getIdentifier().add(identifier);
    //         }
    //     }
    // }

    /// <summary>Significant conditions that contributed to death but did not result in the underlying cause.
    /// Corresponds to part 2 of item 32 of the U.S. Standard Certificate of Death.</summary>
    /// <value>A String containing the significant conditions that contributed to death but did not result in
    /// the underlying cause captured by a CauseOfDeathCondition.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.ContributingConditions = "Example Contributing Condition";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Cause: {ExampleDeathRecord.ContributingConditions}");</para>
    /// </example>

    //  [Property("Contributing Conditions", Property.Types.String, "Death Certification", "Significant conditions that contributed to death but did not result in the underlying cause.", true, IGURL.CauseOfDeathPart2, true, 100)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='69441-4')", "")]
    private String ContributingConditions;

    public String getContributingConditions()
    {
        if (ConditionContributingToDeath != null && ConditionContributingToDeath.getValue() != null)
        {
            return (CodeableConceptToMap((CodeableConcept) ConditionContributingToDeath.getValue())).get("text");
        }
        return null;
    }

    public void setContributingConditions(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (ConditionContributingToDeath != null)
        {
            ConditionContributingToDeath.setValue(new CodeableConcept(new Coding(null, null, null)));//, value);
        }
        else
        {
            ConditionContributingToDeath = new Observation();
            ConditionContributingToDeath.setId(UUID.randomUUID().toString().toString());
            ConditionContributingToDeath.setSubject(new Reference("urn:uuid:" + Decedent.getId()));
            ConditionContributingToDeath.getPerformer().add(new Reference("urn:uuid:" + Certifier.getId()));
            ConditionContributingToDeath.setMeta(new Meta());
            List condition_profile = new ArrayList();
            condition_profile.add(URL.ProfileURL.CauseOfDeathPart2);
            ConditionContributingToDeath.getMeta().setProfile(condition_profile);
            ConditionContributingToDeath.setStatus(ObservationStatus.FINAL);
            ConditionContributingToDeath.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "69441-4", "Other significant causes or conditions of death")));//, null));
            ConditionContributingToDeath.setValue(new CodeableConcept(new Coding(null, null, null)));//, value);
            AddReferenceToComposition(ConditionContributingToDeath.getId(), "DeathCertification");
            Resource resource = ConditionContributingToDeath;
            resource.setId("urn:uuid:" + ConditionContributingToDeath.getId());
            Bundle.addEntry(new BundleEntryComponent().setResource(resource));
            //Bundle.addEntry(ConditionContributingToDeath, "urn:uuid:" + ConditionContributingToDeath.getId());
        }
    }

    /// <summary>Conditions that resulted in the cause of death. Corresponds to part 1 of item 32 of the U.S.
    /// Standard Certificate of Death.</summary>
    /// <value>Conditions that resulted in the underlying cause of death. An array of tuples (in the order they would
    /// appear on a death certificate, from top to bottom), each containing the cause of death literal (Tuple "Item1")
    /// and the approximate onset to death (Tuple "Item2").</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Tuple&lt;String, String&gt;[] causes =</para>
    /// <para>{</para>
    /// <para>    Tuple.Create("Example Immediate COD", "minutes"),</para>
    /// <para>    Tuple.Create("Example Underlying COD 1", "2 hours"),</para>
    /// <para>    Tuple.Create("Example Underlying COD 2", "6 months"),</para>
    /// <para>    Tuple.Create("Example Underlying COD 3", "15 years")</para>
    /// <para>};</para>
    /// <para>ExampleDeathRecord.CausesOfDeath = causes;</para>
    /// <para>// Getter:</para>
    /// <para>Tuple&lt;String, String&gt;[] causes = ExampleDeathRecord.CausesOfDeath;</para>
    /// <para>for(var cause in causes)</para>
    /// <para>{</para>
    /// <para>    Console.WriteLine($"Cause: {cause.Item1}, Onset: {cause.Item2}");</para>
    /// <para>}</para>
    /// </example>

    //  [Property("Causes Of Death", Property.Types.TupleArr, "Death Certification", "Conditions that resulted in the cause of death.", true, IGURL.CauseOfDeathPart1, true, 50)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='69453-9')", "")]
    private List<Tuple> CausesOfDeath;

    public List<Tuple> getCausesOfDeath()
    {
        List<Tuple> results = new ArrayList<Tuple>();
        if (isNullOrEmpty(COD1A)  ||  isNullOrEmpty(INTERVAL1A))
        {
            results.add(Tuple.Create(COD1A, INTERVAL1A));
        }
        if (isNullOrEmpty(COD1B)  ||  isNullOrEmpty(INTERVAL1B))
        {
            results.add(Tuple.Create(COD1B, INTERVAL1B));
        }
        if (isNullOrEmpty(COD1C)  ||  isNullOrEmpty(INTERVAL1C))
        {
            results.add(Tuple.Create(COD1C, INTERVAL1C));
        }
        if (isNullOrEmpty(COD1D)  ||  isNullOrEmpty(INTERVAL1D))
        {
            results.add(Tuple.Create(COD1D, INTERVAL1D));
        }

        return results;
    }

    public void setCausesOfDeath(String value)
    {
        if (value != null)
        {
            if (value.length > 0)
            {
                if (isNullOrWhiteSpace(value[0].Item1))
                {
                    COD1A = value[0].Item1;
                }
                if (isNullOrWhiteSpace(value[0].Item2))
                {
                    INTERVAL1A = value[0].Item2;
                }
            }
            if (value.length > 1)
            {
                if (isNullOrWhiteSpace(value[1].Item1))
                {
                    COD1B = value[1].Item1;
                }
                if (isNullOrWhiteSpace(value[1].Item2))
                {
                    INTERVAL1B = value[1].Item2;
                }
            }
            if (value.length > 2)
            {
                if (isNullOrWhiteSpace(value[2].Item1))
                {
                    COD1C = value[2].Item1;

                }
                if (isNullOrWhiteSpace(value[2].Item2))
                {
                    INTERVAL1C = value[2].Item2;
                }
            }
            if (value.length > 3)
            {
                if (isNullOrWhiteSpace(value[3].Item1))
                {
                    COD1D = value[3].Item1;
                }
                if (isNullOrWhiteSpace(value[3].Item2))
                {
                    INTERVAL1D = value[3].Item2;
                }
            }
        }
    }

    /// <summary>Cause of Death Part I, Line a.</summary>
    /// <value>the immediate cause of death literal.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.COD1A = "Rupture of myocardium";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Cause: {ExampleDeathRecord.COD1A}");</para>
    /// </example>
    //  [Property("COD1A", Property.Types.String, "Death Certification", "Cause of Death Part I, Line a.", false, IGURL.CauseOfDeathPart1, false, 100)]
    private String COD1A;

    public String getCOD1A()
    {
        if (CauseOfDeathConditionA != null && CauseOfDeathConditionA.getValue() != null)
        {
            return (CodeableConceptToMap((CodeableConcept) CauseOfDeathConditionA.getValue())).get("text");
        }
        return null;
    }

    public void setCOD1A(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (CauseOfDeathConditionA == null)
        {
            CauseOfDeathConditionA = CauseOfDeathCondition(0);
        }
        CauseOfDeathConditionA.setValue(new CodeableConcept(new Coding(null, null, null)));//, value);
    }

    /// <summary>Cause of Death Part I Interval, Line a.</summary>
    /// <value>the immediate cause of death approximate interval: onset to death.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.INTERVAL1A = "Minutes";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Interval: {ExampleDeathRecord.INTERVAL1A}");</para>
    /// </example>
    //  [Property("INTERVAL1A", Property.Types.String, "Death Certification", "Cause of Death Part I Interval, Line a.", false, IGURL.CauseOfDeathPart1, false, 100)]
    private String INTERVAL1A;

    public String getINTERVAL1A()
    {
        if (CauseOfDeathConditionA != null && CauseOfDeathConditionA.getComponent() != null)
        {
            Observation.ObservationComponentComponent intervalComp = CauseOfDeathConditionA.getComponent().stream().filter(entry -> entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("69440-6")).findFirst().get();
            if (intervalComp != null && intervalComp.getValue() != null && intervalComp.getValue() instanceof StringType)
            {
                return intervalComp.getValue().toString();
            }
        }
        return null;
    }

    public void setINTERVAL1A(String value)
    {
        if (isNullOrEmpty(value))
        {
            return;
        }
        if (CauseOfDeathConditionA == null)
        {
            CauseOfDeathConditionA = CauseOfDeathCondition(0);
        }
        // Find correct component; if doesn't exist add another
        Observation.ObservationComponentComponent intervalComp = CauseOfDeathConditionA.getComponent().stream().filter(entry -> entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("69440-6")).findFirst().get();
        if (intervalComp != null)
        {
            ((Observation.ObservationComponentComponent) intervalComp).setValue(new StringType(value));
        }
        else
        {
            Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
            component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "69440-6", "Disease onset to death interval")));//, null));
            component.setValue(new StringType(value));
            CauseOfDeathConditionA.getComponent().add(component);
        }
    }

    // /// <summary>Cause of Death Part I Code, Line a.</summary>
    // /// <value>the immediate cause of death coding. A Map representing a code, containing the following key/value pairs:
    // /// <para>"code" - the code</para>
    // /// <para>"system" - the code system this code belongs to</para>
    // /// <para>"display" - a human readable meaning of the code</para>
    // /// </value>
    // /// <example>
    // /// <para>// Setter:</para>
    // /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    // /// <para>code.add("code", "I21.0");</para>
    // /// <para>code.add("system", "http://hl7.org/fhir/sid/icd-10");</para>
    // /// <para>code.add("display", "Acute transmural myocardial infarction of anterior wall");</para>
    // /// <para>ExampleDeathRecord.CODE1A = code;</para>
    // /// <para>// Getter:</para>
    // /// <para>Console.WriteLine($"\tCause of Death: {ExampleDeathRecord.CODE1A['display']}");</para>
    // /// </example>
    // [Property("CODE1A", Property.Types.Dictionary, "Death Certification", "Cause of Death Part I Code, Line a.", false, IGURL.CauseOfDeathPart1, false, 100)]
    // [PropertyParam("code", "The code used to describe this concept.")]
    // [PropertyParam("system", "The relevant code system.")]
    // [PropertyParam("display", "The human readable version of this code.")]
    // public Map<String, String> CODE1A
    // {
    //     get
    //     {
    //         if (CauseOfDeathConditionA != null && CauseOfDeathConditionA.getCode() != null)
    //         {
    //             return CodeableConceptToMap(CauseOfDeathConditionA.Code);
    //         }
    //         return EmptyCodeMap();
    //     }
    //     set
    //     {
    //         if(CauseOfDeathConditionA == null)
    //         {
    //             CauseOfDeathConditionA = CauseOfDeathCondition(0);
    //         }
    //         if (CauseOfDeathConditionA.getCode() != null)
    //         {
    //             CodeableConcept code = MapToCodeableConcept(value);
    //             code.Text = CauseOfDeathConditionA.Code.Text;
    //             CauseOfDeathConditionA.Code = code;
    //         }
    //         else
    //         {
    //         CauseOfDeathConditionA.Code = MapToCodeableConcept(value);
    //         }
    //     }
    // }

    /// <summary>Cause of Death Part I, Line b.</summary>
    /// <value>the first underlying cause of death literal.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.COD1B = "Acute myocardial infarction";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Cause: {ExampleDeathRecord.COD1B}");</para>
    /// </example>
    //  [Property("COD1B", Property.Types.String, "Death Certification", "Cause of Death Part I, Line b.", false, IGURL.CauseOfDeathPart1, false, 100)]
    private String COD1B;

    public String getCOD1B()
    {
        if (CauseOfDeathConditionB != null && CauseOfDeathConditionB.getValue() != null)
        {
            return (CodeableConceptToMap((CodeableConcept) CauseOfDeathConditionB.getValue())).get("text");
        }
        return null;
    }

    public void setCOD1B(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (CauseOfDeathConditionB == null)
        {
            CauseOfDeathConditionB = CauseOfDeathCondition(1);
        }
        CauseOfDeathConditionB.setValue(new CodeableConcept(new Coding(null, null, null)));//, value);
    }

    /// <summary>Cause of Death Part I Interval, Line b.</summary>
    /// <value>the first underlying cause of death approximate interval: onset to death.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.INTERVAL1B = "6 days";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Interval: {ExampleDeathRecord.INTERVAL1B}");</para>
    /// </example>
    //  [Property("INTERVAL1B", Property.Types.String, "Death Certification", "Cause of Death Part I Interval, Line b.", false, IGURL.CauseOfDeathPart1, false, 100)]
    private String INTERVAL1B;

    public String getINTERVAL1B()
    {
        if (CauseOfDeathConditionB != null && CauseOfDeathConditionB.getComponent() != null)
        {
            Observation.ObservationComponentComponent intervalComp = CauseOfDeathConditionB.getComponent().stream().filter(entry -> entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("69440-6")).findFirst().get();
            if (intervalComp != null && intervalComp.getValue() != null && intervalComp.getValue() instanceof StringType)
            {
                return intervalComp.getValue().toString();
            }
        }
        return null;
    }

    public void setINTERVAL1B(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (CauseOfDeathConditionB == null)
        {
            CauseOfDeathConditionB = CauseOfDeathCondition(1);
        }
        // Find correct component; if doesn't exist add another
        Observation.ObservationComponentComponent intervalComp = CauseOfDeathConditionB.getComponent().stream().filter(entry -> entry.getCode() != null && entry.getCode() != null && entry.getCode().getCoding().get(0).getCode().equals("69440-6")).findFirst().get();
        if (intervalComp != null)
        {
            ((Observation.ObservationComponentComponent) intervalComp).setValue(new StringType(value));
        }
        else
        {
            Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
            component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "69440-6", "Disease onset to death interval")));//, null);
            component.setValue(new StringType(value));
            CauseOfDeathConditionB.getComponent().add(component);
        }
    }

    // /// <summary>Cause of Death Part I Code, Line b.</summary>
    // /// <value>the first underlying cause of death coding. A Map representing a code, containing the following key/value pairs:
    // /// <para>"code" - the code</para>
    // /// <para>"system" - the code system this code belongs to</para>
    // /// <para>"display" - a human readable meaning of the code</para>
    // /// </value>
    // /// <example>
    // /// <para>// Setter:</para>
    // /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    // /// <para>code.add("code", "I21.9");</para>
    // /// <para>code.add("system", "http://hl7.org/fhir/sid/icd-10");</para>
    // /// <para>code.add("display", "Acute myocardial infarction, unspecified");</para>
    // /// <para>ExampleDeathRecord.CODE1B = code;</para>
    // /// <para>// Getter:</para>
    // /// <para>Console.WriteLine($"\tCause of Death: {ExampleDeathRecord.CODE1B['display']}");</para>
    // /// </example>
    // [Property("CODE1B", Property.Types.Dictionary, "Death Certification", "Cause of Death Part I Code, Line b.", false, IGURL.CauseOfDeathPart1, false, 100)]
    // [PropertyParam("code", "The code used to describe this concept.")]
    // [PropertyParam("system", "The relevant code system.")]
    // [PropertyParam("display", "The human readable version of this code.")]
    // public Map<String, String> CODE1B
    // {
    //     get
    //     {
    //         if (CauseOfDeathConditionB != null && CauseOfDeathConditionB.getCode() != null)
    //         {
    //             return CodeableConceptToMap(CauseOfDeathConditionB.Code);
    //         }
    //         return EmptyCodeMap();
    //     }
    //     set
    //     {
    //         if(CauseOfDeathConditionB == null)
    //         {
    //             CauseOfDeathConditionB = CauseOfDeathCondition(1);
    //         }
    //         if (CauseOfDeathConditionB.getCode() != null)
    //         {
    //             CodeableConcept code = MapToCodeableConcept(value);
    //             code.Text = CauseOfDeathConditionB.Code.Text;
    //             CauseOfDeathConditionB.Code = code;
    //         }
    //         else
    //         {
    //             CauseOfDeathConditionB.Code = MapToCodeableConcept(value);
    //         }
    //                     }
    // }

    /// <summary>Cause of Death Part I, Line c.</summary>
    /// <value>the second underlying cause of death literal.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.COD1C = "Coronary artery thrombosis";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Cause: {ExampleDeathRecord.COD1C}");</para>
    /// </example>
    //  [Property("COD1C", Property.Types.String, "Death Certification", "Cause of Death Part I, Line c.", false, IGURL.CauseOfDeathPart1, false, 100)]
    private String COD1C;

    public String getCOD1C()
    {
        if (CauseOfDeathConditionC != null && CauseOfDeathConditionC.getValue() != null)
        {
            return (CodeableConceptToMap((CodeableConcept) CauseOfDeathConditionC.getValue())).get("text");
        }
        return null;
    }

    public void setCOD1C(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (CauseOfDeathConditionC == null)
        {
            CauseOfDeathConditionC = CauseOfDeathCondition(2);
        }
        CauseOfDeathConditionC.setValue(new CodeableConcept(new Coding(null, null, null)));//, value);
    }

    /// <summary>Cause of Death Part I Interval, Line c.</summary>
    /// <value>the second underlying cause of death approximate interval: onset to death.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.INTERVAL1C = "5 years";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Interval: {ExampleDeathRecord.INTERVAL1C}");</para>
    /// </example>
    //  [Property("INTERVAL1C", Property.Types.String, "Death Certification", "Cause of Death Part I Interval, Line c.", false, IGURL.CauseOfDeathPart1, false, 100)]
    private String INTERVAL1C;

    public String getINTERVAL1C()
    {
        if (CauseOfDeathConditionC != null && CauseOfDeathConditionC.getComponent() != null)
        {
            Observation.ObservationComponentComponent intervalComp = CauseOfDeathConditionC.getComponent().stream().filter((entry -> (entry).getCode() != null && (entry).getCode().equals("69440-6"))).findFirst().get();
            if (intervalComp != null && intervalComp.getValue() != null && intervalComp.getValue() instanceof StringType)
            {
                return intervalComp.getValue().toString();
            }
        }
        return null;
    }

    public void setINTERVAL1C(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (CauseOfDeathConditionC == null)
        {
            CauseOfDeathConditionC = CauseOfDeathCondition(2);
        }
        // Find correct component; if doesn't exist add another
        Observation.ObservationComponentComponent intervalComp = CauseOfDeathConditionC.getComponent().stream().filter(entry -> entry.getCode() != null && entry.getCode().equals("69440-6")).findFirst().get();
        if (intervalComp != null)
        {
            ((Observation.ObservationComponentComponent) intervalComp).setValue(new StringType(value));
        }
        else
        {
            Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
            CodeableConcept codeableConcept = new CodeableConcept();
            component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "69440-6", "Disease onset to death interval")));//, null);
            component.setValue(new StringType(value));
            CauseOfDeathConditionC.getComponent().add(component);
        }
    }

    // /// <summary>Cause of Death Part I Code, Line c.</summary>
    // /// <value>the second underlying cause of death coding. A Map representing a code, containing the following key/value pairs:
    // /// <para>"code" - the code</para>
    // /// <para>"system" - the code system this code belongs to</para>
    // /// <para>"display" - a human readable meaning of the code</para>
    // /// </value>
    // /// <example>
    // /// <para>// Setter:</para>
    // /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    // /// <para>code.add("code", "I21.9");</para>
    // /// <para>code.add("system", "http://hl7.org/fhir/sid/icd-10");</para>
    // /// <para>code.add("display", "Acute myocardial infarction, unspecified");</para>
    // /// <para>ExampleDeathRecord.CODE1C = code;</para>
    // /// <para>// Getter:</para>
    // /// <para>Console.WriteLine($"\tCause of Death: {ExampleDeathRecord.CODE1C['display']}");</para>
    // /// </example>
    // [Property("CODE1C", Property.Types.Dictionary, "Death Certification", "Cause of Death Part I Code, Line c.", false, IGURL.CauseOfDeathPart1, false, 100)]
    // [PropertyParam("code", "The code used to describe this concept.")]
    // [PropertyParam("system", "The relevant code system.")]
    // [PropertyParam("display", "The human readable version of this code.")]
    public Map<String, String> getCODE1C()
    {
        if (CauseOfDeathConditionC != null && CauseOfDeathConditionC.getCode() != null)
        {
            return CodeableConceptToMap(CauseOfDeathConditionC.getCode());
        }
        return EmptyCodeMap();
    }

    public void setCODE1C(Map<String, String> value)
    {
        if (CauseOfDeathConditionC == null)
        {
            CauseOfDeathConditionC = CauseOfDeathCondition(2);
        }
        if (CauseOfDeathConditionC.getCode() != null)
        {
            CodeableConcept code = MapToCodeableConcept(value);
            code.setText(CauseOfDeathConditionC.getCode().getText());
            CauseOfDeathConditionC.setCode(code);
        }
        else
        {
            CauseOfDeathConditionC.setCode(MapToCodeableConcept(value));
        }
    }


    /// <summary>Cause of Death Part I, Line d.</summary>
    /// <value>the third underlying cause of death literal.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.COD1D = "Atherosclerotic coronary artery disease";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Cause: {ExampleDeathRecord.COD1D}");</para>
    /// </example>
    //  [Property("COD1D", Property.Types.String, "Death Certification", "Cause of Death Part I, Line d.", false, IGURL.CauseOfDeathPart1, false, 100)]
    private String COD1D;

    public String getCOD1D()
    {
        if (CauseOfDeathConditionD != null && CauseOfDeathConditionD.getValue() != null)
        {
            return (CodeableConceptToMap((CodeableConcept) CauseOfDeathConditionD.getValue())).get("text");
        }
        return null;
    }

    public void setCOD1D(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (CauseOfDeathConditionD == null)
        {
            CauseOfDeathConditionD = CauseOfDeathCondition(3);
        }
        CauseOfDeathConditionD.setValue(new CodeableConcept(new Coding(null, null, null)));//, value);
    }

    /// <summary>Cause of Death Part I Interval, Line d.</summary>
    /// <value>the third underlying cause of death approximate interval: onset to death.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.INTERVAL1D = "7 years";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Interval: {ExampleDeathRecord.INTERVAL1D}");</para>
    /// </example>
    //  [Property("INTERVAL1D", Property.Types.String, "Death Certification", "Cause of Death Part I Interval, Line d.", false, IGURL.CauseOfDeathPart1, false, 100)]
    private String INTERVAL1D;

    public String getINTERVAL1D()
    {
        if (CauseOfDeathConditionD != null && CauseOfDeathConditionD.getComponent() != null)
        {
            Observation.ObservationComponentComponent intervalComp = CauseOfDeathConditionD.getComponent().stream().filter(entry -> entry.getCode() != null &&
                    entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("69440-6")).findFirst().get();
            if (intervalComp != null && intervalComp.getValue() != null && intervalComp.getValue() instanceof StringType)
            {
                return intervalComp.getValue().toString();
            }
        }
        return null;
    }

    public void setINTERVAL1D(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (CauseOfDeathConditionD == null)
        {
            CauseOfDeathConditionD = CauseOfDeathCondition(3);
        }
        // Find correct component; if doesn't exist add another
        Observation.ObservationComponentComponent intervalComp = CauseOfDeathConditionD.getComponent().stream().filter((entry -> entry.getCode() != null &&
                entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("69440-6");
        if (intervalComp != null)
        {
            ((Observation.ObservationComponentComponent) intervalComp).setValue(new StringType(value);
        }
        else
        {
            Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
            component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "69440-6", "Disease onset to death interval")));//, null);
            component.setValue(new StringType(value));
            CauseOfDeathConditionD.getComponent().add(component);
        }
    }

    // /// <summary>Cause of Death Part I Code, Line d.</summary>
    // /// <value>the third underlying cause of death coding. A Map representing a code, containing the following key/value pairs:
    // /// <para>"code" - the code</para>
    // /// <para>"system" - the code system this code belongs to</para>
    // /// <para>"display" - a human readable meaning of the code</para>
    // /// </value>
    // /// <example>
    // /// <para>// Setter:</para>
    // /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    // /// <para>code.add("code", "I21.9");</para>
    // /// <para>code.add("system", "http://hl7.org/fhir/sid/icd-10");</para>
    // /// <para>code.add("display", "Acute myocardial infarction, unspecified");</para>
    // /// <para>ExampleDeathRecord.CODE1D = code;</para>
    // /// <para>// Getter:</para>
    // /// <para>Console.WriteLine($"\tCause of Death: {ExampleDeathRecord.CODE1D['display']}");</para>
    // /// </example>
    // [Property("CODE1D", Property.Types.Dictionary, "Death Certification", "Cause of Death Part I Code, Line d.", false, IGURL.CauseOfDeathPart1, false, 100)]
    // [PropertyParam("code", "The code used to describe this concept.")]
    // [PropertyParam("system", "The relevant code system.")]
    // [PropertyParam("display", "The human readable version of this code.")]
    public Map<String, String> getCODE1D()
    {
        if (CauseOfDeathConditionD != null && CauseOfDeathConditionD.getCode() != null)
        {
            return CodeableConceptToMap(CauseOfDeathConditionD.getCode());
        }
        return EmptyCodeMap();
    }

    public void setCODE1D(Map<String, String> value)
    {
        if (CauseOfDeathConditionD == null)
        {
            CauseOfDeathConditionD = CauseOfDeathCondition(3);
        }
        if (CauseOfDeathConditionD.getCode() != null)
        {
            CodeableConcept code = MapToCodeableConcept(value);
            code.setText(CauseOfDeathConditionD.getCode().getText());
            CauseOfDeathConditionD.setCode(code);
        }
        else
        {
            CauseOfDeathConditionD.setCode(MapToCodeableConcept(value));
        }
    }


    /////////////////////////////////////////////////////////////////////////////////
    //
    // Record Properties: Decedent Demographics
    //
    /////////////////////////////////////////////////////////////////////////////////

    /// <summary>Decedent's Legal Name - Given. Middle name should be the last entry.</summary>
    /// <value>the decedent's name (first, etc., middle)</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>String[] names = { "Example", "Something", "Middle" };</para>
    /// <para>ExampleDeathRecord.GivenNames = names;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Given Name(s): {String.Join(", ", ExampleDeathRecord.GivenNames)}");</para>
    /// </example>

    //  [Property("Given Names", Property.Types.StringArr, "Decedent Demographics", "Decedent's Given Name(s).", true, IGURL.Decedent, true, 0)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "name")]
    private String[] GivenNames;

    public String[] getGivenNames()
    {
        String[] names = DeathCertificateDocumentUtil.GetAllString("Bundle.getEntry().getResource().stream().filter($this is Patient).getName().stream().filter(use='official').given");
        return names != null ? names : new String[0];
    }

    public void setGivenNames(String[] value)
    {
        updateGivenHumanName(value, Decedent.getName());
    }

    /// <summary>Decedent's Family Name.</summary>
    /// <value>the decedent's family name (i.e. last name)</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.FamilyName = "Last";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Last Name: {ExampleDeathRecord.FamilyName}");</para>
    /// </example>
    //  [Property("Family Name", Property.Types.String, "Decedent Demographics", "Decedent's Family Name.", true, IGURL.Decedent, true, 5)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "name")]
    private String FamilyName;

    public String getFamilyName()
    {
        return GetFirstString("Bundle.getEntry().getResource().stream().filter($this is Patient).getName().stream().filter(use='official').family");
    }

    public void setFamilyName(String value)
    {
        HumanName name = Decedent.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.OFFICIAL)).findFirst().get();
        if (name != null && isNullOrEmpty(value))
        {
            if (value.equals("UNKNOWN"))
                name.setFamily(null);
            else
                name.setFamily(value);
        }
        else if (isNullOrEmpty(value))
        {
            name = new HumanName();
            name.setUse(HumanName.NameUse.OFFICIAL);
            name.setFamily(value);
            Decedent.getName().add(name);
        }
    }

    /// <summary>Decedent's Suffix.</summary>
    /// <value>the decedent's suffix</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.Suffix = "Jr.";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Suffix: {ExampleDeathRecord.Suffix}");</para>
    /// </example>
    //  [Property("Suffix", Property.Types.String, "Decedent Demographics", "Decedent's Suffix.", true, IGURL.Decedent, true, 6)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "name")]
    private String Suffix;

    public String getSuffix()
    {
        return GetFirstString("Bundle.getEntry().getResource().stream().filter($this is Patient).getName().stream().filter(use='official').suffix");
    }

    public void setSuffix(StringType value)
    {
        HumanName name = Decedent.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.OFFICIAL)).findFirst().get();
        if (name != null && isNullOrEmpty(value.toString()))
        {
            StringType[] suffix = {value};
            name.setSuffix(Arrays.asList(suffix));
        }
        else if (isNullOrEmpty(value.toString()))
        {
            name = new HumanName();
            name.setUse(HumanName.NameUse.OFFICIAL);
            StringType[] suffix = {value};
            name.setSuffix(Arrays.asList(suffix));
            Decedent.getName().add(name);
        }
    }

    /// <summary>Decedent's Maiden Name.</summary>
    /// <value>the decedent's maiden name (i.e. last name before marriage)</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.MaidenName = "Last";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Maiden Name: {ExampleDeathRecord.MaidenName}");</para>
    /// </example>
    //  [Property("Maiden Name", Property.Types.String, "Decedent Demographics", "Decedent's Maiden Name.", true, IGURL.Decedent, true, 10)]
    //	[FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient).getName().stream().findAny(use='maiden')", "family")]
    private String MaidenName;

    public String getMaidenName()
    {
        return GetFirstString("Bundle.getEntry().getResource().stream().filter($this is Patient).getName().stream().filter(use='maiden').getText()");
    }

    public void setMaidenName(String value)
    {
        HumanName name = Decedent.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.MAIDEN)).findFirst().get();
        if (name != null && isNullOrEmpty(value))
        {
            name.setFamily(value);
        }
        else if (isNullOrEmpty(value))
        {
            name = new HumanName();
            name.setUse(HumanName.NameUse.MAIDEN);
            name.setText(value);
            Decedent.getName().add(name);
        }
    }

    /// <summary>Decedent's Gender.</summary>
    /// <value>the decedent's gender</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.Gender = "female";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Gender: {ExampleDeathRecord.Gender}");</para>
    /// </example>
    //  [Property("Gender", Property.Types.String, "Decedent Demographics", "Decedent's Gender.", true, IGURL.Decedent, true, 11)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "gender")]
    private String Gender;

    public String getGender()
    {
        return GetFirstString("Bundle.getEntry().getResource().stream().filter($this is Patient).gender");
    }

    public void setGender(String value)
    {
        switch (value)
        {
            case "male":
            case "Male":
            case "m":
            case "M":
                Decedent.setGender(AdministrativeGender.MALE);
                break;
            case "female":
            case "Female":
            case "f":
            case "F":
                Decedent.setGender(AdministrativeGender.FEMALE);
                break;
            case "other":
            case "Other":
            case "o":
            case "O":
                Decedent.setGender(AdministrativeGender.OTHER);
                break;
            case "unknown":
            case "Unknown":
            case "u":
            case "U":
                Decedent.setGender(AdministrativeGender.UNKNOWN);
                break;
        }
    }

    /// <summary>Decedent's Sex at Death.</summary>
    /// <value>the decedent's sex at time of death</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; sex = new Map&lt;String, String&gt;();</para>
    /// <para>sex.add("code", "female");</para>
    /// <para>sex.add("system", "http://hl7.org/fhir/administrative-gender");</para>
    /// <para>sex.add("display", "female");</para>
    /// <para>ExampleDeathRecord.SexAtDeath = sex;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Sex at Time of Death: {ExampleDeathRecord.SexAtDeath}");</para>
    /// </example>
    //  [Property("Sex At Death", Property.Types.Dictionary, "Decedent Demographics", "Decedent's Sex at Death.", true, IGURL.Decedent, true, 12)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient).getExtension().stream().findAny(url='http://hl7.org/fhir/us/vrdr/StructureDefinition/NVSS-SexAtDeath')", "")]
    private Map<String, String> SexAtDeath;

    public Map<String, String> getSexAtDeath()
    {
        if (Decedent != null)
        {
            Extension sex = Decedent.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.NVSSSexAtDeath);
            if (sex != null && sex.getValue() != null && sex.getValue() != null && sex.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept) sex.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setSexAtDeath(Map<String, String> value)
    {
        Decedent.getExtension().removeIf(ext -> ext.getUrl().equals(URL.ExtensionURL.NVSSSexAtDeath));
        if (isMapEmptyOrDefault(value) && Decedent.getExtension() == null)
        {
            return;
        }
        Extension sex = new Extension();
        sex.setUrl(URL.ExtensionURL.NVSSSexAtDeath);
        sex.setValue(MapToCodeableConcept(value));
        Decedent.getExtension().add(sex);
    }

    /// <summary>Decedent's Sex At Death Helper</summary>
    /// <value>Decedent's sex at death.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SexAtDeathHelper = ValueSets.AdministrativeGender.Male;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's SexAtDeathHelper: {ExampleDeathRecord.SexAtDeathHelper}");</para>
    /// </example>

    //  [Property("Sex At Death Helper", Property.Types.String, "Decedent Demographics", "Decedent's Sex At Death.", false, IGURL.Decedent, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient).getExtension().stream().findAny(url='http://hl7.org/fhir/us/vrdr/StructureDefinition/NVSS-SexAtDeath')", "")]
    private String SexAtDeathHelper;

    public String getSexAtDeathHelper()
    {
        if (SexAtDeath.containsKey("code") && isNullOrWhiteSpace(SexAtDeath.get("code")))
        {
            return SexAtDeath.get("code");
        }
        return null;
    }

    public void setSexAtDeathHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            DeathCertificateDocumentUtil.SetCodeValue("SexAtDeath", value, ValueSets.AdministrativeGender.Codes);
        }
    }

    // // Should this be removed for IG v1.3 updates?
    // /// <summary>Decedent's Birth Sex.</summary>
    // /// <value>the decedent's birth sex</value>
    // /// <example>
    // /// <para>// Setter:</para>
    // /// <para>ExampleDeathRecord.BirthSex = "F";</para>
    // /// <para>// Getter:</para>
    // /// <para>Console.WriteLine($"Birth Sex: {ExampleDeathRecord.BirthSex}");</para>
    // /// </example>
    // [Property("Birth Sex", Property.Types.String, "Decedent Demographics", "Decedent's Birth Sex.", true, IGURL.Decedent, true, 12)]
    // [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient).getExtension().stream().findAny(url='http://hl7.org/fhir/us/core/StructureDefinition/us-core-birthsex')", "")]
    // public String getBirthSex
    // {
    //     get
    //     {
    //         Extension birthsex = Decedent.getExtension().stream().filter(ext -> ext.getUrl().equals(OtherExtensionURL.BirthSex);
    //         if (birthsex != null && birthsex.getValue() != null && birthsex.getValue().GetType() == typeof(Code))
    //         {
    //             return ((Code)birthsex.getValue()).getValue();
    //         }
    //         return null;
    //     }
    //     set
    //     {
    //         Decedent.getExtension().removeIf(ext -> ext.getUrl().equals(OtherExtensionURL.BirthSex);
    //         Extension birthsex = new Extension();
    //         birthsex.Url = OtherExtensionURL.BirthSex;
    //         birthsex.setValue(new Code(value);
    //         Decedent.getExtension().add(birthsex);
    //     }
    // }


    private void AddBirthDateToDecedent()
    {
        Decedent.setBirthDateElement(new DateType());
        Decedent.getBirthDateElement().getExtension().add(NewBlankPartialDateTimeExtension(false));
    }

    /// <summary>Decedent's Year of Birth.</summary>
    /// <value>the decedent's year of birth, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.BirthYear = 1928;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Year of Birth: {ExampleDeathRecord.BirthYear}");</para>
    /// </example>
    //  [Property("BirthYear", Property.Types.Int32, "Decedent Demographics", "Decedent's Year of Birth.", true, IGURL.Decedent, true, 14)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient).birthDate", "")]

    private Integer BirthYear;

    public Integer getBirthYear()
    {
        if (Decedent != null && Decedent.getBirthDateElement() != null)
        {
            return GetDateFragmentOrPartialDate(Decedent.getBirthDateElement(), URL.ExtensionURL.DateYear);
        }
        return null;
    }

    public void setBirthYear(String value)
    {
        if (Decedent.getBirthDateElement() == null)
        {
            AddBirthDateToDecedent();
        }
        DeathCertificateDocumentUtil.SetPartialDate(Decedent.getBirthDateElement().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDate), URL.ExtensionURL.DateYear, value);
    }

    /// <summary>Decedent's Month of Birth.</summary>
    /// <value>the decedent's month of birth, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.BirthMonth = 11;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Month of Birth: {ExampleDeathRecord.BirthMonth}");</para>
    /// </example>
    //  [Property("BirthMonth", Property.Types.Int32, "Decedent Demographics", "Decedent's Month of Birth.", true, IGURL.Decedent, true, 14)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient).birthDate", "")]
    private Integer BirthMonth;

    public Integer getBirthMonth()
    {
        if (Decedent != null && Decedent.getBirthDateElement() != null)
        {
            return GetDateFragmentOrPartialDate(Decedent.getBirthDateElement(), URL.ExtensionURL.DateMonth);
        }
        return null;
    }

    public void setBirthMonth(String value)
    {
        if (Decedent.getBirthDateElement() == null)
        {
            AddBirthDateToDecedent();
        }
        SetPartialDate(Decedent.getBirthDateElement().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDate), URL.ExtensionURL.DateMonth, value);
    }

    /// <summary>Decedent's Day of Birth.</summary>
    /// <value>the decedent's day of birth, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.BirthDay = 11;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Day of Birth: {ExampleDeathRecord.BirthDay}");</para>
    /// </example>
    //  [Property("BirthDay", Property.Types.Int32, "Decedent Demographics", "Decedent's Day of Birth.", true, IGURL.Decedent, true, 14)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient).birthDate", "")]
    private Integer BirthDay;

    public Integer getBirthDay()
    {
        if (Decedent != null && Decedent.getBirthDateElement() != null)
        {
            return GetDateFragmentOrPartialDate(Decedent.getBirthDateElement(), URL.ExtensionURL.DateDay);
        }
        return null;
    }

    public void setBirthDay(String value)
    {
        if (Decedent.getBirthDateElement() == null)
        {
            AddBirthDateToDecedent();
        }
        SetPartialDate(Decedent.getBirthDateElement().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDate)).findFirst().get(), URL.ExtensionURL.DateDay, value);
    }

    /// <summary>Decedent's Date of Birth.</summary>
    /// <value>the decedent's date of birth</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DateOfBirth = "1940-02-19";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Date of Birth: {ExampleDeathRecord.DateOfBirth}");</para>
    /// </example>
    //  [Property("Date Of Birth", Property.Types.String, "Decedent Demographics", "Decedent's Date of Birth.", true, IGURL.Decedent, true, 14)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient).birthDate", "")]

    private String DateOfBirth;

    public String getDateOfBirth()
    {
        // We support this legacy API entrypoint via the new partial date entrypoints
        if (BirthYear != null && BirthYear != -1 && BirthMonth != null && BirthMonth != -1 && BirthDay != null && BirthDay != -1)
        {
            Date result = new Date((int) BirthYear, (int) BirthMonth, (int) BirthDay);
            return result.toString();
        }
        return null;
    }

    public void setDateOfBirth(String value)
    {
        // We support this legacy API entrypoint via the new partial date entrypoints
        OffsetDateTime parsedDate = OffsetDateTime.parse(value);
        if (parsedDate != null)
        {
            BirthYear = parsedDate.getYear();
            BirthMonth = parsedDate.getMonthValue();
            BirthDay = parsedDate.getDayOfMonth();
        }
    }

    /// <summary>Decedent's Residence.</summary>
    /// <value>Decedent's Residence. A Map representing residence address, containing the following key/value pairs:
    /// <para>"addressLine1" - address, line one</para>
    /// <para>"addressLine2" - address, line two</para>
    /// <para>"addressCity" - address, city</para>
    /// <para>"addressCounty" - address, county</para>
    /// <para>"addressState" - address, state</para>
    /// <para>"addressZip" - address, zip</para>
    /// <para>"addressCountry" - address, country</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; address = new Map&lt;String, String&gt;();</para>
    /// <para>address.add("addressLine1", "123 Test Street");</para>
    /// <para>address.add("addressLine2", "Unit 3");</para>
    /// <para>address.add("addressCity", "Boston");</para>
    /// <para>address.add("addressCityC", "1234");</para>
    /// <para>address.add("addressCounty", "Suffolk");</para>
    /// <para>address.add("addressState", "MA");</para>
    /// <para>address.add("addressZip", "12345");</para>
    /// <para>address.add("addressCountry", "US");</para>
    /// <para>SetterDeathRecord.Residence = address;</para> (addressStnum, 6)
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"State of residence: {ExampleDeathRecord.Residence["addressState"]}");</para>
    /// </example>
    //  [Property("Residence", Property.Types.Dictionary, "Decedent Demographics", "Decedent's residence.", true, IGURL.Decedent, true, 19)]
    //  [PropertyParam("addressLine1", "address, line one")]
    //  [PropertyParam("addressLine2", "address, line two")]
    //  [PropertyParam("addressCity", "address, city")]
    //  [PropertyParam("addressCityC", "address, _city")]
    //  [PropertyParam("addressCounty", "address, county")]
    //  [PropertyParam("addressState", "address, state")]
    //  [PropertyParam("addressZip", "address, zip")]
    //  [PropertyParam("addressCountry", "address, country")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "address")]
    private Map<String, StringType> Residence;

    public Map<String, StringType> getResidence()
    {
        if (Decedent != null && Decedent.getAddress() != null && Decedent.getAddress().size() > 0)
        {
            Map<String, StringType> address = addressToMap(Decedent.getAddress().get(0));
            return address;
        }
        return DeathCertificateDocumentUtil.EmptyAddrMap();
    }

    public void setResidence(Map<String, StringType> value)
    {
        if (Decedent.getAddress() == null)
        {
            Decedent.setAddress(new ArrayList());
        }
        // Clear out the address since we're replacing it completely, except we need to keep the "WithinCityLimits" extension if present
        Extension withinCityLimits = Decedent.getAddress() != null ? Decedent.getAddress().get(0) != null ? Decedent.getAddress().get(0).getExtension() != null ? Decedent.getAddress().get(0).getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.WithinCityLimitsIndicator)).findFirst().get() : null : null : null;
        Decedent.getAddress().clear();
        Decedent.getAddress().add(mapToAddress(value));
        if (withinCityLimits != null)
            Decedent.getAddress().get(0).getExtension().add(withinCityLimits);

        // Now encode -
        //        Address.Country as PH_Country_GEC
        //        Adress.County as PHVS_DivisionVitalStatistics__County
        //        Address.City as 5 digit code as per FIPS 55-3, which are included as the preferred alternate code in https://phinvads.cdc.gov/vads/ViewValueSet.action?id=D06EE94C-4D4C-440A-AD2A-1C3CB35E6D08#
        //Address a = Decedent.getAddress().get(0);
    }

    /// <summary>Decedent's residence is/is not within city limits.</summary>
    /// <value>Decedent's residence is/is not within city limits. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para></value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; within = new Map&lt;String, String&gt;();</para>
    /// <para>within.add("code", "Y");</para>
    /// <para>within.add("system", CodeSystems.PH_YesNo_HL7_2x);</para>
    /// <para>within.add("display", "Yes");</para>
    /// <para>SetterDeathRecord.ResidenceWithinCityLimits = within;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Residence within city limits: {ExampleDeathRecord.ResidenceWithinCityLimits['display']}");</para>
    /// </example>
    //  [Property("Residence Within City Limits", Property.Types.Dictionary, "Decedent Demographics", "Decedent's residence is/is not within city limits.", true, IGURL.Decedent, true, 20)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "address")]
    private Map<String, String> ResidenceWithinCityLimits;

    public Map<String, String> getResidenceWithinCityLimits()
    {
        if (Decedent != null && Decedent.getAddress().get(0) != null)
        {
            Extension cityLimits = Decedent.getAddress().get(0).getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.WithinCityLimitsIndicator)).findFirst().get();
            if (cityLimits != null && cityLimits.getValue() != null && cityLimits.getValue() instanceof Coding)
            {
                return CodingToDict((Coding) cityLimits.getValue());
            }
        }
        return DeathCertificateDocumentUtil.EmptyCodeMap();
    }

    public void setResidenceWithinCityLimits(Map<String, String> value)
    {
        if (Decedent != null)
        {
            if (Decedent.getAddress().get(0) == null)
            {
                Decedent.getAddress().add(new Address());
            }
            Decedent.getAddress().get(0).getExtension().removeIf(ext -> ext.getUrl().equals(URL.ExtensionURL.WithinCityLimitsIndicator));
            Extension withinCityLimits = new Extension();
            withinCityLimits.setUrl(URL.ExtensionURL.WithinCityLimitsIndicator);
            withinCityLimits.setValue(MapToCoding(value));
            Decedent.getAddress().get(0).getExtension().add(withinCityLimits);
        }
    }

    /// <summary>Residence Within City Limits Helper</summary>
    /// <value>Residence Within City Limits.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.ResidenceWithinCityLimitsHelper = ValueSets.YesNoUnknown.Y;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Residence within city limits: {ExampleDeathRecord.ResidenceWithinCityLimitsHelper}");</para>
    /// </example>
    //  [Property("ResidenceWithinCityLimits Helper", Property.Types.String, "Decedent Demographics", "Decedent's ResidenceWithinCityLimits.", false, IGURL.Decedent, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "address")]
    private String ResidenceWithinCityLimitsHelper;

    public String getResidenceWithinCityLimitsHelper()
    {
        if (ResidenceWithinCityLimits.containsKey("code") && isNullOrWhiteSpace(ResidenceWithinCityLimits.get("code")))
        {
            return ResidenceWithinCityLimits.get("code");
        }
        return null;
    }

    public void setResidenceWithinCityLimitsHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("ResidenceWithinCityLimits", value, ValueSets.YesNoUnknown.Codes);
        }
    }

    /// <summary>Decedent's Social Security Number.</summary>
    /// <value>the decedent's social security number, without dashes.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SSN = "12345678";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Suffix: {ExampleDeathRecord.SSN}");</para>
    /// </example>
    //  [Property("SSN", Property.Types.String, "Decedent Demographics", "Decedent's Social Security Number.", true, IGURL.Decedent, true, 13)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient).getIdentifier().stream().findAny(system='http://hl7.org/fhir/sid/us-ssn')", "")]
    private String SSN;

    public String getSSN()
    {
        return GetFirstString("Bundle.getEntry().getResource().stream().filter($this is Patient).getIdentifier().stream().filter(system = 'http://hl7.org/fhir/sid/us-ssn').getValue()");
    }

    public void setSSN(String value)
    {
        Decedent.getIdentifier().removeIf(iden -> iden.getSystem().equals(CodeSystems.US_SSN));
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        Identifier ssn = new Identifier();
        ssn.setType(new CodeableConcept(new Coding(CodeSystems.HL7_identifier_type, "SB", "Social Beneficiary Identifier")));//, null));
        ssn.setSystem(CodeSystems.US_SSN);
        ssn.setValue(value.replace("-", ""));
        Decedent.getIdentifier().add(ssn);
    }

    /// <summary>Decedent's Ethnicity Hispanic Mexican.</summary>
    /// <value>the decedent's ethnicity. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; ethnicity = new Map&lt;String, String&gt;();</para>
    /// <para>ethnicity.add("code", "Y");</para>
    /// <para>ethnicity.add("system", CodeSystems.YesNo);</para>
    /// <para>ethnicity.add("display", "Yes");</para>
    /// <para>ExampleDeathRecord.Ethnicity = ethnicity;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Ethnicity: {ExampleDeathRecord.Ethnicity1['display']}");</para>
    /// </example>
    //  [Property("Ethnicity1", Property.Types.Dictionary, "Decedent Demographics", "Decedent's Ethnicity Hispanic Mexican.", true, IGURL.InputRaceAndEthnicity, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='inputraceandethnicity')", "")]
    private Map<String, String> Ethnicity1;

    public Map<String, String> getEthnicity1()
    {
        if (InputRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent ethnicity = InputRaceAndEthnicityObs.getComponent().stream().filter((c -> c.getCode().getCoding().get(0).getCode().equals(NvssEthnicity.Mexican));
            if (ethnicity != null && ethnicity.getValue() != null && ethnicity.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept) ethnicity.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setEthnicity1(Map<String, String> value)
    {
        if (InputRaceAndEthnicityObs == null)
        {
            CreateInputRaceEthnicityObs();
        }
        InputRaceAndEthnicityObs.getComponent().removeIf(c -> c.getCode().getCoding().get(0).getCode().equals(NvssEthnicity.Mexican));
        Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode, NvssEthnicity.Mexican, NvssEthnicity.MexicanDisplay)));//, null));
        component.setValue(MapToCodeableConcept(value));
        InputRaceAndEthnicityObs.getComponent().add(component);
    }

    /// <summary>Decedent's Ethnicity 1 Helper</summary>
    /// <value>Decedent's Ethnicity 1.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.EthnicityLevel = ValueSets.YesNoUnknown.Yes;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Ethnicity: {ExampleDeathRecord.Ethnicity1Helper}");</para>
    /// </example>
    //  [Property("Ethnicity 1 Helper", Property.Types.String, "Decedent Demographics", "Decedent's Ethnicity 1.", false, IGURL.InputRaceAndEthnicity, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='inputraceandethnicity')", "")]
    private String Ethnicity1Helper;

    public String getEthnicity1Helper()
    {
        if (Ethnicity1.containsKey("code") && isNullOrWhiteSpace(Ethnicity1.get("code")))
        {
            return Ethnicity1.get("code");
        }
        return null;
    }

    public void setEthnicity1Helper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("Ethnicity1", value, ValueSets.HispanicNoUnknown.Codes);
        }
    }

    /// <summary>Decedent's Ethnicity Hispanic Puerto Rican.</summary>
    /// <value>the decedent's ethnicity. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; ethnicity = new Map&lt;String, String&gt;();</para>
    /// <para>ethnicity.add("code", "Y");</para>
    /// <para>ethnicity.add("system", CodeSystems.YesNo);</para>
    /// <para>ethnicity.add("display", "Yes");</para>
    /// <para>ExampleDeathRecord.Ethnicity2 = ethnicity;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Ethnicity: {ExampleDeathRecord.Ethnicity2['display']}");</para>
    /// </example>
    //  [Property("Ethnicity2", Property.Types.Dictionary, "Decedent Demographics", "Decedent's Ethnicity Hispanic Puerto Rican.", true, IGURL.InputRaceAndEthnicity, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='inputraceandethnicity')", "")]
    private Map<String, String> Ethnicity2;

    public Map<String, String> getEthnicity2()
    {
        if (InputRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent ethnicity = InputRaceAndEthnicityObs.getComponent().stream().filter((c -> c.getCode().getCoding().get(0).getCode().equals(NvssEthnicity.PuertoRican));
            if (ethnicity != null && ethnicity.getValue() != null && ethnicity.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept) ethnicity.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setEthnicity2(Map<String, String> value)
    {
        if (InputRaceAndEthnicityObs == null)
        {
            CreateInputRaceEthnicityObs();
        }
        InputRaceAndEthnicityObs.getComponent().removeIf(c -> c.getCode().getCoding().get(0).getCode().equals(NvssEthnicity.PuertoRican));
        Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode, NvssEthnicity.PuertoRican, NvssEthnicity.PuertoRicanDisplay)));//, null));
        component.setValue(MapToCodeableConcept(value));
        InputRaceAndEthnicityObs.getComponent().add(component);
    }

    /// <summary>Decedent's Ethnicity 2 Helper</summary>
    /// <value>Decedent's Ethnicity 2.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.Ethnicity2Helper = "Y";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Ethnicity: {ExampleDeathRecord.Ethnicity1Helper}");</para>
    /// </example>
    //  [Property("Ethnicity 2 Helper", Property.Types.String, "Decedent Demographics", "Decedent's Ethnicity 2.", false, IGURL.InputRaceAndEthnicity, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='inputraceandethnicity')", "")]
    private String Ethnicity2Helper;

    public String getEthnicity2Helper()
    {
        if (Ethnicity2.containsKey("code") && isNullOrWhiteSpace(Ethnicity2.get("code")))
        {
            return Ethnicity2.get("code");
        }
        return null;
    }

    public void setEthnicity2Helper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("Ethnicity2", value, ValueSets.HispanicNoUnknown.Codes);
        }
    }

    /// <summary>Decedent's Ethnicity Hispanic Cuban.</summary>
    /// <value>the decedent's ethnicity. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; ethnicity = new Map&lt;String, String&gt;();</para>
    /// <para>ethnicity.add("code", "Y");</para>
    /// <para>ethnicity.add("system", CodeSystems.YesNo);</para>
    /// <para>ethnicity.add("display", "Yes");</para>
    /// <para>ExampleDeathRecord.Ethnicity3 = ethnicity;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Ethnicity: {ExampleDeathRecord.Ethnicity3['display']}");</para>
    /// </example>
    //  [Property("Ethnicity3", Property.Types.Dictionary, "Decedent Demographics", "Decedent's Ethnicity Hispanic Cuban.", true, IGURL.InputRaceAndEthnicity, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='inputraceandethnicity')", "")]
    private Map<String, String> Ethnicity3;

    public Map<String, String> getEthnicity3()
    {
        if (InputRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent ethnicity = InputRaceAndEthnicityObs.getComponent().stream().filter((c -> c.getCode().getCoding().get(0).getCode().equals(NvssEthnicity.Cuban));
            if (ethnicity != null && ethnicity.getValue() != null && ethnicity.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept) ethnicity.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setEthnicity3(Map<String, String> value)
    {
        if (InputRaceAndEthnicityObs == null)
        {
            CreateInputRaceEthnicityObs();
        }
        InputRaceAndEthnicityObs.getComponent().removeIf(c -> c.getCode().getCoding().get(0).getCode().equals(NvssEthnicity.Cuban));
        Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode, NvssEthnicity.Cuban, NvssEthnicity.CubanDisplay)));//, null));
        component.setValue(MapToCodeableConcept(value));
        InputRaceAndEthnicityObs.getComponent().add(component);
    }

    /// <summary>Decedent's Ethnicity 3 Helper</summary>
    /// <value>Decedent's Ethnicity 3.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.Ethnicity3Helper = "Y";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Ethnicity: {ExampleDeathRecord.Ethnicity3Helper}");</para>
    /// </example>
    //  [Property("Ethnicity 3 Helper", Property.Types.String, "Decedent Demographics", "Decedent's Ethnicity 3.", false, IGURL.InputRaceAndEthnicity, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='inputraceandethnicity')", "")]
    private String Ethnicity3Helper;

    public String getEthnicity3Helper()
    {
        if (Ethnicity3.containsKey("code") && isNullOrWhiteSpace(Ethnicity3.get("code")))
        {
            return Ethnicity3.get("code");
        }
        return null;
    }

    public void setEthnicity3Helper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("Ethnicity3", value, ValueSets.HispanicNoUnknown.Codes);
        }
    }

    /// <summary>Decedent's Ethnicity Hispanic Other.</summary>
    /// <value>the decedent's ethnicity. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; ethnicity = new Map&lt;String, String&gt;();</para>
    /// <para>ethnicity.add("code", "Y");</para>
    /// <para>ethnicity.add("system", CodeSystems.YesNo);</para>
    /// <para>ethnicity.add("display", "Yes");</para>
    /// <para>ExampleDeathRecord.Ethnicity4 = ethnicity;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Ethnicity: {ExampleDeathRecord.Ethnicity4['display']}");</para>
    /// </example>
    //  [Property("Ethnicity4", Property.Types.Dictionary, "Decedent Demographics", "Decedent's Ethnicity Hispanic Other.", true, IGURL.InputRaceAndEthnicity, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='inputraceandethnicity')", "")]
    private Map<String, String> Ethnicity4;

    public Map<String, String> getEthnicity4()
    {
        if (InputRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent ethnicity = InputRaceAndEthnicityObs.getComponent().stream().filter(c -> c.getCode().getCoding().get(0).getCode().equals(NvssEthnicity.Other)).findFirst().get();
            if (ethnicity != null && ethnicity.getValue() != null && ethnicity.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept) ethnicity.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setEthnicity4(Map<String, String> value)
    {
        if (InputRaceAndEthnicityObs == null)
        {
            CreateInputRaceEthnicityObs();
        }
        InputRaceAndEthnicityObs.getComponent().removeIf(c -> c.getCode().getCoding().get(0).getCode().equals(NvssEthnicity.Other));
        Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode, NvssEthnicity.Other, NvssEthnicity.OtherDisplay)));//, null));
        component.setValue(MapToCodeableConcept(value));
        InputRaceAndEthnicityObs.getComponent().add(component);
    }

    /// <summary>Decedent's Ethnicity 4 Helper</summary>
    /// <value>Decedent's Ethnicity 4.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.Ethnicity4Helper = "Y";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Ethnicity: {ExampleDeathRecord.Ethnicity4Helper}");</para>
    /// </example>
    //  [Property("Ethnicity 4 Helper", Property.Types.String, "Decedent Demographics", "Decedent's Ethnicity 4.", false, IGURL.InputRaceAndEthnicity, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='inputraceandethnicity')", "")]
    private String Ethnicity4Helper;

    public String getEthnicity4Helper()
    {
        if (Ethnicity4.containsKey("code") && isNullOrWhiteSpace(Ethnicity4.get("code")))
        {
            return Ethnicity4.get("code");
        }
        return null;
    }

    public void setEthnicity4Helper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("Ethnicity4", value, ValueSets.HispanicNoUnknown.Codes);
        }
    }

    /// <summary>Decedent's Ethnicity Hispanic Literal.</summary>
    /// <value>the decedent's ethnicity. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.EthnicityLiteral = ethnicity;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Ethnicity: {ExampleDeathRecord.Ethnicity4['display']}");</para>
    /// </example>
    //  [Property("EthnicityLiteral", Property.Types.String, "Decedent Demographics", "Decedent's Ethnicity Literal.", true, IGURL.InputRaceAndEthnicity, false, 34)]
    //  [PropertyParam("ethnicity", "The literal String to describe ethnicity.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='inputraceandethnicity')", "")]
    private String EthnicityLiteral;

    public String getEthnicityLiteral()
    {
        if (InputRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent ethnicity = InputRaceAndEthnicityObs.getComponent().stream().filter(c -> c.getCode().getCoding().get(0).getCode().equals(NvssEthnicity.Literal)).findFirst().get();
            if (ethnicity != null && ethnicity.getValue() != null && ethnicity.getValue() instanceof StringType)
            {
                return ethnicity.getValue().toString();
            }
        }
        return null;
    }

    public void setEthnicityLiteral(String value)
    {
        if (InputRaceAndEthnicityObs == null)
        {
            CreateInputRaceEthnicityObs();
        }
        InputRaceAndEthnicityObs.getComponent().removeIf(c -> c.getCode().getCoding().get(0).getCode().equals(NvssEthnicity.Literal));
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode, NvssEthnicity.Literal, NvssEthnicity.LiteralDisplay)));//, null));
        component.setValue(new StringType(value));
        InputRaceAndEthnicityObs.getComponent().add(component);
    }

    /// <summary>Decedent's Race values.</summary>
    /// <value>the decedent's race. A tuple, where the first value of the tuple is the display value, and the second is
    /// the IJE code Y or N.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.Race = {NvssRace.BlackOrAfricanAmerican, "Y"};</para>
    /// <para>// Getter:</para>
    /// <para>String boaa = ExampleDeathRecord.RaceBlackOfAfricanAmerican;</para>
    /// </example>
    //  [Property("Race", Property.Types.TupleArr, "Decedent Demographics", "Decedent's Race", true, IGURL.InputRaceAndEthnicity, true, 38)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='inputraceandethnicity')", "")]
    private Tuple<String, String>[] Race;

    public Tuple<String, String>[] getRace()
    {
        // filter the boolean race values
        var booleanRaceCodes = NvssRace.GetBooleanRaceCodes();
        List<String> raceCodes = booleanRaceCodes.Concat(NvssRace.GetLiteralRaceCodes()).ToList();

        var races = new ArrayList<Tuple<String, String>>()
        {
        };

        if (InputRaceAndEthnicityObs == null)
        {
            return races.toArray();
        }
        for (String raceCode:raceCodes)
        {
            Observation.ObservationComponentComponent component = InputRaceAndEthnicityObs.getComponent().stream().filter(c -> c.getCode().getCoding().get(0).getCode().equals(raceCode)).findFirst().get();
            if (component != null)
            {
                // convert boolean race codes to Strings
                if (booleanRaceCodes.contains(raceCode))
                {
                    if (component.getValue() == null)
                    {
                        // If there is no value given, set the race to blank.
                        var race = Tuple.Create(raceCode, "");
                        races.add(race);
                        continue;
                    }

                    // Todo Find conversion from BooleanType to bool
                    String raceboolean = ((BooleanType) component.getValue()).toString();

                    if (Convert.ToBoolean(raceBool))
                    {
                        var race = Tuple.Create(raceCode, "Y");
                        races.add(race);
                    } else {
                        var race = Tuple.Create(raceCode, "N");
                        races.add(race);
                    }
                } else {
                    // Ignore unless there's a value present
                    if (component.getValue() != null)
                    {
                        var race = Tuple.Create(raceCode, component.getValue().toString());
                        races.add(race);
                    }
                }

            }
        }
        return races.toArray();
    }

    public void setRace(String value)
    {
        if (InputRaceAndEthnicityObs == null)
        {
            CreateInputRaceEthnicityObs();
        }
        var booleanRaceCodes = NvssRace.GetBooleanRaceCodes();
        var literalRaceCodes = NvssRace.GetLiteralRaceCodes();
        for (Tuple<String, String> element : value)
        {
            InputRaceAndEthnicityObs.getComponent().removeIf(c -> c.getCode().getCoding().get(0).getCode().equals(element.));
            Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
            String displayValue = NvssRace.GetDisplayValueForCode(element.getItem1());
            component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode, element.getItem1()), displayValue));//, null);
            if (booleanRaceCodes.contains(element.Item1))
            {
                if (element.Item2 == "Y")
                {
                    component.setValue(new BooleanType(true));
                }
                else
                {
                    component.setValue(new BooleanType(false));
                }
            } else if (literalRaceCodes.contains(element.getItem1()))
            {
                component.setValue(new StringType(element.Item2));
            }
            else
            {
                throw new ArgumentException("Invalid race literal code found: " + element.Item1 + " with value: " + element.Item2);
            }
            InputRaceAndEthnicityObs.getComponent().add(component);
        }
    }

    /// <summary>Decedent's Race MissingValueReason.</summary>
    /// <value>why the decedent's race is missing. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; mvr = new Map&lt;String, String&gt;();</para>
    /// <para>mvr.add("code", "R");</para>
    /// <para>mvr.add("system", CodeSystems.MissingValueReason);</para>
    /// <para>mvr.add("display", "Refused");</para>
    /// <para>ExampleDeathRecord.RaceMissingValueReason = mvr;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Missing Race: {ExampleDeathRecord.RaceMissingValueReason['display']}");</para>
    /// </example>
    //  [Property("RaceMissingValueReason", Property.Types.Dictionary, "Decedent Demographics", "Decedent's Race MissingValueReason.", true, IGURL.InputRaceAndEthnicity, true, 38)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='MissingValueReason')", "")]
    public Map<String, String> RaceMissingValueReason;

    public Map<String, String> getRaceMissingValueReason()
    {
        if (InputRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent raceMVR = InputRaceAndEthnicityObs.getComponent().stream().filter(c -> c.getCode().getCoding().get(0).getCode().equals(NvssRace.MissingValueReason)).findFirst().get();
            if (raceMVR != null && raceMVR.getValue() != null && raceMVR.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept) raceMVR.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setRaceMissingValueReason(Map<String, String> value)
    {
        if (InputRaceAndEthnicityObs == null)
        {
            CreateInputRaceEthnicityObs();
        }
        InputRaceAndEthnicityObs.getComponent().removeIf(c -> c.getCode().getCoding().get(0).getCode().equals(NvssRace.MissingValueReason));
        Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode, NvssRace.MissingValueReason, NvssRace.MissingValueReason)));//, null);
        component.setValue(MapToCodeableConcept(value));
        InputRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Decedent's RaceMissingValueReason</summary>
    /// <value>Decedent's RaceMissingValueReason.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.RaceMissingValueReasonHelper = ValueSets.RaceMissingValueReason.R;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's RaceMissingValueReason: {ExampleDeathRecord.RaceMissingValueReasonHelper}");</para>
    /// </example>
    //  [Property("RaceMissingValueReasonHelper", Property.Types.String, "Decedent Demographics", "Decedent's Race MissingValueReason.", false, IGURL.InputRaceAndEthnicity, true, 38)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='MissingValueReason')", "")]
    private String RaceMissingValueReasonHelper;

    public String getRaceMissingValueReasonHelper()
    {
        if (RaceMissingValueReason.containsKey("code") && isNullOrWhiteSpace(RaceMissingValueReason.get("code")))
        {
            return RaceMissingValueReason.get("code");
        }
        return null;
    }

    public void setRaceMissingValueReasonHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("RaceMissingValueReason", value, ValueSets.RaceMissingValueReason.Codes);
        }
    }

    /// <summary>Decedent's Place Of Birth.</summary>
    /// <value>decedent's Place Of Birth. A Map representing residence address, containing the following key/value pairs:
    /// <para>"addressLine1" - address, line one</para>
    /// <para>"addressLine2" - address, line two</para>
    /// <para>"addressCity" - address, city</para>
    /// <para>"addressCounty" - address, county</para>
    /// <para>"addressState" - address, state</para>
    /// <para>"addressZip" - address, zip</para>
    /// <para>"addressCountry" - address, country</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; address = new Map&lt;String, String&gt;();</para>
    /// <para>address.add("addressLine1", "123 Test Street");</para>
    /// <para>address.add("addressLine2", "Unit 3");</para>
    /// <para>address.add("addressCity", "Boston");</para>
    /// <para>address.add("addressCounty", "Suffolk");</para>
    /// <para>address.add("addressState", "MA");</para>
    /// <para>address.add("addressZip", "12345");</para>
    /// <para>address.add("addressCountry", "US");</para>
    /// <para>SetterDeathRecord.PlaceOfBirth = address;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"State where decedent was born: {ExampleDeathRecord.PlaceOfBirth["placeOfBirthState"]}");</para>
    /// </example>
    //  [Property("Place Of Birth", Property.Types.Dictionary, "Decedent Demographics", "Decedent's Place Of Birth.", true, IGURL.Decedent, true, 15)]
    //  [PropertyParam("addressLine1", "address, line one")]
    //  [PropertyParam("addressLine2", "address, line two")]
    //  [PropertyParam("addressCity", "address, city")]
    //  [PropertyParam("addressCounty", "address, county")]
    //  [PropertyParam("addressState", "address, state")]
    //  [PropertyParam("addressZip", "address, zip")]
    //  [PropertyParam("addressCountry", "address, country")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient).getExtension().stream().findAny(url='" + OtherExtensionURL.PatientBirthPlace + "')", "")]
    private Map<String, StringType> PlaceOfBirth;

    public Map<String, StringType> getPlaceOfBirth()
    {
        if (Decedent != null)
        {
            Extension addressExt = Decedent.getExtension().stream().filter(extension -> extension.getUrl().equals(URL.OtherExtensionURL.PatientBirthPlace)).findFirst().get();
            if (addressExt != null)
            {
                Address address = (Address) addressExt.getValue();
                if (address != null)
                {
                    return addressToMap(address);
                }
                return EmptyAddrMap();
            }
        }
        return EmptyAddrMap();
    }

    public void setPlaceOfBirth(Map<String, StringType> value)
    {
        Decedent.getExtension().removeIf(ext -> ext.getUrl().equals(URL.OtherExtensionURL.PatientBirthPlace));
        if (!DeathCertificateDocumentUtil.isMapEmptyOrDefault(value))
        {
            Extension placeOfBirthExt = new Extension();
            placeOfBirthExt.setUrl(URL.OtherExtensionURL.PatientBirthPlace);
            placeOfBirthExt.setValue(mapToAddress(value));
            Decedent.getExtension().add(placeOfBirthExt);
        }
    }

    /// <summary>The informant of the decedent's death.</summary>
    /// <value>String representation of the informant's relationship to the decedent
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; relationship = new Map&lt;String, String&gt;();</para>
    /// <para>relationship.add("text", "sibling");</para>
    /// <para>SetterDeathRecord.ContactRelationship = relationship;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Contact's Relationship: {ExampleDeathRecord.ContactRelationship["text"]}");</para>
    /// </example>
    //  [Property("Contact Relationship", Property.Types.Dictionary, "Decedent Demographics", "The informant's relationship to the decedent", true, IGURL.Decedent, true, 24)]
    //  [PropertyParam("relationship", "The relationship to the decedent.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "contact")]
    private Map<String, String> ContactRelationship;

    public Map<String, String> getContactRelationship()
    {
        if (Decedent != null && Decedent.getContact() != null)
        {
            Patient.ContactComponent contact = Decedent.getContact().get(0);
            if (contact != null && contact.getRelationship() != null)
            {
                return CodeableConceptToMap(contact.getRelationship().get(0));
            }
        }
        return EmptyCodeableMap();
    }

    public void setContactRelationship(Map<String, String> value)
    {
        Patient.ContactComponent component = new Patient.ContactComponent();
        component.getRelationship().add(MapToCodeableConcept(value));
        Decedent.getContact().add(component);
    }


    /// <summary>The marital status of the decedent at the time of death.</summary>
    /// <value>the marital status of the decedent at the time of death. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code describing this finding</para>
    /// <para>"system" - the system the given code belongs to</para>
    /// <para>"display" - the human readable display text that corresponds to the given code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    /// <para>code.add("code", "S");</para>
    /// <para>code.add("system", "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus");</para>
    /// <para>code.add("display", "Never Married");</para>
    /// <para>ExampleDeathRecord.MaritalStatus = code;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Marital status: {ExampleDeathRecord.MaritalStatus["display"]}");</para>
    /// </example>
    //  [Property("Marital Status", Property.Types.Dictionary, "Decedent Demographics", "The marital status of the decedent at the time of death.", true, IGURL.Decedent, true, 24)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "maritalStatus")]
    private Map<String, String> MaritalStatus;

    public Map<String, String> getMaritalStatus()
    {
        if (Decedent != null && Decedent.getMaritalStatus() != null && Decedent.getMaritalStatus() != null)
        {
            return CodeableConceptToMap((CodeableConcept) Decedent.getMaritalStatus());
        }
        return EmptyCodeableMap();
    }

    public void setMaritalStatus(Map<String, String> value)
    {
        if (Decedent.getMaritalStatus() == null)
        {
            Decedent.setMaritalStatus(MapToCodeableConcept(value));
        }
        else
        {
            // Need to keep any existing text or extension that could be there
            String text = Decedent.getMaritalStatus().getText();
            List<Extension> extensions = Decedent.getMaritalStatus().getExtension().stream().filter(e -> true).collect(Collectors.toList());
            Decedent.setMaritalStatus(MapToCodeableConcept(value));
            Decedent.getMaritalStatus().getExtension().addAll(extensions);
            Decedent.getMaritalStatus().setText(text);
        }
    }

    /// <summary>The marital status edit flag.</summary>
    /// <value>the marital status edit flag
    /// <para>"code" - the code describing this finding</para>
    /// <para>"system" - the system the given code belongs to</para>
    /// <para>"display" - the human readable display text that corresponds to the given code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    /// <para>code.add("code", "S");</para>
    /// <para>code.add("system", "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus");</para>
    /// <para>code.add("display", "Never Married");</para>
    /// <para>ExampleDeathRecord.MaritalStatus = code;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Marital status: {ExampleDeathRecord.MaritalStatus["display"]}");</para>
    /// </example>
    //  [Property("Marital Status Edit Flag", Property.Types.Dictionary, "Decedent Demographics", "The marital status edit flag.", true, IGURL.Decedent, true, 24)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "maritalStatus")]
    private Map<String, String> MaritalStatusEditFlag;

    public Map<String, String> getMaritalStatusEditFlag()
    {
        if (Decedent != null && Decedent.getMaritalStatus() != null && Decedent.getMaritalStatus().getExtension().get(0) != null)
        {
            Extension addressExt = Decedent.getMaritalStatus().getExtension().stream().filter(extension -> extension.getUrl().equals(URL.ExtensionURL.BypassEditFlag)).findFirst().get();
            if (addressExt != null && addressExt.getValue() != null && addressExt.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept) addressExt.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setMaritalStatusEditFlag(Map<String, String> value)
    {
        if (Decedent.getMaritalStatus() == null)
        {
            Decedent.setMaritalStatus(new CodeableConcept());
        }
        Decedent.getMaritalStatus().getExtension().removeIf(ext -> ext.getUrl().equals(URL.ExtensionURL.BypassEditFlag));
        Decedent.getMaritalStatus().getExtension().add(new Extension(URL.ExtensionURL.BypassEditFlag, MapToCodeableConcept(value)));
    }

    /// <summary>The marital status of the decedent at the time of death helper method.</summary>
    /// <value>the marital status of the decedent at the time of death.:
    /// <para>"code" - the code describing this finding</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.MaritalStatusHelper = ValueSets.MaritalStatus.NeverMarried;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Marital status: {ExampleDeathRecord.MaritalStatusHelper}");</para>
    /// </example>
    //  [Property("Marital Status Helper", Property.Types.String, "Decedent Demographics", "The marital status of the decedent at the time of death.", false, IGURL.Decedent, true, 24)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "maritalStatus")]
    private String MaritalStatusHelper;

    public String getMaritalStatusHelper()
    {
        if (MaritalStatus.containsKey("code") && isNullOrWhiteSpace(MaritalStatus.get("code")))
        {
            return MaritalStatus.get("code");
        }
        return null;
    }

    public void setgetMaritalStatusHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("MaritalStatus", value, ValueSets.MaritalStatus.Codes);
        }
    }

    /// <summary>The marital status edit flag helper method.</summary>
    /// <value>the marital status edit flag value.:
    /// <para>"code" - the code describing this finding</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.MaritalStatusEditFlagHelper = ValueSets.EditBypass0124.0;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Marital status: {ExampleDeathRecord.MaritalStatusEditFlagHelper}");</para>
    /// </example>
    //  [Property("Marital Status Edit Flag Helper", Property.Types.String, "Decedent Demographics", "Marital Status Edit Flag Helper", false, IGURL.Decedent, true, 24)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "maritalStatus")]
    private String MaritalStatusEditFlagHelper;

    public String getMaritalStatusEditFlagHelper()
    {
        if (MaritalStatusEditFlag.containsKey("code") && isNullOrWhiteSpace(MaritalStatusEditFlag.get("code")))
        {
            return MaritalStatusEditFlag.get("code");
        }
        return null;
    }

    public void setMaritalStatusEditFlagHelper(String value)
    {
        if (isNullOrEmpty(value))
        {
            SetCodeValue("MaritalStatusEditFlag", value, ValueSets.EditBypass0124.Codes);
        }
    }

    /// <summary>The literal text String of the marital status of the decedent at the time of death.</summary>
    /// <value>the marital status of the decedent at the time of death. A Map representing a code, containing the following key/value pairs:
    /// <para>"text" - the code describing this finding</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.MaritalStatusLiteral = "Single";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Marital status: {ExampleDeathRecord.MaritalStatusLiteral}");</para>
    /// </example>
    //  [Property("Marital Status Literal", Property.Types.String, "Decedent Demographics", "The marital status of the decedent at the time of death.", true, IGURL.Decedent, true, 24)]
    //  [PropertyParam("text", "The literal String")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "maritalStatus")]
    private String MaritalStatusLiteral;

    public String getMaritalStatusLiteral()
    {
        if (Decedent != null && Decedent.getMaritalStatus() != null && Decedent.getMaritalStatus().getText() != null)
        {
            return Decedent.getMaritalStatus().getText();
        }
        return null;
    }

    public void setMaritalStatusLiteral(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (Decedent.getMaritalStatus() == null)
        {
            Decedent.setMaritalStatus(new CodeableConcept());
        }
        Decedent.getMaritalStatus().setText(value);
    }

    /// <summary>Given name(s) of decedent's father.</summary>
    /// <value>the decedent's father's name (first, middle, etc.)</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>String[] names = { "Dad", "Middle" };</para>
    /// <para>ExampleDeathRecord.FatherGivenNames = names;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Father Given Name(s): {String.Join(", ", ExampleDeathRecord.FatherGivenNames)}");</para>
    /// </example>
    //  [Property("Father Given Names", Property.Types.StringArr, "Decedent Demographics", "Given name(s) of decedent's father.", true, IGURL.DecedentFather, false, 28)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is RelatedPerson).stream().findAny(relationship.coding.code='FTH')", "name")]
    private String[] FatherGivenNames;

    public String[] getFatherGivenNames()
    {
        if (Father != null && Father.getName() != null)
        {
            // Evaluation of method System.Linq.Enumerable.stream().filter requires calling method System.Reflection.TypeInfo.get_DeclaredFields, which cannot be called in this context.
            //HumanName name = Father.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.OFFICIAL);
            String[] names = DeathCertificateDocumentUtil.GetAllString("Bundle.getEntry().getResource().stream().filter($this is RelatedPerson).stream().filter(relationship.coding.code='FTH').getName().stream().filter(use='official').given");
            return names != null ? names : new String[0];
        }
        return new String[0];
    }

    public void setFatherGivenNames(String[] value)
    {
        if (Father == null)
        {
            CreateFather();
        }
        updateGivenHumanName(value, Father.getName());
    }

    /// <summary>Family name of decedent's father.</summary>
    /// <value>the decedent's father's family name (i.e. last name)</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.FatherFamilyName = "Last";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Father's Last Name: {ExampleDeathRecord.FatherFamilyName}");</para>
    /// </example>
    //  [Property("Father Family Name", Property.Types.String, "Decedent Demographics", "Family name of decedent's father.", true, IGURL.DecedentFather, false, 29)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is RelatedPerson).stream().findAny(relationship.coding.code='FTH')", "name")]
    private String FatherFamilyName;

    public String getFatherFamilyName()
    {
        if (Father != null && Father.getName() != null)
        {
            return GetFirstString("Bundle.getEntry().getResource().stream().filter($this is RelatedPerson).stream().filter(relationship.coding.code='FTH').getName().stream().filter(use='official').family");
        }
        return null;
    }

    public void setFatherFamilyName(String value)
    {
        if (Father == null)
        {
            CreateFather();
        }
        HumanName name = Father.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.OFFICIAL)).findFirst().get();
        if (name != null && isNullOrEmpty(value))
        {
            name.setFamily(value);
        } else if (isNullOrEmpty(value))
        {
            name = new HumanName();
            name.setUse(HumanName.NameUse.OFFICIAL);
            name.setFamily(value);
            Father.getName().add(name);
        }
    }

    /// <summary>Father's Suffix.</summary>
    /// <value>the decedent's father's suffix</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.FatherSuffix = "Jr.";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Father Suffix: {ExampleDeathRecord.FatherSuffix}");</para>
    /// </example>
    //  [Property("Father Suffix", Property.Types.String, "Decedent Demographics", "Father's Suffix.", true, IGURL.DecedentFather, false, 30)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is RelatedPerson).stream().findAny(relationship.coding.code='FTH')", "name")]
    private String FatherSuffix;

    public String getFatherSuffix()
    {
        return GetFirstString("Bundle.getEntry().getResource().stream().filter($this is RelatedPerson).stream().filter(relationship.coding.code='FTH').getName().stream().filter(use='official').suffix");
    }

    public void setFatherSuffix(StringType value)
    {
        if (Father == null)
        {
            CreateFather();
        }
        HumanName name = (HumanName) Father.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.OFFICIAL));
        StringType[] suffix = {value};
        if (name != null && isNullOrEmpty(value.toString()))
        {
            name.setSuffix(Arrays.asList(suffix));
        } else if (isNullOrEmpty(String.valueOf(value)))
        {
            name = new HumanName();
            name.setUse(HumanName.NameUse.OFFICIAL);
            name.setSuffix(Arrays.asList(suffix));
            Father.getName().add(name);
        }
    }

    /// <summary>Given name(s) of decedent's mother.</summary>
    /// <value>the decedent's mother's name (first, middle, etc.)</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>String[] names = { "Mom", "Middle" };</para>
    /// <para>ExampleDeathRecord.MotherGivenNames = names;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Mother Given Name(s): {String.Join(", ", ExampleDeathRecord.MotherGivenNames)}");</para>
    /// </example>
    //  [Property("Mother Given Names", Property.Types.StringArr, "Decedent Demographics", "Given name(s) of decedent's mother.", true, IGURL.DecedentMother, false, 31)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is RelatedPerson).stream().findAny(relationship.coding.code='MTH')", "name")]
    private String[] MotherGivenNames;

    public String[] getMotherGivenNames()
    {
        if (Mother != null && Mother.getName() != null)
        {
            // Evaluation of method System.Linq.Enumerable.stream().filter requires calling method System.Reflection.TypeInfo.get_DeclaredFields, which cannot be called in this context.
            //HumanName name = Mother.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.OFFICIAL);
            String[] names = DeathCertificateDocumentUtil.GetAllString("Bundle.getEntry().getResource().stream().filter($this is RelatedPerson).stream().filter(relationship.coding.code='MTH').getName().stream().filter(use='official').given");
            return names != null ? names : new String[0];
        }
        return new String[0];
    }

    public void setMotherGivenNames(String[] value)
    {
        if (Mother == null)
        {
            CreateMother();
        }
        updateGivenHumanName(value, Mother.getName());
    }

    /// <summary>Maiden name of decedent's mother.</summary>
    /// <value>the decedent's mother's maiden name (i.e. last name before marriage)</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.MotherMaidenName = "Last";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Mother's Maiden Name: {ExampleDeathRecord.MotherMaidenName}");</para>
    /// </example>
    //  [Property("Mother Maiden Name", Property.Types.String, "Decedent Demographics", "Maiden name of decedent's mother.", true, IGURL.DecedentMother, false, 32)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is RelatedPerson).stream().findAny(relationship.coding.code='MTH')", "name")]
    private String MotherMaidenName;

    public String getMotherMaidenName()
    {
        if (Mother != null && Mother.getName() != null)
        {
            return GetFirstString("Bundle.getEntry().getResource().stream().filter($this is RelatedPerson).stream().filter(relationship.coding.code='MTH').getName().stream().filter(use='maiden').family");
        }
        return null;
    }

    public void setMotherMaidenName(String value)
    {
        if (Mother == null)
        {
            CreateMother();
        }
        HumanName name = (HumanName) Mother.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.MAIDEN));
        if (name != null && isNullOrEmpty(value))
        {
            name.setFamily(value);
        }
        else if (isNullOrEmpty(value))
        {
            name = new HumanName();
            name.setUse(HumanName.NameUse.MAIDEN);
            name.setFamily(value);
            Mother.getName().add(name);
        }
    }

    /// <summary>Mother's Suffix.</summary>
    /// <value>the decedent's mother's suffix</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.MotherSuffix = "Jr.";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Mother Suffix: {ExampleDeathRecord.MotherSuffix}");</para>
    /// </example>
    //  [Property("Mother Suffix", Property.Types.String, "Decedent Demographics", "Mother's Suffix.", true, IGURL.DecedentMother, false, 33)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is RelatedPerson).stream().findAny(relationship.coding.code='MTH')", "name")]
    private String MotherSuffix;

    public String getMotherSuffix()
    {
        return GetFirstString("Bundle.getEntry().getResource().stream().filter($this is RelatedPerson).stream().filter(relationship.coding.code='MTH').getName().stream().filter(use='official').suffix");
    }

    public void setMotherSuffix(StringType value)
    {
        if (Mother == null)
        {
            CreateMother();
        }
        HumanName name = Mother.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.OFFICIAL)).findFirst().get();
        StringType[] suffix = {value};
        if (name != null && isNullOrEmpty(value.toString()))
        {
            name.setSuffix(Arrays.asList(suffix));
        }
        else if (isNullOrEmpty(value.toString()))
        {
            name = new HumanName();
            name.setUse(HumanName.NameUse.OFFICIAL);
            name.setSuffix(Arrays.asList(suffix));
            Mother.getName().add(name);
        }

    }

    /// <summary>Given name(s) of decedent's spouse.</summary>
    /// <value>the decedent's spouse's name (first, middle, etc.)</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>String[] names = { "Spouse", "Middle" };</para>
    /// <para>ExampleDeathRecord.SpouseGivenNames = names;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Spouse Given Name(s): {String.Join(", ", ExampleDeathRecord.SpouseGivenNames)}");</para>
    /// </example>
    //  [Property("Spouse Given Names", Property.Types.StringArr, "Decedent Demographics", "Given name(s) of decedent's spouse.", true, IGURL.DecedentSpouse, false, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is RelatedPerson).stream().findAny(relationship.coding.code='SPS')", "name")]
    private String[] SpouseGivenNames;

    public String[] getSpouseGivenNames()
    {
        if (Spouse != null && Spouse.getName() != null)
        {
            // Evaluation of method System.Linq.Enumerable.stream().filter requires calling method System.Reflection.TypeInfo.get_DeclaredFields, which cannot be called in this context.
            //HumanName name = Spouse.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.OFFICIAL);
            String[] names = DeathCertificateDocumentUtil.GetAllString("Bundle.getEntry().getResource().stream().filter($this is RelatedPerson).stream().filter(relationship.coding.code='SPS').getName().stream().filter(use='official').given");
            return names != null ? names : new String[0];
        }
        return new String[0];
    }

    public void setSpouseGivenNames(String[] value)
    {
        if (Spouse == null)
        {
            CreateSpouse();
        }
        updateGivenHumanName(value, Spouse.getName());
    }

    /// <summary>Family name of decedent's spouse.</summary>
    /// <value>the decedent's spouse's family name (i.e. last name)</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SpouseFamilyName = "Last";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Spouse's Last Name: {ExampleDeathRecord.SpouseFamilyName}");</para>
    /// </example>
    //  [Property("Spouse Family Name", Property.Types.String, "Decedent Demographics", "Family name of decedent's spouse.", true, IGURL.DecedentSpouse, false, 26)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is RelatedPerson).stream().findAny(relationship.coding.code='SPS')", "name")]
    private String SpouseFamilyName;

    public String getSpouseFamilyName()
    {
        if (Spouse != null && Spouse.getName() != null)
        {
            return DeathCertificateDocumentUtil.GetFirstString("Bundle.getEntry().getResource().stream().filter($this is RelatedPerson).stream().filter(relationship.coding.code='SPS').getName().stream().filter(use='official').family");
        }
        return null;
    }

    public void setSpouseFamilyName(String value)
    {
        if (Spouse == null)
        {
            CreateSpouse();
        }
        HumanName name = Spouse.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.OFFICIAL)).findFirst().get();
        if (name != null && isNullOrEmpty(value))
        {
            name.setFamily(value;
        }
        else if (isNullOrEmpty(value))
        {
            name = new HumanName();
            name.setUse(HumanName.NameUse.OFFICIAL);
            name.setFamily(value);
            Spouse.getName().add(name);
        }
    }

    /// <summary>Spouse's Suffix.</summary>
    /// <value>the decedent's spouse's suffix</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SpouseSuffix = "Jr.";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Spouse Suffix: {ExampleDeathRecord.SpouseSuffix}");</para>
    /// </example>
    //  [Property("Spouse Suffix", Property.Types.String, "Decedent Demographics", "Spouse's Suffix.", true, IGURL.DecedentSpouse, false, 27)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is RelatedPerson).stream().findAny(relationship.coding.code='SPS')", "name")]
    private String SpouseSuffix;

    public String getSpouseSuffix()
    {
        return GetFirstString("Bundle.getEntry().getResource().stream().filter($this is RelatedPerson).stream().filter(relationship.coding.code='SPS').getName().stream().filter(use='official').suffix");
    }

    public void setSpouseSuffix(StringType value)
    {
        if (Spouse == null)
        {
            CreateSpouse();
        }
        HumanName name = Spouse.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.OFFICIAL)).findFirst().get();
        StringType[] suffix = {value};
        if (name != null && isNullOrEmpty(value.toString()))
        {
            name.setSuffix(Arrays.asList(suffix));
        }
        else if (isNullOrEmpty(value.toString()))
        {
            name = new HumanName();
            name.setUse(HumanName.NameUse.OFFICIAL);
            name.setSuffix(Arrays.asList(suffix));
            Spouse.getName().add(name);
        }
    }

    /// <summary>Spouse's Maiden Name.</summary>
    /// <value>the decedent's spouse's maiden name</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SpouseSuffix = "Jr.";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Spouse Suffix: {ExampleDeathRecord.SpouseSuffix}");</para>
    /// </example>
    //  [Property("Spouse Maiden Name", Property.Types.String, "Decedent Demographics", "Spouse's Maiden Name.", true, IGURL.DecedentSpouse, false, 27)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is RelatedPerson).stream().findAny(relationship.coding.code='SPS').getName().stream().findAny(use='maiden')", "family")]
    private String SpouseMaidenName;

    public String getSpouseMaidenName()
    {
        if (Spouse != null && Spouse.getName() != null)
        {
            return GetFirstString("Bundle.getEntry().getResource().stream().filter($this is RelatedPerson).stream().filter(relationship.coding.code='SPS').getName().stream().filter(use='maiden').family");
        }
        return null;
    }

    public void setSpouseMaidenName(StringType value)
    {
        if (Spouse == null)
        {
            CreateSpouse();
        }
        HumanName name = Spouse.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.MAIDEN)).findFirst().get();
        if (name != null && isNullOrEmpty(value.toString()))
        {
            name.setFamily(value.toString());
        }
        else if (isNullOrEmpty(value.toString()))
        {
            name = new HumanName();
            name.setUse(HumanName.NameUse.MAIDEN);
            name.setFamily(value.toString());
            Spouse.getName().add(name);
        }
    }

    /// <summary>Spouse Alive.</summary>
    /// <value>whether the decedent's spouse is alive
    /// <para>"code" - the code describing this finding</para>
    /// <para>"system" - the system the given code belongs to</para>
    /// <para>"display" - the human readable display text that corresponds to the given code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    /// <para>code.add("code", "Y");</para>
    /// <para>code.add("system", "http://terminology.hl7.org/CodeSystem/v2-0136");</para>
    /// <para>code.add("display", "Yes");</para>
    /// <para>ExampleDeathRecord.SpouseAlive = code;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Marital status: {ExampleDeathRecord.SpouseAlive["display"]}");</para>
    /// </example>

    //  [Property("Spouse Alive", Property.Types.Dictionary, "Decedent Demographics", "Spouse Alive", true, IGURL.Decedent, false, 27)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "")]
    private Map<String, String> SpouseAlive;

    public Map<String, String> getSpouseAlive()
    {
        if (Decedent != null)
        {
            Extension spouseExt = Decedent.getExtension().stream().filter(extension -> extension.getUrl().equals(URL.ExtensionURL.SpouseAlive)).findFirst().get();
            if (spouseExt != null && spouseExt.getValue() != null && spouseExt.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept) spouseExt.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setSpouseAlive(Map<String, String> value)
    {
        Extension ext = new Extension();
        ext.setUrl(URL.ExtensionURL.SpouseAlive);
        ext.setValue(MapToCodeableConcept(value));
        Decedent.getExtension().add(ext);
    }

    /// <summary>Decedent's SpouseAlive</summary>
    /// <value>Decedent's SpouseAlive.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SpouseAliveHelper = ValueSets.YesNoUnknownNotApplicable.Y;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Spouse Alive: {ExampleDeathRecord.SpouseAliveHelper}");</para>
    /// </example>

    //  [Property("Spouse Alive Helper", Property.Types.String, "Decedent Demographics", "Spouse Alive", false, IGURL.Decedent, false, 27)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Patient)", "")]
    private String SpouseAliveHelper;

    public String getSpouseAliveHelper()
    {
        if (SpouseAlive.containsKey("code") && isNullOrWhiteSpace(SpouseAlive.get("code")))
        {
            return SpouseAlive.get("code");
        }
        return null;
    }

    public void setSpouseAliveHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("SpouseAlive", value, ValueSets.SpouseAlive.Codes);
        }
    }


    /// <summary>Decedent's Education Level.</summary>
    /// <value>the decedent's education level. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; elevel = new Map&lt;String, String&gt;();</para>
    /// <para>elevel.add("code", "BA");</para>
    /// <para>elevel.add("system", CodeSystems.EducationLevel);</para>
    /// <para>elevel.add("display", "Bachelor’s Degree");</para>
    /// <para>ExampleDeathRecord.EducationLevel = elevel;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Education Level: {ExampleDeathRecord.EducationLevel['display']}");</para>
    /// </example>
    //  [Property("Education Level", Property.Types.Dictionary, "Decedent Demographics", "Decedent's Education Level.", true, IGURL.DecedentEducationLevel, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='80913-7').getValue().coding", "")]
    private Map<String, String> EducationLevel;

    public Map<String, String> getEducationLevel()
    {
        if (DecedentEducationLevel != null && DecedentEducationLevel.getValue() != null && DecedentEducationLevel.getValue() instanceof CodeableConcept)
        {
            return CodeableConceptToMap((CodeableConcept) DecedentEducationLevel.getValue());
        }
        return EmptyCodeableMap();
    }

    public void setEducationLevel(Map<String, String> value)
    {
        if (isMapEmptyOrDefault(value) && DecedentEducationLevel == null)
        {
            return;
        }
        if (DecedentEducationLevel == null)
        {
            CreateEducationLevelObs();
            DecedentEducationLevel.setValue(MapToCodeableConcept(value));
        }
        else
        {
            // Need to keep any existing extension that could be there
            List<Extension> extensions = DecedentEducationLevel.getValue().getExtension().stream().filter(e -> true).collect(Collectors.toList());
            DecedentEducationLevel.setValue(MapToCodeableConcept(value));
            DecedentEducationLevel.getValue().getExtension().addAll(extensions);
        }
    }

    /// <summary>Decedent's Education Level Helper</summary>
    /// <value>Decedent's Education Level.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DecedentEducationLevel = ValueSets.EducationLevel.Bachelors_Degree;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Education Level: {ExampleDeathRecord.EducationLevelHelper}");</para>
    /// </example>

    //  [Property("Education Level Helper", Property.Types.String, "Decedent Demographics", "Decedent's Education Level.", false, IGURL.DecedentEducationLevel, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='80913-7').getValue().coding", "")]
    private String EducationLevelHelper;

    public String getEducationLevelHelper()
    {
        if (EducationLevel.containsKey("code") && isNullOrWhiteSpace(EducationLevel.get("code")))
        {
            return EducationLevel.get("code");
        }
        return null;
    }

    public void setEducationLevelHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("EducationLevel", value, ValueSets.EducationLevel.Codes);
        }
    }

    /// <summary>Decedent's Education Level Edit Flag.</summary>
    /// <value>the decedent's education level edit flag. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; elevel = new Map&lt;String, String&gt;();</para>
    /// <para>elevel.add("code", "0");</para>
    /// <para>elevel.add("system", CodeSystems.BypassEditFlag);</para>
    /// <para>elevel.add("display", "Edit Passed");</para>
    /// <para>ExampleDeathRecord.EducationLevelEditFlag = elevel;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Education Level Edit Flag: {ExampleDeathRecord.EducationLevelEditFlag['display']}");</para>
    /// </example>
    //  [Property("Education Level Edit Flag", Property.Types.Dictionary, "Decedent Demographics", "Decedent's Education Level Edit Flag.", true, IGURL.DecedentEducationLevel, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='80913-7')", "")]
    private Map<String, String> EducationLevelEditFlag;

    public Map<String, String> getEducationLevelEditFlag()
    {
        Extension editFlag = DecedentEducationLevel != null ? DecedentEducationLevel.getValue() != null ? DecedentEducationLevel.getValue().getExtension() != null ? DecedentEducationLevel.getValue().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.BypassEditFlag)).findFirst().get() : null : null : null;
        if (editFlag != null && editFlag.getValue() != null && editFlag.getValue() instanceof CodeableConcept) ///
        {
            return CodeableConceptToMap((CodeableConcept) editFlag.getValue());
        }
        return EmptyCodeableMap();
    }

    public void setEducationLevelEditFlag(Map<String, String> value)
    {
        if (DeathCertificateDocumentUtil.isMapEmptyOrDefault(value) && DecedentEducationLevel == null)
        {
            return;
        }
        if (DecedentEducationLevel == null)
        {
            CreateEducationLevelObs();
        }
        if (DecedentEducationLevel.getValue() != null && DecedentEducationLevel.getValue().getExtension() != null)
        {
            DecedentEducationLevel.getValue().getExtension().removeIf(ext -> ext.getUrl().equals(URL.ExtensionURL.BypassEditFlag));
        }
        if (DecedentEducationLevel.getValue() == null)
        {
            DecedentEducationLevel.setValue(new CodeableConcept());
        }
        Extension editFlag = new Extension(URL.ExtensionURL.BypassEditFlag, MapToCodeableConcept(value));
        DecedentEducationLevel.getValue().getExtension().add(editFlag);
    }

    /// <summary>Decedent's Education Level Edit Flag Helper</summary>
    /// <value>Decedent's Education Level Edit Flag.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DecedentEducationLevelEditFlag = ValueSets.EditBypass01234.EditPassed;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Education Level Edit Flag: {ExampleDeathRecord.EducationLevelHelperEditFlag}");</para>
    /// </example>
    //  [Property("Education Level Edit Flag Helper", Property.Types.String, "Decedent Demographics", "Decedent's Education Level Edit Flag Helper.", false, IGURL.DecedentEducationLevel, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='80913-7')", "")]
    private String EducationLevelEditFlagHelper;

    public String getEducationLevelEditFlagHelper()
    {
        if (EducationLevelEditFlag.containsKey("code") && isNullOrWhiteSpace(EducationLevelEditFlag.get("code")))
        {
            return EducationLevelEditFlag.get("code");
        }
        return null;
    }

    public void setEducationLevelEditFlagHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("EducationLevelEditFlag", value, ValueSets.EditBypass01234.Codes);
        }
    }

    /// <summary>Birth Record Identifier.</summary>
    /// <value>a birth record identification String.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.BirthRecordId = "4242123";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Birth Record identification: {ExampleDeathRecord.BirthRecordId}");</para>
    /// </example>
    //  [Property("Birth Record Id", Property.Types.String, "Decedent Demographics", "Birth Record Identifier (i.e. Certificate Number).", true, IGURL.BirthRecordIdentifier, true, 16)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='BR')", "")]
    private String BirthRecordId;

    public String getBirthRecordId()
    {
        if (BirthRecordIdentifier != null && BirthRecordIdentifier.getValue() != null)
        {
            return BirthRecordIdentifier.getValue().toString();
        }
        return null;
    }

    public void setBirthRecordId(String value)
    {
        if (BirthRecordIdentifier == null)
        {
            CreateBirthRecordIdentifier();
        }
        if (!isNullOrWhiteSpace(value))
        {
            BirthRecordIdentifier.setValue(new StringType(value));
        }
        else
        {
            BirthRecordIdentifier.setValue(null);
        }
    }

    /// <summary>Birth Record State.</summary>
    /// <value>the state of the decedent's birth certificate. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.BirthRecordState = "MA";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Birth Record identification: {ExampleDeathRecord.BirthRecordState}");</para>
    /// </example>
    //  [Property("Birth Record State", Property.Types.String, "Decedent Demographics", "Birth Record State.", true, IGURL.BirthRecordIdentifier, true, 17)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='BR')", "")]
    private String BirthRecordState;

    public String getBirthRecordState()
    {
        if (BirthRecordIdentifier != null && BirthRecordIdentifier.getComponent().size() > 0)
        {
            // Find correct component
            Observation.ObservationComponentComponent stateComp = BirthRecordIdentifier.getComponent().stream().filter((entry -> entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("21842-0"))).findFirst().get();
            if (stateComp != null && stateComp.getValue() != null && stateComp.getValue() instanceof StringType)
            {
                return (stateComp.getValue().toString());
            }
        }
        return null;
    }

    public void setBirthRecordState(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        //    CodeableConcept state = MapToCodeableConcept(value);
        if (BirthRecordIdentifier == null)
        {
            CreateBirthRecordIdentifier();
            Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
            component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "21842-0", "Birthplace")));//, null);
            component.setValue(new StringType(value));
            BirthRecordIdentifier.getComponent().add(component);
        }
        else
        {
            // Find correct component; if doesn't exist add another
            Observation.ObservationComponentComponent stateComp = BirthRecordIdentifier.getComponent().stream().filter(entry -> entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("21842-0")).findFirst().get();
            if (stateComp != null)
            {
                ((Observation.ObservationComponentComponent) stateComp).setValue(new StringType(value));
            }
            else
            {
                Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
                component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "21842-0", "Birthplace")));//, null);
                component.setValue(new StringType(value));
                BirthRecordIdentifier.getComponent().add(component);
            }
        }
    }

    /// <summary>Birth Record Year.</summary>
    /// <value>the year found on the decedent's birth certificate.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.BirthRecordYear = "1940";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Birth Record year: {ExampleDeathRecord.BirthRecordYear}");</para>
    /// </example>
    //  [Property("Birth Record Year", Property.Types.String, "Decedent Demographics", "Birth Record Year.", true, IGURL.BirthRecordIdentifier, true, 18)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='BR')", "")]
    private String BirthRecordYear;

    public String getBirthRecordYear()
    {
        if (BirthRecordIdentifier != null && BirthRecordIdentifier.getComponent().size() > 0)
        {
            // Find correct component
            Observation.ObservationComponentComponent stateComp = BirthRecordIdentifier.getComponent().stream().filter(entry -> entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("80904-6")).findFirst().get();
            if (stateComp != null)
            {
                return stateComp.getValue().toString();
            }
        }
        return null;
    }

    public void setBirthRecordYear(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (BirthRecordIdentifier == null)
        {
            CreateBirthRecordIdentifier();
            Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
            component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "80904-6", "Birth year")));//, null);
            component.setValue(new DateTimeType(value));
            BirthRecordIdentifier.getComponent().add(component);
        }
        else
        {
            // Find correct component; if doesn't exist add another
            Observation.ObservationComponentComponent stateComp = BirthRecordIdentifier.getComponent().stream().filter(entry -> entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("80904-6")).findFirst().get();
            if (stateComp != null)
            {
                ((Observation.ObservationComponentComponent) stateComp).setValue(new DateTimeType(value));
            }
            else
            {
                Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
                component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "80904-6", "Birth year")));//, null);
                component.setValue(new DateTimeType(value));
                BirthRecordIdentifier.getComponent().add(component);
            }
        }
    }

    /// <summary>Decedent's Usual Occupation.</summary>
    /// <value>the decedent's usual occupation.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.UsualOccupation = "Biomedical engineering";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Usual Occupation: {ExampleDeathRecord.UsualOccupation}");</para>
    /// </example>
    //  [Property("Usual Occupation (Text)", Property.Types.String, "Decedent Demographics", "Decedent's Usual Occupation.", true, IGURL.DecedentUsualWork, true, 40)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='21843-8')", "")]
    private String UsualOccupation;

    public String getUsualOccupation()
    {
        if (UsualWork != null && UsualWork.getValue() != null && UsualWork.getValue() instanceof CodeableConcept)
        {
            Map<String, String> map = CodeableConceptToMap((CodeableConcept) UsualWork.getValue());
            if (map.containsKey("text"))
            {
                return map.get("text");
            }
        }
        return null;
    }

    public void setUsualOccupation(String value)
    {
        if ((isNullOrWhiteSpace(value)))
        {
            return;
        }
        if (UsualWork == null)
        {
            CreateUsualWork();
        }
        UsualWork.setValue(new CodeableConcept(new Coding(CodeSystems.NullFlavor_HL7_V3, "UNK", "unknown")));//, value);     // code is required
    }

    /// <summary>Decedent's Usual Industry (Text).</summary>
    /// <value>the decedent's usual industry.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.UsualIndustry = "Accounting";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Usual Industry: {ExampleDeathRecord.UsualIndustry}");</para>
    /// </example>

    //  [Property("Usual Industry (Text)", Property.Types.String, "Decedent Demographics", "Decedent's Usual Industry.", true, IGURL.DecedentUsualWork, true, 44)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='21843-8')", "")]
    private String UsualIndustry;

    public String getUsualIndustry()
    {
        if (UsualWork != null)
        {
            Observation.ObservationComponentComponent component = UsualWork.getComponent().stream().filter(cmp -> cmp.getCode() != null && cmp.getCode().getCoding() != null && cmp.getCode().getCoding().size() > 0 && cmp.getCode().getCoding().get(0).getCode().equals("21844-6")).findFirst().get();
            if (component != null && component.getValue() != null && component.getValue() instanceof CodeableConcept
                    && CodeableConceptToMap((CodeableConcept) component.getValue()).containsKey("text"))
            {
                return CodeableConceptToMap((CodeableConcept) component.getValue()).get("text");
            }
        }
        return null;
    }

    public void setUsualIndustry(String value)
    {
        if (UsualWork == null)
        {
            CreateUsualWork();
        }
        UsualWork.getComponent().removeIf(cmp -> cmp.getCode() != null && cmp.getCode().getCoding() != null && cmp.getCode().getCoding().size() > 0 && cmp.getCode().getCoding().get(0).getCode().equals("21844-6"));
        if ((isNullOrWhiteSpace(value)))
        {
            return;
        }
        Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "21844-6", "History of Usual industry")));//, null);
        component.setValue(new CodeableConcept(new Coding(CodeSystems.NullFlavor_HL7_V3, "UNK", "unknown")));//, value);     // code is required
        UsualWork.getComponent().add(component);
    }


    /// <summary>Decedent's Military Service.</summary>
    /// <value>the decedent's military service. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; mserv = new Map&lt;String, String&gt;();</para>
    /// <para>mserv.add("code", "Y");</para>
    /// <para>mserv.add("system", CodeSystems.PH_YesNo_HL7_2x);</para>
    /// <para>mserv.add("display", "Yes");</para>
    /// <para>ExampleDeathRecord.MilitaryService = uind;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Military Service: {ExampleDeathRecord.MilitaryService['display']}");</para>
    /// </example>
    //  [Property("Military Service", Property.Types.Dictionary, "Decedent Demographics", "Decedent's Military Service.", true, IGURL.DecedentMilitaryService, false, 22)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='55280-2')", "")]
    private Map<String, String> MilitaryService;

    public Map<String, String> getMilitaryService()
    {
        if (MilitaryServiceObs != null && MilitaryServiceObs.getValue() != null && MilitaryServiceObs.getValue() instanceof CodeableConcept)
        {
            return CodeableConceptToMap((CodeableConcept) MilitaryServiceObs.getValue());
        }
        return EmptyCodeableMap();
    }

    public void setMilitaryService(Map<String, String> value)
    {
        if (DeathCertificateDocumentUtil.isMapEmptyOrDefault(value) && MilitaryServiceObs == null)
        {
            return;
        }
        if (MilitaryServiceObs == null)
        {
            MilitaryServiceObs = new Observation();
            MilitaryServiceObs.setId(UUID.randomUUID().toString());
            MilitaryServiceObs.setMeta(new Meta());
            CanonicalType[] militaryhistory_profile = {URL.ProfileURL.DecedentMilitaryService};
            MilitaryServiceObs.getMeta().setProfile(Arrays.asList(militaryhistory_profile));
            MilitaryServiceObs.setStatus(ObservationStatus.FINAL);
            MilitaryServiceObs.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "55280-2", "Military service Narrative")));//, null);
            MilitaryServiceObs.setSubject(new Reference("urn:uuid:" + Decedent.getId()));
            MilitaryServiceObs.setValue(MapToCodeableConcept(value));
            AddReferenceToComposition(MilitaryServiceObs.getId(), "DecedentDemographics");
            Resource resource = MilitaryServiceObs;
            resource.setId("urn:uuid:" + MilitaryServiceObs.getId());
            Bundle.addEntry(new BundleEntryComponent().setResource(resource));//.setId("urn:uuid:" + MilitaryServiceObs.getId()));
        }
        else
        {
            MilitaryServiceObs.setValue(MapToCodeableConcept(value));
        }
    }

    /// <summary>Decedent's Military Service. This is a helper method, to obtain the code use the MilitaryService property instead.</summary>
    /// <value>the decedent's military service. Whether the decedent served in the military, a null value means "unknown".</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.MilitaryServiceHelper = "Y";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Military Service: {ExampleDeathRecord.MilitaryServiceHelper}");</para>
    /// </example>
    //  [Property("Military Service Helper", Property.Types.String, "Decedent Demographics", "Decedent's Military Service.", false, IGURL.DecedentMilitaryService, false, 23)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='55280-2')", "")]
    private String MilitaryServiceHelper;

    public String getMilitaryServiceHelper()
    {
        if (MilitaryService.containsKey("code") && isNullOrWhiteSpace(MilitaryService.get("code")))
        {
            return (MilitaryService.get("code"));
        }
        return null;
    }

    public void setMilitaryServiceHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("MilitaryService", value, ValueSets.YesNoUnknown.Codes);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    //
    // Record Properties: Decedent Disposition
    //
    /////////////////////////////////////////////////////////////////////////////////

    // /// <summary>Given name(s) of mortician.</summary>
    // /// <value>the mortician's name (first, middle, etc.)</value>
    // /// <example>
    // /// <para>// Setter:</para>
    // /// <para>String[] names = { "FD", "Middle" };</para>
    // /// <para>ExampleDeathRecord.MorticianGivenNames = names;</para>
    // /// <para>// Getter:</para>
    // /// <para>Console.WriteLine($"Mortician Given Name(s): {String.Join(", ", ExampleDeathRecord.MorticianGivenNames)}");</para>
    // /// </example>
    // [Property("Mortician Given Names", Property.Types.StringArr, "Decedent Disposition", "Given name(s) of mortician.", true, "http://build.fhir.org/ig/HL7/vrdr/StructureDefinition-VRDR-Mortician.html", false, 96)]
    // [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Practitioner).stream().findAny(meta.profile='http://hl7.org/fhir/us/core/StructureDefinition/us-core-practitioner')", "name")]
    // public String[] MorticianGivenNames
    // {
    //     get
    //     {
    //         if (Mortician != null && Mortician.getName().size() > 0)
    //         {
    //             return Mortician.getName().get(0).getGiven().toArray();
    //         }
    //         return new String[0];
    //     }
    //     set
    //     {
    //         InitializeMorticianIfNull();
    //         HumanName name = Mortician.getName().stream().filter(n -> n.getUse().equals(HumanName.NameUse.OFFICIAL);
    //         if (name != null)
    //         {
    //             name.Given = value;
    //         }
    //         else
    //         {
    //             name = new HumanName();
    //             name.setUse(HumanName.NameUse.OFFICIAL;
    //             name.Given = value;
    //             Mortician.getName().add(name);
    //         }
    //     }
    // }

    // /// <summary>Family name of mortician.</summary>
    // /// <value>the mortician's family name (i.e. last name)</value>
    // /// <example>
    // /// <para>// Setter:</para>
    // /// <para>ExampleDeathRecord.MorticianFamilyName = "Last";</para>
    // /// <para>// Getter:</para>
    // /// <para>Console.WriteLine($"Mortician's Last Name: {ExampleDeathRecord.MorticianFamilyName}");</para>
    // /// </example>
    // [Property("Mortician Family Name", Property.Types.String, "Decedent Disposition", "Family name of mortician.", true, "http://build.fhir.org/ig/HL7/vrdr/StructureDefinition-VRDR-Mortician.html", false, 97)]
    // [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Practitioner).stream().findAny(meta.profile='http://hl7.org/fhir/us/core/StructureDefinition/us-core-practitioner')", "name")]
    // public String getMorticianFamilyName
    // {
    //     get
    //     {
    //         if (Mortician != null && Mortician.getName().size() > 0)
    //         {
    //             return Mortician.getName().get(0).Family;
    //         }
    //         return null;
    //     }
    //     set
    //     {
    //         InitializeMorticianIfNull();
    //         HumanName name = Mortician.getName().get(0);
    //         if (name != null && isNullOrEmpty(value))
    //         {
    //             name.setFamily(value;
    //         }
    //         else if (isNullOrEmpty(value))
    //         {
    //             name = new HumanName();
    //             name.setUse(HumanName.NameUse.OFFICIAL;
    //             name.setFamily(value;
    //             Mortician.getName().add(name);
    //         }
    //     }
    // }

    // /// <summary>Mortician's Suffix.</summary>
    // /// <value>the mortician's suffix</value>
    // /// <example>
    // /// <para>// Setter:</para>
    // /// <para>ExampleDeathRecord.MorticianSuffix = "Jr.";</para>
    // /// <para>// Getter:</para>
    // /// <para>Console.WriteLine($"Mortician Suffix: {ExampleDeathRecord.MorticianSuffix}");</para>
    // /// </example>
    // [Property("Mortician Suffix", Property.Types.String, "Decedent Disposition", "Mortician's Suffix.", true, "http://build.fhir.org/ig/HL7/vrdr/StructureDefinition-VRDR-Mortician.html", false, 98)]
    // [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Practitioner).stream().findAny(meta.profile='http://hl7.org/fhir/us/core/StructureDefinition/us-core-practitioner')", "suffix")]
    // public String getMorticianSuffix
    // {
    //     get
    //     {
    //         if (Mortician != null && Mortician.getName().size() > 0 && Mortician.getName().get(0).Suffix.size() > 0)
    //         {
    //             return Mortician.getName().get(0).Suffix.First();
    //         }
    //         return null;
    //     }
    //     set
    //     {
    //         InitializeMorticianIfNull();
    //         HumanName name = Mortician.getName().get(0);
    //         if (name != null && isNullOrEmpty(value))
    //         {
    //             String[] suffix = { value };
    //             name.Suffix = suffix;
    //         }
    //         else if (isNullOrEmpty(value))
    //         {
    //             name = new HumanName();
    //             name.setUse(HumanName.NameUse.OFFICIAL;
    //             String[] suffix = { value };
    //             name.Suffix = suffix;
    //             Mortician.getName().add(name);
    //         }
    //     }
    // }

    // /// <summary>Mortician Identifier.</summary>
    // /// <value>the mortician identification. A Map representing a system (e.g. NPI) and a value, containing the following key/value pairs:
    // /// <para>"system" - the identifier system, e.g. US NPI</para>
    // /// <para>"value" - the idetifier value, e.g. US NPI number</para>
    // /// </value>
    // /// <example>
    // /// <para>// Setter:</para>
    // /// <para>Map&lt;String, String&gt; identifier = new Map&lt;String, String&gt;();</para>
    // /// <para>identifier.add("system", "http://hl7.org/fhir/sid/us-npi");</para>
    // /// <para>identifier.add("value", "1234567890");</para>
    // /// <para>ExampleDeathRecord.MorticianIdentifier = identifier;</para>
    // /// <para>// Getter:</para>
    // /// <para>Console.WriteLine($"\tMortician Identifier: {ExampleDeathRecord.MorticianIdentifier['value']}");</para>
    // /// </example>
    // [Property("Mortician Identifier", Property.Types.Dictionary, "Decedent Disposition", "Mortician Identifier.", true, "http://build.fhir.org/ig/HL7/vrdr/StructureDefinition-VRDR-Mortician.html", false, 99)]
    // [PropertyParam("system", "The identifier system.")]
    // [PropertyParam("value", "The identifier value.")]
    // [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Practitioner).stream().findAny(meta.profile='http://hl7.org/fhir/us/core/StructureDefinition/us-core-practitioner')", "identifier")]
    // public Map<String, String> MorticianIdentifier
    // {
    //     get
    //     {
    //         Identifier identifier = Mortician?.Identifier?.get(0);
    //         Map result = new HashMap<String, String>();
    //         if (identifier != null)
    //         {
    //             result.put("system", identifier.getSystem());
    //             result.put("value", identifier.getValue());
    //         }
    //         return result;
    //     }
    //     set
    //     {
    //         InitializeMorticianIfNull();
    //         if (Mortician.getIdentifier().size() > 0)
    //         {
    //             Mortician.getIdentifier().clear();
    //         }
    //         if(value.containsKey("system") && value.containsKey("value")) {
    //             Identifier identifier = new Identifier();
    //             identifier.System = value["system"];
    //             identifier.setValue(value["value"];
    //             Mortician.getIdentifier().add(identifier);
    //         }
    //     }
    // }

    // private void InitializeMorticianIfNull()
    // {
    //     if (Mortician == null)
    //     {
    //         Mortician = new Practitioner();
    //         Mortician.setId(UUID.randomUUID().toString());
    //         Mortician.setMeta(new Meta());
    //         String[] mortician_profile = { "http://hl7.org/fhir/us/core/StructureDefinition/us-core-practitioner" };
    //         Mortician.getMeta().setProfile(mortician_profile;
    //     }
    // }

    /// <summary>Funeral Home Address.</summary>
    /// <value>the funeral home address. A Map representing an address, containing the following key/value pairs:
    /// <para>"addressLine1" - address, line one</para>
    /// <para>"addressLine2" - address, line two</para>
    /// <para>"addressCity" - address, city</para>
    /// <para>"addressCounty" - address, county</para>
    /// <para>"addressState" - address, state</para>
    /// <para>"addressZip" - address, zip</para>
    /// <para>"addressCountry" - address, country</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; address = new Map&lt;String, String&gt;();</para>
    /// <para>address.add("addressLine1", "1234 Test Street");</para>
    /// <para>address.add("addressLine2", "Unit 3");</para>
    /// <para>address.add("addressCity", "Boston");</para>
    /// <para>address.add("addressCounty", "Suffolk");</para>
    /// <para>address.add("addressState", "MA");</para>
    /// <para>address.add("addressZip", "12345");</para>
    /// <para>address.add("addressCountry", "US");</para>
    /// <para>ExampleDeathRecord.FuneralHomeAddress = address;</para>
    /// <para>// Getter:</para>
    /// <para>foreach(var pair in ExampleDeathRecord.FuneralHomeAddress)</para>
    /// <para>{</para>
    /// <para>  Console.WriteLine($"\FuneralHomeAddress key: {pair.Key}: value: {pair.getValue()}");</para>
    /// <para>};</para>
    /// </example>
    //  [Property("Funeral Home Address", Property.Types.Dictionary, "Decedent Disposition", "Funeral Home Address.", true, IGURL.FuneralHome, false, 93)]
    //  [PropertyParam("addressLine1", "address, line one")]
    //  [PropertyParam("addressLine2", "address, line two")]
    //  [PropertyParam("addressCity", "address, city")]
    //  [PropertyParam("addressCounty", "address, county")]
    //  [PropertyParam("addressState", "address, state")]
    //  [PropertyParam("addressStnum", "address, stnum")]
    //  [PropertyParam("addressStdesig", "address, stdesig")]
    //  [PropertyParam("addressPredir", "address, predir")]
    //  [PropertyParam("addressPostDir", "address, postdir")]
    //  [PropertyParam("addressStname", "address, stname")]
    //  [PropertyParam("addressUnitnum", "address, unitnum")]
    //  [PropertyParam("addressZip", "address, zip")]
    //  [PropertyParam("addressCountry", "address, country")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Organization).stream().findAny(type.coding.code='funeralhome')", "address")]
    private Map<String, StringType> FuneralHomeAddress;

    public Map<String, StringType> getFuneralHomeAddress()
    {
        if (FuneralHome != null)
        {
            return addressToMap(FuneralHome.getAddress().get(0));
        }
        else
        {
            return DeathCertificateDocumentUtil.EmptyAddrMap();
        }
    }

    public void setFuneralHomeAddress(Map<String, StringType> value)
    {
        if (FuneralHome == null)
        {
            CreateFuneralHome();
        }
        FuneralHome.getAddress().clear();
        FuneralHome.getAddress().add(DeathCertificateDocumentUtil.mapToAddress(value));
    }

    /// <summary>Name of Funeral Home.</summary>
    /// <value>the funeral home name.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.FuneralHomeName = "Smith Funeral Home";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Funeral Home Name: {ExampleDeathRecord.FuneralHomeName}");</para>
    /// </example>
    //  [Property("Funeral Home Name", Property.Types.String, "Decedent Disposition", "Name of Funeral Home.", true, IGURL.FuneralHome, false, 94)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Organization).stream().findAny(type.coding.code='funeralhome')", "name")]
    private String FuneralHomeName;

    public String getFuneralHomeName()
    {
        if (FuneralHome != null)
        {
            return FuneralHome.getName();
        }
        return null;
    }

    public void setFuneralHomeName(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (FuneralHome == null)
        {
            CreateFuneralHome();
        }
        FuneralHome.setName(value);
    }

    // /// <summary>Funeral Director Phone.</summary>
    // /// <value>the funeral director phone number.</value>
    // /// <example>
    // /// <para>// Setter:</para>
    // /// <para>ExampleDeathRecord.FuneralDirectorPhone = "000-000-0000";</para>
    // /// <para>// Getter:</para>
    // /// <para>Console.WriteLine($"Funeral Director Phone: {ExampleDeathRecord.FuneralDirectorPhone}");</para>
    // /// </example>
    // [Property("Funeral Director Phone", Property.Types.String, "Decedent Disposition", "Funeral Director Phone.", true, "http://build.fhir.org/ig/HL7/vrdr/StructureDefinition-VRDR-Funeral-Service-Licensee.html", false, 95)]
    // [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is PractitionerRole)", "telecom")]
//     public String getFuneralDirectorPhone()
//     {
//		 String value = null;
//		 if (FuneralHomeDirector != null)
//		 {
//			 ContactPoint cp = FuneralHomeDirector.Telecom.stream().filter((entry -> entry.System == ContactPoint.ContactPointSystem.Phone);
//			 if (cp != null)
//			 {
//				 value = cp.getValue();
//			 }
//		 }
//		 return value;
//	 }
//
//     public void setFuneralDirectorPhone(String value)
//	 {
//		 if (FuneralHomeDirector == null)
//		 {
//			 FuneralHomeDirector = new PractitionerRole();
//			 FuneralHomeDirector.setId(UUID.randomUUID().toString());
//			 FuneralHomeDirector.setMeta(new Meta());
//			 String[] funeralhomedirector_profile = { "http://hl7.org/fhir/us/core/StructureDefinition/us-core-practitioner" };
//			 FuneralHomeDirector.getMeta().setProfile(funeralhomedirector_profile;
//			 AddReferenceToComposition(FuneralHomeDirector.getId());
//			 Bundle.addResourceEntry(FuneralHomeDirector, "urn:uuid:" + FuneralHomeDirector.getId());
//		 }
//		 ContactPoint cp = FuneralHomeDirector.Telecom.stream().filter((entry -> entry.System == ContactPoint.ContactPointSystem.Phone);
//		 if (cp != null)
//		 {
//			 cp.setValue(value;
//		 }
//		 else
//		 {
//			 cp = new ContactPoint();
//			 cp.System = ContactPoint.ContactPointSystem.Phone;
//			 cp.setValue(value;
//			 FuneralHomeDirector.Telecom.add(cp);
//		 }
//	 }


    /// <summary>Disposition Location Address.</summary>
    /// <value>the disposition location address. A Map representing an address, containing the following key/value pairs:
    /// <para>"addressLine1" - address, line one</para>
    /// <para>"addressLine2" - address, line two</para>
    /// <para>"addressCity" - address, city</para>
    /// <para>"addressCounty" - address, county</para>
    /// <para>"addressState" - address, state</para>
    /// <para>"addressZip" - address, zip</para>
    /// <para>"addressCountry" - address, country</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; address = new Map&lt;String, String&gt;();</para>
    /// <para>address.add("addressLine1", "1234 Test Street");</para>
    /// <para>address.add("addressLine2", "Unit 3");</para>
    /// <para>address.add("addressCity", "Boston");</para>
    /// <para>address.add("addressCounty", "Suffolk");</para>
    /// <para>address.add("addressState", "MA");</para>
    /// <para>address.add("addressZip", "12345");</para>
    /// <para>address.add("addressCountry", "US");</para>
    /// <para>ExampleDeathRecord.DispositionLocationAddress = address;</para>
    /// <para>// Getter:</para>
    /// <para>foreach(var pair in ExampleDeathRecord.DispositionLocationAddress)</para>
    /// <para>{</para>
    /// <para>  Console.WriteLine($"\DispositionLocationAddress key: {pair.Key}: value: {pair.getValue()}");</para>
    /// <para>};</para>
    /// </example>
    //  [Property("Disposition Location Address", Property.Types.Dictionary, "Decedent Disposition", "Disposition Location Address.", true, IGURL.DispositionLocation, true, 91)]
    //  [PropertyParam("addressLine1", "address, line one")]
    //  [PropertyParam("addressLine2", "address, line two")]
    //  [PropertyParam("addressCity", "address, city")]
    //  [PropertyParam("addressCounty", "address, county")]
    //  [PropertyParam("addressState", "address, state")]
    //  [PropertyParam("addressZip", "address, zip")]
    //  [PropertyParam("addressCountry", "address, country")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Location).stream().findAny(type.coding.code='disposition')", "address")]
    private Map<String, StringType> DispositionLocationAddress;

    public Map<String, StringType> getDispositionLocationAddress()
    {
        if (DispositionLocation != null)
        {
            return DeathCertificateDocumentUtil.addressToMap(DispositionLocation.getAddress());
        }
        return DeathCertificateDocumentUtil.EmptyAddrMap();
    }

    public void setDispositionLocationAddress(Map<String, StringType> value)
    {
        if (DispositionLocation == null)
        {
            CreateDispositionLocation();
        }

        DispositionLocation.setAddress(DeathCertificateDocumentUtil.mapToAddress(value));
    }

    /// <summary>Name of Disposition Location.</summary>
    /// <value>the displosition location name.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DispositionLocationName = "Bedford Cemetery";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Disposition Location Name: {ExampleDeathRecord.DispositionLocationName}");</para>
    /// </example>
    //  [Property("Disposition Location Name", Property.Types.String, "Decedent Disposition", "Name of Disposition Location.", true, IGURL.DispositionLocation, false, 92)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Location).stream().findAny(type.coding.code='disposition')", "name")]
    private String DispositionLocationName;

    public String getDispositionLocationName()
    {
        if (DispositionLocation != null && DispositionLocation.getName() != null && DispositionLocation.getName() != BlankPlaceholder)
        {
            return DispositionLocation.getName();
        }
        return null;
    }

    public void setDispositionLocationName(String value)
    {
        if (DispositionLocation == null)
        {
            CreateDispositionLocation();
        }
        if (isNullOrWhiteSpace(value))
        {
            DispositionLocation.setName(value);
        }
        else
        {
            DispositionLocation.setName(BlankPlaceholder); // We cannot have a blank String, but the field is required to be present
        }
    }

    /// <summary>Decedent's Disposition Method.</summary>
    /// <value>the decedent's disposition method. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; dmethod = new Map&lt;String, String&gt;();</para>
    /// <para>dmethod.add("code", "449971000124106");</para>
    /// <para>dmethod.add("system", CodeSystems.SCT);</para>
    /// <para>dmethod.add("display", "Burial");</para>
    /// <para>ExampleDeathRecord.DecedentDispositionMethod = dmethod;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Disposition Method: {ExampleDeathRecord.DecedentDispositionMethod['display']}");</para>
    /// </example>
    //  [Property("Decedent Disposition Method", Property.Types.Dictionary, "Decedent Disposition", "Decedent's Disposition Method.", true, IGURL.DecedentDispositionMethod, true, 1)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='80905-3')", "")]
    private Map<String, String> DecedentDispositionMethod;
    public Map<String, String> getDecedentDispositionMethod()
    {
        if (DispositionMethod != null && DispositionMethod.getValue() != null && DispositionMethod.getValue() instanceof CodeableConcept)
        {
            return DeathCertificateDocumentUtil.CodeableConceptToMap((CodeableConcept) DispositionMethod.getValue());
        }
        return DeathCertificateDocumentUtil.EmptyCodeableMap();
    }

    public void setDecedentDispositionMethod(Map<String, String> value)
    {
        if (DispositionMethod == null)
        {
            DispositionMethod = new Observation();
            DispositionMethod.setId(UUID.randomUUID().toString());
            DispositionMethod.setMeta(new Meta());
            CanonicalType[] dispositionmethod_profile = {URL.ProfileURL.DecedentDispositionMethod};
            DispositionMethod.getMeta().setProfile(Arrays.asList(dispositionmethod_profile));
            DispositionMethod.setStatus(ObservationStatus.FINAL);
            DispositionMethod.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "80905-3", "Body disposition method")));//, null);
            DispositionMethod.setSubject(new Reference("urn:uuid:" + Decedent.getId()));
            //                    DispositionMethod.Performer.add(new Reference("urn:uuid:" + Mortician.getId()));
            DispositionMethod.setValue(DeathCertificateDocumentUtil.MapToCodeableConcept(value));
            AddReferenceToComposition(DispositionMethod.getId(), "DecedentDisposition");
            Resource resource = DispositionMethod;
            resource.setId("urn:uuid:" + DispositionMethod.getId());
            Bundle.addEntry(new BundleEntryComponent().setResource(resource));
        }
        else
        {
            DispositionMethod.setValue(MapToCodeableConcept(value));
        }
    }

    /// <summary>Decedent's Disposition Method Helper.</summary>
    /// <value>the decedent's disposition method. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DecedentDispositionMethodHelper = ValueSets.MethodOfDisposition.Burial;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Disposition Method: {ExampleDeathRecord.DecedentDispositionMethodHelper}");</para>
    /// </example>

    //  [Property("Decedent Disposition Method Helper", Property.Types.String, "Decedent Disposition", "Decedent's Disposition Method.", false, IGURL.DecedentDispositionMethod, true, 1)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='80905-3')", "")]
    public String getDecedentDispositionMethodHelper()
    {
        if (DecedentDispositionMethod.containsKey("code") && isNullOrWhiteSpace(DecedentDispositionMethod.get("code")))
        {
            return DecedentDispositionMethod.get("code");
        }
        return null;
    }

    public void setDecedentDispositionMethodHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("DecedentDispositionMethod", value, ValueSets.MethodOfDisposition.Codes);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    //
    // Record Properties: Death Investigation
    //
    /////////////////////////////////////////////////////////////////////////////////

    /// <summary>Autopsy Performed Indicator.</summary>
    /// <value>autopsy performed indicator. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    /// <para>code.add("code", "Y");</para>
    /// <para>code.add("system", CodeSystems.PH_YesNo_HL7_2x);</para>
    /// <para>code.add("display", "Yes");</para>
    /// <para>ExampleDeathRecord.AutopsyPerformedIndicator = code;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Autopsy Performed Indicator: {ExampleDeathRecord.AutopsyPerformedIndicator['display']}");</para>
    /// </example>
    //  [Property("Autopsy Performed Indicator", Property.Types.Dictionary, "Death Investigation", "Autopsy Performed Indicator.", true, IGURL.AutopsyPerformedIndicator, true, 28)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='85699-7')", "")]
    private Map<String, String> AutopsyPerformedIndicator;

    public Map<String, String> getAutopsyPerformedIndicator()
    {
        if (AutopsyPerformed != null && AutopsyPerformed.getValue() != null && AutopsyPerformed.getValue() instanceof CodeableConcept)
        {
            return CodeableConceptToMap((CodeableConcept) AutopsyPerformed.getValue());
        }
        return EmptyCodeableMap();
    }

    public void setAutopsyPerformedIndicator(Map<String, String> value)
    {
        if (isMapEmptyOrDefault(value) && AutopsyPerformed == null)
        {
            return;
        }
        if (AutopsyPerformed == null)
        {
            CreateAutopsyPerformed();
        }

        AutopsyPerformed.setValue(MapToCodeableConcept(value));
    }

    /// <summary>Autopsy Performed Indicator Helper. This is a helper method, to access the code use the AutopsyPerformedIndicator property.</summary>
    /// <value>autopsy performed indicator. A null value indicates "not applicable".</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.AutopsyPerformedIndicatorHelper = "Y"";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Autopsy Performed Indicator: {ExampleDeathRecord.AutopsyPerformedIndicatorBoolean}");</para>
    /// </example>
    //  [Property("Autopsy Performed Indicator Helper", Property.Types.String, "Death Investigation", "Autopsy Performed Indicator.", false, IGURL.AutopsyPerformedIndicator, true, 29)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='85699-7')", "")]
    public String getAutopsyPerformedIndicatorHelper()
    {
        if (AutopsyPerformedIndicator.containsKey("code") && isNullOrWhiteSpace(AutopsyPerformedIndicator.get("code")))
        {
            return AutopsyPerformedIndicator.get("code");
        }
        return null;
    }

    public void setAutopsyPerformedIndicatorHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("AutopsyPerformedIndicator", value, ValueSets.YesNoUnknown.Codes);
        }
    }

    // The idea here is that we have getters and setters for each of the parts of the death datetime, which get used in IJEMortality.cs
    // These getters and setters 1) use the DeathDateObs Observation 2) get and set values on the PartialDateTime extension using helpers that
    // can be reused across year, month, etc. 3) interpret -1 and null as data being absent (intentionally and unintentially, respectively),
    // and so set the data absent reason if value is -1 or null 4) when getting, look also in the valueDateTime and return the year from there
    // if it happens to be set (but never bother to set it ourselves)

    /// <summary>Decedent's Year of Death.</summary>
    /// <value>the decedent's year of death, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DeathYear = 2018;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Year of Death: {ExampleDeathRecord.DeathYear}");</para>
    /// </example>
    //  [Property("DeathYear", Property.Types.Int32, "Death Investigation", "Decedent's Year of Death.", true, IGURL.DeathDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='81956-5')", "")]
    public Integer getDeathYear()
    {
        if (DeathDateObs != null && DeathDateObs.getValue() != null)
        {
            return DeathCertificateDocumentUtil.GetDateFragmentOrPartialDate(DeathDateObs.getValue(), URL.ExtensionURL.DateYear);
        }
        return null;
    }

    public void setDeathYear(Integer value)
    {
        if (DeathDateObs == null)
        {
            CreateDeathDateObs();
        }
        SetPartialDate(DeathDateObs.getValue().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDateTime)).findFirst().get(), URL.ExtensionURL.DateYear, value);
        UpdateDeathRecordIdentifier();
    }

    /// <summary>Decedent's Month of Death.</summary>
    /// <value>the decedent's month of death, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DeathMonth = 6;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Month of Death: {ExampleDeathRecord.DeathMonth}");</para>
    /// </example>
    //  [Property("DeathMonth", Property.Types.Int32, "Death Investigation", "Decedent's Month of Death.", true, IGURL.DeathDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='81956-5')", "")]
    public Integer setDeathMonth()
    {
        if (DeathDateObs != null && DeathDateObs.getValue() != null)
        {
            return GetDateFragmentOrPartialDate(DeathDateObs.getValue(), URL.ExtensionURL.DateMonth);
        }
        return null;
    }

    public void setDeathMonth(Integer value)
    {
        if (DeathDateObs == null)
        {
            CreateDeathDateObs();
        }
        SetPartialDate(DeathDateObs.getValue().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDateTime)).findFirst().get(), URL.ExtensionURL.DateMonth, value);
    }


    /// <summary>Decedent's Day of Death.</summary>
    /// <value>the decedent's day of death, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DeathDay = 16;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Day of Death: {ExampleDeathRecord.DeathDay}");</para>
    /// </example>
    //  [Property("DeathDay", Property.Types.Int32, "Death Investigation", "Decedent's Day of Death.", true, IGURL.DeathDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='81956-5')", "")]
    public Integer getDeathDay()
    {
        if (DeathDateObs != null && DeathDateObs.getValue() != null)
        {
            return GetDateFragmentOrPartialDate(DeathDateObs.getValue(), URL.ExtensionURL.DateDay);
        }
        return null;
    }

    public void setDeathDay(String value)
    {
        if (DeathDateObs == null)
        {
            CreateDeathDateObs();
        }
        SetPartialDate(DeathDateObs.getValue().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDateTime), URL.ExtensionURL.DateDay, value);
    }

    /// <summary>Decedent's Time of Death.</summary>
    /// <value>the decedent's time of death, or "-1" if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DeathTime = "07:15:00";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Time of Death: {ExampleDeathRecord.DeathTime}");</para>
    /// </example>
    //  [Property("DeathTime", Property.Types.String, "Death Investigation", "Decedent's Time of Death.", true, IGURL.DeathDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='81956-5')", "")]
    public String getDeathTime()
    {
        if (DeathDateObs != null && DeathDateObs.getValue() != null)
        {
            return GetTimeFragmentOrPartialTime(DeathDateObs.getValue());
        }
        return null;
    }

    public void setDeathTime(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (DeathDateObs == null)
        {
            CreateDeathDateObs();
        }
        DeathCertificateDocumentUtil.SetPartialTime(DeathDateObs.getValue().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDateTime)).findFirst().get(), value);
    }

    /* START datetimePronouncedDead */
    /// <summary>Decedent's Pronouncement Year of Death.</summary>
    /// <value>the decedent's pronouncement year of death, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DeathPronouncementYear = 2018;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Pronouncement Year of Death: {ExampleDeathRecord.DateOfDeathPronouncementYear}");</para>
    /// </example>
    //  [Property("DateOfDeathPronouncementYear", Property.Types.Int32, "Death Investigation", "Decedent's Pronouncement Year of Death.", true, IGURL.DeathDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='81956-5')", "")]
    public Integer getDateOfDeathPronouncementYear()
    {
        Observation.ObservationComponentComponent pronouncementDateObs = GetDateOfDeathPronouncementObs();
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() != null && pronouncementDateObs.getValue() instanceof DateTimeType)
        {
            return GetDateFragment(pronouncementDateObs.getValue(), URL.ExtensionURL.DateYear);
        }
        return null;
    }

    public void setDateOfDeathPronouncementYear(String value)
    {
        if (value == null  ||  value.isBlank())
        {
            return;
        }
        Observation.ObservationComponentComponent pronouncementDateObs = GetOrCreateDateOfDeathPronouncementObs();
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() != null && pronouncementDateObs.getValue() instanceof TimeType)
        {
            // we need to convert to a DateTimeType
            pronouncementDateObs.setValue(DeathCertificateDocumentUtil.ConvertFhirTimeToDateTimeType((TimeType) pronouncementDateObs.getValue()));
        }
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() == null)
        {
            pronouncementDateObs.setValue(new DateTimeType()); // initialize date object
        }
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() != null && pronouncementDateObs.getValue() instanceof DateTimeType)
        {
            // If we have a basic value as a valueDateTime use that, otherwise pull from the PartialDateTime extension
            OffsetDateTime offsetDateTime = null;
            if (pronouncementDateObs.getValue() instanceof DateTimeType && ((DateTimeType) pronouncementDateObs.getValue()).getValue() != null)
            {
                // Note: We can't just call ToOffsetDateTime() on the DateTimeType because want the datetime in whatever local time zone was provided
                offsetDateTime = OffsetDateTime.parse((CharSequence) ((DateTimeType) pronouncementDateObs.getValue()).getValue());
            }

            OffsetDateTime dt = offsetDateTime != null ? OffsetDateTime.MIN : null;
            Calendar calendar = Calendar.getInstance();
            calendar.set(dt.getYear() - 1900, dt.getMonthValue(), dt.getDayOfMonth(), dt.getHour(), dt.getMinute(), dt.getSecond());
            DateTimeType newFdt = new DateTimeType(calendar);
            pronouncementDateObs.setValue(newFdt);
        }
    }


    /// <summary>Decedent's Pronouncement Month of Death.</summary>
    /// <value>the decedent's pronouncement month of death, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DateOfDeathPronouncementMonth = 6;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Pronouncement Month of Death: {ExampleDeathRecord.DateOfDeathPronouncementMonth}");</para>
    /// </example>
    //  [Property("DeathMonth", Property.Types.Int32, "Death Investigation", "Decedent's Pronouncement Month of Death.", true, IGURL.DeathDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='81956-5')", "")]
    public Integer getDateOfDeathPronouncementMonth()
    {
        Observation.ObservationComponentComponent pronouncementDateObs = GetDateOfDeathPronouncementObs();
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() != null && pronouncementDateObs.getValue() instanceof DateTimeType)
        {
            return GetDateFragment(pronouncementDateObs.getValue(), URL.ExtensionURL.DateMonth);
        }
        return null;
    }

    public void setDateOfDeathPronouncementMonth(String value)
    {
        if (value == null  ||  value.isBlank())
        {
            return;
        }
        Observation.ObservationComponentComponent pronouncementDateObs = GetOrCreateDateOfDeathPronouncementObs();
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() != null && pronouncementDateObs.getValue() instanceof TimeType)
        {
            // we need to convert to a DateTimeType
            pronouncementDateObs.setValue(ConvertFhirTimeToDateTimeType((TimeType) pronouncementDateObs.getValue()));
        }
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() == null)
        {
            pronouncementDateObs.setValue(new DateTimeType()); // initialize date object
        }
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() != null && pronouncementDateObs.getValue() instanceof DateTimeType)
        {
            // If we have a basic value as a valueDateTime use that, otherwise pull from the PartialDateTime extension
            OffsetDateTime offsetDateTime = null;
            if (pronouncementDateObs.getValue() instanceof DateTimeType && ((DateTimeType) pronouncementDateObs.getValue()).getValue() != null)
            {
                // Note: We can't just call ToOffsetDateTime() on the DateTimeType because want the datetime in whatever local time zone was provided
                offsetDateTime = OffsetDateTime.parse((CharSequence) pronouncementDateObs.getValueDateTimeType());
            }
            OffsetDateTime dt = offsetDateTime != null ? OffsetDateTime.MIN : null;
            Calendar calendar = Calendar.getInstance();
            calendar.set(dt.getYear() - 1900, dt.getMonthValue(), dt.getDayOfMonth(), dt.getHour(), dt.getMinute(), dt.getSecond());
            DateTimeType newFdt = new DateTimeType(calendar);
            pronouncementDateObs.setValue(newFdt);
        }
    }

    /// <summary>Decedent's Pronouncement Day of Death.</summary>
    /// <value>the decedent's pronouncement day of death, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DateOfDeathPronouncementDay = 16;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Prounecement Day of Death: {ExampleDeathRecord.DateOfDeathPronouncementDay}");</para>
    /// </example>
    //  [Property("DeathDay", Property.Types.Int32, "Death Investigation", "Decedent's Pronouncement Day of Death.", true, IGURL.DeathDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='81956-5')", "")]
    public Integer getDateOfDeathPronouncementDay()
    {
        Observation.ObservationComponentComponent pronouncementDateObs = GetDateOfDeathPronouncementObs();
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() != null && pronouncementDateObs.getValue() instanceof DateTimeType)
        {
            return GetDateFragment(pronouncementDateObs.getValue(), URL.ExtensionURL.DateDay);
        }
        return null;

    }

    public void setDateOfDeathPronouncementDay(String value)
    {
        if (value == null  ||  value.isBlank())
        {
            return;
        }
        Observation.ObservationComponentComponent pronouncementDateObs = GetOrCreateDateOfDeathPronouncementObs();
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() != null)
        {
            // we need to convert to a DateTimeType
            pronouncementDateObs.setValue(ConvertFhirTimeToDateTimeType((TimeType) pronouncementDateObs.getValue()));
        }
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() == null)
        {
            pronouncementDateObs.setValue(new DateTimeType(); // initialize date object
        }
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() != null && pronouncementDateObs.getValue() instanceof DateTimeType)
        {
            // If we have a basic value as a valueDateTime use that, otherwise pull from the PartialDateTime extension
            OffsetDateTime offsetDateTime = null;
            if (pronouncementDateObs.getValue() instanceof DateTimeType && ((DateTimeType) pronouncementDateObs.getValue()).getValue() != null)
            {
                // Note: We can't just call ToOffsetDateTime() on the DateTimeType because want the datetime in whatever local time zone was provided
                offsetDateTime = OffsetDateTime.parse((CharSequence) pronouncementDateObs.getValueDateTimeType());
            }
            OffsetDateTime dt = offsetDateTime != null ? OffsetDateTime.MIN : null;
            Calendar calendar = Calendar.getInstance();
            calendar.set(dt.getYear() - 1900, dt.getMonthValue(), dt.getDayOfMonth(), dt.getHour(), dt.getMinute(), dt.getSecond());
            DateTimeType newFdt = new DateTimeType(calendar);// value.getValue(), dt.Hour, dt.Minute, dt.Second, TimeSpan.Zero);
            pronouncementDateObs.setValue(newFdt);
        }
    }

    /// <summary>Decedent's Pronouncement Time of Death.</summary>
    /// <value>the decedent's pronouncement time of death, or "-1" if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DateOfDeathPronouncementTime = "07:15:00";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Pronouncement Time of Death: {ExampleDeathRecord.DateOfDeathPronouncementTime}");</para>
    /// </example>
    //  [Property("DeathTime", Property.Types.String, "Death Investigation", "Decedent's Pronoucement Time of Death.", true, IGURL.DeathDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='81956-5')", "")]
    private String DateOfDeathPronouncementTime;

    public String getDateOfDeathPronouncementTime()
    {
        Observation.ObservationComponentComponent pronouncementDateObs = GetDateOfDeathPronouncementObs();
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() != null && pronouncementDateObs.getValue() instanceof DateTimeType)
        {
            return GetTimeFragment(pronouncementDateObs.getValue());
        }
        if (pronouncementDateObs != null && pronouncementDateObs.getValue() != null && pronouncementDateObs.getValue() instanceof DateTimeType)
        {
            DateTimeType time = (DateTimeType) pronouncementDateObs.getValue();
            return time.getValue().toString();
        }
        return null;
    }

    public void setDateOfDeathPronouncementTime(String value)
    {
        if (isNullOrEmpty(value))
        {
            return;
        }

        // we need to force it to be 00:00:00 format to be compliant with the IG because the FHIR class doesn't
        if (value.length() < 8)
        {
            value += ":";
            value = StringUtils.rightPad(value, 8, "0");
        }
        Observation.ObservationComponentComponent pronouncementDateObs = GetOrCreateDateOfDeathPronouncementObs();
        // if we are only storing time, set just the valueTime
        if (pronouncementDateObs != null && (pronouncementDateObs.getValue() == null  ||  pronouncementDateObs.getValue() instanceof Time))
        {
            pronouncementDateObs.setValue(new DateTimeType(value)); // set to FhirTime
            return;
        }

        // otherwise we need to set the time portion of the valueDateTime
        if (pronouncementDateObs.getValue() != null && pronouncementDateObs.getValue() instanceof DateTimeType)
        {
            // set time part of DateTimeType
            DateTimeType ft = new DateTimeType(value);
            DateTimeType fdt = (DateTimeType) pronouncementDateObs.getValue();
            OffsetDateTime offsetDateTime = null;
            if (pronouncementDateObs.getValue() instanceof DateTimeType && ((DateTimeType) pronouncementDateObs.getValue()).getValue() != null)
            {
                // Note: We can't just call ToOffsetDateTime() on the DateTimeType because want the datetime in whatever local time zone was provided
                offsetDateTime = OffsetDateTime.parse((CharSequence) pronouncementDateObs.getValueDateTimeType());
            }
            OffsetDateTime dt = offsetDateTime != null ? OffsetDateTime.MIN : null;
            Calendar calendar = Calendar.getInstance();
            calendar.set(dt.getYear() - 1900, dt.getMonthValue(), dt.getDayOfMonth(), dt.getHour(), dt.getMinute(), dt.getSecond());
            DateTimeType newFdt = new DateTimeType(calendar);
            pronouncementDateObs.setValue(newFdt);
        }
    }

    /* END datetimePronouncedDead */

    /// <summary>DateOfDeathDeterminationMethod.</summary>
    /// <value>method. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    /// <para>code.add("code", "exact");</para>
    /// <para>code.add("system", CodeSystems.DateOfDeathDeterminationMethods);</para>
    /// <para>code.add("display", "exact");</para>
    /// <para>ExampleDeathRecord.DateOfDeathDeterminationMethod = code;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Date of Death Determination Method: {ExampleDeathRecord.DateOfDeathDeterminationMethod['display']}");</para>
    /// </example>

    //  [Property("DateOfDeathDeterminationMethod", Property.Types.Dictionary, "Death Investigation", "Date of Death Determination Method.", true, IGURL.DeathDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='81956-5').method", "")]
    private Map<String, String> DateOfDeathDeterminationMethod;

    public Map<String, String> getDateOfDeathDeterminationMethod()
    {
        if (DeathDateObs != null && (DeathDateObs.getMethod()) != null)
        {
            return CodeableConceptToMap(DeathDateObs.getMethod());
        }
        return EmptyCodeableMap();
    }

    public void setDateOfDeathDeterminationMethod(Map<String, String> value)
    {
        if (DeathDateObs == null)
        {
            CreateDeathDateObs();
        }
        DeathDateObs.setMethod(MapToCodeableConcept(value));
    }

    /// <summary>Decedent's Date/Time of Death.</summary>
    /// <value>the decedent's date and time of death</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DateOfDeath = "2018-02-19T16:48:06-05:00";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Date of Death: {ExampleDeathRecord.DateOfDeath}");</para>
    /// </example>
    //  [Property("Date/Time Of Death", Property.Types.StringDateTime, "Death Investigation", "Decedent's Date+Time of Death.", true, IGURL.DeathDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='81956-5')", "")]
    private String DateOfDeath;
    private Integer DeathYear;
    private Integer DeathMonth;
    private Integer DeathDay;
    private String DeathTime;

    public String getDateOfDeath()
    {
        // We support this legacy API entrypoint via the new partial date and time entrypoints
        if (DeathYear != null && DeathYear != -1 && DeathMonth != null && DeathMonth != -1 && DeathDay != null && DeathDay != -1 && DeathTime != null && DeathTime != "-1")
        {
            OffsetDateTime parsedTime = OffsetDateTime.parse(DeathTime);
            //OffsetDateTime parsedTime = offsetDateTime != null ? OffsetDateTime.MIN : null;
            if (parsedTime != null)
            {
                //OffsetDateTime result = new OffsetDateTime((int) DeathYear, (int) DeathMonth, (int) DeathDay, parsedTime.getHour(), parsedTime.getMinute(), parsedTime.getSecond());//, TimeSpan.Zero);
                LocalDateTime localDateTime = LocalDateTime.of(DeathYear - 1900, (int)DeathMonth, (int)DeathDay, parsedTime.getHour(), parsedTime.getMinute(), parsedTime.getSecond());
                OffsetDateTime result = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
                return result.toString();//("s");
            }
        }
        else if (DeathYear != null && DeathYear != -1 && DeathMonth != null && DeathMonth != -1 && DeathDay != null && DeathDay != -1)
        {
            //DateTime result = new DateTime((int) DeathYear, (int) DeathMonth, (int) DeathDay);
            LocalDateTime localDateTime = LocalDateTime.of(DeathYear - 1900, (int)DeathMonth, (int)DeathDay, 0, 0, 0);
            OffsetDateTime result = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
            return result.toString();//("s");
        }
        return null;
    }

    public void setDateOfDeath(String value)
    {
        // We support this legacy API entrypoint via the new partial date and time entrypoints
        OffsetDateTime parsedTime = OffsetDateTime.parse(value);
        if (parsedTime != null)
        {
            DeathYear = parsedTime.getYear();
            DeathMonth = parsedTime.getMonthValue();
            DeathDay = parsedTime.getDayOfMonth();
            //TimeSpan timeSpan = new TimeSpan(0, parsedTime.getHour(), parsedTime.getMinute(), parsedTime.getSecond());
            Map mapHourMinSec = DeathCertificateDocumentUtil.getHourMinSecFromParsedTime(parsedTime);
            DeathTime = mapHourMinSec.toString();// @ "hh\:mm\:ss");
        }
    }

    /// <summary>Decedent's Date/Time of Death Pronouncement as a component.</summary>
    /// <value>the decedent's date and time of death pronouncement observation component</value>
    public Observation.ObservationComponentComponent GetDateOfDeathPronouncementObs()
    {
        if (DeathDateObs != null && DeathDateObs.getComponent().size() > 0) // if there is a value for death pronouncement type, return it
        {
            Observation.ObservationComponentComponent pronComp = DeathDateObs.getComponent().stream().filter(entry -> entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("80616-6")).findFirst().get();
            if (pronComp != null && pronComp.getValue() != null && pronComp.getValue() instanceof DateTimeType)
            {
                return pronComp;
            }
        }
        return null;
    }

    /// <summary>Get or Create Decedent's Date/Time of Death Pronouncement as a component.</summary>
    /// <value>the decedent's date and time of death pronouncement observation component, if not present create it, return it in either case</value>
    public Observation.ObservationComponentComponent GetOrCreateDateOfDeathPronouncementObs()
    {
        Observation.ObservationComponentComponent pronouncementDateObs = GetDateOfDeathPronouncementObs();
        if (pronouncementDateObs == null)
        {
            pronouncementDateObs = CreateDateOfDeathPronouncementObs();
        }
        return pronouncementDateObs;
    }

    /// <summary>Decedent's Date/Time of Death Pronouncement.</summary>
    /// <value>the decedent's date and time of death pronouncement</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DateOfDeathPronouncement = "2018-02-20T16:48:06-05:00";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Date of Death Pronouncement: {ExampleDeathRecord.DateOfDeathPronouncement}");</para>
    /// </example>
    //  [Property("Date/Time Of Death Pronouncement", Property.Types.StringDateTime, "Death Investigation", "Decedent's Date/Time of Death Pronouncement.", true, IGURL.DeathDate, false, 20)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='81956-5').getComponent().stream().findAny(code.coding.code='80616-6')", "")]
    private Integer DateOfDeathPronouncementYear;
    private Integer DateOfDeathPronouncementMonth;
    private Integer DateOfDeathPronouncementDay;
    private String DateOfDeathPronouncementTime;

    public String getDateOfDeathPronouncement()
    {
        if (DateOfDeathPronouncementYear != null && DateOfDeathPronouncementYear != -1 && DateOfDeathPronouncementMonth != null && DateOfDeathPronouncementMonth != -1 && DateOfDeathPronouncementDay != null && DateOfDeathPronouncementDay != -1 && DateOfDeathPronouncementTime != null && DateOfDeathPronouncementTime != "-1")
        {
            OffsetDateTime parsedTime = OffsetDateTime.parse(DateOfDeathPronouncementTime);
            if (parsedTime != null)
            {
                OffsetDateTime result = OffsetDateTime.of((int) DateOfDeathPronouncementYear, (int) DateOfDeathPronouncementMonth, (int) DateOfDeathPronouncementDay, parsedTime.getHour(), parsedTime.getMinute(), parsedTime.getSecond(), 0, ZoneOffset.UTC);
                return result.toString();//("s");
            }
        }
        else if (DateOfDeathPronouncementYear != null && DateOfDeathPronouncementYear != -1 && DateOfDeathPronouncementMonth != null && DateOfDeathPronouncementMonth != -1 && DateOfDeathPronouncementDay != null && DateOfDeathPronouncementDay != -1)
        {
            LocalDateTime result = LocalDateTime.of((int) DateOfDeathPronouncementYear, (int) DateOfDeathPronouncementMonth, (int) DateOfDeathPronouncementDay, 0, 0, 0);
            return result.toString();//("s");
        }
        else if (DateOfDeathPronouncementYear == null && DateOfDeathPronouncementMonth == null && DateOfDeathPronouncementDay == null && DateOfDeathPronouncementTime != null)
        {
            return DateOfDeathPronouncementTime;
        }
        return null;
    }

    public void setDateOfDeathPronouncement(String value)
    {
        if (isNullOrEmpty(value))
        {
            return;
        }
        // We support this legacy API entrypoint via the new partial date and time entrypoints
        OffsetDateTime parsedTime = OffsetDateTime.parse(value);
        if (parsedTime != null)
        {
            DateOfDeathPronouncementYear = parsedTime.getYear();
            DateOfDeathPronouncementMonth = parsedTime.getMonthValue();
            DateOfDeathPronouncementDay = parsedTime.getDayOfMonth();
//            TimeSpan timeSpan = new TimeSpan(0, parsedTime.getHour(), parsedTime.getMinute(), parsedTime.getSecond());
//            DateOfDeathPronouncementTime = timeSpan.toString( @ "hh\:mm\:ss");

            Map map = DeathCertificateDocumentUtil.getHourMinSecFromParsedTime(parsedTime);
            DateOfDeathPronouncementTime = new StringBuffer(String.valueOf(map.get("hh"))).append(String.valueOf(map.get("mm"))).append(String.valueOf(map.get("ss"))).toString();// timeSpan.toString()); //@"hh\:mm\:ss"));
        }
    }

    /// <summary>Decedent's Year of Surgery.</summary>
    /// <value>the decedent's year of surgery, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SurgeryYear = 2018;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Year of Surgery: {ExampleDeathRecord.SurgeryYear}");</para>
    /// </example>
    //  [Property("SurgeryYear", Property.Types.Int32, "Death Investigation", "Decedent's Year of Surgery.", true, IGURL.SurgeryDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='80992-1')", "")]
    private Integer SurgeryYear;

    public Integer getSurgeryYear()
    {
        if (SurgeryDateObs != null && SurgeryDateObs.getValue() != null)
        {
            return GetDateFragmentOrPartialDate(SurgeryDateObs.getValue(), URL.ExtensionURL.DateYear);
        }
        return null;
    }

    public void setSurgeryYear(String value)
    {
        if (SurgeryDateObs == null)
        {
            CreateSurgeryDateObs();
        }
        SetPartialDate(SurgeryDateObs.getValue().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDate), URL.ExtensionURL.DateYear, value);
    }


    /// <summary>Decedent's Month of Surgery.</summary>
    /// <value>the decedent's month of surgery, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SurgeryMonth = 6;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Month of Surgery: {ExampleDeathRecord.SurgeryMonth}");</para>
    /// </example>
    //  [Property("SurgeryMonth", Property.Types.Int32, "Death Investigation", "Decedent's Month of Surgery.", true, IGURL.SurgeryDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='80992-1')", "")]
    private Integer SurgeryMonth;

    public Integer getSurgeryMonth()
    {
        if (SurgeryDateObs != null && SurgeryDateObs.getValue() != null)
        {
            return GetDateFragmentOrPartialDate(SurgeryDateObs.getValue(), URL.ExtensionURL.DateMonth);
        }
        return null;
    }

    public void setSurgeryMonth(String value)
    {
        if (SurgeryDateObs == null)
        {
            CreateSurgeryDateObs();
        }
        SetPartialDate(SurgeryDateObs.getValue().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDate), URL.ExtensionURL.DateMonth, value);
    }

    /// <summary>Decedent's Day of Surgery.</summary>
    /// <value>the decedent's day of surgery, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SurgeryDay = 16;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Day of Surgery: {ExampleDeathRecord.SurgeryDay}");</para>
    /// </example>
    //  [Property("SurgeryDay", Property.Types.Int32, "Death Investigation", "Decedent's Day of Surgery.", true, IGURL.SurgeryDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='80992-1')", "")]
    private Integer SurgeryDay;

    public Integer getSurgeryDay()
    {
        if (SurgeryDateObs != null && SurgeryDateObs.getValue() != null)
        {
            return GetDateFragmentOrPartialDate(SurgeryDateObs.getValue(), URL.ExtensionURL.DateDay);
        }
        return null;
    }

    public void setSurgeryDay(Integer value)
    {
        if (SurgeryDateObs == null)
        {
            CreateSurgeryDateObs();
        }
        SetPartialDate(SurgeryDateObs.getValue().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDate)).findFirst().get(), URL.ExtensionURL.DateDay, value);
    }

    /// <summary>Decedent's Surgery Date.</summary>
    /// <value>the decedent's date of surgery</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SurgeryDate = "2018-02-19";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent Surgery Date: {ExampleDeathRecord.SurgeryDate}");</para>
    /// </example>
    //  [Property("Surgery Date", Property.Types.StringDateTime, "Death Investigation", "Decedent's Date and Time of Surgery.", true, IGURL.SurgeryDate, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='80992-1')", "")]
    private String SurgeryDate;

    public String getSurgeryDate()
    {
        // We support this legacy-style API entrypoint via the new partial date and time entrypoints
        if (SurgeryYear != null && SurgeryYear != -1 && SurgeryMonth != null && SurgeryMonth != -1 && SurgeryDay != null && SurgeryDay != -1)
        {
            Date result = new Date((int) SurgeryYear, (int) SurgeryMonth, (int) SurgeryDay);
            return result.toString();
        }
        return null;
    }

    public void setSurgeryDate(String value)
    {
        // We support this legacy-style API entrypoint via the new partial date and time entrypoints
        OffsetDateTime parsedDate = OffsetDateTime.parse(value);
        if (parsedDate != null)
        {
            SurgeryYear = parsedDate.getYear();
            SurgeryMonth = parsedDate.getMonthValue();
            SurgeryDay = parsedDate.getDayOfMonth();
        }
    }

    /// <summary>Autopsy Results Available.</summary>
    /// <value>autopsy results available. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    /// <para>code.add("code", "Y");</para>
    /// <para>code.add("system", CodeSystems.PH_YesNo_HL7_2x);</para>
    /// <para>code.add("display", "Yes");</para>
    /// <para>ExampleDeathRecord.AutopsyResultsAvailable = code;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Autopsy Results Available: {ExampleDeathRecord.AutopsyResultsAvailable['display']}");</para>
    /// </example>
    //  [Property("Autopsy Results Available", Property.Types.Dictionary, "Death Investigation", "Autopsy results available, used to complete cause of death.", true, IGURL.AutopsyPerformedIndicator, true, 30)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='85699-7')", "")]
    private Map<String, String> AutopsyResultsAvailable;

    public Map<String, String> getAutopsyResultsAvailable()
    {
        if (AutopsyPerformed == null || AutopsyPerformed.getComponent() == null)
        {
            return EmptyCodeableMap();
        }
        Observation.ObservationComponentComponent performed = AutopsyPerformed.getComponent().stream().filter(entry -> entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("69436-4")).findFirst().get();
        if (performed != null)
        {
            return CodeableConceptToMap((CodeableConcept) performed.getValue());
        }
        return EmptyCodeableMap();
    }

    public void setAutopsyResultsAvailable(Map<String, String> value)
    {
        if (DeathCertificateDocumentUtil.isMapEmptyOrDefault(value) && AutopsyPerformed == null)
        {
            return;
        }
        if (AutopsyPerformed == null)
        {
            CreateAutopsyPerformed();
        }
        Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "69436-4", "Autopsy results available")));//, null);
        component.setValue(MapToCodeableConcept(value));
        AutopsyPerformed.getComponent().clear();
        AutopsyPerformed.getComponent().add(component);
    }

    /// <summary>Autopsy Results Available Helper. This is a convenience method, to access the coded value use AutopsyResultsAvailable.</summary>
    /// <value>autopsy results available. A null value indicates "not applicable".</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.AutopsyResultsAvailableHelper = "N";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Autopsy Results Available: {ExampleDeathRecord.AutopsyResultsAvailableHelper}");</para>
    /// </example>
    //  [Property("Autopsy Results Available Helper", Property.Types.String, "Death Investigation", "Autopsy results available, used to complete cause of death.", false, IGURL.AutopsyPerformedIndicator, true, 31)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='85699-7')", "")]
    private String AutopsyResultsAvailableHelper;

    public String getAutopsyResultsAvailableHelper()
    {
        if (AutopsyResultsAvailable.containsKey("code") && isNullOrWhiteSpace(AutopsyResultsAvailable.get("code")))
        {
            return AutopsyResultsAvailable.get("code");
        }
        return null;
    }

    public void setAutopsyResultsAvailableHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("AutopsyResultsAvailable", value, ValueSets.YesNoUnknownNotApplicable.Codes);
        }
    }

    /// <summary>Death Location Jurisdiction.</summary>
    /// <value>the vital record jurisdiction identifier.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DeathLocationJurisdiction = "MA";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Death Location Jurisdiction: {ExampleDeathRecord.DeathLocationJurisdiction}");</para>
    /// </example>

    //  [Property("Death Location Jurisdiction", Property.Types.String, "Death Investigation", "Vital Records Jurisdiction of Death Location (two character jurisdiction code, e.g. CA).", true, IGURL.DeathLocation, false, 16)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Location).stream().findAny(type.coding.code='death')", "")]
    private String DeathLocationJurisdiction;

    public String getDeathLocationJurisdiction()
    {
        // If addressJurisdiction is present use it, otherwise return the addressState
        if (DeathLocationAddress.containsKey("addressJurisdiction") && isNullOrWhiteSpace(DeathLocationAddress.get("addressJurisdiction").toString()))
        {
            return DeathLocationAddress.get("addressJurisdiction").toString();
        }
        if (DeathLocationAddress.containsKey("addressState") && isNullOrWhiteSpace(DeathLocationAddress.get("addressState").toString()))
        {
            return DeathLocationAddress.get("addressState").toString();
        }
        return null;
    }

    public void setDeathLocationJurisdiction(StringType value)
    {
        // If the jurisdiction is YC (New York City) set the addressJurisdiction to YC and the addressState to NY, otherwise set both to the same;
        // setting the addressJurisdiction is technically optional but the way we use DeathLocationAddress to constantly read the existing values
        // when adding new values means that having both set correctly is important for consistency
        if (isNullOrWhiteSpace(value.toString()))
        {
            Map<String, StringType> currentAddress = DeathLocationAddress;
            if (value.equals("YC"))
            {
                currentAddress.put("addressJurisdiction", value);
                currentAddress.put("addressState", new StringType("NY"));
            }
            else
            {
                currentAddress.put("addressJurisdiction", value);
                currentAddress.put("addressState", value);
            }
            DeathLocationAddress = currentAddress;
            UpdateDeathRecordIdentifier();
        }
    }

    /// <summary>Location of Death.</summary>
    /// <value>location of death. A Map representing an address, containing the following key/value pairs:
    /// <para>"addressLine1" - address, line one</para>
    /// <para>"addressLine2" - address, line two</para>
    /// <para>"addressCity" - address, city</para>
    /// <para>"addressCounty" - address, county</para>
    /// <para>"addressState" - address, state</para>
    /// <para>"addressZip" - address, zip</para>
    /// <para>"addressCountry" - address, country</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; address = new Map&lt;String, String&gt;();</para>
    /// <para>address.add("addressLine1", "123456789 Test Street");</para>
    /// <para>address.add("addressLine2", "Unit 3");</para>
    /// <para>address.add("addressCity", "Boston");</para>
    /// <para>address.add("addressCounty", "Suffolk");</para>
    /// <para>address.add("addressState", "MA");</para>
    /// <para>address.add("addressZip", "12345");</para>
    /// <para>address.add("addressCountry", "US");</para>
    /// <para>ExampleDeathRecord.DeathLocationAddress = address;</para>
    /// <para>// Getter:</para>
    /// <para>foreach(var pair in ExampleDeathRecord.DeathLocationAddress)</para>
    /// <para>{</para>
    /// <para>  Console.WriteLine($"\DeathLocationAddress key: {pair.Key}: value: {pair.getValue()}");</para>
    /// <para>};</para>
    /// </example>
    //  [Property("Death Location Address", Property.Types.Dictionary, "Death Investigation", "Location of Death.", true, IGURL.DeathLocation, true, 15)]
    //  [PropertyParam("addressLine1", "address, line one")]
    //  [PropertyParam("addressLine2", "address, line two")]
    //  [PropertyParam("addressCity", "address, city")]
    //  [PropertyParam("addressCounty", "address, county")]
    //  [PropertyParam("addressState", "address, state literal")]
    //  [PropertyParam("addressZip", "address, zip")]
    //  [PropertyParam("addressCountry", "address, country literal")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Location).stream().findAny(type.coding.code='death')", "address")]
    private Map<String, StringType> DeathLocationAddress;

    public Map<String, StringType> getDeathLocationAddress()
    {
        if (DeathLocationLoc != null)
        {
            return addressToMap(DeathLocationLoc.getAddress());
        }
        return EmptyAddrMap();
    }

    public void setDeathLocationAddress(Map<String, StringType> value)
    {
        if (DeathLocationLoc == null)
        {
            CreateDeathLocation();
        }
        DeathLocationLoc.setAddress(mapToAddress(value);
        UpdateDeathRecordIdentifier();
    }

    /// <summary>Name of Death Location.</summary>
    /// <value>the death location name.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DeathLocationName = "Example Death Location Name";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Death Location Name: {ExampleDeathRecord.DeathLocationName}");</para>
    /// </example>
    //  [Property("Death Location Name", Property.Types.String, "Death Investigation", "Name of Death Location.", true, IGURL.DeathLocation, false, 17)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Location).stream().findAny(type.coding.code='death')", "name")]
    private String DeathLocationName;

    public String getDeathLocationName()
    {
        if (DeathLocationLoc != null && DeathLocationLoc.getName() != null && DeathLocationLoc.getName() != BlankPlaceholder)
        {
            return DeathLocationLoc.getName();
        }
        return null;
    }

    public void setDeathLocationName(String value)
    {
        if (DeathLocationLoc == null)
        {
            CreateDeathLocation();
        }
        if (isNullOrWhiteSpace(value))
        {
            DeathLocationLoc.setName(value);
        } else {
            DeathLocationLoc.setName(BlankPlaceholder); // We cannot have a blank String, but the field is required to be present
        }
    }

    /// <summary>Lattitude of Death Location.</summary>
    /// <value>tLattitude of Death Location.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DeathLocationLattitude = "37.88888" ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Death Location Lattitude: {ExampleDeathRecord.DeathLocationLattitude}");</para>
    /// </example>
    //  [Property("Death Location Latitude", Property.Types.String, "Death Investigation", "Death Location Lattitude.", true, IGURL.DeathLocation, false, 17)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Location).stream().findAny(type.coding.code='death')", "position")]
    private String DeathLocationLatitude;

    public String getDeathLocationLatitude()
    {
        if (DeathLocationLoc != null && DeathLocationLoc.getPosition() != null)
        {
            return DeathLocationLoc.getPosition().getLatitude().toString();
        }
        return null;
    }

    public void setDeathLocationLatitude(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (DeathLocationLoc == null)
        {
            CreateDeathLocation();
        }
        if (DeathLocationLoc.getPosition() == null)
        {
            DeathLocationLoc.setPosition(new Location.LocationPositionComponent());
        }
        DeathLocationLoc.getPosition().setLatitude(Double.parseDouble(value));
    }

    /// <summary>Longitude of Death Location.</summary>
    /// <value>Longitude of Death Location.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DeathLocationLongitude = "-50.000" ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Death Location Longitude: {ExampleDeathRecord.DeathLocationLongitude}");</para>
    /// </example>
    //  [Property("Death Location Longitude", Property.Types.String, "Death Investigation", "Death Location Lattitude.", true, IGURL.DeathLocation, false, 17)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Location).stream().findAny(type.coding.code='death')", "position")]
    private String DeathLocationLongitude;

    public String getDeathLocationLongitude()
    {
        if (DeathLocationLoc != null && DeathLocationLoc.getPosition() != null)
        {
            return DeathLocationLoc.getPosition().getLongitude().toString();
        }
        return null;
    }

    public void setDeathLocationLongitude(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (DeathLocationLoc == null)
        {
            CreateDeathLocation();
        }
        if (DeathLocationLoc.getPosition() == null)
        {
            DeathLocationLoc.setPosition(new Location.LocationPositionComponent());
        }
        DeathLocationLoc.getPosition().setLongitude(Double.parseDouble(value));
    }

    /// <summary>Description of Death Location.</summary>
    /// <value>the death location description.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DeathLocationDescription = "Bedford Cemetery";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Death Location Description: {ExampleDeathRecord.DeathLocationDescription}");</para>
    /// </example>
    //  [Property("Death Location Description", Property.Types.String, "Death Investigation", "Description of Death Location.", true, IGURL.DeathLocation, false, 18)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Location).stream().findAny(type.coding.code='death')", "description")]
    private String DeathLocationDescription;

    public String getDeathLocationDescription()
    {
        if (DeathLocationLoc != null)
        {
            return DeathLocationLoc.getDescription();
        }
        return null;
    }

    public void setDeathLocationDescription(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;
        }
        if (DeathLocationLoc == null)
        {
            DeathLocationLoc = new Location();
            DeathLocationLoc.setId(UUID.randomUUID().toString());
            DeathLocationLoc.setMeta(new Meta());
            CanonicalType[] deathlocation_profile = {URL.ProfileURL.DeathLocation};
            DeathLocationLoc.getMeta().setProfile(Arrays.asList(deathlocation_profile));
            DeathLocationLoc.setDescription(value);
            DeathLocationLoc.getType().add(new CodeableConcept(new Coding(CodeSystems.LocationType, "death", "death location")));//, null));
            // LinkObservationToLocation(DeathDateObs, DeathLocationLoc);
            AddReferenceToComposition(DeathLocationLoc.getId(), "DeathInvestigation");
            Resource resource = DeathLocationLoc;
            resource.setId("urn:uuid:" + DeathLocationLoc.getId());
            Bundle.addEntry(new BundleEntryComponent().setResource(resource));
            //Bundle.addResourceEntry(DeathLocationLoc, "urn:uuid:" + DeathLocationLoc.getId());
        }
        else
        {
            DeathLocationLoc.setDescription(value);
        }
    }

    /// <summary>Type of Death Location</summary>
    /// <value>type of death location. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    /// <para>code.add("code", "16983000");</para>
    /// <para>code.add("system", CodeSystems.SCT);</para>
    /// <para>code.add("display", "Death in hospital");</para>
    /// <para>ExampleDeathRecord.DeathLocationType = code;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Death Location Type: {ExampleDeathRecord.DeathLocationType['display']}");</para>
    /// </example>
    //  [Property("Death Location Type", Property.Types.Dictionary, "Death Investigation", "Type of Death Location.", true, IGURL.DeathDate, false, 19)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [PropertyParam("text", "Additional descriptive text.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='81956-5')", "component")]
    private Map<String, String> DeathLocationType;

    public Map<String, String> getDeathLocationType()
    {
        if (DeathDateObs != null && DeathDateObs.getComponent().size() > 0) // if there is a value for death location type, return it
        {
            Observation.ObservationComponentComponent placeComp = DeathDateObs.getComponent().stream().filter(entry -> entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("58332-8")).findFirst().get();
            if (placeComp != null && placeComp.getValue() != null && placeComp.getValue() instanceof CodeableConcept)
            {
                return (CodeableConceptToMap((CodeableConcept) placeComp.getValue()));
            }
        }
        return EmptyCodeableMap();
    }

    public void setDeathLocationType(Map<String, String> value)
    {
        if (isMapEmptyOrDefault(value) && DeathDateObs == null)
        {
            return;
        }
        if (DeathDateObs == null)
        {
            CreateDeathDateObs(); // Create it
        }

        // Find correct component; if doesn't exist add another
        Observation.ObservationComponentComponent placeComp = DeathDateObs.getComponent().stream().filter(entry -> entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("58332-8")).findFirst().get();
        if (placeComp != null)
        {
            ((Observation.ObservationComponentComponent) placeComp).setValue(MapToCodeableConcept(value));
        }
        else
        {
            Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
            component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "58332-8", "Location of death")));//, null);
            component.setValue(MapToCodeableConcept(value));
            DeathDateObs.getComponent().add(component);
        }
    }

    /// <summary>Type of Death Location Helper</summary>
    /// <value>type of death location code.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DeathLocationTypeHelper = ValueSets.PlaceOfDeath.Death_In_Home;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Death Location Type: {ExampleDeathRecord.DeathLocationTypeHelper}");</para>
    /// </example>
    //  [Property("Death Location Type Helper", Property.Types.String, "Death Investigation", "Type of Death Location.", false, IGURL.DeathDate, false, 19)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='81956-5')", "component")]
    private String DeathLocationTypeHelper;

    public String getDeathLocationTypeHelper()
    {
        if (DeathLocationType != null && DeathLocationType.containsKey("code") && isNullOrWhiteSpace(DeathLocationType.get("code")))
        {
            return DeathLocationType.get("code");
        }
        return null;
    }

    public void setDeathLocationTypeHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("DeathLocationType", value, ValueSets.PlaceOfDeath.Codes);
        }
    }

    /// <summary>Age At Death.</summary>
    /// <value>decedent's age at time of death. A Map representing a length of time,
    /// containing the following key/value pairs: </value>
    /// <para>"value" - the quantity value, structured as valueQuantity.getValue()</para>
    /// <para>"code" - the unit a PHIN VADS code set UnitsOfAge, structed as valueQuantity.code
    ///   USE: http://hl7.org/fhir/us/vrdr/STU2/StructureDefinition-vrdr-decedent-age.html </para>
    /// <para>"system" - OPTIONAL: from the example page http://hl7.org/fhir/us/vrdr/Observation-DecedentAge-Example1.json.html</para>
    /// <para>"unit" - OPTIONAL: from the example page http://hl7.org/fhir/us/vrdr/Observation-DecedentAge-Example1.json.html</para>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; age = new Map&lt;String, String&gt;();</para>
    /// <para>age.add("value", "100");</para>
    /// <para>age.add("code", "a"); // e.g. "min" = minutes, "d" = days, "h" = hours, "mo" = months, "a" = years, "UNK" = unknown</para>
    /// <para>age.add("system", "http://unitsofmeasure.org");</para>
    /// <para>age.add("unit", "years");</para>
    /// <para>ExampleDeathRecord.AgeAtDeath = age;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Age At Death: {ExampleDeathRecord.AgeAtDeath['value']} years");</para>
    /// </example>
    //  [Property("Age At Death", Property.Types.Dictionary, "Decedent Demographics", "Age At Death.", true, IGURL.DecedentAge, true, 2)]
    //  [PropertyParam("value", "The quantity value.")]
    //  [PropertyParam("code", "The unit type, from UnitsOfAge ValueSet.")]
    //  [PropertyParam("system", "OPTIONAL: The coding system.")]
    //  [PropertyParam("unit", "OPTIONAL: The unit description.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='39016-1')", "")]
    private Map<String, String> AgeAtDeath;

    public Map<String, String> getAgeAtDeath()
    {
        if (AgeAtDeathObs != null && AgeAtDeathObs.getValue() != null)
        {
            Map<String, String> age = new HashMap<String, String>();
            Quantity quantity = (Quantity) AgeAtDeathObs.getValue();
            age.put("value", quantity.getValue() == null ? "" : quantity.getValue().toString());
            age.put("code", quantity.getCode() == null ? "" : quantity.getCode());
            age.put("system", quantity.getSystem() == null ? "" : quantity.getSystem());
            age.put("unit", quantity.getUnit() == null ? "" : quantity.getUnit());
            return age;
        }
        return new HashMap<String, String>()
        {
            {
                put("value", "");
                put("code", "");
                put("system", null);
                put("unit", null);
            }
        };
    }

    public void setAgeAtDeath(String value)
    {
        String extractedValue = GetValue(value, "value");
        String extractedCode = GetValue(value, "code");
        ;
        String extractedSystem = GetValue(value, "system");
        String extractedUnit = GetValue(value, "unit");
        if ((extractedValue == null && extractedCode == null && extractedUnit == null && extractedSystem == null)) // if there is nothing to do, do nothing.
        {
            return;
        }
        if (AgeAtDeathObs == null)
        {
            CreateAgeAtDeathObs();
        }
        Quantity quantity = (Quantity) AgeAtDeathObs.getValue();

        if (extractedValue != null)
        {
            quantity.setValue(Double.parseDouble(extractedValue));
        }
        if (extractedCode != null)
        {
            quantity.setCode(extractedCode);
        }
        if (extractedSystem != null)
        {
            quantity.setSystem(extractedSystem);
        }
        if (extractedUnit != null)
        {
            quantity.setUnit(extractedUnit);
        }
        AgeAtDeathObs.setValue(quantity);
    }

    /// <summary>Get Age At Death For Code</summary>
    /// <value>Private helper method to get the age at death for a given code.</value>
    private Integer _getAgeAtDeathForCode(String code)
    {
        if (AgeAtDeath.get("code").equals(code) && AgeAtDeath.get("value") != null)
        {
            return Integer.parseInt(AgeAtDeath.get("value"));
        }
        else
        {
            return null;
        }
    }

    /// <summary>Set Age At Death For Code</summary>
    /// <value>Private helper method to set the age at death for a given code and value.</value>
    private void _setAgeAtDeathForCode(String code, Integer value)
    {
        if (value != null)
        {
            AgeAtDeath = new HashMap<String, String>()
            {{
                put("value", String.valueOf(value));
                put("code", code);
            }};
        }
    }

    /// <summary>Age At Death Years Helper</summary>
    /// <value>Set decedent's age at time of death in years.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.AgeAtDeathYears = 100;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Age At Death: {ExampleDeathRecord.AgeAtDeathYears} years");</para>
    /// </example>


    //  [Property("Age At Death Years Helper", Property.Types.Int32, "Decedent Demographics", "Age At Death in Years.", false, IGURL.DecedentAge, true, 2)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='39016-1')", "")]
    public Integer getAgeAtDeathYears()
    {
        return _getAgeAtDeathForCode("a");
    }

    public void setgetAgeAtDeathYears(Integer value)
    {
        _setAgeAtDeathForCode("a", value);
    }

    /// <summary>Age At Death Months Helper</summary>
    /// <value>Set decedent's age at time of death in months.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.AgeAtDeathMonths = 11;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Age At Death: {ExampleDeathRecord.AgeAtDeathMonths} months");</para>
    /// </example>

    //  [Property("Age At Death Months Helper", Property.Types.Int32, "Decedent Demographics", "Age At Death in Months.", false, IGURL.DecedentAge, true, 2)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='39016-1')", "")]
    public Integer getAgeAtDeathMonths()
    {
        return _getAgeAtDeathForCode("mo");
    }

    public void setAgeAtDeathMonths(Integer value)
    {
        _setAgeAtDeathForCode("mo", value);
    }

    /// <summary>Age At Death Days Helper</summary>
    /// <value>Set decedent's age at time of death in days.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.AgeAtDeathDays = 11;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Age At Death: {ExampleDeathRecord.AgeAtDeathDays} days");</para>
    /// </example>

    //  [Property("Age At Death Days Helper", Property.Types.Int32, "Decedent Demographics", "Age At Death in Days.", false, IGURL.DecedentAge, true, 2)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='39016-1')", "")]
    public Integer getAgeAtDeathDays()
    {
        return _getAgeAtDeathForCode("d");
    }

    public void setAgeAtDeathDays(Integer value)
    {
        _setAgeAtDeathForCode("d", value);
    }

    /// <summary>Age At Death Hours Helper</summary>
    /// <value>Set decedent's age at time of death in hours.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.AgeAtDeathHours = 11;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Age At Death: {ExampleDeathRecord.AgeAtDeathHours} hours");</para>
    /// </example>

    //  [Property("Age At Death Hours Helper", Property.Types.Int32, "Decedent Demographics", "Age At Death in Hours.", false, IGURL.DecedentAge, true, 2)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='39016-1')", "")]
    public Integer getAgeAtDeathHours()
    {
        return _getAgeAtDeathForCode("h");
    }

    public void setAgeAtDeathHours(Integer value)
    {
        _setAgeAtDeathForCode("h", value);
    }

    /// <summary>Age At Death Minutes Helper</summary>
    /// <value>Set decedent's age at time of death in minutes.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.AgeAtDeathMinutes = 11;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Age At Death: {ExampleDeathRecord.AgeAtDeathMinutes} minutes");</para>
    /// </example>

    //  [Property("Age At Death Minutes Helper", Property.Types.Int32, "Decedent Demographics", "Age At Death in Minutes.", false, IGURL.DecedentAge, true, 2)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='39016-1')", "")]
    public Integer getAgeAtDeathMinutes()
    {
        return _getAgeAtDeathForCode("min");
    }

    public void setAgeAtDeathMinutes(String value)
    {
        //setAgeAtDeathForCode("min", value);
    }

    /// <summary>Decedent's Age At Death Edit Flag.</summary>
    /// <value>the decedent's age at death edit flag. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; ageEdit = new Map&lt;String, String&gt;();</para>
    /// <para>ageEdit.add("code", "0");</para>
    /// <para>ageEdit.add("system", CodeSystems.BypassEditFlag);</para>
    /// <para>ageEdit.add("display", "Edit Passed");</para>
    /// <para>ExampleDeathRecord.AgeAtDeathEditFlag = ageEdit;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Age At Death Edit Flag: {ExampleDeathRecord.AgeAtDeathEditFlag['display']}");</para>
    /// </example>

    //  [Property("Age At Death Edit Flag", Property.Types.Dictionary, "Decedent Demographics", "Age At Death Edit Flag.", true, IGURL.DecedentAge, true, 2)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [PropertyParam("text", "Additional descriptive text.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='39016-1').getValue().extension", "")]
    private Map<String, String> AgeAtDeathEditFlag;

    public Map<String, String> getAgeAtDeathEditFlag()
    {
        Extension editFlag = AgeAtDeathObs != null ? AgeAtDeathObs.getValue() != null ? AgeAtDeathObs.getValue().getExtension() != null ? AgeAtDeathObs.getValue().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.BypassEditFlag)).findFirst().get() : null : null : null;
        if (editFlag != null && editFlag.getValue() != null && editFlag.getValue() instanceof CodeableConcept)
        {
            return CodeableConceptToMap((CodeableConcept) editFlag.getValue());
        }
        return EmptyCodeableMap();
    }

    public void setAgeAtDeathEditFlag(Map<String, String> value)
    {
        if (isMapEmptyOrDefault(value) && AgeAtDeathObs == null)
        {
            return;
        }
        if (AgeAtDeathObs == null)
        {
            CreateAgeAtDeathObs();
        }
        AgeAtDeathObs.getValue().getExtension().removeIf(ext -> ext.getUrl().equals(URL.ExtensionURL.BypassEditFlag));
        if (isMapEmptyOrDefault(value))
        {
            return;
        }
        Extension editFlag = new Extension(URL.ExtensionURL.BypassEditFlag, MapToCodeableConcept(value));
        AgeAtDeathObs.getValue().getExtension().add(editFlag);
    }

    /// <summary>
    /// Age at Death Edit Bypass Flag Helper
    /// </summary>

    //  [Property("Age At Death Edit Flag Helper", Property.Types.String, "Decedent Demographics", "Age At Death Edit Flag Helper.", false, IGURL.DecedentAge, true, 2)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='39016-1').getValue().extension", "")]
    public String getAgeAtDeathEditFlagHelper()
    {
        return AgeAtDeathEditFlag.containsKey("code") && isNullOrWhiteSpace(AgeAtDeathEditFlag.get("code")) ? AgeAtDeathEditFlag.get("code") : null;
    }

    public void setAgeAtDeathEditFlagHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("AgeAtDeathEditFlag", value, ValueSets.EditBypass01.Codes);
        }
    }


    /// <summary>Pregnancy Status At Death.</summary>
    /// <value>pregnancy status at death. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    /// <para>code.add("code", "1");</para>
    /// <para>code.add("system", CodeSystems.PregnancyStatus);</para>
    /// <para>code.add("display", "Not pregnant within past year");</para>
    /// <para>ExampleDeathRecord.PregnancyObs = code;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Pregnancy Status: {ExampleDeathRecord.PregnancyObs['display']}");</para>
    /// </example>

    //  [Property("Pregnancy Status", Property.Types.Dictionary, "Death Investigation", "Pregnancy Status At Death.", true, IGURL.DecedentPregnancyStatus, true, 33)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='69442-2')", "")]
    private Map<String, String> PregnancyStatus;

    public Map<String, String> getPregnancyStatus()
    {
        if (PregnancyObs != null && PregnancyObs.getValue() != null && PregnancyObs.getValue() instanceof CodeableConcept)
        {
            return CodeableConceptToMap((CodeableConcept) PregnancyObs.getValue());
        }
        return EmptyCodeableMap();
    }

    public void setPregnancyStatus(Map<String, String> value)
    {
        if (DeathCertificateDocumentUtil.isMapEmptyOrDefault(value) && PregnancyObs == null)
        {
            return;
        }
        if (PregnancyObs == null)
        {
            CreatePregnancyObs();
            PregnancyObs.setValue(MapToCodeableConcept(value));
        }
        else
        {
            // Need to keep any existing extension that could be there
            List<Extension> extensions = PregnancyObs.getValue().getExtension().stream().collect(Collectors.toList());
            PregnancyObs.setValue(MapToCodeableConcept(value));
            PregnancyObs.getValue().getExtension().addAll(extensions);
        }
    }

    /// <summary>Pregnancy Status At Death Helper.</summary>
    /// <value>pregnancy status at death.
    /// <para>"code" - the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.PregnancyStatusHelper = ValueSets.PregnancyStatus.Not_Pregnant_Within_Past_Year;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Pregnancy Status: {ExampleDeathRecord.PregnancyStatusHelper}");</para>
    /// </example>

    //  [Property("Pregnancy Status Helper", Property.Types.String, "Death Investigation", "Pregnancy Status At Death.", false, IGURL.DecedentPregnancyStatus, true, 33)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='69442-2')", "")]
    private String PregnancyStatusHelper;

    public String getPregnancyStatusHelper()
    {
        if (PregnancyStatus.containsKey("code") && isNullOrWhiteSpace(PregnancyStatus.get("code")))
        {
            return PregnancyStatus.get("code");
        }
        return null;
    }

    public void setPregnancyStatusHelper(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            SetCodeValue("PregnancyStatus", value, ValueSets.PregnancyStatus.Codes);
        }
    }

/// <summary>Decedent's Pregnancy Status at Death Edit Flag.</summary>
/// <value>the decedent's pregnancy status at death edit flag. A Dictionary representing a code, containing the following key/value pairs:
/// <para>"code" - the code</para>
/// <para>"system" - the code system this code belongs to</para>
/// <para>"display" - a human readable meaning of the code</para>
/// </value>
/// <example>
/// <para>// Setter:</para>
/// <para>Map&lt;string, string&gt; elevel = new HashMap&lt;string, string&gt;();</para>
/// <para>elevel.Add("code", "0");</para>
/// <para>elevel.Add("system", CodeSystems.BypassEditFlag);</para>
/// <para>elevel.Add("display", "Edit Passed");</para>
/// <para>ExampleDeathRecord.PregnancyStatusEditFlag = elevel;</para>
/// <para>// Getter:</para>
/// <para>Console.WriteLine($"Pregnancy Status Edit Flag: {ExampleDeathRecord.PregnancyStatusEditFlag['display']}");</para>
/// </example>
//        [Property("Pregnancy Status Edit Flag",Property.Types.Dictionary,"Decedent Demographics","Decedent's Pregnancy Status at Death Edit Flag.",true,IGURL.DecedentPregnancyStatus,false,34)]
//                [PropertyParam("code","The code used to describe this concept.")]
//                [PropertyParam("system","The relevant code system.")]
//                [PropertyParam("display","The human readable version of this code.")]
//                [FHIRPath("Bundle.entry.resource.where($this is Observation).where(code.coding.code='69442-2')","")]

    private Map<String, String> PregnancyStatusEditFlag;
    public Map<String, String> getPregnancyStatusEditFlag()
    {
        if (PregnancyObs != null && PregnancyObs.getValue() != null && PregnancyObs.getValue().getExtension() != null)
        {
            Extension editFlag = PregnancyObs.getValue().getExtension().stream().filter(extension -> extension.getUrl().equals(URL.ExtensionURL.BypassEditFlag)).findFirst().get();
            if (editFlag != null)
            {
                return CodeableConceptToMap((CodeableConcept) editFlag.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setPregnancyStatusEditFlag(Map<String, String> value)
    {
        if (isMapEmptyOrDefault(value) && PregnancyObs == null)
        {
            return;
        }
        if (PregnancyObs == null)
        {
            CreatePregnancyObs();
        }
        if (PregnancyObs.getValue() != null && PregnancyObs.getValue().getExtension() != null)
        {
            PregnancyObs.getValue().getExtension().removeIf(ext -> ext.getUrl().equals(URL.ExtensionURL.BypassEditFlag));
        }
        if (PregnancyObs.getValue() == null)
        {
            PregnancyObs.setValue(new CodeableConcept());
        }
        Extension editFlag = new Extension(URL.ExtensionURL.BypassEditFlag, MapToCodeableConcept(value));
        PregnancyObs.getValue().getExtension().add(editFlag);
    }


/// <summary>Decedent's Pregnancy Status at Death Edit Flag.</summary>
/// <value>the decedent's pregnancy status at death edit flag. A Map representing a code, containing the following key/value pairs:
/// <para>"code" - the code</para>
/// <para>"system" - the code system this code belongs to</para>
/// <para>"display" - a human readable meaning of the code</para>
/// </value>
/// <example>
/// <para>// Setter:</para>
/// <para>Map&lt;String, String&gt; elevel = new Map&lt;String, String&gt;();</para>
/// <para>elevel.add("code", "0");</para>
/// <para>elevel.add("system", CodeSystems.BypassEditFlag);</para>
/// <para>elevel.add("display", "Edit Passed");</para>
/// <para>ExampleDeathRecord.PregnancyStatusEditFlag = elevel;</para>
/// <para>// Getter:</para>
/// <para>Console.WriteLine($"Pregnancy Status Edit Flag: {ExampleDeathRecord.PregnancyStatusEditFlag['display']}");</para>
/// </example>
    //  [Property("Pregnancy Status Edit Flag", Property.Types.Dictionary, "Decedent Demographics", "Decedent's Pregnancy Status at Death Edit Flag.", true, IGURL.DecedentPregnancyStatus, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='69442-2')", "")]
//private Map<String, String> PregnancyObs;
    public Map<String, String> getPregnancyObs()
    {
        if(PregnancyObs != null && PregnancyObs.getValue() != null && PregnancyObs.getValue().getExtension() != null)
        {
            Extension editFlag=PregnancyObs.getValue().getExtension().stream().filter(extension->extension.getUrl().equals(URL.ExtensionURL.BypassEditFlag)).findFirst().get();
            if(editFlag!= null)
            {
                return CodeableConceptToMap((CodeableConcept)editFlag.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setPregnancyObs(Map<String, String> value)
    {
        if(isMapEmptyOrDefault(value)&&PregnancyObs == null)
        {
            return;
        }
        if(PregnancyObs == null)
        {
            CreatePregnancyObs();
        }
        if(PregnancyObs.getValue() != null && PregnancyObs.getValue().getExtension() != null)
        {
            PregnancyObs.getValue().getExtension().removeIf(ext->ext.getUrl().equals(URL.ExtensionURL.BypassEditFlag));
        }
        if(PregnancyObs.getValue() == null)
        {
            PregnancyObs.setValue(new CodeableConcept());
        }
        Extension editFlag=new Extension(URL.ExtensionURL.BypassEditFlag,MapToCodeableConcept(value));
        PregnancyObs.getValue().getExtension().add(editFlag);
    }

    /// <summary>Decedent's Pregnancy Status Edit Flag Helper</summary>
    /// <value>Decedent's Pregnancy Status Edit Flag.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.DecedentPregnancyStatusEditFlag = ValueSets.EditBypass012.EditPassed;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Pregnancy Status Edit Flag: {ExampleDeathRecord.PregnancyStatusHelperEditFlag}");</para>
    /// </example>
    //  [Property("Pregnancy Status Edit Flag Helper", Property.Types.String, "Decedent Demographics", "Decedent's Pregnancy Status Edit Flag Helper.", false, IGURL.DecedentPregnancyStatus, false, 34)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='69442-2')", "")]
    private String PregnancyStatusEditFlagHelper;

    public String getPregnancyStatusEditFlagHelper()
    {
        if(PregnancyStatusEditFlag.containsKey("code")&&isNullOrWhiteSpace(PregnancyStatusEditFlag.get("code"))){
            return PregnancyStatusEditFlag.get("code");
        }
        return null;
    }

    public void setPregnancyStatusEditFlagHelper(String value)
    {
        if(isNullOrWhiteSpace(value)
        {
            SetCodeValue("PregnancyStatusEditFlag", value, ValueSets.EditBypass012.Codes);
        }
    }


    /// <summary>Examiner Contacted.</summary>
    /// <value>if a medical examiner was contacted.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; ec = new Map&lt;String, String&gt;();</para>
    /// <para>within.add("code", "Y");</para>
    /// <para>within.add("system", CodeSystems.PH_YesNo_HL7_2x);</para>
    /// <para>within.add("display", "Yes");</para>
    /// <para>ExampleDeathRecord.ExaminerContacted = ec;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Examiner Contacted: {ExampleDeathRecord.ExaminerContacted['display']}");</para>
    /// </example>
    //  [Property("Examiner Contacted", Property.Types.Dictionary, "Death Investigation", "Examiner Contacted.", true, IGURL.ExaminerContacted, true, 26)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='74497-9')", "")]
    private Map<String, String> ExaminerContacted;

    public Map<String, String> getExaminerContacted(){
        if(ExaminerContactedObs != null && ExaminerContactedObs.getValue() != null && ExaminerContactedObs.getValue() instanceof CodeableConcept)
        {
            CodeableConcept cc=(CodeableConcept)ExaminerContactedObs.getValue();
            return CodeableConceptToMap(cc);
        }
        return EmptyCodeableMap();
    }

    public void setExaminerContacted(Map<String, String> value)
    {
        if(isMapEmptyOrDefault(value)&&ExaminerContactedObs == null)
        {
            return;
        }
        CodeableConcept contactedCoding=MapToCodeableConcept(value);
        if(ExaminerContactedObs == null)
        {
            ExaminerContactedObs=new Observation();
            ExaminerContactedObs.setId(UUID.randomUUID().toString());
            ExaminerContactedObs.setMeta(new Meta());
            CanonicalType[]ec_profile={URL.ProfileURL.ExaminerContacted};
            ExaminerContactedObs.getMeta().setProfile(Arrays.asList(ec_profile));
            ExaminerContactedObs.setStatus(ObservationStatus.FINAL);
            ExaminerContactedObs.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"74497-9","Medical examiner or coroner was contacted [US Standard Certificate of Death]")));//, null);
            ExaminerContactedObs.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
            ExaminerContactedObs.setValue(contactedCoding);
            AddReferenceToComposition(ExaminerContactedObs.getId(),"DeathInvestigation");
            //Bundle.addResourceEntry(ExaminerContactedObs, "urn:uuid:" + ExaminerContactedObs.getId());
            Resource resource=ExaminerContactedObs;
            resource.setId("urn:uuid:"+ExaminerContactedObs.getId());
            Bundle.addEntry(new BundleEntryComponent().setResource(resource));
        }
        else
        {
            ExaminerContactedObs.setValue(contactedCoding);
        }
    }

    /// <summary>Examiner Contacted Helper. This is a convenience method, to access the code use ExaminerContacted instead.</summary>
    /// <value>if a medical examiner was contacted. A null value indicates "unknown".</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.ExaminerContactedHelper = "N"</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Examiner Contacted: {ExampleDeathRecord.ExaminerContactedHelper}");</para>
    /// </example>
    //  [Property("Examiner Contacted Helper", Property.Types.String, "Death Investigation", "Examiner Contacted.", false, IGURL.ExaminerContacted, true, 27)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='74497-9')", "")]
    private String ExaminerContactedHelper;

    public String getExaminerContactedHelper()
    {
        if(ExaminerContacted.containsKey("code")&&isNullOrWhiteSpace(ExaminerContacted.get("code")))
        {
            return ExaminerContacted.get("code");
        }
        return null;
    }

    public void setExaminerContactedHelper(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
            SetCodeValue("ExaminerContacted", value, ValueSets.YesNoUnknown.Codes);
        }
    }

    /// <summary>Location of Injury.</summary>
    /// <value>location of injury. A Map representing an address, containing the following key/value pairs:
    /// <para>"addressLine1" - address, line one</para>
    /// <para>"addressLine2" - address, line two</para>
    /// <para>"addressCity" - address, city</para>
    /// <para>"addressCounty" - address, county</para>
    /// <para>"addressState" - address, state</para>
    /// <para>"addressZip" - address, zip</para>
    /// <para>"addressCountry" - address, country</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; address = new Map&lt;String, String&gt;();</para>
    /// <para>address.add("addressLine1", "123456 Test Street");</para>
    /// <para>address.add("addressLine2", "Unit 3");</para>
    /// <para>address.add("addressCity", "Boston");</para>
    /// <para>address.add("addressCounty", "Suffolk");</para>
    /// <para>address.add("addressState", "MA");</para>
    /// <para>address.add("addressZip", "12345");</para>
    /// <para>address.add("addressCountry", "US");</para>
    /// <para>ExampleDeathRecord.InjuryLocationAddress = address;</para>
    /// <para>// Getter:</para>
    /// <para>foreach(var pair in ExampleDeathRecord.InjuryLocationAddress)</para>
    /// <para>{</para>
    /// <para>  Console.WriteLine($"\InjuryLocationAddress key: {pair.Key}: value: {pair.getValue()}");</para>
    /// <para>};</para>
    /// </example>
    //  [Property("Injury Location Address", Property.Types.Dictionary, "Death Investigation", "Location of Injury.", true, IGURL.InjuryLocation, true, 34)]
    //  [PropertyParam("addressLine1", "address, line one")]
    //  [PropertyParam("addressLine2", "address, line two")]
    //  [PropertyParam("addressCity", "address, city")]
    //  [PropertyParam("addressCityC", "address, city code")]
    //  [PropertyParam("addressCounty", "address, county")]
    //  [PropertyParam("addressCountyC", "address, county code")]
    //  [PropertyParam("addressState", "address, state")]
    //  [PropertyParam("addressZip", "address, zip")]
    //  [PropertyParam("addressCountry", "address, country")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Location).stream().findAny(type.coding.code='injury')", "address")]
    private Map<String, StringType> InjuryLocationAddress;

    public Map<String, StringType> getInjuryLocationAddress()
    {
        if(InjuryLocationLoc!= null)
        {
            return addressToMap(InjuryLocationLoc.getAddress());
        }
        return EmptyAddrMap();
    }

    public void setInjuryLocationAddress(Map<String, StringType> value)
    {
        if(value == null&&InjuryLocationLoc == null)
        {
            return;
        }
        if(InjuryLocationLoc == null)
        {
            CreateInjuryLocationLoc();
            //LinkObservationToLocation(InjuryIncidentObs, InjuryLocationLoc);
        }
        InjuryLocationLoc.setAddress(mapToAddress(value));
    }

    /// <summary>Lattitude of Injury Location.</summary>
    /// <value>tLattitude of Injury Location.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.InjuryLocationLattitude = "37.88888" ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Injury Location Lattitude: {ExampleDeathRecord.InjuryLocationLattitude}");</para>
    /// </example>

    //  [Property("Injury Location Latitude", Property.Types.String, "Death Investigation", "Injury Location Lattitude.", true, IGURL.InjuryLocation, false, 17)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Location).stream().findAny(type.coding.code='injury')", "position")]
    private String InjuryLocationLatitude;

    public String getInjuryLocationLatitude()
    {
        if(InjuryLocationLoc != null && InjuryLocationLoc.getPosition() != null)
        {
            return InjuryLocationLoc.getPosition().getLatitude().toString();
        }
        return null;
    }

    public void setInjuryLocationLatitude(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
            return;
        }
        if(value == null&&InjuryLocationLoc == null)
        {
            return;
        }
        if(InjuryLocationLoc == null)
        {
            CreateInjuryLocationLoc();
        }
        if(InjuryLocationLoc.getPosition() == null)
        {
            InjuryLocationLoc.setPosition(new Location().getPosition());
        }
        InjuryLocationLoc.getPosition().setLatitude(Double.parseDouble(value));
    }

    /// <summary>Longitude of Injury Location.</summary>
    /// <value>Longitude of Injury Location.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.InjuryLocationLongitude = "-50.000" ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Injury Location Longitude: {ExampleDeathRecord.InjuryLocationLongitude}");</para>
    /// </example>
    //  [Property("Injury Location Longitude", Property.Types.String, "Death Investigation", "Injury Location Lattitude.", true, IGURL.DeathLocation, false, 17)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Location).stream().findAny(type.coding.code='injury')", "position")]
    private String InjuryLocationLongitude;

    public String getInjuryLocationLongitude()
    {
        if(InjuryLocationLoc != null && InjuryLocationLoc.getPosition() != null)
        {
            return(InjuryLocationLoc.getPosition().getLongitude()).toString();
        }
        return null;
    }

    public void setInjuryLocationLongitude(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
            return;
        }
        if(value == null&&InjuryLocationLoc == null)
        {
            return;
        }
        if(InjuryLocationLoc == null)
        {
            CreateInjuryLocationLoc();
        }
        if(InjuryLocationLoc.getPosition() == null)
        {
            InjuryLocationLoc.setPosition(new Location.LocationPositionComponent());
        }
        InjuryLocationLoc.getPosition().setLongitude(Double.parseDouble(value));
    }


    /// <summary>Name of Injury Location.</summary>
    /// <value>the injury location name.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.InjuryLocationName = "Bedford Cemetery";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Injury Location Name: {ExampleDeathRecord.InjuryLocationName}");</para>
    /// </example>
    //  [Property("Injury Location Name", Property.Types.String, "Death Investigation", "Name of Injury Location.", true, IGURL.InjuryLocation, true, 35)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Location).stream().findAny(type.coding.code='injury')", "name")]
    private String InjuryLocationName;

    public String getInjuryLocationName()
    {
        if(InjuryLocationLoc != null && InjuryLocationLoc.getName() != null && InjuryLocationLoc.getName()!=BlankPlaceholder)
        {
            return InjuryLocationLoc.getName();
        }
        return null;
    }

    public void setInjuryLocationName(String value)
    {
        if(isNullOrWhiteSpace(value)&&InjuryLocationLoc == null)
        {
        return;
        }
        if(InjuryLocationLoc == null)
        {
            CreateInjuryLocationLoc();
            // LinkObservationToLocation(InjuryIncidentObs, InjuryLocationLoc);
        }
        if(isNullOrWhiteSpace(value))
        {
            InjuryLocationLoc.setName(value;
        }
        else
        {
            InjuryLocationLoc.setName(BlankPlaceholder); // We cannot have a blank String, but the field is required to be present
        }
    }


    /// <summary>Set an emerging issue value, creating an empty EmergingIssues Observation as needed.</summary>
    private void SetEmergingIssue(String identifier,String value)
    {
        if(isNullOrEmpty(value)&&EmergingIssues == null)
        {
            return;
        }
        if(EmergingIssues == null)
        {
            EmergingIssues=new Observation();
            EmergingIssues.setId(UUID.randomUUID().toString());
            EmergingIssues.setMeta(new Meta());
            CanonicalType[]tb_profile={URL.ProfileURL.EmergingIssues};
            EmergingIssues.getMeta().setProfile(Arrays.asList(tb_profile));
            EmergingIssues.setStatus(ObservationStatus.FINAL);
            EmergingIssues.setCode(new CodeableConcept(new Coding(CodeSystems.ObservationCode,"emergingissues","NCHS-required Parameter Slots for Emerging Issues")));//, null);
            EmergingIssues.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
            AddReferenceToComposition(EmergingIssues.getId(),"DecedentDemographics");
            //Bundle.addResourceEntry(EmergingIssues, "urn:uuid:" + EmergingIssues.getId());
            Resource resource=EmergingIssues;
            resource.setId("urn:uuid:"+EmergingIssues.getId());
            Bundle.addEntry(new BundleEntryComponent().setResource(resource));
        }
        // Remove existing component (if it exists) and add an appropriate component.
        EmergingIssues.getComponent().removeIf(cmp->cmp.getCode() != null && cmp.getCode().getCoding() != null && cmp.getCode().getCoding().size()>0&&cmp.getCode().getCoding().get(0).getCode().equals(identifier));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,identifier,null)));//, null);
        component.setValue(new StringType(value));
        EmergingIssues.getComponent().add(component);
    }

    /// <summary>Get an emerging issue value.</summary>
    private String GetEmergingIssue(String identifier)
    {
        if(EmergingIssues == null)
        {
            return null;
        }
        // Remove existing component (if it exists) and add an appropriate component.
        Observation.ObservationComponentComponent issue=EmergingIssues.getComponent().stream().filter((c->c.getCode().getCoding()[0].Code==identifier);
        if(issue != null && issue.getValue() != null && issue.getValue() instanceof StringType)
        {
            return issue.getValue().toString();
        }
        return null;
    }


    /// <summary>Emerging Issue Field Length 1 Number 1</summary>
    /// <value>the emerging issue value</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.EmergingIssue1_1 = "X";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Emerging Issue Value: {ExampleDeathRecord.EmergingIssue1_1}");</para>
    /// </example>
    //  [Property("Emerging Issue Field Length 1 Number 1", Property.Types.String, "Decedent Demographics", "One-Byte Field 1", true, IGURL.EmergingIssues, false, 50)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='emergingissues')", "")]
    private String EmergingIssue1_1;

    public String getEmergingIssue1_1()
    {
        return GetEmergingIssue("EmergingIssue1_1");
    }

    public void setEmergingIssue1_1(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
            SetEmergingIssue("EmergingIssue1_1",value);
        }
    }

    /// <summary>Emerging Issue Field Length 1 Number 2</summary>
    /// <value>the emerging issue value</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.EmergingIssue1_2 = "X";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Emerging Issue Value: {ExampleDeathRecord.EmergingIssue1_2}");</para>
    /// </example>
    //  [Property("Emerging Issue Field Length 1 Number 2", Property.Types.String, "Decedent Demographics", "1-Byte Field 2", true, IGURL.EmergingIssues, false, 50)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='emergingissues')", "")]
    private String EmergingIssue1_2;

    public String getEmergingIssue1_2()
    {
        return GetEmergingIssue("EmergingIssue1_2");
    }

    public void setEmergingIssue1_2(String value)
    {
        if(isNullOrWhiteSpace(value)){
            SetEmergingIssue("EmergingIssue1_2", value);
        }
    }


    /// <summary>Emerging Issue Field Length 1 Number 3</summary>
    /// <value>the emerging issue value</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.EmergingIssue1_3 = "X";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Emerging Issue Value: {ExampleDeathRecord.EmergingIssue1_3}");</para>
    /// </example>
    //  [Property("Emerging Issue Field Length 1 Number 3", Property.Types.String, "Decedent Demographics", "1-Byte Field 3", true, IGURL.EmergingIssues, false, 50)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='emergingissues')", "")]
    private String EmergingIssue1_3;

    public String getEmergingIssue1_3()
    {
        return GetEmergingIssue("EmergingIssue1_3");
    }

    public void setEmergingIssue1_3(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
            SetEmergingIssue("EmergingIssue1_3", value);
        }
    }

    /// <summary>Emerging Issue Field Length 1 Number 4</summary>
    /// <value>the emerging issue value</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.EmergingIssue1_4 = "X";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Emerging Issue Value: {ExampleDeathRecord.EmergingIssue1_4}");</para>
    /// </example>
    //  [Property("Emerging Issue Field Length 1 Number 4", Property.Types.String, "Decedent Demographics", "1-Byte Field 4", true, IGURL.EmergingIssues, false, 50)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='emergingissues')", "")]
    private String EmergingIssue1_4;

    public String getEmergingIssue1_4()
    {
        return GetEmergingIssue("EmergingIssue1_4");
    }

    public void setEmergingIssue1_4(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
            SetEmergingIssue("EmergingIssue1_4",value);
        }
    }

    /// <summary>Emerging Issue Field Length 1 Number 5</summary>
    /// <value>the emerging issue value</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.EmergingIssue1_5 = "X";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Emerging Issue Value: {ExampleDeathRecord.EmergingIssue1_5}");</para>
    /// </example>
    //  [Property("Emerging Issue Field Length 1 Number 5", Property.Types.String, "Decedent Demographics", "1-Byte Field 5", true, IGURL.EmergingIssues, false, 50)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='emergingissues')", "")]
    private String EmergingIssue1_5;

    public String getEmergingIssue1_5(){
        return GetEmergingIssue("EmergingIssue1_5");
    }

    public void setEmergingIssue1_5(String value){
        if(isNullOrWhiteSpace(value)){
            SetEmergingIssue("EmergingIssue1_5",value);
        }
    }

    /// <summary>Emerging Issue Field Length 1 Number 6</summary>
    /// <value>the emerging issue value</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.EmergingIssue1_6 = "X";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Emerging Issue Value: {ExampleDeathRecord.EmergingIssue1_6}");</para>
    /// </example>
    //  [Property("Emerging Issue Field Length 1 Number 6", Property.Types.String, "Decedent Demographics", "1-Byte Field 6", true, IGURL.EmergingIssues, false, 50)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='emergingissues')", "")]
    private String EmergingIssue1_6;

    public String getEmergingIssue1_6()
    {
            return GetEmergingIssue("EmergingIssue1_6");
    }

    public void setEmergingIssue1_6(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
            SetEmergingIssue("EmergingIssue1_6",value);
        }
    }

    /// <summary>Emerging Issue Field Length 8 Number 1</summary>
    /// <value>the emerging issue value</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.EmergingIssue8_1 = "XXXXXXXX";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Emerging Issue Value: {ExampleDeathRecord.EmergingIssue8_1}");</para>
    /// </example>
    //  [Property("Emerging Issue Field Length 8 Number 1", Property.Types.String, "Decedent Demographics", "8-Byte Field 1", true, IGURL.EmergingIssues, false, 50)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='emergingissues')", "")]
    private String EmergingIssue8_1;

    public String getEmergingIssue8_1(){
        return GetEmergingIssue("EmergingIssue8_1");
    }

    public void setEmergingIssue8_1(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
            SetEmergingIssue("EmergingIssue8_1",value);
        }
    }


    /// <summary>Emerging Issue Field Length 8 Number 2</summary>
    /// <value>the emerging issue value</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.EmergingIssue8_2 = "XXXXXXXX";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Emerging Issue Value: {ExampleDeathRecord.EmergingIssue8_2}");</para>
    /// </example>
    //  [Property("Emerging Issue Field Length 8 Number 2", Property.Types.String, "Decedent Demographics", "8-Byte Field 2", true, IGURL.EmergingIssues, false, 50)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='emergingissues')", "")]
    private String EmergingIssue8_2;

    public String getEmergingIssue8_2()
    {
        return GetEmergingIssue("EmergingIssue8_2");
    }

    public void setEmergingIssue8_2(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
            SetEmergingIssue("EmergingIssue8_2",value);
        }
    }

    /// <summary>Emerging Issue Field Length 8 Number 3</summary>
    /// <value>the emerging issue value</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.EmergingIssue8_3 = "XXXXXXXX";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Emerging Issue Value: {ExampleDeathRecord.EmergingIssue8_3}");</para>
    /// </example>

    //  [Property("Emerging Issue Field Length 8 Number 3", Property.Types.String, "Decedent Demographics", "8-Byte Field 3", true, IGURL.EmergingIssues, false, 50)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='emergingissues')", "")]
    private String EmergingIssue8_3;

    public String getEmergingIssue8_3(){
        return GetEmergingIssue("EmergingIssue8_3");
    }

    public void setEmergingIssue8_3(String value){
        if(isNullOrWhiteSpace(value))
        {
            SetEmergingIssue("EmergingIssue8_3",value);
        }
    }

    /// <summary>Emerging Issue Field Length 20</summary>
    /// <value>the emerging issue value</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.EmergingIssue20 = "XXXXXXXXXXXXXXXXXXXX";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Emerging Issue Value: {ExampleDeathRecord.EmergingIssue20}");</para>
    /// </example>
    //  [Property("Emerging Issue Field Length 20", Property.Types.String, "Decedent Demographics", "20-Byte Field", true, IGURL.EmergingIssues, false, 50)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='emergingissues')", "")]
    private String EmergingIssue20;

    public String getEmergingIssue20()
    {
        return GetEmergingIssue("EmergingIssue20");
    }

    public void setEmergingIssue20(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
            SetEmergingIssue("EmergingIssue20", value);
        }
    }


/// <summary>Decedent's Year of Injury.</summary>
/// <value>the decedent's year of injury, or -1 if explicitly unknown, or null if never specified</value>
/// <example>
/// <para>// Setter:</para>
/// <para>ExampleDeathRecord.InjuryYear = 2018;</para>
/// <para>// Getter:</para>
/// <para>Console.WriteLine($"Decedent Year of Injury: {ExampleDeathRecord.InjuryYear}");</para>
/// </example>

    //  [Property("InjuryYear", Property.Types.Int32, "Death Investigation", "Decedent's Year of Injury.", true, IGURL.InjuryIncident, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='11374-6')", "")]
//		private Integer InjuryYear;
//		public Integer getInjuryYear()
//		{
//			if (InjuryIncidentObs != null && InjuryIncidentObs.getEffective() != null)
//			{
//				return GetDateFragmentOrPartialDate(InjuryIncidentObs.getEffective(), URL.ExtensionURL.DateYear);
//			}
//			return null;
//		}
//		public void setInjuryYear(String value)
//		{
//			if (value == null && InjuryIncidentObs == null)
//			{
//				return;
//			}
//			if (InjuryIncidentObs == null)
//			{
//				CreateInjuryIncidentObs();
//			}
//			SetPartialDate(InjuryIncidentObs.getEffective().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDateTime), URL.ExtensionURL.DateYear, value);
//		}

/// <summary>Decedent's Month of Injury.</summary>
/// <value>the decedent's month of injury, or -1 if explicitly unknown, or null if never specified</value>
/// <example>
/// <para>// Setter:</para>
/// <para>ExampleDeathRecord.InjuryMonth = 7;</para>
/// <para>// Getter:</para>
/// <para>Console.WriteLine($"Decedent Month of Injury: {ExampleDeathRecord.InjuryMonth}");</para>
/// </example>

    //  [Property("InjuryMonth", Property.Types.Int32, "Death Investigation", "Decedent's Month of Injury.", true, IGURL.InjuryIncident, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='11374-6')", "")]
//		private Integer InjuryMonth;
//		public Integer getInjuryMonth()
//		{
//			if (InjuryIncidentObs != null && InjuryIncidentObs.getEffective() != null)
//			{
//				return GetDateFragmentOrPartialDate(InjuryIncidentObs.getEffective(), URL.ExtensionURL.DateMonth);
//			}
//			return null;
//		}
//		public void setInjuryMonth(String value)
//		{
//			if (value == null && InjuryIncidentObs == null)
//			{
//				return;
//			}
//			if (InjuryIncidentObs == null)
//			{
//				CreateInjuryIncidentObs();
//			}
//			SetPartialDate(InjuryIncidentObs.getEffective().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDateTime), URL.ExtensionURL.DateMonth, value);
//		}

/// <summary>Decedent's Day of Injury.</summary>
/// <value>the decedent's day of injury, or -1 if explicitly unknown, or null if never specified</value>
/// <example>
/// <para>// Setter:</para>
/// <para>ExampleDeathRecord.InjuryDay = 22;</para>
/// <para>// Getter:</para>
/// <para>Console.WriteLine($"Decedent Day of Injury: {ExampleDeathRecord.InjuryDay}");</para>
/// </example>

    //  [Property("InjuryDay", Property.Types.Int32, "Death Investigation", "Decedent's Day of Injury.", true, IGURL.InjuryIncident, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='11374-6')", "")]
//		private Integer InjuryDay;
//		public Integer getInjuryDay()
//		{
//			if (InjuryIncidentObs != null && InjuryIncidentObs.getEffective() != null)
//			{
//				return GetDateFragmentOrPartialDate(InjuryIncidentObs.getEffective(), URL.ExtensionURL.DateDay);
//			}
//			return null;
//		}
//		public void setInjuryDay(String value)
//		{
//			if (value == null && InjuryIncidentObs == null)
//			{
//				return;
//			}
//			if (InjuryIncidentObs == null)
//			{
//				CreateInjuryIncidentObs();
//			}
//			SetPartialDate(InjuryIncidentObs.getEffective().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDateTime), URL.ExtensionURL.DateDay, value);
//		}

/// <summary>Decedent's Time of Injury.</summary>
/// <value>the decedent's time of injury, or "-1" if explicitly unknown, or null if never specified</value>
/// <example>
/// <para>// Setter:</para>
/// <para>ExampleDeathRecord.InjuryTime = "07:15";</para>
/// <para>// Getter:</para>
/// <para>Console.WriteLine($"Decedent Time of Injury: {ExampleDeathRecord.InjuryTime}");</para>
/// </example>

    //  [Property("InjuryTime", Property.Types.String, "Death Investigation", "Decedent's Time of Injury.", true, IGURL.InjuryIncident, true, 25)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='11374-6')", "")]
//		private String InjuryTime;
//		public String getInjuryTime()
//		{
//			if (InjuryIncidentObs != null && InjuryIncidentObs.getEffective() != null)
//			{
//				return GetTimeFragmentOrPartialTime(InjuryIncidentObs.getEffective());
//			}
//			return null;
//		}
//		public void setInjuryTime(String value)
//		{
//			if (isNullOrWhiteSpace(value) && InjuryIncidentObs == null)
//			{
//				return;
//			}
//			if (InjuryIncidentObs == null)
//			{
//				CreateInjuryIncidentObs();
//			}
//			SetPartialTime(InjuryIncidentObs.getEffective().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDateTime), value);
//		}

/// <summary>Date/Time of Injury.</summary>
/// <value>the date and time of injury</value>
/// <example>
/// <para>// Setter:</para>
/// <para>ExampleDeathRecord.InjuryDate = "2018-02-19T16:48:06-05:00";</para>
/// <para>// Getter:</para>
/// <para>Console.WriteLine($"Date of Injury: {ExampleDeathRecord.InjuryDate}");</para>
/// </example>

//   	[Property("Injury Date/Time", Property.Types.StringDateTime, "Death Investigation", "Date/Time of Injury.", true, IGURL.InjuryIncident, true, 37)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='11374-6')", "")]
//		private String InjuryDate;
//		public String getInjuryDate()
//		{
//			// We support this legacy API entrypoint via the new partial date and time entrypoints
//			if (InjuryYear != null && InjuryYear != -1 && InjuryMonth != null && InjuryMonth != -1 && InjuryDay != null && InjuryDay != -1 && InjuryTime != null && InjuryTime != "-1")
//			{
//				OffsetDateTime parsedTime = OffsetDateTime.parse(InjuryTime);
//				if (parsedTime != null)
//				{
//					OffsetDateTime result = new OffsetDateTime((int)InjuryYear, (int)InjuryMonth, (int)InjuryDay, parsedTime.getHour(), parsedTime.getMinute(), parsedTime.getSecond(), TimeSpan.Zero);
//					return result.toString("s");
//				}
//			}
//			else if (InjuryYear != null && InjuryYear != -1 && InjuryMonth != null && InjuryMonth != -1 && InjuryDay != null && InjuryDay != -1)
//			{
//				DateTimeType result = new Date((int)InjuryYear, (int)InjuryMonth, (int)InjuryDay);
//				return result.toString("s");
//			}
//			return null;
//		}
//		public void setInjuryDate(String value)
//		{
//			// We support this legacy API entrypoint via the new partial date and time entrypoints
//			OffsetDateTime parsedTime = OffsetDateTime.parse(InjuryTime);
//			if (parsedTime != null)
//			{
//				InjuryYear = parsedTime.getYear();
//				InjuryMonth = parsedTime.getMonthValue();
//				InjuryDay = parsedTime.getDayOfMonth();
//			//	TimeSpan timeSpan = new TimeSpan(0, parsedTime.Hour, parsedTime.Minute, parsedTime.Second);
//			//	InjuryTime = timeSpan.toString(@"hh\:mm\:ss");
//			}
//		}

    /// <summary>Description of Injury.</summary>
    /// <value>the description of the injury</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.InjuryDescription = "drug toxicity";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Injury Description: {ExampleDeathRecord.InjuryDescription}");</para>
    /// </example>

    //  [Property("Injury Description", Property.Types.String, "Death Investigation", "Description of Injury.", true, IGURL.InjuryIncident, true, 38)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='11374-6')", "")]
    private String InjuryDescription;

    public String getInjuryDescription()
    {
        CodeableConcept concept=InjuryIncidentObs != null ? (CodeableConcept)InjuryIncidentObs.getValue():null;
        if(concept != null)
        {
            return concept.getText();
        }
        return null;
    }

    public void setInjuryDescription(String value){
        if(isNullOrWhiteSpace(value)&&InjuryIncidentObs == null)
        {
            return;
        }
        if(InjuryIncidentObs == null)
        {
            CreateInjuryIncidentObs();
        }
        InjuryIncidentObs.setValue(new CodeableConcept(new Coding(null,null,null)));//, value);
    }

    /// <summary>Place of Injury Description.</summary>
    /// <value>the place of injury.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.InjuryPlaceDescription = "At home, in the kitchen";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Place of Injury Description: {ExampleDeathRecord.InjuryPlaceDescription}");</para>
    /// </example>

    //  [Property("Injury Place Description", Property.Types.String, "Death Investigation", "Place of Injury.", true, IGURL.InjuryIncident, true, 40)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='11374-6')", "")]
    private String InjuryPlaceDescription;

    public String getInjuryPlaceDescription()
    {
        // Find the component
        if(InjuryIncidentObs != null && InjuryIncidentObs.getComponent().size()>0)
        {
            // Find correct component
            Observation.ObservationComponentComponent placeComp=InjuryIncidentObs.getComponent().stream().filter(entry->entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("69450-5")).findFirst().get();
            if(placeComp != null && placeComp.getValue() != null && placeComp.getValue() instanceof CodeableConcept)
            {
                Map<String, String> map=CodeableConceptToMap((CodeableConcept)placeComp.getValue());
                if(map.containsKey("text"))
                {
                    return(map.get("text"));
                }
            }
        }
        return null;
    }

    public void setInjuryPlaceDescription(String value)
    {
        if(isNullOrWhiteSpace(value)&&InjuryIncidentObs == null)
        {
            return;
        }
        if(InjuryIncidentObs == null)
        {
            CreateInjuryIncidentObs();
        }
        // Find correct component; if doesn't exist add another
        Observation.ObservationComponentComponent placeComp=InjuryIncidentObs.getComponent().stream().filter(entry->entry.getCode() != null &&entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("69450-5")).findFirst().get();
        if(placeComp!= null)
        {
            ((Observation.ObservationComponentComponent)placeComp).setValue(new CodeableConcept(new Coding(null,null,null)));//, value);
        }
        else
        {
            Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
            component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"69450-5","Place of injury Facility")));//, null);
            component.setValue(new CodeableConcept(new Coding(null,null,null)));//))), value);
            InjuryIncidentObs.getComponent().add(component);
        }
    }

    /// <summary>Injury At Work?</summary>
    /// <value>did the injury occur at work? A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    /// <para>code.add("code", "N");</para>
    /// <para>code.add("system", CodeSystems.YesNo);</para>
    /// <para>code.add("display", "No");</para>
    /// <para>ExampleDeathRecord.InjuryAtWork = code;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Injury At Work?: {ExampleDeathRecord.InjuryAtWork['display']}");</para>
    /// </example>
    //  [Property("Injury At Work?", Property.Types.Dictionary, "Death Investigation", "Did the injury occur at work?", true, IGURL.InjuryIncident, true, 41)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='11374-6')", "")]
    private Map<String, String> InjuryAtWork;

    public Map<String, String> getInjuryAtWork(){
            if(InjuryIncidentObs != null && InjuryIncidentObs.getComponent().size()>0){
            // Find correct component
            Observation.ObservationComponentComponent placeComp=InjuryIncidentObs.getComponent().stream().filter((entry->entry.getCode() != null
            &&entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("69444-8");
            if(placeComp != null && placeComp.getValue() != null && placeComp.getValue() instanceof CodeableConcept){
            return CodeableConceptToMap((CodeableConcept)placeComp.getValue());
            }
            }
            return EmptyCodeableMap();
            }

    public void setInjuryAtWork(Map<String, String> value)
    {
        if(isMapEmptyOrDefault(value)&&InjuryIncidentObs == null)
        {
            return;
        }
        if(InjuryIncidentObs == null)
        {
            CreateInjuryIncidentObs();
        }

        // Find correct component; if doesn't exist add another
        Observation.ObservationComponentComponent placeComp=InjuryIncidentObs.getComponent().stream().filter(entry->entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("69444-8")).findFirst().get();
        if(placeComp!= null)
        {
            ((Observation.ObservationComponentComponent)placeComp).setValue(MapToCodeableConcept(value));
        }
        else
        {
            Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
            component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"69444-8","Did death result from injury at work")));//, null);
            component.setValue(MapToCodeableConcept(value));
            InjuryIncidentObs.getComponent().add(component);
        }
    }

    /// <summary>Injury At Work Helper This is a convenience method, to access the code use the InjuryAtWork property instead.</summary>
    /// <value>did the injury occur at work? A null value indicates "not applicable".</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.InjuryAtWorkHelper = "Y"";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Injury At Work? : {ExampleDeathRecord.InjuryAtWorkHelper}");</para>
    /// </example>

    //     	[Property("Injury At Work Helper", Property.Types.String, "Death Investigation", "Did the injury occur at work?", false, IGURL.InjuryIncident, true, 42)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='11374-6')", "")]
    private String InjuryAtWorkHelper;

    public String getInjuryAtWorkHelper()
    {
        if(InjuryAtWork.containsKey("code")&&isNullOrWhiteSpace(InjuryAtWork.get("code")))
        {
            return InjuryAtWork.get("code");
        }
        return null;
    }

    public void setInjuryAtWorkHelper(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
            SetCodeValue("InjuryAtWork", value, ValueSets.YesNoUnknownNotApplicable.Codes);
        }
    }

    /// <summary>Transportation Role in death.</summary>
    /// <value>transportation role in death. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    /// <para>code.add("code", "257500003");</para>
    /// <para>code.add("system", CodeSystems.SCT);</para>
    /// <para>code.add("display", "Passenger");</para>
    /// <para>ExampleDeathRecord.TransportationRole = code;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Transportation Role: {ExampleDeathRecord.TransportationRole['display']}");</para>
    /// </example>

    //  [Property("Transportation Role", Property.Types.Dictionary, "Death Investigation", "Transportation Role in death.", true, IGURL.InjuryIncident, true, 45)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='11374-6')", "")]  // The component  code is '69451-3'
    private Map<String, String> TransportationRole;

    public Map<String, String> getTransportationRole()
    {
        if(InjuryIncidentObs != null && InjuryIncidentObs.getComponent().size()>0)
        {
        // Find correct component
        Observation.ObservationComponentComponent transportComp=InjuryIncidentObs.getComponent().stream().filter(entry->entry.getCode() != null && entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("69451-3")).findFirst().get();
            if(transportComp != null && transportComp.getValue() != null && transportComp.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)transportComp.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setTransportationRole(Map<String, String> value)
    {
        if(isMapEmptyOrDefault(value)&&InjuryIncidentObs == null)
        {
            return;
        }
        if(InjuryIncidentObs == null)
        {
            CreateInjuryIncidentObs();
        }
        // Find correct component; if doesn't exist add another
        Observation.ObservationComponentComponent transportComp=InjuryIncidentObs.getComponent().stream().filter(entry->entry.getCode() != null &&
        entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("69451-3")).findFirst().get();
        if(transportComp!= null)
        {
            ((Observation.ObservationComponentComponent)transportComp).setValue(MapToCodeableConcept(value));
        }
        else
        {
            Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
            component.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"69451-3","Transportation role of decedent")));//, null);
            component.setValue(MapToCodeableConcept(value));
            InjuryIncidentObs.getComponent().add(component);
        }
    }

    /// <summary>Transportation Role in death helper.</summary>
    /// <value>transportation code for role in death.
    /// <para>"code" - the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.TransportationRoleHelper = VRDR.TransportationRoles.Passenger;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Transportation Role: {ExampleDeathRecord.TransportationRoleHelper");</para>
    /// </example>

    //  [Property("Transportation Role Helper", Property.Types.String, "Death Investigation", "Transportation Role in death.", false, IGURL.InjuryIncident, true, 45)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='11374-6')", "")]  // The component  code is '69451-3'
    private String getTransportationRoleHelper;

    public String getTransportationRoleHelper()
    {
        if(TransportationRole.containsKey("code"))
        {
            String code=TransportationRole.get("code");
            if(code=="OTH")
            {
                if(TransportationRole.containsKey("text"))
                {
                    return(TransportationRole.get("text"));
                }
                return("Other");
            }
            else if(isNullOrWhiteSpace(code))
            {
                return code;
            }
        }
        return null;
    }

    public void setTransportationRoleHelper(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
        // do nothing
            return;
        }
        if(InjuryIncidentObs == null)
        {
            CreateInjuryIncidentObs();
        }
        if(!Mappings.TransportationIncidentRole.FHIRToIJE.containsKey(value))
        { //other
        //Find the component, or create it
            Observation.ObservationComponentComponent transportComp=InjuryIncidentObs.getComponent().stream().filter(entry->entry.getCode() != null &&
            entry.getCode().getCoding().get(0) != null && entry.getCode().getCoding().get(0).getCode().equals("69451-3")).findFirst().get();
            if(transportComp == null)
            {
                transportComp=new Observation.ObservationComponentComponent();
                transportComp.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"69451-3","Transportation role of decedent"))); ////null
                InjuryIncidentObs.getComponent().add(transportComp);
            }
            transportComp.setValue(new CodeableConcept(new Coding(CodeSystems.NullFlavor_HL7_V3,"OTH","Other"))); ////value
        }
        else
        { // normal path
            SetCodeValue("TransportationRole", value, ValueSets.TransportationIncidentRole.Codes);
        }
    }

    /// <summary>Tobacco Use Contributed To Death.</summary>
    /// <value>if tobacco use contributed to death. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; code = new Map&lt;String, String&gt;();</para>
    /// <para>code.add("code", "373066001");</para>
    /// <para>code.add("system", CodeSystems.SCT);</para>
    /// <para>code.add("display", "Yes");</para>
    /// <para>ExampleDeathRecord.TobaccoUse = code;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Tobacco Use: {ExampleDeathRecord.TobaccoUse['display']}");</para>
    /// </example>

    //  [Property("Tobacco Use", Property.Types.Dictionary, "Death Investigation", "If Tobacco Use Contributed To Death.", true, IGURL.TobaccoUseContributedToDeath, true, 32)]
    //  [PropertyParam("code", "The code used to describe this concept.")]
    //  [PropertyParam("system", "The relevant code system.")]
    //  [PropertyParam("display", "The human readable version of this code.")]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='69443-0')", "")]
    private Map<String, String> TobaccoUse;

    public Map<String, String> getTobaccoUse()
    {
        if(TobaccoUseObs != null && TobaccoUseObs.getValue() != null && TobaccoUseObs.getValue() instanceof CodeableConcept)
        {
            return CodeableConceptToMap((CodeableConcept)TobaccoUseObs.getValue());
        }
        return EmptyCodeableMap();
    }

    public void setTobaccoUse(Map<String, String> value)
    {
        if(TobaccoUseObs == null)
        {
            TobaccoUseObs=new Observation();
            TobaccoUseObs.setId(UUID.randomUUID().toString());
            TobaccoUseObs.setMeta(new Meta());
            CanonicalType[]tb_profile={URL.ProfileURL.TobaccoUseContributedToDeath};
            TobaccoUseObs.getMeta().setProfile(Arrays.asList(tb_profile));
            TobaccoUseObs.setStatus(ObservationStatus.FINAL);
            TobaccoUseObs.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"69443-0","Did tobacco use contribute to death")));//, null);
            TobaccoUseObs.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
            TobaccoUseObs.setValue(MapToCodeableConcept(value));
            AddReferenceToComposition(TobaccoUseObs.getId(),"DeathInvestigation");
            //Bundle.addResourceEntry(TobaccoUseObs, "urn:uuid:" + TobaccoUseObs.getId());
            Resource resource=TobaccoUseObs;
            resource.setId("urn:uuid:"+TobaccoUseObs.getId());
            Bundle.addEntry(new BundleEntryComponent().setResource(resource));
        }
        else
        {
            TobaccoUseObs.setValue(MapToCodeableConcept(value));
        }
    }

    /// <summary>Tobacco Use Helper. This is a convenience method, to access the code use TobaccoUse instead.</summary>
    /// <value>From a value set..</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.TobaccoUseHelper = "N";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Tobacco Use: {ExampleDeathRecord.TobaccoUseHelper}");</para>
    /// </example>

    //  [Property("Tobacco Use Helper", Property.Types.String, "Death Investigation", "Tobacco Use.", false, IGURL.TobaccoUseContributedToDeath, true, 27)]
    //  [FHIRPath("Bundle.getEntry().getResource().stream().findAny($this is Observation).stream().findAny(code.coding.code='69443-0')", "")]
    private String TobaccoUseHelper;

    public String getTobaccoUseHelper()
    {
        if(TobaccoUse.containsKey("code")&&isNullOrWhiteSpace(TobaccoUse.get("code")))
        {
            return TobaccoUse.get("code");
        }
        return null;
    }

    public void setTobaccoUseHelper(String value)
    {
        if(isNullOrWhiteSpace(value)){
            SetCodeValue("TobaccoUse", value, ValueSets.ContributoryTobaccoUse.Codes);
        }
    }


    //============


    /// <summary> Create Input Race and Ethnicity </summary>

    private void CreateInputRaceEthnicityObs()
    {
        InputRaceAndEthnicityObs=new Observation();
        InputRaceAndEthnicityObs.setId(UUID.randomUUID().toString());
        InputRaceAndEthnicityObs.setMeta(new Meta());
        CanonicalType[]raceethnicity_profile={URL.ProfileURL.InputRaceAndEthnicity};
        InputRaceAndEthnicityObs.getMeta().setProfile(Arrays.asList(raceethnicity_profile));
        InputRaceAndEthnicityObs.setStatus(ObservationStatus.FINAL);
        InputRaceAndEthnicityObs.setCode(new CodeableConcept(new Coding(CodeSystems.ObservationCode,"inputraceandethnicity","Input Race and Ethnicity")));//, null);
        InputRaceAndEthnicityObs.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        AddReferenceToComposition(InputRaceAndEthnicityObs.getId(),"DecedentDemographics");
        //Bundle.addResourceEntry(InputRaceAndEthnicityObs, "urn:uuid:" + InputRaceAndEthnicityObs.getId());
        Resource resource=InputRaceAndEthnicityObs;
        resource.setId("urn:uuid:"+InputRaceAndEthnicityObs.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }


    /// <summary>Create Certifier.</summary>
    private void CreateCertifier()
    {
        Certifier=new Practitioner();
        Certifier.setId(UUID.randomUUID().toString());
        Certifier.setMeta(new Meta());
        CanonicalType[]certifier_profile={URL.ProfileURL.Certifier};
        Certifier.getMeta().setProfile(Arrays.asList(certifier_profile));
        // Not linked to Composition or inserted in bundle, since this is run before the composition exists.
    }

    /// <summary>The Certification.</summary>
    private Procedure DeathCertification;

    /// <summary>Create Death Certification.</summary>
    private void CreateDeathCertification()
    {
        DeathCertification=new Procedure();
        DeathCertification.setId(UUID.randomUUID().toString());
        DeathCertification.setSubject(new Reference("urn:uuid:"+Decedent.getId());
        DeathCertification.setMeta(new Meta());
        CanonicalType[]deathcertification_profile={URL.ProfileURL.DeathCertification};
        DeathCertification.getMeta().setProfile(Arrays.asList(deathcertification_profile));
        DeathCertification.setStatus(Procedure.ProcedureStatus.COMPLETED);
        DeathCertification.setCategory(new CodeableConcept(new Coding(CodeSystems.SCT,"103693007","Diagnostic procedure")));//, null));
        DeathCertification.setCode(new CodeableConcept(new Coding(CodeSystems.SCT,"308646001","Death certification")));//, null);
        // Not linked to Composition or inserted in bundle, since this is run before the composition exists.
    }


    /// <summary>Create a Cause Of Death Condition.   Used to create CauseOfDeathConditionA-D </summary>
    private Observation CauseOfDeathCondition(int index)
    {
        Observation CodCondition;
        CodCondition=new Observation();
        CodCondition.setId(UUID.randomUUID().toString());
        CodCondition.setMeta(new Meta());
        CanonicalType[]condition_profile={URL.ProfileURL.CauseOfDeathPart1};
        CodCondition.getMeta().setProfile(Arrays.asList(condition_profile));
        CodCondition.setStatus(ObservationStatus.FINAL);
        CodCondition.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"69453-9","Cause of death [US Standard Certificate of Death]")));//, null);
        CodCondition.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        CodCondition.getPerformer().add(new Reference("urn:uuid:"+Certifier.getId()));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"lineNumber","line number")));//, null);
        component.setValue(new IntegerType(index+1)); // index is 0-3, linenumbers are 1-4
        CodCondition.getComponent().add(component);
        AddReferenceToComposition(CodCondition.getId(),"DeathCertification");
        //Bundle.addResourceEntry(CodCondition, "urn:uuid:" + CodCondition.getId());
        Resource resource=CodCondition;
        resource.setId("urn:uuid:"+CodCondition.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
        return(CodCondition);
    }


    /// <summary>Decedent's Father.</summary>
    private RelatedPerson Father;

    /// <summary>Create Spouse.</summary>
    private void CreateFather()
    {
        Father=new RelatedPerson();
        Father.setId(UUID.randomUUID().toString());
        Father.setMeta(new Meta());
        CanonicalType[]father_profile={URL.ProfileURL.DecedentFather};
        Father.getMeta().setProfile(Arrays.asList(father_profile));
        Father.setPatient(new Reference("urn:uuid:"+Decedent.getId()));
        Father.setActive(true); // USCore RelatedPerson requires active = true
        Father.getRelationship().add(new CodeableConcept(new Coding(CodeSystems.RoleCode_HL7_V3,"FTH","father")));//, null));
        AddReferenceToComposition(Father.getId(),"DecedentDemographics");
        //Bundle.addResourceEntry(Father, "urn:uuid:" + Father.getId());
        Resource resource=Father;
        resource.setId("urn:uuid:"+Father.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Decedent's Mother.</summary>
    private RelatedPerson Mother;

    /// <summary>Create Mother.</summary>
    private void CreateMother()
    {
        Mother=new RelatedPerson();
        Mother.setId(UUID.randomUUID().toString());
        Mother.setMeta(new Meta());
        CanonicalType[]mother_profile={URL.ProfileURL.DecedentMother};
        Mother.getMeta().setProfile(Arrays.asList(mother_profile));
        Mother.setPatient(new Reference("urn:uuid:"+Decedent.getId()));
        Mother.setActive(true); // USCore RelatedPerson requires active = true
        Mother.getRelationship().add(new CodeableConcept(new Coding(CodeSystems.RoleCode_HL7_V3,"MTH","mother")));//, null));
        AddReferenceToComposition(Mother.getId(),"DecedentDemographics");
        //Bundle.addResourceEntry(Mother, "urn:uuid:" + Mother.getId());
        Resource resource=Mother;
        resource.setId("urn:uuid:"+Mother.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Decedent's Spouse.</summary>
    private RelatedPerson Spouse;

    /// <summary>Create Spouse.</summary>
    private void CreateSpouse()
    {
        Spouse=new RelatedPerson();
        Spouse.setId(UUID.randomUUID().toString());
        Spouse.setMeta(new Meta());
        CanonicalType[]spouse_profile={URL.ProfileURL.DecedentSpouse};
        Spouse.getMeta().setProfile(Arrays.asList(spouse_profile));
        Spouse.setPatient(new Reference("urn:uuid:"+Decedent.getId()));
        Spouse.setActive(true); // USCore RelatedPerson requires active = true
        Spouse.getRelationship().add(new CodeableConcept(new Coding(CodeSystems.RoleCode_HL7_V3,"SPS","spouse")));//, null));
        AddReferenceToComposition(Spouse.getId(),"DecedentDemographics");
        //Bundle.addResourceEntry(Spouse, "urn:uuid:" + Spouse.getId());
        Resource resource=Spouse;
        resource.setId("urn:uuid:"+Spouse.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Decedent Education Level.</summary>
    private Observation DecedentEducationLevel;

    /// <summary>Create an empty EducationLevel Observation, to be populated in either EducationLevel or EducationLevelEditFlag.</summary>
    private void CreateEducationLevelObs()
    {
        DecedentEducationLevel=new Observation();
        DecedentEducationLevel.setId(UUID.randomUUID().toString());
        DecedentEducationLevel.setMeta(new Meta());
        CanonicalType[]educationlevel_profile={URL.ProfileURL.DecedentEducationLevel};
        DecedentEducationLevel.getMeta().setProfile(Arrays.asList(educationlevel_profile));
        DecedentEducationLevel.setStatus(ObservationStatus.FINAL);
        DecedentEducationLevel.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"80913-7","Highest level of education [US Standard Certificate of Death]")));//, null);
        DecedentEducationLevel.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        AddReferenceToComposition(DecedentEducationLevel.getId(),"DecedentDemographics");
        //Bundle.addResourceEntry(DecedentEducationLevel, "urn:uuid:" + DecedentEducationLevel.getId());
        Resource resource=DecedentEducationLevel;
        resource.setId("urn:uuid:"+DecedentEducationLevel.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Birth Record Identifier.</summary>
    private Observation BirthRecordIdentifier;

    /// <summary>Create an empty BirthRecordIdentifier Observation.</summary>
    private void CreateBirthRecordIdentifier()
    {
        BirthRecordIdentifier=new Observation();
        BirthRecordIdentifier.setId(UUID.randomUUID().toString());
        BirthRecordIdentifier.setMeta(new Meta());
        CanonicalType[]br_profile={URL.ProfileURL.BirthRecordIdentifier};
        BirthRecordIdentifier.getMeta().setProfile(Arrays.asList(br_profile));
        BirthRecordIdentifier.setStatus(ObservationStatus.FINAL);
        BirthRecordIdentifier.setCode(new CodeableConcept(new Coding(CodeSystems.HL7_identifier_type,"BR","Birth registry number")));//, null);
        BirthRecordIdentifier.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        BirthRecordIdentifier.setValue(null);
        BirthRecordIdentifier.setDataAbsentReason(new CodeableConcept(new Coding(CodeSystems.Data_Absent_Reason_HL7_V3,"unknown","Unknown")));//, null);

        AddReferenceToComposition(BirthRecordIdentifier.getId(),"DecedentDemographics");
        //Bundle.addResourceEntry(BirthRecordIdentifier, "urn:uuid:" + BirthRecordIdentifier.getId());
        Resource resource=BirthRecordIdentifier;
        resource.setId("urn:uuid:"+BirthRecordIdentifier.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }


    /// <summary>Emerging Issues.</summary>
    protected Observation EmergingIssues;

    /// <summary> Coding Status </summary>
    private Parameters CodingStatusValues;

    /// <summary>Create an empty Coding Status Value Parameters.</summary>
    private void CreateCodingStatusValues()
    {
        CodingStatusValues=new Parameters();
        CodingStatusValues.setId(UUID.randomUUID().toString());
        CodingStatusValues.setMeta(new Meta());
        CanonicalType[]profile={URL.ProfileURL.CodingStatusValues};
        CodingStatusValues.getMeta().setProfile(Arrays.asList(profile));
        DateType date=new DateType();
        date.getExtension().add(NewBlankPartialDateTimeExtension(false));
        CodingStatusValues.addParameter("receiptDate",date);
        AddReferenceToComposition(CodingStatusValues.getId(),"CodedContent");
        //Bundle.addResourceEntry(CodingStatusValues, "urn:uuid:" + CodingStatusValues.getId());
        Resource resource=CodingStatusValues;
        resource.setId("urn:uuid:"+CodingStatusValues.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Usual Work.</summary>
    private Observation UsualWork;

    /// <summary>Create Usual Work.</summary>
    private void CreateUsualWork()
    {
        UsualWork=new Observation();
        UsualWork.setId(UUID.randomUUID().toString());
        UsualWork.setMeta(new Meta());
        CanonicalType[]usualwork_profile={URL.ProfileURL.DecedentUsualWork};
        UsualWork.getMeta().setProfile(Arrays.asList(usualwork_profile));
        UsualWork.setStatus(ObservationStatus.FINAL);
        UsualWork.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"21843-8","History of Usual occupation")));//, null);
        UsualWork.getCategory().add(new CodeableConcept(new Coding(CodeSystems.ObservationCategory,"social-history",null)));//, null));
        UsualWork.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        UsualWork.setEffective(new Period());
        AddReferenceToComposition(UsualWork.getId(),"DecedentDemographics");
        //Bundle.addResourceEntry(UsualWork, "urn:uuid:" + UsualWork.getId());
        Resource resource=UsualWork;
        resource.setId("urn:uuid:"+UsualWork.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Whether the decedent served in the military</summary>
    private Observation MilitaryServiceObs;

    /// <summary>The Funeral Home.</summary>
    private Organization FuneralHome;

    /// <summary>Create Funeral Home.</summary>
    private void CreateFuneralHome()
    {
        FuneralHome=new Organization();
        FuneralHome.setId(UUID.randomUUID().toString());
        FuneralHome.setMeta(new Meta());
        CanonicalType[]funeralhome_profile={URL.ProfileURL.FuneralHome};
        FuneralHome.getMeta().setProfile(Arrays.asList(funeralhome_profile));
        FuneralHome.getType().add(new CodeableConcept(new Coding(CodeSystems.OrganizationType,"funeralhome","Funeral Home")));//, null));
        FuneralHome.setActive(true);
        AddReferenceToComposition(FuneralHome.getId(),"DecedentDisposition");
        //Bundle.addResourceEntry(FuneralHome, "urn:uuid:" + FuneralHome.getId());
        Resource resource=FuneralHome;
        resource.setId("urn:uuid:"+FuneralHome.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Disposition Location.</summary>
    private Location DispositionLocation;

    /// <summary>Create Disposition Location.</summary>
    private void CreateDispositionLocation()
    {
        DispositionLocation=new Location();
        DispositionLocation.setId(UUID.randomUUID().toString());
        DispositionLocation.setMeta(new Meta());
        CanonicalType[]dispositionlocation_profile={URL.ProfileURL.DispositionLocation};
        DispositionLocation.getMeta().setProfile(Arrays.asList(dispositionlocation_profile));
        DispositionLocation.setName(BlankPlaceholder); // We cannot have a blank String, but the field is required to be present
        Coding pt=new Coding(CodeSystems.HL7_location_physical_type,"si","Site");
        DispositionLocation.setPhysicalType(new CodeableConcept());
        DispositionLocation.getPhysicalType().getCoding().add(pt);
        DispositionLocation.getType().add(new CodeableConcept(new Coding(CodeSystems.LocationType,"disposition","disposition location")));//, null));
        AddReferenceToComposition(DispositionLocation.getId(),"DecedentDisposition");
        //Bundle.addResourceEntry(DispositionLocation, "urn:uuid:" + DispositionLocation.getId());
        Resource resource=DispositionLocation;
        resource.setId("urn:uuid:"+DispositionLocation.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Disposition Method.</summary>
    private Observation DispositionMethod;

    /// <summary>Autopsy Performed.</summary>
    private Observation AutopsyPerformed;

    /// <summary>Create Autopsy Performed </summary>
    private void CreateAutopsyPerformed()
    {
        AutopsyPerformed=new Observation();
        AutopsyPerformed.setId(UUID.randomUUID().toString());
        AutopsyPerformed.setMeta(new Meta());
        CanonicalType[]autopsyperformed_profile={URL.ProfileURL.AutopsyPerformedIndicator};
        AutopsyPerformed.getMeta().setProfile(Arrays.asList(autopsyperformed_profile));
        AutopsyPerformed.setStatus(ObservationStatus.FINAL);
        AutopsyPerformed.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"85699-7","Autopsy was performed")));//, null);
        AutopsyPerformed.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        AddReferenceToComposition(AutopsyPerformed.getId(),"DeathInvestigation");
        //Bundle.addResourceEntry(AutopsyPerformed, "urn:uuid:" + AutopsyPerformed.getId());
        Resource resource=AutopsyPerformed;
        resource.setId("urn:uuid:"+AutopsyPerformed.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Age At Death.</summary>
    private Observation AgeAtDeathObs;

    /// <summary>Create Age At Death Obs</summary>
    private void CreateAgeAtDeathObs()
    {
        AgeAtDeathObs=new Observation();
        AgeAtDeathObs.setId(UUID.randomUUID().toString());
        AgeAtDeathObs.setMeta(new Meta());
        CanonicalType[]age_profile={URL.ProfileURL.DecedentAge};
        AgeAtDeathObs.getMeta().setProfile(Arrays.asList(age_profile));
        AgeAtDeathObs.setStatus(ObservationStatus.FINAL);
        AgeAtDeathObs.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"39016-1","Age at death")));//, null);
        AgeAtDeathObs.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        AgeAtDeathObs.setValue(new Quantity());
        // AgeAtDeathObs.setDataAbsentReason(new CodeableConcept(CodeSystems.Data_Absent_Reason_HL7_V3, "unknown", "Unknown", null); // set at birth
        AddReferenceToComposition(AgeAtDeathObs.getId(),"DecedentDemographics");
        //Bundle.addResourceEntry(AgeAtDeathObs, "urn:uuid:" + AgeAtDeathObs.getId());
        Resource resource=AgeAtDeathObs;
        resource.setId("urn:uuid:"+AgeAtDeathObs.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Decedent Pregnancy Status.</summary>
    private Observation PregnancyObs;

    /// <summary> Create Pregnancy Status. </summary>
    private void CreatePregnancyObs()
    {
        PregnancyObs=new Observation();
        PregnancyObs.setId(UUID.randomUUID().toString());
        PregnancyObs.setMeta(new Meta());
        CanonicalType[]p_profile={URL.ProfileURL.DecedentPregnancyStatus};
        PregnancyObs.getMeta().setProfile(Arrays.asList(p_profile));
        PregnancyObs.setStatus(ObservationStatus.FINAL);
        PregnancyObs.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"69442-2","Timing of recent pregnancy in relation to death")));//, null);
        PregnancyObs.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        AddReferenceToComposition(PregnancyObs.getId(),"DeathInvestigation");
        //Bundle.addResourceEntry(PregnancyObs, "urn:uuid:" + PregnancyObs.getId());
        Resource resource=PregnancyObs;
        resource.setId("urn:uuid:"+PregnancyObs.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Create Injury Location.</summary>
    private void CreateInjuryLocationLoc()
    {
        InjuryLocationLoc=new Location();
        InjuryLocationLoc.setId(UUID.randomUUID().toString());
        InjuryLocationLoc.setMeta(new Meta());
        CanonicalType[]injurylocation_profile={URL.ProfileURL.InjuryLocation};
        InjuryLocationLoc.getMeta().setProfile(Arrays.asList(injurylocation_profile));
        InjuryLocationLoc.setName(BlankPlaceholder); // We cannot have a blank String, but the field is required to be present
        InjuryLocationLoc.setAddress(DeathCertificateDocumentUtil.mapToAddress(DeathCertificateDocumentUtil.EmptyAddrMap()));
        InjuryLocationLoc.getType().add(new CodeableConcept(new Coding(CodeSystems.LocationType,"injury","injury location")));//, null));
        AddReferenceToComposition(InjuryLocationLoc.getId(),"DeathInvestigation");
        //Bundle.addResourceEntry(InjuryLocationLoc, "urn:uuid:" + InjuryLocationLoc.getId());
        Resource resource=InjuryLocationLoc;
        resource.setId("urn:uuid:"+InjuryLocationLoc.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Injury Incident.</summary>
    private Observation InjuryIncidentObs;

    /// <summary>Create Injury Incident.</summary>
    private void CreateInjuryIncidentObs()
    {
        InjuryIncidentObs=new Observation();
        InjuryIncidentObs.setId(UUID.randomUUID().toString());
        InjuryIncidentObs.setMeta(new Meta());
        CanonicalType[]iio_profile={URL.ProfileURL.InjuryIncident};
        InjuryIncidentObs.getMeta().setProfile(Arrays.asList(iio_profile));
        InjuryIncidentObs.setStatus(ObservationStatus.FINAL);
        InjuryIncidentObs.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"11374-6","Injury incident description Narrative")));//, null);
        InjuryIncidentObs.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        InjuryIncidentObs.setEffective(new DateTimeType());
        InjuryIncidentObs.getEffective().getExtension().add(DeathCertificateDocumentUtil.NewBlankPartialDateTimeExtension(true));
        AddReferenceToComposition(InjuryIncidentObs.getId(),"DeathInvestigation");
        //Bundle.addResourceEntry(InjuryIncidentObs, "urn:uuid:" + InjuryIncidentObs.getId());
        Resource resource=InjuryIncidentObs;
        resource.setId("urn:uuid:"+InjuryIncidentObs.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Death Location.</summary>
    private Location DeathLocationLoc;

    /// <summary>Create Death Location </summary>
    private void CreateDeathLocation()
    {
        DeathLocationLoc=new Location();
        DeathLocationLoc.setId(UUID.randomUUID().toString());
        DeathLocationLoc.setMeta(new Meta());
        CanonicalType[]deathlocation_profile={URL.ProfileURL.DeathLocation};
        DeathLocationLoc.getMeta().setProfile(Arrays.asList(deathlocation_profile));
        DeathLocationLoc.getType().add(new CodeableConcept(new Coding(CodeSystems.LocationType,"death","death location")));//, null));
        DeathLocationLoc.setName(BlankPlaceholder); // We cannot have a blank String, but the field is required to be present
        AddReferenceToComposition(DeathLocationLoc.getId(),"DeathInvestigation");
        //Bundle.addResourceEntry(DeathLocationLoc, "urn:uuid:" + DeathLocationLoc.getId());
        Resource resource=DeathLocationLoc;
        resource.setId("urn:uuid:"+DeathLocationLoc.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }


    /// <summary>Date Of Death.</summary>
    private Observation DeathDateObs;

    /// <summary>Create Death Date Observation.</summary>
    public void CreateDeathDateObs()
    {
        DeathDateObs=new Observation();
        DeathDateObs.setId(UUID.randomUUID().toString());
        DeathDateObs.setMeta(new Meta());
        CanonicalType[]deathdate_profile={URL.ProfileURL.DeathDate};
        DeathDateObs.getMeta().setProfile(Arrays.asList(deathdate_profile));
        DeathDateObs.setStatus(ObservationStatus.FINAL);
        DeathDateObs.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"81956-5","Date+time of death")));//, null);

        // Decedent is present in DeathCertificateDocuments, and absent in all other bundles.
        if(Decedent!= null){
        DeathDateObs.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        }

        // A DeathDate can be represented either using the PartialDateTime or the valueDateTime; we always prefer
        // the PartialDateTime representation (though we'll correctly read records using valueDateTime) and so we
        // by default set up all the PartialDate extensions with a default state of "data absent"
        DeathDateObs.setValue(new DateTimeType());
        DeathDateObs.getValue().getExtension().add(DeathCertificateDocumentUtil.NewBlankPartialDateTimeExtension(true));
        DeathDateObs.setMethod(null);

        AddReferenceToComposition(DeathDateObs.getId(),"DeathInvestigation");
        //Bundle.addResourceEntry(DeathDateObs, "urn:uuid:" + DeathDateObs.getId());
        Resource resource=DeathDateObs;
        resource.setId("urn:uuid:"+DeathDateObs.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Create Death Date Pronouncement Observation Component Component.</summary>
    private Observation.ObservationComponentComponent CreateDateOfDeathPronouncementObs()
    {
        if(DeathDateObs == null)
        {
            CreateDeathDateObs(); // Create it
        }
        Observation.ObservationComponentComponent datetimePronouncedDeadComponent=new Observation.ObservationComponentComponent();
        Observation.ObservationComponentComponent pronComp=DeathDateObs.getComponent().stream().filter((entry->((Observation.ObservationComponentComponent)entry).getCode() != null
        &&((Observation.ObservationComponentComponent)entry).getCode().getCoding().stream().filter(() != null && ((Observation.ObservationComponentComponent)entry).getCode().getCoding().stream().filter().getCode().equals("80616-6")).findFirst();
        if(pronComp!= null)
        {
            datetimePronouncedDeadComponent=pronComp;
        }
        else
        {
            datetimePronouncedDeadComponent=new Observation.ObservationComponentComponent();
            datetimePronouncedDeadComponent.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"80616-6","Date and time pronounced dead [US Standard Certificate of Death]")));//, null);
            DeathDateObs.getComponent().add(datetimePronouncedDeadComponent);
        }
        datetimePronouncedDeadComponent.setValue(null); // will be set to DateTimeType for full datetime or a Time if only time is present
        return datetimePronouncedDeadComponent;
    }

    /// <summary>Date Of Surgery.</summary>
    private Observation SurgeryDateObs;

    /// <summary>Create Surgery Date Observation.</summary>
    private void CreateSurgeryDateObs()
    {
        SurgeryDateObs=new Observation();
        SurgeryDateObs.setId(UUID.randomUUID().toString());
        SurgeryDateObs.setMeta(new Meta());
        CanonicalType[]profile={URL.ProfileURL.SurgeryDate};
        SurgeryDateObs.getMeta().setProfile(Arrays.asList(profile));
        SurgeryDateObs.setStatus(ObservationStatus.FINAL);
        SurgeryDateObs.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"80992-1","Date and time of surgery")));//, null);
        SurgeryDateObs.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        // A SurgeryDate can be represented either using the PartialDateTime or the valueDateTime as with DeathDate above
        SurgeryDateObs.setValue(new DateTimeType();
        SurgeryDateObs.getValue().getExtension().add(NewBlankPartialDateTimeExtension(false));
        AddReferenceToComposition(SurgeryDateObs.getId(),"DeathInvestigation");
        //Bundle.addResourceEntry(SurgeryDateObs, "urn:uuid:" + SurgeryDateObs.getId());
        Resource resource=SurgeryDateObs;
        resource.setId("urn:uuid:"+SurgeryDateObs.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    // Coded Observations
    /// <summary> Activity at Time of Death </summary>
    private Observation ActivityAtTimeOfDeathObs;

    /// <summary>Create an empty ActivityAtTimeOfDeathObs, to be populated in ActivityAtDeath.</summary>
    private void CreateActivityAtTimeOfDeathObs()
    {
        ActivityAtTimeOfDeathObs=new Observation();
        ActivityAtTimeOfDeathObs.setId(UUID.randomUUID().toString());
        ActivityAtTimeOfDeathObs.setMeta(new Meta());
        CanonicalType[]profile={URL.ProfileURL.ActivityAtTimeOfDeath};
        ActivityAtTimeOfDeathObs.getMeta().setProfile(Arrays.asList(profile));
        ActivityAtTimeOfDeathObs.setStatus(ObservationStatus.FINAL);
        ActivityAtTimeOfDeathObs.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"80626-5","Activity at time of death [CDC]")));//, null);
        ActivityAtTimeOfDeathObs.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        AddReferenceToComposition(ActivityAtTimeOfDeathObs.getId(),"CodedContent");
        //Bundle.addResourceEntry(ActivityAtTimeOfDeathObs, "urn:uuid:" + ActivityAtTimeOfDeathObs.getId());
        Resource resource=ActivityAtTimeOfDeathObs;
        resource.setId("urn:uuid:"+ActivityAtTimeOfDeathObs.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary> Automated Underlying Cause of Death </summary>
    private Observation AutomatedUnderlyingCauseOfDeathObs;

    /// <summary>Create an empty AutomatedUnderlyingCODObs, to be populated in AutomatedUnderlyingCOD.</summary>
    private void CreateAutomatedUnderlyingCauseOfDeathObs()
    {
        AutomatedUnderlyingCauseOfDeathObs=new Observation();
        AutomatedUnderlyingCauseOfDeathObs.setId(UUID.randomUUID().toString());
        AutomatedUnderlyingCauseOfDeathObs.setMeta(new Meta());
        CanonicalType[]profile={URL.ProfileURL.AutomatedUnderlyingCauseOfDeath};
        AutomatedUnderlyingCauseOfDeathObs.getMeta().setProfile(profile;
        AutomatedUnderlyingCauseOfDeathObs.setStatus(ObservationStatus.FINAL;
        AutomatedUnderlyingCauseOfDeathObs.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"80358-5","Cause of death.underlying [Automated]")));//, null);
        AutomatedUnderlyingCauseOfDeathObs.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        AddReferenceToComposition(AutomatedUnderlyingCauseOfDeathObs.getId(),"CodedContent");
        //Bundle.addResourceEntry(AutomatedUnderlyingCauseOfDeathObs, "urn:uuid:" + AutomatedUnderlyingCauseOfDeathObs.getId());
        Resource resource=AutomatedUnderlyingCauseOfDeathObs;
        resource.setId("urn:uuid:"+AutomatedUnderlyingCauseOfDeathObs.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary> Manual Underlying Cause of Death </summary>
    private Observation ManualUnderlyingCauseOfDeathObs;

    /// <summary>Create an empty AutomatedUnderlyingCODObs, to be populated in AutomatedUnderlyingCOD.</summary>
    private void CreateManualUnderlyingCauseOfDeathObs()
    {
        ManualUnderlyingCauseOfDeathObs=new Observation();
        ManualUnderlyingCauseOfDeathObs.setId(UUID.randomUUID().toString());
        ManualUnderlyingCauseOfDeathObs.setMeta(new Meta());
        CanonicalType[]profile={URL.ProfileURL.AutomatedUnderlyingCauseOfDeath};
        ManualUnderlyingCauseOfDeathObs.getMeta().setProfile(Arrays.asList(profile));
        ManualUnderlyingCauseOfDeathObs.setStatus(ObservationStatus.FINAL);
        ManualUnderlyingCauseOfDeathObs.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"80359-3","Cause of death.underlying [Manual]")));//, null);
        ManualUnderlyingCauseOfDeathObs.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        AddReferenceToComposition(ManualUnderlyingCauseOfDeathObs.getId(),"CodedContent");
        //Bundle.addResourceEntry(ManualUnderlyingCauseOfDeathObs, "urn:uuid:" + ManualUnderlyingCauseOfDeathObs.getId());
        Resource resource=ManualUnderlyingCauseOfDeathObs;
        resource.setId("urn:uuid:"+ManualUnderlyingCauseOfDeathObs.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary> Place Of Injury </summary>
    private Observation PlaceOfInjuryObs;

    /// <summary>Create an empty PlaceOfInjuryObs, to be populated in PlaceOfInjury.</summary>
    private void CreatePlaceOfInjuryObs(){
        PlaceOfInjuryObs=new Observation();
        PlaceOfInjuryObs.setId(UUID.randomUUID().toString());
        PlaceOfInjuryObs.setMeta(new Meta());
        CanonicalType[]profile={URL.ProfileURL.AutomatedUnderlyingCauseOfDeath};
        PlaceOfInjuryObs.getMeta().setProfile(Arrays.asList(profile));
        PlaceOfInjuryObs.setStatus(ObservationStatus.FINAL);
        PlaceOfInjuryObs.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC,"11376-1","Injury location")));//, null);
        PlaceOfInjuryObs.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        AddReferenceToComposition(PlaceOfInjuryObs.getId(),"CodedContent");
        //Bundle.addResourceEntry(PlaceOfInjuryObs, "urn:uuid:" + PlaceOfInjuryObs.getId());
        Resource resource=PlaceOfInjuryObs;
        resource.setId("urn:uuid:"+PlaceOfInjuryObs.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>The Decedent's Race and Ethnicity provided by Jurisdiction.</summary>
    private Observation CodedRaceAndEthnicityObs;

    /// <summary>Create an empty CodedRaceAndEthnicityObs, to be populated in Various Methods.</summary>
    private void CreateCodedRaceAndEthnicityObs()
    {
        CodedRaceAndEthnicityObs=new Observation();
        CodedRaceAndEthnicityObs.setId(UUID.randomUUID().toString());
        CodedRaceAndEthnicityObs.setMeta(new Meta());
        CanonicalType[]profile={URL.ProfileURL.CodedRaceAndEthnicity};
        CodedRaceAndEthnicityObs.getMeta().setProfile(Arrays.asList(profile));
        CodedRaceAndEthnicityObs.setStatus(ObservationStatus.FINAL);
        CodedRaceAndEthnicityObs.setCode(new CodeableConcept(new Coding(CodeSystems.ObservationCode,"codedraceandethnicity","Coded Race and Ethnicity")));//, null);
        CodedRaceAndEthnicityObs.setSubject(new Reference("urn:uuid:"+Decedent.getId()));
        AddReferenceToComposition(CodedRaceAndEthnicityObs.getId(),"CodedContent");
        //Bundle. (CodedRaceAndEthnicityObs, "urn:uuid:" + CodedRaceAndEthnicityObs.getId());
        Resource resource=CodedRaceAndEthnicityObs;
        resource.setId("urn:uuid:"+CodedRaceAndEthnicityObs.getId());
        Bundle.addEntry(new BundleEntryComponent().setResource(resource));
    }

    /// <summary>Entity Axis Cause of Death</summary>
    private List<Observation> EntityAxisCauseOfDeathObsList;

    /// <summary>Record Axis Cause of Death</summary>
    private List<Observation> RecordAxisCauseOfDeathObsList;

    /// <summary>Add a reference to the Death Record Composition.</summary>
    /// <param name="reference">a reference.</param>
    /// <param name="code">the code for the section to add to.</param>
    /// The sections are from : CodeSystems.DocumentSections
    ///               DecedentDemographics
    ///               DeathInvestigation
    ///               DeathCertification
    ///               DecedentDisposition
    ///               CodedContent
    private void AddReferenceToComposition(String reference, String code)
    {
        // In many of the createXXXXXX methods this gets called as a last step to add a reference to the new instance to the composition.
        // The Composition is present only in the DeathCertificateDocument, and is absent in all of the other bundles.
        // In lieu of putting conditional logic in all of the calling methods, added it here.
        if(Composition == null)
        {
            return;
        }

        //Composition.Section.get(0).getEntry().add(new Reference("urn:uuid:" + reference));
        Composition.SectionComponent section=new Composition.SectionComponent();
        String[]sections=new String[]{"DecedentDemographics","DeathInvestigation","DeathCertification","DecedentDisposition","CodedContent"};
        if(Arrays.asList(sections).contains(code))
        {
            // Find the right section
            for(Composition.SectionComponent s:Composition.getSection())
            {
                if(s.getCode() != null && s.getCode().getCoding().size()>0 && s.getCode().getCoding().get(0).getCode().equals(code))
                {
                    section=s;
                }
            }
            if(section.getCode() == null)
            {
                Map<String, String> coding=new HashMap<>();
                coding.put("system",CodeSystems.DocumentSections);
                coding.put("code",code);
                section.setCode(DeathCertificateDocumentUtil.MapToCodeableConcept(coding));
                Composition.getSection().add(section);
            }
            section.getEntry().add(new Reference("urn:uuid:"+reference));
        }
    }

    /// <summary>Remove a reference from the Death Record Composition.</summary>
    /// <param name="reference">a reference.</param>
    /// <param name="code">a code for the section to modify.</param>
    private boolean RemoveReferenceFromComposition(String reference,String code)
    {
        Composition.SectionComponent section=Composition.getSection().stream().filter(s->s.getCode().getCoding().get(0).getCode().equals(code)).findFirst().get();
        return section.getEntry().removeIf(entry->entry.getReference().equals(reference));// > 0;
    }

    /// <summary>Restores class references from a newly parsed record.</summary>
    private void RestoreReferences()
    {
        // Depending on the type of bundle, some of this information may not be present, so check it in a null-safe way
        String profile=Bundle.getMeta() != null ?Bundle.getMeta().getProfile() != null ? Bundle.getMeta().getProfile().stream().findFirst().get().fhirType():null:null;
        boolean fullRecord=URL.ProfileURL.DeathCertificateDocument.equals(profile);
        // Grab Composition
        BundleEntryComponent compositionEntry=Bundle.getEntry().stream().filter(entry->entry.getResource()instanceof Composition).findFirst().get();
        if(compositionEntry!= null)
        {
            Composition=(Composition)compositionEntry.getResource();
        }
        else if(fullRecord)
        {
            throw new IllegalArgumentException("Failed to find a Composition. The first entry in the FHIR Bundle should be a Composition.");
        }

        // Grab Patient
        if(fullRecord&&(Composition.getSubject() == null  ||  isNullOrWhiteSpace(Composition.getSubject().getReference())))
        {
            throw new IllegalArgumentException("The Composition is missing a subject (a reference to the Decedent resource).");
        }
        BundleEntryComponent patientEntry=Bundle.getEntry().stream().filter(entry->entry.getResource()instanceof Patient).findFirst().get();
        if(patientEntry!= null)
        {
            Decedent=(Patient)patientEntry.getResource();
        }
        else if(fullRecord)
        {
            throw new IllegalArgumentException("Failed to find a Decedent (Patient).");
        }

        // Grab Certifier
        if(Composition == null || (Composition.getAttester() == null || Composition.getAttester().stream().filter() == null || Composition.getAttester().get(0).getParty() == null || isNullOrWhiteSpace(Composition.getAttester().get(0).getParty().getReference())))
        {
            if(fullRecord)
            {
                throw new IllegalArgumentException("The Composition is missing an attestor (a reference to the Certifier/Practitioner resource).");
            }
        }
        else
        {  // There is an attester
            String attesterID=(Composition.getAttester().get(0).getParty().getReference()).split("/")[1]; // Practititioner/Certifier-Example1 --> Certifier-Example1.  Trims the type off of the path
            BundleEntryComponent practitionerEntry=Bundle.getEntry().stream().filter(entry->entry.getResource() instanceof Practitioner&&(entry.getFullUrl().equals(Composition.getAttester().get(0).getParty().getReference()) || (entry.getResource().getId()) != null && entry.getResource().getId().equals(attesterID))).findFirst().get();
                if(practitionerEntry!= null)
                {
                    Certifier=(Practitioner)practitionerEntry.getResource();
                }
        }
        // else
        // {
        //     throw new IllegalArgumentException("Failed to find a Certifier (Practitioner). The third entry in the FHIR Bundle is usually the Certifier (Practitioner). Either the Certifier is missing from the Bundle, or the attestor reference specified in the Composition is incorrect.");
        // }
        // *** Pronouncer and Mortician are not supported by IJE. ***
        // They can be included in DeathCertificateDocument and linked from DeathCertificate.  THe only sure way to find them is to look for the reference from DeathDate and DispositionMethod, respectively.
        // For now, we comment them out.
        // // Grab Pronouncer
        // // IMPROVEMENT: Move away from using meta profile to find this Practitioner.  Use performer reference from DeathDate
        // var pronouncerEntry = Bundle.getEntry().stream().filter( entry -> entry.getResource().getResource()Type == ResourceType.Practitioner && entry.getResource().Meta.Profile.stream().filter() != null && MatchesProfile("VRDR-Death-Pronouncement-Performer", entry.getResource().Meta.Profile.stream().filter()));
        // if (pronouncerEntry != null)
        // {
        //     Pronouncer = (Practitioner)pronouncerEntry.getResource();
        // }

        // Grab Death Certification
        BundleEntryComponent procedureEntry=Bundle.getEntry().stream().filter(entry->entry.getResource()instanceof Procedure).findFirst().get();
        if(procedureEntry!= null)
        {
            DeathCertification=(Procedure)procedureEntry.getResource();
        }

        // // Grab State Local Identifier
        // var stateDocumentReferenceEntry = Bundle.getEntry().stream().filter( entry -> entry.getResource().getResource()Type == ResourceType.DocumentReference && ((DocumentReference)entry.getResource()).getType().Coding.get(0).Code == "64297-5" );
        // if (stateDocumentReferenceEntry != null)
        // {
        //     StateDocumentReference = (DocumentReference)stateDocumentReferenceEntry.getResource();
        // }

        // Grab Funeral Home  - Organization with type="funeral"
        BundleEntryComponent funeralHome=Bundle.getEntry().stream().filter(entry->entry.getResource() instanceof Organization&&
        ((Organization)entry.getResource()).getType().stream().findFirst().get() != null && (CodeableConceptToMap(((Organization)entry.getResource()).getType().get(0)).get("code").equals("funeralhome"))).findFirst().get();
        if(funeralHome!= null)
        {
            FuneralHome=(Organization)funeralHome.getResource();
        }
        // Grab Coding Status
        BundleEntryComponent parameterEntry=Bundle.getEntry().stream().filter(entry->entry.getResource()instanceof Parameters).findFirst().get();
        if(parameterEntry!= null)
        {
            CodingStatusValues=(Parameters)parameterEntry.getResource();
        }
        // Scan through all Observations to make sure they all have codes!
        for(BundleEntryComponent ob:Bundle.getEntry().stream().filter(entry->entry.getResource()instanceof Observation).collect(Collectors.toList()))
        {
            Observation obs=(Observation)ob.getResource();
            if(obs == null || obs.getCode() == null || obs.getCode().getCoding() == null || obs.getCode().getCoding().get(0) == null || obs.getCode().getCoding().get(0).getCode() == null)
            {
                throw new IllegalArgumentException("Found an Observation resource that did not contain a code. All Observations must include a code to specify what the Observation is referring to.");
            }
            switch(obs.getCode().getCoding().get(0).getCode())
            {
                case"69449-7":
                    MannerOfDeath=(Observation)obs;
                    break;

                case"80905-3":
                    DispositionMethod=(Observation)obs;
                    // Link the Mortician based on the performer of this observation
                    break;

                case"69441-4":
                    ConditionContributingToDeath=(Observation)obs;
                    break;

                case"69453-9":
                    int lineNumber=0;
                    Observation.ObservationComponentComponent lineNumComp=obs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("lineNumber")).findFirst().get();
                    if(lineNumComp != null && lineNumComp.getValue() != null)
                    {
                        lineNumber=Integer.parseInt(lineNumComp.getValue().toString());
                    }
                    switch(lineNumber)
                    {
                        case 1:
                            CauseOfDeathConditionA=obs;
                            break;

                        case 2:
                            CauseOfDeathConditionB=obs;
                            break;

                        case 3:
                            CauseOfDeathConditionC=obs;
                            break;

                        case 4:
                            CauseOfDeathConditionD=obs;
                            break;

                        default: // invalid position, should we go kaboom?
                                // throw new IllegalArgumentException("Found a Cause of Death Part1 Observation with a linenumber other than 1-4.");
                            break;
                    }
                    break;

                case"80913-7":
                    DecedentEducationLevel=obs;
                    break;

                case"21843-8":
                    UsualWork=obs;
                    break;

                case"55280-2":
                    MilitaryServiceObs=obs;
                    break;

                case"BR":
                    BirthRecordIdentifier=obs;
                    break;

                case"emergingissues":
                    EmergingIssues=obs;
                    break;

                case"codedraceandethnicity":
                    CodedRaceAndEthnicityObs=obs;
                    break;

                case"inputraceandethnicity":
                    InputRaceAndEthnicityObs=obs;
                    break;

                case"11376-1":
                    PlaceOfInjuryObs=obs;
                    break;

                case"80358-5":
                    AutomatedUnderlyingCauseOfDeathObs=obs;
                    break;

                case"80359-3":
                    ManualUnderlyingCauseOfDeathObs=obs;
                    break;

                case"80626-5":
                    ActivityAtTimeOfDeathObs=obs;
                    break;

                case"80992-1":
                    SurgeryDateObs=obs;
                    break;

                case"81956-5":
                    DeathDateObs=obs;
                    break;

                case"11374-6":
                    InjuryIncidentObs=obs;
                    break;

                case"69443-0":
                    TobaccoUseObs=obs;
                    break;

                case"74497-9":
                    ExaminerContactedObs=obs;
                    break;

                case"69442-2":
                    PregnancyObs=obs;
                    break;

                case"39016-1":
                    AgeAtDeathObs=obs;
                    break;

                case"85699-7":
                    AutopsyPerformed=obs;
                    break;

                case"80356-9":
                    if(EntityAxisCauseOfDeathObsList == null)
                    {
                        EntityAxisCauseOfDeathObsList=new ArrayList();
                    }
                    EntityAxisCauseOfDeathObsList.add(obs);
                    break;

                case"80357-7":
                    if(RecordAxisCauseOfDeathObsList == null)
                    {
                        RecordAxisCauseOfDeathObsList = new ArrayList();
                    }
                    RecordAxisCauseOfDeathObsList.add(obs);
                    break;

                default:
                    // skip
                    break;
            }
        }

        // Scan through all RelatedPerson to make sure they all have relationship codes!
        for(BundleEntryComponent rp:Bundle.getEntry().stream().filter(entry->entry.getResource() instanceof RelatedPerson).collect(Collectors.toList()))
        {
            RelatedPerson rpn=(RelatedPerson)rp.getResource();
            if(rpn.getRelationship() == null || rpn.getRelationship().get(0) == null || rpn.getRelationship().get(0).getCoding() == null || rpn.getRelationship().get(0).getCoding().get(0) == null || rpn.getRelationship().get(0).getCoding().get(0).getCode() == null)
            {
                throw new IllegalArgumentException("Found a RelatedPerson resource that did not contain a relationship code. All RelatedPersons must include a relationship code to specify how the RelatedPerson is related to the subject.");
            }
            switch(rpn.getRelationship().get(0).getCoding().get(0).getCode())
            {
                case"FTH":
                Father=(RelatedPerson)rpn;
                break;

                case"MTH":
                Mother=(RelatedPerson)rpn;
                break;

                case"SPS":
                Spouse=(RelatedPerson)rpn;
                break;

                default:
                    // skip
                    break;
            }
        }

        for(Element rp:Bundle.getEntry().stream().filter(entry->entry.getResource() instanceof Location).collect(Collectors.toList()))
        {
            Location lcn=(Location)rp.;
            if(lcn.getType() == null || lcn.getType().get(0).getCoding() == null || lcn.getType().get(0).getCoding().get(0).getCode() == null)
            {
                throw new IllegalArgumentException("Found a Location resource that did not contain a type code. All Locations must include a type code to specify the role of the location.");
            }
            else
            {
                switch(lcn.getType().get(0).getCoding().get(0).getCode())
                {
                    case"death":
                    DeathLocationLoc = lcn;
                    break;

                    case"disposition":
                    DispositionLocation=lcn;
                    break;

                    case"injury":
                    InjuryLocationLoc=lcn;
                    break;

                    default:
                        // skip
                        break;
                }
            }
        }
        if(fullRecord)
        {
            UpdateDeathRecordIdentifier();
        }
    }

    /// response only
    /// <summary>Activity at Time of Death.</summary>
    /// <value>the decedent's activity at time of death. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; activity = new Map&lt;String, String&gt;();</para>
    /// <para>elevel.add("code", "0");</para>
    /// <para>elevel.add("system", CodeSystems.ActivityAtTimeOfDeath);</para>
    /// <para>elevel.add("display", "While engaged in sports activity");</para>
    /// <para>ExampleDeathRecord.ActivityAtDeath = activity;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Education Level: {ExampleDeathRecord.EducationLevel['display']}");</para>
    /// </example>
    ///[Property("Activity at Time of Death", Property.Types.Dictionary, "Coded Content", "Decedent's Activity at Time of Death.", true, IGURL.ActivityAtTimeOfDeath, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='80626-5')", "")]
    private Map<String, String> ActivityAtDeath;
    public Map<String, String> getActivityAtDeath()
    {
        if(ActivityAtTimeOfDeathObs != null && ActivityAtTimeOfDeathObs.getValue() != null && ActivityAtTimeOfDeathObs.getValue() instanceof CodeableConcept)
        {
            return CodeableConceptToMap((CodeableConcept)ActivityAtTimeOfDeathObs.getValue());
        }
        return EmptyCodeableMap();
    }

    public void setActivityAtDeath(Map<String, String> value)
    {
        if(ActivityAtTimeOfDeathObs == null)
        {
            CreateActivityAtTimeOfDeathObs();
        }
        ActivityAtTimeOfDeathObs.setValue(MapToCodeableConcept(value));
    }

    /// <summary>Decedent's Activity At Time of Death Helper</summary>
    /// <value>Decedent's Activity at Time of Death.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.ActivityAtDeath = 0;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Activity at Time of Death: {ExampleDeathRecord.ActivityAtDeath}");</para>
    /// </example>
    ///[Property("Activity at Time of Death Helper", Property.Types.String, "Coded Content", "Decedent's Activity at Time of Death.", false, IGURL.ActivityAtTimeOfDeath, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='80626-5')", "")]
    public String getActivityAtDeathHelper()
    {
        if(ActivityAtDeath.containsKey("code")&&isNullOrWhiteSpace(ActivityAtDeath.get("code")))
        {
            return ActivityAtDeath.get("code");
        }
        return null;
    }

    public void setActivityAtDeathHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("ActivityAtDeath", value, ValueSets.ActivityAtTimeOfDeath.Codes);
        }
    }


    /// <summary>Decedent's Automated Underlying Cause of Death</summary>
    /// <value>Decedent's Automated Underlying Cause of Death.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.AutomatedUnderlyingCOD = "I13.1";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Automated Underlying Cause of Death: {ExampleDeathRecord.AutomatedUnderlyingCOD}");</para>
    /// </example>
    ///[Property("Automated Underlying Cause of Death", Property.Types.String, "Coded Content", "Automated Underlying Cause of Death.", true, IGURL.AutomatedUnderlyingCauseOfDeath, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='80358-5')", "")]
    public String getAutomatedUnderlyingCOD()
    {
        if(AutomatedUnderlyingCauseOfDeathObs != null && AutomatedUnderlyingCauseOfDeathObs.getValue() != null && AutomatedUnderlyingCauseOfDeathObs.getValue() instanceof CodeableConcept)
        {
            String codeableConceptValueCode=CodeableConceptToMap((CodeableConcept)AutomatedUnderlyingCauseOfDeathObs.getValue()).get("code");
            if(!isNullOrWhiteSpace(codeableConceptValueCode))
            {
                return codeableConceptValueCode;
            }
            return null;
        }
        return null;
    }

    public void setAutomatedUnderlyingCOD(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
            return;
        }
        if(AutomatedUnderlyingCauseOfDeathObs == null)
        {
            CreateAutomatedUnderlyingCauseOfDeathObs();
        }
        AutomatedUnderlyingCauseOfDeathObs.setValue(new CodeableConcept(new Coding(CodeSystems.ICD10,value,null)));//, null);
    }


    /// <summary>Decedent's Manual Underlying Cause of Death</summary>
    /// <value>Decedent's Manual Underlying Cause of Death.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.ManUnderlyingCOD = "I13.1";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Decedent's Manual Underlying Cause of Death: {ExampleDeathRecord.ManUnderlyingCOD}");</para>
    /// </example>
    ///[Property("Manual Underlying Cause of Death", Property.Types.String, "Coded Content", "Manual Underlying Cause of Death.", true, IGURL.ManualUnderlyingCauseOfDeath, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='80358-580359-3')", "")]
    public String getManUnderlyingCOD()
    {
        if(ManualUnderlyingCauseOfDeathObs != null && ManualUnderlyingCauseOfDeathObs.getValue() != null && ManualUnderlyingCauseOfDeathObs.getValue() instanceof CodeableConcept)
        {
            String codeableConceptValueCode=CodeableConceptToMap((CodeableConcept)ManualUnderlyingCauseOfDeathObs.getValue()).get("code");
            if(isNullOrWhiteSpace(codeableConceptValueCode))
            {
                return null;
            }
            return codeableConceptValueCode;
        }
        return null;
    }

    public void setManUnderlyingCOD(String value)
    {
        if(isNullOrWhiteSpace(value))
        {
            return;
        }
        if(ManualUnderlyingCauseOfDeathObs == null)
        {
            CreateManualUnderlyingCauseOfDeathObs();
        }
        ManualUnderlyingCauseOfDeathObs.setValue(new CodeableConcept(new Coding(CodeSystems.ICD10, value,null)));//, null);
    }


    /// <summary>Place of Injury.</summary>
    /// <value>Place of Injury. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; elevel = new Map&lt;String, String&gt;();</para>
    /// <para>elevel.add("code", "LA14084-0");</para>
    /// <para>elevel.add("system", CodeSystems.LOINC);</para>
    /// <para>elevel.add("display", "Home");</para>
    /// <para>ExampleDeathRecord.PlaceOfInjury = elevel;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"PlaceOfInjury: {ExampleDeathRecord.PlaceOfInjury['display']}");</para>
    /// </example>
    ///[Property("Place of Injury", Property.Types.Dictionary, "Coded Content", "Place of Injury.", true, IGURL.PlaceOfInjury, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='11376-1')", "")]
    private Map<String, String> PlaceOfInjury;
    public Map<String, String> getPlaceOfInjury()
    {
        if(PlaceOfInjuryObs != null && PlaceOfInjuryObs.getValue() != null && PlaceOfInjuryObs.getValue() instanceof CodeableConcept)
        {
            return CodeableConceptToMap((CodeableConcept)PlaceOfInjuryObs.getValue());
        }
        return EmptyCodeableMap();
    }

    public void setPlaceOfInjury(Map<String, String> value)
    {
        if(PlaceOfInjuryObs == null)
        {
            CreatePlaceOfInjuryObs();
        }
        PlaceOfInjuryObs.setValue(MapToCodeableConcept(value));
    }

    /// <summary>Decedent's Place of Injury Helper</summary>
    /// <value>Place of Injury.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.PlaceOfInjuryHelper = ValueSets.PlaceOfInjury.Home;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Place of Injury: {ExampleDeathRecord.PlaceOfInjuryHelper}");</para>
    /// </example>
    ///[Property("Place of Injury Helper", Property.Types.String, "Coded Content", "Place of Injury.", false, IGURL.PlaceOfInjury, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='11376-1')", "")]
    public String getPlaceOfInjuryHelper()
    {
        if(PlaceOfInjury.containsKey("code") && !isNullOrWhiteSpace(PlaceOfInjury.get("code")))
        {
            return PlaceOfInjury.get("code");
        }
        return null;
    }

    public void setPlaceOfInjuryHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("PlaceOfInjury", value, ValueSets.PlaceOfInjury.Codes);
        }
    }


    /// <summary>First Edited Race Code.</summary>
    /// <value>First Edited Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.FirstEditedRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"First Edited Race Code: {ExampleDeathRecord.FirstEditedRaceCode['display']}");</para>
    /// </example>
    ///[Property("FirstEditedRaceCode", Property.Types.Dictionary, "Coded Content", "First Edited Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> FirstEditedRaceCode;
    public Map<String, String> getFirstEditedRaceCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("FirstEditedCode")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setFirstEditedRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("FirstEditedCode"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"FirstEditedCode","First Edited Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }

    /// <summary>First Edited Race Code  Helper</summary>
    /// <value>First Edited Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.FirstEditedRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"First Edited Race Code: {ExampleDeathRecord.FirstEditedRaceCodeHelper}");</para>
    /// </example>
    ///[Property("First Edited Race Code Helper", Property.Types.String, "Coded Content", "First Edited Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getFirstEditedRaceCodeHelper()
    {
        if(FirstEditedRaceCode.containsKey("code") && !isNullOrWhiteSpace(FirstEditedRaceCode.get("code")))
        {
            return FirstEditedRaceCode.get("code");
        }
        return null;
    }

    public void setFirstEditedRaceCodeHelper(String value){
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("FirstEditedRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>Second Edited Race Code.</summary>
    /// <value>Second Edited Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.SecondEditedRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Second Edited Race Code: {ExampleDeathRecord.SecondEditedRaceCode['display']}");</para>
    /// </example>
    ///[Property("SecondEditedRaceCode", Property.Types.Dictionary, "Coded Content", "Second Edited Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> SecondEditedRaceCode;
    public Map<String, String> getSecondEditedRaceCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("SecondEditedCode")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setSecondEditedRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("SecondEditedCode"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"SecondEditedCode","Second Edited Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Second Edited Race Code  Helper</summary>
    /// <value>Second Edited Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SecondEditedRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Second Edited Race Code: {ExampleDeathRecord.SecondEditedRaceCodeHelper}");</para>
    /// </example>
    ///[Property("Second Edited Race Code Helper", Property.Types.String, "Coded Content", "Second Edited Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getSecondEditedRaceCodeHelper()
    {
        if(SecondEditedRaceCode.containsKey("code") && !isNullOrWhiteSpace(SecondEditedRaceCode.get("code")))
        {
            return SecondEditedRaceCode.get("code");
        }
        return null;
    }

    public void setSecondEditedRaceCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("SecondEditedRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }

    /// <summary>Third Edited Race Code.</summary>
    /// <value>Third Edited Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.ThirdEditedRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Third Edited Race Code: {ExampleDeathRecord.ThirdEditedRaceCode['display']}");</para>
    /// </example>
    ///[Property("ThirdEditedRaceCode", Property.Types.Dictionary, "Coded Content", "Third Edited Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> ThirdEditedRaceCode;
    public Map<String, String> getThirdEditedRaceCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("ThirdEditedCode")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setThirdEditedRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("ThirdEditedCode"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"ThirdEditedCode","Third Edited Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Third Edited Race Code  Helper</summary>
    /// <value>Third Edited Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.ThirdEditedRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Third Edited Race Code: {ExampleDeathRecord.ThirdEditedRaceCodeHelper}");</para>
    /// </example>
    ///[Property("Third Edited Race Code Helper", Property.Types.String, "Coded Content", "Third Edited Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String ThirdEditedRaceCodeHelper()
    {
        if(ThirdEditedRaceCode.containsKey("code") && !isNullOrWhiteSpace(ThirdEditedRaceCode.get("code")))
        {
            return ThirdEditedRaceCode.get("code");
        }
        return null;
    }

    public void setThirdEditedRaceCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("ThirdEditedRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }

    /// <summary>Fourth Edited Race Code.</summary>
    /// <value>Fourth Edited Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.FourthEditedRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Fourth Edited Race Code: {ExampleDeathRecord.FourthEditedRaceCode['display']}");</para>
    /// </example>
    ///[Property("FourthEditedRaceCode", Property.Types.Dictionary, "Coded Content", "Fourth Edited Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> FourthEditedRaceCode;
    public Map<String, String> getFourthEditedRaceCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("FourthEditedCode")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setFourthEditedRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("FourthEditedCode"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"FourthEditedCode","Fourth Edited Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Fourth Edited Race Code  Helper</summary>
    /// <value>Fourth Edited Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.FourthEditedRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Fourth Edited Race Code: {ExampleDeathRecord.FourthEditedRaceCodeHelper}");</para>
    /// </example>
    ///[Property("Fourth Edited Race Code Helper", Property.Types.String, "Coded Content", "Fourth Edited Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getFourthEditedRaceCodeHelper()
    {
        if(FourthEditedRaceCode.containsKey("code") && !isNullOrWhiteSpace(FourthEditedRaceCode.get("code")))
        {
            return FourthEditedRaceCode.get("code");
        }
        return null;
    }

    public void setFourthEditedRaceCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("FourthEditedRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>Fifth Edited Race Code.</summary>
    /// <value>Fifth Edited Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.FifthEditedRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Fifth Edited Race Code: {ExampleDeathRecord.FifthEditedRaceCode['display']}");</para>
    /// </example>
    ///[Property("FifthEditedRaceCode", Property.Types.Dictionary, "Coded Content", "Fifth Edited Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> FifthEditedRaceCode;
    public Map<String, String> getFifthEditedRaceCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("FifthEditedCode")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setFifthEditedRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("FifthEditedCode"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"FifthEditedCode","Fifth Edited Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Fifth Edited Race Code  Helper</summary>
    /// <value>Fifth Edited Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.FifthEditedRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Fifth Edited Race Code: {ExampleDeathRecord.FifthEditedRaceCodeHelper}");</para>
    /// </example>
    ///[Property("Fifth Edited Race Code Helper", Property.Types.String, "Coded Content", "Fifth Edited Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getFifthEditedRaceCodeHelper()
    {
        if(FifthEditedRaceCode.containsKey("code") && !isNullOrWhiteSpace(FifthEditedRaceCode.get("code")))
        {
            return FifthEditedRaceCode.get("code");
        }
        return null;
    }

    public void setFifthEditedRaceCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("FifthEditedRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>Sixth Edited Race Code.</summary>
    /// <value>Sixth Edited Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.SixthEditedRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Sixth Edited Race Code: {ExampleDeathRecord.SixthEditedRaceCode['display']}");</para>
    /// </example>
    ///[Property("SixthEditedRaceCode", Property.Types.Dictionary, "Coded Content", "Sixth Edited Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> SixthEditedRaceCode;
    public Map<String, String> getSixthEditedRaceCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("SixthEditedCode")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setSixthEditedRaceCode(Map<String, String> value){
        if(CodedRaceAndEthnicityObs == null){
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("SixthEditedCode"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"SixthEditedCode","Sixth Edited Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Sixth Edited Race Code  Helper</summary>
    /// <value>Sixth Edited Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SixthEditedRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Sixth Edited Race Code: {ExampleDeathRecord.SixthEditedRaceCodeHelper}");</para>
    /// </example>
    ///[Property("Sixth Edited Race Code Helper", Property.Types.String, "Coded Content", "Sixth Edited Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getSixthEditedRaceCodeHelper()
    {
        if(SixthEditedRaceCode.containsKey("code") && !isNullOrWhiteSpace(SixthEditedRaceCode.get("code")))
        {
            return SixthEditedRaceCode.get("code");
        }
        return null;
    }

    public void setSixthEditedRaceCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("SixthEditedRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>Seventh Edited Race Code.</summary>
    /// <value>Seventh Edited Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.SeventhEditedRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Seventh Edited Race Code: {ExampleDeathRecord.SeventhEditedRaceCode['display']}");</para>
    /// </example>
    ///[Property("SeventhEditedRaceCode", Property.Types.Dictionary, "Coded Content", "Seventh Edited Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> SeventhEditedRaceCode;
    public Map<String, String> getSeventhEditedRaceCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("SeventhEditedCode")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setSeventhEditedRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("SeventhEditedCode"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"SeventhEditedCode","Seventh Edited Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Seventh Edited Race Code  Helper</summary>
    /// <value>Seventh Edited Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SeventhEditedRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Seventh Edited Race Code: {ExampleDeathRecord.SeventhEditedRaceCodeHelper}");</para>
    /// </example>
    ///[Property("Seventh Edited Race Code Helper", Property.Types.String, "Coded Content", "Seventh Edited Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getSeventhEditedRaceCodeHelper()
    {
        if(SeventhEditedRaceCode.containsKey("code") && !isNullOrWhiteSpace(SeventhEditedRaceCode.get("code")))
        {
            return SeventhEditedRaceCode.get("code");
        }
        return null;
    }

    public void setSeventhEditedRaceCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
        SetCodeValue("SeventhEditedRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>Eighth Edited Race Code.</summary>
    /// <value>Eighth Edited Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.EighthEditedRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Eighth Edited Race Code: {ExampleDeathRecord.EighthEditedRaceCode['display']}");</para>
    /// </example>
    ///[Property("EighthEditedRaceCode", Property.Types.Dictionary, "Coded Content", "Eighth Edited Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> EighthEditedRaceCode;
    public Map<String, String> getEighthEditedRaceCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("EighthEditedCode")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setEighthEditedRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("EighthEditedCode"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"EighthEditedCode","Eighth Edited Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Eighth Edited Race Code  Helper</summary>
    /// <value>Eighth Edited Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.EighthEditedRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Eighth Edited Race Code: {ExampleDeathRecord.EighthEditedRaceCodeHelper}");</para>
    /// </example>
    ///[Property("Eighth Edited Race Code Helper", Property.Types.String, "Coded Content", "Eighth Edited Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getEighthEditedRaceCodeHelper()
    {
        if(EighthEditedRaceCode.containsKey("code") && !isNullOrWhiteSpace(EighthEditedRaceCode.get("code")))
        {
            return EighthEditedRaceCode.get("code");
        }
        return null;
    }

    public void setEighthEditedRaceCodeHelper(String value){
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("EighthEditedRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>First American Indian Race Code.</summary>
    /// <value>First American Indian Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.FirstAmericanIndianRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"First American Indian Race Code: {ExampleDeathRecord.FirstAmericanIndianRaceCode['display']}");</para>
    /// </example>
    ///[Property("FirstAmericanIndianRaceCode", Property.Types.Dictionary, "Coded Content", "First American Indian Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> FirstAmericanIndianRaceCode;
    public Map<String, String> getFirstAmericanIndianRaceCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("FirstAmericanIndianRace")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setFirstAmericanIndianRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("FirstAmericanIndianRace"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"FirstAmericanIndianRace","First American Indian Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>First American Indian Race Code  Helper</summary>
    /// <value>First American Indian Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.FirstAmericanIndianRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"First American Indian Race Code: {ExampleDeathRecord.FirstAmericanIndianRaceCodeHelper}");</para>
    /// </example>
    ///[Property("First American Indian Race Code Helper", Property.Types.String, "Coded Content", "First American Indian Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getFirstAmericanIndianRaceCodeHelper()
    {
        if(FirstAmericanIndianRaceCode.containsKey("code") && !isNullOrWhiteSpace(FirstAmericanIndianRaceCode.get("code"))){
            return FirstAmericanIndianRaceCode.get("code");
        }
        return null;
    }

    public void setFirstAmericanIndianRaceCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value)){
            SetCodeValue("FirstAmericanIndianRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>Second American Indian Race Code.</summary>
    /// <value>Second American Indian Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.SecondAmericanIndianRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Second American Indian Race Code: {ExampleDeathRecord.SecondAmericanIndianRaceCode['display']}");</para>
    /// </example>
    ///[Property("SecondAmericanIndianRaceCode", Property.Types.Dictionary, "Coded Content", "Second American Indian Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> SecondAmericanIndianRaceCode;
    public Map<String, String> getSecondAmericanIndianRaceCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("SecondAmericanIndianRace")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setSecondAmericanIndianRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("SecondAmericanIndianRace"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"SecondAmericanIndianRace","Second American Indian Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Second American Indian Race Code  Helper</summary>
    /// <value>Second American Indian Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SecondAmericanIndianRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Second American Indian Race Code: {ExampleDeathRecord.SecondAmericanIndianRaceCodeHelper}");</para>
    /// </example>
    ///[Property("Second American Indian Race Code Helper", Property.Types.String, "Coded Content", "Second American Indian Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getSecondAmericanIndianRaceCodeHelper()
    {
        if(SecondAmericanIndianRaceCode.containsKey("code") && !isNullOrWhiteSpace(SecondAmericanIndianRaceCode.get("code")))
        {
            return SecondAmericanIndianRaceCode.get("code");
        }
        return null;
    }

    public void setSecondAmericanIndianRaceCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("SecondAmericanIndianRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>First Other Asian Race Code.</summary>
    /// <value>First Other Asian Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.FirstOtherAsianRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"First Other Asian Race Code: {ExampleDeathRecord.FirstOtherAsianRaceCode['display']}");</para>
    /// </example>
    ///[Property("FirstOtherAsianRaceCode", Property.Types.Dictionary, "Coded Content", "First Other Asian Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> FirstOtherAsianRaceCode;
    public Map<String, String> getFirstOtherAsianRaceCode(){
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("FirstOtherAsianRace")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setFirstOtherAsianRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("FirstOtherAsianRace"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"FirstOtherAsianRace","First Other Asian Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>First Other Asian Race Code  Helper</summary>
    /// <value>First Other Asian Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.FirstOtherAsianRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"First Other Asian Race Code: {ExampleDeathRecord.FirstOtherAsianRaceCodeHelper}");</para>
    /// </example>
    ///[Property("First Other Asian Race Code Helper", Property.Types.String, "Coded Content", "First Other Asian Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getFirstOtherAsianRaceCodeHelper()
    {
        if(FirstOtherAsianRaceCode.containsKey("code") && !isNullOrWhiteSpace(FirstOtherAsianRaceCode.get("code")))
        {
            return FirstOtherAsianRaceCode.get("code");
        }
        return null;
    }

    public void setFirstOtherAsianRaceCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("FirstOtherAsianRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>Second Other Asian Race Code.</summary>
    /// <value>Second Other Asian Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.SecondOtherAsianRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Second Other Asian Race Code: {ExampleDeathRecord.SecondOtherAsianRaceCode['display']}");</para>
    /// </example>
    ///[Property("SecondOtherAsianRaceCode", Property.Types.Dictionary, "Coded Content", "Second Other Asian Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> SecondOtherAsianRaceCode;
    public Map<String, String> getSecondOtherAsianRaceCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("SecondOtherAsianRace")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setSecondOtherAsianRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("SecondOtherAsianRace"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"SecondOtherAsianRace","Second Other Asian Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Second Other Asian Race Code  Helper</summary>
    /// <value>Second Other Asian Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SecondOtherAsianRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Second Other Asian Race Code: {ExampleDeathRecord.SecondOtherAsianRaceCodeHelper}");</para>
    /// </example>
    ///[Property("Second Other Asian Race Code Helper", Property.Types.String, "Coded Content", "Second Other Asian Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getSecondOtherAsianRaceCodeHelper()
    {
        if(SecondOtherAsianRaceCode.containsKey("code") && !isNullOrWhiteSpace(SecondOtherAsianRaceCode.get("code")))
        {
            return SecondOtherAsianRaceCode.get("code");
        }
        return null;
    }

    public void setSecondOtherAsianRaceCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("SecondOtherAsianRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>First Other Pacific Islander Race Code.</summary>
    /// <value>First Other Pacific Islander Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.FirstOtherPacificIslanderRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"First Other Pacific Islander Race Code: {ExampleDeathRecord.FirstOtherPacificIslanderRaceCode['display']}");</para>
    /// </example>
    ///[Property("FirstOtherPacificIslanderRaceCode", Property.Types.Dictionary, "Coded Content", "First Other Pacific Islander Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> FirstOtherPacificIslanderRaceCode;
    public Map<String, String> getFirstOtherPacificIslanderRaceCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("FirstOtherPacificIslanderRace")).findFirst().get();
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setFirstOtherPacificIslanderRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("FirstOtherPacificIslanderRace"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"FirstOtherPacificIslanderRace","First Other Pacific Islander Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>First Other Pacific Islander Race Code  Helper</summary>
    /// <value>First Other Pacific Islander Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.FirstOtherPacificIslanderRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"First Other Pacific Islander Race Code: {ExampleDeathRecord.FirstOtherPacificIslanderRaceCodeHelper}");</para>
    /// </example>
    ///[Property("First Other Pacific Islander Race Code Helper", Property.Types.String, "Coded Content", "First Other Pacific Islander Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getFirstOtherPacificIslanderRaceCodeHelper()
    {
        if(FirstOtherPacificIslanderRaceCode.containsKey("code") && !isNullOrWhiteSpace(FirstOtherPacificIslanderRaceCode.get("code")))
        {
            return FirstOtherPacificIslanderRaceCode.get("code");
        }
        return null;
    }

    public void setFirstOtherPacificIslanderRaceCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
        SetCodeValue("FirstOtherPacificIslanderRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>Second Other Pacific Islander Race Code.</summary>
    /// <value>Second Other Pacific Islander Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.SecondOtherPacificIslanderRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Second Other Pacific Islander Race Code: {ExampleDeathRecord.SecondOtherPacificIslanderRaceCode['display']}");</para>
    /// </example>
    ///[Property("SecondOtherPacificIslanderRaceCode", Property.Types.Dictionary, "Coded Content", "Second Other Pacific Islander Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> SecondOtherPacificIslanderRaceCode;
    public Map<String, String> getSecondOtherPacificIslanderRaceCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("SecondOtherPacificIslanderRace")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setSecondOtherPacificIslanderRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("SecondOtherPacificIslanderRace"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"SecondOtherPacificIslanderRace","Second Other Pacific Islander Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Second Other Pacific Islander Race Code  Helper</summary>
    /// <value>Second Other Pacific Islander Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SecondOtherPacificIslanderRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Second Other Pacific Islander Race Code: {ExampleDeathRecord.SecondOtherPacificIslanderRaceCodeHelper}");</para>
    /// </example>
    ///[Property("Second Other Pacific Islander Race Code Helper", Property.Types.String, "Coded Content", "Second Other Pacific Islander Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getSecondOtherPacificIslanderRaceCodeHelper()
    {
        if(SecondOtherPacificIslanderRaceCode.containsKey("code") && !isNullOrWhiteSpace(SecondOtherPacificIslanderRaceCode.get("code"))){
            return SecondOtherPacificIslanderRaceCode.get("code");
        }
        return null;
    }

    public void setSecondOtherPacificIslanderRaceCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("SecondOtherPacificIslanderRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>First Other Race Code.</summary>
    /// <value>First Other Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.FirstOtherRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"First Other Race Code: {ExampleDeathRecord.FirstOtherRaceCode['display']}");</para>
    /// </example>
    ///[Property("FirstOtherRaceCode", Property.Types.Dictionary, "Coded Content", "First Other Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> FirstOtherRaceCode;
    public Map<String, String> getFirstOtherRaceCode(){
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("FirstOtherRace");
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setFirstOtherRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("FirstOtherRace"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"FirstOtherRace","First Other Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>First Other Race Code  Helper</summary>
    /// <value>First Other Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.FirstOtherRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"First Other Race Code: {ExampleDeathRecord.FirstOtherRaceCodeHelper}");</para>
    /// </example>
    ///[Property("First Other Race Code Helper", Property.Types.String, "Coded Content", "First Other Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getFirstOtherRaceCodeHelper(){
        if(FirstOtherRaceCode.containsKey("code") && !isNullOrWhiteSpace(FirstOtherRaceCode.get("code")))
        {
            return FirstOtherRaceCode.get("code");
        }
        return null;
    }

    public void setFirstOtherRaceCodeHelper(String value){
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("FirstOtherRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>Second Other Race Code.</summary>
    /// <value>Second Other Race Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.SecondOtherRaceCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Second Other Race Code: {ExampleDeathRecord.SecondOtherRaceCode['display']}");</para>
    /// </example>
    ///[Property("SecondOtherRaceCode", Property.Types.Dictionary, "Coded Content", "Second Other Race Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> SecondOtherRaceCode;
    public Map<String, String> getSecondOtherRaceCode(){
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("SecondOtherRace");
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setSecondOtherRaceCode(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("SecondOtherRace");
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"SecondOtherRace","Second Other Race")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Second Other Race Code  Helper</summary>
    /// <value>Second Other Race Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.SecondOtherRaceCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Second Other Race Code: {ExampleDeathRecord.SecondOtherRaceCodeHelper}");</para>
    /// </example>
    ///[Property("Second Other Race Code Helper", Property.Types.String, "Coded Content", "Second Other Race Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getSecondOtherRaceCodeHelper()
    {
        if(SecondOtherRaceCode.containsKey("code") && !isNullOrWhiteSpace(SecondOtherRaceCode.get("code")))
        {
            return SecondOtherRaceCode.get("code");
        }
        return null;
    }

    public void setSecondOtherRaceCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value)){
            SetCodeValue("SecondOtherRaceCode", value, ValueSets.RaceCode.Codes);
        }
    }


    /// <summary>Hispanic Code.</summary>
    /// <value>Hispanic Code. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.HispanicCode = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Hispanic Code: {ExampleDeathRecord.HispanicCode['display']}");</para>
    /// </example>
    ///[Property("HispanicCode", Property.Types.Dictionary, "Coded Content", "Hispanic Code.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> HispanicCode;
    public Map<String, String> getHispanicCode()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("HispanicCode")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }
    public void setHispanicCode(Map<String, String> value){
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("HispanicCode"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"HispanicCode","Hispanic Code")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Hispanic Code  Helper</summary>
    /// <value>Hispanic Code Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.HispanicCodeHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Hispanic Code: {ExampleDeathRecord.HispanicCodeHelper}");</para>
    /// </example>
    ///[Property("Hispanic Code Helper", Property.Types.String, "Coded Content", "Hispanic Code.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getHispanicCodeHelper()
    {
        if(HispanicCode.containsKey("code") && !isNullOrWhiteSpace(HispanicCode.get("code")))
        {
            return HispanicCode.get("code");
        }
        return null;
    }

    public void setHispanicCodeHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("HispanicCode", value, ValueSets.HispanicOrigin.Codes);
        }
    }


    /// <summary>Hispanic Code For Literal.</summary>
    /// <value>Hispanic Code For Literal. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "300");</para>
    /// <para>racecode.add("system", CodeSystems.RaceCode);</para>
    /// <para>racecode.add("display", "African");</para>
    /// <para>ExampleDeathRecord.HispanicCodeForLiteral = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Hispanic Code For Literal: {ExampleDeathRecord.HispanicCodeForLiteral['display']}");</para>
    /// </example>
    ///[Property("HispanicCodeForLiteral", Property.Types.Dictionary, "Coded Content", "Hispanic Code For Literal.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> HispanicCodeForLiteral;
    public Map<String, String> getHispanicCodeForLiteral()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("HispanicCodeForLiteral")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setHispanicCodeForLiteral(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("HispanicCodeForLiteral"));
        Observation.ObservationComponentComponent component = new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"HispanicCodeForLiteral","Hispanic Code For Literal")));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Hispanic Code For Literal  Helper</summary>
    /// <value>Hispanic Code For Literal Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.HispanicCodeForLiteralHelper = ValueSets.RaceCode.African ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Hispanic Code For Literal: {ExampleDeathRecord.HispanicCodeForLiteralHelper}");</para>
    /// </example>
    ///[Property("Hispanic Code For Literal Helper", Property.Types.String, "Coded Content", "Hispanic Code For Literal.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getHispanicCodeForLiteralHelper()
    {
        if(HispanicCodeForLiteral.containsKey("code") && !isNullOrWhiteSpace(HispanicCodeForLiteral.get("code")))
        {
            return HispanicCodeForLiteral.get("code");
        }
        return null;
    }

    public void setHispanicCodeForLiteralHelper(String value)
    {
        if(!isNullOrWhiteSpace(value)){
            SetCodeValue("HispanicCodeForLiteral", value, ValueSets.HispanicOrigin.Codes);
        }
    }


    /// <summary>Race Recode 40.</summary>
    /// <value>Race Recode 40. A Map representing a code, containing the following key/value pairs:
    /// <para>"code" - the code</para>
    /// <para>"system" - the code system this code belongs to</para>
    /// <para>"display" - a human readable meaning of the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; racecode = new Map&lt;String, String&gt;();</para>
    /// <para>racecode.add("code", "09");</para>
    /// <para>racecode.add("system", CodeSystems.RaceRecode40CS);</para>
    /// <para>racecode.add("display", "Vietnamiese");</para>
    /// <para>ExampleDeathRecord.RaceRecode40 = racecode;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"RaceRecode40: {ExampleDeathRecord.RaceRecode40['display']}");</para>
    /// </example>
    ///[Property("RaceRecode40", Property.Types.Dictionary, "Coded Content", "RaceRecode40.", true, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    private Map<String, String> RaceRecode40;
    public Map<String, String> getRaceRecode40()
    {
        if(CodedRaceAndEthnicityObs != null)
        {
            Observation.ObservationComponentComponent racecode = CodedRaceAndEthnicityObs.getComponent().stream().filter(c->c.getCode().getCoding().get(0).getCode().equals("RaceRecode40")).findFirst().get();
            if(racecode != null && racecode.getValue() != null && racecode.getValue() instanceof CodeableConcept&&racecode.getValue() != null)
            {
                return CodeableConceptToMap((CodeableConcept)racecode.getValue());
            }
        }
        return EmptyCodeableMap();
    }

    public void setRaceRecode40(Map<String, String> value)
    {
        if(CodedRaceAndEthnicityObs == null)
        {
            CreateCodedRaceAndEthnicityObs();
        }
        CodedRaceAndEthnicityObs.getComponent().removeIf(c->c.getCode().getCoding().get(0).getCode().equals("RaceRecode40"));
        Observation.ObservationComponentComponent component=new Observation.ObservationComponentComponent();
        component.setCode(new CodeableConcept(new Coding(CodeSystems.ComponentCode,"RaceRecode40",null)));//, null);
        component.setValue(MapToCodeableConcept(value));
        CodedRaceAndEthnicityObs.getComponent().add(component);
    }


    /// <summary>Entity Axis Cause Of Death
    /// <para>Note that record axis codes have an unusual and obscure handling of a Pregnancy flag, for more information see
    /// http://build.fhir.org/ig/HL7/vrdr/branches/master/StructureDefinition-vrdr-record-axis-cause-of-death.html#usage></para>
    /// </summary>
    /// <value>Entity-axis codes</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para> ExampleDeathRecord.EntityAxisCauseOfDeath = new [] {(LineNumber: 2, Position: 1, Code: "T27.3", ECode: true)};</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"First Entity Axis Code: {ExampleDeathRecord.EntityAxisCauseOfDeath.ElementAt(0).Code}");</para>
    /// </example>
    ///[Property("Entity Axis Cause of Death", Property.Types.Tuple4Arr, "Coded Content", "", true, IGURL.EntityAxisCauseOfDeath, false, 50)]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=80356-9)", "")]
    //public Iterable<(int LineNumber, int Position, String Code, boolean ECode)> getEntityAxisCauseOfDeath()
    //        {
    //        List<(int LineNumber, int Position, String Code, boolean ECode)> eac = new List<(int LineNumber, int Position, String Code, boolean ECode)>();
    //        if (EntityAxisCauseOfDeathObsList != null)
    //        {
    //        for(Observation ob:EntityAxisCauseOfDeathObsList)
    //        {
    //        Integer lineNumber = null;
    //        Integer position = null;
    //        String icd10code = null;
    //        boolean ecode = false;
    //        Observation.ObservationComponentComponent positionComp = ob.getComponent().stream().filter(c -> c.getCode().getCoding().get(0).getCode().equals("position").stream().filter();
    //        if (positionComp != null && positionComp.getValue() != null)
    //        {
    //        position = ((IntegerType)positionComp.getValue()).getValue();
    //        }
    //        Observation.ObservationComponentComponent lineNumComp = ob.getComponent().stream().filter()c -> c.getCode().getCoding().get(0).getCode().equals("lineNumber").stream().filter();
    //        if (lineNumComp != null && lineNumComp.getValue() != null)
    //        {
    //        lineNumber = ((IntegerType)lineNumComp.getValue()).getValue();
    //        }
    //        CodeableConcept valueCC = (CodeableConcept)ob.getValue();
    //        if (valueCC != null && valueCC.getCoding() != null && valueCC.getCoding().size() > 0)
    //        {
    //        icd10code = valueCC.getCoding().get(0).getCode().trim();
    //        }
    //        Observation.ObservationComponentComponent ecodeComp = ob.getComponent().stream().filter(c -> c.getCode().getCoding().get(0).getCode().equals("eCodeIndicator").stream().filter();
    //        if (ecodeComp != null && ecodeComp.getValue() != null)
    //        {
    //        ecode = (boolean)((BooleanType)ecodeComp.getValue()).getValue();
    //        }
    //        if (lineNumber != null && position != null && icd10code != null)
    //        {
    //        eac.add((LineNumber: (int)lineNumber, Position: (int)position, Code: icd10code, ECode: ecode));
    //        }
    //        }
    //        }
    //        return eac.OrderBy(element -> element.getLineNumber()).ThenBy(element -> element.getPosition());
    //        }
    //
    //        public void setEntityAxisCauseOfDeath(String value)
    //        {
    //        // clear all existing eac
    //        Bundle.Entry().removeIf(entry -> entry.Resource is Observation && (((Observation)entry.Resource).Code.Coding.First().Code == "80356-9"));
    //        if (EntityAxisCauseOfDeathObsList != null)
    //        {
    //        EntityAxisCauseOfDeathObsList.Clear();
    //        }
    //        else
    //        {
    //        EntityAxisCauseOfDeathObsList = new List<Observation>();
    //        }
    //        // Rebuild the list of observations
    //        for((int LineNumber, int Position, String Code, boolean ECode) eac:value)
    //        {
    //        if(!isNullOrEmpty(eac.Code))
    //        {
    //        Observation ob = new Observation();
    //        ob.setId(UUID.randomUUID().toString());
    //        ob.setMeta(new Meta());
    //        CanonicalType[] entityAxis_profile = { URL.ProfileURL.EntityAxisCauseOfDeath };
    //        ob.getMeta().setProfile(entityAxis_profile;
    //        ob.setStatus(Observation.ObservationStatus.FINAL);
    //        ob.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "80356-9", "Cause of death entity axis code [Automated]")));//, null);
    //        ob.setSubject(new Reference("urn:uuid:" + Decedent.getId()));
    //        AddReferenceToComposition(ob.getId(), "CodedContent");
    //
    //        ob.setEffective(new DateTimeType());
    //        ob.setValue(new CodeableConcept(new Coding (CodeSystems.ICD10, eac.Code, null)));//, null));
    //        Observation.ObservationComponentComponent lineNumComp = new Observation.ObservationComponentComponent();
    //        lineNumComp.setValue(new Integer(eac.LineNumber);
    //        lineNumComp.setCode(new CodeableConcept(new Coding (CodeSystems.Component, "lineNumber", "lineNumber")));//, null);
    //        ob.getComponent().add(lineNumComp);
    //
    //        Observation.ObservationComponentComponent positionComp = new Observation.ObservationComponentComponent();
    //        positionComp.setValue(new Integer(eac.Position);
    //        positionComp.setCode(new CodeableConcept(new Coding(CodeSystems.Component, "position", "Position")));//, null);
    //        ob.getComponent().add(positionComp);
    //
    //        Observation.ObservationComponentComponent eCodeComp = new Observation.ObservationComponentComponent();
    //        eCodeComp.setValue(new BooleanType(eac.ECode));
    //        eCodeComp.setCode(new CodeableConcept(new Coding(CodeSystems.Component, "eCodeIndicator", "eCodeIndicator")));//, null);
    //        ob.getComponent().add(eCodeComp);
    //
    //        Bundle.AddResourceEntry(ob, "urn:uuid:" + ob.getId());
    //        EntityAxisCauseOfDeathObsList.add(ob);
    //        }
    //        }
    //        }


    /// <summary>Record Axis Cause Of Death</summary>
    /// <value>record-axis codes</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Tuple&lt;String, String, String&gt;[] eac = new Tuple&lt;String, String, String&gt;{Tuple.Create("position", "code", "pregnancy")}</para>
    /// <para>ExampleDeathRecord.RecordAxisCauseOfDeath = new [] { (Position: 1, Code: "T27.3", Pregnancy: true) };</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"First Record Axis Code: {ExampleDeathRecord.RecordAxisCauseOfDeath.ElememtAt(0).Code}");</para>
    /// </example>
    ///[Property("Record Axis Cause Of Death", Property.Types.Tuple4Arr, "Coded Content", "", true, IGURL.RecordAxisCauseOfDeath, false, 50)]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=80357-7)", "")]
    //public Iterable<(int Position, String Code, boolean Pregnancy)> getRecordAxisCauseOfDeath()
    //        {
    //
    //        List<(int Position, String Code, boolean Pregnancy)> rac = new List<(int Position, String Code, boolean Pregnancy)>();
    //        if (RecordAxisCauseOfDeathObsList != null)
    //        {
    //        for(Observation ob:RecordAxisCauseOfDeathObsList)
    //        {
    //        Integer position = null;
    //        String icd10code = null;
    //        boolean pregnancy = false;
    //        Observation.ObservationComponentComponent positionComp = ob.getComponent().stream().filter(c -> c.getCode().getCoding().get(0).getCode().equals("position")).findFirst().get();
    //        if (positionComp != null && positionComp.getValue() != null)
    //        {
    //        position = ((IntegerType)positionComp.getValue()).getValue();
    //        }
    //        CodeableConcept valueCC = (CodeableConcept)ob.getValue();
    //        if (valueCC != null && valueCC.getCoding() != null && valueCC.getCoding().size() > 0)
    //        {
    //        icd10code = valueCC.getCoding().get(0).getCode();
    //        }
    //        Observation.ObservationComponentComponent pregComp = ob.getComponent().stream().filter(c -> c.getCode().getCoding().get(0).getCode().equals("wouldBeUnderlyingCauseOfDeathWithoutPregnancy")).findFirst().get();
    //        if (pregComp != null && pregComp.getValue() != null)
    //        {
    //        pregnancy = ((BooleanType)pregComp.getValue()).getValue();
    //        }
    //        if (position != null && icd10code != null)
    //        {
    //        rac.add((Position: (int)position, Code: icd10code, Pregnancy: pregnancy));
    //        }
    //        }
    //        }
    //        return rac.OrderBy(entry -> entry.Position);
    //        }
    //public void setRecordAxisCauseOfDeath(String value)
    //        {
    //        // clear all existing eac
    //        Bundle.entry.removeIf(entry -> entry.getResource() instanceof Observation && (((Observation)entry.Resource).getCode().getCoding().get(0).getCode().equals("80357-7")));
    //        if (RecordAxisCauseOfDeathObsList != null)
    //        {
    //        RecordAxisCauseOfDeathObsList.Clear();
    //        }
    //        else
    //        {
    //        RecordAxisCauseOfDeathObsList = new List<Observation>();
    //        }
    //        // Rebuild the list of observations
    //        for((int Position, String Code, boolean Pregnancy) rac:value)
    //        {
    //        if(!isNullOrEmpty(rac.getCode()))
    //        {
    //        Observation ob = new Observation();
    //        ob.setId(UUID.randomUUID().toString());
    //        ob.setMeta(new Meta());
    //        CanonicalType[] recordAxis_profile = { URL.ProfileURL.RecordAxisCauseOfDeath };
    //        ob.getMeta().setProfile(Arrays.asList(recordAxis_profile));
    //        ob.setStatus(Observation.ObservationStatus.FINAL);
    //        ob.setCode(new CodeableConcept(new Coding (CodeSystems.LOINC, "80357-7", "Cause of death record axis code [Automated]")));//, null);
    //        ob.setSubject(new Reference("urn:uuid:" + Decedent.getId()));
    //        AddReferenceToComposition(ob.getId(), "CodedContent");
    //        ob.setEffective(new DateTimeType());
    //        ob.setValue(new CodeableConcept(new Coding (CodeSystems.ICD10, rac.getCode(), null)));//, null);
    //        Observation.ObservationComponentComponent positionComp = new Observation.ObservationComponentComponent();
    //        positionComp.setValue(new Integer(rac.getPosition())));
    //        positionComp.setCode(new CodeableConcept(new Coding (CodeSystems.Component, "position", "Position")));//, null);
    //        ob.getComponent().add(positionComp);

    // Record axis codes have an unusual and obscure handling of a Pregnancy flag, for more information see
    // http://build.fhir.org/ig/HL7/vrdr/branches/master/StructureDefinition-vrdr-record-axis-cause-of-death.html#usage
    //        if (rac.getPregnancy())
    //        {
    //        Observation.ObservationComponentComponent pregComp = new Observation.ObservationComponentComponent();
    //        pregComp.setValue(new BooleanType(true));
    //        pregComp.setCode(new CodeableConcept(new Coding (CodeSystems.Component, "wouldBeUnderlyingCauseOfDeathWithoutPregnancy", "Would be underlying cause of death without pregnancy, if true")));
    //        ob.getComponent().add(pregComp);
    //        }
    //
    //        Bundle.AddResourceEntry(ob, "urn:uuid:" + ob.getId());
    //        RecordAxisCauseOfDeathObsList.add(ob);
    //        }
    //        }
    //        }


    /// <summary>The year NCHS received the death record.</summary>
    /// <value>year, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.ReceiptYear = 2022 </para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Receipt Year: {ExampleDeathRecord.ReceiptYear}");</para>
    /// </example>
    ///[Property("ReceiptYear", Property.Types.Int32, "Coded Content", "Coding Status", true, IGURL.CodingStatusValues, true)]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=codingstatus)", "")]
    private Integer ReceiptYear;
    public Integer getReceiptYear()
    {
        DateType date=CodingStatusValues != null ? (DateType)CodingStatusValues.getParameter("receiptDate") : null;
        return GetDateFragmentOrPartialDate(date, URL.ExtensionURL.DateYear);
    }

    public void setReceiptYear(Integer value)
    {
        if(CodingStatusValues == null)
        {
            CreateCodingStatusValues();
        }
        DateType date=CodingStatusValues != null ? (DateType)CodingStatusValues.getParameter("receiptDate") : null;
        SetPartialDate(date.getExtension().stream().filter(ext->ext.getUrl().equals(URL.ExtensionURL.PartialDate)).findFirst().get(), URL.ExtensionURL.DateYear, value);
    }


    /// <summary>
    /// The month NCHS received the death record.
    /// </summary>
    /// <summary>The month NCHS received the death record.</summary>
    /// <value>month, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.ReceiptMonth = 11 </para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Receipt Month: {ExampleDeathRecord.ReceiptMonth}");</para>
    /// </example>
    ///[Property("ReceiptMonth", Property.Types.Int32, "Coded Content", "Coding Status", true, IGURL.CodingStatusValues, true)]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=codingstatus)", "")]
    private Integer ReceiptMonth;
    public Integer getReceiptMonth()
    {
        DateType date=CodingStatusValues != null ? (DateType)CodingStatusValues.getParameter("receiptDate") : null;
        return GetDateFragmentOrPartialDate(date, URL.ExtensionURL.DateMonth);
    }

    public void setReceiptMonth(Integer value)
    {
        if(CodingStatusValues == null)
        {
            CreateCodingStatusValues();
        }
        DateType date=CodingStatusValues != null ? (DateType)CodingStatusValues.getParameter("receiptDate") : null;
        SetPartialDate(date.getExtension().stream().filter(ext->ext.getUrl().equals(URL.ExtensionURL.PartialDate)).findFirst().get(), URL.ExtensionURL.DateMonth, value);
    }


    /// <summary>The day NCHS received the death record.</summary>
    /// <value>day, or -1 if explicitly unknown, or null if never specified</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.ReceiptDay = 13 </para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Receipt Day: {ExampleDeathRecord.ReceiptDay}");</para>
    /// </example>
    ///[Property("ReceiptDay", Property.Types.Int32, "Coded Content", "Coding Status", true, IGURL.CodingStatusValues, true)]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=codingstatus)", "")]
    private Integer ReceiptDay;
    public Integer getReceiptDay()
    {
        DateType date=CodingStatusValues != null ? (DateType)CodingStatusValues.getParameter("receiptDate") : null;
        return GetDateFragmentOrPartialDate(date, URL.ExtensionURL.DateDay);
    }

    public void setReceiptDay(Integer value)
    {
        if(CodingStatusValues == null)
        {
            CreateCodingStatusValues();
        }
        DateType date=CodingStatusValues != null ? (DateType)CodingStatusValues.getParameter("receiptDate") : null;
        SetPartialDate(date.getExtension().stream().filter(ext->ext.getUrl().equals(URL.ExtensionURL.PartialDate)).findFirst().get(), URL.ExtensionURL.DateDay, value);
    }


    /// <summary>Receipt Date.</summary>
    /// <value>receipt date</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.ReceiptDate = "2018-02-19";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Receipt Date: {ExampleDeathRecord.ReceiptDate}");</para>
    /// </example>
    ///[Property("Receipt Date", Property.Types.StringDateTime, "Coded Content", "Receipt Date.", true, IGURL.CodingStatusValues, true, 25)]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=codingstatus)", "")]
    private String ReceiptDate;
    public String getReceiptDate()
    {
        // We support this legacy-style API entrypoint via the new partial date and time entrypoints
        if(ReceiptYear != null && ReceiptYear !=-1 && ReceiptMonth != null && ReceiptMonth != -1 && ReceiptDay != null && ReceiptDay != -1)
        {
            DateType result= new DateType(ReceiptYear,ReceiptMonth,ReceiptDay);
            return result.toString();
        }
        return null;
    }
    
    public void setReceiptDate(String value)
    {
        // We support this legacy-style API entrypoint via the new partial date and time entrypoints
        OffsetDateTime parsedDate=OffsetDateTime.parse(value);
        if(parsedDate != null)
        {
            ReceiptYear = parsedDate.getYear();
            ReceiptMonth = parsedDate.getMonthValue();
            ReceiptDay = parsedDate.getDayOfMonth();
        }
   }


    /// <summary>
    /// Coder Status; TRX field with no IJE mapping
    /// </summary>
    /// <summary>Coder Status; TRX field with no IJE mapping</summary>
    /// <value>integer code</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.CoderStatus = 3;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Coder STatus {ExampleDeathRecord.CoderStatus}");</para>
    /// </example>
    ///[Property("CoderStatus", Property.Types.Int32, "Coded Content", "Coding Status", true, IGURL.CodingStatusValues, false)]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=codingstatus)", "")]
    public Integer getCoderStatus()
    {
        return this.CodingStatusValues != null ? this.CodingStatusValues.getParameter("coderStatus") != null ? ((IntegerType)this.CodingStatusValues.getParameter("coderStatus")).getValue() : null : null;
    }
    
    public void setCoderStatus(String value)
    {
        if(CodingStatusValues == null)
        {
            CreateCodingStatusValues();
        }
        if(CodingStatusValues.hasParameter("coderStatus")) CodingStatusValues.setParameter("coderStatus", "");
        if(value != null)
        {
            CodingStatusValues.setParameter("coderStatus", value);
        }
    }


    /// <summary>
    /// Shipment Number; TRX field with no IJE mapping
    /// </summary>
    /// <summary>Coder Status; TRX field with no IJE mapping</summary>
    /// <value>String</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.ShipmentNumber = "abc123"";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Shipment Number{ExampleDeathRecord.ShipmentNumber}");</para>
    /// </example>
    ///[Property("ShipmentNumber", Property.Types.String, "Coded Content", "Coding Status", true, IGURL.CodingStatusValues, false)]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=codingstatus)", "")]
    public String getShipmentNumber()
    {
        return this.CodingStatusValues != null ? this.CodingStatusValues.getParameter("shipmentNumber") != null ? ((StringType)this.CodingStatusValues.getParameter("shipmentNumber")).getValue() : null : null;
    }

    public void setShipmentNumber(String value)
    {
        if(isNullOrWhiteSpace(value)){
            return;
        }
        if(CodingStatusValues == null)
        {
            CreateCodingStatusValues();
        }
        if(CodingStatusValues.hasParameter("shipmentNumber")) CodingStatusValues.setParameter("shipmentNumber", "");
        CodingStatusValues.setParameter("shipmentNumber", value);
    }

    /// <summary>
    /// Intentional Reject
    /// </summary>
    /// <summary>Intentional Reject</summary>
    /// <value>String</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; reject = new Map&lt;String, String&gt;();</para>
    /// <para>format.add("code", ValueSets.FilingFormat.electronic);</para>
    /// <para>format.add("system", CodeSystems.IntentionalReject);</para>
    /// <para>format.add("display", "Reject1");</para>
    /// <para>ExampleDeathRecord.IntentionalReject = "reject";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Intentional Reject {ExampleDeathRecord.IntentionalReject}");</para>
    /// </example>
    ///[Property("IntentionalReject", Property.Types.Dictionary, "Coded Content", "Coding Status", true, IGURL.CodingStatusValues, true)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=codingstatus)", "")]
    private Map<String, String> IntentionalReject;
    public Map<String, String> getIntentionalReject()
    {
        CodeableConcept intentionalReject = this.CodingStatusValues != null ? (CodeableConcept)this.CodingStatusValues.getParameter("intentionalReject") : null;
        if(intentionalReject != null)
        {
            return CodeableConceptToMap(intentionalReject);
        }
        return EmptyCodeableMap();
    }

    public void setIntentionalReject(Map<String, String> value)
    {
        if(CodingStatusValues == null)
        {
            CreateCodingStatusValues();
        }
        if(CodingStatusValues.hasParameter("intentionalReject")) CodingStatusValues.setParameter("intentionalReject", "");
        if(value != null)
        {
            CodingStatusValues.setParameter("intentionalReject", MapToCodeableConcept(value));
        }
    }


    /// <summary>Intentional Reject Helper.</summary>
    /// <value>Intentional Reject
    /// <para>"code" - the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.IntentionalRejectHelper = ValueSets.IntentionalReject.Not_Rejected;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Intentional Reject Code: {ExampleDeathRecord.IntentionalRejectHelper}");</para>
    /// </example>
    ///[Property("IntentionalRejectHelper", Property.Types.String, "Intentional Reject Codes", "IntentionalRejectCodes.", false, IGURL.CodingStatusValues, true, 4)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=codingstatus)", "")]
    public String getIntentionalRejectHelper()
    {
        if(IntentionalReject.containsKey("code") && !isNullOrWhiteSpace(IntentionalReject.get("code"))){
            return IntentionalReject.get("code");
        }
        return null;
    }
    
    public void setIntentionalRejectHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("IntentionalReject", value, ValueSets.IntentionalReject.Codes);
        }
    }


    /// <summary>Acme System Reject.</summary>
    /// <value>
    /// <para>"code" - the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; reject = new Map&lt;String, String&gt;();</para>
    /// <para>format.add("code", ValueSets.FilingFormat.electronic);</para>
    /// <para>format.add("system", CodeSystems.SystemReject);</para>
    /// <para>format.add("display", "3");</para>
    /// <para>ExampleDeathRecord.AcmeSystemReject = reject;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Acme System Reject Code: {ExampleDeathRecord.AcmeSystemReject}");</para>
    /// </example>
    
    ///[Property("AcmeSystemReject", Property.Types.Dictionary, "Coded Content", "Coding Status", true, IGURL.CodingStatusValues, true)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=codingstatus)", "")]
    private Map<String, String> AcmeSystemReject;
    public Map<String, String> getAcmeSystemReject()
    {
        CodeableConcept acmeSystemReject = this.CodingStatusValues != null ? (CodeableConcept)this.CodingStatusValues.getParameter("acmeSystemReject");
        if(acmeSystemReject != null)
        {
            return CodeableConceptToMap(acmeSystemReject);
        }
        return EmptyCodeableMap();
     }
    
    public void setAcmeSystemReject(Map<String, String> value){
        if(CodingStatusValues == null){
            CreateCodingStatusValues();
        }
        if(CodingStatusValues.hasParameter("acmeSystemReject"))
            CodingStatusValues.setParameter("acmeSystemReject", "");
        if(value != null)
        {
            CodingStatusValues.setParameter("acmeSystemReject",MapToCodeableConcept(value));
        }
    }


    /// <summary>Acme System Reject.</summary>
    /// <value>
    /// <para>"code" - the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.AcmeSystemRejectHelper = "3";</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Acme System Reject Code: {ExampleDeathRecord.AcmeSystemReject}");</para>
    /// </example>
    ///[Property("AcmeSystemRejectHelper", Property.Types.String, "Acme System Reject Codes", "AcmeSystemRejectCodes.", false, IGURL.CodingStatusValues, true, 4)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=codingstatus)", "")]
    public String getAcmeSystemRejectHelper()
    {
        if(AcmeSystemReject.containsKey("code") && !isNullOrWhiteSpace(ValueSets.AcmeSystemReject.Codes[0][0].toString())){
            return AcmeSystemReject.get("code");
        }
        return null;
    }

    public void setAcmeSystemRejectHelper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("AcmeSystemReject", value, ValueSets.AcmeSystemReject.Codes);
        }
    }


    /// <summary>Transax Conversion Flag</summary>
    /// <value>
    /// <para>"code" - the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Map&lt;String, String&gt; tcf = new Map&lt;String, String&gt;();</para>
    /// <para>tcf.add("code", "3");</para>
    /// <para>tcf.add("system", CodeSystems.TransaxConversion);</para>
    /// <para>tcf.add("display", "Conversion using non-ambivalent table entries");</para>
    /// <para>ExampleDeathRecord.TransaxConversion = tcf;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Transax Conversion Code: {ExampleDeathRecord.TransaxConversion}");</para>
    /// </example>
    ///[Property("TransaxConversion", Property.Types.Dictionary, "Coded Content", "Coding Status", true, IGURL.CodingStatusValues, true)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[PropertyParam("system", "The relevant code system.")]
    ///[PropertyParam("display", "The human readable version of this code.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=codingstatus)", "")]
    private Map<String, String> TransaxConversion;
    public Map<String, String> getTransaxConversion()
    {
        CodeableConcept transaxConversion = this.CodingStatusValues != null ? (CodeableConcept)CodingStatusValues.getParameter("transaxConversion") : null;
        if(transaxConversion != null)
        {
            return CodeableConceptToMap(transaxConversion);
        }
        return EmptyCodeableMap();
    }

    public void setTransaxConversion(Map<String, String> value)
    {
        if(CodingStatusValues == null)
        {
            CreateCodingStatusValues();
        }
        // if(CodingStatusValues.hasParameter("transaxConversion")) CodingStatusValues.setParameter("transaxConversion", "");
        if(value != null)
        {
            CodingStatusValues.addParameter("transaxConversion", MapToCodeableConcept(value));
        }
    }


    /// <summary>TransaxConversion Helper.</summary>
    /// <value>transax conversion code
    /// <para>"code" - the code</para>
    /// </value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.TransaxConversionHelper = ValueSets.TransaxConversion.Conversion_Using_Non_Ambivalent_Table_Entries;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Filing Format: {ExampleDeathRecord.TransaxConversionHelper}");</para>
    /// </example>
    ///[Property("TransaxConversionFlag Helper", Property.Types.String, "Transax Conversion", "TransaxConversion Flag.", false, IGURL.CodingStatusValues, true, 4)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=codingstatus)", "")]
    public String getTransaxConversionHelper()
    {
        if(TransaxConversion.containsKey("code") && !isNullOrWhiteSpace(TransaxConversion.get("code"))){
            return TransaxConversion.get("code");
        }
        return null;
    }

    public void setTransaxConversionHelper(String value)
    {
        if(!isNullOrWhiteSpace(value)){
            SetCodeValue("TransaxConversion", value, ValueSets.TransaxConversion.Codes);
        }
    }


    /// <summary>Race Recode 40  Helper</summary>
    /// <value>Race Recode 40 Helper.</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>ExampleDeathRecord.RaceRecode40Helper = ValueSets.RaceRecode40.AIAN ;</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"Race Recode 40: {ExampleDeathRecord.RaceRecode40Helper}");</para>
    /// </example>
    ///[Property("Race Recode 40 Helper", Property.Types.String, "Coded Content", "Race Recode 40.", false, IGURL.CodedRaceAndEthnicity, false, 34)]
    ///[PropertyParam("code", "The code used to describe this concept.")]
    ///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code='codedraceandethnicity')", "")]
    public String getRaceRecode40Helper()
    {
        if(RaceRecode40.containsKey("code") && !isNullOrWhiteSpace(RaceRecode40.get("code")))
        {
            return RaceRecode40.get("code");
        }
        return null;
    }

    public void setRaceRecode40Helper(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            SetCodeValue("RaceRecode40", value, ValueSets.RaceRecode40.Codes);
        }
    }

/// <summary>Entity Axis Cause Of Death
/// <para>Note that record axis codes have an unusual and obscure handling of a Pregnancy flag, for more information see
/// http://build.fhir.org/ig/HL7/vrdr/branches/master/StructureDefinition-vrdr-record-axis-cause-of-death.html#usage></para>
/// </summary>
/// <value>Entity-axis codes</value>
/// <example>
/// <para>// Setter:</para>
/// <para> ExampleDeathRecord.EntityAxisCauseOfDeath = new [] {(LineNumber: 2, Position: 1, Code: "T27.3", ECode: true)};</para>
/// <para>// Getter:</para>
/// <para>Console.WriteLine($"First Entity Axis Code: {ExampleDeathRecord.EntityAxisCauseOfDeath.ElementAt(0).Code}");</para>
/// </example>
///[Property("Entity Axis Cause of Death", Property.Types.Tuple4Arr, "Coded Content", "", true, IGURL.EntityAxisCauseOfDeath, false, 50)]
///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=80356-9)", "")]
//public Iterable<(int LineNumber, int Position, String Code, boolean ECode)> getEntityAxisCauseOfDeath()
//        {
//        List<(int LineNumber, int Position, String Code, boolean ECode)> eac = new List<(int LineNumber, int Position, String Code, boolean ECode)>();
//        if (EntityAxisCauseOfDeathObsList != null)
//        {
//        for(Observation ob:EntityAxisCauseOfDeathObsList)
//        {
//        Integer lineNumber = null;
//        Integer position = null;
//        String icd10code = null;
//        boolean ecode = false;
//        Observation.ObservationComponentComponent positionComp = ob.getComponent().stream().filter(c -> c.getCode().getCoding().get(0).getCode().equals("position").stream().filter();
//        if (positionComp != null && positionComp.getValue() != null)
//        {
//        position = ((IntegerType)positionComp.getValue()).getValue();
//        }
//        Observation.ObservationComponentComponent lineNumComp = ob.getComponent().stream().filter()c -> c.getCode().getCoding().get(0).getCode().equals("lineNumber").stream().filter();
//        if (lineNumComp != null && lineNumComp.getValue() != null)
//        {
//        lineNumber = ((IntegerType)lineNumComp.getValue()).getValue();
//        }
//        CodeableConcept valueCC = (CodeableConcept)ob.getValue();
//        if (valueCC != null && valueCC.getCoding() != null && valueCC.getCoding().size() > 0)
//        {
//        icd10code = valueCC.getCoding().get(0).getCode().trim();
//        }
//        Observation.ObservationComponentComponent ecodeComp = ob.getComponent().stream().filter(c -> c.getCode().getCoding().get(0).getCode().equals("eCodeIndicator").stream().filter();
//        if (ecodeComp != null && ecodeComp.getValue() != null)
//        {
//        ecode = (boolean)((BooleanType)ecodeComp.getValue()).getValue();
//        }
//        if (lineNumber != null && position != null && icd10code != null)
//        {
//        eac.add((LineNumber: (int)lineNumber, Position: (int)position, Code: icd10code, ECode: ecode));
//        }
//        }
//        }
//        return eac.OrderBy(element -> element.getLineNumber()).ThenBy(element -> element.getPosition());
//        }
//public void setEntityAxisCauseOfDeath(String value)
//        {
//        // clear all existing eac
//        Bundle.Entry().removeIf(entry -> entry.Resource is Observation && (((Observation)entry.Resource).Code.Coding.First().Code == "80356-9"));
//        if (EntityAxisCauseOfDeathObsList != null)
//        {
//        EntityAxisCauseOfDeathObsList.Clear();
//        }
//        else
//        {
//        EntityAxisCauseOfDeathObsList = new List<Observation>();
//        }
//        // Rebuild the list of observations
//        for((int LineNumber, int Position, String Code, boolean ECode) eac:value)
//        {
//        if(!isNullOrEmpty(eac.Code))
//        {
//        Observation ob = new Observation();
//        ob.setId(UUID.randomUUID().toString());
//        ob.setMeta(new Meta());
//        CanonicalType[] entityAxis_profile = { URL.ProfileURL.EntityAxisCauseOfDeath };
//        ob.getMeta().setProfile(entityAxis_profile;
//        ob.setStatus(Observation.ObservationStatus.FINAL);
//        ob.setCode(new CodeableConcept(new Coding(CodeSystems.LOINC, "80356-9", "Cause of death entity axis code [Automated]")));//, null);
//        ob.setSubject(new Reference("urn:uuid:" + Decedent.getId()));
//        AddReferenceToComposition(ob.getId(), "CodedContent");
//
//        ob.setEffective(new DateTimeType());
//        ob.setValue(new CodeableConcept(new Coding (CodeSystems.ICD10, eac.Code, null)));//, null));
//        Observation.ObservationComponentComponent lineNumComp = new Observation.ObservationComponentComponent();
//        lineNumComp.setValue(new Integer(eac.LineNumber);
//        lineNumComp.setCode(new CodeableConcept(new Coding (CodeSystems.Component, "lineNumber", "lineNumber")));//, null);
//        ob.getComponent().add(lineNumComp);
//
//        Observation.ObservationComponentComponent positionComp = new Observation.ObservationComponentComponent();
//        positionComp.setValue(new Integer(eac.Position);
//        positionComp.setCode(new CodeableConcept(new Coding(CodeSystems.Component, "position", "Position")));//, null);
//        ob.getComponent().add(positionComp);
//
//        Observation.ObservationComponentComponent eCodeComp = new Observation.ObservationComponentComponent();
//        eCodeComp.setValue(new BooleanType(eac.ECode));
//        eCodeComp.setCode(new CodeableConcept(new Coding(CodeSystems.Component, "eCodeIndicator", "eCodeIndicator")));//, null);
//        ob.getComponent().add(eCodeComp);
//
//        Bundle.AddResourceEntry(ob, "urn:uuid:" + ob.getId());
//        EntityAxisCauseOfDeathObsList.add(ob);
//        }
//        }
//        }


/// <summary>Record Axis Cause Of Death</summary>
/// <value>record-axis codes</value>
/// <example>
/// <para>// Setter:</para>
/// <para>Tuple&lt;String, String, String&gt;[] eac = new Tuple&lt;String, String, String&gt;{Tuple.Create("position", "code", "pregnancy")}</para>
/// <para>ExampleDeathRecord.RecordAxisCauseOfDeath = new [] { (Position: 1, Code: "T27.3", Pregnancy: true) };</para>
/// <para>// Getter:</para>
/// <para>Console.WriteLine($"First Record Axis Code: {ExampleDeathRecord.RecordAxisCauseOfDeath.ElememtAt(0).Code}");</para>
/// </example>
///[Property("Record Axis Cause Of Death", Property.Types.Tuple4Arr, "Coded Content", "", true, IGURL.RecordAxisCauseOfDeath, false, 50)]
///[FHIRPath("Bundle.entry.resource.stream().filter($this is Observation).stream().filter(code.coding.code=80357-7)", "")]
//public Iterable<(int Position, String Code, boolean Pregnancy)> getRecordAxisCauseOfDeath()
//        {
//
//        List<(int Position, String Code, boolean Pregnancy)> rac = new List<(int Position, String Code, boolean Pregnancy)>();
//        if (RecordAxisCauseOfDeathObsList != null)
//        {
//        for(Observation ob:RecordAxisCauseOfDeathObsList)
//        {
//        Integer position = null;
//        String icd10code = null;
//        boolean pregnancy = false;
//        Observation.ObservationComponentComponent positionComp = ob.getComponent().stream().filter(c -> c.getCode().getCoding().get(0).getCode().equals("position")).findFirst().get();
//        if (positionComp != null && positionComp.getValue() != null)
//        {
//        position = ((IntegerType)positionComp.getValue()).getValue();
//        }
//        CodeableConcept valueCC = (CodeableConcept)ob.getValue();
//        if (valueCC != null && valueCC.getCoding() != null && valueCC.getCoding().size() > 0)
//        {
//        icd10code = valueCC.getCoding().get(0).getCode();
//        }
//        Observation.ObservationComponentComponent pregComp = ob.getComponent().stream().filter(c -> c.getCode().getCoding().get(0).getCode().equals("wouldBeUnderlyingCauseOfDeathWithoutPregnancy")).findFirst().get();
//        if (pregComp != null && pregComp.getValue() != null)
//        {
//        pregnancy = ((BooleanType)pregComp.getValue()).getValue();
//        }
//        if (position != null && icd10code != null)
//        {
//        rac.add((Position: (int)position, Code: icd10code, Pregnancy: pregnancy));
//        }
//        }
//        }
//        return rac.OrderBy(entry -> entry.Position);
//        }
//public void setRecordAxisCauseOfDeath(String value)
//        {
//        // clear all existing eac
//        Bundle.entry.removeIf(entry -> entry.getResource instanceof Observation && (((Observation)entry.Resource).Code.Coding.First().Code == "80357-7"));
//        if (RecordAxisCauseOfDeathObsList != null)
//        {
//        RecordAxisCauseOfDeathObsList.Clear();
//        }
//        else
//        {
//        RecordAxisCauseOfDeathObsList = new List<Observation>();
//        }
//        // Rebuild the list of observations
//        for((int Position, String Code, boolean Pregnancy) rac:value)
//        {
//        if(!isNullOrEmpty(rac.getCode()))
//        {
//        Observation ob = new Observation();
//        ob.setId(UUID.randomUUID().toString());
//        ob.setMeta(new Meta());
//        CanonicalType[] recordAxis_profile = { URL.ProfileURL.RecordAxisCauseOfDeath };
//        ob.getMeta().setProfile(Arrays.asList(recordAxis_profile));
//        ob.setStatus(Observation.ObservationStatus.FINAL);
//        ob.setCode(new CodeableConcept(new Coding (CodeSystems.LOINC, "80357-7", "Cause of death record axis code [Automated]")));//, null);
//        ob.setSubject(new Reference("urn:uuid:" + Decedent.getId()));
//        AddReferenceToComposition(ob.getId(), "CodedContent");
//        ob.setEffective(new DateTimeType());
//        ob.setValue(new CodeableConcept(new Coding (CodeSystems.ICD10, rac.getCode(), null)));//, null);
//        Observation.ObservationComponentComponent positionComp = new Observation.ObservationComponentComponent();
//        positionComp.setValue(new Integer(rac.getPosition())));
//        positionComp.setCode(new CodeableConcept(new Coding (CodeSystems.Component, "position", "Position")));//, null);
//        ob.getComponent().add(positionComp);
//
//        // Record axis codes have an unusual and obscure handling of a Pregnancy flag, for more information see
//        // http://build.fhir.org/ig/HL7/vrdr/branches/master/StructureDefinition-vrdr-record-axis-cause-of-death.html#usage
//        if (rac.getPregnancy())
//        {
//        Observation.ObservationComponentComponent pregComp = new Observation.ObservationComponentComponent();
//        pregComp.setValue(new BooleanType(true));
//        pregComp.setCode(new CodeableConcept(new Coding (CodeSystems.Component, "wouldBeUnderlyingCauseOfDeathWithoutPregnancy", "Would be underlying cause of death without pregnancy, if true")));
//        ob.getComponent().add(pregComp);
//        }
//
//        Bundle.AddResourceEntry(ob, "urn:uuid:" + ob.getId());
//        RecordAxisCauseOfDeathObsList.add(ob);
//        }
//        }
//        }


        }