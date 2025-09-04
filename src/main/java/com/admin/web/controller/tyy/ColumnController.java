package com.ruoyi.web.controller.tyy;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.tyy.domain.*;
import com.ruoyi.tyy.reqvo.*;
import com.ruoyi.tyy.service.*;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 栏目Controller
 * 
 * @author liyf
 * @date 2022-10-25
 */
@RestController
@RequestMapping("/tyy/column")
public class ColumnController extends BaseController
{
    @Autowired
    private IColumnService columnService;

    @Autowired
    private INavigationService navigationService;

    @Autowired
    private IColumnRowService columnRowService;

    @Autowired
    private IColumnContentService columnContentService;

    @Autowired
    private IResourceService resourceService;

    /**
     * 查询栏目列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:column:list')")
    @GetMapping("/list")
    public TableDataInfo list(Column column)
    {
        startPage();
        List<Column> list = columnService.selectColumnList(column);
        return getDataTable(list);
    }

    /**
     * 导出栏目列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:column:export')")
    @Log(title = "栏目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Column column)
    {
        List<Column> list = columnService.selectColumnList(column);
        ExcelUtil<Column> util = new ExcelUtil<Column>(Column.class);
        util.exportExcel(response, list, "栏目数据");
    }

    /**
     * 获取栏目详细信息
     */
    @PreAuthorize("@ss.hasPermi('tyy:column:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(columnService.selectColumnById(id));
    }

    /**
     * 新增栏目
     */
    @PreAuthorize("@ss.hasPermi('tyy:column:add')")
    @Log(title = "栏目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Column column)
    {
        return toAjax(columnService.insertColumn(column));
    }

    /**
     * 修改栏目
     */
    @PreAuthorize("@ss.hasPermi('tyy:column:edit')")
    @Log(title = "栏目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Column column)
    {
        return toAjax(columnService.updateColumn(column));
    }

    /**
     * 删除栏目
     */
    @PreAuthorize("@ss.hasPermi('tyy:column:remove')")
    @Log(title = "栏目", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(columnService.deleteColumnByIds(ids));
    }

    @PostMapping("/saveLayout")
    public AjaxResult saveLayout(@RequestBody LayoutReqVo layoutReqVo) {
        System.out.println(JSONObject.toJSONString(layoutReqVo));
        //删除已有数据
        Column columnQuery = new Column();
        columnQuery.setNavigationId(layoutReqVo.getId());
        List<Column> columns = columnService.selectColumnList(columnQuery);
        for (Column column : columns) {
            ColumnRow columnRowQuery = new ColumnRow();
            columnRowQuery.setColumnId(column.getId());
            List<ColumnRow> columnRows = columnRowService.selectColumnRowList(columnRowQuery);
            for (ColumnRow columnRow : columnRows) {
                ColumnContent columnContentQuery = new ColumnContent();
                columnContentQuery.setColumnRowId(columnRow.getId());
                List<ColumnContent> columnContents = columnContentService.selectColumnContentList(columnContentQuery);
                for (ColumnContent columnContent : columnContents) {
                    columnContentService.deleteColumnContentById(columnContent.getId());
                }
                columnRowService.deleteColumnRowById(columnRow.getId());
            }
            columnService.deleteColumnById(column.getId());
        }

        //更新数据
        Navigation navigation = new Navigation();
        navigation.setId(layoutReqVo.getId());
        navigation.setBgImage(layoutReqVo.getBgImage());
        navigationService.updateNavigation(navigation);
        for (ColumnReqVo columnReqVo : layoutReqVo.getColumns()) {
            Column columnAdd = new Column();
            columnAdd.setNavigationId(layoutReqVo.getId());
            columnAdd.setTitle(columnReqVo.getTitle());
            if (columnService.insertColumn(columnAdd) <= 0) {
                AjaxResult.error("栏目保存失败", columnAdd);
            }
            for (ColumnRowReqVo columnRowReqVo : columnReqVo.getRows()) {
                ColumnRow columnRowAdd = new ColumnRow();
                columnRowAdd.setColumnId(columnAdd.getId());
                columnRowAdd.setLayoutType(columnRowReqVo.getLayoutType());
                columnRowAdd.setDisplayCount(columnRowReqVo.getDisplayCount());
                columnRowAdd.setTotalCount(columnRowReqVo.getTotalCount());
                columnRowAdd.setDisplayStyle(columnRowReqVo.getDisplayStyle() == null ? 1 : columnRowReqVo.getDisplayStyle());
                if (columnRowService.insertColumnRow(columnRowAdd) <= 0) {
                    AjaxResult.error("栏目行保存失败", columnRowAdd);
                }
                for (ColumnContentReqVo columnContentReqVo : columnRowReqVo.getContentIds()) {
                    ColumnContent columnContentAdd = new ColumnContent();
                    columnContentAdd.setColumnRowId(columnRowAdd.getId());
                    if (columnRowReqVo.getLayoutType() < 3) {
                        columnContentAdd.setResourceId(0L);
                        if (columnContentReqVo.getResourceId() != null) {
                            Resource resource = resourceService.selectResourceById(columnContentReqVo.getResourceId());
                            if (resource != null && resource.getId() > 0) {
                                columnContentAdd.setResourceId(resource.getId());
                                String resourceData = JSONObject.toJSONString(resource);
                                columnContentAdd.setResourceData(resourceData);
                                columnContentAdd.setContentId(resource.getContentId());
                                columnContentAdd.setContentType("OPEN_DETAILS");
                                columnContentAdd.setCdnType(resource.getContentType());
                            }
                        }

                    } else if (columnRowReqVo.getLayoutType().equals(3)) {
                        columnContentAdd.setDisplayImage(columnContentReqVo.getDisplayImage());
                        columnContentAdd.setDisplayTitle(columnContentReqVo.getDisplayTitle());
                        columnContentAdd.setContentType(columnContentReqVo.getContentType());
                        if ("OTHER".equals(columnContentReqVo.getContentType())) {
                            columnContentAdd.setDataUri(columnContentReqVo.getDataUri());
                            columnContentAdd.setContentId(null);
                            columnContentAdd.setContentFid(null);
                            columnContentAdd.setCdnType(null);
                        } else {
                            columnContentAdd.setContentId(columnContentReqVo.getContentId());
                            columnContentAdd.setContentFid(columnContentReqVo.getContentFid());
                            columnContentAdd.setContentType(columnContentReqVo.getContentType());
                            columnContentAdd.setCdnType("Page");
                            if ("panel".equals(columnContentReqVo.getContentType())) {
                                columnContentAdd.setCdnType(null);
                            }
                        }
                    }
                    if (columnContentService.insertColumnContent(columnContentAdd) <= 0) {
                        AjaxResult.error("栏目内容保存失败", columnContentAdd);
                    }
                }
            }
        }
        return AjaxResult.success();
    }

    @PostMapping("/getLayout")
    public AjaxResult getLayout(@RequestBody Column column) {
        System.out.println(column);
        List<ColumnReqVo> columnReqVos = columnService.getColumnDataByNavId(column.getNavigationId());
        return AjaxResult.success(columnReqVos);
    }
}
