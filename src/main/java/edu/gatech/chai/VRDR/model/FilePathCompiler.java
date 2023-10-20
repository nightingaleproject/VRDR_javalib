package edu.gatech.chai.VRDR.model;
import org.hl7.*;
import org.fhir.*;

public class FhirPathCompiler
{
    private static Lazy<SymbolTable> _defaultSymbolTable = new(() => new SymbolTable().AddStandardFP());

    public static void SetDefaultSymbolTable(Lazy<SymbolTable> st)
    {
        _defaultSymbolTable = st;
    }

    public static SymbolTable getDefaultSymbolTable() {
        return _defaultSymbolTable;
    }

//    public static SymbolTable DefaultSymbolTable
//    {
//        get { return _defaultSymbolTable.Value; }
//    }

    public SymbolTable Symbols;// { get; private set; }

    public SymbolTable getSymbols() {
        return Symbols;
    }

    private void setSymbols(SymbolTable symbols) {
        Symbols = symbols;
    }



    public FhirPathCompiler(SymbolTable symbols)
    {
        Symbols = symbols;
    }

    public FhirPathCompiler() : this(DefaultSymbolTable)
    {
    }

//#pragma warning disable CA1822 // Mark members as static
    public Expression Parse(String expression)
//#pragma warning restore CA1822 // This might access instance data in the future.
    {
        var parse = Grammar.Expression.End().TryParse(expression);

        return parse.WasSuccessful ? parse.Value : throw new FormatException("Compilation failed: " + parse.ToString());
    }

    public CompiledExpression Compile(Expression expression)
    {
        Invokee inv = expression.ToEvaluator(Symbols);

        return (TypedElemental focus, EvaluationContext ctx) =>
        {
            var closure = Closure.Root(focus, ctx);
            return inv(closure, InvokeeFactory.EmptyArgs);
        };
    }

    public CompiledExpression Compile(String expression)
    {
        return Compile(Parse(expression));
    }
}
