package org.springboot.insurancemanagementsystem.enums;

public enum AllowedRelation {
    FATHER, MOTHER, SPOUSE, SON, DAUGHTER, BROTHER, SISTER, OTHER;

    public static boolean isValid(String val) {
        if (val == null) return false;
        for (AllowedRelation rel : values()) {
            if (rel.name().equalsIgnoreCase(val.trim())) return true;
        }
        return false;
    }
}