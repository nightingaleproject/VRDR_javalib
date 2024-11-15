import sys
import os
from pathlib import Path
import json
from typing import List

# import fhir.resources.codeableconcept
# import fhir.resources.codesystem as cs
from fhir.resources.valueset import ValueSet
from fhir.resources.codeableconcept import CodeableConcept
from fhir.resources.coding import Coding
from fhir.resources.codesystem import CodeSystem
from pprint import pprint

import fhir.resources

# package edu.gatech.chai.VRDR.model.util;

# import java.io.File;
# import java.io.FileReader;
# import java.util.Arrays;
# import java.util.HashSet;

# import org.hl7.fhir.r4.model.CodeableConcept;
# import org.hl7.fhir.r4.model.Coding;

# public class ValueSets {
#     public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
# 			new CodeableConcept().addCoding(new Coding(valueSetSystemUrl,"0","While engaged in sports activity")),
# 			new CodeableConcept().addCoding(new Coding(valueSetSystemUrl,"1","While engaged in leisure activities.")),
# 			new CodeableConcept().addCoding(new Coding(valueSetSystemUrl,"2","While working for income")),
# 			new CodeableConcept().addCoding(new Coding(valueSetSystemUrl,"3","While engaged in other types of work")),
# 			new CodeableConcept().addCoding(new Coding(valueSetSystemUrl,"4","While resting, sleeping, eating, or engaging in other vital activities")),
# 			new CodeableConcept().addCoding(new Coding(valueSetSystemUrl,"8","While engaged in other specified activities.")),
# 			new CodeableConcept().addCoding(new Coding(valueSetSystemUrl,"9","During unspecified activity")),
# 			CommonUtil.unknownCode
# 			));


def help():
    print("Usage: python generate_valueset_lookups_from_ig.py <path_to_ig>")
    sys.exit(1)


def main():
    ig_resource_path = Path(os.path.join(sys.argv[1],"fsh-generated","resources"))

    for vs_file in ig_resource_path.rglob("ValueSet-vrdr-*.json"):
        vs = ValueSet.parse_file(vs_file)
        print(f"=== {vs_file} ===")

        with open(
            os.path.join(
                Path(__file__).parent.parent,
                "src/main/java/edu/gatech/chai/VRDR/model/valueset",
                f"{vs.name}.java",
            ),
            "w",
        ) as f:
            f.write(
                f"""
package edu.gatech.chai.VRDR.model.valueset;
import java.util.Arrays;
import java.util.HashSet;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
"""
            )

            # pprint(vs.compose.include)

            codings = []

            for compose_include in vs.compose.include:
                if compose_include.concept:
                    for concept in compose_include.concept:
                        c = Coding()
                        c.system = compose_include.system
                        c.code = concept.code
                        c.display = concept.display
                        codings.append(c)
                else:
                    # no specified concepts, need to include the complete coding system
                    for cs_file in ig_resource_path.rglob("CodeSystem-*.json"):
                        cs = CodeSystem.parse_file(cs_file)
                        if cs.url == compose_include.system:
                            for concept in cs.concept:
                                c = Coding()
                                c.system = cs.url
                                c.code = concept.code
                                c.display = concept.display
                                codings.append(c)
            print(f"length of codings after include: {len(codings)}")

            if vs.compose.exclude:
                for compose_exclude in vs.compose.exclude:
                    for concept in compose_exclude.concept:
                        codings = [
                            c
                            for c in codings
                            if not (
                                c.code == concept.code
                                and c.system == compose_exclude.system
                            )
                        ]

            print(f"length of codings after exclude: {len(codings)}")

            f.write(
                f"""
public class {vs.name} {{
    public static final String url = "{vs.url}";
    public static final HashSet<CodeableConcept> valueSet = new HashSet<>(Arrays.asList(
        """
            )
            f.write(
                ",\n".join(
                    [
                        f"""        new CodeableConcept().addCoding(new Coding("{c.system}","{c.code}","{c.display}"))"""
                        for c in codings
                    ]
                )
            )
            f.write(
                """
    ));
};"""
            )


if __name__ == "__main__":
    # args = sys.argv[1:]  #

    if len(sys.argv[1:]) != 1:
        help()
        exit(1)

    main()
    # print(
    #     os.path.join(
    #         Path(__file__).parent.parent,
    #         "src/main/java/edu/gatech/chai/VRDR/model/valueset",
    #     )
    # )
