package com.contenthub.content.controller;

import com.contenthub.common.result.Result;
import com.contenthub.content.dto.CreateTagDTO;
import com.contenthub.content.service.TagService;
import com.contenthub.content.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 */
@Tag(name = "标签管理", description = "标签相关的API接口")
@RestController
@RequestMapping("/content/tag")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    
    @Operation(summary = "创建标签", description = "创建新的标签", security = {@SecurityRequirement(name = "Authorization")})
    @PostMapping("/create")
    public Result<Long> createTag(@Validated @RequestBody CreateTagDTO createTagDTO) {
        Long tagId = tagService.createTag(createTagDTO);
        return Result.success("创建成功", tagId);
    }
    
    @Operation(summary = "更新标签", description = "更新标签信息", security = {@SecurityRequirement(name = "Authorization")})
    @PutMapping("/{tagId}")
    public Result<String> updateTag(
            @Parameter(description = "标签ID", required = true) @PathVariable Long tagId,
            @Validated @RequestBody CreateTagDTO createTagDTO) {
        tagService.updateTag(tagId, createTagDTO);
        return Result.success("更新成功", null);
    }
    
    @Operation(summary = "删除标签", description = "删除标签", security = {@SecurityRequirement(name = "Authorization")})
    @DeleteMapping("/{tagId}")
    public Result<String> deleteTag(
            @Parameter(description = "标签ID", required = true) @PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return Result.success("删除成功", null);
    }
    
    @Operation(summary = "获取标签列表", description = "获取标签列表（按文章数量排序）")
    @GetMapping("/list")
    public Result<List<TagVO>> getTagList(
            @Parameter(description = "限制数量（可选）") @RequestParam(required = false) Integer limit) {
        List<TagVO> tags = tagService.getTagList(limit);
        return Result.success(tags);
    }
    
    @Operation(summary = "获取标签详情", description = "根据标签ID获取标签详细信息")
    @GetMapping("/{tagId}")
    public Result<TagVO> getTagDetail(
            @Parameter(description = "标签ID", required = true) @PathVariable Long tagId) {
        TagVO tag = tagService.getTagDetail(tagId);
        return Result.success(tag);
    }
    
    @Operation(summary = "根据文章ID获取标签列表", description = "获取指定文章的所有标签")
    @GetMapping("/article/{articleId}")
    public Result<List<TagVO>> getTagsByArticleId(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        List<TagVO> tags = tagService.getTagsByArticleId(articleId);
        return Result.success(tags);
    }
}

