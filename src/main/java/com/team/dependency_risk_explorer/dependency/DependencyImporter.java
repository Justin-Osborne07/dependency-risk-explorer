package com.team.dependency_risk_explorer.dependency;

import com.team.dependency_risk_explorer.graph.PackageCoordinate;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import com.team.dependency_risk_explorer.graph.DependencyGraph;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.regex.Pattern;

public class DependencyImporter {

    private Map<String, PackageCoordinate> indexNodes(List<DependencyImport.Node>nodes){

        // Creates a Map with the String & Package Coordinate
        Map<String, PackageCoordinate> coordinatesById = new HashMap<>();

        // Scans through nodes and creates new PackageCord which saves the ID as a map key
        for (DependencyImport.Node node:nodes){
            PackageCoordinate coordinate = new PackageCoordinate(node.ecosystem() , node.name(), node.version());

        // Catcher for duplicate node ID's
        if (coordinatesById.putIfAbsent(node.id(), coordinate) != null){
                throw new IllegalArgumentException("Duplicate node ID: " + node.id());
            }
        }

        return coordinatesById;

    }

    private Set<PackageCoordinate> resolveRoots(
            List<String> rootIds,
            Map<String, PackageCoordinate> coordinatesById) {
        Set<PackageCoordinate> roots = new HashSet<>();

        // Maps each root to a package coordinate
        for (String rootId : rootIds) {
            PackageCoordinate coordinate = coordinatesById.get(rootId);

            // Catcher for no input
            if (coordinate == null) {
                throw new IllegalArgumentException("Unknown root ID: " + rootId);
            }

            // Catcher for duplicated roots
            if (!roots.add(coordinate)) {
                throw new IllegalArgumentException("Duplicate root: " + rootId);
            }
        }

        return roots;
    }

    private Map<PackageCoordinate, List<PackageCoordinate>> buildAdjacencyMap(
            List<DependencyImport.Edge> edges,
            Map<String, PackageCoordinate> coordinatesById) {
        Map<PackageCoordinate, List<PackageCoordinate>> adjacencyMap = new HashMap<>();

        for (PackageCoordinate coordinate : coordinatesById.values()) {
            if (adjacencyMap.putIfAbsent(coordinate, new ArrayList<>()) != null) {
                throw new IllegalArgumentException("Duplicate package coordinate: " + coordinate);
            }
        }

        Set<DependencyImport.Edge> seenEdges = new HashSet<>();
        for (DependencyImport.Edge edge : edges) {
            PackageCoordinate from = coordinatesById.get(edge.from());
            PackageCoordinate to = coordinatesById.get(edge.to());

            if (from == null) {
                throw new IllegalArgumentException("Unknown edge source ID: " + edge.from());
            }
            if (to == null) {
                throw new IllegalArgumentException("Unknown edge destination ID: " + edge.to());
            }
            if (!seenEdges.add(edge)) {
                throw new IllegalArgumentException("Duplicate edge: " + edge.from() + " -> " + edge.to());
            }

            adjacencyMap.get(from).add(to);
        }

        return adjacencyMap;
    }

    private static final Pattern MAVEN_NAME = Pattern.compile("[^:\\s]+:[^:\\s]+", Pattern.UNICODE_CHARACTER_CLASS);

    private static final Pattern SURROUNDING_WHITESPACE = Pattern.compile("\\A\\s|\\s\\z", Pattern.UNICODE_CHARACTER_CLASS);

    private final Validator validator;

    // Uses the Jakarta Validator supplied by the caller.
    public DependencyImporter(Validator validator) {
        this.validator = validator;
    }

    // Validates the import and uses the three helpers to build the graph.
    public DependencyGraph importGraph(DependencyImport input) {
        validateInput(input);

        var coordinatesById = indexNodes(input.nodes());
        var adjacencyMap = buildAdjacencyMap(input.edges(), coordinatesById);
        var roots = resolveRoots(input.roots(), coordinatesById);

        return new DependencyGraph(roots, adjacencyMap);
    }

    // Runs the J01 annotations and checks the remaining import rules.
    private void validateInput(DependencyImport input) {
        if (input == null) {
            throw new IllegalArgumentException("Import data is required.");
        }

        var violations = validator.validate(input);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        rejectSurroundingWhitespace("Project name", input.projectName());
        for (String rootId : input.roots()) {
            rejectSurroundingWhitespace("Root ID", rootId);
        }

        for (DependencyImport.Node node : input.nodes()) {
            rejectSurroundingWhitespace("Node ID", node.id());
            if (!MAVEN_NAME.matcher(node.name()).matches()) {
                throw new IllegalArgumentException(
                        "Invalid Maven package name for node " + node.id() + ": " + node.name());
            }

            String version = node.version();
            rejectSurroundingWhitespace("Version for node " + node.id(), version);
            if (version.startsWith("[") || version.startsWith("(")
                    || version.equals("LATEST") || version.equals("RELEASE")
                    || version.contains("${")) {
                throw new IllegalArgumentException(
                        "Expected an exact version for node " + node.id() + ": " + version);
            }
        }

        for (DependencyImport.Edge edge : input.edges()) {
            rejectSurroundingWhitespace("Edge source ID", edge.from());
            rejectSurroundingWhitespace("Edge destination ID", edge.to());
        }
    }

    // Reports which field has whitespace around it.
    private void rejectSurroundingWhitespace(String field, String value) {
        if (SURROUNDING_WHITESPACE.matcher(value).find()) {
            throw new IllegalArgumentException(field + " cannot have surrounding whitespace.");
        }
    }
}
