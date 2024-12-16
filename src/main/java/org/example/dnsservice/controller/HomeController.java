package org.example.dnsservice.controller;

import org.example.dnsservice.service.UIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/dns-service"}, produces = {"application/html"})
public class HomeController {

    private final UIService uiService;

    @Autowired
    public HomeController(UIService uiService) {
        this.uiService = uiService;
    }

    @GetMapping("/home")
    public ResponseEntity<String> home(){
        String html = uiService.renderHtml();
        return ResponseEntity.ok().contentLength(html.length())
                .contentType(MediaType.TEXT_HTML).body(html);
    }

}
