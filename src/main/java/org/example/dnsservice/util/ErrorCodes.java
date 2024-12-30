package org.example.dnsservice.util;

public class ErrorCodes {

    public static class Errors{
        public static final String ERROR_EMPTY_DOMAIN_REGIONS = "Domain regions are empty or could not be retrieved";
        public static final String ERROR_DUPLICATE_LOCALITY_CODE = "Duplicate locality code: {}";
        public static final String ERROR_INVALID_REGION = "Locality codes are empty or could not be retrieved for region: {}";
        public static final String ERROR_INVALID_SUBDOMAIN = "Subdomain does not contain only lowercase letters: a-z. ServerId: {}";
        public static final String ERROR_DUPLICATE_IP_ADDRESSES = "IP Addresses are not unique across servers";
    }

}
