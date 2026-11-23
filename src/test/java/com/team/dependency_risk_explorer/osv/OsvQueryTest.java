
package com.team.dependency_risk_explorer.osv;

import tools.jackson.databind.ObjectMapper;
import com.team.dependency_risk_explorer.graph.PackageCoordinate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OsvQueryTest {

    @Test
    void convertsCoordinateToOsvJson() throws Exception {

        PackageCoordinate coordinate =
                new PackageCoordinate(
                        "Maven",
                        "org.example:library",
                        "1.0.0"
                );

        OsvQuery query = OsvQuery.fromCoordinate(coordinate);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(query);

        String expected = """
                {"version":"1.0.0","package":{"name":"org.example:library","ecosystem":"Maven"}}
                """.trim();

        assertEquals(expected, json);
    }
}
