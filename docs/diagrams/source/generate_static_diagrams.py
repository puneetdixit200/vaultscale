#!/usr/bin/env python3
"""No-dependency Draw.io generator for VaultScale's implementation diagrams."""
from pathlib import Path
from xml.sax.saxutils import escape

OUT = Path(__file__).resolve().parents[1]
BLUE = "rounded=1;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;"
GREEN = "rounded=1;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;"
ORANGE = "rounded=1;whiteSpace=wrap;html=1;fillColor=#ffe6cc;strokeColor=#d79b00;"
PURPLE = "rounded=1;whiteSpace=wrap;html=1;fillColor=#e1d5e7;strokeColor=#9673a6;"
YELLOW = "rounded=1;whiteSpace=wrap;html=1;fillColor=#fff2cc;strokeColor=#d6b656;"
RED = "rounded=1;whiteSpace=wrap;html=1;fillColor=#f8cecc;strokeColor=#b85450;"
GREY = "rounded=1;whiteSpace=wrap;html=1;fillColor=#f5f5f5;strokeColor=#666666;"
DB = "shape=cylinder3;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;"
DECISION = "rhombus;whiteSpace=wrap;html=1;fillColor=#e1d5e7;strokeColor=#9673a6;"
EDGE = "edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;jettySize=auto;html=1;labelBackgroundColor=#ffffff;fontSize=11;"


def node(i, label, x, y, style=BLUE, w=170, h=70):
    return {"id": str(i), "label": label, "x": x, "y": y, "style": style, "w": w, "h": h}


DIAGRAMS = {
    "system-context": (
        "VaultScale system context",
        [
            node(2, "Workspace user", 80, 180),
            node(3, "VaultScale\nMulti-tenant API operations workspace", 390, 160, ORANGE, 250, 100),
            node(4, "External HTTP API", 790, 180, GREY),
        ],
        [(2, 3, "uses in browser"), (3, 4, "runs allowed saved requests")],
    ),
    "container-architecture": (
        "VaultScale container architecture",
        [
            node(2, "Browser\nNext.js 16 App Router", 50, 250),
            node(3, "Nginx reverse proxy\npublic ingress", 290, 250, ORANGE),
            node(4, "VaultScale API\nSpring Boot 4 + Security", 540, 230, ORANGE, 220, 100),
            node(5, "Main PostgreSQL 16\nusers, orgs, collections,\nendpoints, request history", 850, 70, DB, 230, 120),
            node(6, "Redis 7\nmyOrgs cache\n60s TTL", 850, 240, GREEN, 200, 90),
            node(7, "Kafka\nvaultscale.audit.events", 850, 400, YELLOW, 200, 80),
            node(8, "Audit Service\nconsumer-only Spring Boot service", 1120, 400, PURPLE, 240, 90),
            node(9, "Audit PostgreSQL\naudit_db / audit_logs", 1430, 400, DB, 220, 90),
            node(10, "External HTTP API", 1120, 100, GREY, 210, 80),
            node(11, "Prometheus", 540, 500, GREEN),
            node(12, "Grafana", 760, 590, GREEN),
            node(13, "ZooKeeper\nKafka coordination", 1080, 590, GREY),
        ],
        [
            (2, 3, "HTTP"), (3, 4, "/api/v1"), (3, 2, "Next.js UI"),
            (4, 5, "JPA / Flyway"), (4, 6, "Spring Cache"), (4, 7, "publish audit event"),
            (7, 8, "consumer group"), (8, 9, "persist audit log"),
            (4, 10, "SSRF-guarded outbound HTTP"),
            (11, 4, "scrape /actuator/prometheus"), (12, 11, "query metrics"),
            (13, 7, "coordinates"),
        ],
    ),
    "compose-deployment": (
        "VaultScale local Compose deployment",
        [
            node(2, "Browser / Internet", 50, 220),
            node(3, "Nginx :80\nonly all-interface ingress", 300, 220, ORANGE, 220, 90),
            node(4, "Next.js :3000\nlocalhost bind", 580, 80),
            node(5, "Backend :8080\nlocalhost bind", 580, 260, ORANGE, 190, 90),
            node(6, "PostgreSQL 16\nprivate Compose network", 850, 80, DB, 210, 90),
            node(7, "Redis 7\nprivate Compose network", 850, 220, GREEN, 200, 80),
            node(8, "Kafka\ninternal :29092\nhost localhost:9092", 850, 380, YELLOW, 220, 100),
            node(9, "Audit Service :8085\nlocalhost bind", 1140, 380, PURPLE, 220, 90),
            node(10, "Audit PostgreSQL\nhost localhost:5434", 1420, 380, DB, 220, 90),
            node(11, "ZooKeeper\nhost localhost:2181", 1120, 550, GREY, 210, 80),
            node(12, "Prometheus\nlocalhost:9090", 560, 520, GREEN, 190, 80),
            node(13, "Grafana\nlocalhost:3001", 810, 620, GREEN, 190, 80),
        ],
        [
            (2, 3, "HTTP"), (3, 4, "UI"), (3, 5, "API + public health"),
            (5, 6, "JDBC"), (5, 7, "cache"), (5, 8, "Kafka internal listener"),
            (8, 9, "Kafka internal listener"), (9, 10, "JDBC"), (11, 8, "coordination"),
            (12, 5, "internal metrics scrape"), (13, 12, "dashboards"),
        ],
    ),
    "database-model": (
        "VaultScale database model",
        [
            node(2, "users\nPK id\nemail UNIQUE index\npassword hash\nfull_name", 70, 80, BLUE, 200, 150),
            node(3, "organizations\nPK id\nFK owner_id\nname\nslug UNIQUE", 370, 70, BLUE, 200, 150),
            node(4, "org_memberships\nPK id\nFK user_id\nFK organization_id\nrole\nUNIQUE(user, org)", 370, 310, BLUE, 220, 170),
            node(5, "collections\nPK id\norganization_id\ncreated_by\nname", 690, 80, BLUE, 210, 150),
            node(6, "endpoints\nPK id\ncollection_id\nmethod\nurl\nheaders JSONB", 1000, 70, BLUE, 210, 170),
            node(7, "request_history\nPK id\nendpoint_id\nexecuted_by\nstatus/body/time/error", 1000, 340, BLUE, 220, 180),
            node(8, "audit-postgres.audit_logs\nindependent service-owned DB\norganization_id + user_id values\naction + metadata JSONB\n(no cross-DB FK)", 680, 370, PURPLE, 260, 190),
        ],
        [(2, 3, "owner"), (2, 4, "member"), (3, 4, "membership"), (3, 5, "tenant"), (5, 6, "contains"), (6, 7, "run history")],
    ),
    "endpoint-execution-flow": (
        "Tenant-safe endpoint execution flow",
        [
            node(2, "Authenticated user", 30, 240),
            node(3, "POST .../{endpointId}/run\nRequestRunnerController", 250, 220, ORANGE, 230, 90),
            node(4, "RBAC check\nOWNER / ADMIN / MEMBER", 530, 80, DECISION, 210, 110),
            node(5, "Verify collection ∈ org\nand endpoint ∈ collection", 530, 260, DECISION, 220, 120),
            node(6, "SafeApiRequestValidator\nHTTP(S), no credentials,\nall resolved IPs preflight", 820, 230, DECISION, 240, 140),
            node(7, "Blocked / forbidden\ncontrolled error", 820, 500, RED, 200, 80),
            node(8, "CircuitBreaker\nexternalApiRunner", 1130, 220, YELLOW, 210, 90),
            node(9, "Java HttpClient\n5s connect, 10s request\nredirects disabled", 1400, 220, ORANGE, 220, 100),
            node(10, "Public external API", 1690, 220, GREY, 190, 80),
            node(11, "request_history\nresult / error / latency", 1390, 480, DB, 220, 100),
            node(12, "RunResultResponse", 1700, 480, GREEN, 190, 80),
        ],
        [
            (2, 3, "Bearer JWT"), (3, 4, "authorize"), (4, 7, "not allowed"),
            (4, 5, "allowed"), (5, 7, "nested ID mismatch"), (5, 6, "tenant-safe"),
            (6, 7, "unsafe URL"), (6, 8, "safe preflight"), (8, 9, "permission granted"),
            (8, 7, "OPEN: fail fast"), (9, 10, "outbound HTTP"),
            (9, 8, "network/5xx failure recorded"), (10, 11, "response snapshot"),
            (7, 11, "error snapshot"), (11, 12, "return"),
        ],
    ),
    "audit-event-pipeline": (
        "Audit event pipeline",
        [
            node(2, "Authenticated user", 30, 230),
            node(3, "POST /api/v1/orgs", 240, 230, ORANGE),
            node(4, "OrganizationService\nsave org + OWNER membership", 470, 210, ORANGE, 240, 90),
            node(5, "Main PostgreSQL", 780, 80, DB, 190, 80),
            node(6, "KafkaDomainEventPublisher\nORG_CREATED", 770, 270, BLUE, 220, 90),
            node(7, "Kafka topic\nvaultscale.audit.events", 1050, 270, YELLOW, 220, 80),
            node(8, "Audit Service\nconsumer-only\nvaultscale-audit-service group", 1330, 250, PURPLE, 240, 110),
            node(9, "Audit PostgreSQL\naudit_logs", 1640, 270, DB, 210, 80),
            node(10, "Audit service unavailable", 1050, 500, RED, 220, 70),
            node(11, "Kafka retains backlog\nconsumer resumes from committed offset", 1330, 490, GREEN, 260, 90),
        ],
        [
            (2, 3, "create org"), (3, 4, ""), (4, 5, "transactional writes"),
            (4, 6, "publish after saves"), (6, 7, "async"), (7, 8, "consume"),
            (8, 9, "persist"), (8, 10, "consumer stopped"),
            (10, 11, "events remain in Kafka"), (11, 8, "restart and drain lag"),
        ],
    ),
}


def render(title, nodes, edges):
    cells = ['<mxCell id="0"/>', '<mxCell id="1" parent="0"/>']
    for item in nodes:
        label = escape(item["label"]).replace("\n", "&lt;br&gt;")
        cells.append(
            f'<mxCell id="{item["id"]}" value="{label}" style="{item["style"]}" vertex="1" parent="1">'
            f'<mxGeometry x="{item["x"]}" y="{item["y"]}" width="{item["w"]}" height="{item["h"]}" as="geometry"/>'
            '</mxCell>'
        )
    for i, (source, target, label) in enumerate(edges, start=100):
        cells.append(
            f'<mxCell id="{i}" value="{escape(label)}" style="{EDGE}" edge="1" parent="1" source="{source}" target="{target}">'
            '<mxGeometry relative="1" as="geometry"/></mxCell>'
        )
    return (
        '<?xml version="1.0" encoding="UTF-8"?>\n'
        f'<mxfile host="drawio" version="30.0.4"><diagram name="{escape(title)}">'
        '<mxGraphModel dx="1600" dy="900" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="2400" pageHeight="1400" math="0" shadow="0"><root>'
        + ''.join(cells)
        + '</root></mxGraphModel></diagram></mxfile>\n'
    )


for filename, specification in DIAGRAMS.items():
    (OUT / f"{filename}.drawio").write_text(render(*specification), encoding="utf-8")
    print(f"wrote {filename}.drawio")
