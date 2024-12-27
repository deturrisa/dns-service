package org.example.dnsservice.configuration;

import java.util.List;

public class Location {
    private String country;
    private List<String> domains;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }
    @Override public String toString() {
        return "Location{" + "country='" + country + '\'' + ", domains=" + domains + '}';
    }
}
