package org.example.dnsservice.controller;

import static org.example.dnsservice.controller.Endpoints.BASE_URL;

import org.example.dnsservice.service.EntryStoreService;
import org.example.dnsservice.service.UIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping(
    value = {BASE_URL},
    produces = {"application/html"})
public class HomeController {

  private final Logger log = LoggerFactory.getLogger(HomeController.class);

  private final UIService uiService;
  private final EntryStoreService entryStoreService;

  @Autowired
  public HomeController(UIService uiService, EntryStoreService entryStoreService) {
    this.uiService = uiService;
    this.entryStoreService = entryStoreService;
  }

  @PostMapping("/add/{serverId}")
  public RedirectView add(@PathVariable Integer serverId) {
    entryStoreService.addToRotation(serverId);
    log.info("Successfully added server: {}. Redirecting home", serverId);
    return new RedirectView(BASE_URL);
  }

  @DeleteMapping("/remove/{serverId}")
  public RedirectView remove(@PathVariable Integer serverId) {
    entryStoreService.removeFromRotation(serverId);
    log.info("Successfully removed server: {}. Redirecting home", serverId);
    return new RedirectView(BASE_URL);
  }

  @GetMapping()
  public ResponseEntity<String> home() {
    return getHomeResponse();
  }

  private ResponseEntity<String> getHomeResponse() {
    var html = uiService.renderHtml();
    return getOkHtmlResponse(html);
  }

  private static ResponseEntity<String> getOkHtmlResponse(String html) {
    return ResponseEntity.ok()
        .contentLength(html.length())
        .contentType(MediaType.TEXT_HTML)
        .body(html);
  }
}
