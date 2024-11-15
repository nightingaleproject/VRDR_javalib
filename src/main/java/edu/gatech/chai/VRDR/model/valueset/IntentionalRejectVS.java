
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class IntentionalRejectVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-intentional-reject-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-intentional-reject-cs","1","Reject1")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-intentional-reject-cs","2","Reject2")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-intentional-reject-cs","3","Reject3")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-intentional-reject-cs","4","Reject4")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-intentional-reject-cs","5","Reject5")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-intentional-reject-cs","9","Reject9"))
    ));
};