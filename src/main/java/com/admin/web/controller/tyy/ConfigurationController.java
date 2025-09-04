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
import com.ruoyi.tyy.domain.Configuration;
import com.ruoyi.tyy.service.IConfigurationService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 应用配置Controller
 * 
 * @author ruoyi
 * @date 2023-11-17
 */
@RestController
@RequestMapping("/tyy/configuration")
public class ConfigurationController extends BaseController
{
    @Autowired
    private IConfigurationService configurationService;

    /**
     * 查询应用配置列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:configuration:list')")
    @GetMapping("/list")
    public TableDataInfo list(Configuration configuration)
    {
        startPage();
        List<Configuration> list = configurationService.selectConfigurationList(configuration);
        return getDataTable(list);
    }

    /**
     * 导出应用配置列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:configuration:export')")
    @Log(title = "应用配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Configuration configuration)
    {
        List<Configuration> list = configurationService.selectConfigurationList(configuration);
        ExcelUtil<Configuration> util = new ExcelUtil<Configuration>(Configuration.class);
        util.exportExcel(response, list, "应用配置数据");
    }

    /**
     * 获取应用配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('tyy:configuration:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(configurationService.selectConfigurationById(id));
    }

    /**
     * 新增应用配置
     */
    @PreAuthorize("@ss.hasPermi('tyy:configuration:add')")
    @Log(title = "应用配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Configuration configuration)
    {
        return toAjax(configurationService.insertConfiguration(configuration));
    }

    /**
     * 修改应用配置
     */
    @PreAuthorize("@ss.hasPermi('tyy:configuration:edit')")
    @Log(title = "应用配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Configuration configuration)
    {
        return toAjax(configurationService.updateConfiguration(configuration));
    }

    /**
     * 删除应用配置
     */
    @PreAuthorize("@ss.hasPermi('tyy:configuration:remove')")
    @Log(title = "应用配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(configurationService.deleteConfigurationByIds(ids));
    }
}
