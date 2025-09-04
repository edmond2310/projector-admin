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
import com.ruoyi.tyy.domain.ColumnRow;
import com.ruoyi.tyy.service.IColumnRowService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 栏目行Controller
 * 
 * @author liyf
 * @date 2022-10-25
 */
@RestController
@RequestMapping("/tyy/row")
public class ColumnRowController extends BaseController
{
    @Autowired
    private IColumnRowService columnRowService;

    /**
     * 查询栏目行列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:row:list')")
    @GetMapping("/list")
    public TableDataInfo list(ColumnRow columnRow)
    {
        startPage();
        List<ColumnRow> list = columnRowService.selectColumnRowList(columnRow);
        return getDataTable(list);
    }

    /**
     * 导出栏目行列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:row:export')")
    @Log(title = "栏目行", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ColumnRow columnRow)
    {
        List<ColumnRow> list = columnRowService.selectColumnRowList(columnRow);
        ExcelUtil<ColumnRow> util = new ExcelUtil<ColumnRow>(ColumnRow.class);
        util.exportExcel(response, list, "栏目行数据");
    }

    /**
     * 获取栏目行详细信息
     */
    @PreAuthorize("@ss.hasPermi('tyy:row:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(columnRowService.selectColumnRowById(id));
    }

    /**
     * 新增栏目行
     */
    @PreAuthorize("@ss.hasPermi('tyy:row:add')")
    @Log(title = "栏目行", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ColumnRow columnRow)
    {
        return toAjax(columnRowService.insertColumnRow(columnRow));
    }

    /**
     * 修改栏目行
     */
    @PreAuthorize("@ss.hasPermi('tyy:row:edit')")
    @Log(title = "栏目行", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ColumnRow columnRow)
    {
        return toAjax(columnRowService.updateColumnRow(columnRow));
    }

    /**
     * 删除栏目行
     */
    @PreAuthorize("@ss.hasPermi('tyy:row:remove')")
    @Log(title = "栏目行", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(columnRowService.deleteColumnRowByIds(ids));
    }
}
