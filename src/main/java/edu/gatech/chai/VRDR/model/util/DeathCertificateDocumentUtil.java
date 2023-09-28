package edu.gatech.chai.VRDR.model.util;

import edu.gatech.chai.VRDR.model.URL;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.TimeType;

import java.lang.reflect.Field;
import java.time.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Resource;

import static org.hl7.fhir.r4.model.Enumerations.*;

public class DeathCertificateDocumentUtil {
	public static final String auxillaryStateIndentifierUrl = "http://hl7.org/fhir/us/vrdr/StructureDefinition/AuxiliaryStateIdentifier1";
	public static final String certificateNumberUrl = "http://hl7.org/fhir/us/vrdr/StructureDefinition/CertificateNumber";

	/// <summary>Check if a Map is empty or a default empty Map (all values are null or empty Strings)</summary>
	/// <param name="dict">represents a code.</param>
	/// <returns>A boolean identifying whether the provided Map is empty or default.</returns>


//	public static boolean isMapEmptyOrDefault(Map<String, String> map)
//	{
//		return map.size() == 0 || map.values().stream().allMatch(v -> v == null || v.equals(""));
//	}

	public static boolean isMapEmptyOrDefault(Map<String, StringType> map)
	{
		return map.size() == 0 || map.values().stream().allMatch(v -> v == null || v.equals(""));
	}


	/// <summary>Convert a FHIR Coding to a "code" Map</summary>
	/// <param name="coding">a FHIR Coding.</param>
	/// <returns>the corresponding Map representation of the code.</returns>
	public static Map<String, StringType> CodingToMap(Coding coding)
	{
		Map<String, StringType> map = EmptyCodeMap();
		if (coding != null)
		{
			if (isNullOrEmpty(coding.getCode()))
			{
				map.put("code", new StringType(coding.getCode()));
			}
			if (isNullOrEmpty(coding.getSystem()))
			{
				map.put("system", new StringType(coding.getSystem()));
			}
			if (isNullOrEmpty(coding.getDisplay()))
			{
				map.put("display", new StringType(coding.getDisplay()));
			}
		}
		return map;
	}

	/// <summary>Convert a FHIR CodableConcept to a "code" Map</summary>
	/// <param name="codeableConcept">a FHIR CodeableConcept.</param>
	/// <returns>the corresponding Map representation of the code.</returns>
	public static Map<String, StringType> CodeableConceptToMap(CodeableConcept codeableConcept)
	{
		if (codeableConcept != null && codeableConcept.getCoding() != null)
		{
			Coding coding = codeableConcept.getCoding().get(0);
			Map codeMap = CodingToMap(coding);
			if (codeableConcept != null && isNullOrEmpty(codeableConcept.getText()))
			{
				codeMap.put("text", codeableConcept.getText());
			}
			return codeMap;
		}
		else
		{
			return EmptyCodeableMap();
		}
	}

	/// <summary>Convert an "address" Map to a FHIR Address.</summary>
	/// <param name="dict">represents an address.</param>
	/// <returns>the corresponding FHIR Address representation of the address.</returns>
	private Address MapToAddress(Map<String, StringType> map)
	{
		Address address = new Address();

		if (map != null)
		{
			List<StringType> lines = new ArrayList();
			if (map.containsKey("addressLine1") && isNullOrEmpty(String.valueOf(map.get("addressLine1"))))
			{
				lines.add(map.get("addressLine1"));
			}
			if (map.containsKey("addressLine2") && isNullOrEmpty(String.valueOf(map.get("addressLine2"))))
			{
				lines.add(map.get("addressLine2"));
			}
			if (lines.size() > 0)
			{
				address.setLine(lines);
			}
			if (map.containsKey("addressCityC") && isNullOrEmpty(String.valueOf(map.get("addressCityC"))))
			{
				Extension cityCode = new Extension();
				cityCode.setUrl(URL.ExtensionURL.CityCode);
				cityCode.setValue(new IntegerType(Integer.parseInt(String.valueOf(map.get("addressCityC")))));
				address.setCityElement(new StringType());
				address.getCityElement().getExtension().add(cityCode);
			}
			if (map.containsKey("addressCity") && isNullOrEmpty(String.valueOf(map.get("addressCity"))))
			{
				if (address.getCityElement() != null)
				{
					address.getCityElement().setValue(String.valueOf(map.get("addressCity")));
				}
				else
				{
					address.setCity(String.valueOf(map.get("addressCity")));
				}

			}
			if (map.containsKey("addressCountyC") && isNullOrEmpty(String.valueOf(map.get("addressCountyC"))))
			{
				Extension countyCode = new Extension();
				countyCode.setUrl(URL.ExtensionURL.DistrictCode);
				countyCode.setValue(new IntegerType(Integer.parseInt(String.valueOf(map.get("addressCountyC")))));
				address.setDistrictElement(new StringType());
				address.getDistrictElement().getExtension().add(countyCode);
			}
			if (map.containsKey("addressCounty") && isNullOrEmpty(String.valueOf(map.get("addressCounty"))))
			{
				if (address.getDistrictElement() != null)
				{
					address.getDistrictElement().setValue(String.valueOf(map.get("addressCounty")));
				}
				else
				{
					address.setDistrict(String.valueOf(map.get("addressCounty")));
				}
			}
			if (map.containsKey("addressState") && isNullOrEmpty(String.valueOf(map.get("addressState"))))
			{
				address.setState(String.valueOf(map.get("addressState")));
			}
			// Special address field to support the jurisdiction extension custom to VRDR to support YC (New York City)
			// as used in the DeathLocationLoc
			if (map.containsKey("addressJurisdiction") && isNullOrEmpty(String.valueOf(map.get("addressJurisdiction"))))
			{
				if (address.getStateElement() == null)
				{
					address.setStateElement(new StringType());
				}
				address.getStateElement().getExtension().removeIf(ext -> ext.getUrl().equals(URL.ExtensionURL.LocationJurisdictionId));
				Extension extension = new Extension(URL.ExtensionURL.LocationJurisdictionId, map.get("addressJurisdiction"));
				address.getStateElement().getExtension().add(extension);
			}
			if (map.containsKey("addressZip") && isNullOrEmpty(String.valueOf(map.get("addressZip"))))
			{
				address.setPostalCode(String.valueOf(map.get("addressZip")));
			}
			if (map.containsKey("addressCountry") && isNullOrEmpty(String.valueOf(map.get("addressCountry"))))
			{
				address.setCountry(String.valueOf(map.get("addressCountry")));
			}
			if (map.containsKey("addressStnum") && isNullOrEmpty(String.valueOf(map.get("addressStnum"))))
			{
				Extension stnum = new Extension();
				stnum.setUrl(URL.ExtensionURL.StreetNumber);
				stnum.setValue(map.get("addressStnum"));
				address.getExtension().add(stnum);
			}
			if (map.containsKey("addressPredir") && isNullOrEmpty(String.valueOf(map.get("addressPredir"))))
			{
				Extension predir = new Extension();
				predir.setUrl(URL.ExtensionURL.PreDirectional);
				predir.setValue(map.get("addressPredir"));
				address.getExtension().add(predir);
			}
			if (map.containsKey("addressStname") && isNullOrEmpty(String.valueOf(map.get("addressStname"))))
			{
				Extension stname = new Extension();
				stname.setUrl(URL.ExtensionURL.StreetName);
				stname.setValue(map.get("addressStname"));
				address.getExtension().add(stname);
			}
			if (map.containsKey("addressStdesig") && isNullOrEmpty(String.valueOf(map.get("addressStdesig"))))
			{
				Extension stdesig = new Extension();
				stdesig.setUrl(URL.ExtensionURL.StreetDesignator);
				stdesig.setValue(map.get("addressStdesig"));
				address.getExtension().add(stdesig);
			}
			if (map.containsKey("addressPostdir") && isNullOrEmpty(String.valueOf(map.get("addressPostdir"))))
			{
				Extension postdir = new Extension();
				postdir.setUrl(URL.ExtensionURL.PostDirectional);
				postdir.setValue(map.get("addressPostdir"));
				address.getExtension().add(postdir);
			}
			if (map.containsKey("addressUnitnum") && isNullOrEmpty(String.valueOf(map.get("addressUnitnum"))))
			{
				Extension unitnum = new Extension();
				unitnum.setUrl(URL.ExtensionURL.UnitOrAptNumber);
				unitnum.setValue(map.get("addressUnitnum"));
				address.getExtension().add(unitnum);
			}

		}
		return address;
	}


	/// <summary>Convert a Date Part Extension to an Array.</summary>
	/// <param name="datePartAbsent">a Date Part Extension.</param>
	/// <returns>the corresponding array representation of the date parts.</returns>
	private List<List<String>> DatePartsToArray(Extension datePartAbsent)
	{
		List<List<String>> dateParts = new ArrayList<List<String>>();
		if (datePartAbsent != null)
		{
			Extension yearAbsentPart = datePartAbsent.getExtension().stream().filter(ext -> ext.getUrl().equals("year-absent-reason")).findFirst().get();
			Extension monthAbsentPart = datePartAbsent.getExtension().stream().filter(ext -> ext.getUrl().equals("month-absent-reason")).findFirst().get();
			Extension dayAbsentPart = datePartAbsent.getExtension().stream().filter(ext -> ext.getUrl().equals("day-absent-reason")).findFirst().get();
			Extension yearPart = datePartAbsent.getExtension().stream().filter(ext -> ext.getUrl().equals("date-year")).findFirst().get();
			Extension monthPart = datePartAbsent.getExtension().stream().filter(ext -> ext.getUrl().equals("date-month")).findFirst().get();
			Extension dayPart = datePartAbsent.getExtension().stream().filter(ext -> ext.getUrl().equals("date-day")).findFirst().get();


			// Year part
			if (yearAbsentPart != null)
			{
				dateParts.add(new ArrayList<String>(){{add("year-absent-reason"); add(yearAbsentPart.getValue().toString());}});
			}
			if (yearPart != null)
			{
				dateParts.add(new ArrayList<String>(){{add("date-year"); add(yearPart.getValue().toString());}});

			}
			// Month part
			if (monthAbsentPart != null)
			{
				dateParts.add(new ArrayList<String>(){{add("month-absent-reason"); add(monthAbsentPart.getValue().toString());}});

			}
			if (monthPart != null)
			{
				dateParts.add(new ArrayList<String>(){{add("date-month"); add(monthPart.getValue().toString());}});

			}
			// Day Part
			if (dayAbsentPart != null)
			{
				dateParts.add(new ArrayList<String>(){{add("day-absent-reason"); add(dayAbsentPart.getValue().toString());}});

			}
			if (dayPart != null)
			{
				dateParts.add(new ArrayList<String>(){{add("date-day"); add(dayPart.getValue().toString());}});

			}
		}
		return dateParts;
	}

	/// <summary>Convert an element to an integer or code depending on if the input element is a date part.</summary>
	/// <param name="pair">A key value pair, the key will be used to identify whether the element is a date part.</param>
	private Element DatePartToIntegerOrCode(List<String> pair)
	{
		if (pair.get(0) == "date-year" || pair.get(0) == "date-month" || pair.get(0) == "date-day")
		{
			return new IntegerType(pair.get(1));
		}
		else
		{
			return new Coding().setCode(pair.get(1));
		}
	}

	/// <summary>Convert a FHIR Address to an "address" Map.</summary>
	/// <param name="addr">a FHIR Address.</param>
	/// <returns>the corresponding Map representation of the FHIR Address.</returns>
	public static Map<String, StringType> addressToMap(Address addr)
	{
		Map<String, StringType> map = EmptyAddrMap();
		if (addr != null)
		{
			if (addr.getLine() != null && addr.getLine().size() > 0)
			{
				map.put("addressLine1", addr.getLine().get(0));
			}

			if (addr.getLine() != null && addr.getLine().size() > 1)
			{
				map.put("addressLine2", addr.getLine().get(addr.getLine().size()-1));
			}

			if (addr.getCityElement() != null)
			{
				Extension cityCode = addr.getCityElement().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.CityCode)).findFirst().get();
				if (cityCode != null)
				{
					map.put("addressCityC", new StringType(cityCode.getValue().fhirType()));
				}
			}

			if (addr.getDistrictElement() != null)
			{
				Extension districtCode = addr.getDistrictElement().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.DistrictCode)).findFirst().get();
				if (districtCode != null)
				{
					map.put("addressCountyC", new StringType(districtCode.getValue().fhirType()));
				}
			}

			Extension stnum = addr.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.StreetNumber)).findFirst().get();
			if (stnum != null)
			{
				map.put("addressStnum", new StringType(stnum.getValue().fhirType()));
			}

			Extension predir = addr.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PreDirectional)).findFirst().get();
			if (predir != null)
			{
				map.put("addressPredir", new StringType(predir.getValue().fhirType()));
			}

			Extension stname = addr.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.StreetName)).findFirst().get();
			if (stname != null)
			{
				map.put("addressStname", new StringType(stname.getValue().fhirType()));
			}

			Extension stdesig = addr.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.StreetDesignator)).findFirst().get();
			if (stdesig != null)
			{
				map.put("addressStdesig", new StringType(stdesig.getValue().fhirType()));
			}

			Extension postdir = addr.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PostDirectional)).findFirst().get();
			if (postdir != null)
			{
				map.put("addressPostdir", new StringType(postdir.getValue().fhirType()));
			}

			Extension unitnum = addr.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.UnitOrAptNumber)).findFirst().get();
			if (unitnum != null)
			{
				map.put("addressUnitnum", new StringType(unitnum.getValue().fhirType()));
			}


			if (addr.getState() != null)
			{
				map.put("addressState", new StringType(addr.getState()));
			}
			if (addr.getStateElement() != null)
			{
				map.put("addressJurisdiction", new StringType(addr.getState())); // by default.  If extension present, override
				Extension stateExt = addr.getStateElement().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.LocationJurisdictionId)).findFirst().get();
				if (stateExt != null)
				{
					map.put("addressJurisdiction", new StringType(stateExt.getValue().fhirType()));
				}
			}
			if (addr.getCity() != null)
			{
				map.put("addressCity", new StringType(addr.getCity()));
			}
			if (addr.getDistrict() != null)
			{
				map.put("addressCounty", new StringType(addr.getDistrict()));
			}
			if (addr.getPostalCode() != null)
			{
				map.put("addressZip", new StringType(addr.getPostalCode()));
			}
			if (addr.getCountry() != null)
			{
				map.put("addressCountry", new StringType(addr.getCountry()));
			}
		}
		return map;
	}

	/// <summary>Convert an "address" dictionary to a FHIR Address.</summary>
	/// <param name="dict">represents an address.</param>
	/// <returns>the corresponding FHIR Address representation of the address.</returns>
	public static Address mapToAddress(Map<String, StringType> map)
	{
		Address address = new Address();

		if (map != null)
		{
			List<StringType> lines = new ArrayList();
			if (map.containsKey("addressLine1") && !isNullOrEmpty(map.get("addressLine1").toString()))
			{
				lines.add(map.get("addressLine1"));
			}
			if (map.containsKey("addressLine2") && !isNullOrEmpty(map.get("addressLine2").toString()))
			{
				lines.add(map.get("addressLine2"));
			}
			if (lines.size() > 0)
			{
				address.setLine(lines);
			}
			if (map.containsKey("addressCityC") && !isNullOrEmpty(map.get("addressCityC").toString()))
			{
				Extension cityCode = new Extension();
				cityCode.setUrl(URL.ExtensionURL.CityCode);
				cityCode.setValue(map.get("addressCityC"));
				address.setCityElement(new StringType());
				address.getCityElement().getExtension().add(cityCode);
			}
			if (map.containsKey("addressCity") && !isNullOrEmpty(map.get("addressCity").toString()))
			{
				if (address.getCityElement() != null)
				{
					address.getCityElement().setValue(map.get("addressCity").toString());
				}
				else
				{
					address.setCity((map.get("addressCity").toString()));
				}

			}
			if (map.containsKey("addressCountyC") && !isNullOrEmpty(map.get("addressCountyC").toString()))
			{
				Extension countyCode = new Extension();
				countyCode.setUrl(URL.ExtensionURL.DistrictCode);
				countyCode.setValue(map.get("addressCountyC"));
				address.setDistrictElement(new StringType());
				address.getDistrictElement().getExtension().add(countyCode);
			}
			if (map.containsKey("addressCounty") && !isNullOrEmpty(map.get("addressCounty").toString()))
			{
				if (address.getDistrictElement() != null)
				{
					address.getDistrictElement().setValue(map.get("addressCounty").getValue());
				}
				else
				{
					address.setDistrict(map.get("addressCounty").toString());
				}
			}
			if (map.containsKey("addressState") && !isNullOrEmpty(map.get("addressState").toString()))
			{
				address.setState(map.get("addressState").toString());
			}
			// Special address field to support the jurisdiction extension custom to VRDR to support YC (New York City)
			// as used in the DeathLocationLoc
			if (map.containsKey("addressJurisdiction") && !isNullOrEmpty(map.get("addressJurisdiction").toString()))
			{
				if (address.getStateElement() == null)
				{
					address.setStateElement(new StringType());
				}
				address.getStateElement().getExtension().removeIf(ext -> ext.getUrl().equals(URL.ExtensionURL.LocationJurisdictionId));
				Extension extension = new Extension(URL.ExtensionURL.LocationJurisdictionId, map.get("addressJurisdiction"));
				address.getStateElement().getExtension().add(extension);
			}
			if (map.containsKey("addressZip") && !isNullOrEmpty(String.valueOf(map.get("addressZip"))))
			{
				address.setPostalCode(map.get("addressZip").toString());
			}
			if (map.containsKey("addressCountry") && !isNullOrEmpty(String.valueOf(map.get("addressCountry"))))
			{
				address.setCountry(map.get("addressCountry").toString());
			}
			if (map.containsKey("addressStnum") && !isNullOrEmpty(String.valueOf(map.get("addressStnum"))))
			{
				Extension stnum = new Extension();
				stnum.setUrl(URL.ExtensionURL.StreetNumber);
				stnum.setValue(map.get("addressStnum"));
				address.getExtension().add(stnum);
			}
			if (map.containsKey("addressPredir") && !isNullOrEmpty(String.valueOf(map.get("addressPredir"))))
			{
				Extension predir = new Extension();
				predir.setUrl(URL.ExtensionURL.PreDirectional);
				predir.setValue(map.get("addressPredir"));
				address.getExtension().add(predir);
			}
			if (map.containsKey("addressStname") && !isNullOrEmpty(String.valueOf(map.get("addressStname"))))
			{
				Extension stname = new Extension();
				stname.setUrl(URL.ExtensionURL.StreetName);
				stname.setValue(map.get("addressStname"));
				address.getExtension().add(stname);
			}
			if (map.containsKey("addressStdesig") && !isNullOrEmpty(String.valueOf(map.get("addressStdesig"))))
			{
				Extension stdesig = new Extension();
				stdesig.setUrl(URL.ExtensionURL.StreetDesignator);
				stdesig.setValue(map.get("addressStdesig"));
				address.getExtension().add(stdesig);
			}
			if (map.containsKey("addressPostdir") && !isNullOrEmpty(String.valueOf(map.get("addressPostdir"))))
			{
				Extension postdir = new Extension();
				postdir.setUrl(URL.ExtensionURL.PostDirectional);
				postdir.setValue(map.get("addressPostdir"));
				address.getExtension().add(postdir);
			}
			if (map.containsKey("addressUnitnum") && !isNullOrEmpty(String.valueOf(map.get("addressUnitnum"))))
			{
				Extension unitnum = new Extension();
				unitnum.setUrl(URL.ExtensionURL.UnitOrAptNumber);
				unitnum.setValue(map.get("addressUnitnum"));
				address.getExtension().add(unitnum);
			}

		}
		return address;
	}

	/// <summary>Returns an empty "address" Map.</summary>
	/// <returns>an empty "address" Map.</returns>
	public static Map<String, StringType> EmptyAddrMap()
	{
		Map<String, StringType> map = new HashMap();
		map.put("addressLine1", new StringType(""));
		map.put("addressLine2", new StringType(""));
		map.put("addressCity", new StringType(""));
		map.put("addressCityC", new StringType(""));
		map.put("addressCounty", new StringType(""));
		map.put("addressCountyC", new StringType(""));
		map.put("addressState", new StringType(""));
		map.put("addressJurisdiction", new StringType(""));
		map.put("addressZip", new StringType(""));
		map.put("addressCountry", new StringType(""));
		map.put("addressStnum", new StringType(""));
		map.put("addressPredir", new StringType(""));
		map.put("addressStname", new StringType(""));
		map.put("addressStdesig", new StringType(""));
		map.put("addressPostdir", new StringType(""));
		map.put("addressUnitnum", new StringType(""));
		return map;
	}

	/// <summary>Returns an empty "code" Map.</summary>
	/// <returns>an empty "code" Map.</returns>
	public static Map<String, StringType> EmptyCodeMap()
	{
		Map<String, StringType> map = new HashMap();
		map.put("code", new StringType(""));
		map.put("system", new StringType(""));
		map.put("display", new StringType(""));
		return map;
	}

	/// <summary>Returns an empty "codeable" Map.</summary>
	/// <returns>an empty "codeable" Map.</returns>
	public static Map<String, StringType> EmptyCodeableMap()
	{
		//Map<String, StringType> map = new HashMap<String, String>();
		Map<String, StringType> map = new HashMap();
		map.put("code", new StringType(""));
		map.put("system", new StringType(""));
		map.put("display", new StringType(""));
		map.put("text", new StringType(""));
		return map;
	}



	/// <summary>Get a value from a Map, but return null if the key doesn't exist or the value is an empty String.</summary>
	public  static String GetValue(Map<String, String> map, String key)
	{
		if (map != null && map.containsKey(key) && isNullOrWhiteSpace(map.get(key)))
		{
			return map.get(key);
		}
		return null;
	}

	// /// <summary>Check to make sure the given profile contains the given resource.</summary>
	// private static bool MatchesProfile(String resource, String profile)
	// {
	//     if (isNullOrWhiteSpace(profile) && profile.contains(resource))
	//     {
	//         return true;
	//     }
	//     return false;
	// }

	/// <summary>Combine the given dictionaries and return the combined result.</summary>
	private static Map<String, String> UpdateMap(Map<String, String> a, Map<String, String> b)
	{
		Map<String, String> map = new HashMap<String, String>();
		for (Map.Entry<String,String> entry : a.entrySet())
			map.put(entry.getKey(), entry.getValue());
		for (Map.Entry<String,String> entry : b.entrySet())
			map.put(entry.getKey(), entry.getValue());

		return map;
	}

	/// <summary>Returns a JSON encoded structure that maps to the various property
	/// annotations found in the DeathCertificateDocument class. This is useful for scenarios
	/// where you may want to display the data in user interfaces.</summary>
	/// <returns>a String representation of this Death Record in a descriptive format.</returns>
//	public String ToDescription()
//	{
//		Map<String, Map<String, Object>> description = new HashMap<String, Map<String, Object>>();
//		// the priority values should order the categories as: Decedent Demographics, Decedent Disposition, Death Investigation, Death Certification
//		for(PropertyInfo property:typeof(DeathCertificateDocument).GetProperties().OrderBy(p -> p.GetCustomAttribute<Property>().Priority))
//		{
//			// Grab property annotation for this property
//			Property info = property.GetCustomAttribute<Property>();
//
//			// Skip properties that shouldn't be serialized.
//			if (!info.Serialize)
//			{
//				continue;
//			}
//
//			// Add category if it doesn't yet exist
//			if (!description.containsKey(info.Category))
//			{
//				description.add(info.Category, new HashMap<String, Object>());
//			}
//
//			// Add the new property to the category
//			Map<String, Object> category = description[info.Category];
//			category[property.Name] = new HashMap<String, Object>();
//
//			// Add the attributes of the property
//			category[property.Name].put("Name", info.getName());
//			category[property.Name].put("Type", info.getTypeCode());
//			category[property.Name].put("Description", info.getDefinition());
//			category[property.Name].put("IGUrl", info.IGUrl);
//			category[property.Name].put("CapturedInIJE", info.CapturedInIJE);
//
//			// Add snippets
//			FHIRPath path = property.GetCustomAttribute<FHIRPath>();
//			var matches = Navigator.Select(path.Path);
//			if (matches.size() > 0)
//			{
//				if (info.Type == Property.Types.ListCOD || info.Type == Property.Types.ListArr || info.Type == Property.Types.List4Arr)
//				{
//					// Make sure to grab all of the Conditions for COD
//					String xml = "";
//					String json = "";
//					for(var match:matches)
//					{
//						xml += match.ToXml();
//						json += match.ToJson() + ",";
//					}
//					category[property.Name].put("SnippetXML", xml);
//					category[property.Name].put("SnippetJSON", "[" + json + "]");
//				}
//				else if (isNullOrWhiteSpace(path.Element))
//				{
//					// Since there is an "Element" for this path, we need to be more
//					// specific about what is included in the snippets.
//					XElement root = XElement.Parse(matches.get(0).ToXml());
//					XElement node = root.DescendantsAndSelf("{http://hl7.org/fhir}" + path.Element).FirstOrDefault();
//					if (node != null)
//					{
//						node.Name = node.Name.LocalName;
//						category[property.Name]["SnippetXML"] = node.toString();
//					}
//					else
//					{
//						category[property.Name]["SnippetXML"] = "";
//					}
//					Map<String, Object> jsonRoot =
//					JsonConvert.DeserializeObject<Map<String, Object>>(matches.get(0).ToJson(),
//						new JsonSerializerSettings() { DateParseHandling = DateParseHandling.None });
//					if (jsonRoot != null && jsonRoot.Keys.contains(path.Element))
//					{
//						category[property.Name]["SnippetJSON"] = "{" + $"\"{path.Element}\": \"{jsonRoot[path.Element]}\"" + "}";
//					}
//					else
//					{
//						category[property.Name]["SnippetJSON"] = "";
//					}
//				}
//				else
//				{
//					category[property.Name]["SnippetXML"] = matches.get(0).ToXml();
//					category[property.Name]["SnippetJSON"] = matches.get(0).ToJson();
//				}
//
//			}
//			else
//			{
//				category[property.Name]["SnippetXML"] = "";
//				category[property.Name]["SnippetJSON"] = "";
//			}
//
//			// Add the current value of the property
//			if (info.Type == Property.Types.Map)
//			{
//				// Special case for Map; we want to be able to describe what each key means
//				Map<String, String> value = (Map<String, String>)property.GetValue(this);
//				if (value == null)
//				{
//					continue;
//				}
//				Map<String, Map<String, String>> moreInfo = new HashMap<String, Map<String, String>>();
//				for(PropertyParam propParameter:property.GetCustomAttributes<PropertyParam>())
//				{
//					moreInfo[propParameter.Key] = new HashMap<String, String>();
//					moreInfo[propParameter.Key].put("Description", propParameter.Description);
//					if (value.containsKey(propParameter.Key))
//					{
//						moreInfo[propParameter.Key].put("Value", value[propParameter.Key]);
//					}
//					else
//					{
//						moreInfo[propParameter.Key].put("Value", null);
//					}
//				}
//				category[property.Name].put("Value", moreInfo);
//			}
//			else
//			{
//				category[property.Name].put("Value", property.GetValue(this));
//			}
//		}
//		return JsonConvert.SerializeObject(description);
//	}

	/// <summary>Helper method to return a JSON String representation of this Death Record.</summary>
	/// <param name="contents">String that represents </param>
	/// <returns>a new DeathCertificateDocument that corresponds to the given descriptive format</returns>
//	public static DeathCertificateDocument FromDescription(String contents)
//	{
//		DeathCertificateDocument record = new DeathCertificateDocument();
//		Map<String, Map<String, Object>> description = JsonConvert.DeserializeObject<Map<String, Map<String, Object>>>(contents, new JsonSerializerSettings() { DateParseHandling = DateParseHandling.None });
//		// Loop over each category
//		for(Map.Entry<String, Map<String, Object>> category:description.entrySet())
//		{
//			// Loop over each property
//			for(Map.Entry<String, Object> property:category.getValue().entrySet()) {
//				if (!property.getKey().contains("Value") || property.getValue() == null) {
//					continue;
//				}
//				// Set the property on the new DeathCertificateDocument based on its type
//				String propertyName = property.getKey();
//				Object value = null;
//				if (propertyName.equals("Type")) {
//					if (property.getValue() == Property.Types.String || property.getValue() == Property.Types.StringDateTime) {
//						value = property.getValue()["Value"].toString();
//						if (isNullOrWhiteSpace((String) value)) {
//							value = null;
//						}
//					}
//				}
//				else if (property..get("Type") == Property.Types.StringArr)
//				{
//					value = property.getValue()["Value"].ToObject<String[]>();
//				}
//				else if (property.get("Type") == Property.Types.Bool)
//				{
//					value = property.getValue()["Value"].ToObject<bool>();
//				}
//				else if (property.get("Type") == Property.Types.ListArr)
//				{
//					value = property.getValue()["Value"].ToObject<List<String>[]>();
//				}
//				else if (property.get("Type") == Property.Types.ListCOD)
//				{
//					value = property.getValue()["Value"].ToObject<List<String, String /*, Map<String, String>*/>[]>();
//				}
//				else if (property.get("Type") == Property.Types.Map)
//				{
//					Map<String, Map<String, String>> moreInfo =
//					property.getValue()["Value"].ToObject<Map<String, Map<String, String>>>();
//					Map<String, String> result = new HashMap<String, String>();
//					for(KeyValuePair<String, Map<String, String>> entry in moreInfo)
//					{
//						result[entry.Key] = entry.getValue()["Value");
//					}
//					value = result;
//				}
//				if (value != null)
//				{
//					typeof(DeathCertificateDocument).GetProperty(propertyName).SetValue(record, value);
//				}
//			}
//		}
//		return record;
//	}

	/// <summary>Helper method to create a HumanName from a list of Strings.</summary>
	/// <param name="value">A list of Strings to be converted into a name.</param>
	/// <param name="names">The current list of HumanName attributes for the person.</param>
	public static void updateGivenHumanName(String[] value, List<HumanName> names)
	{
		// Remove any blank or null values.
		List nameFragments = Arrays.asList(value).stream().filter(v -> !isNullOrEmpty(v)).collect(Collectors.toList());
		// Set names only if there are non-blank values.
		if (value.length < 1)
		{
			return;
		}
		HumanName name = names.stream().filter(n -> n.getUse().equals(HumanName.NameUse.OFFICIAL)).findFirst().orElse(null);;
		if (name != null)
		{
			name.setGiven(nameFragments);
		}
		else
		{
			name = new HumanName();
			name.setUse(HumanName.NameUse.OFFICIAL);
			name.setGiven(nameFragments);
			names.add(name);
		}
	}

	/// <summary>Helper method to validate that all PartialDate and PartialDateTime exensions are valid and have the valid required sub-extensions.</summary>
	/// <param name="bundle">The bundle in which to validate the PartialDate/Time extensions.</param>
	private static void ValidatePartialDates(Bundle bundle)
	{
		StringBuilder errors = new StringBuilder();
		List<Resource> resources = bundle.getEntry().stream().map(entry -> entry.getResource()).collect(Collectors.toList());

		for(Resource resource:resources)
		{
			for(Property child:resource.children().stream().filter(child -> child.getClass().isAssignableFrom(DataType.class)).collect(Collectors.toList())); // Class.forName("Enumerations.DataType").isAssignableFrom(child)));//child instanceof Enumerations.DataType)))
			{
				// Extract PartialDates and PartialDateTimes.

				List<Extension> partialDateExtensions = child.stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDate) || ext.getUrl().equals(URL.ExtensionURL.PartialDateTime)).toList();
				for(Extension partialDateExtension:partialDateExtensions)
				{
					// Validate that the required sub-extensions are in the PartialDate/Time component.
					List<String> partialDateSubExtensions = partialDateExtension.getExtension().stream().findAny(ext -> ext.g).ToList();
					if (!partialDateSubExtensions.contains(URL.ExtensionURL.DateDay))
					{
						errors.append("Missing 'Date-Day' of [" + partialDateExtension.getUrl() + "] for resource [" + resource.getId() + "].").append("\n");
					}
					if (!partialDateSubExtensions.contains(URL.ExtensionURL.DateMonth))
					{
						errors.append("Missing 'Date-Month' of [" + partialDateExtension.getUrl() + "] for resource [" + resource.getId() + "].").append("\n");
					}
					if (!partialDateSubExtensions.contains(URL.ExtensionURL.DateYear))
					{
						errors.append("Missing 'Date-Year' of [" + partialDateExtension.getUrl() + "] for resource [" + resource.getId() + "].").append("\n");
					}
					if (partialDateExtension.getUrl().equals(URL.ExtensionURL.PartialDateTime) && !partialDateSubExtensions.contains(URL.ExtensionURL.DateTime))
					{
						errors.append("Missing 'Date-Time' of [" + partialDateExtension.getUrl() + "] for resource [" + resource.getId() + "].").append("\n");
					}
					// Validate that there are no extraneous invalid sub-extensions of the PartialDate/Time component.
					partialDateSubExtensions.remove(URL.ExtensionURL.DateDay);
					partialDateSubExtensions.remove(URL.ExtensionURL.DateMonth);
					partialDateSubExtensions.remove(URL.ExtensionURL.DateYear);
					partialDateSubExtensions.remove(URL.ExtensionURL.DateTime);
					if (partialDateSubExtensions.size() > 0) {
						errors.append("[" + partialDateExtension.getUrl() + "] component contains extra invalid fields [" + StringUtils.join(partialDateSubExtensions, ", ") + "] for resource [" + resource.getId() + "].").append("\n");
					}
				}
			}
		}
		if (errors.length() > 0)
		{
			throw new IllegalArgumentException(errors.toString());
		}
	}

	public static boolean isNullOrEmpty(String s) { ////
		return s == null || s.length() == 0;
	}

	public static boolean isNullOrWhiteSpace(String s) {
		return s == null || isWhitespace(s);
	}

	public static boolean isWhitespace(String s) {
		int length = s.length();
		if (length > 0) {
			for (int i = 0; i < length; i++) {
				if (!Character.isWhitespace(s.charAt(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}


	public static Integer GetPartialDate(Extension partialDateTime, String partURL)
	{
		Extension part = partialDateTime != null ? partialDateTime.getExtension() != null ? partialDateTime.getExtension().stream().filter(ext -> ext.getUrl().equals(partURL)).findFirst().get():null:null;
		Extension dataAbsent = part != null ? part.getExtension() != null ? part.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.OtherExtensionURL.DataAbsentReason)).findFirst().get():null:null;
		// extension for absent date can be directly on the part as with year, month, day
		if (dataAbsent != null)
		{
			// The data absent reason is either a placeholder that a field hasen't been set yet (data absent reason of 'temp-unknown') or
			// a claim that there's no data (any other data absent reason, e.g., 'unknown'); return null for the former and "-1" for the latter
			String code = ((Coding)dataAbsent.getValue()).getCode();
			if(code.equals("temp-unknown"))
			{
				return null;
			}
			else
			{
				return -1;
			}
		}
		// check if the part (e.g. "_valueUnsignedInt") has a data absent reason extension on the value
		Extension dataAbsentOnValue = part != null ? part.getValue() != null ? part.getValue().getExtension() != null ? part.getValue().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.OtherExtensionURL.DataAbsentReason)).findFirst().get():null:null:null;
		if (dataAbsentOnValue != null)
		{
			String code = ((Coding)dataAbsentOnValue.getValue()).getCode();
			if (code.equals("temp-unknown"))
			{
				return null;
			}
			else
			{
				return -1;
			}
		}
		// If we have a value, return it
		if (part != null && part.getValue() != null)
		{
			return Integer.parseInt(part.getValue().toString()); // Untangle a FHIR UnsignedInt in an extension into an int
		}
		// No data present at all, return null
		return null;
	}

	/// <summary>NewBlankPartialDateTimeExtension, Build a blank PartialDateTime extension (which means all the placeholder data absent
	/// reasons are present to note that the data is not in fact present). This method takes an optional flag to determine if this extension
	/// should include the time field, which is not always needed</summary>
	public static Extension NewBlankPartialDateTimeExtension(boolean includeTime)
	{
		Extension partialDateTime = new Extension(includeTime ? URL.ExtensionURL.PartialDateTime : URL.ExtensionURL.PartialDate, null);
		Extension year = new Extension(URL.ExtensionURL.DateYear, null);
		year.getExtension().add(new Extension(URL.OtherExtensionURL.DataAbsentReason, new Coding().setCode("temp-unknown")));
		partialDateTime.getExtension().add(year);
		Extension month = new Extension(URL.ExtensionURL.DateMonth, null);
		month.getExtension().add(new Extension(URL.OtherExtensionURL.DataAbsentReason, new Coding().setCode("temp-unknown")));
		partialDateTime.getExtension().add(month);
		Extension day = new Extension(URL.ExtensionURL.DateDay, null);
		day.getExtension().add(new Extension(URL.OtherExtensionURL.DataAbsentReason, new Coding().setCode("temp-unknown")));
		partialDateTime.getExtension().add(day);
		if (includeTime)
		{
			Extension time = new Extension(URL.ExtensionURL.DateTime, null);
			time.getExtension().add(new Extension(URL.OtherExtensionURL.DataAbsentReason, new Coding().setCode("temp-unknown")));
			partialDateTime.getExtension().add(time);
		}
		return partialDateTime;
	}
	/// <summary>Setter helper for anything that uses PartialDateTime, allowing a particular date field (year, month, or day) to be
	/// set in the extension. Arguments are the extension to poplulate, the part of the URL to populate, and the value to specify.
	/// The value can be a positive number for an actual value, a -1 meaning that the value is explicitly unknown, or null meaning
	/// the data has not been specified.</summary>
	public static void SetPartialDate(Extension partialDateTime, String partURL, Integer value)
	{
		Extension part = partialDateTime.getExtension().stream().filter(ext -> ext.getUrl().equals(partURL)).findFirst().get();
		part.getExtension().removeIf(ext -> ext.getUrl().equals(URL.OtherExtensionURL.DataAbsentReason));
		if (value != null && value != -1)
		{
			part.setValue(new DateTimeType(value.toString()));
		}
		else
		{
			part.setValue(new DateTimeType());
			// Determine which data absent reason to use based on whether the value is unknown or -1
			part.getValue().getExtension().add(new Extension(URL.OtherExtensionURL.DataAbsentReason, new Coding().setCode(value == -1 ? "unknown" : "temp-unknown")));
		}
	}

	/// <summary>Getter helper for anything that uses PartialDateTime, allowing the time to be read from the extension</summary>
	public static String GetPartialTime(Extension partialDateTime)
	{
		Extension part = partialDateTime != null? partialDateTime.getExtension() != null ? partialDateTime.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.DateTime)).findFirst().get():null:null;
		Extension dataAbsent = part != null ? part.getExtension() != null ? part.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.OtherExtensionURL.DataAbsentReason)).findFirst().get():null:null;
		// extension for absent date can be directly on the part as with year, month, day
		if (dataAbsent != null)
		{
			// The data absent reason is either a placeholder that a field hasen't been set yet (data absent reason of 'temp-unknown') or
			// a claim that there's no data (any other data absent reason, e.g., 'unknown'); return null for the former and "-1" for the latter
			String code = ((Coding)dataAbsent.getValue()).getCode();
			if (code == "temp-unknown")
			{
				return null;
			}
			else
			{
				return "-1";
			}
		}
		// check if the part (e.g. "_valueTime") has a data absent reason extension on the value
		Extension dataAbsentOnValue = part != null ? part.getValue() != null ? part.getValue().getExtension() != null ? part.getValue().getExtension().stream().filter(ext -> ext.getUrl().equals(URL.OtherExtensionURL.DataAbsentReason)).findFirst().get():null:null:null;
		if (dataAbsentOnValue != null)
		{
			String code = ((Coding)dataAbsentOnValue.getValue()).getCode();
			if (code == "temp-unknown")
			{
				return null;
			}
			else
			{
				return "-1";
			}
		}
		// If we have a value, return it
		if (part != null && part.getValue() != null)
		{
			return part.getValue().toString();
		}
		// No data present at all, return null
		return null;
	}

	/// <summary>Setter helper for anything that uses PartialDateTime, allowing the time to be set in the extension</summary>
	public static void SetPartialTime(Extension partialDateTime, String value)
	{
		Extension part = partialDateTime.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.DateTime)).findFirst().get();
		part.getExtension().removeIf(ext -> ext.getUrl().equals(URL.OtherExtensionURL.DataAbsentReason));
		if (value != null && value != "-1")
		{
			// we need to force it to be 00:00:00 format to be compliant with the IG because the FHIR class doesn't
			if (value.length() < 8)
			{

				value += ":";
				StringUtils.rightPad(value, 8, "0");
			}
			part.setValue(new DateTimeType().setMinute(OffsetDateTime.MIN.getMinute()).setHour(OffsetDateTime.MIN.getSecond()).setHour(OffsetDateTime.MIN.getSecond()));
		}
		else
		{
			part.setValue(new DateTimeType());
			// Determine which data absent reason to use based on whether the value is unknown or -1
			part.getValue().getExtension().add(new Extension(URL.OtherExtensionURL.DataAbsentReason, new Coding().setCode(value == "-1" ? "unknown" : "temp-unknown")));
		}
	}

	/// <summary>Getter helper for anything that can have a regular FHIR date/time
	/// field (year, month, or day) to be read the value
	/// supports dates and date times but does NOT support extensions</summary>
	public static Integer GetDateFragment(Element value, String partURL)
	{
		if (value == null)
		{
			return null;
		}
		// If we have a basic value as a valueDateTime use that, otherwise pull from the PartialDateTime extension
		OffsetDateTime offsetDateTime = null;
		if (value instanceof DateTimeType && ((DateTimeType)value).getValue() != null)
		{
			// Note: We can't just call ToOffsetDateTime() on the DateTimeType because want the datetime in whatever local time zone was provided
			offsetDateTime = OffsetDateTime.parse((CharSequence) ((DateTimeType)value).getValue());
		}
            else if (value instanceof DateTimeType && ((DateTimeType)value).getValue() != null)
		{
			// Note: We can't just call ToOffsetDate() on the Date because want the date in whatever local time zone was provided
			offsetDateTime = OffsetDateTime.parse((CharSequence)((DateTimeType)value).getValue());
		}
		if (offsetDateTime != null)
		{
			switch (partURL)
			{
				case URL.ExtensionURL.DateYear:
					return ((OffsetDateTime)offsetDateTime).MIN.getYear();
				case URL.ExtensionURL.DateMonth:
					return ((OffsetDateTime)offsetDateTime).MIN.getDayOfMonth();
				case URL.ExtensionURL.DateDay:
					return ((OffsetDateTime)offsetDateTime).MIN.getDayOfMonth();
				default:
					try {
						throw new Exception("GetDateFragment called with unsupported PartialDateTime segment");
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
			}
		}
		return null;
	}

	/// <summary>Getter helper for anything that can have a regular FHIR date/time or a PartialDateTime extension, allowing a particular date
	/// field (year, month, or day) to be read from either the value or the extension</summary>
	public static Integer GetDateFragmentOrPartialDate(Element value, String partURL)
	{
		if (value == null) {
			return null;
		}
		Integer dateFragment = GetDateFragment(value, partURL);
		if (dateFragment != null)
		{
			return dateFragment;
		}
		// Look for either PartialDate or PartialDateTime
		Extension extension = value.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDateTime)).findFirst().get();
		if (extension == null)
		{
			extension = value.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDate)).findFirst().get();
		}
		return GetPartialDate(extension, partURL);
	}

	public static DateTimeType ConvertFhirTimeToDateTimeType(TimeType value) {
		return (DateTimeType) new DateTimeType().setYear(OffsetDateTime.MIN.getYear()).setMonth(OffsetDateTime.MIN.getMonthValue()).setDay(OffsetDateTime.MIN.getDayOfMonth()).setHour(FhirTimeHour(value)).setMinute(FhirTimeMin(value)).setSecond(FhirTimeSec(value));
		//return new DateTimeType(OffsetDateTime.MIN.getYear(), OffsetDateTime.MIN.getMonth(), OffsetDateTime.MIN.getDayOfMonth(),FhirTimeHour(value), FhirTimeMin(value), FhirTimeSec(value), 0);
	}

	public static Map getHourMinSecFromParsedTime(OffsetDateTime parsedTime)
	{
		int seconds = parsedTime.getHour() * 3600 + parsedTime.getMinute() * 60 + parsedTime.getSecond();
		int hour = seconds / 3600;
		int minute = (seconds - hour * 3600)/60;
		int second = seconds % 60;
		return new HashMap() {{put("hh", hour); put("mm", minute); put("ss", second);}};
	}

	public static int FhirTimeHour(TimeType value) {
		return Integer.parseInt(value.toString().substring(0, 2));
	}

	public static int FhirTimeMin(TimeType value) {
		return Integer.parseInt(value.toString().substring(3, 2));
	}

	public static int FhirTimeSec(TimeType value) {
		return Integer.parseInt(value.toString().substring(6, 2));
	}

	/// <summary>Getter helper for anything that can have a regular FHIR date/time, allowing the time to be read from the value</summary>
	public static String GetTimeFragment(Element value) {
		if(value instanceof DateTimeType && ((DateTimeType)value).getValue() != null)
		{
			// Using DateTimeType's ToOffsetDateTime doesn't keep the time in the original time zone, so we parse the String representation, first using the appropriate segment of
			// the Regex defined at http://hl7.org/fhir/R4/datatypes.html#dateTime to pull out everything except the time zone
			String dateRegex = "([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)?)?)?)?";
			//Match dateStringMatch = Regex.Match(((DateTimeType)value).toString(), dateRegex);

			Pattern pattern = Pattern.compile("([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)?)?)?)?");
			Matcher matcher = pattern.matcher((value).toString());
			DateTimeType dateTimeType;
			//if (dateStringMatch != null && DateTimeType.TryParse(dateStringMatch.toString(), out dateTime))
			if(matcher.find())
			{
//				LocalDate today = LocalDate.now();
//				LocalDate birthday = LocalDate.of(1960, Month.JANUARY, 1);
//				Period p = Period.between(birthday, today);
//				long p2 = ChronoUnit.DAYS.between(birthday, today);

			//	Period timeSpan = new Period.between(0, dateTimeType.getHour(), dateTimeType.getMinute(), dateTimeType.getSecond());
			//	Period timeSpan = new Period.between(0, dateTimeType.getHour(), dateTimeType.getMinute(), dateTimeType.getSecond());
				OffsetDateTime parsedDateTime = OffsetDateTime.parse((CharSequence) value);
				StringBuffer sb = new StringBuffer();
				sb.append(parsedDateTime.getHour()).append(":").append(parsedDateTime.getMinute()).append(":").append(parsedDateTime.getSecond()).append("hh:mm:ss");
				return sb.toString();
			}
		}
		return null;
	}

	/// <summary>Getter helper for anything that can have a regular FHIR date/time or a PartialDateTime extension, allowing the time to be read
	/// from either the value or the extension</summary>
	public static String GetTimeFragmentOrPartialTime(Element value)
	{
		// If we have a basic value as a valueDateTime use that, otherwise pull from the PartialDateTime extension
		String time = GetTimeFragment(value);
		if (time != null) {
			return time;
		}
		return GetPartialTime(value.getExtension().stream().filter(ext -> ext.getUrl().equals(URL.ExtensionURL.PartialDateTime)).findFirst().get());
	}



	/// <summary>Convert a "code" dictionary to a FHIR Coding.</summary>
	/// <param name="dict">represents a code.</param>
	/// <returns>the corresponding Coding representation of the code.</returns>
	public static Coding MapToCoding(Map<String, StringType> map)
	{
		Coding coding = new Coding();
		if (map != null)
		{
			if (map.containsKey("code") && !isNullOrEmpty(String.valueOf(map.get("code"))))
			{
				coding.setCode(String.valueOf(map.get("code")));
			}
			if (map.containsKey("system") && !isNullOrEmpty(String.valueOf(map.get("system"))))
			{
				coding.setSystem(String.valueOf(map.get("system")));
			}
			if (map.containsKey("display") && !isNullOrEmpty(String.valueOf(map.get("display"))))
			{
				coding.setDisplay(String.valueOf(map.get("display")));
			}
			return coding;
		}
		return null;
	}

	/// <summary>Convert a "code" dictionary to a FHIR CodableConcept.</summary>
	/// <param name="dict">represents a code.</param>
	/// <returns>the corresponding CodeableConcept representation of the code.</returns>
	public static CodeableConcept MapToCodeableConcept(Map<String, StringType> map)
	{
		CodeableConcept codeableConcept = new CodeableConcept();
		Coding coding = MapToCoding(map);
		codeableConcept.getCoding().add(coding);
		if (map != null && map.containsKey("text") && !isNullOrEmpty(String.valueOf(map.get("text"))))
		{
			codeableConcept.setText(String.valueOf(map.get("text")));
		}
		return codeableConcept;
	}


}
