package edu.gatech.chai.VRDR.model;

import edu.gatech.chai.VRDR.model.util.DeathCertificateDocumentUtil;
import edu.gatech.chai.VRDR.model.util.DecedentUtil;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Period;
import org.slf4j.ILoggerFactory;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.*;

import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.lang.Exception;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.gatech.chai.VRDR.model.util.DeathCertificateDocumentUtil.*;

//public class IJEMortality {


/// <summary>Property attribute used to describe a field in the IJE Mortality format.</summary>
//[System.AttributeUsage(System.AttributeTargets.Property)]

///       record IJEField(int Field, int Location, int Length, String Contents, String Name, int Priority) implements Serializable { } {
//            @ConstructorProperties({"Field", "Location", "Length", "Contents", "Name", "Priority"})
//                public Person {}
///        }





/// <summary>A "wrapper" class to convert between a FHIR based <c>DeathRecord</c> and
/// a record in IJE Mortality format. Each property of this class corresponds exactly
/// with a field in the IJE Mortality format. The getters convert from the embedded
/// FHIR based <c>DeathRecord</c> to the IJE format for a specific field, and
/// the setters convert from IJE format for a specific field and set that value
/// on the embedded FHIR based <c>DeathRecord</c>.</summary>
public class IJEMortality
{
    /// <summary>Utility location to provide support for setting TRX-only fields that have no mapping in IJE when creating coding response records</summary>
    public TRXHelper trx;

    /// <summary>Utility location to provide support for setting MRE-only fields that have no mapping in IJE when creating coding response records</summary>
    public MREHelper mre;

    /// <summary>Field _void.</summary>
    private String _void;

    /// <summary>Field _alias.</summary>
    private String _alias;

    public static class IJEField {
        IJEField(int Field, int Location, int Length, String Contents, String Name, int Priority){};
        int Field;
        int Location;

        public int getField() {
            return Field;
        }

        public void setField(int field) {
            Field = field;
        }

        public int getLocation() {
            return Location;
        }

        public void setLocation(int location) {
            Location = location;
        }

        public int getLength() {
            return Length;
        }

        public void setLength(int length) {
            Length = length;
        }

        public String getContents() {
            return Contents;
        }

        public void setContents(String Contents) {
            Contents = Contents;
        }

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }

        public int getPriority() {
            return Priority;
        }

        public void setPriority(int priority) {
            Priority = priority;
        }

        int Length;
        String Contents;
        String Name;
        int Priority;

    }

    /// <summary>Helper class to contain properties for setting TRX-only fields that have no mapping in IJE when creating coding response records</summary>
    public class TRXHelper
    {
        private DeathCertificateDocument record;
        /// <summary>Constructor for class to contain properties for setting TRX-only fields that have no mapping in IJE when creating coding response records</summary>
        public TRXHelper(DeathCertificateDocument record)
        {
            this.record = record;
        }
        /// <summary>coder status - Property for setting the CodingStatus of a Cause of Death Coding Submission</summary>
        public String getCS()
        {
            return record.getCoderStatus().toString();
        }
        public void setCS(String value)
        {
            if (!isNullOrWhiteSpace(value))
            {
                record.getEntry().
                    record.setCoderStatus(Integer.parseInt(value));
            }
        }
        /// <summary>shipment number - Property for setting the ShipmentNumber of a Cause of Death Coding Submission</summary>
        public String getSHIP()
        {
            return record.shipmentNumber;
        }
        public void setSHIP(String value)
        {
            record.setShipmentNumber(Integer.parseInt(value));
        }
    }

    /// <summary>Helper class to contain properties for setting MRE-only fields that have no mapping in IJE when creating coding response records</summary>
    public class MREHelper
    {
        private DeathCertificateDocument record;
        /// <summary>Constructor for class to contain properties for setting MRE-only fields that have no mapping in IJE when creating coding response records</summary>
        public MREHelper(DeathCertificateDocument record)
        {
            this.record = record;
        }
        /// <summary>Property for setting the Race Recode 40 of a Demographic Coding Submission</summary>
        public String getRECODE40()
        {
            return record.getRaceRecode40Helper();
        }
        public void setRECODE40(String value)
        {
            record.setRaceRecode40Helper(value);
        }
    }

    /// <summary>FHIR based death record.</summary>
    private DeathCertificateDocument record;

    /// <summary>IJE data lookup helper. Thread-safe singleton!</summary>
    private MortalityData dataLookup = MortalityData.getInstance();

    /// <summary>Validation errors encountered while converting a record</summary>
    private List<String> validationErrors = new ArrayList<String>();

    /// <summary>Constructor that takes a <c>DeathRecord</c>.</summary>
    public IJEMortality(DeathCertificateDocument record, boolean validate)// = true)
    {
        this.record = record;
        this.trx = new TRXHelper(record);
        this.mre = new MREHelper(record);
        if (validate)
        {
            // We need to force a conversion to happen by calling toString() if we want to validate
            toString();
            if (validationErrors.size() > 0)
            {
                String errorString = new StringBuffer().append(validationErrors.size()).append(" validation errors:\n").append(validationErrors).toString();
                try {
                    throw new Exception(errorString);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /// <summary>Constructor that takes an IJE String and builds a corresponding internal <c>DeathRecord</c>.</summary>
    public IJEMortality(String ije, boolean validate)// = true) : this()
    {
        if (ije == null)
        {
            throw new IllegalArgumentException("IJE String cannot be null.");
        }
        if (ije.length() < 5000)
        {
            ije = StringUtils.rightPad(ije, 5000, " ");
        }
        // Loop over every property (these are the fields); Order by priority
        //List<PropertyInfo> properties = typeof(IJEMortality).GetProperties().ToList().OrderBy(p -> p.GetCustomAttribute<IJEField>().Priority).ToList();
        List<Field> properties = Arrays.stream(IJEMortality.class.getFields()).sorted(Comparator.comparing(Field.::).toList()..OrderBy(p -> p.GetCustomAttribute<IJEField>().Priority).ToList();
        for(PropertyInfo property:properties)
        {
            // Grab the field attributes
            IJEField info = property.GetCustomAttribute<IJEField>();
            // Grab the field value
            String field = ije.substring(info.getLocation() - 1, info.getLength());
            // Set the value on this IJEMortality (and the embedded record)
            property.set(this, field);
        }
        if (validate && validationErrors.size() > 0)
        {
            String errorString = new StringBuffer().append(validationErrors.size()).append(" validation errors:\n").append( validationErrors).toString();
            try {
                throw new Exception(errorString);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /// <summary>Constructor that creates an empty record for constructing records using the IJE properties.</summary>
    public IJEMortality() {
        this.record = new DeathCertificateDocument();
        this.trx = new TRXHelper(record);
        this.mre = new MREHelper(record);
    }

    /// <summary>Converts the internal <c>DeathRecord</c> into an IJE String.</summary>
    @Override
    public String toString()
    {
        // Start with empty IJE Mortality record
        StringBuilder ije = new StringBuilder(new String(" ".toCharArray(), 0, 5000));

        // Loop over every property (these are the fields)
        for(Field property:IJEMortality.class.getFields())
        {
            // Grab the field value
            String field = null;//, null).toString();
            try {
                field = property.get(this).toString();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            // Grab the field attributes
            IJEField info = property.GetCustomAttribute<IJEField>();
            // Be mindful about lengths
            if (field.length() > info.getLength())
            {
                field = field.substring(0, info.getLength());
            }
            // Insert the field value into the record
            ije.remove(info.getLocation() - 1, field.length());
            ije.Insert(info.getLocation() - 1, field);
        }
        return ije.toString();
    }

    public String toString() {
        //IJEMortality ijeEMortality = new IJEMortality();
        //Field[] fields = ijeEMortality.getClass().getDeclaredFields()
        // Start with empty IJE Mortality record
        StringBuilder ije = new StringBuilder(new String(" ".toCharArray(), 0, 5000));
        Stream.of(IJEMortality.class.getDeclaredMethods())
                .filter(method -> method.getName().startsWith("get"))
                .map(getterMethod -> {
                    try {
                        return getterMethod.invoke(this);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .forEach(fieldValue -> {
                    System.out.println(fieldValue);
                    Method fieldGetter = null;
                    try {
                        fieldGetter = IJEField.class.getMethod("getmeta"+fieldValue.toString());
                        String metaFieldValue = fieldGetter.invoke(this).toString();
                        if (fieldValue.toString().length() > metaFieldValue.length())
                        {
                            fieldValue = fieldValue.toString().substring(0, metaFieldValue.length());
                        }
                        ije.Remove(info.getLocation() - 1, field.length());
                        ije.Insert(info.getLocation() - 1, field);
                        ije=ije.replace(this.IJEField.get, int last, String st)
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /// <summary>Returns the corresponding <c>DeathRecord</c> for this IJE String.</summary>
    public DeathCertificateDocument ToDeathRecord()
    {
        return this.record;
    }

    /////////////////////////////////////////////////////////////////////////////////
    //
    // Class helper methods for getting and settings IJE fields.
    //
    /////////////////////////////////////////////////////////////////////////////////

    /// <summary>Truncates the given String to the given length.</summary>
    private static String Truncate(String value, int length)
    {
        if (isNullOrWhiteSpace(value) || value.length() <= length)
        {
            return value;
        }
        else
        {
            return value.substring(0, length);
        }
    }

    /// <summary>Grabs the IJEInfo for a specific IJE field name.</summary>
    private IJEField FieldInfo(String ijeFieldName)
    {
        //return typeof(IJEMortality).getField(ijeFieldName).GetCustomAttribute<IJEField>();
        Method fieldGetter = null;
        try {
            fieldGetter = IJEField.class.getMethod("getmeta"+ijeFieldName);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            return (IJEField)fieldGetter.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /// <summary>Helps decompose a DateTime into individual parts (year, month, day, time).</summary>
    private String DateTimeStringHelper(IJEField info, String value, String type, OffsetDateTime date, boolean dateOnly = false, boolean withTimezoneOffset = false)
    {
        if (type.equals("yyyy"))
        {
            if (value == null || value.length() < 4)
            {
                return "";
            }
            int year = Integer.parseInt(Truncate(value, info.getLength()));
            if (year > 1900 && year <= LocalDate.now().getYear())
            {
                //date = new OffsetDateTime(year, date.Month, date.Day, date.Hour, date.Minute, date.Second, date.Millisecond, TimeSpan.Zero);
                LocalDateTime localDateTime = LocalDateTime.of(year, date.getMonthValue(), date.getMonthValue(), date.getHour(), date.getMinute(), date.getSecond());
                date = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
            }
        }
        else if (type.equals("MM"))
        {
            if (value == null || value.length() < 2)
            {
                return "";
            }
            int month = Integer.parseInt(Truncate(value, info.getLength()));
            if(month > 0 && month <= 12)
            {
                LocalDateTime localDateTime = LocalDateTime.of(date.getYear(), date.getMonthValue(), month, date.getHour(), date.getMinute(), date.getSecond());
                date = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
            }
        }
        else if (type.equals("dd"))
        {
            if (value == null || value.length() < 2)
            {
                return "";
            }
            int day = Integer.parseInt(Truncate(value, info.getLength()));
            {
                LocalDateTime localDateTime = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), day, date.getMinute(), date.getSecond());
                date = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
            }
        }
        else if (type.equals("HHmm"))
        {
            if (value == null || value.length() < 4)
            {
                return "";
            }
            int hour = Integer.parseInt(Truncate(value, info.getLength()).substring(0, 2));
                // Treat 99 as blank
                if (hour != 99)
                {
                   // date = new OffsetDateTime(date.Year, date.Month, date.Day, hour, date.Minute, date.Second, date.Millisecond, TimeSpan.Zero);
                    LocalDateTime localDateTime = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), hour, date.getMinute(), date.getSecond());
                    date = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
                }

            int minute = Integer.parseInt(Truncate(value, info.getLength()).substring(2, 2));
                // Treat 99 as blank
                if (minute != 99)
                {
                    LocalDateTime localDateTime = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), minute, date.getSecond());
                    date = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
                }
        }
        else if (type.equals("MMddyyyy"))
        {
            if (value == null || value.length() < 8)
            {
                return "";
            }
            int month = Integer.parseInt(Truncate(value, info.getLength()).substring(0, 2));
                // Treat 99 as blank
                if (month != 99)
                {
                    LocalDateTime localDateTime = LocalDateTime.of(date.getYear(), month, date.getDayOfMonth(), date.getHour(), date.getMinute(), date.getSecond());
                    date = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
                }

            int day = Integer.parseInt(Truncate(value, info.getLength()).substring(2, 2));
                // Treat 99 as blank
                if (day != 99)
                {
                    LocalDateTime localDateTime = LocalDateTime.of(date.getYear(), date.getMonthValue(), day, date.getHour(), date.getMinute(), date.getSecond());
                    date = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
                }

            int year = Integer.parseInt(Truncate(value, info.getLength()).substring(4, 4));
                // Treat 9999 as blank
                if (year != 9999)
                {
                    LocalDateTime localDateTime = LocalDateTime.of(year, date.getMonthValue(), date.getDayOfMonth(), date.getHour(), date.getMinute(), date.getSecond());
                    date = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
                }
        }
        if (dateOnly)
        {
            return date == null ? null : date.toString("yyyy-MM-dd");
        }
        else if (withTimezoneOffset)
        {
            return date == null ? null : date.toString("o");
        }
        else
        {
            return date == null ? null : date.toString("s");
        }
    }

    /// <summary>Get a value on the DeathCertificateDocument whose type is some part of a DateTime.</summary>
    private String DateTime_Get(String ijeFieldName, String dateTimeType, String fhirFieldName)
    {
        IJEField info = FieldInfo(ijeFieldName);

        String current = null;
        try {
            current = this.record == null ? null : DeathCertificateDocument.class.getField(fhirFieldName).get(this.record).toString();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        OffsetDateTime date = OffsetDateTime.parse(current);
        if (date != null)
        {
            //date = date.ToUniversalTime();
            //date = new OffsetDateTime(date.Year, date.Month, date.Day, date.Hour, date.Minute, date.Second, date.Millisecond, TimeSpan.Zero);
            LocalDateTime localDateTime = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getMonthValue(), date.getHour(), date.getMinute(), date.getSecond());
            date = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
            return Truncate(date.toString(), info.getLength()); //Truncate(date.toString(dateTimeType), info.getLength());
        }
        else
        {
            return StringUtils.repeat(" ", info.getLength());
        }
    }

    /// <summary>Set a value on the DeathCertificateDocument whose type is some part of a DateTime.</summary>
    private void DateTime_Set(String ijeFieldName, String dateTimeType, String fhirFieldName, String value, boolean dateOnly = false, boolean withTimezoneOffset = false)
    {
        IJEField info = FieldInfo(ijeFieldName);
        String current = null;
        try {
            current = DeathCertificateDocument.class.getField(fhirFieldName).get(this.record).toString();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        OffsetDateTime date = OffsetDateTime.parse(current);
        if (current != null && date != null)
        {
           // date = date.ToUniversalTime();
          // date = new OffsetDateTime(date.Year, date.Month, date.Day, date.Hour, date.Minute, date.Second, date.Millisecond, TimeSpan.Zero);
            LocalDateTime localDateTime = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getMonthValue(), date.getHour(), date.getMinute(), date.getSecond());
            date = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
            try {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, DateTimeStringHelper(info, value, dateTimeType, date, dateOnly, withTimezoneOffset));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        else
        {
            LocalDateTime localDateTime = LocalDateTime.of(1, 1, 1, 0, 0, 0, 0);
            date = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);// TimeSpan.Zero);
            try {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, DateTimeStringHelper(info, value, dateTimeType, date, dateOnly, withTimezoneOffset));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /// <summary>Get a value on the DeathCertificateDocument that is a numeric String with the option of being set to all 9s on the IJE side and -1 on the
    /// FHIR side to represent'unknown' and blank on the IJE side and null on the FHIR side to represent unspecified</summary>
    private String NumericAllowingUnknown_Get(String ijeFieldName, String fhirFieldName)
    {
        IJEField info = FieldInfo(ijeFieldName);
        Integer value = null;
        try {
            value = (Integer) DeathCertificateDocument.class.getField(fhirFieldName).get(this.record);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        if (value == null) return StringUtils.repeat(" ", info.getLength()); // No value specified
        if (value == -1) return StringUtils.repeat("9", info.getLength()); // Explicitly set to unknown
        String valueString = value.toString();
        if (valueString.length() > info.getLength())
        {
            validationErrors.add(new StringBuffer("Error: FHIR field ").append(fhirFieldName).append(" contains String '").append(valueString).append("' that is not the expected length for IJE field ").append(ijeFieldName).append(" of length ").append(info.getLength()).append("").toString());
        }
        return  StringUtils.leftPad(Truncate(valueString, info.getLength()), info.getLength(), '0');
    }

    /// <summary>Set a value on the DeathCertificateDocument that is a numeric String with the option of being set to all 9s on the IJE side and -1 on the
    /// FHIR side to represent'unknown' and blank on the IJE side and null on the FHIR side to represent unspecified</summary>
    private void NumericAllowingUnknown_Set(String ijeFieldName, String fhirFieldName, String value)
    {
        IJEField info = FieldInfo(ijeFieldName);
        if (value.equals(StringUtils.repeat(" ", info.getLength())))
        {
            try {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        else if (value.equals(StringUtils.repeat("9", info.getLength())))
        {
            try {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, -1);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        else
        {
            try {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, Integer.parseInt(value));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /// <summary>Get a value on the DeathCertificateDocument that is a time with the option of being set to all 9s on the IJE side and null on the FHIR side to represent null</summary>
    private String TimeAllowingUnknown_Get(String ijeFieldName, String fhirFieldName)
    {
        IJEField info = FieldInfo(ijeFieldName);
        String timeString = null;
        try {
            timeString = (String) DeathCertificateDocument.class.getField(fhirFieldName).get(this.record);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        if (timeString == null) return StringUtils.repeat(" ", info.getLength()); // No value specified
        if (timeString.equals("-1")) return StringUtils.repeat("9", info.getLength()); // Explicitly set to unknown
        OffsetDateTime parsedTime = OffsetDateTime.parse(timeString);
        if (parsedTime != null)
        {
            //TimeSpan timeSpan = new TimeSpan(0, parsedTime.Hour, parsedTime.Minute, parsedTime.Second);
            //return timeSpan.toString(@"hhmm");
//            LocalDateTime startTime = LocalDateTime.of(1900,1,1,0,0,0);
//            LocalDateTime endTime = LocalDateTime.of(1900,1,1, parsedTime.getHour(), parsedTime.getMinute(), parsedTime.getSecond());
//            Period timeSpan = Period.between(startTime, endTime);

            Map map = getHourMinSecFromParsedTime(parsedTime);
            return new StringBuffer(String.valueOf(map.get("mm"))).append("min").append(String.valueOf(map.get("ss"))).append("sec").toString();

        }
        // No valid date found
        validationErrors.add(new StringBuffer("Error: FHIR field ").append(fhirFieldName).append(" contains value '").append(timeString).append("' that cannot be parsed into a time for IJE field ").append(ijeFieldName).toString());
        return StringUtils.repeat(" ", info.getLength());
    }



    /// <summary>Set a value on the DeathCertificateDocument that is a time with the option of being set to all 9s on the IJE side and null on the FHIR side to represent null</summary>
    private void TimeAllowingUnknown_Set(String ijeFieldName, String fhirFieldName, String value)
    {
        IJEField info = FieldInfo(ijeFieldName);
        if (value.equals(StringUtils.repeat(" ", info.getLength())))
        {
            try {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        else if (value.equals(StringUtils.repeat("9", info.getLength())))
        {
            try {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, "-1");
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        else
        {
            OffsetDateTime parsedTime = OffsetDateTime.parse(value, DateTimeFormatter.ofPattern("HHmm"));
            if (parsedTime != null)
            {
               // TimeSpan timeSpan = new TimeSpan(0, parsedTime.Hour, parsedTime.Minute, 0);
                Map map = getHourMinSecFromParsedTime(parsedTime);
                try {
                    DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, new StringBuffer(String.valueOf(map.get("hh"))).append(String.valueOf(map.get("mm"))).append(String.valueOf(map.get("ss"))));// timeSpan.toString()); //@"hh\:mm\:ss"));
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                validationErrors.add(new StringBuffer("Error: FHIR field ").append(fhirFieldName).append(" value of '").append(value).append("' is invalid for IJE field ").append(ijeFieldName).toString());
            }
        }
    }

    /// <summary>Get a value on the DeathCertificateDocument whose IJE type is a right justified, zero filled String.</summary>
    private String RightJustifiedZeroed_Get(String ijeFieldName, String fhirFieldName)
    {
        IJEField info = FieldInfo(ijeFieldName);
        String current = null;
        try {
            current = DeathCertificateDocument.class.getField(fhirFieldName).get(this.record).toString();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (current != null)
        {
            return  StringUtils.leftPad(Truncate(current, info.getLength()), info.getLength(), '0');
        }
        else
        {
            return StringUtils.repeat("0", info.getLength());
        }
    }

    /// <summary>Set a value on the DeathCertificateDocument whose IJE type is a right justified, zero filled String.</summary>
    private void RightJustifiedZeroed_Set(String ijeFieldName, String fhirFieldName, String value) {
        IJEField info = FieldInfo(ijeFieldName);
        try {
            DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, value.replaceFirst("0", ""));//TrimStart('0'));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /// <summary>Get a value on the DeathCertificateDocument whose IJE type is a left justified String.</summary>
    private String LeftJustified_Get(String ijeFieldName, String fhirFieldName)
    {
        IJEField info = FieldInfo(ijeFieldName);
        String current = null;
        try {
            current = DeathCertificateDocument.class.getField(fhirFieldName).get(this.record).toString();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        if (current != null)
        {
            if (current.length() > info.getLength())
            {
                validationErrors.add(new StringBuffer("Error: FHIR field ").append(fhirFieldName).append(" contains String '").append(current).append("' too long for IJE field ").append(ijeFieldName).append(" of length ").append(info.getLength()).toString());
            }
            return StringUtils.rightPad(Truncate(current, info.getLength()), info.getLength(), " ");
        }
        else
        {
            return StringUtils.repeat(" ", info.getLength());
        }
    }

    /// <summary>Set a value on the DeathCertificateDocument whose IJE type is a left justified String.</summary>
    private void LeftJustified_Set(String ijeFieldName, String fhirFieldName, String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            IJEField info = FieldInfo(ijeFieldName);
            //Method method = Arrays.stream(DeathCertificateDocument.class.getMethods()).filter(m->m.getName().equals("set"+StringUtils.capitalize(ijeFieldName))).findFirst().get();//getField(fhirFieldName).(this.record, value.trim());
            try {
                Field field = DeathCertificateDocument.class.getField(ijeFieldName);
                field.setAccessible(true);
                field.set(this.record, value.trim());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /// <summary>Get a value on the DeathCertificateDocument whose property is a Map type.</summary>
    private String Map_Get(String ijeFieldName, String fhirFieldName, String key)
    {
        IJEField info = FieldInfo(ijeFieldName);
        Map<String, String> map = null;
        try {
            map = (Map<String, String>) DeathCertificateDocument.class.getField(fhirFieldName).get(this.record);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        if (map == null || !map.containsKey(key))
        {
            return "";
        }
        String current = map.get(key);
        if (current != null)
        {
            return StringUtils.rightPad(Truncate(current, info.getLength()), info.getLength(), " ");
        }
        else
        {
            return StringUtils.repeat(" ", info.getLength());
        }
    }

    /// <summary>Get a value on the DeathCertificateDocument whose property is a Map type, with NO truncating.</summary>
    private String Map_Get_Full(String ijeFieldName, String fhirFieldName, String key)
    {
        IJEField info = FieldInfo(ijeFieldName);
        Map<String, String> map = null;
        try {
            map = (Map<String, String>) DeathCertificateDocument.class.getField(fhirFieldName).get(this.record);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (map != null && map.containsKey(key))
        {
            String current = map.get(key).toString();
            if (current != null)
            {
                return current;
            }
            else
            {
                return "";
            }
        }
        return "";
    }

    /// <summary>Set a value on the DeathCertificateDocument whose property is a Map type.</summary>
    private void Map_Set(String ijeFieldName, String fhirFieldName, String key, String value)
    {
        IJEField info = FieldInfo(ijeFieldName);
        Map<String, String> map = null;
        try {
            map = (Map<String, String>) DeathCertificateDocument.class.getField(fhirFieldName).get(this.record);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (map == null)
        {
            map = new HashMap<String, String>();
        }
        if (!isNullOrWhiteSpace(value))
        {
            map.put(key, value.trim());
        }

        try {
            DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, map);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /// <summary>Get a value on the DeathCertificateDocument whose property is a geographic type (and is contained in a map).</summary>
    private String Map_Geo_Get(String ijeFieldName, String fhirFieldName, String keyPrefix, String geoType, boolean isCoded)
    {
        IJEField info = FieldInfo(ijeFieldName);
        Map<String, String> map = this.record == null ? null : (Map<String, String>)DeathCertificateDocument.class.getField(fhirFieldName).GetValue(this.record);
        String key = keyPrefix + char.toUpperCase(geoType[0]) + geoType.substring(1);
        if (map == null || !map.containsKey(key))
        {
            return StringUtils.repeat(" ", info.getLength());
        }
        String current = map.get(key).toString();
        if (isCoded)
        {
            if (geoType.equals("insideCityLimits"))
            {
                if (isNullOrWhiteSpace(current))
                {
                    current = "U";
                }
                else if (current.equals("true") || current.equals("True"))
                {
                    current = "Y";
                }
                else if (current.equals("false") || current.equals("False"))
                {
                    current = "N";
                }
            }
            else if (geoType.equals("countyC") || geoType.equals("cityC"))
            {
                current =  StringUtils.leftPad(Truncate(current, info.getLength()), info.getLength(), '0');
            }
        }

        if (geoType.equals("zip"))
        {  // Remove "-" for zip
            current.replace("-", "");
        }
        if (current != null)
        {
            return StringUtils.rightPad(Truncate(current, info.getLength()), info.getLength(), " ");
        }
        else
        {
            return StringUtils.repeat(" ", info.getLength());
        }
    }

    /// <summary>Set a value on the DeathCertificateDocument whose property is a geographic type (and is contained in a map).</summary>
    private void Map_Geo_Set(String ijeFieldName, String fhirFieldName, String keyPrefix, String geoType, boolean isCoded, String value)
    {
        IJEField info = FieldInfo(ijeFieldName);
        Map<String, String> map = null;
        try {
            map = (Map<String, String>) DeathCertificateDocument.class.getField(fhirFieldName).get(this.record);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        String key = keyPrefix + char.toUpperCase(geoType[0]) + geoType.substring(1);

        // if the value is null, and the Map does not exist, return
        if (map == null && isNullOrWhiteSpace(value))
        {
            return;
        }
        // initialize the Map if it does not exist
        if (map == null)
        {
            map = new HashMap<String, String>();
        }

        if (!map.containsKey(key) || isNullOrWhiteSpace(map.get(key)))
        {
            if (isCoded)
            {
                if (geoType.equals("insideCityLimits"))
                {
                    if (!isNullOrWhiteSpace(value) && value.equals("N"))
                    {
                        map.put(key, "False");
                    }
                }
                else
                {
                    map.put(key, value.trim());
                }
            }
            else
            {
                map.put(key, value.trim());
            }
        }
        else
        {
            map.put(key, value.trim());
        }
        try {
            DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, map);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /// <summary>Checks if the given race exists in the record.</summary>
    private String Get_Race(String name)
    {
        Tuple<String, String>[] raceStatus = record.Race.ToArray();

        Tuple<String, String> raceTuple = Array.Find(raceStatus, element -> element.getItem1().equals(name));
        if (raceTuple != null)
        {
            return (raceTuple.getItem2()).trim();
        }
        return "";
    }

    /// <summary>Adds the given race to the record.</summary>
    private void Set_Race(String name, String value)
    {
        List<Tuple<String, String>> raceStatus = Arrays.asList(record.getRace());
        raceStatus.add(Tuple.Create(name, value));
        record.setRace(raceStatus.get(0).toString());
    }

    // /// <summary>Gets a "Yes", "No", or "Unknown" value.</summary>
    // private String Get_YNU(String fhirFieldName)
    // {
    //     object status = DeathCertificateDocument.class.getField(fhirFieldName).GetValue(this.record);
    //     if (status == null)
    //     {
    //         return "U";
    //     }
    //     else
    //     {
    //         return ((bool)status) ? "Y" : "N";
    //     }
    // }

    // /// <summary>Sets a "Yes", "No", or "Unkown" value.</summary>
    // private void Set_YNU(String fhirFieldName, String value)
    // {
    //     if (value != "U" && value == "Y")
    //     {
    //         DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, true);
    //     }
    //     else if (value != "U" && value == "N")
    //     {
    //         DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, false);
    //     }
    // }

    /// <summary>Given a Map mapping FHIR codes to IJE Strings and the relevant FHIR and IJE fields pull the value
    /// from the FHIR record object and provide the appropriate IJE String</summary>
    /// <param name="mapping">Map for mapping the desired concept from FHIR to IJE; these dictionaries are defined in Mappings.cs</param>
    /// <param name="fhirField">Name of the FHIR field to get from the record; must have a related Helper property, e.g., EducationLevel must have EducationLevelHelper</param>
    /// <param name="ijeField">Name of the IJE field that the FHIR field content is being placed into</param>
    /// <returns>The IJE value of the field translated from the FHIR value on the record</returns>
    private String Get_MappingFHIRToIJE(Map<String, String> mapping, String fhirField, String ijeField)
    {
        Field helperProperty = null;
        try {
            helperProperty = DeathCertificateDocument.class.getField(fhirField + "Helper");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        if (helperProperty == null)
        {
            throw new NullPointerException(new StringBuffer("No helper method found called '").append(fhirField).append("Helper'").toString());
        }
        String fhirCode = (String)helperProperty.get(this.record);
        if (isNullOrWhiteSpace(fhirCode))
        {
            return "";
        }
        try
        {
            return mapping.get(fhirCode);
        }
        catch (NoSuchElementException)
        {
            switch (ijeField)
            {
                case "COD":
                    ijeField = "County of Death";
                    break;
                case "COD1A":
                    ijeField = "Cause of Death-1A";
                    break;
                case "COD1B":
                    ijeField = "Cause of Death-1B";
                    break;
                case "COD1C":
                    ijeField = "Cause of Death-1C";
                    break;
                case "COD1D":
                    ijeField = "Cause of Death-1D";
                    break;
                default:
                    break;
            }
            validationErrors.add(new StringBuffer("Error: Unable to find IJE ").append(ijeField).append(" mapping for FHIR ").append(fhirField).append(" field value '").append(fhirCode).toString());
            return "";
        }

    }

    /// <summary>Given a Map mapping IJE codes to FHIR Strings and the relevant IJE and FHIR fields translate the IJE
    /// String to the appropriate FHIR code and set the value on the FHIR record object</summary>
    /// <param name="mapping">Map for mapping the desired concept from IJE to FHIR; these dictionaries are defined in Mappings.cs</param>
    /// <param name="ijeField">Name of the IJE field that the FHIR field content is being set from</param>
    /// <param name="fhirField">Name of the FHIR field to set on the record; must have a related Helper property, e.g., EducationLevel must have EducationLevelHelper</param>
    /// <param name="value">The value to translate from IJE to FHIR and set on the record</param>
    private void Set_MappingIJEToFHIR(Map<String, String> mapping, String ijeField, String fhirField, String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            try
            {
                Field helperProperty = DeathCertificateDocument.class.getField(fhirField +"Helper");
                if (helperProperty == null)
                {
                    throw new NullPointerException(new StringBuffer("No helper method found called '").append(fhirField).append("Helper'").toString());
                }
                helperProperty.set(this.record, mapping.get(value));
            }
            catch (NoSuchElementException e) {
                validationErrors.add(new StringBuffer("Error: Unable to find FHIR ").append(fhirField).append(" mapping for IJE ").append(ijeField).append(" field value '").append(value).append("'").toString());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /// <summary>NCHS ICD10 to actual ICD10 </summary>
    private String NCHSICD10toActualICD10(String nchsicd10code)
    {
        String code = "";

        if (!isNullOrEmpty(nchsicd10code))
        {
            if (ValidNCHSICD10(nchsicd10code.trim()))
            {
                code = nchsicd10code.trim();
            }
            else
            {
                throw new IllegalArgumentException(new StringBuffer().append("NCHS ICD10 code ").append(nchsicd10code).append(" is invalid.").toString());
            }

        }

        if (code.length() >= 4)    // codes of length 4 or 5 need to have a decimal inserted
        {
           // code = nchsicd10code.insert(3, ".");
            code = new StringBuilder(nchsicd10code).insert(3, ".").toString();
        }

        return (code);
    }
    /// <summary>Actual ICD10 to NCHS ICD10 </summary>
    private String ActualICD10toNCHSICD10(String icd10code)
    {
        if (!isNullOrEmpty(icd10code))
        {
            return (icd10code.replace(".", ""));
        }
        else
        {
            return "";
        }
    }

    /// <summary>Actual ICD10 to NCHS ICD10 </summary>
    public static boolean ValidNCHSICD10(String nchsicd10code)
    {
        // ICD-10 diagnosis codes always begin with a letter followed by a digit.
        // The third character is usually a digit, but could be an A or B [1].
        // After the first three characters, there may be a decimal point, and up to three more alphanumeric characters.
        // Sometimes the decimal is left out.
        // NCHS ICD10 codes are the same as above for the first three characters.
        // The decimal point is always dropped.
        // Some codes have a fourth character that reflects an actual ICD10 code.
        // NCHS tacks on an extra character to some ICD10 codes, e.g., K7210 (K27.10)
        ///Regex NCHSICD10regex = new Regex(@"^[A-Z][0-9][0-9AB][0-9A-Z]{0,2}$");
        Pattern pattern = Pattern.compile("^[A-Z][0-9][0-9AB][0-9A-Z]{0,2}$s", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher("Visit W3Schools!");

        //return (DeathCertificateDocumentUtil.isNullOrEmpty(nchsicd10code) || NCHSICD10regex.Match(nchsicd10code).Success);
        return (isNullOrEmpty(nchsicd10code) || pattern.matcher(nchsicd10code).find());

    }


    /////////////////////////////////////////////////////////////////////////////////
    //
    // Class Properties that provide getters and setters for each of the IJE
    // Mortality fields.
    //
    // Getters look at the embedded DeathCertificateDocument and convert values to IJE style.
    // Setters convert and store IJE style values to the embedded DeathCertificateDocument .
    //
    /////////////////////////////////////////////////////////////////////////////////

    /// <summary>Date of Death--Year</summary>
    IJEField DOD_YR = new IJEField(1, 1, 4, "Date of Death--Year", "DOD_YR", 1);
    public String getDOD_YR()
    {
        return NumericAllowingUnknown_Get("DOD_YR", "DeathYear");
    }
    public void setDOD_YR(String value)
    {
        NumericAllowingUnknown_Set("DOD_YR", "DeathYear", value);
    }

    /// <summary>State, U.S. Territory or Canadian Province of Death - code</summary>
    IJEField DSTATE = new IJEField(2, 5, 2, "State, U.S. Territory or Canadian Province of Death - code", "DSTATE", 1);
    public String getDSTATE()
    {
        String value = LeftJustified_Get("DSTATE", "DeathLocationJurisdiction");
        if (isNullOrWhiteSpace(value))
        {
        validationErrors.add("Error: FHIR field DeathLocationJurisdiction is blank, which is invalid for IJE field DSTATE.");
        }
        else if (dataLookup.JurisdictionNameToJurisdictionCode(value) == null)
        {
            validationErrors.add(new StringBuffer("Error: FHIR field DeathLocationJurisdiction has value '").append(value).append("', which is invalid for IJE field DSTATE.").;
        }
        return value;
    }
    public void setDSTATE(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("DSTATE", "DeathLocationJurisdiction", value);
            // We used to state the DeathLocationAddress here as well, but that's now handled in DeathCertificateDocument
            // Map_Set("STATEC", "DeathLocationAddress", "addressState", value);
        }
    }

    /// <summary>Certificate Number</summary>
    IJEField FILENO = new IJEField(3, 7, 6, "Certificate Number", "FILENO", 1);
    public String getFILENO()
    {
        if (isNullOrWhiteSpace(record != null ? record.getIdentifier() : null))
        {
            return  StringUtils.leftPad("", 6, '0');
        }
        String id_str = record.getIdentifier();
        if (id_str.length() > 6)
        {
            id_str = id_str.substring(id_str.length() - 6);
        }
        return  StringUtils.leftPad(id_str, 6, '0');
    }
    public void setFILENO(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            RightJustifiedZeroed_Set("FILENO", "Identifier", value);
        }
    }

    /// <summary>Void flag</summary>
    IJEField VOID = new IJEField(4, 13, 1, "Void flag", "VOID", 1);
    public String getVOID()
    {
        return _void;
    }
    public void setVOID(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            String valueTrim = value.trim();
            if(valueTrim.equals("0") || valueTrim.equals("1"))
            {
                _void = valueTrim;
            }
        }
    }

    /// <summary>Auxiliary State file number</summary>
    IJEField AUXNO = new IJEField(5, 14, 12, "Auxiliary State file number", "AUXNO", 1);
    public String getAUXNO()
    {
        if (record.getStateLocalIdentifier1() == null)
        {
            return StringUtils.repeat(" ", 12);
        }
        return LeftJustified_Get("AUXNO", "StateLocalIdentifier1");
    }
    public void setAUXNO(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            value =  StringUtils.leftPad(value, 12 , '0');
            LeftJustified_Set("AUXNO", "StateLocalIdentifier1", value);
        }
    }

    /// <summary>Source flag: paper/electronic</summary>
    IJEField MFILED = new IJEField(6, 26, 1, "Source flag: paper/electronic", "MFILED", 1);
    public String getMFILED()
    {
        return Get_MappingFHIRToIJE(Mappings.FilingFormat.FHIRToIJE, "FilingFormat", "MFILED");
    }
    public void setMFILED(String value)
    {
        Set_MappingIJEToFHIR(Mappings.FilingFormat.IJEToFHIR, "MFILED", "FilingFormat", value);
    }

    /// <summary>Decedent's Legal Name--Given</summary>
    IJEField GNAME = new IJEField(7, 27, 50, "Decedent's Legal Name--Given", "GNAME", 1);
    public String getGNAME()
    {
        String[] names = record.getGivenNames();
        if (names.length > 0)
        {
            return StringUtils.rightPad(Truncate(names[0], 50), 50, " ");
        }
        return StringUtils.repeat(" ", 50);
    }
    public void setGNAME(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setGivenNames(new String[] { value.trim() });
        }
    }

    /// <summary>Decedent's Legal Name--Middle</summary>
    IJEField MNAME = new IJEField(8, 77, 1, "Decedent's Legal Name--Middle", "MNAME", 2);
    public String getMNAME()
    {
        String[] names = record.getGivenNames();
        if (names.length > 1)
        {
            return StringUtils.rightPad(Truncate(names[1], 1), 1, " ");
        }
        return " ";
    }
    public void setMNAME(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            if (isNullOrWhiteSpace(GNAME)) throw new IllegalArgumentException("Middle name cannot be set before first name");
            if (isNullOrWhiteSpace(DMIDDLE))
            {
                if (record.getGivenNames() != null)
                {
                    List<String> names = Arrays.asList(record.getGivenNames());
                    if (names.size() > 1)
                        names.set(1, value.trim());
                    else
                        names.add(value.trim());
                    record.setGivenNames((String[]) names.toArray());
                }
            }
        }
    }

    /// <summary>Decedent's Legal Name--Last</summary>
    IJEField LNAME = new IJEField(9, 78, 50, "Decedent's Legal Name--Last", "LNAME", 1);
    public String getLNAME()
    {
        if (!isNullOrWhiteSpace(record.getFamilyName()))
        {
            return LeftJustified_Get("LNAME", "FamilyName");
        }
        else
        {
            return "UNKNOWN";
        }
    }
    public void setLNAME(String value)
    {
        if (value.equals("UNKNOWN"))
        {
            Set_MappingIJEToFHIR(Mappings.AdministrativeGender.IJEToFHIR, "LNAME", "FamilyName", null);
        }
        else
        {
            LeftJustified_Set("LNAME", "FamilyName", value);
        }
    }

    /// <summary>Decedent's Legal Name--Suffix</summary>
    IJEField SUFF = new IJEField(10, 128, 10, "Decedent's Legal Name--Suffix", "SUFF", 1);
    public String getSUFF()
    {
        return LeftJustified_Get("SUFF", "Suffix");
    }
    public void setSUFF(String value)
    {
        LeftJustified_Set("SUFF", "Suffix", value);
    }

    /// <summary>Decedent's Legal Name--Alias</summary>
    IJEField ALIAS = new IJEField(11, 138, 1, "Decedent's Legal Name--Alias", "ALIAS", 1);
    public String getALIAS()
    {
        return _alias;
    }
    public void setALIAS(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            String valueTrim = value.trim();
            if(valueTrim.equals("0") || valueTrim.equals("1"))
            {
                _alias = valueTrim;
            }
        }
    }

    /// <summary>Father's Surname</summary>
    IJEField FLNAME = new IJEField(12, 139, 50, "Father's Surname", "FLNAME", 1);
    public String getFLNAME()
    {
        return LeftJustified_Get("FLNAME", "FatherFamilyName");
    }
    public void setFLNAME(String value)
    {
        LeftJustified_Set("FLNAME", "FatherFamilyName", value);
    }

    /// <summary>Sex</summary>
    IJEField SEX = new IJEField(13, 189, 1, "Sex", "SEX", 1);
    public String getSEX()
    {
        return Get_MappingFHIRToIJE(Mappings.AdministrativeGender.FHIRToIJE, "SexAtDeath", "SEX");
    }
    public void setSEX(String value)
    {
        Set_MappingIJEToFHIR(Mappings.AdministrativeGender.IJEToFHIR, "SEX", "SexAtDeath", value);
    }

    /// <summary>Sex--Edit Flag</summary>
    IJEField SEX_BYPASS = new IJEField(14, 190, 1, "Sex--Edit Flag", "SEX_BYPASS", 1);
    public String getSEX_BYPASS()
    {
        return ""; // Blank
    }
    public void setSEX_BYPASS(String value)
    {
        // NOOP
    }

    /// <summary>Social Security Number</summary>
    IJEField SSN = new IJEField(15, 191, 9, "Social Security Number", "SSN", 1);
    public String getSSN()
    {
        String fhirFieldName = "SSN";
        String ijeFieldName = "SSN";
        int ssnLength = 9;
        String ssn = record.getSSN();
        if (!isNullOrWhiteSpace(ssn))
        {
            String formattedSSN = ssn.replace("-", "").replace(" ", "");
            if (formattedSSN.length() != ssnLength)
            {
                validationErrors.add(new StringBuffer("Error: FHIR field ").append(fhirFieldName).append(" contains String '").append(ssn).append("' which is not the expected length (without dashes or spaces) for IJE field ").append(ijeFieldName).append(" of length ").append(ssnLength).toString());
            }
            return StringUtils.rightPad(Truncate(formattedSSN, ssnLength), ssnLength, " ");
        }
        else
        {
            return StringUtils.repeat(" ", ssnLength);
        }
    }

    public void setSSNString (String value)
    {
        String fhirFieldName = "SSN";
        String ijeFieldName = "SSN";
        int ssnLength = 9;
        if (!isNullOrWhiteSpace(value))
        {
            String ssn = value.trim();
            if (ssn.contains("-") || ssn.contains(" "))
            {
                validationErrors.add(new StringBuffer("Error: IJE field ").append(ijeFieldName).append(" contains String '").append(value).append("' which cannot contain ` ` or `-` characters for FHIR field ").append(fhirFieldName).toString());
            }
            String formattedSSN = ssn.replace("-", "").replace(" ", "");
            if (formattedSSN.length() != ssnLength)
            {
                validationErrors.add(new StringBuffer("Error: IJE field ").append(ijeFieldName).append(" contains String '").append(value).append("' which is not the expected length (without dashes or spaces) for FHIR field ").append(fhirFieldName).append(" of length ").append(ssnLength).toString());
            }
        }
        LeftJustified_Set(ijeFieldName, fhirFieldName, value);
    }

    /// <summary>Decedent's Age--Type</summary>
    IJEField AGETYPE = new IJEField(16, 200, 1, "Decedent's Age--Type", "AGETYPE", 1);
    public String getAGETYPE()
    {
        // Pull code from coded unit.   "code" field is not required by VRDR IG
        String code = Map_Get_Full("AGETYPE", "AgeAtDeath", "code") ?? "";
        Mappings.UnitsOfAge.FHIRToIJE.TryGetValue(code, out String ijeValue);
        return ijeValue ?? "9";
    }
    public void setAGETYPE(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;  // nothing to do
        }
        // If we have an IJE value map it to FHIR and set the unit, code and system appropriately, otherwise set to unknown
        if (!Mappings.UnitsOfAge.IJEToFHIR.TryGetValue(value, out String fhirValue))
        {
            // We have an invalid code, map it to unknown
            fhirValue = ValueSets.UnitsOfAge.Unknown;
        }
        // We have the code, now we need the corresponding unit and system
        // Iterate over the allowed options and see if the code supplies is one of them
        int length = ValueSets.UnitsOfAge.Codes.length;
        for (int i = 0; i < length; i += 1)
        {
            if (ValueSets.UnitsOfAge.Codes[i, 0].equals(fhirValue))
            {
                // Found it, so call the supplied setter with the appropriate Map built based on the code
                // using the supplied options and return
                Map<String, String> map = new HashMap<String, String>();
                map.put("code", fhirValue);
                map.put("unit", ValueSets.UnitsOfAge.Codes[i, 1]);
                map.put("system", ValueSets.UnitsOfAge.Codes[i, 2]);
                try {
                    DeathCertificateDocument.class.getField("AgeAtDeath").set(this.record, map);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
        }
    }

    /// <summary>Decedent's Age--Units</summary>
    IJEField metaAGE = new IJEField(17, 201, 3, "Decedent's Age--Units", "AGE", 2);

    public String getAGE()
    {
        if ((record.getDecedentAge().get(0) != null) && !this.AGETYPE.equals("9"))
        {
            // IJEField info = FieldInfo("AGE");
            return  StringUtils.leftPad(Truncate(record.getDecedentAge().get(0), AGE), metaAGE.getLength(), '0');
        }
        else
        {
            return "999";
        }
    }
    public void setAGE(String value)
    {
        Map_Set("AGE", "AgeAtDeath", "value", value.replaceFirst("0", ""));
    }

    /// <summary>Decedent's Age--Edit Flag</summary>
    IJEField metaAGE_BYPASS = new IJEField(18, 204, 1, "Decedent's Age--Edit Flag", "AGE_BYPASS", 1);
    public String getAGE_BYPASS()
    {
        return Get_MappingFHIRToIJE(Mappings.EditBypass01.FHIRToIJE, "AgeAtDeathEditFlag", "AGE_BYPASS");
    }
    public void setAGE_BYPASS(String value)
    {
        Set_MappingIJEToFHIR(Mappings.EditBypass01.IJEToFHIR, "AGE_BYPASS", "AgeAtDeathEditFlag", value);
    }

    /// <summary>Date of Birth--Year</summary>
    IJEField metaDOB_YR = new IJEField(19, 205, 4, "Date of Birth--Year", "DOB_YR", 1);
    public String getDOB_YR()
    {
        return NumericAllowingUnknown_Get("DOB_YR", "BirthYear");
    }
    public void setDOB_YR(String value)
    {
        NumericAllowingUnknown_Set("DOB_YR", "BirthYear", value);
    }

    /// <summary>Date of Birth--Month</summary>
    IJEField metaDOB_MO = new IJEField(20, 209, 2, "Date of Birth--Month", "DOB_MO", 1);
    public String getDOB_MO()
    {
        return NumericAllowingUnknown_Get("DOB_MO", "BirthMonth");
    }
    public void setDOB_MO(String value)
    {
        NumericAllowingUnknown_Set("DOB_MO", "BirthMonth", value);
    }

    /// <summary>Date of Birth--Day</summary>
    IJEField metaDOB_DY = new IJEField(21, 211, 2, "Date of Birth--Day", "DOB_DY", 1);
    public String getDOB_DY()
    {
        return NumericAllowingUnknown_Get("DOB_DY", "BirthDay");
    }
    public void setDOB_DY(String value)
    {
        NumericAllowingUnknown_Set("DOB_DY", "BirthDay", value);
    }

    /// <summary>Birthplace--Country</summary>
    IJEField metaBPLACE_CNT = new IJEField(22, 213, 2, "Birthplace--Country", "BPLACE_CNT", 1);
    public String getBPLACE_CNT()
    {
        return Map_Geo_Get("BPLACE_CNT", "PlaceOfBirth", "address", "country", true);
    }
    public void setBPLACE_CNT(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Set("BPLACE_CNT", "PlaceOfBirth", "addressCountry", value);
        }
    }

    /// <summary>State, U.S. Territory or Canadian Province of Birth - code</summary>
    IJEField metaBPLACE_ST = new IJEField(23, 215, 2, "State, U.S. Territory or Canadian Province of Birth - code", "BPLACE_ST", 1);
    public String getBPLACE_ST()
    {
        return Map_Geo_Get("BPLACE_ST", "PlaceOfBirth", "address", "state", true);
    }
    public void setBPLACE_ST(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("BPLACE_ST", "PlaceOfBirth", "address", "state", true, value);
        }
    }

    /// <summary>Decedent's Residence--City</summary>
    IJEField metaCITYC = new IJEField(24, 217, 5, "Decedent's Residence--City", "CITYC", 3);
    public String getCITYC()
    {
        return Map_Geo_Get("CITYC", "Residence", "address", "cityC", true);
    }
    public void setCITYC(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("CITYC", "Residence", "address", "cityC", true, value);
        }
    }

    /// <summary>Decedent's Residence--County</summary>
    IJEField metaCOUNTYC = new IJEField(25, 222, 3, "Decedent's Residence--County", "COUNTYC", 2);
    public String getCOUNTYC()
    {
        return Map_Geo_Get("COUNTYC", "Residence", "address", "countyC", true);
    }
    public void setCOUNTYC(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("COUNTYC", "Residence", "address", "countyC", true, value);
        }
    }

    /// <summary>State, U.S. Territory or Canadian Province of Decedent's residence - code</summary>
    IJEField metaSTATEC = new IJEField(26, 225, 2, "State, U.S. Territory or Canadian Province of Decedent's residence - code", "STATEC", 1);
    public String getSTATEC()
    {
        return Map_Geo_Get("STATEC", "Residence", "address", "State", true);
    }
    public void setSTATEC(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Set("STATEC", "Residence", "addressState", value);
        }
    }

    /// <summary>Decedent's Residence--Country</summary>
    IJEField metaCOUNTRYC = new IJEField(27, 227, 2, "Decedent's Residence--Country", "COUNTRYC", 1);
    public String getCOUNTRYC()
    {
        return Map_Geo_Get("COUNTRYC", "Residence", "address", "country", true); // NVSS-234 -- use 2 letter encoding for country, so no translation.
    }
    public void setCOUNTRYC(String value)
    {
        if (!isNullOrWhiteSpace(value)) // need to filter out countries that are excluded as residences because they are defunct, e.g., "UR"
        {
            Map_Geo_Set("COUNTRYC", "Residence", "address", "country", true, value); // NVSS-234 -- use 2 letter encoding for country, so no translation.
        }
    }

    /// <summary>Decedent's Residence--Inside City Limits</summary>
    IJEField metaLIMITS = new IJEField(28, 229, 1, "Decedent's Residence--Inside City Limits", "LIMITS", 10);
    public String getLIMITS()
    {
        return Get_MappingFHIRToIJE(Mappings.YesNoUnknown.FHIRToIJE, "ResidenceWithinCityLimits", "LIMITS");
    }
    public void setLIMITS(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_MappingIJEToFHIR(Mappings.YesNoUnknown.IJEToFHIR, "LIMITS", "ResidenceWithinCityLimits", value);
        }
    }

    /// <summary>Marital Status</summary>
    IJEField metaMARITAL = new IJEField(29, 230, 1, "Marital Status", "MARITAL", 1);
    public String getMARITAL()
    {
        return Get_MappingFHIRToIJE(Mappings.MaritalStatus.FHIRToIJE, "MaritalStatus", "MARITAL");
    }
    public void setMARITAL(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_MappingIJEToFHIR(Mappings.MaritalStatus.IJEToFHIR, "MARITAL", "MaritalStatus", value);
        }
    }

    /// <summary>Marital Status--Edit Flag</summary>
    IJEField metaMARITAL_BYPASS = new IJEField(30, 231, 1, "Marital Status--Edit Flag", "MARITAL_BYPASS", 1);
    public String getMARITAL_BYPASS()
    {
        return Get_MappingFHIRToIJE(Mappings.EditBypass0124.FHIRToIJE, "MaritalStatusEditFlag", "MARITAL_BYPASS");
    }
    public void setMARITAL_BYPASS(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_MappingIJEToFHIR(Mappings.EditBypass0124.IJEToFHIR, "MARITAL_BYPASS", "MaritalStatusEditFlag", value);
        }
    }

    /// <summary>Place of Death</summary>
    IJEField metaDPLACE = new IJEField(31, 232, 1, "Place of Death", "DPLACE", 1);
    public String getDPLACE()
    {
        return Get_MappingFHIRToIJE(Mappings.PlaceOfDeath.FHIRToIJE, "DeathLocationType", "DPLACE");
    }
    public void setDPLACE(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_MappingIJEToFHIR(Mappings.PlaceOfDeath.IJEToFHIR, "DPLACE", "DeathLocationType", value);
        }
    }

    /// <summary>County of Death Occurrence</summary>
    IJEField metaCOD = new IJEField(32, 233, 3, "County of Death Occurrence", "COD", 2);
    public String getCOD()
    {
        return Map_Geo_Get("COD", "DeathLocationAddress", "address", "countyC", true);
    }
    public void setCOD(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("COD", "DeathLocationAddress", "address", "countyC", true, value);
        }
    }

    /// <summary>Method of Disposition</summary>
    IJEField metaDISP = new IJEField(33, 236, 1, "Method of Disposition", "DISP", 1);
    public String getDISP()
    {
        return Get_MappingFHIRToIJE(Mappings.MethodOfDisposition.FHIRToIJE, "DecedentDispositionMethod", "DISP");
    }
    public void setDISP(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_MappingIJEToFHIR(Mappings.MethodOfDisposition.IJEToFHIR, "DISP", "DecedentDispositionMethod", value);
        }
    }

    /// <summary>Date of Death--Month</summary>
    IJEField metaDOD_MO = new IJEField(34, 237, 2, "Date of Death--Month", "DOD_MO", 1);
    public String getDOD_MO()
    {
        return NumericAllowingUnknown_Get("DOD_MO", "DeathMonth");
    }
    public void setDOD_MO(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            NumericAllowingUnknown_Set("DOD_MO", "DeathMonth", value);
        }
    }

    /// <summary>Date of Death--Day</summary>
    IJEField metaDOD_DY = new IJEField(35, 239, 2, "Date of Death--Day", "DOD_DY", 1);
    public String getDOD_DY()
    {
        return NumericAllowingUnknown_Get("DOD_DY", "DeathDay");
    }
    public void setDOD_DY(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            NumericAllowingUnknown_Set("DOD_DY", "DeathDay", value);
        }
    }

    /// <summary>Time of Death</summary>
    IJEField metaTOD = new IJEField(36, 241, 4, "Time of Death", "TOD", 1);
    public String getTOD()
    {
        return TimeAllowingUnknown_Get("TOD", "DeathTime");
    }
    public void setTOD(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            TimeAllowingUnknown_Set("TOD", "DeathTime", value);
        }
    }

    /// <summary>Decedent's Education</summary>
    IJEField metaDEDUC = new IJEField(37, 245, 1, "Decedent's Education", "DEDUC", 1);
    public String getDEDUC()
    {
        return Get_MappingFHIRToIJE(Mappings.EducationLevel.FHIRToIJE, "EducationLevel", "DEDUC");
    }
    public void setDEDUC(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_MappingIJEToFHIR(Mappings.EducationLevel.IJEToFHIR, "DEDUC", "EducationLevel", value);
        }
    }

    /// <summary>Decedent's Education--Edit Flag</summary>
    IJEField metaDEDUC_BYPASS = new IJEField(38, 246, 1, "Decedent's Education--Edit Flag", "DEDUC_BYPASS", 1);
    public String getDEDUC_BYPASS()
    {
        return Get_MappingFHIRToIJE(Mappings.EditBypass01234.FHIRToIJE, "EducationLevelEditFlag", "DEDUC_BYPASS");
    }
    public void setDEDUC_BYPASS(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_MappingIJEToFHIR(Mappings.EditBypass01234.IJEToFHIR, "DEDUC_BYPASS", "EducationLevelEditFlag", value);
        }
    }

    // The DETHNIC functions handle unknown ethnicity as follows
    // All of the DETHNIC fields have to be unknown, U, for the ethnicity json data to be empty ex. UUUU
    // Individual "Unknown" DETHNIC fields cannot be preserved in a roundtrip, only UUUU will return UUUU
    // If at least one DETHNIC field is H, the json data should show Hispanic or Latino ex. NNHN will return NNHN
    // If at least one DETHNIC field is N and no fields are H, the json data should show Non-Hispanic or Latino ex. UUNU will return NNNN
    /// <summary>Decedent of Hispanic Origin?--Mexican</summary>
    IJEField metaDETHNIC1 = new IJEField(39, 247, 1, "Decedent of Hispanic Origin?--Mexican", "DETHNIC1", 1);
    public String getDETHNIC1()
    {
        String code = Get_MappingFHIRToIJE(Mappings.YesNoUnknown.FHIRToIJE, "Ethnicity1", "DETHNIC1");
        if (code.equals("Y"))
        {
            code = "H";
        }
        if (isNullOrWhiteSpace(code))
        {
            code = "U";
        }
        return code;
    }
    public void setDETHNIC1(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            if (value.equals("H"))
            {
                value = "Y";
            }
            Set_MappingIJEToFHIR(Mappings.YesNoUnknown.IJEToFHIR, "DETHNIC1", "Ethnicity1", value);
        }
    }

    /// <summary>Decedent of Hispanic Origin?--Puerto Rican</summary>
    IJEField metaDETHNIC2 = new IJEField(40, 248, 1, "Decedent of Hispanic Origin?--Puerto Rican", "DETHNIC2", 1);
    public String getDETHNIC2()
    {
        String code = Get_MappingFHIRToIJE(Mappings.YesNoUnknown.FHIRToIJE, "Ethnicity2", "DETHNIC2");
        if (code.equals("Y"))
        {
            code = "H";
        }
        if (isNullOrWhiteSpace(code))
        {
            code = "U";
        }
        return code;
    }
    public void setDETHNIC2(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            if (value.equals("H"))
            {
                value = "Y";
            }
            Set_MappingIJEToFHIR(Mappings.YesNoUnknown.IJEToFHIR, "DETHNIC2", "Ethnicity2", value);
        }
    }

    /// <summary>Decedent of Hispanic Origin?--Cuban</summary>
    IJEField metaDETHNIC3 = new IJEField(41, 249, 1, "Decedent of Hispanic Origin?--Cuban", "DETHNIC3", 1);
    public String getDETHNIC3()
    {
        String code = Get_MappingFHIRToIJE(Mappings.YesNoUnknown.FHIRToIJE, "Ethnicity3", "DETHNIC3");
        if (code.equals("Y"))
        {
            code = "H";
        }
        if (isNullOrWhiteSpace(code))
        {
            code = "U";
        }
        return code;
    }
    public void setDETHNIC3(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            if (value.equals("H"))
            {
                value = "Y";
            }
            Set_MappingIJEToFHIR(Mappings.YesNoUnknown.IJEToFHIR, "DETHNIC3", "Ethnicity3", value);
        }
    }

    /// <summary>Decedent of Hispanic Origin?--Other</summary>
    IJEField metaDETHNIC4 = new IJEField(42, 250, 1, "Decedent of Hispanic Origin?--Other", "DETHNIC4", 1);
    public String getDETHNIC4()
    {
        String code = Get_MappingFHIRToIJE(Mappings.YesNoUnknown.FHIRToIJE, "Ethnicity4", "DETHNIC4");
        if (code.equals("Y"))
        {
            code = "H";
        }
        if (isNullOrWhiteSpace(code))
        {
            code = "U";
        }
        return code;
    }
    public void setDETHNIC4(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            if (value.equals("H"))
            {
                value = "Y";
            }
            Set_MappingIJEToFHIR(Mappings.YesNoUnknown.IJEToFHIR, "DETHNIC4", "Ethnicity4", value);
        }
    }

    /// <summary>Decedent of Hispanic Origin?--Other, Literal</summary>
    IJEField metaDETHNIC5 = new IJEField(43, 251, 20, "Decedent of Hispanic Origin?--Other, Literal", "DETHNIC5", 1);
    public String getDETHNIC5()
    {
        String ethnicityLiteral = record.getEthnicityLiteral();
        if (!isNullOrWhiteSpace(ethnicityLiteral))
        {
            return Truncate(ethnicityLiteral, 20).trim();
        }
        else
        {
            return "";
        }
    }
    public void setDETHNIC5(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setEthnicityLiteral(value);
        }
    }

    /// <summary>Decedent's Race--White</summary>
    IJEField metaRACE1 = new IJEField(44, 271, 1, "Decedent's Race--White", "RACE1", 1);
    public String getRACE1()
    {
        return Get_Race(NvssRace.White);
    }
    public void setRACE1(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.White, value);
        }
    }
    /// <summary>Decedent's Race--Black or African American</summary>
    IJEField metaRACE2 = new IJEField(45, 272, 1, "Decedent's Race--Black or African American", "RACE2", 1);
    public String getRACE2()
    {
        return Get_Race(NvssRace.BlackOrAfricanAmerican);
    }
    public void setRACE2(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.BlackOrAfricanAmerican, value);
        }
    }

    /// <summary>Decedent's Race--American Indian or Alaska Native</summary>
    IJEField metaRACE3 = new IJEField(46, 273, 1, "Decedent's Race--American Indian or Alaska Native", "RACE3", 1);
    public String getRACE3()
    {
        return Get_Race(NvssRace.AmericanIndianOrAlaskanNative);
    }
    public void setRACE3(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.AmericanIndianOrAlaskanNative, value);
        }
    }

    /// <summary>Decedent's Race--Asian Indian</summary>
    IJEField metaRACE4 = new IJEField(47, 274, 1, "Decedent's Race--Asian Indian", "RACE4", 1);
    public String getRACE4()
    {
        return Get_Race(NvssRace.AsianIndian);
    }
    public void setRACE4(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.AsianIndian, value);
        }
    }

    /// <summary>Decedent's Race--Chinese</summary>
    IJEField metaRACE5 = new IJEField(48, 275, 1, "Decedent's Race--Chinese", "RACE5", 1);
    public String getRACE5()
    {
        return Get_Race(NvssRace.Chinese);
    }
    public void setRACE5(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.Chinese, value);
        }
    }

    /// <summary>Decedent's Race--Filipino</summary>
    IJEField metaRACE6 = new IJEField(49, 276, 1, "Decedent's Race--Filipino", "RACE6", 1);
    public String getRACE6()
    {
        return Get_Race(NvssRace.Filipino);
    }
    public void setRACE6(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.Filipino, value);
        }
    }

    /// <summary>Decedent's Race--Japanese</summary>
    IJEField metaRACE7 = new IJEField(50, 277, 1, "Decedent's Race--Japanese", "RACE7", 1);
    public String getRACE7()
    {
        return Get_Race(NvssRace.Japanese);
    }
    public void setRACE7(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.Japanese, value);
        }
    }

    /// <summary>Decedent's Race--Korean</summary>
    IJEField metaRACE8 = new IJEField(51, 278, 1, "Decedent's Race--Korean", "RACE8", 1);
    public String getRACE8()
    {
        return Get_Race(NvssRace.Korean);
    }
    public void setRACE8(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.Korean, value);
        }
    }

    /// <summary>Decedent's Race--Vietnamese</summary>
    IJEField metaRACE9 = new IJEField(52, 279, 1, "Decedent's Race--Vietnamese", "RACE9", 1);
    public String getRACE9()
    {
        return Get_Race(NvssRace.Vietnamese);
    }
    public void setRACE9(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.Vietnamese, value);
        }
    }

    /// <summary>Decedent's Race--Other Asian</summary>
    IJEField metaRACE10 = new IJEField(53, 280, 1, "Decedent's Race--Other Asian", "RACE10", 1);
    public String getRACE10()
    {
        return Get_Race(NvssRace.OtherAsian);
    }
    public void setRACE10(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.OtherAsian, value);
        }
    }

    /// <summary>Decedent's Race--Native Hawaiian</summary>
    IJEField metaRACE11 = new IJEField(54, 281, 1, "Decedent's Race--Native Hawaiian", "RACE11", 1);
    public String getRACE11()
    {
        return Get_Race(NvssRace.NativeHawaiian);
    }
    public void setRACE11(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.NativeHawaiian, value);
        }
    }

    /// <summary>Decedent's Race--Guamanian or Chamorro</summary>
    IJEField metaRACE12 = new IJEField(55, 282, 1, "Decedent's Race--Guamanian or Chamorro", "RACE12", 1);
    public String getRACE12()
    {
        return Get_Race(NvssRace.GuamanianOrChamorro);
    }
    public void setRACE12(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.GuamanianOrChamorro, value);
        }
    }

    /// <summary>Decedent's Race--Samoan</summary>
    IJEField metaRACE13 = new IJEField(56, 283, 1, "Decedent's Race--Samoan", "RACE13", 1);
    public String getRACE13()
    {
        return Get_Race(NvssRace.Samoan);
    }
    public void setRACE13(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.Samoan, value);
        }
    }

    /// <summary>Decedent's Race--Other Pacific Islander</summary>
    IJEField metaRACE14 = new IJEField(57, 284, 1, "Decedent's Race--Other Pacific Islander", "RACE14", 1);
    public String getRACE14()
    {
        return Get_Race(NvssRace.OtherPacificIslander);
    }
    public void setRACE14(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.OtherPacificIslander, value);
        }
    }

    /// <summary>Decedent's Race--Other</summary>
    IJEField metaRACE15 = new IJEField(58, 285, 1, "Decedent's Race--Other", "RACE15", 1);
    public String getRACE15()
    {
        return Get_Race(NvssRace.OtherRace);
    }
    public void setRACE15(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.OtherRace, value);
        }
    }

    /// <summary>Decedent's Race--First American Indian or Alaska Native Literal</summary>
    IJEField metaRACE16 = new IJEField(59, 286, 30, "Decedent's Race--First American Indian or Alaska Native Literal", "RACE16", 1);
    public String getRACE16()
    {
        return Get_Race(NvssRace.FirstAmericanIndianOrAlaskanNativeLiteral);
    }
    public void setRACE16(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.FirstAmericanIndianOrAlaskanNativeLiteral, value);
        }
    }

    /// <summary>Decedent's Race--Second American Indian or Alaska Native Literal</summary>
    IJEField metaRACE17 = new IJEField(60, 316, 30, "Decedent's Race--Second American Indian or Alaska Native Literal", "RACE17", 1);
    public String getRACE17()
    {
        return Get_Race(NvssRace.SecondAmericanIndianOrAlaskanNativeLiteral);
    }
    public void setRACE17(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.SecondAmericanIndianOrAlaskanNativeLiteral, value);
        }
    }

    /// <summary>Decedent's Race--First Other Asian Literal</summary>
    IJEField metaRACE18 = new IJEField(61, 346, 30, "Decedent's Race--First Other Asian Literal", "RACE18", 1);
    public String getRACE18()
    {
        return Get_Race(NvssRace.FirstOtherAsianLiteral);
    }
    public void setRACE18(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.FirstOtherAsianLiteral, value);
        }
    }

    /// <summary>Decedent's Race--Second Other Asian Literal</summary>
    IJEField metaRACE19 = new IJEField(62, 376, 30, "Decedent's Race--Second Other Asian Literal", "RACE19", 1);
    public String getRACE19()
    {
        return Get_Race(NvssRace.SecondOtherAsianLiteral);
    }
    public void setRACE19(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.SecondOtherAsianLiteral, value);
        }
    }

    /// <summary>Decedent's Race--First Other Pacific Islander Literal</summary>
    IJEField metaRACE20 = new IJEField(63, 406, 30, "Decedent's Race--First Other Pacific Islander Literal", "RACE20", 1);
    public String getRACE20()
    {
        return Get_Race(NvssRace.FirstOtherPacificIslanderLiteral);
    }
    public void setRACE20(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.FirstOtherPacificIslanderLiteral, value);
        }
    }

    /// <summary>Decedent's Race--Second Other Pacific Islander Literal</summary>
    IJEField metaRACE21 = new IJEField(64, 436, 30, "Decedent's Race--Second Other Pacific Islander Literal", "RACE21", 1);
    public String getRACE21()
    {
        return Get_Race(NvssRace.SecondOtherPacificIslanderLiteral);
    }
    public void setRACE21(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.SecondOtherPacificIslanderLiteral, value);
        }
    }

    /// <summary>Decedent's Race--First Other Literal</summary>
    IJEField metaRACE22 = new IJEField(65, 466, 30, "Decedent's Race--First Other Literal", "RACE22", 1);
    public String getRACE22()
    {
        return Get_Race(NvssRace.FirstOtherRaceLiteral);
    }
    public void setRACE22(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.FirstOtherRaceLiteral, value);
        }
    }

    /// <summary>Decedent's Race--Second Other Literal</summary>
    IJEField metaRACE23 = new IJEField(66, 496, 30, "Decedent's Race--Second Other Literal", "RACE23", 1);
    public String getRACE23()
    {
        return Get_Race(NvssRace.SecondOtherRaceLiteral);
    }
    public void setRACE23(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_Race(NvssRace.SecondOtherRaceLiteral, value);
        }
    }

    /// <summary>First Edited Code</summary>
    IJEField metaRACE1E = new IJEField(67, 526, 3, "First Edited Code", "RACE1E", 1);
    public String getRACE1E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FirstEditedRaceCode", "RACE1E");
    }
    public void setRACE1E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE1E", "FirstEditedRaceCode", value);
    }

    /// <summary>Second Edited Code</summary>
    IJEField metaRACE2E = new IJEField(68, 529, 3, "Second Edited Code", "RACE2E", 1);
    public String getRACE2E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SecondEditedRaceCode", "RACE2E");
    }
    public void setRACE2E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE2E", "SecondEditedRaceCode", value);
    }

    /// <summary>Third Edited Code</summary>
    IJEField metaRACE3E = new IJEField(69, 532, 3, "Third Edited Code", "RACE3E", 1);
    public String getRACE3E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "ThirdEditedRaceCode", "RACE3E");
    }
    public void setRACE3E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE3E", "ThirdEditedRaceCode", value);
    }

    /// <summary>Fourth Edited Code</summary>
    IJEField metaRACE4E = new IJEField(70, 535, 3, "Fourth Edited Code", "RACE4E", 1);
    public String getRACE4E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FourthEditedRaceCode", "RACE4E");
    }
    public void setRACE4E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE4E", "FourthEditedRaceCode", value);
    }

    /// <summary>Fifth Edited Code</summary>
    IJEField metaRACE5E = new IJEField(71, 538, 3, "Fifth Edited Code", "RACE5E", 1);
    public String getRACE5E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FifthEditedRaceCode", "RACE5E");
    }
    public void setRACE5E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE5E", "FifthEditedRaceCode", value);
    }

    /// <summary>Sixth Edited Code</summary>
    IJEField metaRACE6E = new IJEField(72, 541, 3, "Sixth Edited Code", "RACE6E", 1);
    public String getRACE6E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SixthEditedRaceCode", "RACE6E");
    }
    public void setRACE6E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE6E", "SixthEditedRaceCode", value);
    }

    /// <summary>Seventh Edited Code</summary>
    IJEField metaRACE7E = new IJEField(73, 544, 3, "Seventh Edited Code", "RACE7E", 1);
    public String getRACE7E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SeventhEditedRaceCode", "RACE7E");
    }
    public void setRACE7E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE7E", "SeventhEditedRaceCode", value);
    }

    /// <summary>Eighth Edited Code</summary>
    IJEField metaRACE8E = new IJEField(74, 547, 3, "Eighth Edited Code", "RACE8E", 1);
    public String getRACE8E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "EighthEditedRaceCode", "RACE8E");
    }
    public void setRACE8E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE8E", "EighthEditedRaceCode", value);
    }

    /// <summary>First American Indian Code</summary>
    IJEField metaRACE16C = new IJEField(75, 550, 3, "First American Indian Code", "RACE16C", 1);
    public String getRACE16C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FirstAmericanIndianRaceCode", "RACE16C");
    }
    public void setRACE16C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE16C", "FirstAmericanIndianRaceCode", value);
    }

    /// <summary>Second American Indian Code</summary>
    IJEField metaRACE17C = new IJEField(76, 553, 3, "Second American Indian Code", "RACE17C", 1);
    public String getRACE17C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SecondAmericanIndianRaceCode", "RACE17C");
    }
    public void setRACE17C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE17C", "SecondAmericanIndianRaceCode", value);
    }

    /// <summary>First Other Asian Code</summary>
    IJEField metaRACE18C = new IJEField(77, 556, 3, "First Other Asian Code", "RACE18C", 1);
    public String getRACE18C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FirstOtherAsianRaceCode", "RACE18C");
    }
    public void setRACE18C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE18C", "FirstOtherAsianRaceCode", value);
    }

    /// <summary>Second Other Asian Code</summary>
    IJEField metaRACE19C = new IJEField(78, 559, 3, "Second Other Asian Code", "RACE19C", 1);
    public String getRACE19C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SecondOtherAsianRaceCode", "RACE19C");
    }
    public void setRACE19C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE19C", "SecondOtherAsianRaceCode", value);
    }

    /// <summary>First Other Pacific Islander Code</summary>
    IJEField metaRACE20C = new IJEField(79, 562, 3, "First Other Pacific Islander Code", "RACE20C", 1);
    public String getRACE20C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FirstOtherPacificIslanderRaceCode", "RACE20C");
    }
    public void setRACE20C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE20C", "FirstOtherPacificIslanderRaceCode", value);
    }

    /// <summary>Second Other Pacific Islander Code</summary>
    IJEField metaRACE21C = new IJEField(80, 565, 3, "Second Other Pacific Islander Code", "RACE21C", 1);
    public String getRACE21C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SecondOtherPacificIslanderRaceCode", "RACE21C");
    }
    public void setRACE21C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE21C", "SecondOtherPacificIslanderRaceCode", value);
    }

    /// <summary>First Other Race Code</summary>
    IJEField metaRACE22C = new IJEField(81, 568, 3, "First Other Race Code", "RACE22C", 1);
    public String getRACE22C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FirstOtherRaceCode", "RACE22C");
    }
    public void setRACE22C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE22C", "FirstOtherRaceCode", value);
    }

    /// <summary>Second Other Race Code</summary>
    IJEField metaRACE23C = new IJEField(82, 571, 3, "Second Other Race Code", "RACE23C", 1);
    public String getRACE23C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SecondOtherRaceCode", "RACE23C");
    }
    public void setRACE23C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE23C", "SecondOtherRaceCode", value);
    }

    /// <summary>Decedent's Race--Missing</summary>
    IJEField metaRACE_MVR = new IJEField(83, 574, 1, "Decedent's Race--Missing", "RACE_MVR", 1);
    public String getRACE_MVR()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceMissingValueReason.FHIRToIJE, "RaceMissingValueReason", "RACE_MVR");
    }
    public void setRACE_MVR(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceMissingValueReason.IJEToFHIR, "RACE_MVR", "RaceMissingValueReason", value);
    }

    /// <summary>Occupation -- Literal (OPTIONAL)</summary>
    IJEField metaOCCUP = new IJEField(84, 575, 40, "Occupation -- Literal (OPTIONAL)", "OCCUP", 1);
    public String getOCCUP()
    {
        return LeftJustified_Get("OCCUP", "UsualOccupation");
    }
    public void setOCCUP(String value)
    {
        LeftJustified_Set("OCCUP", "UsualOccupation", value);
    }

    /// <summary>Occupation -- Code</summary>
    IJEField metaOCCUPC = new IJEField(85, 615, 3, "Occupation -- Code", "OCCUPC", 1);
    public String getOCCUPC()
    {
        // NOTE: This is a placeholder, the IJE field OCCUPC is not currently implemented in FHIR
        return "";
    }
    public void setOCCUPC(String value)
    {
        // NOTE: This is a placeholder, the IJE field OCCUPC is not currently implemented in FHIR
    }

    /// <summary>Industry -- Literal (OPTIONAL)</summary>
    IJEField metaINDUST = new IJEField(86, 618, 40, "Industry -- Literal (OPTIONAL)", "INDUST", 1);
    public String getINDUST()
    {
        return LeftJustified_Get("INDUST", "UsualIndustry");
    }
    public void setINDUST(String value)
    {
        LeftJustified_Set("INDUST", "UsualIndustry", value);
    }

    /// <summary>Industry -- Code</summary>
    IJEField metaINDUSTC = new IJEField(87, 658, 3, "Industry -- Code", "INDUSTC", 1);
//    public String getINDUSTC()
//    {
//        // NOTE: This is a placeholder, the IJE field INDUSTC is not currently implemented in FHIR
//        return "";
//    }
//    public void setINDUSTC(String value)
//    {
//        // NOTE: This is a placeholder, the IJE field INDUSTC is not currently implemented in FHIR
//    }

    /// <summary>Infant Death/Birth Linking - birth certificate number</summary>
    IJEField metaBCNO = new IJEField(88, 661, 6, "Infant Death/Birth Linking - birth certificate number", "BCNO", 1);
    public String getBCNO()
    {
        String bcno = record.getBirthRecordId();
        if (bcno != null)
        {
            return bcno;
        }
        return "";
    }
    public void setBCNO(String value)
    {
        // if value is null, the library will add the data absent reason

        record.setBirthRecordId(value);
    }

    /// <summary>Infant Death/Birth Linking - year of birth</summary>
    IJEField metaIDOB_YR = new IJEField(89, 667, 4, "Infant Death/Birth Linking - year of birth", "IDOB_YR", 1);
    public String getIDOB_YR()
    {
        return LeftJustified_Get("IDOB_YR", "BirthRecordYear");
    }
    public void setIDOB_YR(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("IDOB_YR", "BirthRecordYear", value);
        }
    }

    /// <summary>Infant Death/Birth Linking - Birth state</summary>
    IJEField metaBSTATE = new IJEField(90, 671, 2, "Infant Death/Birth Linking - State, U.S. Territory or Canadian Province of Birth - code", "BSTATE", 1);
    public String getBSTATE()
    {
        return LeftJustified_Get("BSTATE", "BirthRecordState");
    }
    public void setBSTATE(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("BSTATE", "BirthRecordState", value);
        }
    }

    /// <summary>Receipt date -- Year</summary>
    IJEField metaR_YR = new IJEField(91, 673, 4, "Receipt date -- Year", "R_YR", 1);
    public String getR_YR()
    {
        return NumericAllowingUnknown_Get("R_YR", "ReceiptYear");
    }
    public void setR_YR(String value)
    {
        NumericAllowingUnknown_Set("R_YR", "ReceiptYear", value);
    }

    /// <summary>Receipt date -- Month</summary>
    IJEField metaR_MO = new IJEField(92, 677, 2, "Receipt date -- Month", "R_MO", 1);
    public String getR_MO()
    {
        return NumericAllowingUnknown_Get("R_MO", "ReceiptMonth");
    }
    public void setR_MO(String value)
    {
        NumericAllowingUnknown_Set("R_MO", "ReceiptMonth", value);
    }

    /// <summary>Receipt date -- Day</summary>
    IJEField metaR_DY = new IJEField(93, 679, 2, "Receipt date -- Day", "R_DY", 1);
    public String getR_DY()
    {
        return NumericAllowingUnknown_Get("R_DY", "ReceiptDay");
    }
    public void setR_DY(String value)
    {
        NumericAllowingUnknown_Set("R_DY", "ReceiptDay", value);
    }

    /// <summary>Occupation -- 4 digit Code (OPTIONAL)</summary>
    IJEField metaOCCUPC4 = new IJEField(94, 681, 4, "Occupation -- 4 digit Code (OPTIONAL)", "OCCUPC4", 1);
    public String getOCCUPC4()
    {
        // NOTE: This is a placeholder, the IJE field OCCUPC4 is not currently implemented in FHIR
        return "";
    }
    public void setOCCUPC4(String value)
    {
        // NOTE: This is a placeholder, the IJE field OCCUPC4 is not currently implemented in FHIR
    }

    /// <summary>Industry -- 4 digit Code (OPTIONAL)</summary>
    IJEField metaINDUSTC = new IJEField(95, 685, 4, "Industry -- 4 digit Code (OPTIONAL)", "INDUSTC4", 1);
    public String getINDUSTC()
    {
        // NOTE: This is a placeholder, the IJE field INDUSTC4 is not currently implemented in FHIR
        return "";
    }
    public void setINDUSTC(String value)
    {
        // NOTE: This is a placeholder, the IJE field INDUSTC4 is not currently implemented in FHIR
    }

    /// <summary>Date of Registration--Year</summary>
    IJEField metaDOR_YR = new IJEField(96, 689, 4, "Date of Registration--Year", "DOR_YR", 1);
    public String getDOR_YR()
    {
        return DateTime_Get("DOR_YR", "yyyy", "RegisteredTime");
    }
    public void setDOR_YR(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            DateTime_Set("DOR_YR", "yyyy", "RegisteredTime", value, true, true);
        }
    }

    /// <summary>Date of Registration--Month</summary>
    IJEField metaDOR_MO = new IJEField(97, 693, 2, "Date of Registration--Month", "DOR_MO", 1);
    public String getDOR_MO()
    {
        return DateTime_Get("DOR_MO", "MM", "RegisteredTime");
    }
    public void setDOR_MO(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            DateTime_Set("DOR_MO", "MM", "RegisteredTime", value, true, true);
        }
    }

    /// <summary>Date of Registration--Day</summary>
    IJEField metaDOR_DY = new IJEField(98, 695, 2, "Date of Registration--Day", "DOR_DY", 1);
    public String getDOR_DY()
    {
        return DateTime_Get("DOR_DY", "dd", "RegisteredTime");
    }
    public void setDOR_DY(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            DateTime_Set("DOR_DY", "dd", "RegisteredTime", value, true, true);
        }
    }

    /// <summary>FILLER 2 for expansion</summary>
    IJEField metaFILLER2 = new IJEField(99, 697, 4, "FILLER 2 for expansion", "FILLER2", 1);
    public String getFILLER2()
    {
        // NOTE: This is a placeholder, the IJE field  is not currently implemented in FHIR
        return "";
    }
    public void setFILLER2(String value)
    {
        // NOTE: This is a placeholder, the IJE field  is not currently implemented in FHIR
    }

    /// <summary>Manner of Death</summary>
    IJEField metaMANNER = new IJEField(100, 701, 1, "Manner of Death", "MANNER", 1);
    public String getMANNER()
    {
        return Get_MappingFHIRToIJE(Mappings.MannerOfDeath.FHIRToIJE, "MannerOfDeathType", "MANNER");
    }
    public void setMANNER(String value)
    {
        Set_MappingIJEToFHIR(Mappings.MannerOfDeath.IJEToFHIR, "MANNER", "MannerOfDeathType", value);
    }

    /// <summary>Intentional Reject</summary>
    IJEField metaINT_REJ = new IJEField(101, 702, 1, "Intentional Reject", "INT_REJ", 1);
    public String getINT_REJ()
    {
        return Get_MappingFHIRToIJE(Mappings.IntentionalReject.FHIRToIJE, "IntentionalReject", "INT_REJ");
    }
    public void setINT_REJ(String value)
    {
        Set_MappingIJEToFHIR(Mappings.IntentionalReject.IJEToFHIR, "INT_REJ", "IntentionalReject", value);
    }

    /// <summary>Acme System Reject Codes</summary>
    IJEField metaSYS_REJ = new IJEField(102, 703, 1, "Acme System Reject Codes", "SYS_REJ", 1);
    public String getSYS_REJ()
    {
        return Get_MappingFHIRToIJE(Mappings.SystemReject.FHIRToIJE, "AcmeSystemReject", "SYS_REJ");
    }
    public void setSYS_REJ(String value)
    {
        Set_MappingIJEToFHIR(Mappings.SystemReject.IJEToFHIR, "SYS_REJ", "AcmeSystemReject", value);
    }

    /// <summary>Place of Injury (computer generated)</summary>
    IJEField metaINJPL = new IJEField(103, 704, 1, "Place of Injury (computer generated)", "INJPL", 1);
    public String getINJPL()
    {
        return Get_MappingFHIRToIJE(Mappings.PlaceOfInjury.FHIRToIJE, "PlaceOfInjury", "INJPL");
    }
    public void setINJPL(String value)
    {
        Set_MappingIJEToFHIR(Mappings.PlaceOfInjury.IJEToFHIR, "INJPL", "PlaceOfInjury", value);
    }

    /// <summary>Manual Underlying Cause</summary>
    IJEField metaMAN_UC = new IJEField(104, 705, 5, "Manual Underlying Cause", "MAN_UC", 1);
    public String getMAN_UC()
    {
        return (ActualICD10toNCHSICD10(LeftJustified_Get("MAN_UC", "ManUnderlyingCOD")));
    }
    public void setMAN_UC(String value)
    {
        LeftJustified_Set("MAN_UC", "ManUnderlyingCOD", NCHSICD10toActualICD10(value));
    }

    /// <summary>ACME Underlying Cause</summary>
    IJEField metaACME_UC = new IJEField(105, 710, 5, "ACME Underlying Cause", "ACME_UC", 1);
    public String getACME_UC()
    {
        return (ActualICD10toNCHSICD10(LeftJustified_Get("ACME_UC", "AutomatedUnderlyingCOD")));
    }
    public void setACME_UC(String value)
    {
        LeftJustified_Set("ACME_UC", "AutomatedUnderlyingCOD", NCHSICD10toActualICD10(value));
    }

    /// <summary>Entity-axis codes</summary>
    // 20 codes, each taking up 8 characters:
    // 1 char:   part/line number (1-6)
    // 1 char: sequence within the line. (1-8)
    // 4 char “ICD code” in NCHS format, without the ‘.’
    // 1 char reserved”.  (not represented in the FHIR specification)
    // 1 char ‘e code indicator’
    IJEField metaEAC = new IJEField(106, 715, 160, "Entity-axis codes", "EAC", 1);
    public String getEAC()
    {
        String eacStr = "";
        for((int LineNumber, int Position, String Code, boolean ECode) entry : record.getEntityAxisCauseOfDeath())
        {
            String lineNumber = StringUtils.rightPad(Truncate(entry.LineNumber.toString(), 1), 1, " ");
            String position = StringUtils.rightPad(Truncate(entry.Position.toString(), 1), 1, " ");
            String icdCode = StringUtils.rightPad(Truncate(ActualICD10toNCHSICD10(entry.Code), 5), 5, " "); ;
            String eCode = entry.getECode() ? "&" : " ";
            eacStr += lineNumber + position + icdCode + eCode;
        }
        String fmtEac = StringUtils.rightPad(Truncate(eacStr, 160), 160, " ");
        return fmtEac;
    }
    public void setEAC(String value)
    {
        List eac = new ArrayList<>();
        String paddedValue = StringUtils.rightPad(value, 160); // Accept input that's missing white space padding to the right
        //IEnumerable<String> codes = Enumerable.Range(0, paddedValue.length() / 8).Select(i -> paddedValue.substring(i * 8, 8));
        Iterable<String> codes = Enumerable.Range(0, paddedValue.length() / 8).Select(i -> paddedValue.substring(i * 8, 8));

        for(String code:codes)
        {
            if (!isNullOrWhiteSpace(code))
            {
                if (int.TryParse(code.substring(0, 1), out int lineNumber) && int.TryParse(code.substring(1, 1), out int position))
                {
                    String icdCode = NCHSICD10toActualICD10(code.substring(2, 5));
                    String eCode = code.substring(7, 1);
                    eac.add((LineNumber: lineNumber, Position: position, Code: icdCode, ECode: eCode.equals("&")));
                }
            }
        }
        if (eac.size() > 0)
        {
            record.setEntityAxisCauseOfDeath(eac);
        }
    }

    /// <summary>Transax conversion flag: Computer Generated</summary>
    IJEField metaTRX_FLG = new IJEField(107, 875, 1, "Transax conversion flag: Computer Generated", "TRX_FLG", 1);
    public String getTRX_FLG()
    {
        return Get_MappingFHIRToIJE(Mappings.TransaxConversion.FHIRToIJE, "TransaxConversion", "TRX_FLG");
    }
    public void setTRX_FLG(String value)
    {
        Set_MappingIJEToFHIR(Mappings.TransaxConversion.IJEToFHIR, "TRXFLG", "TransaxConversion", value);
    }

    /// <summary>Record-axis codes</summary>
    // 20 codes, each taking up 5 characters:
    // 4 char “ICD code” in NCHS format, without the ‘.’
    // 1 char WouldBeUnderlyingCauseOfDeathWithoutPregnancy, only significant if position=2”
    IJEField metaRAC = new IJEField(108, 876, 100, "Record-axis codes", "RAC", 1);
    public String getRAC()
    {
        String racStr = "";
        for((int Position, String Code, boolean Pregnancy) entry:record.getRecordAxisCauseOfDeath())
        {
            // Position doesn't appear in the IJE/TRX format it's just implicit
            String icdCode = StringUtils.rightPad(Truncate(ActualICD10toNCHSICD10(entry.Code), 4), 4, " ");
            String preg = entry.Pregnancy ? "1" : " ";
            racStr += icdCode + preg;
        }
        String fmtRac = StringUtils.rightPad(Truncate(racStr, 100), 100, " ");
        return fmtRac;
    }
    public void setRAC(String value)
    {
        List rac = new ArrayList<>();
        String paddedValue = StringUtils.rightPad(value, 100); // Accept input that's missing white space padding to the right
        //IEnumerable<String> codes = Enumerable.Range(0, paddedValue.length() / 5).Select(i -> paddedValue.substring(i * 5, 5));
        Iterable<String> codes = Enumerable.Range(0, paddedValue.length() / 5).Select(i -> paddedValue.substring(i * 5, 5));

        int position = 1;
        for(String code:codes)
        {
            if (!isNullOrWhiteSpace(code))
            {
                String icdCode = NCHSICD10toActualICD10(code.substring(0, 4));
                String preg = code.substring(4, 1);
                Tuple<String, String, String> entry = Tuple.Create(position), icdCode, preg);
                rac.add((Position: position, Code: icdCode, Pregnancy: preg.equals("1")));
            }
            position++;
        }
        if (rac.size() > 0)
        {
            record.setRecordAxisCauseOfDeath(rac);
        }
    }

    /// <summary>Was Autopsy performed</summary>
    IJEField metaAUTOP = new IJEField(109, 976, 1, "Was Autopsy performed", "AUTOP", 1);
    public String getAUTOP()
    {
        return Get_MappingFHIRToIJE(Mappings.YesNoUnknown.FHIRToIJE, "AutopsyPerformedIndicator", "AUTOP");
    }
    public void setAUTOP(String value)
    {
        Set_MappingIJEToFHIR(Mappings.YesNoUnknown.IJEToFHIR, "AUTOP", "AutopsyPerformedIndicator", value);
    }

    /// <summary>Were Autopsy Findings Available to Complete the Cause of Death?</summary>
    IJEField metaAUTOPF = new IJEField(110, 977, 1, "Were Autopsy Findings Available to Complete the Cause of Death?", "AUTOPF", 1);
    public String getAUTOPF()
    {
        return Get_MappingFHIRToIJE(Mappings.YesNoUnknownNotApplicable.FHIRToIJE, "AutopsyResultsAvailable", "AUTOPF");
    }
    public void setUTOPF(String value)
    {
        Set_MappingIJEToFHIR(Mappings.YesNoUnknownNotApplicable.IJEToFHIR, "AUTOPF", "AutopsyResultsAvailable", value);
    }

    /// <summary>Did Tobacco Use Contribute to Death?</summary>
    IJEField metaTOBAC = new IJEField(111, 978, 1, "Did Tobacco Use Contribute to Death?", "TOBAC", 1);
    public String getTOBAC()
    {
        return Get_MappingFHIRToIJE(Mappings.ContributoryTobaccoUse.FHIRToIJE, "TobaccoUse", "TOBAC");
    }
    public void setTOBAC(String value)
    {
        Set_MappingIJEToFHIR(Mappings.ContributoryTobaccoUse.IJEToFHIR, "TOBAC", "TobaccoUse", value);
    }

    /// <summary>Pregnancy</summary>
    IJEField metaPREG = new IJEField(112, 979, 1, "Pregnancy", "PREG", 1);
    public String getPREG()
    {
        return Get_MappingFHIRToIJE(Mappings.PregnancyStatus.FHIRToIJE, "PregnancyStatus", "PREG");
    }
    public void setPREG(String value)
    {
        Set_MappingIJEToFHIR(Mappings.PregnancyStatus.IJEToFHIR, "PREG", "PregnancyStatus", value);
    }

    /// <summary>If Female--Edit Flag: From EDR only</summary>
    IJEField metaPREG_BYPASS = new IJEField(113, 980, 1, "If Female--Edit Flag: From EDR only", "PREG_BYPASS", 1);
    public String getPREG_BYPASS()
    {
        return Get_MappingFHIRToIJE(Mappings.EditBypass012.FHIRToIJE, "PregnancyStatusEditFlag", "PREG_BYPASS");
    }
    public void setPREG_BYPASS(String value)
    {
        Set_MappingIJEToFHIR(Mappings.EditBypass012.IJEToFHIR, "PREG_BYPASS", "PregnancyStatusEditFlag", value);
    }

    /// <summary>Date of injury--month</summary>
    IJEField metaDOI_MO = new IJEField(114, 981, 2, "Date of injury--month", "DOI_MO", 1);
    public String getDOI_MO()
    {
        return NumericAllowingUnknown_Get("DOI_MO", "InjuryMonth");
    }
    public void setDOI_MO(String value)
    {
        NumericAllowingUnknown_Set("DOI_MO", "InjuryMonth", value);
    }

    /// <summary>Date of injury--day</summary>
    IJEField metaDOI_DY = new IJEField(115, 983, 2, "Date of injury--day", "DOI_DY", 1);
    public String getDOI_DY()
    {
        return NumericAllowingUnknown_Get("DOI_DY", "InjuryDay");
    }
    public void setDOI_DY(String value)
    {
        NumericAllowingUnknown_Set("DOI_DY", "InjuryDay", value);
    }

    /// <summary>Date of injury--year</summary>
    IJEField metaDOI_YR = new IJEField(116, 985, 4, "Date of injury--year", "DOI_YR", 1);
    public String getDOI_YR()
    {
        return NumericAllowingUnknown_Get("DOI_YR", "InjuryYear");
    }
    public void setDOI_YR(String value)
    {
        NumericAllowingUnknown_Set("DOI_YR", "InjuryYear", value);
    }

    /// <summary>Time of injury</summary>
    IJEField metaTOI_HR = new IJEField(117, 989, 4, "Time of injury", "TOI_HR", 1);
    public String getTOI_HR()
    {
        return TimeAllowingUnknown_Get("TOI_HR", "InjuryTime");
    }
    public void setTOI_HR(String value)
    {
        TimeAllowingUnknown_Set("TOI_HR", "InjuryTime", value);
    }

    /// <summary>Time of injury</summary>
    IJEField metaWORKINJ = new IJEField(118, 993, 1, "Injury at work", "WORKINJ", 1);
    public String getWORKINJ()
    {
        return Get_MappingFHIRToIJE(Mappings.YesNoUnknownNotApplicable.FHIRToIJE, "InjuryAtWork", "WORKINJ");
    }
    public void setWORKINJ(String value)
    {
        Set_MappingIJEToFHIR(Mappings.YesNoUnknownNotApplicable.IJEToFHIR, "WORKINJ", "InjuryAtWork", value);
    }

    /// <summary>Title of Certifier</summary>
    IJEField metaCERTL = new IJEField(119, 994, 30, "Title of Certifier", "CERTL", 1);
    public String getCERTL()
    {
        String ret = record.getCertificationRoleHelper();
        if (ret != null && Mappings.CertifierTypes.FHIRToIJE.containsKey(ret))
        {
            return Get_MappingFHIRToIJE(Mappings.CertifierTypes.FHIRToIJE, "CertificationRole", "CERTL");
        }
        else  // If the return value is not a code, it is just an arbitrary String, so return it.
        {
            return ret;
        }
    }
    public void setCERTL(String value)
    {
        if (Mappings.CertifierTypes.IJEToFHIR.containsKey(value.split(" ")[0]))
        {
            Set_MappingIJEToFHIR(Mappings.CertifierTypes.IJEToFHIR, "CERTL", "CertificationRole", value.trim());
        }
        else  // If the value is not a valid code, it is just an arbitrary String.  The helper will deal with it.
        {
            record.setCertificationRoleHelper(value);
        }
    }

    /// <summary>Activity at time of death (computer generated)</summary>
    IJEField metaINACT = new IJEField(120, 1024, 1, "Activity at time of death (computer generated)", "INACT", 1);
    public String getINACT()
    {
        return Get_MappingFHIRToIJE(Mappings.ActivityAtTimeOfDeath.FHIRToIJE, "ActivityAtDeath", "INACT");
    }
    public void setINACT(String value)
    {
        Set_MappingIJEToFHIR(Mappings.ActivityAtTimeOfDeath.IJEToFHIR, "INACT", "ActivityAtDeath", value);
    }

    /// <summary>Auxiliary State file number</summary>
    IJEField metaAUXNO2 = new IJEField(121, 1025, 12, "Auxiliary State file number", "AUXNO2", 1);
    public String getAUXNO2()
    {
        if (record.getStateLocalIdentifier2() == null)
        {
            return StringUtils.repeat(" ", 12);
        }
        return LeftJustified_Get("AUXNO2", "StateLocalIdentifier2");
    }
    public void setAUXNO2(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            value = StringUtils.leftPad(value, 12 , '0');
            LeftJustified_Set("AUXNO2", "StateLocalIdentifier2", value);
        }
    }

    /// <summary>State Specific Data</summary>
    IJEField metaSTATESP = new IJEField(122, 1037, 30, "State Specific Data", "STATESP", 1);
    public String getSTATESP()
    {
        return LeftJustified_Get("STATESP", "StateSpecific");
    }
    public void setSTATESP(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("STATESP", "StateSpecific", value);
        }
    }

    /// <summary>Surgery Date--month</summary>
    IJEField metaSUR_MO = new IJEField(123, 1067, 2, "Surgery Date--month", "SUR_MO", 1);
    public String getSUR_MO()
    {
        return NumericAllowingUnknown_Get("SUR_MO", "SurgeryMonth");
    }
    public void setSUR_MO(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            NumericAllowingUnknown_Set("SUR_MO", "SurgeryMonth", value);
        }
    }

    /// <summary>Surgery Date--day</summary>
    IJEField metaSUR_DY = new IJEField(124, 1069, 2, "Surgery Date--day", "SUR_DY", 1);
    public String getSUR_DY()
    {
        return NumericAllowingUnknown_Get("SUR_DY", "SurgeryDay");
    }
    public void setSUR_DY(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            NumericAllowingUnknown_Set("SUR_DY", "SurgeryDay", value);
        }
    }

    /// <summary>Surgery Date--year</summary>
    IJEField metaSUR_YR = new IJEField(125, 1071, 4, "Surgery Date--year", "SUR_YR", 1);
    public String getSUR_YR()
    {
        return NumericAllowingUnknown_Get("SUR_YR", "SurgeryYear");
    }
    public void setSUR_YR(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            NumericAllowingUnknown_Set("SUR_YR", "SurgeryYear", value);
        }
    }

    /// <summary>Time of Injury Unit</summary>
    IJEField metaTOI_UNIT = new IJEField(126, 1075, 1, "Time of Injury Unit", "TOI_UNIT", 1);
    public String getTOI_UNIT()
    {
        if (DOI_YR != "9999" && DOI_YR != "    ")
        {
            // Military time since that's the form the datetime object VRDR stores the time of injury as.
            return "M";
        }
        else
        {
            // Blank since there is no time of injury.
            return " ";

        }
    }
    public void setTOI_UNIT(String value)
    { // The TOI is persisted as a datetime, so the A/P/M is meaningless.   This set is a NOOP, but generate a diagnostic for A and P
        if (value != "M" && value != " ")
        {
            validationErrors.add(new StringBuffer("Error: IJE field TOI_UNIT contains String '{value}' but can only be set to M or blank");
        }
    }

    /// <summary>For possible future change in transax</summary>
    IJEField metaBLANK1 = new IJEField(127, 1076, 5, "For possible future change in transax", "BLANK1", 1);
    public String getBLANK1()
    {
        // NOTE: This is a placeholder, the IJE field BLANK1 is not currently implemented in FHIR
        return "";
    }
    public void setBLANK1(String value)
    {
        // NOTE: This is a placeholder, the IJE field BLANK1 is not currently implemented in FHIR
    }

    /// <summary>Decedent ever served in Armed Forces?</summary>
    IJEField metaARMEDF = new IJEField(128, 1081, 1, "Decedent ever served in Armed Forces?", "ARMEDF", 1);
    public String getARMEDF()
    {
        return Get_MappingFHIRToIJE(Mappings.YesNoUnknown.FHIRToIJE, "MilitaryService", "ARMEDF");
    }
    public void setARMEDF(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_MappingIJEToFHIR(Mappings.YesNoUnknown.IJEToFHIR, "ARMEDF", "MilitaryService", value);
        }
    }

    /// <summary>Death Institution name</summary>
    IJEField metaDINSTI = new IJEField(129, 1082, 30, "Death Institution name", "DINSTI", 1);
    public String getDINSTI()
    {
        return LeftJustified_Get("DINSTI", "DeathLocationName");
    }
    public void setDINSTI(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("DINSTI", "DeathLocationName", value);
        }
    }

    /// <summary>Long String address for place of death</summary>
    IJEField metaADDRESS_D = new IJEField(130, 1112, 50, "Long String address for place of death", "ADDRESS_D", 1);
    public String getADDRESS_D()
    {
        return Map_Get("ADDRESS_D", "DeathLocationAddress", "addressLine1");
    }
    public void setADDRESS_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Set("ADDRESS_D", "DeathLocationAddress", "addressLine1", value);
        }
    }

    /// <summary>Place of death. Street number</summary>
    IJEField metaSTNUM_D = new IJEField(131, 1162, 10, "Place of death. Street number", "STNUM_D", 1);
    public String getSTNUM_D()
    {
        return Map_Geo_Get("STNUM_D", "DeathLocationAddress", "address", "stnum", true);
    }
    public void setSTNUM_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("STNUM_D", "DeathLocationAddress", "address", "stnum", false, value);
        }
    }

//    public boolean isNullOrEmpty(String s) { ////
//        return s == null || s.length() == 0;
//    }
//
//    public static boolean isNullOrWhiteSpace(String s) {
//        return s == null || isWhitespace(s);
//    }
//
//    public boolean isWhitespace(String s) {
//        int length = s.length();
//        if (length > 0) {
//            for (int i = 0; i < length; i++) {
//                if (!Character.isWhitespace(s.charAt(i))) {
//                    return false;
//                }
//            }
//            return true;
//        }
//        return false;
//    }

    /// <summary>Place of death. Pre Directional</summary>
    IJEField metaPREDIR_D = new IJEField(132, 1172, 10, "Place of death. Pre Directional", "PREDIR_D", 1);
    public String getPREDIR_D()
    {
        return Map_Geo_Get("PREDIR_D", "DeathLocationAddress", "address", "predir", true);
    }
    public void setPREDIR_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("PREDIR_D", "DeathLocationAddress", "address", "predir", false, value);
        }
    }

    /// <summary>Place of death. Street name</summary>
    IJEField metaSTNAME_D = new IJEField(133, 1182, 50, "Place of death. Street name", "STNAME_D", 1);
    public String getSTNAME_D()
    {
        return Map_Geo_Get("STNAME_D", "DeathLocationAddress", "address", "stname", true);
    }
    public void setSTNAME_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("STNAME_D", "DeathLocationAddress", "address", "stname", false, value);
        }
    }

    /// <summary>Place of death. Street designator</summary>
    IJEField metaSTDESIG_D = new IJEField(134, 1232, 10, "Place of death. Street designator", "STDESIG_D", 1);
    public String getSTDESIG_D()
    {
        return Map_Geo_Get("STDESIG_D", "DeathLocationAddress", "address", "stdesig", true);
    }
    public void setSTDESIG_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("STDESIG_D", "DeathLocationAddress", "address", "stdesig", false, value);
        }
    }

    /// <summary>Place of death. Post Directional</summary>
    IJEField metaPOSTDIR_D = new IJEField(135, 1242, 10, "Place of death. Post Directional", "POSTDIR_D", 1);
    public String getPOSTDIR_D()
    {
        return Map_Geo_Get("POSTDIR_D", "DeathLocationAddress", "address", "postdir", true);
    }
    public void setPOSTDIR_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("POSTDIR_D", "DeathLocationAddress", "address", "postdir", false, value);
        }
    }

    /// <summary>Place of death. City or Town name</summary>
    IJEField metaCITYTEXT_D = new IJEField(136, 1252, 28, "Place of death. City or Town name", "CITYTEXT_D", 1);
    public String getCITYTEXT_D()
    {
        return Map_Geo_Get("CITYTEXT_D", "DeathLocationAddress", "address", "city", false);
    }
    public void setCITYTEXT_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("CITYTEXT_D", "DeathLocationAddress", "address", "city", false, value);
        }
    }

    /// <summary>Place of death. State name literal</summary>
    IJEField metaSTATETEXT_D = new IJEField(137, 1280, 28, "Place of death. State name literal", "STATETEXT_D", 1);
    public String getSTATETEXT_D()
    {
        String stateCode = Map_Geo_Get("DSTATE", "DeathLocationAddress", "address", "state", false);
        //var mortalityData = MortalityData.Instance;
        String statetextd = dataLookup.StateCodeToStateName(stateCode);
        if (statetextd == null)
        {
            statetextd = " ";
        }
        return StringUtils.rightPad(Truncate(statetextd, 28), 28, " ");
    }
    public void setSTATETEXT_D(String value)
    {
        // NOOP
    }

    /// <summary>Place of death. Zip code</summary>
    IJEField metaZIP9_D = new IJEField(138, 1308, 9, "Place of death. Zip code", "ZIP9_D", 1);
    public String getZIP9_D()
    {
        return Map_Get("ZIP9_D", "DeathLocationAddress", "addressZip");
    }
    public void setZIP9_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Set("ZIP9_D", "DeathLocationAddress", "addressZip", value);
        }
    }

    /// <summary>Place of death. County of Death</summary>
    IJEField metaCOUNTYTEXT_D = new IJEField(139, 1317, 28, "Place of death. County of Death", "COUNTYTEXT_D", 2);
    public String getCOUNTYTEXT_D()
    {
        return Map_Geo_Get("COUNTYTEXT_D", "DeathLocationAddress", "address", "county", false);
    }
    public void setCOUNTYTEXT_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("COUNTYTEXT_D", "DeathLocationAddress", "address", "county", false, value);
        }
    }

    /// <summary>Place of death. City FIPS code</summary>
    IJEField metaCITYCODE_D = new IJEField(140, 1345, 5, "Place of death. City FIPS code", "CITYCODE_D", 1);
    public String getCITYCODE_D()
    {
        return Map_Geo_Get("CITYCODE_D", "DeathLocationAddress", "address", "cityC", true);
    }
    public void setCITYCODE_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("COUNTYTEXT_D", "DeathLocationAddress", "address", "cityC", false, value);
        }
    }

    /// <summary>Place of death. Longitude</summary>
    IJEField metaLONG_D = new IJEField(141, 1350, 17, "Place of death. Longitude", "LONG_D", 1);
    public String getLONG_D()
    {
        return LeftJustified_Get("LONG_D", "DeathLocationLongitude");
    }
    public void setLONG_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("LONG_D", "DeathLocationLongitude", value);
        }
    }

    /// <summary>Place of Death. Latitude</summary>
    IJEField metaLAT_D = new IJEField(142, 1367, 17, "Place of Death. Latitude", "LAT_D", 1);
    public String getLAT_D()
    {
        return LeftJustified_Get("LAT_D", "DeathLocationLatitude");
    }
    public void setLAT_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("LAT_D", "DeathLocationLatitude", value);
        }
    }

    /// <summary>Decedent's spouse living at decedent's DOD?</summary>
    IJEField metaSPOUSELV = new IJEField(143, 1384, 1, "Decedent's spouse living at decedent's DOD?", "SPOUSELV", 1);
    public String getSPOUSELV()
    {
        return Get_MappingFHIRToIJE(Mappings.SpouseAlive.FHIRToIJE, "SpouseAlive", "SPOUSELV");
    }
    public void setSPOUSELV(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_MappingIJEToFHIR(Mappings.SpouseAlive.IJEToFHIR, "SPOUSELV", "SpouseAlive", value);
        }
    }

    /// <summary>Spouse's First Name</summary>
    IJEField metaSPOUSEF = new IJEField(144, 1385, 50, "Spouse's First Name", "SPOUSEF", 1);
    public String getSPOUSEF()
    {
        String[] names = record.getSpouseGivenNames();
        if (names.length > 0)
        {
            return StringUtils.rightPad(Truncate(names[0], 50), 50, " ");
        }
        return StringUtils.repeat(" ", 50);
    }
    public void setSPOUSEF(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setSpouseGivenNames(new String[] { value.trim() });
        }
    }

    /// <summary>Husband's Surname/Wife's Maiden Last Name</summary>
    IJEField metaSPOUSEL = new IJEField(145, 1435, 50, "Husband's Surname/Wife's Maiden Last Name", "SPOUSEL", 1);
    public String getSPOUSEL()
    {
        return LeftJustified_Get("SPOUSEL", "SpouseMaidenName");
    }
    public void setSPOUSEL(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("SPOUSEL", "SpouseMaidenName", value);
        }
    }

    /// <summary>Decedent's Residence - City or Town name</summary>
    IJEField metaCITYTEXT_R = new IJEField(152, 1560, 28, "Decedent's Residence - City or Town name", "CITYTEXT_R", 3);
    public String getCITYTEXT_R()
    {
        return Map_Geo_Get("CITYTEXT_R", "Residence", "address", "city", false);
    }
    public void setCITYTEXT_R(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("CITYTEXT_R", "Residence", "address", "city", false, value);
        }
    }

    /// <summary>Decedent's Residence - ZIP code</summary>
    IJEField metaZIP9_R = new IJEField(153, 1588, 9, "Decedent's Residence - ZIP code", "ZIP9_R", 1);
    public String getZIP9_R()
    {
        return Map_Geo_Get("ZIP9_R", "Residence", "address", "zip", false);
    }
    public void setZIP9_R(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("ZIP9_R", "Residence", "address", "zip", false, value);
        }
    }

    /// <summary>Decedent's Residence - County</summary>
    IJEField metaCOUNTYTEXT_R = new IJEField(154, 1597, 28, "Decedent's Residence - County", "COUNTYTEXT_R", 1);
    public String getCOUNTYTEXT_R()
    {
        return Map_Geo_Get("COUNTYTEXT_R", "Residence", "address", "county", false);
    }
    public void setCOUNTYTEXT_R(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("COUNTYTEXT_R", "Residence", "address", "county", false, value);
        }
    }

    /// <summary>Decedent's Residence - State name</summary>
    IJEField metaSTATETEXT_R = new IJEField(155, 1625, 28, "Decedent's Residence - State name", "STATETEXT_R", 1);
    public String getSTATETEXT_R()
    {
        // expand STATEC 2 letter code to full name
        String stateCode = Map_Geo_Get("STATEC", "Residence", "address", "state", false);
        //               var mortalityData = MortalityData.Instance;
        String statetextr = dataLookup.StateCodeToStateName(stateCode);
        if (statetextr == null)
        {
            statetextr = " ";
        }
        return StringUtils.rightPad(Truncate(statetextr, 28), 28, " ");
    }
    public void setSTATETEXT_R(String value)
    {
        // NOOP, this field does not exist in FHIR
    }

    /// <summary>Decedent's Residence - COUNTRY name</summary>
    IJEField metaCOUNTRYTEXT_R = new IJEField(156, 1653, 28, "Decedent's Residence - COUNTRY name", "COUNTRYTEXT_R", 1);
    public String getCOUNTRYTEXT_R()
    {
        // This is Now just the two letter code.  Need to map it to country name
        String countryCode = Map_Geo_Get("COUNTRYC", "Residence", "address", "country", false);
        //                var mortalityData = MortalityData.Instance;
        String countrytextr = dataLookup.CountryCodeToCountryName(countryCode);
        if (countrytextr == null)
        {
            countrytextr = " ";
        }
        return StringUtils.rightPad(Truncate(countrytextr, 28), 28, " ");
    }
    public void setCOUNTRYTEXT_R(String value)
    {
        // NOOP, field does not exist in FHIR
    }

    /// <summary>Long String address for decedent's place of residence same as above but allows states to choose the way they capture information.</summary>
    IJEField metaADDRESS_R = new IJEField(157, 1681, 50, "Long String address for decedent's place of residence same as above but allows states to choose the way they capture information.", "ADDRESS_R", 1);
    public String getADDRESS_R()
    {
        return Map_Get("ADDRESS_R", "Residence", "addressLine1");
    }
    public void setADDRESS_R(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Set("ADDRESS_R", "Residence", "addressLine1", value);
        }
    }

    /// <summary>Old NCHS residence state code</summary>
    IJEField metaRESSTATE = new IJEField(158, 1731, 2, "Old NCHS residence state code", "RESSTATE", 1);
    public String getRESSTATE()
    {
        // NOTE: This is a placeholder, the IJE field RESSTATE is not currently implemented in FHIR
        return "";
    }
    public void setRESSTATE(String value)
    {
        // NOTE: This is a placeholder, the IJE field RESSTATE is not currently implemented in FHIR
    }

    /// <summary>Old NCHS residence city/county combo code</summary>
    IJEField metaRESCON = new IJEField(159, 1733, 3, "Old NCHS residence city/county combo code", "RESCON", 1);
    public String getRESCON()
    {
        // NOTE: This is a placeholder, the IJE field RESCON is not currently implemented in FHIR
        return "";
    }
    public void setRESCON(String value)
    {
        // NOTE: This is a placeholder, the IJE field RESCON is not currently implemented in FHIR
    }

    /// <summary>Place of death. City FIPS code</summary>
    IJEField metaSTNUM_R = new IJEField(145, 1485, 10, "Place of death. Decedent's Residence - Street number", "STNUM_R", 1);
    public String getSTNUM_R()
    {
        return Map_Geo_Get("STNUM_R", "Residence", "address", "stnum", true);
    }
    public void setSTNUM_R(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("STNUM_R", "Residence", "address", "stnum", false, value);
        }
    }

    /// <summary>Pre directional </summary>
    IJEField metaPREDIR_R = new IJEField(146, 1495, 10, "Place of death. Decedent's Residence - Pre Directional", "PREDIR_R", 2);
    public String getPREDIR_R()
    {
        return Map_Geo_Get("PREDIR_R", "Residence", "address", "predir", true);
    }
    public void setPREDIR_R(String value)
    {
        // NOOP
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("PREDIR_R", "Residence", "address", "predir", false, value);
        }
    }

    /// <summary>Street name</summary>
    IJEField metaSTNAME_R = new IJEField(147, 1505, 28, "Place of death. Decedent's Residence - Street Name", "STNAME_R", 3);
    public String getSTNAME_R()
    {
        return Map_Geo_Get("STNAME_R", "Residence", "address", "stname", true);
    }
    public void setSTNAME_R(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("STNAME_R", "Residence", "address", "stname", false, value);
        }
    }

    /// <summary>Street designator</summary>
    IJEField metaSTDESIG_R = new IJEField(148, 1533, 10, "Place of death. Decedent's Residence - Street Designator", "STDESIG_R", 4);
    public String getSTDESIG_R()
    {
        return Map_Geo_Get("STDESIG_R", "Residence", "address", "stdesig", true);
    }
    public void setSTDESIG_R(String value)
    {
        // NOOP
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("STDESIG_R", "Residence", "address", "stdesig", false, value);
        }
    }

    /// <summary>Post Directional</summary>
    IJEField metaPOSTDIR_R = new IJEField(149, 1543, 10, "Place of death. Decedent's Residence - Post directional", "POSTDIR_R", 5);
    public String getPOSTDIR_R()
    {
        return Map_Geo_Get("POSTDIR_R", "Residence", "address", "postdir", true);
    }
    public void setPOSTDIR_R(String value)
    {
        // NOOP
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("POSTDIR_R", "Residence", "address", "postdir", false, value);
        }
    }

    /// <summary>Unit number</summary>
    IJEField metaUNITNUM_R = new IJEField(150, 1553, 7, "Place of death. Decedent's Residence - Unit number", "UNITNUM_R", 6);
    public String getUNITNUM_R()
    {
        return Map_Geo_Get("UNITNUM_R", "Residence", "address", "unitnum", true);
    }
    public void setUNITNUM_R(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("UNITNUM_R", "Residence", "address", "unitnum", false, value);
        }
    }

    /// <summary>Hispanic</summary>
    IJEField metaDETHNICE = new IJEField(160, 1736, 3, "Hispanic", "DETHNICE", 1);
    public String getDETHNICE()
    {
        return Get_MappingFHIRToIJE(Mappings.HispanicOrigin.FHIRToIJE, "HispanicCode", "DETHNICE");
    }
    public void setDETHNICE(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_MappingIJEToFHIR(Mappings.HispanicOrigin.IJEToFHIR, "DETHNICE", "HispanicCode", value);
        }
    }

    /// <summary>Bridged Race</summary>
    IJEField metaNCHSBRIDGE = new IJEField(161, 1739, 2, "Bridged Race", "NCHSBRIDGE", 1);
    public String getNCHSBRIDGE()
    {
        // NOTE: This is a placeholder, the IJE field NCHSBRIDGE is not currently implemented in FHIR
        return "";
    }
    public void setNCHSBRIDGE(String value)
    {
        // NOTE: This is a placeholder, the IJE field NCHSBRIDGE is not currently implemented in FHIR
    }

    /// <summary>Hispanic - old NCHS single ethnicity codes</summary>
    IJEField metaHISPOLDC = new IJEField(162, 1741, 1, "Hispanic - old NCHS single ethnicity codes", "HISPOLDC", 1);
    public String getHISPOLDC()
    {
        // NOTE: This is a placeholder, the IJE field HISPOLDC is not currently implemented in FHIR
        return "";
    }
    public void setHISPOLDC(String value)
    {

        // NOTE: This is a placeholder, the IJE field HISPOLDC is not currently implemented in FHIR
    }

    /// <summary>Race - old NCHS single race codes</summary>
    IJEField metaRACEOLDC = new IJEField(163, 1742, 1, "Race - old NCHS single race codes", "RACEOLDC", 1);
    public String getRACEOLDC()
    {
        // NOTE: This is a placeholder, the IJE field RACEOLDC is not currently implemented in FHIR
        return "";
    }
    public void setRACEOLDC(String value)
    {
        // NOTE: This is a placeholder, the IJE field RACEOLDC is not currently implemented in FHIR
    }

    /// <summary>Hispanic Origin - Specify</summary>
    IJEField metaHISPSTSP = new IJEField(164, 1743, 15, "Hispanic Origin - Specify", "HISPSTSP", 1);
    public String getHISPSTSP()
    {
        // NOTE: This is a placeholder, the IJE field HISPSTSP is not currently implemented in FHIR
        return "";
    }
    public void setHISPSTSP(String value)
    {
        // NOTE: This is a placeholder, the IJE field HISPSTSP is not currently implemented in FHIR
    }

    /// <summary>Race - Specify</summary>
    IJEField metaRACESTSP = new IJEField(165, 1758, 50, "Race - Specify", "RACESTSP", 1);
    public String getRACESTSP()
    {
        // NOTE: This is a placeholder, the IJE field RACESTSP is not currently implemented in FHIR
        return "";
    }
    public void setRACESTSP(String value)
    {
        // NOTE: This is a placeholder, the IJE field RACESTSP is not currently implemented in FHIR
    }

    /// <summary>Middle Name of Decedent</summary>
    IJEField metaDMIDDLE = new IJEField(166, 1808, 50, "Middle Name of Decedent", "DMIDDLE", 3);
    public String getDMIDDLE()
    {
        String[] names = record.getGivenNames();
        if (names.length > 1)
        {
            return StringUtils.rightPad(Truncate(names[1], 50), 50, " ");
        }
        return StringUtils.repeat(" ", 50);
    }
    public void setDMIDDLE(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            if (isNullOrWhiteSpace(GNAME)) throw new IllegalArgumentException("Middle name cannot be set before first name");
            if (record.getGivenNames() != null)
            {
                List<String> names = Arrays.asList(record.getGivenNames());
                if (names.size() > 1)
                    names.set(1, value.trim());
                else
                    names.add(value.trim());
                record.setGivenNames((String[]) names.toArray());
            }
        }
    }

    /// <summary>Father's First Name</summary>
    IJEField metaDDADF = new IJEField(167, 1858, 50, "Father's First Name", "DDADF", 1);
    public String getDDADF()
    {
        String[] names = record.getFatherGivenNames();
        if (names != null && names.length > 0)
        {
            return StringUtils.rightPad(Truncate(names[0], 50), 50, " ");
        }
        return StringUtils.repeat(" ", 50);
    }
    public void setDDADF(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setFatherGivenNames(new String[] { value.trim() });
        }
    }

    /// <summary>Father's Middle Name</summary>
    IJEField metaDDADMID = new IJEField(168, 1908, 50, "Father's Middle Name", "DDADMID", 2);
    public String getDDADMID()
    {
        String[] names = record.getFatherGivenNames();
        if (names != null && names.length > 1)
        {
            return StringUtils.rightPad(Truncate(names[1], 50), 50, " ");
        }
        return StringUtils.repeat(" ", 50);
    }
    public void setDDADMID(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            if (isNullOrWhiteSpace(DDADF)) throw new IllegalArgumentException("Middle name cannot be set before first name");
            if (record.getFatherGivenNames() != null)
            {
                List<String> names = Arrays.asList(record.getFatherGivenNames());
                if (names.size() > 1)
                    names.set(1, value.trim());
                else
                    names.add(value.trim());
                record.setFatherGivenNames((String[]) names.toArray());
            }
        }
    }

    /// <summary>Mother's First Name</summary>
    IJEField metaDMOMF = new IJEField(169, 1958, 50, "Mother's First Name", "DMOMF", 1);
    public String getDMOMF()
    {
        String[] names = record.getMotherGivenNames();
        if (names != null && names.length > 0)
        {
            return StringUtils.rightPad(Truncate(names[0], 50), 50, " ");
        }
        return StringUtils.repeat(" ", 50);
    }
    public void setDMOMF(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setMotherGivenNames(new String[] { value.trim() });
        }
    }

    /// <summary>Mother's Middle Name</summary>
    IJEField metaDMOMMID = new IJEField(170, 2008, 50, "Mother's Middle Name", "DMOMMID", 2);
    public String getDMOMMID()
    {
        String[] names = record.getMotherGivenNames();
        if (names != null && names.length > 1)
        {
            return StringUtils.rightPad(Truncate(names[1], 50), 50, " ");
        }
        return StringUtils.repeat(" ", 50);
    }
    public void setDMOMMID(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            if (isNullOrWhiteSpace(DMOMF)) throw new IllegalArgumentException("Middle name cannot be set before first name");
            if (record.getMotherGivenNames() != null)
            {
                List<String> names = Arrays.asList(record.getMotherGivenNames());
                if (names.size() > 1)
                    names.set(1, value.trim());
                else
                    names.add(value.trim());
                record.setMotherGivenNames((String[]) names.toArray());
            }
        }
    }

    /// <summary>Mother's Maiden Surname</summary>
    IJEField metaDMOMMDN = new IJEField(171, 2058, 50, "Mother's Maiden Surname", "DMOMMDN", 1);
    public String getDMOMMDN()
    {
        return LeftJustified_Get("DMOMMDN", "MotherMaidenName");
    }
    public void setDMOMMDN(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("DMOMMDN", "MotherMaidenName", value);
        }
    }

    /// <summary>Was case Referred to Medical Examiner/Coroner?</summary>
    IJEField metaREFERRED = new IJEField(172, 2108, 1, "Was case Referred to Medical Examiner/Coroner?", "REFERRED", 1);
    public String getREFERRED()
    {
        return Get_MappingFHIRToIJE(Mappings.YesNoUnknown.FHIRToIJE, "ExaminerContacted", "REFERRED");
    }
    public void setREFERRED(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_MappingIJEToFHIR(Mappings.YesNoUnknown.IJEToFHIR, "REFERRED", "ExaminerContacted", value);
        }
    }

    /// <summary>Place of Injury- literal</summary>
    IJEField metaPOILITRL = new IJEField(173, 2109, 50, "Place of Injury- literal", "POILITRL", 1);
    public String getPOILITRL()
    {
        return LeftJustified_Get("POILITRL", "InjuryPlaceDescription");
    }
    public void setPOILITRL(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("POILITRL", "InjuryPlaceDescription", value);
        }
    }

    /// <summary>Describe How Injury Occurred</summary>
    IJEField metaHOWINJ = new IJEField(174, 2159, 250, "Describe How Injury Occurred", "HOWINJ", 1);
    public String getgetHOWINJ()
    {
        return LeftJustified_Get("HOWINJ", "InjuryDescription");
    }
    public void setHOWINJ(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("HOWINJ", "InjuryDescription", value);
        }
    }

    /// <summary>If Transportation Accident, Specify</summary>
    IJEField metaTRANSPRT = new IJEField(175, 2409, 30, "If Transportation Accident, Specify", "TRANSPRT", 1);
    public String getTRANSPRT()
    {
        String ret = record.getTransportationRoleHelper();
        if (ret != null && Mappings.TransportationIncidentRole.FHIRToIJE.containsKey(ret))
        {
            return Get_MappingFHIRToIJE(Mappings.TransportationIncidentRole.FHIRToIJE, "TransportationRole", "TRANSPRT");
        }
        else
        {
            return ret;  // If the return value is not a code, it is just an arbitrary String, so return it.
        }
    }
    public void setTRANSPRT(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            if (Mappings.TransportationIncidentRole.IJEToFHIR.containsKey(value.split(" ")[0]))
            {
                Set_MappingIJEToFHIR(Mappings.TransportationIncidentRole.IJEToFHIR, "TRANSPRT", "TransportationRole", value.trim());
            }
            else
            {
                record.setTransportationRoleHelper(value);   // If the value is not a valid code, it is just an arbitrary String.  The helper will deal with it.
            }
        }
    }

    /// <summary>County of Injury - literal</summary>
    IJEField metaCOUNTYTEXT_I = new IJEField(176, 2439, 28, "County of Injury - literal", "COUNTYTEXT_I", 1);
    public String getCOUNTYTEXT_I()
    {
        return Map_Geo_Get("COUNTYTEXT_I", "InjuryLocationAddress", "address", "county", false);
    }
    public void setCOUNTYTEXT_I(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("COUNTYTEXT_I", "InjuryLocationAddress", "address", "county", false, value);
        }
    }

    /// <summary>County of Injury code</summary>
    IJEField metaCOUNTYCODE_I = new IJEField(177, 2467, 3, "County of Injury code", "COUNTYCODE_I", 2);
    public String getCOUNTYCODE_I()
    {
        return Map_Geo_Get("COUNTYCODE_I", "InjuryLocationAddress", "address", "countyC", true);
    }
    public void setCOUNTYCODE_I(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("COUNTYCODE_I", "InjuryLocationAddress", "address", "countyC", true, value);
        }
    }

    /// <summary>Town/city of Injury - literal</summary>
    IJEField metaCITYTEXT_I = new IJEField(178, 2470, 28, "Town/city of Injury - literal", "CITYTEXT_I", 3);
    public String getCITYTEXT_I()
    {
        return Map_Geo_Get("CITYTEXT_I", "InjuryLocationAddress", "address", "city", false);
    }
    public void setCITYTEXT_I(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("CITYTEXT_I", "InjuryLocationAddress", "address", "city", false, value);
        }
    }

    /// <summary>Town/city of Injury code</summary>
    IJEField metaCITYCODE_I = new IJEField(179, 2498, 5, "Town/city of Injury code", "CITYCODE_I", 3);
    public String getCITYCODE_I()
    {
        return Map_Geo_Get("CITYCODE_I", "InjuryLocationAddress", "address", "cityC", true);
    }
    public void setCITYCODE_I(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("CITYCODE_I", "InjuryLocationAddress", "address", "cityC", true, value);
        }
    }

    /// <summary>State, U.S. Territory or Canadian Province of Injury - code</summary>
    IJEField metaSTATECODE_I = new IJEField(180, 2503, 2, "State, U.S. Territory or Canadian Province of Injury - code", "STATECODE_I", 1);
    public String getSTATECODE_I()
    {
        return Map_Geo_Get("STATECODE_I", "InjuryLocationAddress", "address", "state", true);
    }
    public void setSTATECODE_I(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("STATECODE_I", "InjuryLocationAddress", "address", "state", true, value);
        }
    }

    /// <summary>Place of injury. Longitude</summary>
    IJEField metaLONG_I = new IJEField(181, 2505, 17, "Place of injury. Longitude", "LONG_I", 1);
    public String getLONG_I()
    {
        return LeftJustified_Get("LONG_I", "InjuryLocationLongitude");
    }
    public void setLONG_I(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("LONG_I", "InjuryLocationLongitude", value);
        }
    }

    /// <summary>Place of injury. Latitude</summary>
    IJEField metaLAT_I = new IJEField(182, 2522, 17, "Place of injury. Latitude", "LAT_I", 1);
    public String getLAT_I()
    {
        return LeftJustified_Get("LAT_I", "InjuryLocationLatitude");
    }
    public void setLAT_I(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("LAT_I", "InjuryLocationLatitude", value);
        }
    }

    /// <summary>Old NCHS education code if collected - receiving state will recode as they prefer</summary>
    IJEField metaOLDEDUC = new IJEField(183, 2539, 2, "Old NCHS education code if collected - receiving state will recode as they prefer", "OLDEDUC", 1);
    public String getOLDEDUC()
    {
        // NOTE: This is a placeholder, the IJE field OLDEDUC is not currently implemented in FHIR
        return "";
    }
    public void setOLDEDUC(String value)
    {
        // NOTE: This is a placeholder, the IJE field OLDEDUC is not currently implemented in FHIR
    }

    /// <summary>Replacement Record -- suggested codes</summary>
    IJEField metaREPLACE = new IJEField(184, 2541, 1, "Replacement Record -- suggested codes", "REPLACE", 1);
    public String getREPLACE()
    {
        return Get_MappingFHIRToIJE(Mappings.ReplaceStatus.FHIRToIJE, "ReplaceStatus", "REPLACE");
    }
    public void setREPLACE(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Set_MappingIJEToFHIR(Mappings.ReplaceStatus.IJEToFHIR, "REPLACE", "ReplaceStatus", value);
        }
    }

    /// <summary>Cause of Death Part I Line a</summary>
    IJEField metaCOD1A = new IJEField(185, 2542, 120, "Cause of Death Part I Line a", "COD1A", 1);
    public String getCOD1A()
    {
        if (!isNullOrWhiteSpace(record.getCOD1A()))
        {
            return record.getCOD1A().trim();
        }
        return "";
    }
    public void setCOD1A(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setCOD1A(value.trim());
        }
    }

    /// <summary>Cause of Death Part I Interval, Line a</summary>
    IJEField metaINTERVAL1A = new IJEField(186, 2662, 20, "Cause of Death Part I Interval, Line a", "INTERVAL1A", 2);
    public String getINTERVAL1A()
    {
        if (!isNullOrWhiteSpace(record.getINTERVAL1A()))
        {
            return record.getINTERVAL1A().trim();
        }
        return "";
    }
    public void setINTERVAL1A(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setINTERVAL1A(value.trim());
        }
    }

    /// <summary>Cause of Death Part I Line b</summary>
    IJEField metaCOD1B = new IJEField(187, 2682, 120, "Cause of Death Part I Line b", "COD1B", 3);
    public String getCOD1B()
    {
        if (!isNullOrWhiteSpace(record.getCOD1B()))
        {
            return record.getCOD1B().trim();
        }
        return "";
    }
    public void setCOD1B(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setCOD1B(value.trim());
        }
    }

    /// <summary>Cause of Death Part I Interval, Line b</summary>
    IJEField metaINTERVAL1B = new IJEField(188, 2802, 20, "Cause of Death Part I Interval, Line b", "INTERVAL1B", 4);
    public String getINTERVAL1B()
    {
        if (!isNullOrWhiteSpace(record.getINTERVAL1B()))
        {
            return record.getINTERVAL1B().trim();
        }
        return "";
    }
    public void setINTERVAL1B(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setINTERVAL1B(value.trim());
        }
    }

    /// <summary>Cause of Death Part I Line c</summary>
    IJEField metaCOD1C = new IJEField(189, 2822, 120, "Cause of Death Part I Line c", "COD1C", 5);
    public String getCOD1C()
    {
        if (!isNullOrWhiteSpace(record.getCOD1C()))
        {
            return record.getCOD1C().trim();
        }
        return "";
    }
    public void setCOD1C(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setCOD1C(value.trim());
        }
    }

    /// <summary>Cause of Death Part I Interval, Line c</summary>
    IJEField metaINTERVAL1C = new IJEField(190, 2942, 20, "Cause of Death Part I Interval, Line c", "INTERVAL1C", 6);
    public String getINTERVAL1C()
    {
        if (!isNullOrWhiteSpace(record.getINTERVAL1C()))
        {
            return record.getINTERVAL1C().trim();
        }
        else
            return "";
    }
    public void setINTERVAL1C(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setINTERVAL1C(value.trim());
        }
    }

    /// <summary>Cause of Death Part I Line d</summary>
    IJEField metaCOD1D = new IJEField(191, 2962, 120, "Cause of Death Part I Line d", "COD1D", 7);
    public String getCOD1D()
    {
        if (!isNullOrWhiteSpace(record.getCOD1D()))
        {
            return record.getCOD1D().trim();
        }
        return "";
    }
    public void setCOD1D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setCOD1D(value.trim());
        }
    }

    /// <summary>Cause of Death Part I Interval, Line d</summary>
    IJEField metaINTERVAL1D = new IJEField(192, 3082, 20, "Cause of Death Part I Interval, Line d", "INTERVAL1D", 8);
    public String getINTERVAL1D()
    {
        if (!isNullOrWhiteSpace(record.getINTERVAL1D()))
        {
            return record.getINTERVAL1D().trim();
        }
        return "";
    }
    public void setINTERVAL1D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setINTERVAL1D(value.trim());
        }
    }

    /// <summary>Cause of Death Part II</summary>
    IJEField metaOTHERCONDITION = new IJEField(193, 3102, 240, "Cause of Death Part II", "OTHERCONDITION", 1);
    public String getOTHERCONDITION()
    {
        if (record.getContributingConditions() != null)
        {
            return record.getContributingConditions().trim();
        }
        return "";
    }
    public void setOTHERCONDITION(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setContributingConditions(value.trim());
        }
    }

    /// <summary>Decedent's Maiden Name</summary>
    IJEField metaDMAIDEN = new IJEField(194, 3342, 50, "Decedent's Maiden Name", "DMAIDEN", 1);
    public String getDMAIDEN()
    {
        return LeftJustified_Get("DMAIDEN", "MaidenName");
    }
    public void setDMAIDEN(String value)
    {
        LeftJustified_Set("DMAIDEN", "MaidenName", value);
    }

    /// <summary>Decedent's Birth Place City - Code</summary>
    IJEField metaDBPLACECITYCODE = new IJEField(194, 3392, 5, "Decedent's Birth Place City - Code", "DBPLACECITYCODE", 3);
    public String getDBPLACECITYCODE()
    {
        return Map_Geo_Get("DBPLACECITYCODE", "PlaceOfBirth", "address", "cityC", false);
    }
    public void setDBPLACECITYCODE(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("DBPLACECITYCODE", "PlaceOfBirth", "address", "cityC", false, value);
        }
    }

    /// <summary>Decedent's Birth Place City - Literal</summary>
    IJEField metaDBPLACECITY = new IJEField(196, 3397, 28, "Decedent's Birth Place City - Literal", "DBPLACECITY", 3);
    public String getDBPLACECITY()
    {
        return Map_Geo_Get("DBPLACECITY", "PlaceOfBirth", "address", "city", false);
    }
    public void setDBPLACECITY(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("DBPLACECITY", "PlaceOfBirth", "address", "city", false, value);
        }
    }

    /// <summary>Informant's Relationship</summary>
    IJEField metaINFORMRELATE = new IJEField(200, 3505, 30, "Informant's Relationship", "INFORMRELATE", 3);
    public String getINFORMRELATE()
    {
        return Map_Get("INFORMRELATE", "ContactRelationship", "text");
    }
    public void setINFORMRELATE(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Set("INFORMRELATE", "ContactRelationship", "text", value);
        }
    }

    /// <summary>Spouse's Middle Name</summary>
    IJEField metaSPOUSEMIDNAME = new IJEField(197, 3425, 50, "Spouse's Middle Name", "SPOUSEMIDNAME", 2);
    public String getSPOUSEMIDNAME()
    {
        String[] names = record.getSpouseGivenNames();
        if (names != null && names.length > 1)
        {
            return StringUtils.rightPad(Truncate(names[1], 50), 50, " ");
        }
        return StringUtils.repeat(" ", 50);
    }
    public void setSPOUSEMIDNAME(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            if (isNullOrWhiteSpace(SPOUSEF)) throw new IllegalArgumentException("Middle name cannot be set before first name");
            if (record.getSpouseGivenNames() != null)
            {
                List<String> names = Arrays.asList(record.getSpouseGivenNames());
                if (names.size() > 1)
                    names.set(1, value.trim());
                else
                    names.add(value.trim());
                record.setSpouseGivenNames((String[]) names.toArray());
            }
        }
    }

    /// <summary>Spouse's Suffix</summary>
    IJEField metaSPOUSESUFFIX = new IJEField(198, 3475, 10, "Spouse's Suffix", "SPOUSESUFFIX", 1);
    public String getSPOUSESUFFIX()
    {
        return LeftJustified_Get("SPOUSESUFFIX", "SpouseSuffix");
    }
    public void setSPOUSESUFFIX(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("SPOUSESUFFIX", "SpouseSuffix", value.trim());
        }
    }

    /// <summary>Father's Suffix</summary>
    IJEField metaFATHERSUFFIX = new IJEField(199, 3485, 10, "Father's Suffix", "FATHERSUFFIX", 1);
    public String getFATHERSUFFIX()
    {
        return LeftJustified_Get("FATHERSUFFIX", "FatherSuffix");
    }
    public void setFATHERSUFFIX(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("FATHERSUFFIX", "FatherSuffix", value.trim());
        }
    }

    /// <summary>Mother's Suffix</summary>
    IJEField metaMOTHERSSUFFIX = new IJEField(200, 3495, 10, "Mother's Suffix", "MOTHERSSUFFIX", 1);
    public String getMOTHERSSUFFIX()
    {

        return LeftJustified_Get("MOTHERSSUFFIX", "MotherSuffix");
    }
    public void setMOTHERSSUFFIX(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("MOTHERSSUFFIX", "MotherSuffix", value.trim());
        }
    }

    /// <summary>State, U.S. Territory or Canadian Province of Disposition - code</summary>
    IJEField metaDISPSTATECD = new IJEField(202, 3535, 2, "State, U.S. Territory or Canadian Province of Disposition - code", "DISPSTATECD", 1);
    public String getDISPSTATECD()
    {
        return Map_Geo_Get("DISPSTATECD", "DispositionLocationAddress", "address", "state", true);
    }
    public void setDISPSTATECD(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("DISPSTATECD", "DispositionLocationAddress", "address", "state", true, value);
        }
    }

    /// <summary>Disposition State or Territory - Literal</summary>
    IJEField metaDISPSTATE = new IJEField(203, 3537, 28, "Disposition State or Territory - Literal", "DISPSTATE", 1);
    public String getDISPSTATE()
    {
        String stateCode = Map_Geo_Get("DISPSTATECD", "InjuryLocationAddress", "address", "state", false);
        //                var mortalityData = MortalityData.Instance;
        return dataLookup.StateCodeToStateName(stateCode);
    }
    public void setDISPSTATE(String value)
    {
        // NOOP
    }

    /// <summary>Disposition City - Code</summary>
    IJEField metaDISPCITYCODE = new IJEField(204, 3565, 5, "Disposition City - Code", "DISPCITYCODE", 1);
    public String getDISPCITYCODE()
    {
        return Map_Geo_Get("DISPCITYCODE", "DispositionLocationAddress", "address", "cityC", false);
    }
    public void setDISPCITYCODE(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("DISPCITYCODE", "DispositionLocationAddress", "address", "cityC", false, value);
        }
    }

    /// <summary>Disposition City - Literal</summary>
    IJEField metaDISPCITY = new IJEField(205, 3570, 28, "Disposition City - Literal", "DISPCITY", 3);
    public String getDISPCITY()
    {
        return Map_Geo_Get("DISPCITY", "DispositionLocationAddress", "address", "city", false);
    }
    public void setDISPCITY(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("DISPCITY", "DispositionLocationAddress", "address", "city", false, value);
        }
    }

    /// <summary>Funeral Facility Name</summary>
    IJEField metaFUNFACNAME = new IJEField(206, 3598, 100, "Funeral Facility Name", "FUNFACNAME", 1);
    public String getFUNFACNAME()
    {

        return LeftJustified_Get("FUNFACNAME", "FuneralHomeName");
    }
    public void setFUNFACNAME(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("FUNFACNAME", "FuneralHomeName", value);
        }
    }

    /// <summary>Funeral Facility - Street number</summary>
    IJEField metaFUNFACSTNUM = new IJEField(207, 3698, 10, "Funeral Facility - Street number", "FUNFACSTNUM", 1);
    public String getFUNFACSTNUM()
    {
        return Map_Geo_Get("FUNFACSTNUM", "FuneralHomeAddress", "address", "stnum", true);
    }
    public void setFUNFACSTNUM(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("FUNFACSTNUM", "FuneralHomeAddress", "address", "stnum", false, value);
        }
    }

    /// <summary>Funeral Facility - Pre Directional</summary>
    IJEField metaFUNFACPREDIR = new IJEField(208, 3708, 10, "Funeral Facility - Pre Directional", "FUNFACPREDIR", 1);
    public String getFUNFACPREDIR()
    {
        return Map_Geo_Get("FUNFACPREDIR", "FuneralHomeAddress", "address", "predir", true);
    }
    public void setFUNFACPREDIR(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("FUNFACPREDIR", "FuneralHomeAddress", "address", "predir", false, value);
        }
    }

    /// <summary>Funeral Facility - Street name</summary>
    IJEField metaFUNFACSTRNAME = new IJEField(209, 3718, 28, "Funeral Facility - Street name", "FUNFACSTRNAME", 1);
    public String getFUNFACSTRNAME()
    {
        return Map_Geo_Get("FUNFACSTRNAME", "FuneralHomeAddress", "address", "stname", true);
    }
    public void setFUNFACSTRNAME(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("FUNFACSTRNAME", "FuneralHomeAddress", "address", "stname", false, value);
        }
    }

    /// <summary>Funeral Facility - Street designator</summary>
    IJEField metaFUNFACSTRDESIG = new IJEField(210, 3746, 10, "Funeral Facility - Street designator", "FUNFACSTRDESIG", 1);
    public String getFUNFACSTRDESIG()
    {
        return Map_Geo_Get("FUNFACSTRDESIG", "FuneralHomeAddress", "address", "stdesig", true);
    }
    public void setFUNFACSTRDESIG(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("FUNFACSTRDESIG", "FuneralHomeAddress", "address", "stdesig", false, value);
        }
    }

    /// <summary>Funeral Facility - Post Directional</summary>
    IJEField metaFUNPOSTDIR = new IJEField(211, 3756, 10, "Funeral Facility - Post Directional", "FUNPOSTDIR", 1);
    public String getFUNPOSTDIR()
    {
        return Map_Geo_Get("FUNPOSTDIR", "FuneralHomeAddress", "address", "postdir", true);
    }
    public void setFUNPOSTDIR(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("FUNPOSTDIR", "FuneralHomeAddress", "address", "postdir", false, value);
        }
    }

    /// <summary>Funeral Facility - Unit or apt number</summary>
    IJEField metaFUNUNITNUM = new IJEField(212, 3766, 7, "Funeral Facility - Unit or apt number", "FUNUNITNUM", 1);
    public String getFUNUNITNUM()
    {
        return Map_Geo_Get("FUNUNITNUM", "FuneralHomeAddress", "address", "unitnum", true);
    }
    public void setFUNUNITNUM(String value)
    {
        if(!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("FUNUNITNUM", "FuneralHomeAddress", "address", "unitnum", false, value);
        }
    }

    /// <summary>Long String address for Funeral Facility same as above but allows states to choose the way they capture information.</summary>
    IJEField metaFUNFACADDRESS = new IJEField(213, 3773, 50, "Long String address for Funeral Facility same as above but allows states to choose the way they capture information.", "FUNFACADDRESS", 1);
    public String getFUNFACADDRESS()
    {
        return Map_Get("FUNFACADDRESS", "FuneralHomeAddress", "addressLine1");
    }
    public void setFUNFACADDRESS(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Set("FUNFACADDRESS", "FuneralHomeAddress", "addressLine1", value);
        }
    }

    /// <summary>Funeral Facility - City or Town name</summary>
    IJEField metaFUNCITYTEXT = new IJEField(214, 3823, 28, "Funeral Facility - City or Town name", "FUNCITYTEXT", 3);
    public String getFUNCITYTEXT()
    {
        return Map_Get("FUNCITYTEXT", "FuneralHomeAddress", "addressCity");
    }
    public void setFUNCITYTEXT(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Set("FUNCITYTEXT", "FuneralHomeAddress", "addressCity", value);
        }
    }

    /// <summary>State, U.S. Territory or Canadian Province of Funeral Facility - code</summary>
    IJEField metaFUNSTATECD = new IJEField(215, 3851, 2, "State, U.S. Territory or Canadian Province of Funeral Facility - code", "FUNSTATECD", 1);
    public String getFUNSTATECD()
    {
        return Map_Geo_Get("FUNSTATECD", "FuneralHomeAddress", "address", "state", true);
    }
    public void setFUNSTATECD(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("FUNSTATECD", "FuneralHomeAddress", "address", "state", true, value);
        }
    }

    /// <summary>State, U.S. Territory or Canadian Province of Funeral Facility - literal</summary>
    IJEField metaFUNSTATE = new IJEField(216, 3853, 28, "State, U.S. Territory or Canadian Province of Funeral Facility - literal", "FUNSTATE", 1);
    public String getFUNSTATE()
    {
        String stateCode = Map_Geo_Get("FUNSTATE", "FuneralHomeAddress", "address", "state", false);
        //                var mortalityData = MortalityData.Instance;
        String funstate = dataLookup.StateCodeToStateName(stateCode);
        if (funstate == null)
        {
            funstate = " ";
        }
        return StringUtils.rightPad(Truncate(funstate, 28), 28, " ");
    }
    public void setFUNSTATE(String value)
    {
        // NOOP
    }

    /// <summary>Funeral Facility - ZIP</summary>
    IJEField metaFUNZIP = new IJEField(217, 3881, 9, "Funeral Facility - ZIP", "FUNZIP", 1);
    public String getFUNZIP()
    {
        return Map_Get("FUNZIP", "FuneralHomeAddress", "addressZip");
    }
    public void setFUNZIP(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Set("FUNZIP", "FuneralHomeAddress", "addressZip", value);
        }
    }

    /// <summary>Person Pronouncing Date Signed</summary>
    IJEField metaPPDATESIGNED = new IJEField(218, 3890, 8, "Person Pronouncing Date Signed", "PPDATESIGNED", 1);
    public String getPPDATESIGNED()
    {
        Integer month = record.getDateOfDeathPronouncementMonth();
        Integer day = record.getDateOfDeathPronouncementDay();
        Integer year = record.getDateOfDeathPronouncementYear();
        if (month == null || day == null || year == null)
        {
            return StringUtils.repeat(" ", 8);
        }
        else
        {
            return String.Format("{0:00}{1:00}{2:0000}", month, day, year);
        }
    }
    public void setPPDATESIGNED(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            String mm = value.substring(0, 2);
            String dd = value.substring(2, 2);
            String yyyy = value.substring(4, 4);
            record.setDateOfDeathPronouncementMonth(mm);
            record.setDateOfDeathPronouncementDay(dd);
            record.setDateOfDeathPronouncementYear(yyyy);
        }
    }

    /// <summary>Person Pronouncing Time Pronounced</summary>
    IJEField metaPPTIME = new IJEField(219, 3898, 4, "Person Pronouncing Time Pronounced", "PPTIME", 1);
    public String getPPTIME()
    {
        String fhirTimeStr = record.getDateOfDeathPronouncementTime();
        if (fhirTimeStr == null) {
            return "    ";
        }
        else {
            String HH = fhirTimeStr.substring(0, 2);
            String mm = fhirTimeStr.substring(3, 2);
            String ijeTime = HH + mm;
            return ijeTime;
        }
    }
    public void setPPTIME(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            String HH = value.substring(0, 2);
            String mm = value.substring(2, 2);
            String fhirTimeStr = HH + ":" + mm + ":00";
            record.setDateOfDeathPronouncementTime(fhirTimeStr);
        }
    }

    /// <summary>Certifier's First Name</summary>
    IJEField metaCERTFIRST = new IJEField(220, 3902, 50, "Certifier's First Name", "CERTFIRST", 1);
    public String getCERTFIRST()
    {
        String[] names = record.getCertifierGivenNames();
        if (names != null && names.length > 0)
        {
            return StringUtils.rightPad(Truncate(names[0], 50), 50, " ");
        }
        return StringUtils.repeat(" ", 50);
    }
    public void setCERTFIRST(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            record.setCertifierGivenNames(new String[] { value.trim() });
        }
    }

    /// <summary>Certifier's Middle Name </summary>
    IJEField metaCERTMIDDLE = new IJEField(221, 3952, 50, "Certifier's Middle Name", "CERTMIDDLE", 2);
    public String getCERTMIDDLE()
    {
        String[] names = record.getCertifierGivenNames();
        if (names != null && names.length > 1)
        {
            return StringUtils.rightPad(Truncate(names[1], 50), 50, " ");
        }
        return StringUtils.repeat(" ", 50);
    }
    public void setCERTMIDDLE(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            if (isNullOrWhiteSpace(CERTFIRST)) throw new IllegalArgumentException("Middle name cannot be set before first name");
            if (record.getGivenNames() != null)
            {
                List<String> names = Arrays.asList(record.getCertifierGivenNames());
                if (names.size() > 1)
                    names.set(1, value.trim());
                else
                    names.add(value.trim());
                record.setCertifierGivenNames((String[]) names.toArray());
            }
        }
    }

    /// <summary>Certifier's Last Name</summary>
    IJEField metaCERTLAST = new IJEField(222, 4002, 50, "Certifier's Last Name", "CERTLAST", 3);
    public String getCERTLAST()
    {
        return LeftJustified_Get("CERTLAST", "CertifierFamilyName");
    }
    public void setCERTLAST(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("CERTLAST", "CertifierFamilyName", value);
        }
    }

    /// <summary>Certifier's Suffix Name</summary>
    IJEField metaCERTSUFFIX = new IJEField(223, 4052, 10, "Certifier's Suffix Name", "CERTSUFFIX", 4);
    public String getCERTSUFFIX()
    {
        return LeftJustified_Get("CERTSUFFIX", "CertifierSuffix");
    }
    public void setCERTSUFFIX(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("CERTSUFFIX", "CertifierSuffix", value);
        }
    }

    /// <summary>Certifier - Street number</summary>
    IJEField metaCERTSTNUM = new IJEField(224, 4062, 10, "Certifier - Street number", "CERTSTNUM", 1);
    public String getCERTSTNUM()
    {
        return Map_Geo_Get("CERTSTNUM", "CertifierAddress", "address", "stnum", true);
    }
    public void setCERTSTNUM(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("CERTSTNUM", "CertifierAddress", "address", "stnum", false, value);
        }
    }

    /// <summary>Certifier - Pre Directional</summary>
    IJEField metaCERTPREDIR = new IJEField(225, 4072, 10, "Certifier - Pre Directional", "CERTPREDIR", 1);
    public String getCERTPREDIR()
    {
        return Map_Geo_Get("CERTPREDIR", "CertifierAddress", "address", "predir", true);
    }
    public void setCERTPREDIR(String value)
    {
        // NOOP
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("CERTPREDIR", "CertifierAddress", "address", "predir", false, value);
        }
    }

    /// <summary>Certifier - Street name</summary>
    IJEField metaCERTSTRNAME = new IJEField(226, 4082, 28, "Certifier - Street name", "CERTSTRNAME", 1);
    public String getCERTSTRNAME()
    {
        return Map_Geo_Get("CERTSTRNAME", "CertifierAddress", "address", "stname", true);
    }
    public void setCERTSTRNAME(String value)
    {
        // NOOP
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("CERTSTRNAME", "CertifierAddress", "address", "stname", false, value);
        }
    }

    /// <summary>Certifier - Street designator</summary>
    IJEField metaCERTSTRDESIG = new IJEField(227, 4110, 10, "Certifier - Street designator", "CERTSTRDESIG", 1);
    public String getCERTSTRDESIG()
    {
        return Map_Geo_Get("CERTSTRDESIG", "CertifierAddress", "address", "stdesig", true);
    }
    public void setCERTSTRDESIG(String value)
    {
        // NOOP
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("CERTSTRDESIG", "CertifierAddress", "address", "stdesig", false, value);
        }
    }

    /// <summary>Certifier - Post Directional</summary>
    IJEField metaCERTPOSTDIR = new IJEField(228, 4120, 10, "Certifier - Post Directional", "CERTPOSTDIR", 1);
    public String getCERTPOSTDIR()
    {
        return Map_Geo_Get("CERTPOSTDIR", "CertifierAddress", "address", "postdir", true);
    }
    public void setCERTPOSTDIR(String value)
    {
        // NOOP
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("CERTPOSTDIR", "CertifierAddress", "address", "postdir", false, value);
        }
    }

    /// <summary>Certifier - Unit or apt number</summary>
    IJEField metaCERTUNITNUM = new IJEField(229, 4130, 7, "Certifier - Unit or apt number", "CERTUNITNUM", 1);
    public String getCERTUNITNUM()
    {
        return Map_Geo_Get("CERTUNITNUM", "CertifierAddress", "address", "unitnum", true);
    }
    public void setCERTUNITNUM(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("CERTUNITNUM", "CertifierAddress", "address", "unitnum", false, value);
        }
    }

    /// <summary>Long String address for Certifier same as above but allows states to choose the way they capture information.</summary>
    IJEField metaCERTADDRESS = new IJEField(230, 4137, 50, "Long String address for Certifier same as above but allows states to choose the way they capture information.", "CERTADDRESS", 1);
    public String getCERTADDRESS()
    {
        return Map_Get("CERTADDRESS", "CertifierAddress", "addressLine1");
    }
    public void setCERTADDRESS(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Set("CERTADDRESS", "CertifierAddress", "addressLine1", value);
        }
    }

    /// <summary>Certifier - City or Town name</summary>
    IJEField metaCERTCITYTEXT = new IJEField(231, 4187, 28, "Certifier - City or Town name", "CERTCITYTEXT", 2);
    public String getCERTCITYTEXT()
    {
        return Map_Get("CERTCITYTEXT", "CertifierAddress", "addressCity");
    }
    public void setCERTCITYTEXT(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Set("CERTCITYTEXT", "CertifierAddress", "addressCity", value);
            // // We've got city, and we probably also have state now - so attempt to find county while we're at it (IJE does NOT include this).
            // String county = dataLookup.StateCodeAndCityNameToCountyName(CERTSTATECD, value);
            // if (!isNullOrWhiteSpace(county))
            // {
            //     Map_Geo_Set("CERTCITYTEXT", "CertifierAddress", "address", "county", false, county);
            //     // If we found a county, we know the country.
            //     Map_Geo_Set("CERTCITYTEXT", "CertifierAddress", "address", "country", false, "US");
            // }
        }
    }

    /// <summary>State, U.S. Territory or Canadian Province of Certifier - code</summary>
    IJEField metaCERTSTATECD = new IJEField(232, 4215, 2, "State, U.S. Territory or Canadian Province of Certifier - code", "CERTSTATECD", 1);
    public String getCERTSTATECD()
    {
        return dataLookup.StateNameToStateCode(Map_Get_Full("CERTSTATECD", "CertifierAddress", "addressState"));
    }
    public void setCERTSTATECD(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Set("CERTSTATECD", "CertifierAddress", "addressState", value);
        }
    }

    /// <summary>State, U.S. Territory or Canadian Province of Certifier - literal</summary>
    IJEField metaCERTSTATE = new IJEField(233, 4217, 28, "State, U.S. Territory or Canadian Province of Certifier - literal", "CERTSTATE", 1);
    public String getCERTSTATE()
    {
        String stateCode = Map_Get("CERTSTATE", "CertifierAddress", "addressState");
        //                var mortalityData = MortalityData.Instance;
        String certstate = dataLookup.StateCodeToStateName(stateCode);
        if (certstate == null)
        {
            certstate = " ";
        }
        return StringUtils.rightPad(Truncate(certstate, 28), 28, " ");
    }
    public void setCERTSTATE(String value)
    {
        // NOOP
    }

    /// <summary>Certifier - Zip</summary>
    IJEField metaCERTZIP = new IJEField(234, 4245, 9, "Certifier - Zip", "CERTZIP", 1);
    public String getCERTZIP()
    {
        return Map_Get("CERTZIP", "CertifierAddress", "addressZip");
    }
    public void setCERTZIP(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Set("CERTZIP", "CertifierAddress", "addressZip", value);
        }
    }

    /// <summary>Certifier Date Signed</summary>
    IJEField metaCERTDATE = new IJEField(235, 4254, 8, "Certifier Date Signed", "CERTDATE", 1);
    public String getCERTDATE()
    {
        return DateTime_Get("CERTDATE", "MMddyyyy", "CertifiedTime");
    }
    public void setCERTDATE(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            DateTime_Set("CERTDATE", "MMddyyyy", "CertifiedTime", value, true, false);
        }
    }

    /// <summary>Date Filed</summary>
    IJEField metaFILEDATE = new IJEField(236, 4262, 8, "Date Filed", "FILEDATE", 1);
    public String getFILEDATE()
    {
        // NOTE: This is a placeholder, the IJE field FILEDATE is not currently implemented in FHIR
        return "";
    }
    public void setFILEDATE(String value)
    {
        // NOTE: This is a placeholder, the IJE field FILEDATE is not currently implemented in FHIR
    }

    /// <summary>State, U.S. Territory or Canadian Province of Injury - literal</summary>
    IJEField metaSTINJURY = new IJEField(237, 4270, 28, "State, U.S. Territory or Canadian Province of Injury - literal", "STINJURY", 1);
    public String getSTINJURY()
    {
        String stateCode = Map_Geo_Get("STATECODE_I", "InjuryLocationAddress", "address", "state", false);
        //                var mortalityData = MortalityData.Instance;
        String stinjury = dataLookup.StateCodeToStateName(stateCode);
        if (stinjury == null)
        {
            stinjury = " ";
        }
        return StringUtils.rightPad(Truncate(stinjury, 28), 28, " ");
    }
    public void setSTINJURY(String value)
    {
        // NOOP
    }

    /// <summary>State, U.S. Territory or Canadian Province of Birth - literal</summary>
    IJEField metaSTATEBTH = new IJEField(238, 4298, 28, "State, U.S. Territory or Canadian Province of Birth - literal", "STATEBTH", 1);
    public String getSTATEBTH()
    {
        String stateCode = Map_Geo_Get("BPLACE_ST", "PlaceOfBirth", "address", "state", false);
        //                var mortalityData = MortalityData.Instance;
        String statebth = dataLookup.StateCodeToStateName(stateCode);
        if (statebth == null)
        {
            statebth = " ";
        }
        return StringUtils.rightPad(Truncate(statebth, 28), 28, " ");
    }
    public void setSTATEBTH(String value)
    {
        // NOOP, field does not exist in FHIR
    }

    /// <summary>Country of Death - Code</summary>
    IJEField metaDTHCOUNTRYCD = new IJEField(239, 4326, 2, "Country of Death - Code", "DTHCOUNTRYCD", 1);
    public String getDTHCOUNTRYCD()
    {
        return Map_Geo_Get("DTHCOUNTRYCD", "DeathLocationAddress", "address", "country", true);
    }
    public void setDTHCOUNTRYCD(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("DTHCOUNTRYCD", "DeathLocationAddress", "address", "country", true, value);
        }
    }

    /// <summary>Country of Death - Literal</summary>
    IJEField metaDTHCOUNTRY = new IJEField(240, 4328, 28, "Country of Death - Literal", "DTHCOUNTRY", 1);
    public String getDTHCOUNTRY()
    {
        String countryCode = Map_Geo_Get("DTHCOUNTRYCD", "Residence", "address", "country", false);
        //                var mortalityData = MortalityData.Instance;
        String dthcountry = dataLookup.CountryCodeToCountryName(countryCode);
        if (dthcountry == null)
        {
            dthcountry = " ";
        }
        return StringUtils.rightPad(Truncate(dthcountry, 28), 28, " ");
    }
    public void setDTHCOUNTRY(String value)
    {
        // NOOP
    }

    /// <summary>SSA State Source of Death</summary>
    IJEField metaSSADTHCODE = new IJEField(241, 4356, 3, "SSA State Source of Death", "SSADTHCODE", 1);
    public String getSSADTHCODE()
    {
        // NOTE: This is a placeholder, the IJE field SSADTHCODE is not currently implemented in FHIR
        return "";
    }
    public void setSSADTHCODE(String value)
    {
        // NOTE: This is a placeholder, the IJE field SSADTHCODE is not currently implemented in FHIR
    }

    /// <summary>SSA Foreign Country Indicator</summary>
    IJEField metaSSAFOREIGN = new IJEField(242, 4359, 1, "SSA Foreign Country Indicator", "SSAFOREIGN", 1);
    public String getSSAFOREIGN()
    {
        // NOTE: This is a placeholder, the IJE field SSAFOREIGN is not currently implemented in FHIR
        return "";
    }
    public void setSSAFOREIGN(String value)
    {
        // NOTE: This is a placeholder, the IJE field SSAFOREIGN is not currently implemented in FHIR
    }

    /// <summary>SSA EDR Verify Code</summary>
    IJEField metaSSAVERIFY = new IJEField(243, 4360, 1, "SSA EDR Verify Code", "SSAVERIFY", 1);
    public String getSSAVERIFY()
    {
        // NOTE: This is a placeholder, the IJE field SSAVERIFY is not currently implemented in FHIR
        return "";
    }
    public void setSSAVERIFY(String value)
    {
        // NOTE: This is a placeholder, the IJE field SSAVERIFY is not currently implemented in FHIR
    }

    /// <summary>SSA Date of SSN Verification</summary>
    IJEField metaSSADATEVER = new IJEField(244, 4361, 8, "SSA Date of SSN Verification", "SSADATEVER", 1);
    public String getSSADATEVER()
    {
        // NOTE: This is a placeholder, the IJE field SSADATEVER is not currently implemented in FHIR
        return "";
    }
    public void setSSADATEVER(String value)
    {
        // NOTE: This is a placeholder, the IJE field SSADATEVER is not currently implemented in FHIR
    }

    /// <summary>SSA Date of State Transmission</summary>
    IJEField metaSSADATETRANS = new IJEField(245, 4369, 8, "SSA Date of State Transmission", "SSADATETRANS", 1);
    public String getSSADATETRANS()
    {
        // NOTE: This is a placeholder, the IJE field SSADATETRANS is not currently implemented in FHIR
        return "";
    }
    public void setSSADATETRANS(String value)
    {
        // NOTE: This is a placeholder, the IJE field SSADATETRANS is not currently implemented in FHIR
    }

    /// <summary>Hispanic Code for Literal</summary>
    IJEField metaDETHNIC5C = new IJEField(247, 4427, 3, "Hispanic Code for Literal", "DETHNIC5C", 1);
    public String getDETHNIC5C()
    {
        return Get_MappingFHIRToIJE(Mappings.HispanicOrigin.FHIRToIJE, "HispanicCodeForLiteral", "DETHNIC5C");
    }
    public void setDETHNIC5C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.HispanicOrigin.IJEToFHIR, "DETHNIC5C", "HispanicCodeForLiteral", value);
    }

    /// <summary>Blank for One-Byte Field 1</summary>
    IJEField metaPLACE1_1 = new IJEField(248, 4430, 1, "Blank for One-Byte Field 1", "PLACE1_1", 1);
    public String getPLACE1_1()
    {
        return LeftJustified_Get("PLACE1_1", "EmergingIssue1_1");
    }
    public void setPLACE1_1(String value)
    {
        LeftJustified_Set("PLACE1_1", "EmergingIssue1_1", value);
    }

    /// <summary>Blank for One-Byte Field 2</summary>
    IJEField metaPLACE1_2 = new IJEField(249, 4431, 1, "Blank for One-Byte Field 2", "PLACE1_2", 1);
    public String getPLACE1_2()
    {
        return LeftJustified_Get("PLACE1_2", "EmergingIssue1_2");
    }
    public void setPLACE1_2(String value)
    {
        LeftJustified_Set("PLACE1_2", "EmergingIssue1_2", value);
    }

    /// <summary>Blank for One-Byte Field 3</summary>
    IJEField metaPLACE1_3 = new IJEField(250, 4432, 1, "Blank for One-Byte Field 3", "PLACE1_3", 1);
    public String getPLACE1_3()
    {
        return LeftJustified_Get("PLACE1_3", "EmergingIssue1_3");
    }
    public void setPLACE1_3(String value)
    {
        LeftJustified_Set("PLACE1_3", "EmergingIssue1_3", value);
    }

    /// <summary>Blank for One-Byte Field 4</summary>
    IJEField metaPLACE1_4 = new IJEField(251, 4433, 1, "Blank for One-Byte Field 4", "PLACE1_4", 1);
    public String getPLACE1_4()
    {
        return LeftJustified_Get("PLACE1_4", "EmergingIssue1_4");
    }
    public void setPLACE1_4(String value)
    {
        LeftJustified_Set("PLACE1_4", "EmergingIssue1_4", value);
    }

    /// <summary>Blank for One-Byte Field 5</summary>
    IJEField metaPLACE1_5 = new IJEField(252, 4434, 1, "Blank for One-Byte Field 5", "PLACE1_5", 1);
    public String getPLACE1_5()
    {
        return LeftJustified_Get("PLACE1_5", "EmergingIssue1_5");
    }
    public void setPLACE1_5(String value)
    {
        LeftJustified_Set("PLACE1_5", "EmergingIssue1_5", value);
    }

    /// <summary>Blank for One-Byte Field 6</summary>
    IJEField metaPLACE1_6 = new IJEField(253, 4435, 1, "Blank for One-Byte Field 6", "PLACE1_6", 1);
    public String getPLACE1_6()
    {
        return LeftJustified_Get("PLACE1_6", "EmergingIssue1_6");
    }
    public void setgetPLACE1_6(String value)
    {
        LeftJustified_Set("PLACE1_6", "EmergingIssue1_6", value);
    }

    /// <summary>Blank for Eight-Byte Field 1</summary>
    IJEField metaPLACE8_1 = new IJEField(254, 4436, 8, "Blank for Eight-Byte Field 1", "PLACE8_1", 1);
    public String getPLACE8_1()
    {
        return LeftJustified_Get("PLACE8_1", "EmergingIssue8_1");
    }
    public void setPLACE8_1(String value)
    {
        LeftJustified_Set("PLACE8_1", "EmergingIssue8_1", value);
    }

    /// <summary>Blank for Eight-Byte Field 2</summary>
    IJEField metaPLACE8_2 = new IJEField(255, 4444, 8, "Blank for Eight-Byte Field 2", "PLACE8_2", 1);
    public String getPLACE8_2()
    {
        return LeftJustified_Get("PLACE8_2", "EmergingIssue8_2");
    }
    public void setPLACE8_2(String value)
    {
        LeftJustified_Set("PLACE8_2", "EmergingIssue8_2", value);
    }

    /// <summary>Blank for Eight-Byte Field 3</summary>
    IJEField metaPLACE8_3 = new IJEField(256, 4452, 8, "Blank for Eight-Byte Field 3", "PLACE8_3", 1);
    public String getPLACE8_3()
    {
        return LeftJustified_Get("PLACE8_3", "EmergingIssue8_3");
    }
    public void setPLACE8_3(String value)
    {
        LeftJustified_Set("PLACE8_3", "EmergingIssue8_3", value);
    }

    /// <summary>Blank for Twenty-Byte Field</summary>
    IJEField metaPLACE20 = new IJEField(257, 4460, 20, "Blank for Twenty-Byte Field", "PLACE20", 1);
    public String getPLACE20()
    {
        return LeftJustified_Get("PLACE20", "EmergingIssue20");
    }
    public void setPLACE20(String value)
    {
        LeftJustified_Set("PLACE20", "EmergingIssue20", value);
    }

    /// <summary>Record Axis Cause Of Death</summary>
    /// <value>record-axis codes</value>
    /// <example>
    /// <para>// Setter:</para>
    /// <para>Tuple&lt;string, string, string&gt;[] eac = new Tuple&lt;string, string, string&gt;{Tuple.Create("position", "code", "pregnancy")}</para>
    /// <para>ExampleDeathRecord.RecordAxisCauseOfDeath = new [] { (Position: 1, Code: "T27.3", Pregnancy: true) };</para>
    /// <para>// Getter:</para>
    /// <para>Console.WriteLine($"First Record Axis Code: {ExampleDeathRecord.RecordAxisCauseOfDeath.ElememtAt(0).Code}");</para>
    /// </example>
//  [Property("Record Axis Cause Of Death", Property.Types.Tuple4Arr, "Coded Content", "", true, IGURL.RecordAxisCauseOfDeath, false, 50)]
//  [FHIRPath("Bundle.entry.resource.where($this is Observation).where(code.coding.code=80357-7)", "")]
    private Iterable RecordAxisCauseOfDeath;
    //public Iterable<(int Position, string Code, bool Pregnancy)> getRecordAxisCauseOfDeath()
    public Iterable getRecordAxisCauseOfDeath()
    {
        List<(int Position, string Code, bool Pregnancy)> rac = new List<(int Position, string Code, bool Pregnancy)>();
        if (DeathCertificateDocument.getRecordAxisCauseOfDeathObsList() != null)
        {
        for(Observation ob:RecordAxisCauseOfDeathObsList)
        {
        Integer position = null;
        String icd10code = null;
        boolean pregnancy = false;
        Observation.ObservationComponentComponent positionComp = ob.getComponent().stream().filter(c -> c.getCode().equals("position")).findFirst().get();
        if (positionComp != null && positionComp.getValue() != null)
        {
        position = ((IntegerType)positionComp.getValue()).getValue();
        }
        CodeableConcept valueCC = (CodeableConcept)ob.getValue();
        if (valueCC != null && valueCC.getCoding() != null && valueCC.getCoding().size() > 0)
        {
        icd10code = valueCC.getCoding().get(0).getCode();
        }

        Observation.ObservationComponentComponent pregComp = ob.getComponent().stream().filter(c -> c.getCode().getCoding().get(0).getCode().equals("wouldBeUnderlyingCauseOfDeathWithoutPregnancy")).findFirst().get();
        if (pregComp != null && pregComp.getValue() != null)
        {
        pregnancy = (boolean)((BooleanType)pregComp.getValue()).getValue();
        }
        if (position != null && icd10code != null)
        {
        rac.add((Position: (int)position, Code: icd10code, Pregnancy: pregnancy));
        }
        }
        }
        return rac.OrderBy(entry -> entry.Position);
    }
//        public vois setRecordAxisCauseOfDeath()
//        {
//        // clear all existing eac
//        Bundle.Entry.RemoveAll(entry -> entry.Resource is Observation && (((Observation)entry.Resource).Code.Coding.First().Code == "80357-7"));
//        if (RecordAxisCauseOfDeathObsList != null)
//        {
//        RecordAxisCauseOfDeathObsList.Clear();
//        }
//        else
//        {
//        RecordAxisCauseOfDeathObsList = new List<Observation>();
//        }
//        // Rebuild the list of observations
//        foreach ((int Position, string Code, bool Pregnancy) rac in value)
//        {
//        if(!String.IsNullOrEmpty(rac.Code))
//        {
//        Observation ob = new Observation();
//        ob.Id = Guid.NewGuid().ToString();
//        ob.Meta = new Meta();
//        string[] recordAxis_profile = { ProfileURL.RecordAxisCauseOfDeath };
//        ob.Meta.Profile = recordAxis_profile;
//        ob.Status = ObservationStatus.Final;
//        ob.Code = new CodeableConcept(CodeSystems.LOINC, "80357-7", "Cause of death record axis code [Automated]", null);
//        ob.Subject = new ResourceReference("urn:uuid:" + Decedent.Id);
//        AddReferenceToComposition(ob.Id, "CodedContent");
//        ob.Effective = new FhirDateTime();
//        ob.Value = new CodeableConcept(CodeSystems.ICD10, rac.Code, null, null);
//        Observation.ComponentComponent positionComp = new Observation.ComponentComponent();
//        positionComp.Value = new Integer(rac.Position);
//        positionComp.Code = new CodeableConcept(CodeSystems.Component, "position", "Position", null);
//        ob.Component.Add(positionComp);
//
//        // Record axis codes have an unusual and obscure handling of a Pregnancy flag, for more information see
//        // http://build.fhir.org/ig/HL7/vrdr/branches/master/StructureDefinition-vrdr-record-axis-cause-of-death.html#usage
//        if (rac.Pregnancy)
//        {
//        Observation.ComponentComponent pregComp = new Observation.ComponentComponent();
//        pregComp.Value = new FhirBoolean(true);
//        pregComp.Code = new CodeableConcept(CodeSystems.Component, "wouldBeUnderlyingCauseOfDeathWithoutPregnancy", "Would be underlying cause of death without pregnancy, if true");
//        ob.Component.Add(pregComp);
//        }
//
//        Bundle.AddResourceEntry(ob, "urn:uuid:" + ob.Id);
//        RecordAxisCauseOfDeathObsList.Add(ob);
//        }
//        }
//        }
//        }

}




