package edu.gatech.chai.VRDR.model;
import org.hl7.*;
import org.fhir.*;
import org.hl7.fhir.r4.model.Expression;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class FhirPathCompiler {
    private static final Lazy<SymbolTable> _defaultSymbolTable = new Lazy<>(() -> new SymbolTable().addStandardFhirPath());

    public static void setDefaultSymbolTable(Lazy<SymbolTable> st) {
        // Do nothing
    }

    public static SymbolTable getDefaultSymbolTable() {
        return _defaultSymbolTable.get();
    }

    private final SymbolTable symbols;

    public FhirPathCompiler(SymbolTable symbols) {
        this.symbols = symbols;
    }

    public FhirPathCompiler() {
        this(getDefaultSymbolTable());
    }

    public Expression parse(String expression) {
        ParseResult<Expression> parse = Grammar.expression.end().tryParse(expression);
        if (parse.wasSuccessful()) {
            return parse.get();
        } else {
            throw new IllegalArgumentException("Compilation failed: " + parse.toString());
        }
    }

    public CompiledExpression compile(Expression expression) {
        Invokee inv = expression.toEvaluator(symbols);
        return (TypedElemental focus, EvaluationContext ctx) -> {
            Closure closure = Closure.root(focus, ctx);
            return inv.invoke(closure, InvokeeFactory.emptyArgs());
        };
    }

    public CompiledExpression compile(String expression) {
        return compile(parse(expression));
    }
}
