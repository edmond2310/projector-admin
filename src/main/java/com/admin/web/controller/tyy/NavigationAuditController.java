package com.ruoyi.web.controller.tyy;

import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.tyy.domain.Navigation;
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
import com.ruoyi.tyy.domain.NavigationAudit;
import com.ruoyi.tyy.service.INavigationAuditService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 导航菜单审核Controller
 * 
 * @author ruoyi
 * @date 2023-02-15
 */
@RestController
@RequestMapping("/tyy/navigationAudit")
public class NavigationAuditController extends BaseController
{
    @Autowired
    private INavigationService navigationService;

    @Autowired
    private INavigationAuditService navigationAuditService;

    /**
     * 查询导航菜单审核列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:navigationAudit:list')")
    @GetMapping("/list")
    public TableDataInfo list(NavigationAudit navigationAudit)
    {
        startPage();
        List<NavigationAudit> list = navigationAuditService.selectNavigationAuditList(navigationAudit);
        return getDataTable(list);
    }

    /**
     * 导出导航菜单审核列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:navigationAudit:export')")
    @Log(title = "导航菜单审核", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NavigationAudit navigationAudit)
    {
        List<NavigationAudit> list = navigationAuditService.selectNavigationAuditList(navigationAudit);
        ExcelUtil<NavigationAudit> util = new ExcelUtil<NavigationAudit>(NavigationAudit.class);
        util.exportExcel(response, list, "导航菜单审核数据");
    }

    /**
     * 获取导航菜单审核详细信息
     */
    @PreAuthorize("@ss.hasPermi('tyy:navigationAudit:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(navigationAuditService.selectNavigationAuditById(id));
    }

    /**
     * 新增导航菜单审核
     */
    @PreAuthorize("@ss.hasPermi('tyy:navigationAudit:add')")
    @Log(title = "导航菜单审核", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody NavigationAudit navigationAudit)
    {
        return toAjax(navigationAuditService.insertNavigationAudit(navigationAudit));
    }

    /**
     * 修改导航菜单审核
     */
    @PreAuthorize("@ss.hasPermi('tyy:navigationAudit:edit')")
    @Log(title = "导航菜单审核", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody NavigationAudit navigationAudit)
    {
        return toAjax(navigationAuditService.updateNavigationAudit(navigationAudit));
    }

    /**
     * 删除导航菜单审核
     */
    @PreAuthorize("@ss.hasPermi('tyy:navigationAudit:remove')")
    @Log(title = "导航菜单审核", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(navigationAuditService.deleteNavigationAuditByIds(ids));
    }

    @PostMapping("/publish")
    public AjaxResult publish(@RequestBody NavigationAudit navigationAudit) throws Exception {
        NavigationAudit navigationAuditLast = navigationAuditService.selectLastNavigationAudit();
        Date lastUpdateTime = null;
        List<Navigation> navigations = navigationService.selectNavigationList(null);
        for (Navigation navigation : navigations) {
            if (lastUpdateTime == null) {
                lastUpdateTime = navigation.getUpdateTime();
                continue;
            }
            if (lastUpdateTime.getTime() < navigation.getUpdateTime().getTime()) {
                lastUpdateTime = navigation.getUpdateTime();
            }
        }
        if (navigationAuditLast != null && navigationAuditLast.getCreateTime().getTime() > lastUpdateTime.getTime()) {
            throw new Exception("内容已提交审核，请更新后再重新提交");
        }
        String navigationData = JSONObject.toJSONString(navigations);
        navigationAudit.setNavigationData(navigationData);
        navigationAudit.setStatus(1);
        navigationAudit.setPublish(1);
        return toAjax(navigationAuditService.insertNavigationAudit(navigationAudit));
    }
}
