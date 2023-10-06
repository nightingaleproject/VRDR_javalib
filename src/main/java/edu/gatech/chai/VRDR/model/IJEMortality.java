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
import java.util.stream.Collectors;
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

    //public static List<IJEField> ijeFields = new ArrayList<>();

    public static void setIJEFields(List metaIJEFields)
    {
        metaIJEFields = metaIJEFields;
    }

    public static List<MetaIJEField> getIJEFields()
    {
        return metaIJEFields;
    }

    public static void addMetaIJEField(MetaIJEField metaIJEField)
    {
        metaIJEFields.add(metaIJEField);
    }

    public static class MetaIJEField
    {
        MetaIJEField(int Field, int Location, int Length, String Contents, String Name, int Priority){};
        int Field;
        int Location;

        public int getField()
        {
            return Field;
        }

        public void setField(int field) { Field = field;}

        public int getLocation()
        {
            return Location;
        }

        public void setLocation(int location)
        {
            Location = location;
        }

        public int getLength()
        {
            return Length;
        }

        public void setLength(int length)
        {
            Length = length;
        }

        public String getContents() { return Contents; }

        public void setContents(String Contents)
        {
            Contents = Contents;
        }

        public String getName()
        {
            return Name;
        }

        public void setName(String name)
        {
            Name = name;
        }

        public int getPriority()
        {
            return Priority;
        }

        public void setPriority(int priority)
        {
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
                record.setCoderStatus(value);
            }
        }
        /// <summary>shipment number - Property for setting the ShipmentNumber of a Cause of Death Coding Submission</summary>
        public String getSHIP()
        {
            return record.getShipmentNumber();
        }
        public void setSHIP(String value)
        {
            record.setShipmentNumber(value);
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
    private MortalityData dataLookup;

    {
        try {
            dataLookup = MortalityData.class.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

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
                try
                {
                    throw new Exception(errorString);
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /// <summary>Constructor that takes an IJE String and builds a corresponding internal <c>DeathRecord</c>.</summary>
//    public IJEMortality(String ije, boolean validate)// = true) : this()
//    {
//        if (ije == null)
//        {
//            throw new IllegalArgumentException("IJE String cannot be null.");
//        }
//        if (ije.length() < 5000)
//        {
//            ije = StringUtils.rightPad(ije, 5000, " ");
//        }
//        // Loop over every property (these are the fields); Order by priority
//        //List<PropertyInfo> properties = typeof(IJEMortality).GetProperties().ToList().OrderBy(p -> p.GetCustomAttribute<MetaIJEField>().Priority).ToList();
//        List<Field> fields = Arrays.stream(this.getClass().getDeclaredFields()).sorted(Comparator.comparingInt(f -> f.getAnnotation(MetaIJEField.class).priority())).collect(Collectors.toList());
//
//        for(Field field:fields)
//        {
//            // Grab the field attributes
//            //MetaIJEField info = field.GetCustomAttribute<MetaIJEField>();
//            MetaIJEField info = field.getAnnotation(MetaIJEField.class);
//            // Grab the field value
//            //String field = ije.substring(info.getLocation() - 1, info.getLength());
//            String fieldValue = ije.substring(info.getLocation() - 1, info.getLength());
//            // Set the value on this IJEMortality (and the embedded record)
//            //field.set(this, field);
//            field.setAccessible(true);
//            try {
//                field.set(this, fieldValue);
//            } catch (IllegalAccessException e) {
//                validationErrors.add(e.getMessage());
//            }
//        }
//        if (validate && validationErrors.size() > 0)
//        {
//            String errorString = new StringBuffer().append(validationErrors.size()).append(" validation errors:\n").append( validationErrors).toString();
//            try
//            {
//                throw new Exception(errorString);
//            } catch (Exception e)
//            {
//                throw new RuntimeException(e);
//            }
//        }
//    }

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
        ///List<Field> ijeFields = List.of(IJEMortality.class.getDeclaredFields());
        List<MetaIJEField> sortedMetaIJEFields = metaIJEFields.stream().sorted(Comparator.comparingInt(f -> f.getPriority())).collect(Collectors.toList());

        for(MetaIJEField metaIJEField:sortedMetaIJEFields)
        {
            // Grab the field value
            String fieldValue = ije.substring(metaIJEField.getLocation() - 1, metaIJEField.getLength());
            // Set the value on this IJEMortality (and the embedded record)
            Field ijeField = null;
            try {
                ijeField = IJEMortality.class.getField(mapIJEFields.get(metaIJEField.Name));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            ijeField.setAccessible(true);
            try {
                ijeField.set(this, fieldValue);
            } catch (IllegalAccessException e) {
                validationErrors.add(e.getMessage());
            }
        }
        if (validate && validationErrors.size() > 0)
        {
            String errorString = new StringBuffer().append(validationErrors.size()).append(" validation errors:\n").append( validationErrors).toString();
            try
            {
                throw new Exception(errorString);
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /// <summary>Constructor that creates an empty record for constructing records using the IJE properties.</summary>
    public IJEMortality()
    {
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
        List<MetaIJEField> sortedMetaIJEFields = metaIJEFields.stream().sorted(Comparator.comparingInt(f -> f.getPriority())).collect(Collectors.toList());

        for(MetaIJEField metaIJEField:sortedMetaIJEFields)
        {
            // Grab the field value
            String fieldValue = null;//, null).toString();
            Field ijeField = null;
            try
            {
                ijeField = IJEMortality.class.getField(metaIJEField.Name);
                fieldValue = ijeField.get(this).toString();
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
            // Grab the field attributes
            //MetaIJEField info = field.GetCustomAttribute<MetaIJEField>();
            // Be mindful about lengths
            if (fieldValue.length() > metaIJEField.getLength())
            {
                fieldValue = fieldValue.substring(0, metaIJEField.getLength());
            }
            // Insert the field value into the record
            ije.delete(metaIJEField.getLocation() - 1, fieldValue.length());
            ije.insert(metaIJEField.getLocation() - 1, fieldValue);
        }
        return ije.toString();
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
    private MetaIJEField FieldInfo(String ijeFieldName)
    {
        //return typeof(IJEMortality).getField(ijeFieldName).GetCustomAttribute<MetaIJEField>();
        Method fieldGetter = null;
        try
        {
            fieldGetter = MetaIJEField.class.getMethod("getmeta"+ijeFieldName);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
        try
        {
            return (MetaIJEField)fieldGetter.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

    /// <summary>Helps decompose a DateTime into individual parts (year, month, day, time).</summary>
    private String DateTimeStringHelper(MetaIJEField info, String value, String type, OffsetDateTime date, boolean dateOnly, boolean withTimezoneOffset)
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
        MetaIJEField info = FieldInfo(ijeFieldName);

        String current = null;
        try
        {
            current = this.record == null ? null : DeathCertificateDocument.class.getField(fhirFieldName).get(this.record).toString();
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
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
        MetaIJEField info = FieldInfo(ijeFieldName);
        String current = null;
        try
        {
            current = DeathCertificateDocument.class.getField(fhirFieldName).get(this.record).toString();
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
        OffsetDateTime date = OffsetDateTime.parse(current);
        if (current != null && date != null)
        {
            // date = date.ToUniversalTime();
            // date = new OffsetDateTime(date.Year, date.Month, date.Day, date.Hour, date.Minute, date.Second, date.Millisecond, TimeSpan.Zero);
            LocalDateTime localDateTime = LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getMonthValue(), date.getHour(), date.getMinute(), date.getSecond());
            date = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
            try
            {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, DateTimeStringHelper(info, value, dateTimeType, date, dateOnly, withTimezoneOffset));
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            LocalDateTime localDateTime = LocalDateTime.of(1, 1, 1, 0, 0, 0, 0);
            date = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);// TimeSpan.Zero);
            try
            {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, DateTimeStringHelper(info, value, dateTimeType, date, dateOnly, withTimezoneOffset));
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /// <summary>Get a value on the DeathCertificateDocument that is a numeric String with the option of being set to all 9s on the IJE side and -1 on the
    /// FHIR side to represent'unknown' and blank on the IJE side and null on the FHIR side to represent unspecified</summary>
    private String NumericAllowingUnknown_Get(String ijeFieldName, String fhirFieldName)
    {
        MetaIJEField info = FieldInfo(ijeFieldName);
        Integer value = null;
        try
        {
            value = (Integer) DeathCertificateDocument.class.getField(fhirFieldName).get(this.record);
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
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
        MetaIJEField info = FieldInfo(ijeFieldName);
        if (value.equals(StringUtils.repeat(" ", info.getLength())))
        {
            try
            {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, null);
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
        else if (value.equals(StringUtils.repeat("9", info.getLength())))
        {
            try
            {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, -1);
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            try
            {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, Integer.parseInt(value));
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /// <summary>Get a value on the DeathCertificateDocument that is a time with the option of being set to all 9s on the IJE side and null on the FHIR side to represent null</summary>
    private String TimeAllowingUnknown_Get(String ijeFieldName, String fhirFieldName)
    {
        MetaIJEField info = FieldInfo(ijeFieldName);
        String timeString = null;
        try
        {
            timeString = (String) DeathCertificateDocument.class.getField(fhirFieldName).get(this.record);
        }
        catch (NoSuchFieldException e)
        {
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
        MetaIJEField info = FieldInfo(ijeFieldName);
        if (value.equals(StringUtils.repeat(" ", info.getLength())))
        {
            try
            {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, null);
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
        else if (value.equals(StringUtils.repeat("9", info.getLength())))
        {
            try
            {
                DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, "-1");
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
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
                try
                {
                    DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, new StringBuffer(String.valueOf(map.get("hh"))).append(String.valueOf(map.get("mm"))).append(String.valueOf(map.get("ss"))));// timeSpan.toString()); //@"hh\:mm\:ss"));
                }
                catch (IllegalAccessException | NoSuchFieldException e)
                {
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
        MetaIJEField info = FieldInfo(ijeFieldName);
        String current = null;
        try
        {
            current = DeathCertificateDocument.class.getField(fhirFieldName).get(this.record).toString();
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
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
    private void RightJustifiedZeroed_Set(String ijeFieldName, String fhirFieldName, String value)
    {
        MetaIJEField info = FieldInfo(ijeFieldName);
        try
        {
            DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, value.replaceFirst("0", ""));//TrimStart('0'));
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    /// <summary>Get a value on the DeathCertificateDocument whose IJE type is a left justified String.</summary>
    private String LeftJustified_Get(String ijeFieldName, String fhirFieldName)
    {
        MetaIJEField info = FieldInfo(ijeFieldName);
        String current = null;
        try
        {
            current = DeathCertificateDocument.class.getField(fhirFieldName).get(this.record).toString();
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
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
            MetaIJEField info = FieldInfo(ijeFieldName);
            //Method method = Arrays.stream(DeathCertificateDocument.class.getMethods()).filter(m->m.getName().equals("set"+StringUtils.capitalize(ijeFieldName))).findFirst().get();//getField(fhirFieldName).(this.record, value.trim());
            try
            {
                Field field = DeathCertificateDocument.class.getField(ijeFieldName);
                field.setAccessible(true);
                field.set(this.record, value.trim());
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /// <summary>Get a value on the DeathCertificateDocument whose property is a Map type.</summary>
    private String Map_Get(String ijeFieldName, String fhirFieldName, String key)
    {
        MetaIJEField info = FieldInfo(ijeFieldName);
        Map<String, String> map = null;
        try
        {
            map = (Map<String, String>) DeathCertificateDocument.class.getField(fhirFieldName).get(this.record);
        }
        catch (NoSuchFieldException e)
        {
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
        MetaIJEField info = FieldInfo(ijeFieldName);
        Map<String, String> map = null;
        try
        {
            map = (Map<String, String>) DeathCertificateDocument.class.getField(fhirFieldName).get(this.record);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
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
        MetaIJEField info = FieldInfo(ijeFieldName);
        Map<String, String> map = null;
        try
        {
            map = (Map<String, String>) DeathCertificateDocument.class.getField(fhirFieldName).get(this.record);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
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

        try
        {
            DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, map);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    /// <summary>Get a value on the DeathCertificateDocument whose property is a geographic type (and is contained in a map).</summary>
    private String Map_Geo_Get(String ijeFieldName, String fhirFieldName, String keyPrefix, String geoType, boolean isCoded)
    {
        MetaIJEField info = FieldInfo(ijeFieldName);
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
        MetaIJEField info = FieldInfo(ijeFieldName);
        Map<String, String> map = null;
        try
        {
            map = (Map<String, String>) DeathCertificateDocument.class.getField(fhirFieldName).get(this.record);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
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
        try
        {
            DeathCertificateDocument.class.getField(fhirFieldName).set(this.record, map);
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    /// <summary>Checks if the given race exists in the record.</summary>
    private String Get_Race(String name)
    {
        Tuple[] raceStatus = record.getRace();
        Tuple raceTuple = Arrays.stream(raceStatus).filter(element -> element.children().get(0).equals(name)).findFirst().get();


        if (raceTuple != null)
        {
            return raceTuple.children().get(1).toString().trim();
        }
        return "";
    }

    /// <summary>Adds the given race to the record.</summary>
    private void Set_Race(String name, String value)
    {
        List<Tuple> raceStatus = Arrays.asList(record.getRace());
        //raceStatus.add(Tuple.Create(name, value));

        Tuple race = new Tuple();
        race.addChild(name).addChild(value);
        raceStatus.add(race);
        record.setRace((Tuple[]) raceStatus.toArray());
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
        try
        {
            helperProperty = DeathCertificateDocument.class.getField(fhirField + "Helper");
        }
        catch (NoSuchFieldException e)
        {
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
            catch (NoSuchElementException e)
            {
                validationErrors.add(new StringBuffer("Error: Unable to find FHIR ").append(fhirField).append(" mapping for IJE ").append(ijeField).append(" field value '").append(value).append("'").toString());
            } catch (NoSuchFieldException | IllegalAccessException e)
            {
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
    static MetaIJEField metaDOD_YR = new MetaIJEField(1, 1, 4, "Date of Death--Year", "DOD_YR", 1);


    private final static List<MetaIJEField> metaIJEFields = new ArrayList<>()
    {{
        add(metaDOD_YR);
        add(metaDSTATE);
        add(metaFILENO);
        add(metaVOID);
        add(metaAUXNO);
        add(metaMFILED);
        add(metaGNAME);
        add(metaMNAME);
        add(metaLNAME);
        add(metaSUFF);
        add(metaALIAS);
        add(metaFLNAME);
        add(metaSEX);
        add(metaSEX_BYPASS);
        add(metaSSN);
        add(metaAGETYPE);
        add(metaAGE);
        add(metaAGE_BYPASS);
        add(metaDOB_YR);
        add(metaDOB_MO);
        add(metaDOB_DY);
        add(metaBPLACE_CNT);
        add(metaBPLACE_ST);
        add(metaCOUNTYC);
        add(metaSTATEC);
        add(metaCOUNTRYC);
        add(metaLIMITS);
        add(metaMARITAL);
        add(metaMARITAL_BYPASS);
        add(metaDPLACE);
        add(metaCOD);
        add(metaDISP);
        add(metaDOD_MO);
        add(metaDOD_DY);
        add(metaTOD);
        add(metaDEDUC);
        add(metaDEDUC_BYPASS);
        add(metaDETHNIC2);
        add(metaDETHNIC3);
        add(metaDETHNIC4);
        add(metaDETHNIC5);
        add(metaRACE1);
        add(metaRACE2);
        add(metaRACE3);
        add(metaRACE4);
        add(metaRACE5);
        add(metaRACE6);
        add(metaRACE7);
        add(metaRACE8);
        add(metaRACE9);
        add(metaRACE10);
        add(metaRACE11);
        add(metaRACE12);
        add(metaRACE13);
        add(metaRACE14);
        add(metaRACE15);
        add(metaRACE16);
        add(metaRACE17);
        add(metaRACE18);
        add(metaRACE19);
        add(metaRACE20);
        add(metaRACE21);
        add(metaRACE22);
        add(metaRACE23);
        add(metaRACE1E);
        add(metaRACE2E);
        add(metaRACE3E);
        add(metaRACE4E);
        add(metaRACE5E);
        add(metaRACE6E);
        add(metaRACE7E);
        add(metaRACE8E);
        add(metaRACE16C);
        add(metaRACE17C);
        add(metaRACE18C);
        add(metaRACE19C);
        add(metaRACE20C);
        add(metaRACE21C);
        add(metaRACE22C);
        add(metaRACE23C);
        add(metaRACE_MVR);
        add(metaOCCUP);
        add(metaOCCUPC);
        add(metaINDUST);
        add(metaINDUSTC);
        add(metaBCNO);
        add(metaIDOB_YR);
        add(metaBSTATE);
        add(metaR_YR);
        add(metaR_MO);
        add(metaR_DY);
        add(metaOCCUPC4);
        add(metaINDUSTC);
        add(metaDOR_YR);
        add(metaDOR_MO);
        add(metaDOR_DY);
        add(metaFILLER2);
        add(metaMANNER);
        add(metaINT_REJ);
        add(metaSYS_REJ);
        add(metaINJPL);
        add(metaMAN_UC);
        add(metaACME_UC);
        add(metaEAC);
        add(metaTRX_FLG);
        add(metaRAC);
        add(metaAUTOP);
        add(metaAUTOPF);
        add(metaTOBAC);
        add(metaPREG);
        add(metaPREG_BYPASS);
        add(metaDOI_MO);
        add(metaDOI_DY);
        add(metaDOI_YR);
        add(metaTOI_HR);
        add(metaWORKINJ);
        add(metaCERTL);
        add(metaINACT);
        add(metaAUXNO2);
        add(metaSTATESP);
        add(metaSUR_MO);
        add(metaSUR_DY);
        add(metaSUR_YR);
        add(metaTOI_UNIT);
        add(metaBLANK1);
        add(metaARMEDF);
        add(metaDINSTI);
        add(metaADDRESS_D);
        add(metaSTNUM_D);
        add(metaPREDIR_D);
        add(metaSTNAME_D);
        add(metaSTDESIG_D);
        add(metaPOSTDIR_D);
        add(metaCITYTEXT_D);
        add(metaSTATETEXT_D);
        add(metaZIP9_D);
        add(metaCOUNTYTEXT_D);
        add(metaCITYCODE_D);
        add(metaLONG_D);
        add(metaLAT_D);
        add(metaSPOUSELV);
        add(metaSPOUSEF);
        add(metaSPOUSEL);
        add(metaCITYTEXT_R);
        add(metaZIP9_R);
        add(metaCOUNTYTEXT_R);
        add(metaSTATETEXT_R);
        add(metaCOUNTRYTEXT_R);
        add(metaADDRESS_R);
        add(metaRESSTATE);
        add(metaRESCON);
        add(metaSTNUM_R);
        add(metaPREDIR_R);
        add(metaSTNAME_R);
        add(metaPOSTDIR_R);
        add(metaUNITNUM_R);
        add(metaDETHNICE);
        add(metaNCHSBRIDGE);
        add(metaHISPOLDC);
        add(metaRACEOLDC);
        add(metaHISPSTSP);
        add(metaRACESTSP);
        add(metaDMIDDLE);
        add(metaDDADF);
        add(metaDDADMID);
        add(metaDMOMF);
        add(metaDMOMMID);
        add(metaDMOMMDN);
        add(metaREFERRED);
        add(metaPOILITRL);
        add(metaHOWINJ);
        add(metaTRANSPRT);
        add(metaCOUNTYTEXT_I);
        add(metaCOUNTYCODE_I);
        add(metaCITYTEXT_I);
        add(metaCITYCODE_I);
        add(metaSTATECODE_I);
        add(metaLONG_I );
        add(metaLAT_I);
        add(metaOLDEDUC);
        add(metaCOD1A);
        add(metaINTERVAL1A);
        add(metaCOD1B);
        add(metaINTERVAL1B);
        add(metaCOD1C);
        add(metaINTERVAL1C);
        add(metaCOD1D);
        add(metaINTERVAL1D);
        add(metaOTHERCONDITION);
        add(metaDMAIDEN);
        add(metaDBPLACECITYCODE);
        add(metaDBPLACECITY);
        add(metaINFORMRELATE);
        add(metaSPOUSEMIDNAME);
        add(metaSPOUSESUFFIX);
        add(metaFATHERSUFFIX);
        add(metaMOTHERSSUFFIX);
        add(metaDISPSTATECD);
        add(metaDISPSTATE);
        add(metaDISPCITYCODE);
        add(metaDISPCITY);
        add(metaFUNFACNAME);
        add(metaFUNFACSTNUM);
        add(metaFUNFACPREDIR);
        add(metaFUNFACSTRNAME);
        add(metaFUNFACSTRDESIG);
        add(metaFUNPOSTDIR);
        add(metaFUNUNITNUM);
        add(metaFUNFACADDRESS);
        add(metaFUNCITYTEXT);
        add(metaFUNSTATECD);
        add(metaFUNSTATE);
        add(metaFUNZIP);
        add(metaPPDATESIGNED);
        add(metaPPTIME);
        add(metaCERTFIRST);
        add(metaCERTMIDDLE);
        add(metaCERTLAST);
        add(metaCERTSUFFIX);
        add(metaCERTSTNUM);
        add(metaCERTPREDIR);
        add(metaCERTSTRNAME);
        add(metaCERTSTRDESIG);
        add(metaCERTPOSTDIR);
        add(metaCERTUNITNUM);
        add(metaCERTADDRESS);
        add(metaCERTCITYTEXT);
        add(metaCERTSTATECD);
        add(metaCERTSTATE);
        add(metaCERTZIP);
        add(metaCERTDATE);
        add(metaFILEDATE);
        add(metaSTINJURY);
        add(metaSTATEBTH);
        add(metaDTHCOUNTRYCD);
        add(metaDTHCOUNTRY);
        add(metaSSADTHCODE);
        add(metaSSAFOREIGN);
        add(metaSSAVERIFY);
        add(metaSSADATEVER);
        add(metaSSADATETRANS);
        add(metaDETHNIC5C);
        add(metaPLACE1_1);
        add(metaPLACE1_2);
        add(metaPLACE1_3);
        add(metaPLACE1_4);
        add(metaPLACE1_5);
        add(metaPLACE1_6);
        add(metaPLACE8_1);
        add(metaPLACE8_2);
        add(metaPLACE8_3);
        add(metaPLACE20);
    }};

    private final Map<String, String> mapIJEFields = new HashMap<>()
    {{
        put(metaDOD_YR.Name, getDOD_YR());
        put(metaDSTATE.Name, getDSTATE());
        put(metaFILENO.Name, getFILENO());
        put(metaVOID.Name, getVOID());
        put(metaAUXNO.Name, getAUXNO());
        put(metaMFILED.Name, getMFILED());
        put(metaGNAME.Name, getGNAME());
        put(metaMNAME.Name, getMNAME());
        put(metaLNAME.Name, getLNAME());
        put(metaSUFF.Name, getSUFF());
        put(metaALIAS.Name, getALIAS());
        put(metaFLNAME.Name, getFLNAME());
        put(metaSEX.Name, getSEX());
        put(metaSEX_BYPASS.Name, getSEX_BYPASS());
        put(metaSSN.Name, getSSN());
        put(metaAGETYPE.Name, getAGETYPE());
        put(metaAGE.Name, getAGE());
        put(metaAGE_BYPASS.Name, getAGE_BYPASS());
        put(metaDOB_YR.Name, getDOB_YR());
        put(metaDOB_MO.Name, getDOB_MO());
        put(metaDOB_DY.Name, getDOB_DY());
        put(metaBPLACE_CNT.Name, getBPLACE_CNT());
        put(metaBPLACE_ST.Name, getBPLACE_ST());
        put(metaCOUNTYC.Name, getCOUNTYC());
        put(metaSTATEC.Name, getSTATEC());
        put(metaCOUNTRYC.Name, getCOUNTRYC());
        put(metaLIMITS.Name, getLIMITS());
        put(metaMARITAL.Name, getMARITAL());
        put(metaMARITAL_BYPASS.Name, getMARITAL_BYPASS());
        put(metaDPLACE.Name, getDPLACE());
        put(metaCOD.Name, getCOD());
        put(metaDISP.Name, getDISP());
        put(metaDOD_MO.Name, getDOD_MO());
        put(metaDOD_DY.Name, getDOD_DY());
        put(metaTOD.Name, getTOD());
        put(metaDEDUC.Name, getDEDUC());
        put(metaDEDUC_BYPASS.Name, getDEDUC_BYPASS());
        put(metaDETHNIC2.Name, getDETHNIC2());
        put(metaDETHNIC3.Name, getDETHNIC3());
        put(metaDETHNIC4.Name, getDETHNIC4());
        put(metaDETHNIC5.Name, getDETHNIC5());
        put(metaRACE1.Name, getRACE1());
        put(metaRACE2.Name, getRACE2());
        put(metaRACE3.Name, getRACE3());
        put(metaRACE4.Name, getRACE4());
        put(metaRACE5.Name, getRACE5());
        put(metaRACE6.Name, getRACE6());
        put(metaRACE7.Name, getRACE7());
        put(metaRACE8.Name, getRACE8());
        put(metaRACE9.Name, getRACE9());
        put(metaRACE10.Name, getRACE10());
        put(metaRACE11.Name, getRACE11());
        put(metaRACE12.Name, getRACE12());
        put(metaRACE13.Name, getRACE13());
        put(metaRACE14.Name, getRACE14());
        put(metaRACE15.Name, getRACE15());
        put(metaRACE16.Name, getRACE16());
        put(metaRACE17.Name, getRACE17());
        put(metaRACE18.Name, getRACE18());
        put(metaRACE19.Name, getRACE19());
        put(metaRACE20.Name, getRACE20());
        put(metaRACE21.Name, getRACE21());
        put(metaRACE22.Name, getRACE22());
        put(metaRACE23.Name, getRACE23());
        put(metaRACE1E.Name, getRACE1E());
        put(metaRACE2E.Name, getRACE2E());
        put(metaRACE3E.Name, getRACE3E());
        put(metaRACE4E.Name, getRACE4E());
        put(metaRACE5E.Name, getRACE5E());
        put(metaRACE6E.Name, getRACE6E());
        put(metaRACE7E.Name, getRACE7E());
        put(metaRACE8E.Name, getRACE8E());
        put(metaRACE16C.Name, getRACE16C());
        put(metaRACE17C.Name, getRACE17C());
        put(metaRACE18C.Name, getRACE18C());
        put(metaRACE19C.Name, getRACE19C());
        put(metaRACE20C.Name, getRACE20C());
        put(metaRACE21C.Name, getRACE21C());
        put(metaRACE22C.Name, getRACE22C());
        put(metaRACE23C.Name, getRACE23C());
        put(metaRACE_MVR.Name, getRACE_MVR());
        put(metaOCCUP.Name, getOCCUP());
        put(metaOCCUPC.Name, getOCCUPC());
        put(metaINDUST.Name, getINDUST());
        put(metaINDUSTC.Name, getINDUSTC());
        put(metaBCNO.Name, getBCNO());
        put(metaIDOB_YR.Name, getIDOB_YR());
        put(metaBSTATE.Name, getBSTATE());
        put(metaR_YR.Name, getR_YR());
        put(metaR_MO.Name, getR_MO());
        put(metaR_DY.Name, getR_DY());
        put(metaOCCUPC4.Name, getOCCUPC4());
        put(metaINDUSTC.Name, getINDUSTC());
        put(metaDOR_YR.Name, getDOR_YR());
        put(metaDOR_MO.Name, getDOR_MO());
        put(metaDOR_DY.Name, getDOR_DY());
        put(metaFILLER2.Name, getFILLER2());
        put(metaMANNER.Name, getMANNER());
        put(metaINT_REJ.Name, getINT_REJ());
        put(metaSYS_REJ.Name, getSYS_REJ());
        put(metaINJPL.Name, getINJPL());
        put(metaMAN_UC.Name, getMAN_UC());
        put(metaACME_UC.Name, getACME_UC());
        put(metaEAC.Name, getEAC());
        put(metaTRX_FLG.Name, getTRX_FLG());
        put(metaRAC.Name, getRAC());
        put(metaAUTOP.Name, getAUTOP());
        put(metaAUTOPF.Name, getAUTOPF());
        put(metaTOBAC.Name, getTOBAC());
        put(metaPREG.Name, getPREG());
        put(metaPREG_BYPASS.Name, getPREG_BYPASS());
        put(metaDOI_MO.Name, getDOI_MO());
        put(metaDOI_DY.Name, getDOI_DY());
        put(metaDOI_YR.Name, getDOI_YR());
        put(metaTOI_HR.Name, getTOI_HR());
        put(metaWORKINJ.Name, getWORKINJ());
        put(metaCERTL.Name, getCERTL());
        put(metaINACT.Name, getINACT());
        put(metaAUXNO2.Name, getAUXNO2());
        put(metaSTATESP.Name, getSTATESP());
        put(metaSUR_MO.Name, getSUR_MO());
        put(metaSUR_DY.Name, getSUR_DY());
        put(metaSUR_YR.Name, getSUR_YR());
        put(metaTOI_UNIT.Name, getTOI_UNIT());
        put(metaBLANK1.Name, getBLANK1());
        put(metaARMEDF.Name, getARMEDF());
        put(metaDINSTI.Name, getDINSTI());
        put(metaADDRESS_D.Name, getADDRESS_D());
        put(metaSTNUM_D.Name, getSTNUM_D());
        put(metaPREDIR_D.Name, getPREDIR_D());
        put(metaSTNAME_D.Name, getSTNAME_D());
        put(metaSTDESIG_D.Name, getSTDESIG_D());
        put(metaPOSTDIR_D.Name, getPOSTDIR_D());
        put(metaCITYTEXT_D.Name, getCITYTEXT_D());
        put(metaSTATETEXT_D.Name, getSTATETEXT_D());
        put(metaZIP9_D.Name, getZIP9_D());
        put(metaCOUNTYTEXT_D.Name, getCOUNTYTEXT_D());
        put(metaCITYCODE_D.Name, getCITYCODE_D());
        put(metaLONG_D.Name, getLONG_D());
        put(metaLAT_D.Name, getLAT_D());
        put(metaSPOUSELV.Name, getSPOUSELV());
        put(metaSPOUSEF.Name, getSPOUSEF());
        put(metaSPOUSEL.Name, getSPOUSEL());
        put(metaCITYTEXT_R.Name, getCITYTEXT_R());
        put(metaZIP9_R.Name, getZIP9_R());
        put(metaCOUNTYTEXT_R.Name, getCOUNTYTEXT_R());
        put(metaSTATETEXT_R.Name, getSTATETEXT_R());
        put(metaCOUNTRYTEXT_R.Name, getCOUNTRYTEXT_R());
        put(metaADDRESS_R.Name, getADDRESS_R());
        put(metaRESSTATE.Name, getRESSTATE());
        put(metaRESCON.Name, getRESCON());
        put(metaSTNUM_R.Name, getSTNUM_R());
        put(metaPREDIR_R.Name, getPREDIR_R());
        put(metaSTNAME_R.Name, getSTNAME_R());
        put(metaPOSTDIR_R.Name, getPOSTDIR_R());
        put(metaUNITNUM_R.Name, getUNITNUM_R());
        put(metaDETHNICE.Name, getDETHNICE());
        put(metaNCHSBRIDGE.Name, getNCHSBRIDGE());
        put(metaHISPOLDC.Name, getHISPOLDC());
        put(metaRACEOLDC.Name, getRACEOLDC());
        put(metaHISPSTSP.Name, getHISPSTSP());
        put(metaRACESTSP.Name, getRACESTSP());
        put(metaDMIDDLE.Name, getDMIDDLE());
        put(metaDDADF.Name, getDDADF());
        put(metaDDADMID.Name, getDDADMID());
        put(metaDMOMF.Name, getDMOMF());
        put(metaDMOMMID.Name, getDMOMMID());
        put(metaDMOMMDN.Name, getDMOMMDN());
        put(metaREFERRED.Name, getREFERRED());
        put(metaPOILITRL.Name, getPOILITRL());
        put(metaHOWINJ.Name, getHOWINJ());
        put(metaTRANSPRT.Name, getTRANSPRT());
        put(metaCOUNTYTEXT_I.Name, getCOUNTYTEXT_I());
        put(metaCOUNTYCODE_I.Name, getCOUNTYCODE_I());
        put(metaCITYTEXT_I.Name, getCITYTEXT_I());
        put(metaCITYCODE_I.Name, getCITYCODE_I());
        put(metaSTATECODE_I.Name, getSTATECODE_I());
        put(metaLONG_I.Name, getLONG_I());
        put(metaLAT_I.Name, getLAT_I());
        put(metaOLDEDUC.Name, getOLDEDUC());
        put(metaCOD1A.Name, getCOD1A());
        put(metaINTERVAL1A.Name, getINTERVAL1A());
        put(metaCOD1B.Name, getCOD1B());
        put(metaINTERVAL1B.Name, getINTERVAL1B());
        put(metaCOD1C.Name, getCOD1C());
        put(metaINTERVAL1C.Name, getINTERVAL1C());
        put(metaCOD1D.Name, getCOD1D());
        put(metaINTERVAL1D.Name, getINTERVAL1D());
        put(metaOTHERCONDITION.Name, getOTHERCONDITION());
        put(metaDMAIDEN.Name, getDMAIDEN());
        put(metaDBPLACECITYCODE.Name, getDBPLACECITYCODE());
        put(metaDBPLACECITY.Name, getDBPLACECITY());
        put(metaINFORMRELATE.Name, getINFORMRELATE());
        put(metaSPOUSEMIDNAME.Name, getSPOUSEMIDNAME());
        put(metaSPOUSESUFFIX.Name, getSPOUSESUFFIX());
        put(metaFATHERSUFFIX.Name, getFATHERSUFFIX());
        put(metaMOTHERSSUFFIX.Name, getMOTHERSSUFFIX());
        put(metaDISPSTATECD.Name, getDISPSTATECD());
        put(metaDISPSTATE.Name, getDISPSTATE());
        put(metaDISPCITYCODE.Name, getDISPCITYCODE());
        put(metaDISPCITY.Name, getDISPCITY());
        put(metaFUNFACNAME.Name, getFUNFACNAME());
        put(metaFUNFACSTNUM.Name, getFUNFACSTNUM());
        put(metaFUNFACPREDIR.Name, getFUNFACPREDIR());
        put(metaFUNFACSTRNAME.Name, getFUNFACSTRNAME());
        put(metaFUNFACSTRDESIG.Name, getFUNFACSTRDESIG());
        put(metaFUNPOSTDIR.Name, getFUNPOSTDIR());
        put(metaFUNUNITNUM.Name, getFUNUNITNUM());
        put(metaFUNFACADDRESS.Name, getFUNFACADDRESS());
        put(metaFUNCITYTEXT.Name, getFUNCITYTEXT());
        put(metaFUNSTATECD.Name, getFUNSTATECD());
        put(metaFUNSTATE.Name, getFUNSTATE());
        put(metaFUNZIP.Name, getFUNZIP());
        put(metaPPDATESIGNED.Name, getPPDATESIGNED());
        put(metaPPTIME.Name, getPPTIME());
        put(metaCERTFIRST.Name, getCERTFIRST());
        put(metaCERTMIDDLE.Name, getCERTMIDDLE());
        put(metaCERTLAST.Name, getCERTLAST());
        put(metaCERTSUFFIX.Name, getCERTSUFFIX());
        put(metaCERTSTNUM.Name, getCERTSTNUM());
        put(metaCERTPREDIR.Name, getCERTPREDIR());
        put(metaCERTSTRNAME.Name, getCERTSTRNAME());
        put(metaCERTSTRDESIG.Name, getCERTSTRDESIG());
        put(metaCERTPOSTDIR.Name, getCERTPOSTDIR());
        put(metaCERTUNITNUM.Name, getCERTUNITNUM());
        put(metaCERTADDRESS.Name, getCERTADDRESS());
        put(metaCERTCITYTEXT.Name, getCERTCITYTEXT());
        put(metaCERTSTATECD.Name, getCERTSTATECD());
        put(metaCERTSTATE.Name, getCERTSTATE());
        put(metaCERTZIP.Name, getCERTZIP());
        put(metaCERTDATE.Name, getCERTDATE());
        put(metaFILEDATE.Name, getFILEDATE());
        put(metaSTINJURY.Name, getSTINJURY());
        put(metaSTATEBTH.Name, getSTATEBTH());
        put(metaDTHCOUNTRYCD.Name, getDTHCOUNTRYCD());
        put(metaDTHCOUNTRY.Name, getDTHCOUNTRY());
        put(metaSSADTHCODE.Name, getSSADTHCODE());
        put(metaSSAFOREIGN.Name, getSSAFOREIGN());
        put(metaSSAVERIFY.Name, getSSAVERIFY());
        put(metaSSADATEVER.Name, getSSADATEVER());
        put(metaSSADATETRANS.Name, getSSADATETRANS());
        put(metaDETHNIC5C.Name, getDETHNIC5C());
        put(metaPLACE1_1.Name, getPLACE1_1());
        put(metaPLACE1_2.Name, getPLACE1_2());
        put(metaPLACE1_3.Name, getPLACE1_3());
        put(metaPLACE1_4.Name, getPLACE1_4());
        put(metaPLACE1_5.Name, getPLACE1_5());
        put(metaPLACE1_6.Name, getPLACE1_6());
        put(metaPLACE8_1.Name, getPLACE8_1());
        put(metaPLACE8_2.Name, getPLACE8_2());
        put(metaPLACE8_3.Name, getPLACE8_3());
        put(metaPLACE20.Name, getPLACE20());
    }};


    public String getDOD_YR()
    {
        return NumericAllowingUnknown_Get("DOD_YR", "DeathYear");
    }
    public void setDOD_YR(String value)
    {
        NumericAllowingUnknown_Set("DOD_YR", "DeathYear", value);
    }

    /// <summary>State, U.S. Territory or Canadian Province of Death - code</summary>
    static MetaIJEField metaDSTATE = new MetaIJEField(2, 5, 2, "State, U.S. Territory or Canadian Province of Death - code", "DSTATE", 1);
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
    static MetaIJEField metaFILENO = new MetaIJEField(3, 7, 6, "Certificate Number", "FILENO", 1);
    private String FILENO;
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
    static MetaIJEField metaVOID = new MetaIJEField(4, 13, 1, "Void flag", "VOID", 1);
    //private String VOID;
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
    static MetaIJEField metaAUXNO = new MetaIJEField(5, 14, 12, "Auxiliary State file number", "AUXNO", 1);
    private String AUXNO;
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
    static MetaIJEField metaMFILED = new MetaIJEField(6, 26, 1, "Source flag: paper/electronic", "MFILED", 1);
    private String GNAME;
    public String getMFILED()
    {
        return Get_MappingFHIRToIJE(Mappings.FilingFormat.FHIRToIJE, "FilingFormat", "MFILED");
    }
    public void setMFILED(String value)
    {
        Set_MappingIJEToFHIR(Mappings.FilingFormat.IJEToFHIR, "MFILED", "FilingFormat", value);
    }

    /// <summary>Decedent's Legal Name--Given</summary>
    static MetaIJEField metaGNAME = new MetaIJEField(7, 27, 50, "Decedent's Legal Name--Given", "GNAME", 1);
    //private String GNAME;
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
    static MetaIJEField metaMNAME = new MetaIJEField(8, 77, 1, "Decedent's Legal Name--Middle", "MNAME", 2);
    private String MNAME;
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
    static MetaIJEField metaLNAME = new MetaIJEField(9, 78, 50, "Decedent's Legal Name--Last", "LNAME", 1);
    private String LNAME;
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
    static MetaIJEField metaSUFF = new MetaIJEField(10, 128, 10, "Decedent's Legal Name--Suffix", "SUFF", 1);
    private String SUFF;
    public String getSUFF()
    {
        return LeftJustified_Get("SUFF", "Suffix");
    }
    public void setSUFF(String value)
    {
        LeftJustified_Set("SUFF", "Suffix", value);
    }

    /// <summary>Decedent's Legal Name--Alias</summary>
    static MetaIJEField metaALIAS = new MetaIJEField(11, 138, 1, "Decedent's Legal Name--Alias", "ALIAS", 1);
    //private String ALIAS;
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
    static MetaIJEField metaFLNAME = new MetaIJEField(12, 139, 50, "Father's Surname", "FLNAME", 1);
    private String FLNAME;
    public String getFLNAME()
    {
        return LeftJustified_Get("FLNAME", "FatherFamilyName");
    }
    public void setFLNAME(String value)
    {
        LeftJustified_Set("FLNAME", "FatherFamilyName", value);
    }

    /// <summary>Sex</summary>
    static MetaIJEField metaSEX = new MetaIJEField(13, 189, 1, "Sex", "SEX", 1);
    private String SEX;
    public String getSEX()
    {
        return Get_MappingFHIRToIJE(Mappings.AdministrativeGender.FHIRToIJE, "SexAtDeath", "SEX");
    }
    public void setSEX(String value)
    {
        Set_MappingIJEToFHIR(Mappings.AdministrativeGender.IJEToFHIR, "SEX", "SexAtDeath", value);
    }

    /// <summary>Sex--Edit Flag</summary>
    static MetaIJEField metaSEX_BYPASS = new MetaIJEField(14, 190, 1, "Sex--Edit Flag", "SEX_BYPASS", 1);
    private String SEX_BYPASS;
    public String getSEX_BYPASS()
    {
        return ""; // Blank
    }
    public void setSEX_BYPASS(String value)
    {
        // NOOP
    }

    /// <summary>Social Security Number</summary>
    static MetaIJEField metaSSN = new MetaIJEField(15, 191, 9, "Social Security Number", "SSN", 1);
    private String SSN;
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
    static MetaIJEField metaAGETYPE = new MetaIJEField(16, 200, 1, "Decedent's Age--Type", "AGETYPE", 1);
    private String AGETYPE;
    public String getAGETYPE()
    {
        // Pull code from coded unit.   "code" field is not required by VRDR IG
        String code = Map_Get_Full("AGETYPE", "AgeAtDeath", "code") ?? "";
        String ijeValue = Mappings.UnitsOfAge.FHIRToIJE.get(code);
        return ijeValue != null ? ijeValue : "9";
    }
    public void setAGETYPE(String value)
    {
        if (isNullOrWhiteSpace(value))
        {
            return;  // nothing to do
        }
        // If we have an IJE value map it to FHIR and set the unit, code and system appropriately, otherwise set to unknown
        String fhirValue = null;
        if (Mappings.UnitsOfAge.IJEToFHIR.get(value) != null)
        {
            // We have an invalid code, map it to unknown
            fhirValue = ValueSets.UnitsOfAge.Unknown;
        }
        // We have the code, now we need the corresponding unit and system
        // Iterate over the allowed options and see if the code supplies is one of them
        int length = ValueSets.UnitsOfAge.Codes.length;
        for (int i = 0; i < length; i += 1)
        {
            if (ValueSets.UnitsOfAge.Codes[i][0].equals(fhirValue))
            {
                // Found it, so call the supplied setter with the appropriate Map built based on the code
                // using the supplied options and return
                Map<String, String> map = new HashMap<String, String>();
                map.put("code", fhirValue);
                map.put("unit", ValueSets.UnitsOfAge.Codes[i][1]);
                map.put("system", ValueSets.UnitsOfAge.Codes[i][2]);
                try
                {
                    DeathCertificateDocument.class.getField("AgeAtDeath").set(this.record, map);
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
                catch (NoSuchFieldException e)
                {
                    throw new RuntimeException(e);
                }
                return;
            }
        }
    }

    /// <summary>Decedent's Age--Units</summary>
    static MetaIJEField metaAGE = new MetaIJEField(17, 201, 3, "Decedent's Age--Units", "AGE", 2);

    private String AGE;
    public String getAGE()
    {
        if ((record.getDecedentAge().get(0) != null) && !this.AGETYPE.equals("9"))
        {
            // MetaIJEField info = FieldInfo("AGE");
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
    static MetaIJEField metaAGE_BYPASS = new MetaIJEField(18, 204, 1, "Decedent's Age--Edit Flag", "AGE_BYPASS", 1);
    private String AGE_BYPASS;
    public String getAGE_BYPASS()
    {
        return Get_MappingFHIRToIJE(Mappings.EditBypass01.FHIRToIJE, "AgeAtDeathEditFlag", "AGE_BYPASS");
    }
    public void setAGE_BYPASS(String value)
    {
        Set_MappingIJEToFHIR(Mappings.EditBypass01.IJEToFHIR, "AGE_BYPASS", "AgeAtDeathEditFlag", value);
    }

    /// <summary>Date of Birth--Year</summary>
    static MetaIJEField metaDOB_YR = new MetaIJEField(19, 205, 4, "Date of Birth--Year", "DOB_YR", 1);
    private String DOB_YR;
    public String getDOB_YR()
    {
        return NumericAllowingUnknown_Get("DOB_YR", "BirthYear");
    }
    public void setDOB_YR(String value)
    {
        NumericAllowingUnknown_Set("DOB_YR", "BirthYear", value);
    }

    /// <summary>Date of Birth--Month</summary>
    static MetaIJEField metaDOB_MO = new MetaIJEField(20, 209, 2, "Date of Birth--Month", "DOB_MO", 1);
    private String DOB_MO;
    public String getDOB_MO()
    {
        return NumericAllowingUnknown_Get("DOB_MO", "BirthMonth");
    }
    public void setDOB_MO(String value)
    {
        NumericAllowingUnknown_Set("DOB_MO", "BirthMonth", value);
    }

    /// <summary>Date of Birth--Day</summary>
    static MetaIJEField metaDOB_DY = new MetaIJEField(21, 211, 2, "Date of Birth--Day", "DOB_DY", 1);
    private String DOB_DY;
    public String getDOB_DY()
    {
        return NumericAllowingUnknown_Get("DOB_DY", "BirthDay");
    }
    public void setDOB_DY(String value)
    {
        NumericAllowingUnknown_Set("DOB_DY", "BirthDay", value);
    }

    /// <summary>Birthplace--Country</summary>
    static MetaIJEField metaBPLACE_CNT = new MetaIJEField(22, 213, 2, "Birthplace--Country", "BPLACE_CNT", 1);
    private String BPLACE_CNT;
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
    static MetaIJEField metaBPLACE_ST = new MetaIJEField(23, 215, 2, "State, U.S. Territory or Canadian Province of Birth - code", "BPLACE_ST", 1);
    private String BPLACE_ST;
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
    MetaIJEField metaCITYC = new MetaIJEField(24, 217, 5, "Decedent's Residence--City", "CITYC", 3);
    private String CITYC;
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
    static MetaIJEField metaCOUNTYC = new MetaIJEField(25, 222, 3, "Decedent's Residence--County", "COUNTYC", 2);
    private String COUNTYC;
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
    static MetaIJEField metaSTATEC = new MetaIJEField(26, 225, 2, "State, U.S. Territory or Canadian Province of Decedent's residence - code", "STATEC", 1);
    private String STATEC;
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
    static MetaIJEField metaCOUNTRYC = new MetaIJEField(27, 227, 2, "Decedent's Residence--Country", "COUNTRYC", 1);
    private String COUNTRYC;
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
    static MetaIJEField metaLIMITS = new MetaIJEField(28, 229, 1, "Decedent's Residence--Inside City Limits", "LIMITS", 10);
    private String LIMITS;
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
    static MetaIJEField metaMARITAL = new MetaIJEField(29, 230, 1, "Marital Status", "MARITAL", 1);
    private String MARITAL;
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
    static MetaIJEField metaMARITAL_BYPASS = new MetaIJEField(30, 231, 1, "Marital Status--Edit Flag", "MARITAL_BYPASS", 1);
    private String MARITAL_BYPASS;
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
    static MetaIJEField metaDPLACE = new MetaIJEField(31, 232, 1, "Place of Death", "DPLACE", 1);
    private String DPLACE;
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
    static MetaIJEField metaCOD = new MetaIJEField(32, 233, 3, "County of Death Occurrence", "COD", 2);
    private String COD;
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
    static MetaIJEField metaDISP = new MetaIJEField(33, 236, 1, "Method of Disposition", "DISP", 1);
    private String DISP;
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
    static MetaIJEField metaDOD_MO = new MetaIJEField(34, 237, 2, "Date of Death--Month", "DOD_MO", 1);
    private String DOD_MO;
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
    static MetaIJEField metaDOD_DY = new MetaIJEField(35, 239, 2, "Date of Death--Day", "DOD_DY", 1);
    private String DOD_DY;
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
    static MetaIJEField metaTOD = new MetaIJEField(36, 241, 4, "Time of Death", "TOD", 1);
    private String TOD;
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
    static MetaIJEField metaDEDUC = new MetaIJEField(37, 245, 1, "Decedent's Education", "DEDUC", 1);
    private String DEDUC;
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
    static MetaIJEField metaDEDUC_BYPASS = new MetaIJEField(38, 246, 1, "Decedent's Education--Edit Flag", "DEDUC_BYPASS", 1);
    private String DEDUC_BYPASS;
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
    MetaIJEField metaDETHNIC1 = new MetaIJEField(39, 247, 1, "Decedent of Hispanic Origin?--Mexican", "DETHNIC1", 1);
    private String DETHNIC1;
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
    static MetaIJEField metaDETHNIC2 = new MetaIJEField(40, 248, 1, "Decedent of Hispanic Origin?--Puerto Rican", "DETHNIC2", 1);
    private String DETHNIC2;
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
    static MetaIJEField metaDETHNIC3 = new MetaIJEField(41, 249, 1, "Decedent of Hispanic Origin?--Cuban", "DETHNIC3", 1);
    private String DETHNIC3;
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
    static MetaIJEField metaDETHNIC4 = new MetaIJEField(42, 250, 1, "Decedent of Hispanic Origin?--Other", "DETHNIC4", 1);
    private String DETHNIC4;
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
    static MetaIJEField metaDETHNIC5 = new MetaIJEField(43, 251, 20, "Decedent of Hispanic Origin?--Other, Literal", "DETHNIC5", 1);
    private String DETHNIC5;
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
    static MetaIJEField metaRACE1 = new MetaIJEField(44, 271, 1, "Decedent's Race--White", "RACE1", 1);
    private String RACE1;
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
    static MetaIJEField metaRACE2 = new MetaIJEField(45, 272, 1, "Decedent's Race--Black or African American", "RACE2", 1);
    private String RACE2;
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
    static MetaIJEField metaRACE3 = new MetaIJEField(46, 273, 1, "Decedent's Race--American Indian or Alaska Native", "RACE3", 1);
    private String RACE3;
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
    static MetaIJEField metaRACE4 = new MetaIJEField(47, 274, 1, "Decedent's Race--Asian Indian", "RACE4", 1);
    private String RACE4;
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
    static MetaIJEField metaRACE5 = new MetaIJEField(48, 275, 1, "Decedent's Race--Chinese", "RACE5", 1);
    private String RACE5;
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
    static MetaIJEField metaRACE6 = new MetaIJEField(49, 276, 1, "Decedent's Race--Filipino", "RACE6", 1);
    private String RACE6;
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
    static MetaIJEField metaRACE7 = new MetaIJEField(50, 277, 1, "Decedent's Race--Japanese", "RACE7", 1);
    private String RACE7;
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
    static MetaIJEField metaRACE8 = new MetaIJEField(51, 278, 1, "Decedent's Race--Korean", "RACE8", 1);
    private String RACE8;
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
    static MetaIJEField metaRACE9 = new MetaIJEField(52, 279, 1, "Decedent's Race--Vietnamese", "RACE9", 1);
    private String RACE9;
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
    static MetaIJEField metaRACE10 = new MetaIJEField(53, 280, 1, "Decedent's Race--Other Asian", "RACE10", 1);
    private String RACE10;
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
    static MetaIJEField metaRACE11 = new MetaIJEField(54, 281, 1, "Decedent's Race--Native Hawaiian", "RACE11", 1);
    private String RACE11;
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
    static MetaIJEField metaRACE12 = new MetaIJEField(55, 282, 1, "Decedent's Race--Guamanian or Chamorro", "RACE12", 1);
    private String RACE12;
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
    static MetaIJEField metaRACE13 = new MetaIJEField(56, 283, 1, "Decedent's Race--Samoan", "RACE13", 1);
    private String RACE13;
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
    static MetaIJEField metaRACE14 = new MetaIJEField(57, 284, 1, "Decedent's Race--Other Pacific Islander", "RACE14", 1);
    private String RACE14;
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
    static MetaIJEField metaRACE15 = new MetaIJEField(58, 285, 1, "Decedent's Race--Other", "RACE15", 1);
    private String RACE15;
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
    static MetaIJEField metaRACE16 = new MetaIJEField(59, 286, 30, "Decedent's Race--First American Indian or Alaska Native Literal", "RACE16", 1);
    private String RACE16;
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
    static MetaIJEField metaRACE17 = new MetaIJEField(60, 316, 30, "Decedent's Race--Second American Indian or Alaska Native Literal", "RACE17", 1);
    private String RACE17;
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
    static MetaIJEField metaRACE18 = new MetaIJEField(61, 346, 30, "Decedent's Race--First Other Asian Literal", "RACE18", 1);
    private String RACE18;
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
    static MetaIJEField metaRACE19 = new MetaIJEField(62, 376, 30, "Decedent's Race--Second Other Asian Literal", "RACE19", 1);
    private String RACE19;
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
    static MetaIJEField metaRACE20 = new MetaIJEField(63, 406, 30, "Decedent's Race--First Other Pacific Islander Literal", "RACE20", 1);
    private String RACE20;
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
    static MetaIJEField metaRACE21 = new MetaIJEField(64, 436, 30, "Decedent's Race--Second Other Pacific Islander Literal", "RACE21", 1);
    private String RACE21;
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
    static MetaIJEField metaRACE22 = new MetaIJEField(65, 466, 30, "Decedent's Race--First Other Literal", "RACE22", 1);
    private String RACE22;
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
    static MetaIJEField metaRACE23 = new MetaIJEField(66, 496, 30, "Decedent's Race--Second Other Literal", "RACE23", 1);
    private String RACE23;
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
    static MetaIJEField metaRACE1E = new MetaIJEField(67, 526, 3, "First Edited Code", "RACE1E", 1);
    private String RACE1E;
    public String getRACE1E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FirstEditedRaceCode", "RACE1E");
    }
    public void setRACE1E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE1E", "FirstEditedRaceCode", value);
    }

    /// <summary>Second Edited Code</summary>
    static MetaIJEField metaRACE2E = new MetaIJEField(68, 529, 3, "Second Edited Code", "RACE2E", 1);
    private String RACE2E;
    public String getRACE2E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SecondEditedRaceCode", "RACE2E");
    }
    public void setRACE2E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE2E", "SecondEditedRaceCode", value);
    }

    /// <summary>Third Edited Code</summary>
    static MetaIJEField metaRACE3E = new MetaIJEField(69, 532, 3, "Third Edited Code", "RACE3E", 1);
    private String RACE3E;
    public String getRACE3E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "ThirdEditedRaceCode", "RACE3E");
    }
    public void setRACE3E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE3E", "ThirdEditedRaceCode", value);
    }

    /// <summary>Fourth Edited Code</summary>
    static MetaIJEField metaRACE4E = new MetaIJEField(70, 535, 3, "Fourth Edited Code", "RACE4E", 1);
    private String RACE4E;
    public String getRACE4E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FourthEditedRaceCode", "RACE4E");
    }
    public void setRACE4E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE4E", "FourthEditedRaceCode", value);
    }

    /// <summary>Fifth Edited Code</summary>
    static MetaIJEField metaRACE5E = new MetaIJEField(71, 538, 3, "Fifth Edited Code", "RACE5E", 1);
    private String RACE5E;
    public String getRACE5E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FifthEditedRaceCode", "RACE5E");
    }
    public void setRACE5E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE5E", "FifthEditedRaceCode", value);
    }

    /// <summary>Sixth Edited Code</summary>
    static MetaIJEField metaRACE6E = new MetaIJEField(72, 541, 3, "Sixth Edited Code", "RACE6E", 1);
    private String RACE6E;
    public String getRACE6E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SixthEditedRaceCode", "RACE6E");
    }
    public void setRACE6E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE6E", "SixthEditedRaceCode", value);
    }

    /// <summary>Seventh Edited Code</summary>
    static MetaIJEField metaRACE7E = new MetaIJEField(73, 544, 3, "Seventh Edited Code", "RACE7E", 1);
    private String RACE7E;
    public String getRACE7E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SeventhEditedRaceCode", "RACE7E");
    }
    public void setRACE7E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE7E", "SeventhEditedRaceCode", value);
    }

    /// <summary>Eighth Edited Code</summary>
    static MetaIJEField metaRACE8E = new MetaIJEField(74, 547, 3, "Eighth Edited Code", "RACE8E", 1);
    private String RACE8E;
    public String getRACE8E()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "EighthEditedRaceCode", "RACE8E");
    }
    public void setRACE8E(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE8E", "EighthEditedRaceCode", value);
    }

    /// <summary>First American Indian Code</summary>
    static MetaIJEField metaRACE16C = new MetaIJEField(75, 550, 3, "First American Indian Code", "RACE16C", 1);
    private String RACE16C;
    public String getRACE16C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FirstAmericanIndianRaceCode", "RACE16C");
    }
    public void setRACE16C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE16C", "FirstAmericanIndianRaceCode", value);
    }

    /// <summary>Second American Indian Code</summary>
    static MetaIJEField metaRACE17C = new MetaIJEField(76, 553, 3, "Second American Indian Code", "RACE17C", 1);
    private String RACE17C;
    public String getRACE17C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SecondAmericanIndianRaceCode", "RACE17C");
    }
    public void setRACE17C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE17C", "SecondAmericanIndianRaceCode", value);
    }

    /// <summary>First Other Asian Code</summary>
    static MetaIJEField metaRACE18C = new MetaIJEField(77, 556, 3, "First Other Asian Code", "RACE18C", 1);
    private String RACE18C;
    public String getRACE18C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FirstOtherAsianRaceCode", "RACE18C");
    }
    public void setRACE18C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE18C", "FirstOtherAsianRaceCode", value);
    }

    /// <summary>Second Other Asian Code</summary>
    static MetaIJEField metaRACE19C = new MetaIJEField(78, 559, 3, "Second Other Asian Code", "RACE19C", 1);
    private String RACE19C;
    public String getRACE19C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SecondOtherAsianRaceCode", "RACE19C");
    }
    public void setRACE19C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE19C", "SecondOtherAsianRaceCode", value);
    }

    /// <summary>First Other Pacific Islander Code</summary>
    static MetaIJEField metaRACE20C = new MetaIJEField(79, 562, 3, "First Other Pacific Islander Code", "RACE20C", 1);
    private String RACE20C;
    public String getRACE20C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FirstOtherPacificIslanderRaceCode", "RACE20C");
    }
    public void setRACE20C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE20C", "FirstOtherPacificIslanderRaceCode", value);
    }

    /// <summary>Second Other Pacific Islander Code</summary>
    static MetaIJEField metaRACE21C = new MetaIJEField(80, 565, 3, "Second Other Pacific Islander Code", "RACE21C", 1);
    private String RACE21C;
    public String getRACE21C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SecondOtherPacificIslanderRaceCode", "RACE21C");
    }
    public void setRACE21C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE21C", "SecondOtherPacificIslanderRaceCode", value);
    }

    /// <summary>First Other Race Code</summary>
    static MetaIJEField metaRACE22C = new MetaIJEField(81, 568, 3, "First Other Race Code", "RACE22C", 1);
    private String RACE22C;
    public String getRACE22C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "FirstOtherRaceCode", "RACE22C");
    }
    public void setRACE22C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE22C", "FirstOtherRaceCode", value);
    }

    /// <summary>Second Other Race Code</summary>
    static MetaIJEField metaRACE23C = new MetaIJEField(82, 571, 3, "Second Other Race Code", "RACE23C", 1);
    private String RACE23C;
    public String getRACE23C()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceCode.FHIRToIJE, "SecondOtherRaceCode", "RACE23C");
    }
    public void setRACE23C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceCode.IJEToFHIR, "RACE23C", "SecondOtherRaceCode", value);
    }

    /// <summary>Decedent's Race--Missing</summary>
    static MetaIJEField metaRACE_MVR = new MetaIJEField(83, 574, 1, "Decedent's Race--Missing", "RACE_MVR", 1);
    private String RACE_MVR;
    public String getRACE_MVR()
    {
        return Get_MappingFHIRToIJE(Mappings.RaceMissingValueReason.FHIRToIJE, "RaceMissingValueReason", "RACE_MVR");
    }
    public void setRACE_MVR(String value)
    {
        Set_MappingIJEToFHIR(Mappings.RaceMissingValueReason.IJEToFHIR, "RACE_MVR", "RaceMissingValueReason", value);
    }

    /// <summary>Occupation -- Literal (OPTIONAL)</summary>
    static MetaIJEField metaOCCUP = new MetaIJEField(84, 575, 40, "Occupation -- Literal (OPTIONAL)", "OCCUP", 1);
    private String OCCUP;
    public String getOCCUP()
    {
        return LeftJustified_Get("OCCUP", "UsualOccupation");
    }
    public void setOCCUP(String value)
    {
        LeftJustified_Set("OCCUP", "UsualOccupation", value);
    }

    /// <summary>Occupation -- Code</summary>
    static MetaIJEField metaOCCUPC = new MetaIJEField(85, 615, 3, "Occupation -- Code", "OCCUPC", 1);
    private String OCCUPC;
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
    static MetaIJEField metaINDUST = new MetaIJEField(86, 618, 40, "Industry -- Literal (OPTIONAL)", "INDUST", 1);
    private String INDUST;
    public String getINDUST()
    {
        return LeftJustified_Get("INDUST", "UsualIndustry");
    }
    public void setINDUST(String value)
    {
        LeftJustified_Set("INDUST", "UsualIndustry", value);
    }

    /// <summary>Industry -- Code</summary>
    static MetaIJEField metaINDUSTC = new MetaIJEField(87, 658, 3, "Industry -- Code", "INDUSTC", 1);
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
    static MetaIJEField metaBCNO = new MetaIJEField(88, 661, 6, "Infant Death/Birth Linking - birth certificate number", "BCNO", 1);
    private String BCNO;
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
    static MetaIJEField metaIDOB_YR = new MetaIJEField(89, 667, 4, "Infant Death/Birth Linking - year of birth", "IDOB_YR", 1);
    public String getIDOB_YR()
    {
        return LeftJustified_Get("IDOB_YR", "BirthRecordYear");
    }
    private String IDOB_YR;
    public void setIDOB_YR(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            LeftJustified_Set("IDOB_YR", "BirthRecordYear", value);
        }
    }

    /// <summary>Infant Death/Birth Linking - Birth state</summary>
    static MetaIJEField metaBSTATE = new MetaIJEField(90, 671, 2, "Infant Death/Birth Linking - State, U.S. Territory or Canadian Province of Birth - code", "BSTATE", 1);
    private String BSTATE;
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
    static MetaIJEField metaR_YR = new MetaIJEField(91, 673, 4, "Receipt date -- Year", "R_YR", 1);
    private String R_YR;
    public String getR_YR()
    {
        return NumericAllowingUnknown_Get("R_YR", "ReceiptYear");
    }
    public void setR_YR(String value)
    {
        NumericAllowingUnknown_Set("R_YR", "ReceiptYear", value);
    }

    /// <summary>Receipt date -- Month</summary>
    static MetaIJEField metaR_MO = new MetaIJEField(92, 677, 2, "Receipt date -- Month", "R_MO", 1);
    private String R_MO;
    public String getR_MO()
    {
        return NumericAllowingUnknown_Get("R_MO", "ReceiptMonth");
    }
    public void setR_MO(String value)
    {
        NumericAllowingUnknown_Set("R_MO", "ReceiptMonth", value);
    }

    /// <summary>Receipt date -- Day</summary>
    static MetaIJEField metaR_DY = new MetaIJEField(93, 679, 2, "Receipt date -- Day", "R_DY", 1);
    private String R_DY;
    public String getR_DY()
    {
        return NumericAllowingUnknown_Get("R_DY", "ReceiptDay");
    }
    public void setR_DY(String value)
    {
        NumericAllowingUnknown_Set("R_DY", "ReceiptDay", value);
    }

    /// <summary>Occupation -- 4 digit Code (OPTIONAL)</summary>
    static MetaIJEField metaOCCUPC4 = new MetaIJEField(94, 681, 4, "Occupation -- 4 digit Code (OPTIONAL)", "OCCUPC4", 1);
    private String OCCUPC4;
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
    MetaIJEField metaINDUSTC = new MetaIJEField(95, 685, 4, "Industry -- 4 digit Code (OPTIONAL)", "INDUSTC4", 1);
    private String INDUSTC;
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
    static MetaIJEField metaDOR_YR = new MetaIJEField(96, 689, 4, "Date of Registration--Year", "DOR_YR", 1);
    private String DOR_YR;
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
    static MetaIJEField metaDOR_MO = new MetaIJEField(97, 693, 2, "Date of Registration--Month", "DOR_MO", 1);
    private String DOR_MO;
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
    static MetaIJEField metaDOR_DY = new MetaIJEField(98, 695, 2, "Date of Registration--Day", "DOR_DY", 1);
    private String DOR_DY;
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
    static MetaIJEField metaFILLER2 = new MetaIJEField(99, 697, 4, "FILLER 2 for expansion", "FILLER2", 1);
    private String FILLER2;
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
    static MetaIJEField metaMANNER = new MetaIJEField(100, 701, 1, "Manner of Death", "MANNER", 1);
    private String MANNER;
    public String getMANNER()
    {
        return Get_MappingFHIRToIJE(Mappings.MannerOfDeath.FHIRToIJE, "MannerOfDeathType", "MANNER");
    }
    public void setMANNER(String value)
    {
        Set_MappingIJEToFHIR(Mappings.MannerOfDeath.IJEToFHIR, "MANNER", "MannerOfDeathType", value);
    }

    /// <summary>Intentional Reject</summary>
    static MetaIJEField metaINT_REJ = new MetaIJEField(101, 702, 1, "Intentional Reject", "INT_REJ", 1);
    private String INT_REJ;
    public String getINT_REJ()
    {
        return Get_MappingFHIRToIJE(Mappings.IntentionalReject.FHIRToIJE, "IntentionalReject", "INT_REJ");
    }
    public void setINT_REJ(String value)
    {
        Set_MappingIJEToFHIR(Mappings.IntentionalReject.IJEToFHIR, "INT_REJ", "IntentionalReject", value);
    }

    /// <summary>Acme System Reject Codes</summary>
    static MetaIJEField metaSYS_REJ = new MetaIJEField(102, 703, 1, "Acme System Reject Codes", "SYS_REJ", 1);
    private String SYS_REJ;
    public String getSYS_REJ()
    {
        return Get_MappingFHIRToIJE(Mappings.SystemReject.FHIRToIJE, "AcmeSystemReject", "SYS_REJ");
    }
    public void setSYS_REJ(String value)
    {
        Set_MappingIJEToFHIR(Mappings.SystemReject.IJEToFHIR, "SYS_REJ", "AcmeSystemReject", value);
    }

    /// <summary>Place of Injury (computer generated)</summary>
    static MetaIJEField metaINJPL = new MetaIJEField(103, 704, 1, "Place of Injury (computer generated)", "INJPL", 1);
    private String INJPL;
    public String getINJPL()
    {
        return Get_MappingFHIRToIJE(Mappings.PlaceOfInjury.FHIRToIJE, "PlaceOfInjury", "INJPL");
    }
    public void setINJPL(String value)
    {
        Set_MappingIJEToFHIR(Mappings.PlaceOfInjury.IJEToFHIR, "INJPL", "PlaceOfInjury", value);
    }

    /// <summary>Manual Underlying Cause</summary>
    static MetaIJEField metaMAN_UC = new MetaIJEField(104, 705, 5, "Manual Underlying Cause", "MAN_UC", 1);
    private String MAN_UC;
    public String getMAN_UC()
    {
        return (ActualICD10toNCHSICD10(LeftJustified_Get("MAN_UC", "ManUnderlyingCOD")));
    }
    public void setMAN_UC(String value)
    {
        LeftJustified_Set("MAN_UC", "ManUnderlyingCOD", NCHSICD10toActualICD10(value));
    }

    /// <summary>ACME Underlying Cause</summary>
    static MetaIJEField metaACME_UC = new MetaIJEField(105, 710, 5, "ACME Underlying Cause", "ACME_UC", 1);
    private String ACME_UC;
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
    static MetaIJEField metaEAC = new MetaIJEField(106, 715, 160, "Entity-axis codes", "EAC", 1);
    private String EAC;
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
    static MetaIJEField metaTRX_FLG = new MetaIJEField(107, 875, 1, "Transax conversion flag: Computer Generated", "TRX_FLG", 1);
    private String TRX_FLG;
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
    static MetaIJEField metaRAC = new MetaIJEField(108, 876, 100, "Record-axis codes", "RAC", 1);
    private String RAC;
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

    public String getRAC() {
        StringBuilder racStr = new StringBuilder();
        for (RecordAxisCauseOfDeath entry : recordAxisCauseOfDeath) {
            String icdCode = truncate(actualICD10toNCHSICD10(entry.getCode()), 4);
            icdCode = String.format("%1$-" + 4 + "s", icdCode);
            String preg = entry.isPregnancy() ? "1" : " ";
            racStr.append(icdCode).append(preg);
        }
        String fmtRac = truncate(racStr.toString(), 100);
        fmtRac = String.format("%1$-" + 100 + "s", fmtRac);
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
    static MetaIJEField metaAUTOP = new MetaIJEField(109, 976, 1, "Was Autopsy performed", "AUTOP", 1);
    private String AUTOP;
    public String getAUTOP()
    {
        return Get_MappingFHIRToIJE(Mappings.YesNoUnknown.FHIRToIJE, "AutopsyPerformedIndicator", "AUTOP");
    }
    public void setAUTOP(String value)
    {
        Set_MappingIJEToFHIR(Mappings.YesNoUnknown.IJEToFHIR, "AUTOP", "AutopsyPerformedIndicator", value);
    }

    /// <summary>Were Autopsy Findings Available to Complete the Cause of Death?</summary>
    static MetaIJEField metaAUTOPF = new MetaIJEField(110, 977, 1, "Were Autopsy Findings Available to Complete the Cause of Death?", "AUTOPF", 1);
    private String AUTOPF;
    public String getAUTOPF()
    {
        return Get_MappingFHIRToIJE(Mappings.YesNoUnknownNotApplicable.FHIRToIJE, "AutopsyResultsAvailable", "AUTOPF");
    }
    public void setUTOPF(String value)
    {
        Set_MappingIJEToFHIR(Mappings.YesNoUnknownNotApplicable.IJEToFHIR, "AUTOPF", "AutopsyResultsAvailable", value);
    }

    /// <summary>Did Tobacco Use Contribute to Death?</summary>
    static MetaIJEField metaTOBAC = new MetaIJEField(111, 978, 1, "Did Tobacco Use Contribute to Death?", "TOBAC", 1);
    private String TOBAC;
    public String getTOBAC()
    {
        return Get_MappingFHIRToIJE(Mappings.ContributoryTobaccoUse.FHIRToIJE, "TobaccoUse", "TOBAC");
    }
    public void setTOBAC(String value)
    {
        Set_MappingIJEToFHIR(Mappings.ContributoryTobaccoUse.IJEToFHIR, "TOBAC", "TobaccoUse", value);
    }

    /// <summary>Pregnancy</summary>
    static MetaIJEField metaPREG = new MetaIJEField(112, 979, 1, "Pregnancy", "PREG", 1);
    private String PREG;
    public String getPREG()
    {
        return Get_MappingFHIRToIJE(Mappings.PregnancyStatus.FHIRToIJE, "PregnancyStatus", "PREG");
    }
    public void setPREG(String value)
    {
        Set_MappingIJEToFHIR(Mappings.PregnancyStatus.IJEToFHIR, "PREG", "PregnancyStatus", value);
    }

    /// <summary>If Female--Edit Flag: From EDR only</summary>
    static MetaIJEField metaPREG_BYPASS = new MetaIJEField(113, 980, 1, "If Female--Edit Flag: From EDR only", "PREG_BYPASS", 1);
    private String PREG_BYPASS;
    public String getPREG_BYPASS()
    {
        return Get_MappingFHIRToIJE(Mappings.EditBypass012.FHIRToIJE, "PregnancyStatusEditFlag", "PREG_BYPASS");
    }
    public void setPREG_BYPASS(String value)
    {
        Set_MappingIJEToFHIR(Mappings.EditBypass012.IJEToFHIR, "PREG_BYPASS", "PregnancyStatusEditFlag", value);
    }

    /// <summary>Date of injury--month</summary>
    static MetaIJEField metaDOI_MO = new MetaIJEField(114, 981, 2, "Date of injury--month", "DOI_MO", 1);
    private String DOI_MO;
    public String getDOI_MO()
    {
        return NumericAllowingUnknown_Get("DOI_MO", "InjuryMonth");
    }
    public void setDOI_MO(String value)
    {
        NumericAllowingUnknown_Set("DOI_MO", "InjuryMonth", value);
    }

    /// <summary>Date of injury--day</summary>
    static MetaIJEField metaDOI_DY = new MetaIJEField(115, 983, 2, "Date of injury--day", "DOI_DY", 1);
    private String DOI_DY;
    public String getDOI_DY()
    {
        return NumericAllowingUnknown_Get("DOI_DY", "InjuryDay");
    }
    public void setDOI_DY(String value)
    {
        NumericAllowingUnknown_Set("DOI_DY", "InjuryDay", value);
    }

    /// <summary>Date of injury--year</summary>
    static MetaIJEField metaDOI_YR = new MetaIJEField(116, 985, 4, "Date of injury--year", "DOI_YR", 1);
    private String DOI_YR;
    public String getDOI_YR()
    {
        return NumericAllowingUnknown_Get("DOI_YR", "InjuryYear");
    }
    public void setDOI_YR(String value)
    {
        NumericAllowingUnknown_Set("DOI_YR", "InjuryYear", value);
    }

    /// <summary>Time of injury</summary>
    static MetaIJEField metaTOI_HR = new MetaIJEField(117, 989, 4, "Time of injury", "TOI_HR", 1);
    private String TOI_HR;
    public String getTOI_HR()
    {
        return TimeAllowingUnknown_Get("TOI_HR", "InjuryTime");
    }
    public void setTOI_HR(String value)
    {
        TimeAllowingUnknown_Set("TOI_HR", "InjuryTime", value);
    }

    /// <summary>Time of injury</summary>
    static MetaIJEField metaWORKINJ = new MetaIJEField(118, 993, 1, "Injury at work", "WORKINJ", 1);
    private String WORKINJ;
    public String getWORKINJ()
    {
        return Get_MappingFHIRToIJE(Mappings.YesNoUnknownNotApplicable.FHIRToIJE, "InjuryAtWork", "WORKINJ");
    }
    public void setWORKINJ(String value)
    {
        Set_MappingIJEToFHIR(Mappings.YesNoUnknownNotApplicable.IJEToFHIR, "WORKINJ", "InjuryAtWork", value);
    }

    /// <summary>Title of Certifier</summary>
    static MetaIJEField metaCERTL = new MetaIJEField(119, 994, 30, "Title of Certifier", "CERTL", 1);
    private String CERTL;
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
    static MetaIJEField metaINACT = new MetaIJEField(120, 1024, 1, "Activity at time of death (computer generated)", "INACT", 1);
    private String INACT;
    public String getINACT()
    {
        return Get_MappingFHIRToIJE(Mappings.ActivityAtTimeOfDeath.FHIRToIJE, "ActivityAtDeath", "INACT");
    }
    public void setINACT(String value)
    {
        Set_MappingIJEToFHIR(Mappings.ActivityAtTimeOfDeath.IJEToFHIR, "INACT", "ActivityAtDeath", value);
    }

    /// <summary>Auxiliary State file number</summary>
    static MetaIJEField metaAUXNO2 = new MetaIJEField(121, 1025, 12, "Auxiliary State file number", "AUXNO2", 1);
    private String AUXNO2;
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
    static MetaIJEField metaSTATESP = new MetaIJEField(122, 1037, 30, "State Specific Data", "STATESP", 1);
    private String STATESP;
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
    static MetaIJEField metaSUR_MO = new MetaIJEField(123, 1067, 2, "Surgery Date--month", "SUR_MO", 1);
    private String SUR_MO;
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
    static MetaIJEField metaSUR_DY = new MetaIJEField(124, 1069, 2, "Surgery Date--day", "SUR_DY", 1);
    private String SUR_DY;
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
    static MetaIJEField metaSUR_YR = new MetaIJEField(125, 1071, 4, "Surgery Date--year", "SUR_YR", 1);
    private String SUR_YR;
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
    static MetaIJEField metaTOI_UNIT = new MetaIJEField(126, 1075, 1, "Time of Injury Unit", "TOI_UNIT", 1);
    private String TOI_UNIT;
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
    static MetaIJEField metaBLANK1 = new MetaIJEField(127, 1076, 5, "For possible future change in transax", "BLANK1", 1);
    private String BLANK1;
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
    static MetaIJEField metaARMEDF = new MetaIJEField(128, 1081, 1, "Decedent ever served in Armed Forces?", "ARMEDF", 1);
    private String ARMEDF;
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
    static MetaIJEField metaDINSTI = new MetaIJEField(129, 1082, 30, "Death Institution name", "DINSTI", 1);
    private String DINSTI;
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
    static MetaIJEField metaADDRESS_D = new MetaIJEField(130, 1112, 50, "Long String address for place of death", "ADDRESS_D", 1);
    private String ADDRESS_D;
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
    static MetaIJEField metaSTNUM_D = new MetaIJEField(131, 1162, 10, "Place of death. Street number", "STNUM_D", 1);
    private String STNUM_D;
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
    static MetaIJEField metaPREDIR_D = new MetaIJEField(132, 1172, 10, "Place of death. Pre Directional", "PREDIR_D", 1);
    public String getPREDIR_D()
    {
        return Map_Geo_Get("PREDIR_D", "DeathLocationAddress", "address", "predir", true);
    }
    private String PREDIR_D;
    public void setPREDIR_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("PREDIR_D", "DeathLocationAddress", "address", "predir", false, value);
        }
    }

    /// <summary>Place of death. Street name</summary>
    static MetaIJEField metaSTNAME_D = new MetaIJEField(133, 1182, 50, "Place of death. Street name", "STNAME_D", 1);
    public String getSTNAME_D()
    {
        return Map_Geo_Get("STNAME_D", "DeathLocationAddress", "address", "stname", true);
    }
    private String STNAME_D;
    public void setSTNAME_D(String value)
    {
        if (!isNullOrWhiteSpace(value))
        {
            Map_Geo_Set("STNAME_D", "DeathLocationAddress", "address", "stname", false, value);
        }
    }

    /// <summary>Place of death. Street designator</summary>
    static MetaIJEField metaSTDESIG_D = new MetaIJEField(134, 1232, 10, "Place of death. Street designator", "STDESIG_D", 1);
    private String STDESIG_D;
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
    static MetaIJEField metaPOSTDIR_D = new MetaIJEField(135, 1242, 10, "Place of death. Post Directional", "POSTDIR_D", 1);
    private String POSTDIR_D;
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
    static MetaIJEField metaCITYTEXT_D = new MetaIJEField(136, 1252, 28, "Place of death. City or Town name", "CITYTEXT_D", 1);
    private String CITYTEXT_D;
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
    static MetaIJEField metaSTATETEXT_D = new MetaIJEField(137, 1280, 28, "Place of death. State name literal", "STATETEXT_D", 1);
    private String STATETEXT_D;
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
    static MetaIJEField metaZIP9_D = new MetaIJEField(138, 1308, 9, "Place of death. Zip code", "ZIP9_D", 1);
    private String ZIP9_D;
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
    static MetaIJEField metaCOUNTYTEXT_D = new MetaIJEField(139, 1317, 28, "Place of death. County of Death", "COUNTYTEXT_D", 2);
    private String COUNTYTEXT_D;
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
    static MetaIJEField metaCITYCODE_D = new MetaIJEField(140, 1345, 5, "Place of death. City FIPS code", "CITYCODE_D", 1);
    private String CITYCODE_D;
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
    static MetaIJEField metaLONG_D = new MetaIJEField(141, 1350, 17, "Place of death. Longitude", "LONG_D", 1);
    private String LONG_D;
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
    static MetaIJEField metaLAT_D = new MetaIJEField(142, 1367, 17, "Place of Death. Latitude", "LAT_D", 1);
    private String LAT_D;
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
    static MetaIJEField metaSPOUSELV = new MetaIJEField(143, 1384, 1, "Decedent's spouse living at decedent's DOD?", "SPOUSELV", 1);
    private String SPOUSELV;
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
    static MetaIJEField metaSPOUSEF = new MetaIJEField(144, 1385, 50, "Spouse's First Name", "SPOUSEF", 1);
    private String SPOUSEF;
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
    static MetaIJEField metaSPOUSEL = new MetaIJEField(145, 1435, 50, "Husband's Surname/Wife's Maiden Last Name", "SPOUSEL", 1);
    private String SPOUSEL;
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
    static MetaIJEField metaCITYTEXT_R = new MetaIJEField(152, 1560, 28, "Decedent's Residence - City or Town name", "CITYTEXT_R", 3);
    private String CITYTEXT_R;
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
    static MetaIJEField metaZIP9_R = new MetaIJEField(153, 1588, 9, "Decedent's Residence - ZIP code", "ZIP9_R", 1);
    private String ZIP9_R;
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
    static MetaIJEField metaCOUNTYTEXT_R = new MetaIJEField(154, 1597, 28, "Decedent's Residence - County", "COUNTYTEXT_R", 1);
    private String COUNTYTEXT_R;
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
    static MetaIJEField metaSTATETEXT_R = new MetaIJEField(155, 1625, 28, "Decedent's Residence - State name", "STATETEXT_R", 1);
    private String STATETEXT_R;
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
    static MetaIJEField metaCOUNTRYTEXT_R = new MetaIJEField(156, 1653, 28, "Decedent's Residence - COUNTRY name", "COUNTRYTEXT_R", 1);
    private String COUNTRYTEXT_R;
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
    static MetaIJEField metaADDRESS_R = new MetaIJEField(157, 1681, 50, "Long String address for decedent's place of residence same as above but allows states to choose the way they capture information.", "ADDRESS_R", 1);
    private String ADDRESS_R;
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
    static MetaIJEField metaRESSTATE = new MetaIJEField(158, 1731, 2, "Old NCHS residence state code", "RESSTATE", 1);
    private String RESSTATE;
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
    static MetaIJEField metaRESCON = new MetaIJEField(159, 1733, 3, "Old NCHS residence city/county combo code", "RESCON", 1);
    private String RESCON;
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
    static MetaIJEField metaSTNUM_R = new MetaIJEField(145, 1485, 10, "Place of death. Decedent's Residence - Street number", "STNUM_R", 1);
    private String STNUM_R;
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
    static MetaIJEField metaPREDIR_R = new MetaIJEField(146, 1495, 10, "Place of death. Decedent's Residence - Pre Directional", "PREDIR_R", 2);
    private String PREDIR_R;
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
    static MetaIJEField metaSTNAME_R = new MetaIJEField(147, 1505, 28, "Place of death. Decedent's Residence - Street Name", "STNAME_R", 3);
    private String STNAME_R;
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
    MetaIJEField metaSTDESIG_R = new MetaIJEField(148, 1533, 10, "Place of death. Decedent's Residence - Street Designator", "STDESIG_R", 4);
    private String STDESIG_R;
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
    static MetaIJEField metaPOSTDIR_R = new MetaIJEField(149, 1543, 10, "Place of death. Decedent's Residence - Post directional", "POSTDIR_R", 5);
    private String POSTDIR_R;
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
    static MetaIJEField metaUNITNUM_R = new MetaIJEField(150, 1553, 7, "Place of death. Decedent's Residence - Unit number", "UNITNUM_R", 6);
    private String UNITNUM_R;
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
    static MetaIJEField metaDETHNICE = new MetaIJEField(160, 1736, 3, "Hispanic", "DETHNICE", 1);
    private String DETHNICE;
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
    static MetaIJEField metaNCHSBRIDGE = new MetaIJEField(161, 1739, 2, "Bridged Race", "NCHSBRIDGE", 1);
    private String NCHSBRIDGE;
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
    static MetaIJEField metaHISPOLDC = new MetaIJEField(162, 1741, 1, "Hispanic - old NCHS single ethnicity codes", "HISPOLDC", 1);
    private String HISPOLDC;
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
    static MetaIJEField metaRACEOLDC = new MetaIJEField(163, 1742, 1, "Race - old NCHS single race codes", "RACEOLDC", 1);
    private String RACEOLDC;
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
    static MetaIJEField metaHISPSTSP = new MetaIJEField(164, 1743, 15, "Hispanic Origin - Specify", "HISPSTSP", 1);
    private String HISPSTSP;
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
    static MetaIJEField metaRACESTSP = new MetaIJEField(165, 1758, 50, "Race - Specify", "RACESTSP", 1);
    private String RACESTSP;
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
    static MetaIJEField metaDMIDDLE = new MetaIJEField(166, 1808, 50, "Middle Name of Decedent", "DMIDDLE", 3);
    private String DMIDDLE;
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
    static MetaIJEField metaDDADF = new MetaIJEField(167, 1858, 50, "Father's First Name", "DDADF", 1);
    private String DDADF;
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
    static MetaIJEField metaDDADMID = new MetaIJEField(168, 1908, 50, "Father's Middle Name", "DDADMID", 2);
    private String DDADMID;
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
    static MetaIJEField metaDMOMF = new MetaIJEField(169, 1958, 50, "Mother's First Name", "DMOMF", 1);
    private String DMOMF;
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
    static MetaIJEField metaDMOMMID = new MetaIJEField(170, 2008, 50, "Mother's Middle Name", "DMOMMID", 2);
    private String DMOMMID;
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
    static MetaIJEField metaDMOMMDN = new MetaIJEField(171, 2058, 50, "Mother's Maiden Surname", "DMOMMDN", 1);
    private String DMOMMDN;
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
    static MetaIJEField metaREFERRED = new MetaIJEField(172, 2108, 1, "Was case Referred to Medical Examiner/Coroner?", "REFERRED", 1);
    private String REFERRED;
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
    static MetaIJEField metaPOILITRL = new MetaIJEField(173, 2109, 50, "Place of Injury- literal", "POILITRL", 1);
    private String POILITRL;
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
    static MetaIJEField metaHOWINJ = new MetaIJEField(174, 2159, 250, "Describe How Injury Occurred", "HOWINJ", 1);
    private String HOWINJ;
    public String getHOWINJ()
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
    static MetaIJEField metaTRANSPRT = new MetaIJEField(175, 2409, 30, "If Transportation Accident, Specify", "TRANSPRT", 1);
    private String TRANSPRT;
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
    static MetaIJEField metaCOUNTYTEXT_I = new MetaIJEField(176, 2439, 28, "County of Injury - literal", "COUNTYTEXT_I", 1);
    private String COUNTYTEXT_I;
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
    static MetaIJEField metaCOUNTYCODE_I = new MetaIJEField(177, 2467, 3, "County of Injury code", "COUNTYCODE_I", 2);
    private String COUNtYCODE_I;
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
    static MetaIJEField metaCITYTEXT_I = new MetaIJEField(178, 2470, 28, "Town/city of Injury - literal", "CITYTEXT_I", 3);
    private String CITYTEXT_I;
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
    static MetaIJEField metaCITYCODE_I = new MetaIJEField(179, 2498, 5, "Town/city of Injury code", "CITYCODE_I", 3);
    private String CITYCODE_I;
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
    static MetaIJEField metaSTATECODE_I = new MetaIJEField(180, 2503, 2, "State, U.S. Territory or Canadian Province of Injury - code", "STATECODE_I", 1);
    private String STATECODE_I;
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
    static MetaIJEField metaLONG_I = new MetaIJEField(181, 2505, 17, "Place of injury. Longitude", "LONG_I", 1);
    private String LONG_I;
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
    static MetaIJEField metaLAT_I = new MetaIJEField(182, 2522, 17, "Place of injury. Latitude", "LAT_I", 1);
    private String LAT_I;
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
    static MetaIJEField metaOLDEDUC = new MetaIJEField(183, 2539, 2, "Old NCHS education code if collected - receiving state will recode as they prefer", "OLDEDUC", 1);
    private String OLDEDUC;
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
    MetaIJEField metaREPLACE = new MetaIJEField(184, 2541, 1, "Replacement Record -- suggested codes", "REPLACE", 1);
    private String REPLACE;
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
    static MetaIJEField metaCOD1A = new MetaIJEField(185, 2542, 120, "Cause of Death Part I Line a", "COD1A", 1);
    private String COD1A;
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
    static MetaIJEField metaINTERVAL1A = new MetaIJEField(186, 2662, 20, "Cause of Death Part I Interval, Line a", "INTERVAL1A", 2);
    private String INTERVAL1A;
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
    static MetaIJEField metaCOD1B = new MetaIJEField(187, 2682, 120, "Cause of Death Part I Line b", "COD1B", 3);
    private String COD1B;
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
    static MetaIJEField metaINTERVAL1B = new MetaIJEField(188, 2802, 20, "Cause of Death Part I Interval, Line b", "INTERVAL1B", 4);
    private String INTERVAL1B;
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
    static MetaIJEField metaCOD1C = new MetaIJEField(189, 2822, 120, "Cause of Death Part I Line c", "COD1C", 5);
    private String COD1C;
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
    static MetaIJEField metaINTERVAL1C = new MetaIJEField(190, 2942, 20, "Cause of Death Part I Interval, Line c", "INTERVAL1C", 6);
    private String INTERVAL1C;
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
    static MetaIJEField metaCOD1D = new MetaIJEField(191, 2962, 120, "Cause of Death Part I Line d", "COD1D", 7);
    private String COD1D;
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
    static MetaIJEField metaINTERVAL1D = new MetaIJEField(192, 3082, 20, "Cause of Death Part I Interval, Line d", "INTERVAL1D", 8);
    private String INTERVAL1D;
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
    static MetaIJEField metaOTHERCONDITION = new MetaIJEField(193, 3102, 240, "Cause of Death Part II", "OTHERCONDITION", 1);
    private String OTHERCONDITION;
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
    static MetaIJEField metaDMAIDEN = new MetaIJEField(194, 3342, 50, "Decedent's Maiden Name", "DMAIDEN", 1);
    private String DMAIDEN;
    public String getDMAIDEN()
    {
        return LeftJustified_Get("DMAIDEN", "MaidenName");
    }
    public void setDMAIDEN(String value)
    {
        LeftJustified_Set("DMAIDEN", "MaidenName", value);
    }

    /// <summary>Decedent's Birth Place City - Code</summary>
    static MetaIJEField metaDBPLACECITYCODE = new MetaIJEField(194, 3392, 5, "Decedent's Birth Place City - Code", "DBPLACECITYCODE", 3);
    private String DBPLACECITYCODE;
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
    static MetaIJEField metaDBPLACECITY = new MetaIJEField(196, 3397, 28, "Decedent's Birth Place City - Literal", "DBPLACECITY", 3);
    private String DBPLACECITY;
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
    static MetaIJEField metaINFORMRELATE = new MetaIJEField(200, 3505, 30, "Informant's Relationship", "INFORMRELATE", 3);
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
    static MetaIJEField metaSPOUSEMIDNAME = new MetaIJEField(197, 3425, 50, "Spouse's Middle Name", "SPOUSEMIDNAME", 2);
    private String SPOUSEMIDNAME;
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
    static MetaIJEField metaSPOUSESUFFIX = new MetaIJEField(198, 3475, 10, "Spouse's Suffix", "SPOUSESUFFIX", 1);
    private String SPOUSESUFFIX;
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
    static MetaIJEField metaFATHERSUFFIX = new MetaIJEField(199, 3485, 10, "Father's Suffix", "FATHERSUFFIX", 1);
    private String FATHERSUFFIX;
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
    static MetaIJEField metaMOTHERSSUFFIX = new MetaIJEField(200, 3495, 10, "Mother's Suffix", "MOTHERSSUFFIX", 1);
    private String MOTHERSUFFIX;
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
    static MetaIJEField metaDISPSTATECD = new MetaIJEField(202, 3535, 2, "State, U.S. Territory or Canadian Province of Disposition - code", "DISPSTATECD", 1);
    private String DISPSTATECD;
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
    static MetaIJEField metaDISPSTATE = new MetaIJEField(203, 3537, 28, "Disposition State or Territory - Literal", "DISPSTATE", 1);
    private String DISPSTATE;
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
    static MetaIJEField metaDISPCITYCODE = new MetaIJEField(204, 3565, 5, "Disposition City - Code", "DISPCITYCODE", 1);
    private String DISPCITYCODE;
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
    static MetaIJEField metaDISPCITY = new MetaIJEField(205, 3570, 28, "Disposition City - Literal", "DISPCITY", 3);
    private String DISPCITY;
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
    static MetaIJEField metaFUNFACNAME = new MetaIJEField(206, 3598, 100, "Funeral Facility Name", "FUNFACNAME", 1);
    private String FUNFACNAME;
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
    static MetaIJEField metaFUNFACSTNUM = new MetaIJEField(207, 3698, 10, "Funeral Facility - Street number", "FUNFACSTNUM", 1);
    private String FUNFACSTNUM;
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
    static MetaIJEField metaFUNFACPREDIR = new MetaIJEField(208, 3708, 10, "Funeral Facility - Pre Directional", "FUNFACPREDIR", 1);
    private String FUNFACPREDIR;
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
    static MetaIJEField metaFUNFACSTRNAME = new MetaIJEField(209, 3718, 28, "Funeral Facility - Street name", "FUNFACSTRNAME", 1);
    private String FUNFACSTRNAME;
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
    static MetaIJEField metaFUNFACSTRDESIG = new MetaIJEField(210, 3746, 10, "Funeral Facility - Street designator", "FUNFACSTRDESIG", 1);
    private String FUNFACSTRDESIG;
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
    static MetaIJEField metaFUNPOSTDIR = new MetaIJEField(211, 3756, 10, "Funeral Facility - Post Directional", "FUNPOSTDIR", 1);
    private String FUNPOSTDIR;
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
    static MetaIJEField metaFUNUNITNUM = new MetaIJEField(212, 3766, 7, "Funeral Facility - Unit or apt number", "FUNUNITNUM", 1);
    private String FUNUNITNUM;
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
    static MetaIJEField metaFUNFACADDRESS = new MetaIJEField(213, 3773, 50, "Long String address for Funeral Facility same as above but allows states to choose the way they capture information.", "FUNFACADDRESS", 1);
    private String FUNFACADDRESS;
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
    static MetaIJEField metaFUNCITYTEXT = new MetaIJEField(214, 3823, 28, "Funeral Facility - City or Town name", "FUNCITYTEXT", 3);
    private String FUNCITYTEXT;
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
    static MetaIJEField metaFUNSTATECD = new MetaIJEField(215, 3851, 2, "State, U.S. Territory or Canadian Province of Funeral Facility - code", "FUNSTATECD", 1);
    private String FUNSTATECD;
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
    static MetaIJEField metaFUNSTATE = new MetaIJEField(216, 3853, 28, "State, U.S. Territory or Canadian Province of Funeral Facility - literal", "FUNSTATE", 1);
    private String FUNSTATE;
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
    static MetaIJEField metaFUNZIP = new MetaIJEField(217, 3881, 9, "Funeral Facility - ZIP", "FUNZIP", 1);
    private String FUNZIP;
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
    static MetaIJEField metaPPDATESIGNED = new MetaIJEField(218, 3890, 8, "Person Pronouncing Date Signed", "PPDATESIGNED", 1);
    private String PPDATESIGNED;
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
    static MetaIJEField metaPPTIME = new MetaIJEField(219, 3898, 4, "Person Pronouncing Time Pronounced", "PPTIME", 1);
    private String PPTIME;
    public String getPPTIME()
    {
        String fhirTimeStr = record.getDateOfDeathPronouncementTime();
        if (fhirTimeStr == null)
        {
            return "    ";
        }
        else
        {
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
    static MetaIJEField metaCERTFIRST = new MetaIJEField(220, 3902, 50, "Certifier's First Name", "CERTFIRST", 1);
    private String CERTFIRST;
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
    static MetaIJEField metaCERTMIDDLE = new MetaIJEField(221, 3952, 50, "Certifier's Middle Name", "CERTMIDDLE", 2);
    private String CERTMIDDLE;
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
    static MetaIJEField metaCERTLAST = new MetaIJEField(222, 4002, 50, "Certifier's Last Name", "CERTLAST", 3);
    private String CERTLAST;
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
    static MetaIJEField metaCERTSUFFIX = new MetaIJEField(223, 4052, 10, "Certifier's Suffix Name", "CERTSUFFIX", 4);
    private String CERTSUFFIX;
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
    static MetaIJEField metaCERTSTNUM = new MetaIJEField(224, 4062, 10, "Certifier - Street number", "CERTSTNUM", 1);
    private String CERTSTNUM;
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
    static MetaIJEField metaCERTPREDIR = new MetaIJEField(225, 4072, 10, "Certifier - Pre Directional", "CERTPREDIR", 1);
    private String CERTPREDIR;
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
    static MetaIJEField metaCERTSTRNAME = new MetaIJEField(226, 4082, 28, "Certifier - Street name", "CERTSTRNAME", 1);
    private String CERTSTRNAME;
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
    static MetaIJEField metaCERTSTRDESIG = new MetaIJEField(227, 4110, 10, "Certifier - Street designator", "CERTSTRDESIG", 1);
    private String CERTSTRDESIG;
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
    static MetaIJEField metaCERTPOSTDIR = new MetaIJEField(228, 4120, 10, "Certifier - Post Directional", "CERTPOSTDIR", 1);
    private String CERTPOSTDIR;
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
    static MetaIJEField metaCERTUNITNUM = new MetaIJEField(229, 4130, 7, "Certifier - Unit or apt number", "CERTUNITNUM", 1);
    private String CERTUNITNUM;
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
    static MetaIJEField metaCERTADDRESS = new MetaIJEField(230, 4137, 50, "Long String address for Certifier same as above but allows states to choose the way they capture information.", "CERTADDRESS", 1);
    private String CERTADDRESS;
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
    static MetaIJEField metaCERTCITYTEXT = new MetaIJEField(231, 4187, 28, "Certifier - City or Town name", "CERTCITYTEXT", 2);
    private String CERTCITYTEXT;
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
    static MetaIJEField metaCERTSTATECD = new MetaIJEField(232, 4215, 2, "State, U.S. Territory or Canadian Province of Certifier - code", "CERTSTATECD", 1);
    private String CERTSTATECD;
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
    static MetaIJEField metaCERTSTATE = new MetaIJEField(233, 4217, 28, "State, U.S. Territory or Canadian Province of Certifier - literal", "CERTSTATE", 1);
    private String CERTSTATE;
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
    static MetaIJEField metaCERTZIP = new MetaIJEField(234, 4245, 9, "Certifier - Zip", "CERTZIP", 1);
    private String CERTZIP;
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
    static MetaIJEField metaCERTDATE = new MetaIJEField(235, 4254, 8, "Certifier Date Signed", "CERTDATE", 1);
    private String CERTDATE;
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
    static MetaIJEField metaFILEDATE = new MetaIJEField(236, 4262, 8, "Date Filed", "FILEDATE", 1);
    private String FILEDATE;
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
    static MetaIJEField metaSTINJURY = new MetaIJEField(237, 4270, 28, "State, U.S. Territory or Canadian Province of Injury - literal", "STINJURY", 1);
    private String STINJURY;
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
    static MetaIJEField metaSTATEBTH = new MetaIJEField(238, 4298, 28, "State, U.S. Territory or Canadian Province of Birth - literal", "STATEBTH", 1);
    private String STATEBTH;
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
    static MetaIJEField metaDTHCOUNTRYCD = new MetaIJEField(239, 4326, 2, "Country of Death - Code", "DTHCOUNTRYCD", 1);
    private String DTHCOUNTRYCD;
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
    static MetaIJEField metaDTHCOUNTRY = new MetaIJEField(240, 4328, 28, "Country of Death - Literal", "DTHCOUNTRY", 1);
    private String DTHCOUNTRY;
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
    static MetaIJEField metaSSADTHCODE = new MetaIJEField(241, 4356, 3, "SSA State Source of Death", "SSADTHCODE", 1);
    private String SSADTHCODE;
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
    static MetaIJEField metaSSAFOREIGN = new MetaIJEField(242, 4359, 1, "SSA Foreign Country Indicator", "SSAFOREIGN", 1);
    private String SSAFOREIGN;
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
    static MetaIJEField metaSSAVERIFY = new MetaIJEField(243, 4360, 1, "SSA EDR Verify Code", "SSAVERIFY", 1);
    private String SSAVERIFY;
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
    static MetaIJEField metaSSADATEVER = new MetaIJEField(244, 4361, 8, "SSA Date of SSN Verification", "SSADATEVER", 1);
    private String SSADATEVER;
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
    static MetaIJEField metaSSADATETRANS = new MetaIJEField(245, 4369, 8, "SSA Date of State Transmission", "SSADATETRANS", 1);
    private String SSADATETRANS;
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
    static MetaIJEField metaDETHNIC5C = new MetaIJEField(247, 4427, 3, "Hispanic Code for Literal", "DETHNIC5C", 1);
    private String DETHNIC5C;
    public String getDETHNIC5C()
    {
        return Get_MappingFHIRToIJE(Mappings.HispanicOrigin.FHIRToIJE, "HispanicCodeForLiteral", "DETHNIC5C");
    }
    public void setDETHNIC5C(String value)
    {
        Set_MappingIJEToFHIR(Mappings.HispanicOrigin.IJEToFHIR, "DETHNIC5C", "HispanicCodeForLiteral", value);
    }

    /// <summary>Blank for One-Byte Field 1</summary>
    static MetaIJEField metaPLACE1_1 = new MetaIJEField(248, 4430, 1, "Blank for One-Byte Field 1", "PLACE1_1", 1);
    private String PLACE1_1;
    public String getPLACE1_1()
    {
        return LeftJustified_Get("PLACE1_1", "EmergingIssue1_1");
    }
    public void setPLACE1_1(String value)
    {
        LeftJustified_Set("PLACE1_1", "EmergingIssue1_1", value);
    }

    /// <summary>Blank for One-Byte Field 2</summary>
    static MetaIJEField metaPLACE1_2 = new MetaIJEField(249, 4431, 1, "Blank for One-Byte Field 2", "PLACE1_2", 1);
    private String PLACE1_2;
    public String getPLACE1_2()
    {
        return LeftJustified_Get("PLACE1_2", "EmergingIssue1_2");
    }
    public void setPLACE1_2(String value)
    {
        LeftJustified_Set("PLACE1_2", "EmergingIssue1_2", value);
    }

    /// <summary>Blank for One-Byte Field 3</summary>
    static MetaIJEField metaPLACE1_3 = new MetaIJEField(250, 4432, 1, "Blank for One-Byte Field 3", "PLACE1_3", 1);
    private String PLACE1_3;
    public String getPLACE1_3()
    {
        return LeftJustified_Get("PLACE1_3", "EmergingIssue1_3");
    }
    public void setPLACE1_3(String value)
    {
        LeftJustified_Set("PLACE1_3", "EmergingIssue1_3", value);
    }

    /// <summary>Blank for One-Byte Field 4</summary>
    static MetaIJEField metaPLACE1_4 = new MetaIJEField(251, 4433, 1, "Blank for One-Byte Field 4", "PLACE1_4", 1);
    private String PLACE1_4;
    public String getPLACE1_4()
    {
        return LeftJustified_Get("PLACE1_4", "EmergingIssue1_4");
    }
    public void setPLACE1_4(String value)
    {
        LeftJustified_Set("PLACE1_4", "EmergingIssue1_4", value);
    }

    /// <summary>Blank for One-Byte Field 5</summary>
    static MetaIJEField metaPLACE1_5 = new MetaIJEField(252, 4434, 1, "Blank for One-Byte Field 5", "PLACE1_5", 1);
    private String PLACE1_5;
    public String getPLACE1_5()
    {
        return LeftJustified_Get("PLACE1_5", "EmergingIssue1_5");
    }
    public void setPLACE1_5(String value)
    {
        LeftJustified_Set("PLACE1_5", "EmergingIssue1_5", value);
    }

    /// <summary>Blank for One-Byte Field 6</summary>
    static MetaIJEField metaPLACE1_6 = new MetaIJEField(253, 4435, 1, "Blank for One-Byte Field 6", "PLACE1_6", 1);
    private String PLACE1_6;
    public String getPLACE1_6()
    {
        return LeftJustified_Get("PLACE1_6", "EmergingIssue1_6");
    }
    public void setgetPLACE1_6(String value)
    {
        LeftJustified_Set("PLACE1_6", "EmergingIssue1_6", value);
    }

    /// <summary>Blank for Eight-Byte Field 1</summary>
    static MetaIJEField metaPLACE8_1 = new MetaIJEField(254, 4436, 8, "Blank for Eight-Byte Field 1", "PLACE8_1", 1);
    private String PLACE8_1;
    public String getPLACE8_1()
    {
        return LeftJustified_Get("PLACE8_1", "EmergingIssue8_1");
    }
    public void setPLACE8_1(String value)
    {
        LeftJustified_Set("PLACE8_1", "EmergingIssue8_1", value);
    }

    /// <summary>Blank for Eight-Byte Field 2</summary>
    static MetaIJEField metaPLACE8_2 = new MetaIJEField(255, 4444, 8, "Blank for Eight-Byte Field 2", "PLACE8_2", 1);
    private String PLACE8_2;
    public String getPLACE8_2()
    {
        return LeftJustified_Get("PLACE8_2", "EmergingIssue8_2");
    }
    public void setPLACE8_2(String value)
    {
        LeftJustified_Set("PLACE8_2", "EmergingIssue8_2", value);
    }

    /// <summary>Blank for Eight-Byte Field 3</summary>
    static MetaIJEField metaPLACE8_3 = new MetaIJEField(256, 4452, 8, "Blank for Eight-Byte Field 3", "PLACE8_3", 1);
    private String PLACE8_3;
    public String getPLACE8_3()
    {
        return LeftJustified_Get("PLACE8_3", "EmergingIssue8_3");
    }
    public void setPLACE8_3(String value)
    {
        LeftJustified_Set("PLACE8_3", "EmergingIssue8_3", value);
    }

    /// <summary>Blank for Twenty-Byte Field</summary>
    static MetaIJEField metaPLACE20 = new MetaIJEField(257, 4460, 20, "Blank for Twenty-Byte Field", "PLACE20", 1);
    private String PLACE20;
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




