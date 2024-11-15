
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class MaritalStatusVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-marital-status-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-MaritalStatus","D","Divorced")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-MaritalStatus","L","Legally Separated")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-MaritalStatus","M","Married")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-MaritalStatus","S","Never Married")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-MaritalStatus","W","Widowed")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","UNK","unknown"))
    ));
};