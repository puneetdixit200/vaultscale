// frontend/src/main.tsx
// This is the actual entry point — the FIRST JS that runs in the browser.
// It finds the <div id="root"> in index.html and mounts our React app into it.

import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
