package org.example.dnsservice.service;

import org.example.dnsservice.HtmlTemplateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UIService {

    private final ServerService serverService;
    private final HtmlTemplateGenerator htmlTemplateGenerator;

    @Autowired
    public UIService(HtmlTemplateGenerator htmlTemplateGenerator, ServerService serverService) {
        this.serverService = serverService;
        this.htmlTemplateGenerator = htmlTemplateGenerator;
    }

    public String renderHtml(){
        return htmlTemplateGenerator.generate(
                serverService.getServerEntries()
        );
    }

}
