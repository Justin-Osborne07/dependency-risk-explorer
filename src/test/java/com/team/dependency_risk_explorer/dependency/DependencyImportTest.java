package com.team.dependency_risk_explorer.dependency;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

// Static imports let us call these methods without writing Assertions. first
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DependencyImportTest {

    // Create a Jackson tool that converts JSON into Java objects
    private final JsonMapper mapper = JsonMapper.builder().build();

    // @Test tells JUnit to run this method as a test
    @Test
    void readsValidSample() throws Exception {

        // Read the file into a string; file errors will cause the test to fail
        String json = Files.readString(Path.of("examples/sample-project.json"));

        // DependencyImport.class tells Jackson which type of object to create
        DependencyImport data = mapper.readValue(json, DependencyImport.class);

        // assertEquals takes the expected value first, then the actual result
        assertEquals(Integer.valueOf(1), data.schemaVersion());
        assertEquals("Demo Project", data.projectName());
        assertEquals("app", data.roots().get(0));
        assertEquals(4, data.nodes().size());
        assertEquals(4, data.edges().size());

        // get(0) selects the first item; record accessors read its fields
        assertEquals("demo:application", data.nodes().get(0).name());
        assertEquals("app", data.edges().get(0).from());
        assertEquals("web", data.edges().get(0).to());

        // Call our helper to run the validation rules
        Set<String> errors = findInvalidFields(data);

        // Expect no errors; the message is shown if this assertion fails
        assertTrue(errors.isEmpty(), "Unexpected invalid fields: " + errors);
    }

    @Test
    void detectsMissingSchemaAndBlankNodeVersion() {

        // Triple quotes hold multiline text
        // This input deliberately omits schemaVersion and leaves version blank
        String json = """
                {
                  "projectName": "Demo Project",
                  "roots": ["app"],
                  "nodes": [
                    {
                      "id": "app",
                      "ecosystem": "Maven",
                      "name": "demo:application",
                      "version": ""
                    }
                  ],
                  "edges": []
                }
                """;

        // JSON conversion and annotation validation are separate steps
        DependencyImport data = mapper.readValue(json, DependencyImport.class);
        Set<String> errors = findInvalidFields(data);

        // The test passes if exactly these two field locations are reported
        // nodes[0].version refers to the first node's version
        assertEquals(Set.of("schemaVersion", "nodes[0].version"), errors);
    }

    // Validate an import and return the locations of fields that broke rules
    private Set<String> findInvalidFields(DependencyImport data) {

        // A set stores each invalid field location only once
        Set<String> invalidFields = new HashSet<>();

        // The factory provides validators; try closes it automatically afterward
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {

            Validator validator = factory.getValidator();

            // Run the annotations, including nested checks enabled by @Valid
            // Each ConstraintViolation describes one broken rule
            Set<ConstraintViolation<DependencyImport>> violations = validator.validate(data);

            // Extract each problem's field location and store it as text
            for (ConstraintViolation<DependencyImport> violation : violations) {
                invalidFields.add(violation.getPropertyPath().toString());
            }
        }

        // Return the collected locations to the test that called this helper
        return invalidFields;
    }
}