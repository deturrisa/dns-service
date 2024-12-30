package org.example.dnsservice.configuration;

import java.util.List;

public class DomainRegion {
    private String regionCode;
    private List<String> localityCodes;

    public DomainRegion(String regionCode, List<String> domains) {
        this.regionCode = regionCode;
        this.localityCodes = domains;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public List<String> getLocalityCodes() {
        return localityCodes;
    }

    public void setLocalityCodes(List<String> localityCodes) {
        this.localityCodes = localityCodes;
    }
}
