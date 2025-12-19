package com.cmc.board.post;

import com.cmc.board.category.Category;
import com.cmc.board.category.CategoryRepository;
import com.cmc.board.common.exception.BusinessLogicException;
import com.cmc.board.common.exception.ExceptionCode;
import com.cmc.board.user.User;
import com.cmc.board.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public PostResponse createPost(PostRequest request, String email){
        // 입력값 검증
        if (request.getTitle() == null || request.getContent() == null || request.getCategory() == null) {
            throw new BusinessLogicException(ExceptionCode.INPUT_CANNOT_BE_NULL);
        }

        // 작성자 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

        // 카테고리 찾기
        Category category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CATEGORY_NOT_FOUND));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUser(user);
        post.setCategory(category);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        postRepository.save(post);

        // 응답값 생성
        PostResponse response = new PostResponse();
        response.setPostId(post.getPostId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setUser(post.getUser().getNickname());
        response.setCategory(post.getCategory().getName());
        return response;
    }

    public PostResponse findPost(Long postId){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.POST_NOT_FOUND));

        // 응답값 생성
        PostResponse response = new PostResponse();
        response.setPostId(post.getPostId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setUser(post.getUser().getNickname());
        response.setCategory(post.getCategory().getName());
        return response;
    }

    public List<PostResponse> findPosts(int page, int size) {
        // 최신순 정렬
        Pageable pageable = PageRequest.of(page, size, Sort.by("postId").descending());
        Page<Post> postPage = postRepository.findAll(pageable);


        // 응답값 생성
        return postPage.map(post -> {
            PostResponse response = new PostResponse();
            response.setPostId(post.getPostId());
            response.setTitle(post.getTitle());
            response.setContent(post.getContent());
            response.setCreatedAt(post.getCreatedAt());
            response.setUpdatedAt(post.getUpdatedAt());
            response.setUser(post.getUser().getNickname());
            response.setCategory(post.getCategory().getName());
            return response;
        }).getContent();
    }

    public PostResponse updatePost(Long postId, PostRequest request, String email){
        // 게시글 찾기
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.POST_NOT_FOUND));

        // 작성자 본인이 맞는지 확인
        if(!post.getUser().getEmail().equals(email)){
            throw new BusinessLogicException(ExceptionCode.NOT_AUTHORIZED);
        }

        if(request.getTitle() != null) post.setTitle(request.getTitle());
        if(request.getContent() != null) post.setContent(request.getContent());
        if(request.getCategory() != null) {
            // 카테고리 찾기
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CATEGORY_NOT_FOUND));
            post.setCategory(category);
        }
        post.setUpdatedAt(LocalDateTime.now());

        postRepository.save(post);

        // 응답값 생성
        PostResponse response = new PostResponse();
        response.setPostId(post.getPostId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setUser(post.getUser().getNickname());
        response.setCategory(post.getCategory().getName());
        return response;
    }

    public void removePost(Long postId, String email){
        // 게시글 찾기
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.POST_NOT_FOUND));

        // 작성자 본인이 맞는지 확인
        if(!post.getUser().getEmail().equals(email)){
            throw new BusinessLogicException(ExceptionCode.NOT_AUTHORIZED);
        }

        postRepository.delete(post);
    }

}
