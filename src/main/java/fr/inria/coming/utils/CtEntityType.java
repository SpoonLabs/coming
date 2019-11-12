package fr.inria.coming.utils;

public enum CtEntityType {
    EXPRESSION,
    STATEMENT,
    RETURN,
    ABSTRACT_INVOCATION;

    @Override
    public String toString() {
        if(name().toLowerCase().equals("abstract_invocation")){
            return "AbstractInvocation";
        }
        String lowerCaseName = name().toLowerCase();
        return lowerCaseName.substring(0, 1).toUpperCase() + lowerCaseName.substring(1);
    }
}
