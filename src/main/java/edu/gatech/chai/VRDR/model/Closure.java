package edu.gatech.chai.VRDR.model;


import java.util.*;

public class Closure {

    private EvaluationContext evaluationContext;
    private Closure parent;
    private Map<String, List<TypedElemental>> namedValues = new HashMap<>();

    public Closure() {
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }

    public void setEvaluationContext(EvaluationContext evaluationContext) {
        this.evaluationContext = evaluationContext;
    }

    public static List input = new ArrayList();
    public static List resources = new ArrayList();
    public static List rootResources = new ArrayList();
    public static Closure root(TypedElemental root, EvaluationContext ctx) {
        Closure newContext = new Closure();
        newContext.setEvaluationContext(ctx != null ? ctx : EvaluationContext.createDefault());

        input.add(root);//new SingleObjectIterator<>(root);
        newContext.setThis(input);
        newContext.setThat(input);
        newContext.setIndex(input);//new SingleObjectIterator<>(new Integer(0)));
        newContext.setOriginalContext(input);

        if (ctx != null && ctx.getResource() != null) {
            resources.add(ctx.getResource());
            newContext.setResource(resources);
        }

        if (ctx != null && ctx.getRootResource() != null) {
            rootResources.add(ctx.getResource());
            newContext.setRootResource(rootResources);
        }

        return newContext;
    }

    public void setValue(String name, List<TypedElemental> value) {
        namedValues.put(name, value);
    }

    public Closure getParent() {
        return parent;
    }

    public void setParent(Closure parent) {
        this.parent = parent;
    }

    public Closure nest() {
        Closure newClosure = new Closure();
        newClosure.setParent(this);
        newClosure.setEvaluationContext(this.evaluationContext);
        return newClosure;
    }

    public List<TypedElemental> resolveValue(String name) {
        List<TypedElemental> result = namedValues.get(name);

        if (result != null) {
            return result;
        }

        if (parent != null) {
            return parent.resolveValue(name);
        }

        return null;
    }

    public void setThis(List<TypedElemental> value) {
        setValue("this", value);
    }

    public void setThat(List<TypedElemental> value) {
        setValue("that", value);
    }

    public void setIndex(List<TypedElemental> value) {
        setValue("index", value);
    }

    public void setOriginalContext(List<TypedElemental> value) {
        setValue("originalContext", value);
    }

    public void setResource(List<TypedElemental> value) {
        setValue("resource", value);
    }

    public void setRootResource(List<TypedElemental> value) {
        setValue("rootResource", value);
    }

}

