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
import com.ruoyi.tyy.domain.Whitelist;
import com.ruoyi.tyy.service.IWhitelistService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 白名单Controller
 * 
 * @author liyf
 * @date 2022-10-25
 */
@RestController
@RequestMapping("/tyy/whitelist")
public class WhitelistController extends BaseController
{
    @Autowired
    private IWhitelistService whitelistService;

    /**
     * 查询白名单列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:whitelist:list')")
    @GetMapping("/list")
    public TableDataInfo list(Whitelist whitelist)
    {
        startPage();
        List<Whitelist> list = whitelistService.selectWhitelistList(whitelist);
        return getDataTable(list);
    }

    /**
     * 导出白名单列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:whitelist:export')")
    @Log(title = "白名单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Whitelist whitelist)
    {
        List<Whitelist> list = whitelistService.selectWhitelistList(whitelist);
        ExcelUtil<Whitelist> util = new ExcelUtil<Whitelist>(Whitelist.class);
        util.exportExcel(response, list, "白名单数据");
    }

    /**
     * 获取白名单详细信息
     */
    @PreAuthorize("@ss.hasPermi('tyy:whitelist:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(whitelistService.selectWhitelistById(id));
    }

    /**
     * 新增白名单
     */
    @PreAuthorize("@ss.hasPermi('tyy:whitelist:add')")
    @Log(title = "白名单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Whitelist whitelist)
    {
        return toAjax(whitelistService.insertWhitelist(whitelist));
    }

    /**
     * 修改白名单
     */
    @PreAuthorize("@ss.hasPermi('tyy:whitelist:edit')")
    @Log(title = "白名单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Whitelist whitelist)
    {
        return toAjax(whitelistService.updateWhitelist(whitelist));
    }

    /**
     * 删除白名单
     */
    @PreAuthorize("@ss.hasPermi('tyy:whitelist:remove')")
    @Log(title = "白名单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(whitelistService.deleteWhitelistByIds(ids));
    }
}
