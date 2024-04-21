package dev.codedok.util;


import lombok.Data;

@Data
public class Domain {

    private final String subdomain;
    private final String host;

    public String getFullQualifiedDomain() {
        return subdomain + '.' + host;
    }
}
