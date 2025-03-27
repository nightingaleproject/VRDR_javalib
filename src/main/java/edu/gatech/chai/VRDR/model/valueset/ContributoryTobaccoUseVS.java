
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class ContributoryTobaccoUseVS {
    public static final String url = "http://hl7.org/fhir/us/vrdr/ValueSet/vrdr-contributory-tobacco-use-vs";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
                new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","373066001","Yes")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","373067005","No")),
        new CodeableConcept().addCoding(new Coding("http://snomed.info/sct","2931005","Probably")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","UNK","Unknown")),
        new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor","NI","no information"))
    ));
};