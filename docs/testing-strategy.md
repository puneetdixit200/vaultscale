# Testing strategy

Use the diagrams as test-design maps: authentication/RBAC for access tests, endpoint execution for SSRF, timeout, and circuit-breaker tests, audit events for eventual-consistency tests, and Compose deployment for readiness and ingress tests. Existing QA checklists live under [`../qa/`](../qa/).

The diagrams document code and declared infrastructure; they are not a substitute for a live-browser or deployed-environment verification.
