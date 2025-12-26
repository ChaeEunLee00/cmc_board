import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  use: {
    baseURL: 'http://localhost:8080',
    headless: false,          // 브라우저 실제로 띄우기
    // launchOptions: { slowMo: 150 }, // 천천히 움직이는 거 보고 싶으면 켜기
  },
});
