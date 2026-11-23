
package com.team.dependency_risk_explorer.osv;

import com.team.dependency_risk_explorer.graph.PackageCoordinate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OsvClientIntegrationTest {

    @Test
    void queriesRealOsvApi() {

        OsvClient client = new OsvClient();

        PackageCoordinate coordinate =
                new PackageCoordinate(
                        "Maven",
                        "org.apache.logging.log4j:log4j-core",
                        "2.14.1"
                );

        OsvResponse response = client.query(coordinate);

        assertNotNull(response);
        assertNotNull(response.vulns());
        assertFalse(response.vulns().isEmpty());

        System.out.println("Vulnerabilities found:");

        for (OsvResponse.Vulnerability vuln : response.vulns()) {
            System.out.println(vuln.id() + ": " + vuln.summary());
        }
    }
}
