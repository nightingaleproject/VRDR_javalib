
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class YesNoUnknownVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-yes-no-unknown-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v2-0136","N","No")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v2-0136","Y","Yes")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","UNK","unknown"))
    ));
};