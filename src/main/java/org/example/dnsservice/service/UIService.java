package org.example.dnsservice.service;

import org.example.dnsservice.HtmlTemplateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UIService {

    private final EntryStoreService entryStoreService;
    private final HtmlTemplateGenerator htmlTemplateGenerator;

    @Autowired
    public UIService(HtmlTemplateGenerator htmlTemplateGenerator, EntryStoreService entryStoreService) {
        this.entryStoreService = entryStoreService;
        this.htmlTemplateGenerator = htmlTemplateGenerator;
    }

    public String renderHtml(){
        return htmlTemplateGenerator.generate(
                entryStoreService.getEntryStore()
        );
    }

}
