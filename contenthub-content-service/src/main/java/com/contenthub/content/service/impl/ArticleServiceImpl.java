package com.contenthub.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contenthub.common.exception.BusinessException;
import com.contenthub.common.result.Result;
import com.contenthub.content.domain.*;
import com.contenthub.content.dto.CreateArticleDTO;
import com.contenthub.content.dto.UpdateArticleDTO;
import com.contenthub.content.feign.CommentServiceClient;
import com.contenthub.content.feign.FileServiceClient;
import com.contenthub.content.feign.UserServiceClient;
import com.contenthub.content.mapper.*;
import com.contenthub.content.service.ArticleService;
import com.contenthub.content.vo.ArticleVO;
import com.contenthub.content.vo.CategoryVO;
import com.contenthub.content.vo.TagVO;
import com.contenthub.user.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    
    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleCollectionMapper articleCollectionMapper;
    private final UserServiceClient userServiceClient;
    private final CommentServiceClient commentServiceClient;
    private final FileServiceClient fileServiceClient;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArticle(Long userId, CreateArticleDTO createArticleDTO) {
        // 1. 验证标题和内容
        if (StrUtil.isBlank(createArticleDTO.getTitle())) {
            throw new BusinessException("标题不能为空");
        }
        if (StrUtil.isBlank(createArticleDTO.getContent())) {
            throw new BusinessException("内容不能为空");
        }
        
        // 2. 验证分类是否存在（如果提供了分类ID）
        if (createArticleDTO.getCategoryId() != null) {
            Category category = categoryMapper.selectById(createArticleDTO.getCategoryId());
            if (category == null || category.getStatus() == 0) {
                throw new BusinessException("分类不存在或已禁用");
            }
        }
        
        // 3. 生成slug（如果没有提供）
        String slug = createArticleDTO.getTitle();
        if (StrUtil.isNotBlank(slug)) {
            // 简单的slug生成（可以后续优化）
            slug = slug.toLowerCase()
                    .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");
            if (slug.length() > 200) {
                slug = slug.substring(0, 200);
            }
        }
        
        // 检查slug是否已存在
        LambdaQueryWrapper<Article> slugWrapper = new LambdaQueryWrapper<>();
        slugWrapper.eq(Article::getSlug, slug);
        Article existingArticle = articleMapper.selectOne(slugWrapper);
        if (existingArticle != null) {
            slug = slug + "-" + System.currentTimeMillis();
        }
        
        // 4. 生成摘要（如果没有提供）
        String summary = createArticleDTO.getSummary();
        if (StrUtil.isBlank(summary) && StrUtil.isNotBlank(createArticleDTO.getContent())) {
            // 从内容中提取前200字作为摘要
            String plainText = createArticleDTO.getContent()
                    .replaceAll("#+\\s*", "") // 移除Markdown标题
                    .replaceAll("\\*+", "")   // 移除Markdown格式
                    .replaceAll("\\[.*?\\]\\(.*?\\)", "") // 移除Markdown链接
                    .replaceAll("\n", " ");  // 换行符替换为空格
            summary = plainText.length() > 200 ? plainText.substring(0, 200) + "..." : plainText;
        }
        
        // 5. 处理封面图片上传（如果提供了文件）
        String coverImageUrl = createArticleDTO.getCoverImage();
        // 注意：由于DTO中coverImage是String类型，这里假设已经通过Controller处理上传后传入URL
        // 如果需要在Service中处理文件上传，需要修改方法签名，接收MultipartFile参数
        
        // 6. 转换Markdown为HTML（简单处理，可以后续使用专门的Markdown库）
        String htmlContent = convertMarkdownToHtml(createArticleDTO.getContent());
        
        // 7. 创建文章
        Article article = new Article();
        article.setUserId(userId);
        article.setCategoryId(createArticleDTO.getCategoryId());
        article.setTitle(createArticleDTO.getTitle());
        article.setSlug(slug);
        article.setSummary(summary);
        article.setCoverImage(coverImageUrl);
        article.setContent(createArticleDTO.getContent());
        article.setHtmlContent(htmlContent);
        article.setStatus(createArticleDTO.getStatus() != null ? createArticleDTO.getStatus() : 0); // 默认草稿
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setCollectCount(0);
        article.setShareCount(0);
        article.setIsTop(0);
        article.setIsRecommend(0);
        
        // 如果状态是已发布，设置发布时间
        if (article.getStatus() == 1) {
            article.setPublishedAt(LocalDateTime.now());
        }
        
        articleMapper.insert(article);
        
        // 8. 处理标签
        if (createArticleDTO.getTagIds() != null && !createArticleDTO.getTagIds().isEmpty()) {
            saveArticleTags(article.getId(), createArticleDTO.getTagIds());
        }
        
        log.info("创建文章成功: articleId={}, userId={}, title={}", 
                article.getId(), userId, createArticleDTO.getTitle());
        
        return article.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long articleId, Long userId, UpdateArticleDTO updateArticleDTO) {
        // 1. 查询文章
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        
        // 2. 验证权限（只能修改自己的文章）
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("只能修改自己的文章");
        }
        
        // 3. 验证分类（如果更新了分类ID）
        if (updateArticleDTO.getCategoryId() != null && !updateArticleDTO.getCategoryId().equals(article.getCategoryId())) {
            Category category = categoryMapper.selectById(updateArticleDTO.getCategoryId());
            if (category == null || category.getStatus() == 0) {
                throw new BusinessException("分类不存在或已禁用");
            }
        }
        
        // 4. 更新文章字段
        if (StrUtil.isNotBlank(updateArticleDTO.getTitle())) {
            article.setTitle(updateArticleDTO.getTitle());
        }
        if (StrUtil.isNotBlank(updateArticleDTO.getContent())) {
            article.setContent(updateArticleDTO.getContent());
            article.setHtmlContent(convertMarkdownToHtml(updateArticleDTO.getContent()));
        }
        if (StrUtil.isNotBlank(updateArticleDTO.getSummary())) {
            article.setSummary(updateArticleDTO.getSummary());
        }
        if (updateArticleDTO.getCategoryId() != null) {
            article.setCategoryId(updateArticleDTO.getCategoryId());
        }
        if (StrUtil.isNotBlank(updateArticleDTO.getCoverImage())) {
            article.setCoverImage(updateArticleDTO.getCoverImage());
        }
        if (updateArticleDTO.getStatus() != null) {
            article.setStatus(updateArticleDTO.getStatus());
            // 如果从草稿变为已发布，设置发布时间
            if (updateArticleDTO.getStatus() == 1 && article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }
        }
        
        articleMapper.updateById(article);
        
        // 5. 更新标签
        if (updateArticleDTO.getTagIds() != null) {
            // 删除旧标签关联
            LambdaQueryWrapper<ArticleTag> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(ArticleTag::getArticleId, articleId);
            articleTagMapper.delete(deleteWrapper);
            
            // 添加新标签关联
            if (!updateArticleDTO.getTagIds().isEmpty()) {
                saveArticleTags(articleId, updateArticleDTO.getTagIds());
            }
        }
        
        log.info("更新文章成功: articleId={}, userId={}", articleId, userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublishArticle(Long articleId, Long userId) {
        // 1. 查询文章
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        
        // 2. 验证权限（只能下架自己的文章）
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("只能下架自己的文章");
        }
        
        // 3. 下架文章：将状态改为草稿（status=0），不删除
        article.setStatus(0);
        articleMapper.updateById(article);
        
        log.info("下架文章成功: articleId={}, userId={}", articleId, userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long articleId, Long userId) {
        // 1. 验证文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        
        // 2. 验证当前用户是否为管理员
        try {
            Result<UserInfoVO> userResult = userServiceClient.getUserById(userId);
            if (userResult == null || userResult.getCode() != 200 || userResult.getData() == null) {
                throw new BusinessException("获取用户信息失败");
            }
            
            UserInfoVO userInfo = userResult.getData();
            if (!"admin".equals(userInfo.getRole())) {
                throw new BusinessException("无权限操作，只有管理员可以删除文章");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用用户服务失败: userId={}, error={}", userId, e.getMessage());
            throw new BusinessException("验证管理员权限失败");
        }
        
        // 3. 软删除文章（MyBatis-Plus 自动处理）
        articleMapper.deleteById(articleId);
        
        log.info("管理员删除文章成功: articleId={}, adminUserId={}", articleId, userId);
    }
    
    @Override
    public ArticleVO getArticleDetail(Long articleId, Long currentUserId) {
        // 1. 查询文章
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        
        // 2. 如果文章未发布且不是作者，不能查看
        if (article.getStatus() != 1 && (currentUserId == null || !article.getUserId().equals(currentUserId))) {
            throw new BusinessException("文章不存在或未发布");
        }
        
        // 3. 转换VO
        ArticleVO vo = convertToVO(article, currentUserId);
        
        // 4. 增加浏览量（异步，不阻塞返回）
        incrementViewCount(articleId);
        
        return vo;
    }
    
    @Override
    public List<ArticleVO> getArticleList(Long categoryId, Long tagId, String keyword, 
                                         Integer status, String sortBy, Integer page, 
                                         Integer size, Long currentUserId) {
        // 设置分页
        Page<Article> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 20);
        
        // 构建查询条件
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        
        // 分类筛选
        if (categoryId != null) {
            queryWrapper.eq(Article::getCategoryId, categoryId);
        }
        
        // 状态筛选（默认只查询已发布的文章）
        if (status != null) {
            queryWrapper.eq(Article::getStatus, status);
        } else {
            queryWrapper.eq(Article::getStatus, 1); // 默认只查询已发布的
        }
        
        // 关键词搜索（标题和摘要）
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Article::getTitle, keyword)
                    .or()
                    .like(Article::getSummary, keyword));
        }
        
        // 标签筛选（通过关联表查询）
        if (tagId != null) {
            LambdaQueryWrapper<ArticleTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(ArticleTag::getTagId, tagId);
            List<ArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
            List<Long> articleIds = articleTags.stream()
                    .map(ArticleTag::getArticleId)
                    .collect(Collectors.toList());
            if (articleIds.isEmpty()) {
                return Collections.emptyList();
            }
            queryWrapper.in(Article::getId, articleIds);
        }
        
        // 排序
        if ("hot".equals(sortBy)) {
            // 热门：综合浏览量、点赞数、评论数
            queryWrapper.orderByDesc(Article::getViewCount)
                       .orderByDesc(Article::getLikeCount)
                       .orderByDesc(Article::getCommentCount)
                       .orderByDesc(Article::getPublishedAt);
        } else if ("likes".equals(sortBy)) {
            // 按点赞数
            queryWrapper.orderByDesc(Article::getLikeCount)
                       .orderByDesc(Article::getPublishedAt);
        } else if ("views".equals(sortBy)) {
            // 按浏览量
            queryWrapper.orderByDesc(Article::getViewCount)
                       .orderByDesc(Article::getPublishedAt);
        } else if ("recommend".equals(sortBy)) {
            // 推荐文章（置顶 + 推荐）
            queryWrapper.orderByDesc(Article::getIsTop)
                       .orderByDesc(Article::getIsRecommend)
                       .orderByDesc(Article::getPublishedAt);
        } else {
            // 默认：按发布时间（最新）
            queryWrapper.orderByDesc(Article::getIsTop) // 置顶优先
                       .orderByDesc(Article::getPublishedAt);
        }
        
        // 执行查询
        IPage<Article> pageResult = articleMapper.selectPage(pageParam, queryWrapper);
        List<Article> articles = pageResult.getRecords();
        
        if (articles.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 转换为VO
        return articles.stream()
                .map(article -> convertToVO(article, currentUserId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ArticleVO> getMyArticleList(Long userId, Integer status, Integer page, Integer size) {
        Page<Article> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 20);
        
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getUserId, userId);
        
        if (status != null) {
            queryWrapper.eq(Article::getStatus, status);
        }
        
        queryWrapper.orderByDesc(Article::getCreatedAt);
        
        IPage<Article> pageResult = articleMapper.selectPage(pageParam, queryWrapper);
        
        return pageResult.getRecords().stream()
                .map(article -> convertToVO(article, userId))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeArticle(Long articleId, Long userId) {
        // 1. 验证文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        
        // 2. 检查是否已点赞
        LambdaQueryWrapper<ArticleLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLike::getArticleId, articleId)
                   .eq(ArticleLike::getUserId, userId);
        ArticleLike existing = articleLikeMapper.selectOne(queryWrapper);
        
        if (existing != null) {
            throw new BusinessException("已经点赞过该文章");
        }
        
        // 3. 创建点赞记录
        ArticleLike articleLike = new ArticleLike();
        articleLike.setArticleId(articleId);
        articleLike.setUserId(userId);
        articleLike.setCreatedAt(LocalDateTime.now());
        articleLikeMapper.insert(articleLike);
        
        // 4. 更新文章点赞数
        articleMapper.incrementLikeCount(articleId);
        
        log.info("点赞文章成功: articleId={}, userId={}", articleId, userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeArticle(Long articleId, Long userId) {
        // 1. 查询点赞记录
        LambdaQueryWrapper<ArticleLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLike::getArticleId, articleId)
                   .eq(ArticleLike::getUserId, userId);
        ArticleLike articleLike = articleLikeMapper.selectOne(queryWrapper);
        
        if (articleLike == null) {
            throw new BusinessException("未点赞该文章");
        }
        
        // 2. 删除点赞记录
        articleLikeMapper.deleteById(articleLike.getId());
        
        // 3. 更新文章点赞数
        articleMapper.decrementLikeCount(articleId);
        
        log.info("取消点赞成功: articleId={}, userId={}", articleId, userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectArticle(Long articleId, Long userId) {
        // 1. 验证文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        
        // 2. 检查是否已收藏
        LambdaQueryWrapper<ArticleCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleCollection::getArticleId, articleId)
                   .eq(ArticleCollection::getUserId, userId);
        ArticleCollection existing = articleCollectionMapper.selectOne(queryWrapper);
        
        if (existing != null) {
            throw new BusinessException("已经收藏过该文章");
        }
        
        // 3. 创建收藏记录
        ArticleCollection articleCollection = new ArticleCollection();
        articleCollection.setArticleId(articleId);
        articleCollection.setUserId(userId);
        articleCollection.setCreatedAt(LocalDateTime.now());
        articleCollectionMapper.insert(articleCollection);
        
        // 4. 更新文章收藏数
        articleMapper.incrementCollectCount(articleId);
        
        log.info("收藏文章成功: articleId={}, userId={}", articleId, userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectArticle(Long articleId, Long userId) {
        // 1. 查询收藏记录
        LambdaQueryWrapper<ArticleCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleCollection::getArticleId, articleId)
                   .eq(ArticleCollection::getUserId, userId);
        ArticleCollection articleCollection = articleCollectionMapper.selectOne(queryWrapper);
        
        if (articleCollection == null) {
            throw new BusinessException("未收藏该文章");
        }
        
        // 2. 删除收藏记录
        articleCollectionMapper.deleteById(articleCollection.getId());
        
        // 3. 更新文章收藏数
        articleMapper.decrementCollectCount(articleId);
        
        log.info("取消收藏成功: articleId={}, userId={}", articleId, userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shareArticle(Long articleId) {
        articleMapper.incrementShareCount(articleId);
        log.debug("增加分享数: articleId={}", articleId);
    }
    
    @Override
    public void incrementViewCount(Long articleId) {
        articleMapper.incrementViewCount(articleId);
    }
    
    @Override
    public boolean isLiked(Long articleId, Long userId) {
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<ArticleLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLike::getArticleId, articleId)
                   .eq(ArticleLike::getUserId, userId);
        return articleLikeMapper.selectOne(queryWrapper) != null;
    }
    
    @Override
    public boolean isCollected(Long articleId, Long userId) {
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<ArticleCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleCollection::getArticleId, articleId)
                   .eq(ArticleCollection::getUserId, userId);
        return articleCollectionMapper.selectOne(queryWrapper) != null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setTopArticle(Long articleId, boolean isTop) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        article.setIsTop(isTop ? 1 : 0);
        articleMapper.updateById(article);
        log.info("设置文章置顶: articleId={}, isTop={}", articleId, isTop);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setRecommendArticle(Long articleId, boolean isRecommend) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        article.setIsRecommend(isRecommend ? 1 : 0);
        articleMapper.updateById(article);
        log.info("设置文章推荐: articleId={}, isRecommend={}", articleId, isRecommend);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticleStatus(Long articleId, Integer status, Long userId) {
        // 1. 验证文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        
        // 2. 验证当前用户是否为管理员
        try {
            Result<UserInfoVO> userResult = userServiceClient.getUserById(userId);
            if (userResult == null || userResult.getCode() != 200 || userResult.getData() == null) {
                throw new BusinessException("获取用户信息失败");
            }
            
            UserInfoVO userInfo = userResult.getData();
            if (!"admin".equals(userInfo.getRole())) {
                throw new BusinessException("无权限操作，只有管理员可以审核文章");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用用户服务失败: userId={}, error={}", userId, e.getMessage());
            throw new BusinessException("验证管理员权限失败");
        }
        
        // 3. 更新文章状态
        article.setStatus(status);
        if (status == 1 && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        articleMapper.updateById(article);
        log.info("更新文章状态成功: articleId={}, status={}, userId={}", articleId, status, userId);
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 保存文章标签关联
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        // 去重
        List<Long> distinctTagIds = tagIds.stream().distinct().collect(Collectors.toList());
        
        for (Long tagId : distinctTagIds) {
            // 验证标签是否存在
            Tag tag = tagMapper.selectById(tagId);
            if (tag == null) {
                log.warn("标签不存在，跳过: tagId={}", tagId);
                continue;
            }
            
            // 检查是否已关联
            LambdaQueryWrapper<ArticleTag> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ArticleTag::getArticleId, articleId)
                       .eq(ArticleTag::getTagId, tagId);
            ArticleTag existing = articleTagMapper.selectOne(queryWrapper);
            
            if (existing == null) {
                // 创建关联
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(articleId);
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
                
                // 更新标签文章数量
                tagMapper.incrementArticleCount(tagId);
            }
        }
    }
    
    /**
     * 转换为VO
     */
    private ArticleVO convertToVO(Article article, Long currentUserId) {
        ArticleVO vo = new ArticleVO();
        BeanUtil.copyProperties(article, vo);
        
        // 转换 Integer 为 Boolean
        vo.setIsTop(article.getIsTop() != null && article.getIsTop() == 1);
        vo.setIsRecommend(article.getIsRecommend() != null && article.getIsRecommend() == 1);
        
        // 设置点赞和收藏状态
        vo.setIsLiked(isLiked(article.getId(), currentUserId));
        vo.setIsCollected(isCollected(article.getId(), currentUserId));
        
        // 查询并设置用户信息
        try {
            Result<UserInfoVO> userResult = userServiceClient.getUserById(article.getUserId());
            if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                UserInfoVO userInfo = userResult.getData();
                vo.setAuthorUsername(userInfo.getUsername());
                vo.setAuthorNickname(userInfo.getNickname());
                vo.setAuthorAvatar(userInfo.getAvatarUrl());
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败: userId={}, error={}", article.getUserId(), e.getMessage());
        }
        
        // 查询并设置分类信息
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        
        // 查询并设置标签
        List<TagVO> tags = getTagsByArticleId(article.getId());
        vo.setTags(tags);
        
        return vo;
    }
    
    /**
     * 根据文章ID获取标签列表
     */
    private List<TagVO> getTagsByArticleId(Long articleId) {
        LambdaQueryWrapper<ArticleTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleTag::getArticleId, articleId);
        List<ArticleTag> articleTags = articleTagMapper.selectList(queryWrapper);
        
        if (articleTags.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> tagIds = articleTags.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toList());
        
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        
        return tags.stream()
                .map(tag -> {
                    TagVO vo = new TagVO();
                    BeanUtil.copyProperties(tag, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Markdown转HTML（简单实现，可以后续使用专门的Markdown库）
     */
    private String convertMarkdownToHtml(String markdown) {
        if (StrUtil.isBlank(markdown)) {
            return "";
        }
        
        // 简单的Markdown转换（可以后续集成commonmark-java或flexmark-java）
        String html = markdown;
        
        // 标题
        html = html.replaceAll("^### (.*)$", "<h3>$1</h3>");
        html = html.replaceAll("^## (.*)$", "<h2>$1</h2>");
        html = html.replaceAll("^# (.*)$", "<h1>$1</h1>");
        
        // 粗体
        html = html.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");
        
        // 斜体
        html = html.replaceAll("\\*(.*?)\\*", "<em>$1</em>");
        
        // 链接
        html = html.replaceAll("\\[([^\\]]+)\\]\\(([^\\)]+)\\)", "<a href=\"$2\">$1</a>");
        
        // 代码块
        html = html.replaceAll("```([^`]+)```", "<pre><code>$1</code></pre>");
        html = html.replaceAll("`([^`]+)`", "<code>$1</code>");
        
        // 换行
        html = html.replaceAll("\n\n", "</p><p>");
        html = "<p>" + html + "</p>";
        
        return html;
    }
}

