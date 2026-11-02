package com.team.dependency_risk_explorer.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DependencyGraph {

        // Declare roots and Adjacency Map
        // starting packages, where traversal begins
        private final Set<PackageCoordinate> roots;

        // Adj Map stores 2 vals (key -> val), the package and the list of dependent packages
        private final Map<PackageCoordinate, List<PackageCoordinate>> adjacencyMap;

        // Constructor that takes in our 2 parameters
        public DependencyGraph(Set<PackageCoordinate> roots, Map<PackageCoordinate, List<PackageCoordinate>> adjacencyMap){

            // Initialize roots and Adjacency Map
            this.roots = Set.copyOf(roots);
            Map<PackageCoordinate, List<PackageCoordinate>> copy = new HashMap<>();

            // for each package key in map, copy each package and its direct dependencies
            for(PackageCoordinate packageId : adjacencyMap.keySet()){
                copy.put(packageId, List.copyOf(adjacencyMap.get(packageId)));

            }

            // Stores unmodifiable copy of completed map
            this.adjacencyMap = Map.copyOf(copy);
        }

        // Returns Roots (Algorithms starting points)
        public Set<PackageCoordinate> getRoots() {
            return roots;
        }

        // Returns Dependencies (Packages directly required by one package)
        public List<PackageCoordinate> getDependencies(PackageCoordinate packageId) {
            return adjacencyMap.getOrDefault(packageId, List.of());
        }

        // Returns Adjacency Map (Shows all stored relationships)
        public Map<PackageCoordinate, List<PackageCoordinate>> getAdjacencyMap() {
            return adjacencyMap;
        }
}
