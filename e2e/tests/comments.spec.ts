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

    await page.locator('#title').fill(`댓글 테스트용 글 ${Date.now()}`);
    await page.locator('#content').fill('테스트용 내용');
    await page.locator('#categorySelect').selectOption({ index: 1 });
    await page.getByRole('button', { name: '저장' }).click();
    await expect(page).toHaveURL(/\/posts$/);
  }
}

test.describe('댓글/대댓글 E2E 테스트', () => {

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

  test('댓글 등록 → 화면에 보이는지 확인', async ({ page }) => {
    const commentText = `댓글 테스트 ${Date.now()}`;

    // 댓글 등록
    await page.getByTestId('comment-input').fill(commentText);

    // API 응답 대기
    const responsePromise = page.waitForResponse(
      response => response.url().includes('/api/comments') && response.request().method() === 'POST'
    );

    await page.getByTestId('comment-submit').click();
    await responsePromise;

    // 댓글이 화면에 보이는지 확인
    await expect(page.getByTestId('comment-list')).toContainText(commentText);
  });

  test('대댓글 회귀: 부모 댓글 → 대댓글 등록 → 대댓글 보기 클릭 → 화면에 보임', async ({ page }) => {
    // 1. 부모 댓글 생성
    const parentComment = `부모 댓글 ${Date.now()}`;
    await page.getByTestId('comment-input').fill(parentComment);

    const createCommentResponse = page.waitForResponse(
      response => response.url().includes('/api/comments') && response.request().method() === 'POST'
    );
    await page.getByTestId('comment-submit').click();
    await createCommentResponse;

    // 부모 댓글이 화면에 보이는지 확인
    await expect(page.getByTestId('comment-list')).toContainText(parentComment);

    // 2. 방금 생성된 댓글의 대댓글 입력 필드 찾기
    const replyInput = page.locator('[data-testid^="reply-input-"]').first();
    await expect(replyInput).toBeVisible();

    // 댓글 ID 추출
    const commentId = (await replyInput.getAttribute('data-testid'))?.replace('reply-input-', '');
    expect(commentId).toBeTruthy();

    // 3. 대댓글 등록
    const replyText = `대댓글 ${Date.now()}`;
    await replyInput.fill(replyText);

    const createReplyResponse = page.waitForResponse(
      response => response.url().includes('/api/comments') && response.request().method() === 'POST'
    );
    await page.getByTestId(`reply-submit-${commentId}`).click();
    await createReplyResponse;

    // 4. 대댓글 보기 버튼 클릭
    const toggleButton = page.getByTestId(`toggle-replies-${commentId}`);
    await expect(toggleButton).toBeVisible();
    await toggleButton.click();

    // 5. 대댓글이 화면에 보이는지 확인
    await expect(page.getByTestId('comment-list')).toContainText(replyText);
  });

  test('대댓글 여러 개 등록 → 대댓글 목록에 모두 표시', async ({ page }) => {
    // 부모 댓글 생성
    const parentComment = `다중 대댓글 테스트 ${Date.now()}`;
    await page.getByTestId('comment-input').fill(parentComment);

    const createCommentResponse = page.waitForResponse(
      response => response.url().includes('/api/comments') && response.request().method() === 'POST'
    );
    await page.getByTestId('comment-submit').click();
    await createCommentResponse;

    await expect(page.getByTestId('comment-list')).toContainText(parentComment);

    // 댓글 ID 추출
    let replyInput = page.locator('[data-testid^="reply-input-"]').first();
    await expect(replyInput).toBeVisible();
    let commentId = (await replyInput.getAttribute('data-testid'))?.replace('reply-input-', '');

    // 첫 번째 대댓글
    const reply1 = `첫번째 대댓글 ${Date.now()}`;
    await replyInput.fill(reply1);
    await page.getByTestId(`reply-submit-${commentId}`).click();
    await page.waitForResponse(
      response => response.url().includes('/api/comments') && response.request().method() === 'POST'
    );

    // 페이지 새로고침 후 다시 댓글 ID 확인
    replyInput = page.locator('[data-testid^="reply-input-"]').first();
    await expect(replyInput).toBeVisible();
    commentId = (await replyInput.getAttribute('data-testid'))?.replace('reply-input-', '');

    // 두 번째 대댓글
    const reply2 = `두번째 대댓글 ${Date.now()}`;
    await replyInput.fill(reply2);
    await page.getByTestId(`reply-submit-${commentId}`).click();
    await page.waitForResponse(
      response => response.url().includes('/api/comments') && response.request().method() === 'POST'
    );

    // 대댓글 보기 (다시 ID 확인)
    replyInput = page.locator('[data-testid^="reply-input-"]').first();
    commentId = (await replyInput.getAttribute('data-testid'))?.replace('reply-input-', '');
    const toggleButton = page.getByTestId(`toggle-replies-${commentId}`);
    await toggleButton.click();

    // 두 대댓글 모두 보이는지 확인
    const commentList = page.getByTestId('comment-list');
    await expect(commentList).toContainText(reply1);
    await expect(commentList).toContainText(reply2);
  });

});
