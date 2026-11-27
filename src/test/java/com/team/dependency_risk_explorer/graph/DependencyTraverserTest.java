package com.team.dependency_risk_explorer.graph;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DependencyTraverserTest {

    private final DependencyTraverser traverser = new DependencyTraverser();
    private final PackageCoordinate a = new PackageCoordinate("Maven", "demo:a", "1.0");
    private final PackageCoordinate b = new PackageCoordinate("Maven", "demo:b", "1.0");
    private final PackageCoordinate c = new PackageCoordinate("Maven", "demo:c", "1.0");

    @Test
    void returnsEmptySetForEmptyGraph() {
        DependencyGraph graph = new DependencyGraph(Set.of(), Map.of());
        assertEquals(Set.of(), traverser.findReachable(graph));
    }

    @Test
    void followsDependencyChain() {
        // A -> B -> C
        DependencyGraph graph = new DependencyGraph(Set.of(a), Map.of(a, List.of(b), b, List.of(c)));
        assertEquals(Set.of(a, b, c), traverser.findReachable(graph));
    }

    @Test
    void returnsSharedDependencyOnce() {
        // A -> C and B -> C, with both A and B as roots
        DependencyGraph graph = new DependencyGraph(Set.of(a, b), Map.of(a, List.of(c), b, List.of(c)));
        assertEquals(Set.of(a, b, c), traverser.findReachable(graph));
    }

    @Test
    void stopsAtCycle() {
        // A -> B -> C -> A
        DependencyGraph graph = new DependencyGraph(Set.of(a), Map.of(a, List.of(b), b, List.of(c), c, List.of(a)));
        assertEquals(Set.of(a, b, c), traverser.findReachable(graph));
    }

    @Test
    void skipsUnreachablePackage() {
        // A -> B, with C on its own
        DependencyGraph graph = new DependencyGraph(Set.of(a), Map.of(a, List.of(b), c, List.of()));
        assertEquals(Set.of(a, b), traverser.findReachable(graph));
    }
}
