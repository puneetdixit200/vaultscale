# Authentication and authorization

See [authentication and RBAC sequence](diagrams/authentication-rbac-sequence.drawio).

Registration and login return a JWT. The React client stores it in `localStorage`, attaches it as `Authorization: Bearer <token>`, and redirects to login after a 401. Spring Security is stateless; its JWT filter validates the token and loads the user before controllers handle protected routes. Organization-service operations enforce membership roles (`OWNER`, `ADMIN`, `MEMBER`, `VIEWER`) through `requireRole` where the service calls it.
