import { test, expect } from '@playwright/test';

test.describe('Smoke 테스트 (5개 핵심 시나리오)', () => {

  test('미로그인 상태로 /posts 접근 시 로그인 페이지로 이동', async ({ page }) => {
    await page.goto('/posts');

    // 로그인 페이지로 리다이렉트 확인
    await expect(page).toHaveURL(/\/(login)?$/);
    await expect(page.getByRole('button', { name: '로그인' })).toBeVisible();
  });

  test('로그인 성공 후 /posts 진입', async ({ page }) => {
    await page.goto('/');

    // 로그인
    await page.getByRole('textbox', { name: '이메일' }).fill('admin@example.com');
    await page.getByPlaceholder('비밀번호를 입력하세요').fill('admin1234');
    await page.getByRole('button', { name: '로그인' }).click();

    // /posts 페이지 도착 확인
    await expect(page).toHaveURL(/\/posts/);
    await expect(page.getByRole('heading', { name: '게시글 목록' })).toBeVisible();
  });

  test('목록에서 첫 글 클릭 → 상세 이동', async ({ page }) => {
    // 로그인
    await page.goto('/');
    await page.getByRole('textbox', { name: '이메일' }).fill('admin@example.com');
    await page.getByPlaceholder('비밀번호를 입력하세요').fill('admin1234');
    await page.getByRole('button', { name: '로그인' }).click();
    await expect(page).toHaveURL(/\/posts/);

    // 게시글이 없으면 먼저 생성
    const postLink = page.getByTestId('post-title-link').first();
    if (await postLink.count() === 0) {
      await page.getByRole('link', { name: '글 작성' }).click();
      await page.locator('#title').fill(`Smoke 테스트 글 ${Date.now()}`);
      await page.locator('#content').fill('테스트용 내용');
      // 카테고리 API로 생성 후 선택
      await page.evaluate(async () => {
        await fetch('/api/categories', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name: 'E2E테스트' })
        });
      });
      await page.locator('#categorySelect').selectOption({ index: 1 });
      await page.getByRole('button', { name: '저장' }).click();
      await expect(page).toHaveURL(/\/posts$/);
    }

    // 첫 번째 게시글 클릭
    await page.getByTestId('post-title-link').first().click();

    // 상세 페이지 이동 확인
    await expect(page).toHaveURL(/\/posts\/\d+/);
    await expect(page.getByRole('heading', { name: '댓글' })).toBeVisible();
  });

  test('댓글 등록 → 댓글이 화면에 보임', async ({ page }) => {
    // 로그인
    await page.goto('/');
    await page.getByRole('textbox', { name: '이메일' }).fill('admin@example.com');
    await page.getByPlaceholder('비밀번호를 입력하세요').fill('admin1234');
    await page.getByRole('button', { name: '로그인' }).click();
    await expect(page).toHaveURL(/\/posts/);

    // 게시글이 없으면 먼저 생성
    const postLink = page.getByTestId('post-title-link').first();
    if (await postLink.count() === 0) {
      await page.getByRole('link', { name: '글 작성' }).click();
      await page.locator('#title').fill(`Smoke 댓글 테스트 글 ${Date.now()}`);
      await page.locator('#content').fill('테스트용 내용');
      await page.evaluate(async () => {
        await fetch('/api/categories', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name: 'E2E테스트' })
        });
      });
      await page.locator('#categorySelect').selectOption({ index: 1 });
      await page.getByRole('button', { name: '저장' }).click();
      await expect(page).toHaveURL(/\/posts$/);
    }

    // 첫 번째 게시글로 이동
    await page.getByTestId('post-title-link').first().click();
    await expect(page).toHaveURL(/\/posts\/\d+/);

    // 댓글 등록
    const commentText = `Smoke 테스트 댓글 ${Date.now()}`;
    await page.getByTestId('comment-input').fill(commentText);
    await page.getByTestId('comment-submit').click();

    // 댓글이 화면에 보이는지 확인
    await expect(page.getByTestId('comment-list')).toContainText(commentText);
  });

  test('북마크 토글 → UI 상태 변경 확인', async ({ page }) => {
    // 로그인
    await page.goto('/');
    await page.getByRole('textbox', { name: '이메일' }).fill('admin@example.com');
    await page.getByPlaceholder('비밀번호를 입력하세요').fill('admin1234');
    await page.getByRole('button', { name: '로그인' }).click();
    await expect(page).toHaveURL(/\/posts/);

    // 게시글이 없으면 먼저 생성
    const postLink = page.getByTestId('post-title-link').first();
    if (await postLink.count() === 0) {
      await page.getByRole('link', { name: '글 작성' }).click();
      await page.locator('#title').fill(`Smoke 북마크 테스트 글 ${Date.now()}`);
      await page.locator('#content').fill('테스트용 내용');
      await page.evaluate(async () => {
        await fetch('/api/categories', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name: 'E2E테스트' })
        });
      });
      await page.locator('#categorySelect').selectOption({ index: 1 });
      await page.getByRole('button', { name: '저장' }).click();
      await expect(page).toHaveURL(/\/posts$/);
    }

    // 첫 번째 게시글로 이동
    await page.getByTestId('post-title-link').first().click();
    await expect(page).toHaveURL(/\/posts\/\d+/);

    // 북마크 버튼 찾기
    const bookmarkButton = page.locator('#bookmarkBtn');
    await expect(bookmarkButton).toBeVisible();

    // 현재 상태 확인
    const initialText = await page.locator('#bookmarkText').textContent();

    // 토글
    await bookmarkButton.click();
    await page.waitForResponse(response => response.url().includes('/api/') && response.url().includes('/bookmarks'));

    // 상태 변경 확인
    if (initialText === '북마크') {
      await expect(page.locator('#bookmarkText')).toHaveText('북마크됨');
    } else {
      await expect(page.locator('#bookmarkText')).toHaveText('북마크');
    }

    // 원복
    await bookmarkButton.click();
    await page.waitForResponse(response => response.url().includes('/api/') && response.url().includes('/bookmarks'));
    await expect(page.locator('#bookmarkText')).toHaveText(initialText!);
  });

});
