package edu.gatech.VRDR;

import edu.gatech.chai.VRDR.model.util.BuildDCD;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

import edu.gatech.chai.VRDR.model.DeathCertificateDocument;

public class BundleofBundlesTest {
	public void testBundleOfBundles() {
		Bundle outerBundle = new Bundle();
		DeathCertificateDocument deathRecordDocument = BuildDCD.buildExampleDeathCertificateDocument();
		outerBundle.addEntry(new BundleEntryComponent().setResource(deathRecordDocument));		
	}
}