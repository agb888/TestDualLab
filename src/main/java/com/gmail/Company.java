package com.gmail;

import java.util.Arrays;

/**
 * @author Alexander Balabolov on 12.02.2020
 */
public enum Company {
    POSH("Posh"),
    GROTTY("Grotty");
    
    private String name;
    
    Company(String name) {
        this.name = name;
    }
    
    public static Company fromValue(String value) {
        for (Company company : Company.values()) {
            if (company.name().equalsIgnoreCase(value)) {
                return company;
            }
        }
        
        throw new IllegalArgumentException("Incorrect company " + value + ". Accepted companies are " + Arrays.asList(Company.values()));
    }
    
    @Override
    public String toString() {
        return name;
    }
}
