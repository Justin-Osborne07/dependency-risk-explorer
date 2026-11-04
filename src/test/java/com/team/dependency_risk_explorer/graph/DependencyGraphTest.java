package com.team.dependency_risk_explorer.graph;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DependencyGraphTest {

    // Check that the graph stores its roots and direct dependencies correctly
    @Test
    void storesRootsAndDependencies() {

        // Sample package identifiers used only for testing
        PackageCoordinate a = new PackageCoordinate("Maven", "demo:a", "1.0");
        PackageCoordinate b = new PackageCoordinate("Maven", "demo:b", "1.0");

        // A is the starting package, and its dependency list contains B
        // The map connects each package key to the packages it depends on
        DependencyGraph graph = new DependencyGraph(
                Set.of(a),
                Map.of(a, List.of(b))
        );

        // assertEquals compares the expected value first, then the actual result
        // The graph should remember A as its only root
        assertEquals(Set.of(a), graph.getRoots());

        // Asking for A's dependencies should return a list containing B
        assertEquals(List.of(b), graph.getDependencies(a));

        // B has no entry in our map, so the getter should return an empty list
        assertEquals(List.of(), graph.getDependencies(b));
    }

    // Check that changing the original collections cannot change the stored graph
    @Test
    void originalCollectionChangesDoNotAffectGraph() {

        PackageCoordinate a = new PackageCoordinate("Maven", "demo:a", "1.0");
        PackageCoordinate b = new PackageCoordinate("Maven", "demo:b", "1.0");

        // Use a modifiable set so we can remove A after creating the graph
        Set<PackageCoordinate> roots = new HashSet<>();
        roots.add(a);

        // Create a modifiable list containing A's dependency, B
        List<PackageCoordinate> dependencies = new ArrayList<>();
        dependencies.add(b);

        // Associate package A with the dependency list we just created
        Map<PackageCoordinate, List<PackageCoordinate>> adjacencyMap =
                new HashMap<>();
        adjacencyMap.put(a, dependencies);

        // The constructor should copy the roots, map, and dependency lists
        DependencyGraph graph = new DependencyGraph(roots, adjacencyMap);

        // Empty the original collections after the graph has been created
        // Its defensive copies should keep the graph's data unchanged
        roots.clear();
        dependencies.clear();
        adjacencyMap.clear();

        // A should still be a root even though the original roots set is empty
        assertEquals(Set.of(a), graph.getRoots());

        // A should still depend on B even though the original list and map are empty
        assertEquals(List.of(b), graph.getDependencies(a));
    }
}