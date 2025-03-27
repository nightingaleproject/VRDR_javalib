
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class AdministrativeGenderVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-administrative-gender-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/administrative-gender","male","Male")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/administrative-gender","female","Female")),
        new CodeableConcept().addCoding(new Coding("http://hl7.org/fhir/administrative-gender","unknown","unknown"))
    ));
};