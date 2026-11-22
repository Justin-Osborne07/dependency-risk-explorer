package com.team.dependency_risk_explorer.osv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.dependency_risk_explorer.graph.PackageCoordinate;

public record OsvQuery(
        String version,
        @JsonProperty("package") OsvPackage osvPackage
) {

    public record OsvPackage(
            String name,
            String ecosystem
    ) {}

    public static OsvQuery fromCoordinate(PackageCoordinate p) {
        return new OsvQuery(
                p.version(),
                new OsvPackage(
                        p.name(),
                        p.ecosystem()
                )
        );
    }
}