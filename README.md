# VRDR_javalib

Java library implementation of the FHIR VRDR and FHIR VRDR Messaging standards.  This repository includes Java code for:

- Producing and consuming the Vital Records Death Reporting (VRDR) Health Level 7 (HL7) Fast Healthcare Interoperability Resources (FHIR) standard. [Click here to view the FHIR Implementation Guide STU2.1](http://hl7.org/fhir/us/vrdr/).
- Producing and consuming FHIR messages for the exchange of VRDR documents.
- Example VRDR FHIR documents and messages in JSON and XML formats
- Numerous test cases illustrating proper usage of the library

# Standards

This java library implements the FHIR VRDR and VRDR Messaging standards ```http://hl7.org/fhir/us/vrdr/```, explained in the implementation guides below:

* VRDR IG: ```http://build.fhir.org/ig/HL7/vrdr/branches/master/index.html```
* VRDR Messaging IG: ```http://build.fhir.org/ig/nightingaleproject/vital_records_fhir_messaging_ig/branches/main/index.html```

# Info

This project uses the hapi-fhir java libraries extensively to create its representation.

It is recommended to have a strong understanding of the resource extension section of the hapi-fhir library before diving into the code. Info can be found here. https://hapifhir.io/hapi-fhir/docs/model/custom_structures.html

This project is made up of several major components:

* ```edu.gatech.VRDR.model``` Contains all the custom resources needed for the VRDR IG.
* ```edu.gatech.VRDR.model.util``` Contains utility methods like custom codes and static definitions for data structure resources.
* ```edu.gatech.VRDR.messaging``` Contains all the custom resources needed for the VRDR Messaging IG.
* ```edu.gatech.VRDR.messaging.util``` Contains utility methods like custom codes and static definitions for messaging resources.

You can refer to the ```src/test``` directory for an in-depth unit test example on how to construct a full EDRS record, and serialize it to JSON

# Build

Build the library using maven:

* ```mvn clean install```

Optionally, build the command line tool using maven command:

* ```mvn clean package appassembler:assemble```

# Running

For the library, simply place the compiled target jar in your classpath and import the VRDR classes to use the library.

Optionally, use the command line tool to generate example records for Canary testing:

* ```sh target/appassembler/bin/app create submission canary-submission-test.json```

* ```sh target/appassembler/bin/app create update canary-update-test.json```

* ```sh target/appassembler/bin/app create void canary-void-test.json```

You can also output XML instead of JSON by passing the ```--xml``` option.

# Publishing a Version

To create a new release of the VRDR Java Library:

1. Update the CHANGELOG.md file with information on what is changing in the release
1. Create a pull request and merge the above changes to master
1. Create a GitHub release
    1. Go to the Releases page
    1. Click on "Draft a new release"
    1. Create a new release version on the tag and release (e.g., v1.4.0-STU2.1)
    1. Copy the information from the CHANGELOG.md file from this version into the release description
    1. Do not check the "pre-release" button, even for preview releases, since those don't show up on the main GitHub page

