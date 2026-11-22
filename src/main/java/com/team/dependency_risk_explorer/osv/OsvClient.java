
package com.team.dependency_risk_explorer.osv;

import com.team.dependency_risk_explorer.graph.PackageCoordinate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OsvClient {

    private static final String OSV_URL =
            "https://api.osv.dev/v1/query";

    private final RestClient restClient;

    OsvClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public OsvClient() {
        this.restClient = RestClient.create();
    }

    public OsvResponse query(PackageCoordinate coordinate) {

        OsvQuery request = OsvQuery.fromCoordinate(coordinate);

        return restClient.post()
                .uri(OSV_URL)
                .body(request)
                .retrieve()
                .body(OsvResponse.class);
    }
}
