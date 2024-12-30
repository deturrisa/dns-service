package org.example.dnsservice.configuration;

import java.util.Set;

public class DomainRegion {
    private String regionCode;
    private Set<String> localityCodes;

    public DomainRegion(String regionCode, Set<String> domains) {
        this.regionCode = regionCode;
        this.localityCodes = domains;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public Set<String> getLocalityCodes() {
        return localityCodes;
    }

    public void setLocalityCodes(Set<String> localityCodes) {
        this.localityCodes = localityCodes;
    }
}
