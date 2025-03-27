
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class TransaxConversionVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-transax-conversion-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-transax-conversion-cs","3","Conversion using non-ambivalent table entries")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-transax-conversion-cs","4","Conversion using ambivalent table entries")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-transax-conversion-cs","5","Duplicate entity-axis codes deleted; no other action involved")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/us/vrdr/CodeSystem/vrdr-transax-conversion-cs","6","Artificial code conversion; no other action"))
    ));
};