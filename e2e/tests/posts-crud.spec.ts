import { test, expect } from '@playwright/test';

// 카테고리 생성 헬퍼
async function ensureCategory(page) {
  await page.evaluate(async () => {
    await fetch('/api/categories', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: 'E2E테스트' })
    });
  });
  // 카테고리 목록 새로고침
  await page.reload();
  await page.waitForLoadState('networkidle');
}

test.describe('게시글 CRUD E2E 테스트', () => {

  test.beforeEach(async ({ page }) => {
    // 로그인
    await page.goto('/');
    await page.getByRole('textbox', { name: '이메일' }).fill('admin@example.com');
    await page.getByPlaceholder('비밀번호를 입력하세요').fill('admin1234');
    await page.getByRole('button', { name: '로그인' }).click();
    await expect(page).toHaveURL(/\/posts/);
  });

  test('글 작성 → 목록에서 확인', async ({ page }) => {
    const uniqueTitle = `E2E 테스트 제목 ${Date.now()}`;
    const uniqueContent = `E2E 테스트 내용 ${Date.now()}`;

    // 글쓰기 페이지로 이동
    await page.getByRole('link', { name: '글 작성' }).click();
    await expect(page).toHaveURL(/\/posts\/new/);

    // 카테고리가 없으면 생성
    const categoryOptions = await page.locator('#categorySelect option:not([disabled])').count();
    if (categoryOptions === 0) {
      await ensureCategory(page);
      await page.getByRole('link', { name: '글 작성' }).click();
    }

    // 글 작성
    await page.locator('#title').fill(uniqueTitle);
    await page.locator('#content').fill(uniqueContent);
    await page.locator('#categorySelect').selectOption({ index: 1 });
    await page.getByRole('button', { name: '저장' }).click();

    // 목록 페이지로 이동 확인
    await expect(page).toHaveURL(/\/posts$/);

    // 목록에서 작성한 글 확인
    await expect(page.locator('table').first()).toContainText(uniqueTitle);
  });

  test('상세 → 수정 → 제목 변경 → 상세에서 변경 확인', async ({ page }) => {
    const updatedTitle = `수정된 제목 ${Date.now()}`;

    // 게시글이 없으면 먼저 생성
    const postLink = page.getByTestId('post-title-link').first();
    if (await postLink.count() === 0) {
      await page.getByRole('link', { name: '글 작성' }).click();
      const categoryOptions = await page.locator('#categorySelect option:not([disabled])').count();
      if (categoryOptions === 0) {
        await ensureCategory(page);
        await page.getByRole('link', { name: '글 작성' }).click();
      }
      await page.locator('#title').fill(`수정 테스트용 글 ${Date.now()}`);
      await page.locator('#content').fill('수정 테스트용 내용');
      await page.locator('#categorySelect').selectOption({ index: 1 });
      await page.getByRole('button', { name: '저장' }).click();
      await expect(page).toHaveURL(/\/posts$/);
    }

    // 첫 번째 게시글 상세로 이동
    await page.getByTestId('post-title-link').first().click();
    await expect(page).toHaveURL(/\/posts\/\d+/);

    // 기존 제목 저장
    const originalTitle = await page.locator('h1').first().textContent();

    // 수정 페이지로 이동
    await page.getByRole('link', { name: '수정' }).click();
    await expect(page).toHaveURL(/\/posts\/\d+\/edit/);

    // 제목 수정
    await page.locator('#title').clear();
    await page.locator('#title').fill(updatedTitle);
    await page.getByRole('button', { name: '저장' }).click();

    // 저장 후 이동 (목록 또는 상세 페이지)
    await expect(page).toHaveURL(/\/posts/);

    // 상세 페이지면 바로 확인, 목록이면 클릭 후 확인
    const currentUrl = page.url();
    if (currentUrl.match(/\/posts$/)) {
      await page.getByText(updatedTitle).click();
    }

    // 변경된 제목 확인
    await expect(page.locator('h1').first()).toContainText(updatedTitle);
    expect(updatedTitle).not.toBe(originalTitle);
  });

  test('삭제 → 목록에서 글이 사라짐 확인', async ({ page }) => {
    // 먼저 삭제할 글 생성
    const uniqueTitle = `삭제할 글 ${Date.now()}`;

    await page.getByRole('link', { name: '글 작성' }).click();

    // 카테고리가 없으면 생성
    const categoryOptions = await page.locator('#categorySelect option:not([disabled])').count();
    if (categoryOptions === 0) {
      await ensureCategory(page);
      await page.getByRole('link', { name: '글 작성' }).click();
    }

    await page.locator('#title').fill(uniqueTitle);
    await page.locator('#content').fill('삭제 테스트용 내용');
    await page.locator('#categorySelect').selectOption({ index: 1 });
    await page.getByRole('button', { name: '저장' }).click();
    await expect(page).toHaveURL(/\/posts$/);

    // 생성된 글 확인
    await expect(page.locator('table').first()).toContainText(uniqueTitle);

    // 해당 글 상세로 이동
    await page.getByText(uniqueTitle).click();
    await expect(page).toHaveURL(/\/posts\/\d+/);

    // 삭제 버튼 클릭
    page.on('dialog', dialog => dialog.accept());
    await page.getByRole('button', { name: '삭제' }).click();

    // 목록 페이지로 이동 확인
    await expect(page).toHaveURL(/\/posts$/);

    // 목록에서 글이 사라졌는지 확인
    await expect(page.locator('table').first()).not.toContainText(uniqueTitle);
  });

});
