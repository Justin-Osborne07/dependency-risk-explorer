# API Contract

These are the planned requests and responses shared by the frontend
and backend. Update this document when the conventions change.

## Conventions

- All routes start with /api.
- Request and response bodies use JSON.
- Requests containing JSON use Content-Type: application/json.
- Database IDs are positive integers, separate from import node IDs.
- Timestamps use UTC, such as "2026-11-04T18:00:00Z".
- List endpoints return arrays, or [] when empty.

A project holds snapshots. Each snapshot is a saved dependency graph.
A scan checks a snapshot. A finding identifies a vulnerability in a package.

## Routes

| Method | Route | Purpose |
|---|---|---|
| POST | /api/projects | Create a project |
| GET | /api/projects | List projects |
| POST | /api/projects/{projectId}/snapshots | Import a snapshot |
| GET | /api/projects/{projectId}/snapshots | List snapshots |
| POST | /api/snapshots/{snapshotId}/scans | Run a scan |
| GET | /api/scans/{scanId} | Get scan results |

Replace placeholders with actual IDs, such as /api/projects/1/snapshots.

## Projects and snapshots

Create-project request:

```json
{ "name": "Example Project" }
```

Project response:

```json
{
  "id": 1,
  "name": "Example Project",
  "createdAt": "2026-11-04T18:00:00Z"
}
```

Project names must be nonblank, 1–100 characters, with no surrounding whitespace.

To import a snapshot, send the JSON described in import-format.md
directly as the request body. Do not use a multipart file upload.

The URL selects the project. projectName must match its name.
Each valid import creates a new snapshot without overwriting earlier ones.

Snapshot response:

```json
{
  "id": 10,
  "projectId": 1,
  "nodeCount": 2,
  "edgeCount": 1,
  "createdAt": "2026-11-04T18:05:00Z"
}
```

The project and snapshot list routes return arrays of these objects.

## Scans

Starting a scan requires no request body.
The first version waits for the scan to finish before responding.

The start-scan and get-scan routes return:

```json
{
  "id": 20,
  "snapshotId": 10,
  "status": "COMPLETE",
  "totalPackages": 2,
  "checkedPackages": 2,
  "failedPackages": 0,
  "startedAt": "2026-11-04T18:10:00Z",
  "completedAt": "2026-11-04T18:10:02Z",
  "findings": []
}
```

- totalPackages: unique packages reachable from the roots.
- checkedPackages: packages with successful, complete lookups.
- failedPackages: packages whose lookup failed or was incomplete.
- completedAt: null while running.

| Status | Meaning |
|---|---|
| RUNNING | Still processing |
| COMPLETE | All required lookups succeeded |
| PARTIAL | Some lookups succeeded and some failed |
| FAILED | No lookups succeeded, or the scan could not finish |

For a finished scan, checkedPackages + failedPackages = totalPackages.
Count unfinished lookups as failed coverage.

Each finding contains:

```json
{
  "id": 30,
  "packageCoordinate": {
    "ecosystem": "Maven",
    "name": "demo:package-b",
    "version": "2.0"
  },
  "advisoryId": "EXAMPLE-001",
  "summary": "Fictional example finding"
}
```

summary may be null. An empty findings list does not prove safety,
especially when lookups failed.

## Later features

Finding details:
GET /api/scans/{scanId}/findings/{findingId}

Return the finding fields plus references (URL strings) and
dependencyPath (package coordinates from a root to the affected package).
Return one shortest path, including both endpoints, or null if none exists.

Comparison:
GET /api/scans/{scanId}/comparison?baselineScanId={baselineId}

Both scans must be finished and belong to the same project.
Return scanId, baselineScanId, added, removed, unchanged, and incomplete.

The finding arrays use the format above. Match findings by package
coordinate and advisory ID, not database ID. For unchanged findings,
use the selected scan's objects.

incomplete is true if either scan is not COMPLETE.
A removed finding is not automatically a proven fix.

## Status codes and errors

| Code | Meaning |
|---|---|
| 200 | Successful read |
| 201 | Project, snapshot, or scan record created |
| 400 | Invalid request |
| 404 | Requested resource does not exist |
| 413 | Import body exceeds the size limit |
| 500 | Unexpected server error |

A saved scan can return 201 with PARTIAL or FAILED status.
The scan status describes the analysis outcome.

Error response:

```json
{
  "status": 400,
  "message": "Invalid dependency import",
  "details": ["Root ID 'missing' does not match a node."]
}
```

details may be empty. Do not return internal stack traces.