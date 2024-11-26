package org.example.dnsservice.singleservicetests;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Format;
import org.mockserver.model.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;

public class RestServerMockContainerExtension extends ToggleableContainerExtension implements ParameterResolver {

    private static final String MOCK_URL = "MOCK_URL";
    private static final String MOCK_WORKFLOWS_URL = "MOCK_WORKFLOWS_URL";

    private Logger log = LoggerFactory.getLogger(RestServerMockContainerExtension.class);

    private MockServerContainer mockRestServer = new MockServerContainer (
            DockerImageName.parse ( "mockserver/mockserver").withTag("5.15.0")
    );

    private MockServerClient restMockClient;

    public RestServerMockContainerExtension() {
        super(ExternalPlatform.REST_SERVER);
    }

    @Override
    public void beforeAllInternal(ExtensionContext context) {
        log.info("Inside beforeAllInternal RestServerMockContainerExtension");
        log.info("### Starting up REST mock server container.");
        mockRestServer.start();
        restMockClient = new MockServerClient(mockRestServer.getHost(), mockRestServer.getServerPort()) ;
        setConnectionProperties();
        String mockUrl = System.getProperty ("MOCK_URL");
        String workflowsUrl = System.getProperty("MOCK_WORKFLOWS_URL");
        log.info(String.format("### REST mock server container startup finished with properties: [MOCK_URL=%, MOCK_WORKFLOWS_URL=%]", mockUrl, workflowsUrl)) ;
    }

    @Override
    public void afterAllInternal(ExtensionContext context) {
        log.info("Inside afterAllInternal RestServerMockContainerExtension");
        log.info("### Stopping REST mock server container.");
        String recordedRequests = restMockClient.retrieveRecordedRequests(
                HttpRequest.request(),
                Format.JSON
        );
        log.info("### Recorded requests: {}", recordedRequests);
        String recordedExpectations = restMockClient.retrieveRecordedExpectations(
                HttpRequest.request(),
                Format.JSON
        );
        log.info("### Recorded expectations: {}", recordedExpectations);
        String activeExpectations = restMockClient.retrieveActiveExpectations(
                HttpRequest.request(),
                Format.JSON
        );
        log.info("### Active expectations: {}", activeExpectations);
        restMockClient.stop();
        mockRestServer.stop();
        log.info("### Rest mock server container stopped.");
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == MockServerClient.class;
    }

    @Override
    public MockServerClient resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return restMockClient;
    }

    private void setConnectionProperties() {
        System.setProperty (MOCK_URL, "http://localhost:" + mockRestServer.getHost() + ":" +mockRestServer.getServerPort());
        System.setProperty (MOCK_WORKFLOWS_URL, "http://localhost:" + mockRestServer.getHost() + ":" +mockRestServer.getServerPort());
        System.setProperty("mockserver.maxSocketTimeout", "120000");
    }
}
