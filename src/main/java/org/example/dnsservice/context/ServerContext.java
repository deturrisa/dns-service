package org.example.dnsservice.context;

import org.example.dnsservice.model.ServerEntry;
import org.thymeleaf.context.Context;
import java.util.List;

public record ServerContext() {

    public static Context create(List<ServerEntry> serverEntries) {
        Context context = new Context();
        context.setVariable("serverEntries",serverEntries);
        return context;
    }

}
