package edu.gatech.chai.VRDR.model;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.hl7.fhir.r4.model.Enumerations.FHIRAllTypes;
import org.hl7.fhir.r4.model.CodeType;
//import org.hl7.fhir.r4.model.TypeSpecifier;
import org.hl7.fhir.r4.model.codesystems.ConceptPropertyType;
import org.hl7.fhir.r4.utils.CodingUtilities.*;
import org.hl7.fhir.r4.model.*;



public class TypeSpecifier implements Comparable<TypeSpecifier> {
    public static final String SYSTEM_NAMESPACE = "System";
    public static final String DOTNET_NAMESPACE = "DotNet";

    public static final TypeSpecifier Any = new TypeSpecifier(SYSTEM_NAMESPACE, "Any");
    public static final TypeSpecifier Boolean = new TypeSpecifier(SYSTEM_NAMESPACE, "Boolean");
    public static final TypeSpecifier Code = new TypeSpecifier(SYSTEM_NAMESPACE, "Code");
    public static final TypeSpecifier Concept = new TypeSpecifier(SYSTEM_NAMESPACE, "Concept");
    public static final TypeSpecifier Date = new TypeSpecifier(SYSTEM_NAMESPACE, "Date");
    public static final TypeSpecifier DateTime = new TypeSpecifier(SYSTEM_NAMESPACE, "DateTime");
    public static final TypeSpecifier Decimal = new TypeSpecifier(SYSTEM_NAMESPACE, "Decimal");
    public static final TypeSpecifier Integer = new TypeSpecifier(SYSTEM_NAMESPACE, "Integer");
    public static final TypeSpecifier Long = new TypeSpecifier(SYSTEM_NAMESPACE, "Long");
    public static final TypeSpecifier Quantity = new TypeSpecifier(SYSTEM_NAMESPACE, "Quantity");
    public static final TypeSpecifier Ratio = new TypeSpecifier(SYSTEM_NAMESPACE, "Ratio");
    public static final TypeSpecifier String = new TypeSpecifier(SYSTEM_NAMESPACE, "String");
    public static final TypeSpecifier Time = new TypeSpecifier(SYSTEM_NAMESPACE, "Time");
    public static final TypeSpecifier Void = new TypeSpecifier(SYSTEM_NAMESPACE, "Void");

    public static final TypeSpecifier[] AllTypes = new TypeSpecifier[] { Any, Boolean, Code, Concept, Date, DateTime, Decimal, Integer, Long, Quantity, Ratio, String, Time };
    public static final TypeSpecifier[] PrimitiveTypes = new TypeSpecifier[] { Boolean, Code, Date, DateTime, Decimal, Integer, Long, String, Time };

    private String namespace;
    private String name;

    public TypeSpecifier(String namespace, String name) {
        this.namespace = Objects.requireNonNull(namespace, "namespace must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public static TypeSpecifier getByName(String typeName) {
        return getByName(SYSTEM_NAMESPACE, typeName);
    }

    public static TypeSpecifier getByName(String namespace, String typeName) {
        if (namespace == null) {
            throw new NullPointerException("namespace must not be null");
        }
        if (typeName == null) {
            throw new NullPointerException("typeName must not be null");
        }

        switch (typeName) {
            case "Any":
                return Any;
            case "Boolean":
                return Boolean;
            case "Code":
                return Code;
            case "Concept":
                return Concept;
            case "Date":
                return Date;
            case "DateTime":
                return DateTime;
            case "Decimal":
                return Decimal;
            case "Integer":
                return Integer;
            case "Long":
                return Long;
            case "Quantity":
                return Quantity;
            case "Ratio":
                return Ratio;
            case "String":
                return String;
            case "Time":
                return Time;
            case "Void":
                return Void;
            default:
                return new TypeSpecifier(namespace, typeName);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TypeSpecifier)) {
            return false;
        }
        TypeSpecifier other = (TypeSpecifier) obj;
        return Objects.equals(namespace, other.namespace) && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }

    @Override
    public String toString() {
        return namespace + "." + name;
    }

    @Override
    public int compareTo(TypeSpecifier o) {
        int result = namespace.compareTo(o.namespace);
        if (result == 0) {
            result = name.compareTo(o.name);
        }
        return result;
    }


    protected void setName(String name) {
        this.name = name;
    }


    protected void setNamespace(String namespace) {
        this.namespace = namespace;
    }


    public String getFullName() {
        return java.lang.String.format("%s.%s", escape(getNamespace()), escape(getName()));
    }

    private static String escape(String spec) {
        if (!spec.contains(".") && !spec.contains("`")) {
            return spec;
        }

        spec = spec.replace("`", "\\`");
        return java.lang.String.format("`%s`", spec);
    }

    public static TypeSpecifier forNativeType(Class<?> dotNetType) {
        if (dotNetType == null) {
            throw new IllegalArgumentException("dotNetType cannot be null");
        }

        if (FHIRAllTypes.BOOLEAN.getClass().isAssignableFrom(dotNetType) || dotNetType == boolean.class || dotNetType == java.lang.Boolean.class || dotNetType == BooleanType.class) {
            return Boolean;
        } else if (FHIRAllTypes.INTEGER.getClass().isAssignableFrom(dotNetType) || dotNetType == int.class || dotNetType == short.class || dotNetType == Short.class || dotNetType == Integer.class || dotNetType == IntegerType.class) {
            return Integer;
        } else if (dotNetType == long.class || dotNetType == Long.class) {
            return Long;
        } else if (FHIRAllTypes.TIME.getClass().isAssignableFrom(dotNetType) || TimeType.class == dotNetType) {
            return Time;
        } else if (FHIRAllTypes.DATE.getClass().isAssignableFrom(dotNetType) || DateType.class.isAssignableFrom(dotNetType)) {
            return Date;
        } else if (FHIRAllTypes.DATETIME.getClass().isAssignableFrom(dotNetType) || DateTimeType.class.isAssignableFrom(dotNetType) || dotNetType == OffsetDateTime.class) {
            return DateTime;
        } else if (FHIRAllTypes.DECIMAL.getClass().isAssignableFrom(dotNetType) || dotNetType == float.class || dotNetType == Float.class || dotNetType == double.class || dotNetType == Double.class || dotNetType == BigDecimal.class) {
            return Decimal;
        } else if (FHIRAllTypes.STRING.getClass().isAssignableFrom(dotNetType) || dotNetType == String.class || dotNetType == char.class || dotNetType == URI.class) {
            return String;
        } else if (FHIRAllTypes.QUANTITY.getClass().isAssignableFrom(dotNetType)) {
            return Quantity;
        } else if (FHIRAllTypes.RATIO.getClass().isAssignableFrom(dotNetType)) {
            return Ratio;
        } else if (FHIRAllTypes.CODE.getClass().isAssignableFrom(dotNetType) || FHIRAllTypes.CODING.getClass().isAssignableFrom(dotNetType) || CodeType.class.isAssignableFrom(dotNetType) || Enum.class.isAssignableFrom(dotNetType)) {
            return Code;
        } else if (FHIRAllTypes.CODEABLECONCEPT.getClass().isAssignableFrom(dotNetType)) {
            return Concept;
        } else if (FHIRAllTypes.ANY.getClass().isAssignableFrom(dotNetType) || Object.class.isAssignableFrom(dotNetType)) {
            return Any;
        } else {
            return getByName(DOTNET_NAMESPACE, dotNetType.getName());
        }
    }
}

