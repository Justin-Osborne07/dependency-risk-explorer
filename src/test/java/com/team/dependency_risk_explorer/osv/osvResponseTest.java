
package com.team.dependency_risk_explorer.osv;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OsvResponseTest {

    @Test
    void convertsOsvJsonToResponse() throws Exception {

        String json = """
                {
                    "vulns": [
                        {
                            "id": "GHSA-example",
                            "summary": "A security vulnerability"
                        }
                    ]
                }
                """;

        ObjectMapper mapper = new ObjectMapper();

        OsvResponse response =
                mapper.readValue(json, OsvResponse.class);

        assertNotNull(response);
        assertNotNull(response.vulns());
        assertEquals(1, response.vulns().size());

        OsvResponse.Vulnerability vulnerability =
                response.vulns().get(0);

        assertEquals("GHSA-example", vulnerability.id());
        assertEquals("A security vulnerability", vulnerability.summary());
    }
}
