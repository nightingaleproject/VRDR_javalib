
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class CertifierTypesVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-certifier-types-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","455381000124109","Death certification by medical examiner or coroner (procedure)")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","434641000124105","Death certification and verification by physician (procedure)")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","434651000124107","Death certification by physician (procedure)")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","OTH","Other (Specify)"))
    ));
};