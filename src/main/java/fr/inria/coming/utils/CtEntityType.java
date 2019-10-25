package fr.inria.coming.utils;

public enum CtEntityType {
    EXPRESSION,
    STATEMENT;

    @Override
    public String toString() {
        String lowerCaseName = name().toLowerCase();
        return lowerCaseName.substring(0, 1).toUpperCase() + lowerCaseName.substring(1);
    }
}
