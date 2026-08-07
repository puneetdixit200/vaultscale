#!/usr/bin/env python3
"""Small no-dependency Draw.io generator used when Graphviz/Mermaid import is unavailable."""
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
        [node(2, "Workspace user", 80, 180, BLUE), node(3, "VaultScale\nMulti-tenant API request workspace", 390, 160, ORANGE, 250, 100), node(4, "External HTTP API", 790, 180, GREY)],
        [(2, 3, "uses in browser"), (3, 4, "runs allowed saved requests")],
    ),
    "container-architecture": (
        "VaultScale container architecture",
        [node(2, "Browser SPA\nNext.js 16 App Router", 60, 260), node(3, "Nginx reverse proxy\nintended ingress", 300, 260, ORANGE), node(4, "VaultScale API\nSpring Boot + Spring Security", 540, 250, ORANGE, 210, 90), node(5, "PostgreSQL 16\nusers, orgs, endpoints, history", 830, 100, DB, 220, 90), node(6, "Kafka\nvaultscale.audit.events", 830, 310, YELLOW), node(7, "Audit Service\nstandalone Kafka consumer :8085", 1090, 310, PURPLE, 220, 90), node(8, "Audit PostgreSQL\naudit_db / audit_logs", 1390, 310, DB, 220, 90), node(9, "External HTTP API", 1090, 100, GREY), node(10, "Redis\ndeclared and configured; no source integration found", 540, 490, GREY, 220, 80), node(11, "ZooKeeper\nKafka coordination", 830, 500, GREY)],
        [(2, 3, "browser traffic"), (3, 4, "/api and /actuator"), (4, 5, "JPA / Flyway"), (4, 6, "publishes audit event"), (6, 7, "consumes"), (7, 8, "writes audit_logs"), (4, 9, "validated request"), (11, 6, "coordinates")],
    ),
    "compose-deployment": (
        "Declared Docker Compose deployment",
        [node(2, "Browser / Internet", 70, 220), node(3, "Nginx reverse proxy\n:80", 300, 220, ORANGE), node(4, "Next.js standalone\nApp Router :3000", 550, 100), node(5, "Spring Boot backend\n:8080", 550, 300, ORANGE), node(6, "PostgreSQL 16\npostgres_data volume", 830, 140, DB), node(7, "Kafka :9092", 830, 300, YELLOW), node(8, "Audit Service\n:8085", 1090, 300, PURPLE), node(9, "Audit PostgreSQL\naudit_postgres_data", 1330, 300, DB), node(10, "Redis 7", 830, 500, GREY), node(11, "ZooKeeper :2181", 1080, 500, GREY), node(12, "Prometheus :9090", 540, 540, GREEN), node(13, "Grafana :3001", 790, 650, GREEN), node(14, "Terraform\nOracle Cloud VM + security list", 70, 500, GREY)],
        [(2, 3, "HTTP"), (3, 4, "UI"), (3, 5, "/api, /actuator"), (5, 6, "JDBC"), (5, 7, "publishes events"), (7, 8, "consumes"), (8, 9, "JDBC"), (5, 10, "configured host"), (11, 7, "coordination"), (12, 5, "scrapes actuator"), (12, 8, "scrapes actuator"), (13, 12, "queries metrics"), (14, 3, "provisions VM ingress")],
    ),
    "database-model": (
        "VaultScale database model",
        [node(2, "users\nPK id\nemail\npassword\nfull_name", 70, 80, BLUE, 190, 140), node(3, "organizations\nPK id\nFK owner_id\nname\nslug", 370, 70, BLUE, 190, 140), node(4, "org_memberships\nPK id\nFK user_id\nFK organization_id\nrole", 370, 300, BLUE, 210, 150), node(5, "collections\nPK id\nFK organization_id\nFK created_by\nname", 680, 80, BLUE, 210, 150), node(6, "endpoints\nPK id\nFK collection_id\nmethod\nurl\nheaders JSONB", 990, 70, BLUE, 210, 170), node(7, "request_history\nPK id\nFK endpoint_id\nFK executed_by\nstatus_code\nresponse_time_ms", 990, 330, BLUE, 220, 180), node(8, "audit-postgres / audit_logs\nPK id\norganization_id (event value)\nuser_id\naction\nmetadata JSONB\n(no cross-DB FK)", 680, 350, PURPLE, 250, 190)],
        [(2, 3, ""), (2, 4, ""), (3, 4, ""), (3, 5, ""), (5, 6, ""), (6, 7, "")],
    ),
    "endpoint-execution-flow": (
        "Protected endpoint execution flow",
        [node(2, "Authenticated user", 40, 230), node(3, "POST endpoint /run\nRequestRunnerController", 260, 220, ORANGE), node(4, "Load saved endpoint", 500, 220), node(5, "SafeApiRequestValidator\nHTTP(S), resolve host, reject private/reserved IPs", 730, 190, DECISION, 230, 130), node(6, "Blocked result\nSave error history", 730, 420, RED), node(7, "Java HttpClient\n5s connect; 10s timeout", 1040, 190), node(8, "Public external HTTP API", 1280, 190, GREY), node(9, "request_history\nstatus/body/time snapshot", 1040, 420, DB), node(10, "Resilience4j circuit breaker\nopen/exception fast fallback", 1280, 420, YELLOW, 210, 80), node(11, "RunResultResponse", 1540, 310, GREEN)],
        [(2, 3, "JWT-protected request"), (3, 4, ""), (4, 5, ""), (5, 6, "unsafe"), (6, 9, "save error"), (5, 7, "safe"), (7, 8, "HTTP request"), (8, 9, "status/body/time"), (7, 10, "failure"), (10, 11, "fast fallback"), (9, 11, "result")],
    ),
    "audit-event-pipeline": (
        "Implemented audit event pipeline",
        [node(2, "Authenticated user", 40, 230), node(3, "POST /api/v1/orgs", 250, 230, ORANGE), node(4, "OrganizationService\ntransactionally saves org + OWNER membership", 480, 210, ORANGE, 230, 90), node(5, "PostgreSQL\norganizations, org_memberships", 770, 80, DB, 210, 90), node(6, "KafkaDomainEventPublisher\nORG_CREATED", 770, 270), node(7, "Kafka topic\nvaultscale.audit.events", 1040, 270, YELLOW), node(8, "Audit Service\nstandalone consumer :8085", 1280, 270, PURPLE, 210, 80), node(9, "Audit PostgreSQL\naudit_db.audit_logs", 1540, 270, DB, 210, 80), node(10, "Publish failure\nlog error; do not fail request", 1040, 480, RED)],
        [(2, 3, "create organization"), (3, 4, ""), (4, 5, "commit data"), (4, 6, "after saves"), (6, 7, "async publish"), (6, 10, "broker unavailable"), (7, 8, "deserialize DomainEvent"), (8, 9, "persist audit record")],
    ),
}

def render(title, nodes, edges):
    by_id = {n["id"]: n for n in nodes}
    cells = ['<mxCell id="0"/>', '<mxCell id="1" parent="0"/>']
    for n in nodes:
        label = escape(n["label"]).replace("\n", "&lt;br&gt;")
        cells.append(f'<mxCell id="{n["id"]}" value="{label}" style="{n["style"]}" vertex="1" parent="1"><mxGeometry x="{n["x"]}" y="{n["y"]}" width="{n["w"]}" height="{n["h"]}" as="geometry"/></mxCell>')
    for i, (src, target, label) in enumerate(edges, start=100):
        value = escape(label)
        cells.append(f'<mxCell id="{i}" value="{value}" style="{EDGE}" edge="1" parent="1" source="{src}" target="{target}"><mxGeometry relative="1" as="geometry"/></mxCell>')
    return f'''<?xml version="1.0" encoding="UTF-8"?>
<mxfile host="drawio" version="30.0.4"><diagram name="{escape(title)}"><mxGraphModel dx="1600" dy="900" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="2400" pageHeight="1400" math="0" shadow="0"><root>{''.join(cells)}</root></mxGraphModel></diagram></mxfile>\n'''

for filename, spec in DIAGRAMS.items():
    (OUT / f"{filename}.drawio").write_text(render(*spec), encoding="utf-8")
    print(f"wrote {filename}.drawio")
