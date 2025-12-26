package com.cmc.board;

import com.cmc.board.category.domain.Category;
import com.cmc.board.category.repository.CategoryRepository;
import com.cmc.board.category.dto.CategoryRequest;
import com.cmc.board.category.service.CategoryService;
import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import com.cmc.board.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * [Narrative]
 * 관리자는 게시판의 분류를 위해 카테고리를 생성하고 관리할 수 있어야 한다.
 * 중복된 이름의 카테고리는 생성이 제한되며,
 * 데이터 무결성을 위해 게시글이 연결된 카테고리는 삭제할 수 없다.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Nested
    @DisplayName("카테고리 생성 테스트")
    class CreateCategoryTest {

        @Test
        @DisplayName("성공: 중복되지 않은 이름으로 카테고리를 생성한다.")
        void createCategory_Success() {
            // [Given]
            CategoryRequest request = new CategoryRequest("자유게시판");
            Category category = new Category();
            category.setName(request.getName());

            given(categoryRepository.existsByName(request.getName())).willReturn(false);
            given(categoryRepository.save(any(Category.class))).willReturn(category);

            // [When]
            Category result = categoryService.createCategory(request);

            // [Then]
            assertThat(result.getName()).isEqualTo("자유게시판");
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("실패: 이미 존재하는 카테고리 이름이면 CATEGORY_DUPLICATION 예외가 발생한다.")
        void createCategory_Fail_Duplicate() {
            // [Given]
            CategoryRequest request = new CategoryRequest("중복이름");
            given(categoryRepository.existsByName(request.getName())).willReturn(true);

            // [When & Then]
            BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                    () -> categoryService.createCategory(request));

            assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCode.CATEGORY_DUPLICATION);
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("카테고리 조회 테스트")
    class FindCategoryTest {
        @Test
        @DisplayName("성공: 모든 카테고리를 이름 오름차순으로 조회한다.")
        void findCategories_Success() {
            // [Given]
            given(categoryRepository.findAllByOrderByNameAsc()).willReturn(List.of(new Category(), new Category()));

            // [When]
            List<Category> result = categoryService.findCategories();

            // [Then]
            assertThat(result).hasSize(2);
            verify(categoryRepository, times(1)).findAllByOrderByNameAsc();
        }
    }

    @Nested
    @DisplayName("카테고리 삭제 테스트")
    class RemoveCategoryTest {

        @Test
        @DisplayName("성공: 게시글이 없는 카테고리를 삭제한다.")
        void removeCategory_Success() {
            // [Given]
            Long categoryId = 1L;
            Category category = new Category();

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
            given(postRepository.existsByCategoryCategoryId(categoryId)).willReturn(false);

            // [When]
            categoryService.removeCategory(categoryId);

            // [Then]
            verify(categoryRepository, times(1)).delete(category);
        }

        @Test
        @DisplayName("실패: 참조하는 게시글이 존재하면 CATEGORY_CANNOT_BE_DELETED 예외가 발생한다.")
        void removeCategory_Fail_HasPosts() {
            // [Given]
            Long categoryId = 1L;
            Category category = new Category();

            given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
            given(postRepository.existsByCategoryCategoryId(categoryId)).willReturn(true);

            // [When & Then]
            BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                    () -> categoryService.removeCategory(categoryId));

            assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCode.CATEGORY_CANNOT_BE_DELETED);
            verify(categoryRepository, never()).delete(any(Category.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 카테고리 삭제 시 CATEGORY_NOT_FOUND 예외가 발생한다.")
        void removeCategory_Fail_NotFound() {
            // [Given]
            given(categoryRepository.findById(anyLong())).willReturn(Optional.empty());

            // [When & Then]
            assertThrows(BusinessLogicException.class, () -> categoryService.removeCategory(99L));
        }
    }
}