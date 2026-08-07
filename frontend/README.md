# VaultScale Next.js frontend

The VaultScale web client uses Next.js 16 App Router, React 19, and TypeScript. Browser-facing routes are client components because the existing backend issues a JWT that is stored in browser `localStorage`.

## Routes

- `/login` and `/register` authenticate against the Spring Boot API.
- `/dashboard` lists and creates organization workspaces.
- `/orgs/[orgId]/collections` manages collections.
- `/orgs/[orgId]/collections/[collectionId]/endpoints` saves and runs endpoint definitions.

## API proxy

All browser calls use relative `/api/v1` URLs. App Router route handlers proxy `/api/*` and `/actuator/*` to `API_ORIGIN`, defaulting to `http://localhost:8080` for local development. The proxy preserves authentication but does not forward browser CORS headers, so it is valid for both the development server and a public Nginx hostname.

## Commands

```bash
npm install
npm run dev
npm run lint
npm run build
npm run start
```

Copy `.env.example` to `.env.local` only when the backend is not on `http://localhost:8080`.

The Dockerfile creates a non-root standalone Next.js runtime on port 3000. The repository reverse proxy targets `frontend:3000`.
