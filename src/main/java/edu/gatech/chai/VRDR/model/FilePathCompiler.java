package edu.gatech.chai.VRDR.model;

public class FhirPathCompiler
{
    private static Lazy<SymbolTable> _defaultSymbolTable = new(() => new SymbolTable().AddStandardFP());

    public static void SetDefaultSymbolTable(Lazy<SymbolTable> st)
    {
        _defaultSymbolTable = st;
    }

    public static SymbolTable DefaultSymbolTable
    {
        get { return _defaultSymbolTable.Value; }
    }

    public SymbolTable Symbols { get; private set; }

    public FhirPathCompiler(SymbolTable symbols)
    {
        Symbols = symbols;
    }

    public FhirPathCompiler() : this(DefaultSymbolTable)
    {
    }

//#pragma warning disable CA1822 // Mark members as static
    public Expression Parse(string expression)
//#pragma warning restore CA1822 // This might access instance data in the future.
    {
        var parse = Grammar.Expression.End().TryParse(expression);

        return parse.WasSuccessful ? parse.Value : throw new FormatException("Compilation failed: " + parse.ToString());
    }

    public CompiledExpression Compile(Expression expression)
    {
        Invokee inv = expression.ToEvaluator(Symbols);

        return (ITypedElement focus, EvaluationContext ctx) =>
        {
            var closure = Closure.Root(focus, ctx);
            return inv(closure, InvokeeFactory.EmptyArgs);
        };
    }

    public CompiledExpression Compile(string expression)
    {
        return Compile(Parse(expression));
    }
}
