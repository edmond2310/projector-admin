package com.ruoyi.web.controller.tyy;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.tyy.domain.Navigation;
import com.ruoyi.tyy.reqvo.ColumnContentReqVo;
import com.ruoyi.tyy.reqvo.ColumnReqVo;
import com.ruoyi.tyy.reqvo.ColumnRowReqVo;
import com.ruoyi.tyy.reqvo.IdReqVo;
import com.ruoyi.tyy.service.IColumnService;
import com.ruoyi.tyy.service.INavigationService;
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
import com.ruoyi.tyy.domain.PageAudit;
import com.ruoyi.tyy.service.IPageAuditService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 页面审核Controller
 * 
 * @author liyf
 * @date 2022-10-25
 */
@RestController
@RequestMapping("/tyy/pageAudit")
public class PageAuditController extends BaseController
{
    @Autowired
    private IPageAuditService pageAuditService;

    @Autowired
    private INavigationService navigationService;

    @Autowired
    private IColumnService columnService;

    /**
     * 查询页面审核列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:pageAudit:list')")
    @GetMapping("/list")
    public TableDataInfo list(PageAudit pageAudit)
    {
        startPage();
        List<PageAudit> list = pageAuditService.selectPageAuditList(pageAudit);
        return getDataTable(list);
    }

    /**
     * 导出页面审核列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:pageAudit:export')")
    @Log(title = "页面审核", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PageAudit pageAudit)
    {
        List<PageAudit> list = pageAuditService.selectPageAuditList(pageAudit);
        ExcelUtil<PageAudit> util = new ExcelUtil<PageAudit>(PageAudit.class);
        util.exportExcel(response, list, "页面审核数据");
    }

    /**
     * 获取页面审核详细信息
     */
    @PreAuthorize("@ss.hasPermi('tyy:pageAudit:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(pageAuditService.selectPageAuditById(id));
    }

    /**
     * 新增页面审核
     */
    @PreAuthorize("@ss.hasPermi('tyy:pageAudit:add')")
    @Log(title = "页面审核", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PageAudit pageAudit)
    {
        return toAjax(pageAuditService.insertPageAudit(pageAudit));
    }

    /**
     * 修改页面审核
     */
    @PreAuthorize("@ss.hasPermi('tyy:pageAudit:edit')")
    @Log(title = "页面审核", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PageAudit pageAudit)
    {
        return toAjax(pageAuditService.updatePageAudit(pageAudit));
    }

    /**
     * 删除页面审核
     */
    @PreAuthorize("@ss.hasPermi('tyy:pageAudit:remove')")
    @Log(title = "页面审核", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(pageAuditService.deletePageAuditByIds(ids));
    }

    @PostMapping("/publish")
    public AjaxResult publish(@RequestBody PageAudit pageAudit)
    {
        if (pageAudit.getNavigationId() == null) {
            return AjaxResult.error("参数异常, 导航ID为空");
        }
        if (!(pageAudit.getPublish() == 1 || pageAudit.getPublish() == 2)) {
            return AjaxResult.error("发布参数异常");
        }

        //判断是否所有空位都有资源
        List<ColumnReqVo> columnReqVos = columnService.getColumnDataByNavId(pageAudit.getNavigationId());
        int i = 1;
        for (ColumnReqVo columnReqVo : columnReqVos) {
            for (ColumnRowReqVo columnRowReqVo : columnReqVo.getRows()) {
                for (ColumnContentReqVo columnContentReqVo : columnRowReqVo.getContentIds()) {
                    if (columnRowReqVo.getLayoutType() < 3) {
                        if (columnContentReqVo.getResourceId() == null || columnContentReqVo.getResourceId() < 1) {
                            return AjaxResult.error("第" + i + "行 存在推荐位没有内容");
                        }
                    } else if (columnRowReqVo.getLayoutType().equals(3)) {
                        if ((columnContentReqVo.getDisplayImage() == null || columnContentReqVo.getDisplayImage().equals("")) && (columnContentReqVo.getDisplayTitle() == null || columnContentReqVo.getDisplayTitle().equals(""))) {
                            return AjaxResult.error("第" + i + "行 存在按钮没有内容");
                        }
                        if (columnContentReqVo.getContentType() == null || columnContentReqVo.getContentType().equals("")) {
                            return AjaxResult.error("第" + i + "行 存在按钮没有添加资源");
                        }
                    }
                }
                i++;
            }
        }
        pageAudit.setColumnData(JSONObject.toJSONString(columnReqVos));
        Navigation navigation = navigationService.selectNavigationById(pageAudit.getNavigationId());
        pageAudit.setNavigationData(JSONObject.toJSONString(navigation));
        // 设为审核中
        pageAudit.setStatus(1);
        return toAjax(pageAuditService.insertPageAudit(pageAudit));
    }

    /**
     * 栏目发布状态 0编辑中 1审核中 2审核通过 3已驳回
     * @param idReqVo
     * @return int
     */
    @PostMapping("/getStatus")
    public AjaxResult getStatus(@RequestBody IdReqVo idReqVo) {
        System.out.println(idReqVo);
        PageAudit pageAudit = pageAuditService.selectByNavId(idReqVo.getId());
        if (pageAudit == null) {
            PageAudit pageAudit1 = new PageAudit();
            pageAudit1.setStatus(0);
            return AjaxResult.success(pageAudit1);
        }
        if (pageAudit.getStatus().equals(1)) {
            return AjaxResult.success(pageAudit);
        }
        Navigation navigation = navigationService.selectNavigationById(idReqVo.getId());
        if (navigation.getUpdateTime().getTime() > pageAudit.getCreateTime().getTime()) {
            PageAudit pageAudit1 = new PageAudit();
            pageAudit1.setStatus(0);
            return AjaxResult.success(pageAudit1);
        }
        return AjaxResult.success(pageAudit);
    }

    @PostMapping("/getLastOne")
    public AjaxResult getLastOne(@RequestBody IdReqVo idReqVo) {
        System.out.println(idReqVo);
        PageAudit pageAudit = pageAuditService.selectByNavId(idReqVo.getId());
        if (pageAudit == null) {
            return AjaxResult.success(new PageAudit());
        }
        return AjaxResult.success(pageAudit);
    }
}
