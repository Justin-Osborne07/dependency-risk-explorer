package com.team.dependency_risk_explorer.dependency;

import com.team.dependency_risk_explorer.graph.DependencyGraph;
import com.team.dependency_risk_explorer.graph.PackageCoordinate;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DependencyImporterTest {

    @Test
    void buildsGraphFromBaselineSample() throws Exception {
        String json = Files.readString(Path.of("examples", "sample-project.json"));
        var mapper = JsonMapper.builder().build();
        var input = mapper.readValue(json, DependencyImport.class);

        DependencyGraph graph = importGraph(input);

        var app = new PackageCoordinate("Maven", "demo:application", "1.0");
        var web = new PackageCoordinate("Maven", "demo:web-library", "1.0");
        var data = new PackageCoordinate("Maven", "demo:data-library", "1.0");
        var utils = new PackageCoordinate("Maven", "demo:utilities", "1.0");

        assertEquals(Set.of(app), graph.getRoots());
        assertEquals(Set.of(app, web, data, utils), graph.getAdjacencyMap().keySet());

        assertEquals(2, graph.getDependencies(app).size());
        assertEquals(Set.of(web, data), Set.copyOf(graph.getDependencies(app)));
        assertEquals(List.of(utils), graph.getDependencies(web));
        assertEquals(List.of(utils), graph.getDependencies(data));
        assertEquals(List.of(), graph.getDependencies(utils));
    }

    @Test
    void rejectsDuplicateNodeId() {
        var input = new DependencyImport(
                1,
                "Demo Project",
                List.of("app"),
                List.of(
                        new DependencyImport.Node("app", "Maven", "demo:application", "1.0"),
                        new DependencyImport.Node("app", "Maven", "demo:other", "2.0")
                ),
                List.of()
        );

        var error = assertThrows(IllegalArgumentException.class, () -> importGraph(input));
        assertEquals("Duplicate node ID: app", error.getMessage());
    }

    @Test
    void rejectsUnknownEdgeDestination() {
        var input = new DependencyImport(
                1,
                "Demo Project",
                List.of("app"),
                List.of(new DependencyImport.Node("app", "Maven", "demo:application", "1.0")),
                List.of(new DependencyImport.Edge("app", "missing"))
        );

        var error = assertThrows(IllegalArgumentException.class, () -> importGraph(input));
        assertEquals("Unknown edge destination ID: missing", error.getMessage());
    }

    @Test
    void rejectsUnknownRoot() {
        var input = new DependencyImport(
                1,
                "Demo Project",
                List.of("missing"),
                List.of(new DependencyImport.Node("app", "Maven", "demo:application", "1.0")),
                List.of()
        );

        var error = assertThrows(IllegalArgumentException.class, () -> importGraph(input));
        assertEquals("Unknown root ID: missing", error.getMessage());
    }

    private DependencyGraph importGraph(DependencyImport input) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return new DependencyImporter(factory.getValidator()).importGraph(input);
        }
    }
}