import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Dev server proxies /api AND /uploads calls to the Spring Boot backend
// on :8080. /api covers every REST endpoint; /uploads is needed
// separately because FileStorageService returns image URLs shaped like
// "/uploads/complaints/<file>.jpg" (served by WebMvcConfig's static
// resource handler, not under the "/api" prefix) — without this second
// proxy entry, every uploaded complaint/collection photo would 404 in
// local dev even though the backend is serving it correctly.
//
// esbuild.loader / optimizeDeps.esbuildOptions.loader: every React
// component in this project is named ".js" (not ".jsx") but contains
// JSX syntax. @vitejs/plugin-react already knows how to transform JSX
// during the main build, but Vite's separate dependency-pre-bundling
// scan (the step that runs first, before your own code, to find and
// cache node_modules dependencies) uses raw esbuild directly and does
// NOT go through that plugin — it needs to be told explicitly, via its
// own loader map, to treat ".js" files as JSX too. Without this,
// "npm run dev" fails immediately with "The JSX syntax extension is
// not currently enabled" on every .js file containing a JSX tag.
export default defineConfig({
  plugins: [react()],
  esbuild: {
    loader: 'jsx',
    include: /src\/.*\.js$/,
    exclude: [],
  },
  optimizeDeps: {
    esbuildOptions: {
      loader: {
        '.js': 'jsx',
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});