package com.ruoyi.web.controller.tyy;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.tyy.domain.KeywordCctv;
import com.ruoyi.tyy.service.IKeywordCctvService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 关键词库Controller
 * 
 * @author ruoyi
 * @date 2023-11-21
 */
@RestController
@RequestMapping("/tyy/keywordCctv")
public class KeywordCctvController extends BaseController
{
    @Autowired
    private IKeywordCctvService keywordCctvService;

    /**
     * 查询关键词库列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:keywordCctv:list')")
    @GetMapping("/list")
    public TableDataInfo list(KeywordCctv keywordCctv)
    {
        startPage();
        List<KeywordCctv> list = keywordCctvService.selectKeywordCctvList(keywordCctv);
        return getDataTable(list);
    }

    /**
     * 导出关键词库列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:keywordCctv:export')")
    @Log(title = "关键词库", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, KeywordCctv keywordCctv)
    {
        List<KeywordCctv> list = keywordCctvService.selectKeywordCctvList(keywordCctv);
        ExcelUtil<KeywordCctv> util = new ExcelUtil<KeywordCctv>(KeywordCctv.class);
        util.exportExcel(response, list, "关键词库数据");
    }

    /**
     * 获取关键词库详细信息
     */
    @PreAuthorize("@ss.hasPermi('tyy:keywordCctv:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(keywordCctvService.selectKeywordCctvById(id));
    }

    /**
     * 新增关键词库
     */
    @PreAuthorize("@ss.hasPermi('tyy:keywordCctv:add')")
    @Log(title = "关键词库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody KeywordCctv keywordCctv)
    {
        return toAjax(keywordCctvService.insertKeywordCctv(keywordCctv));
    }

    /**
     * 修改关键词库
     */
    @PreAuthorize("@ss.hasPermi('tyy:keywordCctv:edit')")
    @Log(title = "关键词库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody KeywordCctv keywordCctv)
    {
        return toAjax(keywordCctvService.updateKeywordCctv(keywordCctv));
    }

    /**
     * 删除关键词库
     */
    @PreAuthorize("@ss.hasPermi('tyy:keywordCctv:remove')")
    @Log(title = "关键词库", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(keywordCctvService.deleteKeywordCctvByIds(ids));
    }
}
