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
import com.ruoyi.tyy.domain.ColumnContent;
import com.ruoyi.tyy.service.IColumnContentService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 栏目单元内容Controller
 * 
 * @author liyf
 * @date 2022-11-02
 */
@RestController
@RequestMapping("/tyy/content")
public class ColumnContentController extends BaseController
{
    @Autowired
    private IColumnContentService columnContentService;

    /**
     * 查询栏目单元内容列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:content:list')")
    @GetMapping("/list")
    public TableDataInfo list(ColumnContent columnContent)
    {
        startPage();
        List<ColumnContent> list = columnContentService.selectColumnContentList(columnContent);
        return getDataTable(list);
    }

    /**
     * 导出栏目单元内容列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:content:export')")
    @Log(title = "栏目单元内容", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ColumnContent columnContent)
    {
        List<ColumnContent> list = columnContentService.selectColumnContentList(columnContent);
        ExcelUtil<ColumnContent> util = new ExcelUtil<ColumnContent>(ColumnContent.class);
        util.exportExcel(response, list, "栏目单元内容数据");
    }

    /**
     * 获取栏目单元内容详细信息
     */
    @PreAuthorize("@ss.hasPermi('tyy:content:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(columnContentService.selectColumnContentById(id));
    }

    /**
     * 新增栏目单元内容
     */
    @PreAuthorize("@ss.hasPermi('tyy:content:add')")
    @Log(title = "栏目单元内容", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ColumnContent columnContent)
    {
        return toAjax(columnContentService.insertColumnContent(columnContent));
    }

    /**
     * 修改栏目单元内容
     */
    @PreAuthorize("@ss.hasPermi('tyy:content:edit')")
    @Log(title = "栏目单元内容", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ColumnContent columnContent)
    {
        return toAjax(columnContentService.updateColumnContent(columnContent));
    }

    /**
     * 删除栏目单元内容
     */
    @PreAuthorize("@ss.hasPermi('tyy:content:remove')")
    @Log(title = "栏目单元内容", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(columnContentService.deleteColumnContentByIds(ids));
    }
}
