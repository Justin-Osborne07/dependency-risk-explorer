package com.team.dependency_risk_explorer.graph;

// ID versions of package's

public record PackageCoordinate(
        String ecosystem, // Package ecosystem it belongs to (ex: Maven)
        String name, // Package Identifier
        String version // The specific version that is being analyzed

) {
}
