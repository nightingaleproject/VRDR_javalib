package edu.gatech.chai.VRDR.model;

import java.util.List;

public class CallSignature
{
    public String Name { get; private set; }

    public Class[] ArgumentTypes { get; private set; }

    public Class ReturnType { get; private set; }

    public CallSignature(String name, Class returnType, params Class[] argTypes )
    {
        Name = name;
        ArgumentTypes = argTypes;
        ReturnType = returnType;
    }

    //public boolean Matches(String functionName, Iterable <Type> argumentTypes)
    //{
    //    return functionName == Name && argumentTypes.length == ArgumentTypes.length &&
    //           argumentTypes.Zip(ArgumentTypes, (call, sig) -> Typecasts.CanCastTo(call,sig)).All(r -> r == true);
    //}

    public boolean DynamicMatches(String functionName, List<Object> arguments)
    {
        return functionName == Name && arguments.size() == ArgumentTypes.length &&
                arguments.Zip(ArgumentTypes, (call, sig) -> Typecasts.CanCastTo(call, sig)).All(r -> r == true);
    }
    public boolean DynamicExactMatches(String functionName, List<Object> arguments)
    {
        return functionName == Name && arguments.size() == ArgumentTypes.length &&
                arguments.Zip(ArgumentTypes, (call, sig) -> Typecasts.IsOfExactType(call, sig)).All(r -> r == true);
    }

    public boolean Matches(String functionName, int argCount)
    {
        return functionName == Name && ArgumentTypes.length == argCount;
    }
}
