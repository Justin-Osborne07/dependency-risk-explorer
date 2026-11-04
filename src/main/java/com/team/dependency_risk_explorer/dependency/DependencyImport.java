package com.team.dependency_risk_explorer.dependency;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

// Represents the complete JSON document supplied for an import

public record DependencyImport(

        // Only version 1 of our import format is supported
        @NotNull
        @Min(1)
        @Max(1)
        Integer schemaVersion,

        @NotBlank
        @Size(max = 100)
        String projectName,

        // The list is required, and each root ID must be nonblank
        @NotNull
        @Size(min = 1, max = 1000)
        List<@NotBlank @Size(max = 100) String> roots,

        // Validate each node's fields as well as the list itself
        @NotNull
        @Size(min = 1, max = 1000)
        List<@NotNull @Valid Node> nodes,

        // An empty edge list is allowed, but a missing list is not
        @NotNull
        @Size(max = 5000)
        List<@NotNull @Valid Edge> edges
) {

    // One package from the nodes array
    public record Node(

            @NotBlank
            @Size(max = 100)
            String id,

            @NotBlank
            @Pattern(regexp = "Maven")
            String ecosystem,

            @NotBlank
            @Size(max = 300)
            String name,

            @NotBlank
            @Size(max = 100)
            String version
    ) {
    }

    // A directed connection: the "from" package depends on the "to" package
    public record Edge(

            @NotBlank
            @Size(max = 100)
            String from,

            @NotBlank
            @Size(max = 100)
            String to
    ) {
    }
}