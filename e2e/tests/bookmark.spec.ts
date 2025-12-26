import { test, expect } from '@playwright/test';

// 게시글 생성 헬퍼
async function ensurePost(page) {
  const postLink = page.getByTestId('post-title-link').first();
  if (await postLink.count() === 0) {
    await page.getByRole('link', { name: '글 작성' }).click();

    // 카테고리가 없으면 생성
    const categoryOptions = await page.locator('#categorySelect option:not([disabled])').count();
    if (categoryOptions === 0) {
      await page.evaluate(async () => {
        await fetch('/api/categories', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name: 'E2E테스트' })
        });
      });
      // 글 작성 페이지 다시 로드
      await page.goto('/posts/new');
      await page.waitForLoadState('networkidle');
    }

    await page.locator('#title').fill(`북마크 테스트용 글 ${Date.now()}`);
    await page.locator('#content').fill('테스트용 내용');
    await page.locator('#categorySelect').selectOption({ index: 1 });
    await page.getByRole('button', { name: '저장' }).click();
    await expect(page).toHaveURL(/\/posts$/);
  }
}

test.describe('북마크 토글 E2E 테스트', () => {

  test.beforeEach(async ({ page }) => {
    // 로그인
    await page.goto('/');
    await page.getByRole('textbox', { name: '이메일' }).fill('admin@example.com');
    await page.getByPlaceholder('비밀번호를 입력하세요').fill('admin1234');
    await page.getByRole('button', { name: '로그인' }).click();
    await expect(page).toHaveURL(/\/posts/);

    // 게시글이 없으면 생성
    await ensurePost(page);

    // 첫 번째 게시글 상세로 이동
    await page.getByTestId('post-title-link').first().click();
    await expect(page).toHaveURL(/\/posts\/\d+/);
  });

  test('북마크 클릭 → 북마크됨 UI 확인 → 다시 클릭 → 원복', async ({ page }) => {
    const bookmarkButton = page.locator('#bookmarkBtn');
    const bookmarkText = page.locator('#bookmarkText');
    await expect(bookmarkButton).toBeVisible();

    // 초기 상태 저장
    const initialText = await bookmarkText.textContent();

    // 첫 번째 클릭 - 토글
    const toggleResponse1 = page.waitForResponse(
      response => response.url().includes('/api/') && response.url().includes('/bookmarks')
    );
    await bookmarkButton.click();
    await toggleResponse1;

    // 상태 변경 확인
    if (initialText === '북마크') {
      await expect(bookmarkText).toHaveText('북마크됨');
    } else {
      await expect(bookmarkText).toHaveText('북마크');
    }

    // 두 번째 클릭 - 원복
    const toggleResponse2 = page.waitForResponse(
      response => response.url().includes('/api/') && response.url().includes('/bookmarks')
    );
    await bookmarkButton.click();
    await toggleResponse2;

    // 원래 상태로 복구 확인
    await expect(bookmarkText).toHaveText(initialText!);
  });

  test('북마크 토글 시 API 요청 성공 확인', async ({ page }) => {
    const bookmarkButton = page.locator('#bookmarkBtn');
    await expect(bookmarkButton).toBeVisible();

    // 토글 시 API 응답 확인
    const responsePromise = page.waitForResponse(
      response => response.url().includes('/api/') && response.url().includes('/bookmarks')
    );

    await bookmarkButton.click();
    const response = await responsePromise;

    // API 성공 확인
    expect(response.status()).toBe(200);
  });

});
