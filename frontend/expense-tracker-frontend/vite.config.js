import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The Spring Boot backend runs on port 8081 (see application.properties).
// The dev server proxies /api to it, so no CORS configuration is needed on the backend.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
});
