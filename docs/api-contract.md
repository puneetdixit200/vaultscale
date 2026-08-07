# API contract map

The browser calls `/api/v1/auth` for registration/login and uses nested organization routes for collections, endpoints, execution, history, and audit logs. The request-execution safety path is shown in [endpoint execution flow](diagrams/endpoint-execution-flow.drawio), while authorization is shown in [authentication and RBAC sequence](diagrams/authentication-rbac-sequence.drawio).

The key route hierarchy is:

`/api/v1/orgs/{orgId}/collections/{collectionId}/endpoints/{endpointId}`

Appending `/run` executes the saved request; appending `/history` reads that endpoint's execution history.
