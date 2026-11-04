# Dependency Import Format

The application accepts JSON describing packages and their dependencies.
Version 1 supports Maven packages.

## Example

```json
{
  "schemaVersion": 1,
  "projectName": "Example Project",
  "roots": ["a"],
  "nodes": [
    {
      "id": "a",
      "ecosystem": "Maven",
      "name": "demo:package-a",
      "version": "1.0"
    },
    {
      "id": "b",
      "ecosystem": "Maven",
      "name": "demo:package-b",
      "version": "2.0"
    }
  ],
  "edges": [
    { "from": "a", "to": "b" }
  ]
}
```

## Fields

- schemaVersion: must be 1.
- projectName: the project's display name.
- roots: node IDs where dependency traversal begins.
- nodes: packages identified by id, ecosystem, name, and version.
- edges: connections between nodes. From A to B means A depends on B.

Node IDs are labels within this file, not database IDs.
A package coordinate is its ecosystem, name, and version.

## Rules

- All fields shown above are required and cannot be null.
- Strings cannot be blank or have surrounding whitespace.
- Node IDs are case-sensitive and must be unique.
- ecosystem must be "Maven".
- Package names use groupId:artifactId, with exactly one colon,
  nonempty parts, and no whitespace.
- Versions must be exact versions, not ranges.
- Duplicate package coordinates are rejected.
  Different versions of the same package are allowed.
- At least one root is required. Every root and edge ID must match a node.
- Duplicate roots and duplicate from/to edges are rejected.
- The edges list can be empty.
- Cycles are allowed. Traversal must avoid endless loops.
- Only packages reachable from the roots are scanned.

Reject the whole import if a rule fails, and explain the problem.

## Limits

| Item | Limit |
|---|---|
| JSON body | 1 MiB (1,048,576 bytes) |
| Nodes | 1–1,000 |
| Roots | 1–1,000 |
| Edges | 0–5,000 |
| Project name, node IDs, versions | 1–100 characters each |
| Package names | 1–300 characters |

Examples of invalid input: duplicate node IDs, an unknown root,
an edge pointing to a missing node, or a blank version.