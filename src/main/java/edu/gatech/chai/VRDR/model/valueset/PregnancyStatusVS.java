
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class PregnancyStatusVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-pregnancy-status-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-pregnancy-status-cs","1","Not pregnant within past year")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-pregnancy-status-cs","2","Pregnant at time of death")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-pregnancy-status-cs","3","Not pregnant, but pregnant within 42 days of death")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-pregnancy-status-cs","4","Not pregnant, but pregnant 43 days to 1 year before death")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-pregnancy-status-cs","7","Not reported on certificate")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-pregnancy-status-cs","9","Unknown if pregnant within the past year")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","NA","None"))
    ));
};