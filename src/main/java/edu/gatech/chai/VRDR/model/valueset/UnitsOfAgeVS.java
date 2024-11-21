
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class UnitsOfAgeVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-units-of-age-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://unitsofmeasure.org","min","Minutes")),
        new CodeableConcept().addCoding(new Coding("http://unitsofmeasure.org","d","Days")),
        new CodeableConcept().addCoding(new Coding("http://unitsofmeasure.org","h","Hours")),
        new CodeableConcept().addCoding(new Coding("http://unitsofmeasure.org","mo","Months")),
        new CodeableConcept().addCoding(new Coding("http://unitsofmeasure.org","a","Years")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","UNK","unknown"))
    ));
};