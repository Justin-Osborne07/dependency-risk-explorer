package com.team.dependency_risk_explorer.osv;

import java.util.List;

public record OsvResponse(
        List<Vulnerability> vulns
) {
    public record Vulnerability(
            String id,
            String summary
    ) {}
}