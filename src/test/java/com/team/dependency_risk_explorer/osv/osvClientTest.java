
package com.team.dependency_risk_explorer.osv;

import com.team.dependency_risk_explorer.graph.PackageCoordinate;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OsvClientTest {

    @Test
    void returnsVulnerabilities() {

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        OsvClient client = new OsvClient(builder.build());

        server.expect(requestTo("https://api.osv.dev/v1/query"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                            "vulns": [
                                {
                                    "id": "GHSA-test",
                                    "summary": "Test vulnerability"
                                }
                            ]
                        }
                        """, org.springframework.http.MediaType.APPLICATION_JSON));

        PackageCoordinate coordinate =
                new PackageCoordinate("Maven", "example:library", "1.0");

        OsvResponse response = client.query(coordinate);

        assertNotNull(response);
        assertEquals(1, response.vulns().size());
        assertEquals("GHSA-test", response.vulns().get(0).id());

        server.verify();
    }

    @Test
    void returnsNoVulnerabilities() {

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        OsvClient client = new OsvClient(builder.build());

        server.expect(requestTo("https://api.osv.dev/v1/query"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withSuccess(
                        "{}",
                        org.springframework.http.MediaType.APPLICATION_JSON
                ));

        PackageCoordinate coordinate =
                new PackageCoordinate("Maven", "example:safe-library", "1.0");

        OsvResponse response = client.query(coordinate);

        assertNotNull(response);
        assertTrue(response.vulns() == null || response.vulns().isEmpty());

        server.verify();
    }

    @Test
    void sendsCorrectPackageInformation() {

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        OsvClient client = new OsvClient(builder.build());

        server.expect(requestTo("https://api.osv.dev/v1/query"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(content().json("""
                    {
                        "version": "2.14.1",
                        "package": {
                            "name": "org.apache.logging.log4j:log4j-core",
                            "ecosystem": "Maven"
                        }
                    }
                    """))
                .andRespond(withSuccess(
                        "{}",
                        org.springframework.http.MediaType.APPLICATION_JSON
                ));

        PackageCoordinate coordinate = new PackageCoordinate(
                "Maven",
                "org.apache.logging.log4j:log4j-core",
                "2.14.1"
        );

        client.query(coordinate);

        server.verify();
    }

}
