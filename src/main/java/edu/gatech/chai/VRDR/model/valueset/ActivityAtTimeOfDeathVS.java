
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class ActivityAtTimeOfDeathVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-activity-at-time-of-death-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-activity-at-time-of-death-cs","0","While engaged in sports activity")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-activity-at-time-of-death-cs","1","While engaged in leisure activities.")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-activity-at-time-of-death-cs","2","While working for income")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-activity-at-time-of-death-cs","3","While engaged in other types of work")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-activity-at-time-of-death-cs","4","While resting, sleeping, eating, or engaging in other vital activities")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-activity-at-time-of-death-cs","8","While engaged in other specified activities.")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-activity-at-time-of-death-cs","9","During unspecified activity")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","UNK","unknown"))
    ));
};