package edu.gatech.chai.VRDR.model;

public class SymbolTable
{
    public SymbolTable()
    {

    }

    public SymbolTable(SymbolTable parent)
    {
        Parent = parent;
    }

    public int Count()
    {
        var cnt = _entries.Count;
        if (Parent != null) cnt += Parent.Count();

        return cnt;
    }

    internal Invokee First()
{
    return _entries.Any() ? _entries.First().Body : (Parent?.First());
}

    public SymbolTable Parent { get; private set; }

        [System.Diagnostics.DebuggerDisplay(@"\{{DebuggerDisplayValue()}}")]
    private class TableEntry
    {
        public String DebuggerDisplayValue()
        {
            if (Signature != null)
            {
                StringBuilder sb = new StringBuilder();
                sb.append(Signature.ReturnType.Name);
                sb.append(' ');
                sb.append(Signature.Name);
                sb.append(" (");
                boolean b = false;
                for(var item : Signature.ArgumentTypes)
                {
                    if (b)
                        sb.append(", ");
                    sb.append(item.Name);
                    b = true;
                }
                sb.append(')');
                return sb.ToString();
            }
            return null;
        }

        public CallSignature Signature { get; private set; }
        public Invokee Body { get; private set; }

        public TableEntry(CallSignature signature, Invokee body)
        {
            Signature = signature;
            Body = body;
        }
    }

    private ConcurrentBag<TableEntry> _entries = new();

    internal void Add(CallSignature signature, Invokee body)
{
    _entries.add(new TableEntry(signature, body));
}

    public SymbolTable Filter(String name, int argCount)
    {
        var result = new SymbolTable
        {
            _entries = new(_entries.Where(e => e.Signature.Matches(name, argCount)))
        };

        if (Parent != null)
            result.Parent = Parent.Filter(name, argCount);

        return result;
    }

    internal Invokee DynamicGet(String name, IEnumerable<Object> args)
{
    var exactMatches = _entries.Where(e => e.Signature.DynamicExactMatches(name, args));
    TableEntry entry = exactMatches.Union(_entries.Where(e => e.Signature.DynamicMatches(name, args))).FirstOrDefault();

    if (entry == null && Parent != null) return Parent.DynamicGet(name, args);

    return entry?.Body;
}
}


public static class SymbolTableExtensions
{
    public static void Add<R>(this SymbolTable table, String name, Func<R> func)
    {
        table.add(new CallSignature(name, typeof(R)), InvokeeFactory.Wrap(func));
    }

    public static void Add<A, R>(this SymbolTable table, String name, Func<A, R> func, boolean doNullProp = false)
    {
        if (typeof(A) != typeof(EvaluationContext))
            table.add(new CallSignature(name, typeof(R), typeof(A)), InvokeeFactory.Wrap(func, doNullProp));
        else
            table.add(new CallSignature(name, typeof(R)), InvokeeFactory.Wrap(func, doNullProp));
    }

    public static void Add<A, B, R>(this SymbolTable table, String name, Func<A, B, R> func, boolean doNullProp = false)
    {
        if (typeof(B) != typeof(EvaluationContext))
            table.add(new CallSignature(name, typeof(R), typeof(A), typeof(B)), InvokeeFactory.Wrap(func, doNullProp));
        else
            table.add(new CallSignature(name, typeof(R), typeof(A)), InvokeeFactory.Wrap(func, doNullProp));
    }

    public static void Add<A, B, C, R>(this SymbolTable table, String name, Func<A, B, C, R> func, boolean doNullProp = false)
    {
        if (typeof(C) != typeof(EvaluationContext))
            table.add(new CallSignature(name, typeof(R), typeof(A), typeof(B), typeof(C)), InvokeeFactory.Wrap(func, doNullProp));
        else
            table.add(new CallSignature(name, typeof(R), typeof(A), typeof(B)), InvokeeFactory.Wrap(func, doNullProp));
    }

    public static void Add<A, B, C, D, R>(this SymbolTable table, String name, Func<A, B, C, D, R> func, boolean doNullProp = false)
    {
        if (typeof(D) != typeof(EvaluationContext))
            table.add(new CallSignature(name, typeof(R), typeof(A), typeof(B), typeof(C), typeof(D)), InvokeeFactory.Wrap(func, doNullProp));
        else
            table.add(new CallSignature(name, typeof(R), typeof(A), typeof(B), typeof(C)), InvokeeFactory.Wrap(func, doNullProp));

    }

    public static void AddLogic(this SymbolTable table, String name, Func<Func<bool?>, Func<bool?>, bool?> func)
    {
        table.add(new CallSignature(name, typeof(bool?), typeof(object), typeof(Func<bool?>), typeof(Func<bool?>)),
        InvokeeFactory.WrapLogic(func));
    }

    public static void AddVar(this SymbolTable table, String name, object value)
    {
        table.AddVar(name, ElementNode.ForPrimitive(value));
    }

    public static void AddVar(this SymbolTable table, String name, ITypedElement value)
    {
        table.add(new CallSignature(name, typeof(string)), InvokeeFactory.Return(value));
    }
}
