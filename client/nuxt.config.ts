export default defineNuxtConfig({
  ssr: false,
  modules: ['@pinia/nuxt'],
  experimental: {
    viteEnvironmentApi: true,
  },
  css: ['element-plus/dist/index.css', '@/assets/css/global.css'],
  app: {
    head: {
      title: '复习刷题系统',
      charset: 'utf-8',
      viewport: 'width=device-width, initial-scale=1',
    },
  },
  runtimeConfig: {
    public: {
      apiBase: 'http://localhost:8080/api',
    },
  },
  vite: {
    server: {
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
  },
  compatibilityDate: '2025-05-16',
})
