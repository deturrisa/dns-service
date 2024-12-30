package org.example.dnsservice.service;

import org.example.dnsservice.HtmlTemplateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UIService {

    private final ServerEntryService serverEntryService;
    private final HtmlTemplateGenerator htmlTemplateGenerator;

    @Autowired
    public UIService(HtmlTemplateGenerator htmlTemplateGenerator, ServerEntryService serverEntryService) {
        this.serverEntryService = serverEntryService;
        this.htmlTemplateGenerator = htmlTemplateGenerator;
    }

    public String renderHtml(){
        return htmlTemplateGenerator.generate(
                serverEntryService.getServerEntries()
        );
    }

}
