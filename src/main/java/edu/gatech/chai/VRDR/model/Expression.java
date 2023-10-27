package edu.gatech.chai.VRDR.model;

import org.hl7.fhir.r4.elementmodel.Element;
import org.hl7.fhir.r4.elementmodel.Types;
import org.hl7.fhir.r4.elementmodel.ObjectConverter;
import org.hl7.fhir.r4.elementmodel.Property;
import org.hl7.fhir.r4.model.Base;
//import org.hl7.fhir.r4.model.TypeSpecifier;
//import org.hl7.fhir.r4.utils.exceptions.FHIRException;

public abstract class Expression implements IEquatable<Expression> {
    private static final String OP_PREFIX = "builtin.";
    private static final int OP_PREFIX_LEN = OP_PREFIX.length();

    protected TypeSpecifier expressionType;

    protected Expression(TypeSpecifier type) {
        expressionType = type;
    }

    protected Expression(TypeSpecifier type, ISourcePositionInfo location) {
        expressionType = type;
        // Location = location;
    }

    public TypeSpecifier getExpressionType() {
        return expressionType;
    }

    public abstract <T> T accept(ExpressionVisitor<T> visitor, SymbolTable scope);

    @Override
    public boolean equals(Object obj) {
        return equals(obj as Expression);
    }

    public boolean equals(Expression other) {
        return other != null && expressionType.equals(other.expressionType);
    }

    @Override
    public int hashCode() {
        return -28965461 + expressionType.hashCode();
    }

    public static boolean equals(Expression left, Expression right) {
        return left.equals(right);
    }

    public static boolean notEquals(Expression left, Expression right) {
        return !left.equals(right);
    }
}

public class ConstantExpression extends Expression {
    private Object value;

    public ConstantExpression(Object value, TypeSpecifier type, ISourcePositionInfo location) {
        super(type, location);
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        if (value instanceof Types.Any && (value instanceof Types.Boolean || value instanceof Types.Decimal || value instanceof Types.Integer || value instanceof Types.Long || value instanceof Types.String)) {
            throw new IllegalArgumentException("Internal error: not yet ready to handle Any-based primitives in FhirPath.");
        }
        this.value = value;
    }

    public ConstantExpression(Object value, ISourcePositionInfo location) {
        super(TypeSpecifier.Any, location);
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        if (ElementNode.tryConvertToElementValue(value, systemValue)) {
            this.value = systemValue;
            expressionType = TypeSpecifier.forNativeType(value.getClass());
        } else {
            throw new IllegalStateException("Internal logic error: encountered unmappable Value of type " + value.getClass().getName());
        }
    }

    public Object getValue() {
        return value;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor, SymbolTable scope) {
        return visitor.visitConstant(this, scope);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj) && obj instanceof ConstantExpression) {
            ConstantExpression ce = (ConstantExpression) obj;
            return Objects.equals(ce.value, value);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ value.hashCode();
    }
}

