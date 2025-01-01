package org.example.dnsservice.context;

import org.example.dnsservice.model.EntryStore;
import org.thymeleaf.context.Context;

public record ServerContext() {

    public static Context create(EntryStore entryStore) {
        Context context = new Context();
        context.setVariable("serverEntries",entryStore.serverEntries());
        context.setVariable("dnsEntries", entryStore.dnsEntries());
        return context;
    }
}
