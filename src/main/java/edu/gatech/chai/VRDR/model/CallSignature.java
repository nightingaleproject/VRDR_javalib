package edu.gatech.chai.VRDR.model;

import java.util.List;
import java.util.Arrays;

public class CallSignature {
    private String name;
    private List<Class<?>> argumentTypes;
    private Class<?> returnType;

    public CallSignature(String name, Class<?> returnType, Class<?>... argumentTypes) { //Class[] argumentTypes
        this.name = name;
        this.argumentTypes = Arrays.asList(argumentTypes);
        this.returnType = returnType;
    }


    public boolean dynamicMatches(String functionName, List<Object> arguments) {
        if (!functionName.equals(name) || arguments.size() != argumentTypes.size()) {
            return false;
        }

        for (int i = 0; i < arguments.size(); i++) {
            if (!Typecasts.CanCastTo(arguments.get(i), argumentTypes.get(i))) {
                return false;
            }
        }

        return true;
    }

    public boolean dynamicExactMatches(String functionName, List<Object> arguments) {
        if (!functionName.equals(name) || arguments.size() != argumentTypes.size()) {
            return false;
        }

        for (int i = 0; i < arguments.size(); i++) {
            if (!Typecasts.isOfExactType(arguments.get(i), argumentTypes.get(i))) {
                return false;
            }
        }

        return true;
    }

    public boolean matches(String functionName, int argCount) {
        return functionName.equals(name) && argumentTypes.size() == argCount;
    }

    public String getName() {
        return name;
    }

    public List<Class<?>> getArgumentTypes() {
        return argumentTypes;
    }

    public Class<?> getReturnType() {
        return returnType;
    }
}
class CallSignature1
{
    public String Name;
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }


    public Class[] ArgumentTypes;
    public Class[] getArgumentTypes() {
        return ArgumentTypes;
    }

    public void setArgumentTypes(Class[] argumentTypes) {
        ArgumentTypes = argumentTypes;
    }

    public Class ReturnType;
    public Class getReturnType() {
        return ReturnType;
    }

    public void setReturnType(Class returnType) {
        ReturnType = returnType;
    }


    public boolean Matches(String functionName, int argCount)
    {
        return functionName == Name && ArgumentTypes.length == argCount;
    }
}
