package com.ruoyi.web.controller.tyy;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
import com.ruoyi.tyy.domain.Navigation;
import com.ruoyi.tyy.service.INavigationService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 导航Controller
 * 
 * @author liyf
 * @date 2022-10-24
 */
@RestController
@RequestMapping("/tyy/navigation")
public class NavigationController extends BaseController
{
    @Autowired
    private INavigationService navigationService;

    /**
     * 查询导航列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:navigation:list')")
    @GetMapping("/list")
    public TableDataInfo list(Navigation navigation)
    {
        startPage();
        List<Navigation> list = navigationService.selectNavigationList(navigation);
        return getDataTable(list);
    }

    /**
     * 导出导航列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:navigation:export')")
    @Log(title = "导航", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Navigation navigation)
    {
        List<Navigation> list = navigationService.selectNavigationList(navigation);
        ExcelUtil<Navigation> util = new ExcelUtil<Navigation>(Navigation.class);
        util.exportExcel(response, list, "导航数据");
    }

    /**
     * 获取导航详细信息
     */
    @PreAuthorize("@ss.hasPermi('tyy:navigation:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(navigationService.selectNavigationById(id));
    }

    /**
     * 新增导航
     */
    @PreAuthorize("@ss.hasPermi('tyy:navigation:add')")
    @Log(title = "导航", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Navigation navigation) throws Exception {
        if (navigation == null || navigation.getType() == null) {
            throw new Exception("参数错误");
        }
        if (navigation.getType().equals(1)) {
            if (StringUtils.isEmpty(navigation.getName())) {
                throw new Exception("栏目名称不能空");
            }
        }
        if (navigation.getType().equals(2)) {
            if (StringUtils.isEmpty(navigation.getTitleImage())) {
                throw new Exception("栏目标题图不能空");
            }
            if (StringUtils.isEmpty(navigation.getTitleImage2())) {
                throw new Exception("栏目标题图不能空");
            }
        }
        int i = navigationService.insertNavigation(navigation);
        navigation.setSortNum(navigation.getId().intValue());
        navigationService.updateNavigation(navigation);
        return toAjax(i);
    }

    /**
     * 修改导航
     */
    @PreAuthorize("@ss.hasPermi('tyy:navigation:edit')")
    @Log(title = "导航", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Navigation navigation) throws Exception {
        if (navigation == null || navigation.getType() == null) {
            throw new Exception("参数错误");
        }
        if (navigation.getType().equals(1)) {
            if (StringUtils.isEmpty(navigation.getName())) {
                throw new Exception("栏目名称不能空");
            }
        }
        if (navigation.getType().equals(2)) {
            if (StringUtils.isEmpty(navigation.getTitleImage())) {
                throw new Exception("栏目标题图不能空");
            }
            if (StringUtils.isEmpty(navigation.getTitleImage2())) {
                throw new Exception("栏目标题图不能空");
            }
        }
        return toAjax(navigationService.updateNavigation(navigation));
    }

    /**
     * 删除导航
     */
    @PreAuthorize("@ss.hasPermi('tyy:navigation:remove')")
    @Log(title = "导航", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(navigationService.deleteNavigationByIds(ids));
    }

    @PostMapping(value = "/sortUp")
    public AjaxResult sortUp(@RequestBody Navigation navigation)
    {
        Navigation navigation2 = navigationService.selectNavigationById(navigation.getId());
        Integer sortNum = navigation2.getSortNum();
        Navigation navigation1 = navigationService.selectPrevBySortNum(sortNum);
        if (navigation1 == null) {
            return AjaxResult.success("已经是第一个了");
        }
        //交换sortnum
        navigation2.setSortNum(navigation1.getSortNum());
        navigationService.updateNavigation(navigation2);
        navigation1.setSortNum(sortNum);
        navigationService.updateNavigation(navigation1);
        return AjaxResult.success();
    }

    @PostMapping(value = "/sortDown")
    public AjaxResult sortDown(@RequestBody Navigation navigation)
    {
        Navigation navigation1 = navigationService.selectNavigationById(navigation.getId());
        Integer sortNum = navigation1.getSortNum();
        Navigation navigation2 = navigationService.selectPostBySortNum(sortNum);
        if (navigation2 == null) {
            return AjaxResult.success("已经是最后一个了");
        }
        //交换sortnum
        navigation1.setSortNum(navigation2.getSortNum());
        navigationService.updateNavigation(navigation1);
        navigation2.setSortNum(sortNum);
        navigationService.updateNavigation(navigation2);
        return AjaxResult.success();
    }
}
