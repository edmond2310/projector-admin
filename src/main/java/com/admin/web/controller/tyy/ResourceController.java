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
import com.ruoyi.tyy.domain.Resource;
import com.ruoyi.tyy.service.IResourceService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 媒体资源Controller
 * 
 * @author liyf
 * @date 2023-04-18
 */
@RestController
@RequestMapping("/tyy/resource")
public class ResourceController extends BaseController
{
    @Autowired
    private IResourceService resourceService;

    /**
     * 查询媒体资源列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:resource:list')")
    @GetMapping("/list")
    public TableDataInfo list(Resource resource)
    {
        startPage();
        List<Resource> list = resourceService.selectResourceList(resource);
        return getDataTable(list);
    }

    /**
     * 导出媒体资源列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:resource:export')")
    @Log(title = "媒体资源", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Resource resource)
    {
        List<Resource> list = resourceService.selectResourceList(resource);
        ExcelUtil<Resource> util = new ExcelUtil<Resource>(Resource.class);
        util.exportExcel(response, list, "媒体资源数据");
    }

    /**
     * 获取媒体资源详细信息
     */
    @PreAuthorize("@ss.hasPermi('tyy:resource:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(resourceService.selectResourceById(id));
    }

    /**
     * 新增媒体资源
     */
    @PreAuthorize("@ss.hasPermi('tyy:resource:add')")
    @Log(title = "媒体资源", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Resource resource)
    {
        return toAjax(resourceService.insertResource(resource));
    }

    /**
     * 修改媒体资源
     */
    @PreAuthorize("@ss.hasPermi('tyy:resource:edit')")
    @Log(title = "媒体资源", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Resource resource)
    {
        return toAjax(resourceService.updateResource(resource));
    }

    /**
     * 删除媒体资源
     */
    @PreAuthorize("@ss.hasPermi('tyy:resource:remove')")
    @Log(title = "媒体资源", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(resourceService.deleteResourceByIds(ids));
    }
}
