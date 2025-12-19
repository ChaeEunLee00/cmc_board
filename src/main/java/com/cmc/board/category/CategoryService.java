package com.cmc.board.category;

import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import com.cmc.board.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    public Category createCategory(CategoryRequest request){
        Category category = new Category();
        category.setName(request.getName());

        return categoryRepository.save(category);
    }

    public List<Category> findCategories(){
        return  categoryRepository.findAllByOrderByNameAsc();
    }

    public void removeCategory(Long categoryId){
        // 카테고리 찾기
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CATEGORY_NOT_FOUND));

        // 카테고리를 참조하는 게시글이 없을 때만 삭제 가능
        if (postRepository.existsByCategoryCategoryId(categoryId)) {
            throw new BusinessLogicException(ExceptionCode.CATEGORY_CANNOT_BE_DELETED);
        }

        categoryRepository.delete(category);
    }
}
