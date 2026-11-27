package com.team.dependency_risk_explorer.graph;

import java.util.HashSet;
import java.util.Set;

public class DependencyTraverser {

    /**
     * Returns all packages reachable from the graph's roots.
     * Runs in O(V + E) time for V reachable packages and E dependency edges
     * leaving those packages.
     */
    public Set<PackageCoordinate> findReachable(DependencyGraph graph) {

        //Empty list of packages
        Set<PackageCoordinate> visited = new HashSet<>();

        //start at each root
        for (PackageCoordinate root : graph.getRoots()) {
            visit(graph, root, visited);
        }

        return Set.copyOf(visited);
    }

    private void visit(DependencyGraph graph, PackageCoordinate current, Set<PackageCoordinate> visited) {
        // Add current package or stop the current branch
        if (!visited.add(current)) {
            return;
        }

        //follow current packages dependencies
        for (PackageCoordinate dependency : graph.getDependencies(current)) {
            visit(graph, dependency, visited);
        }
    }

}
