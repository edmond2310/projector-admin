package com.ruoyi.web.controller.tyy;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.tyy.domain.ASAItem;
import com.ruoyi.tyy.domain.ASAParam;
import com.ruoyi.tyy.domain.ModelStatistics;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.tyy.domain.Device;
import com.ruoyi.tyy.service.IDeviceService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 设备Controller
 * 
 * @author liyf
 * @date 2022-10-25
 */
@RestController
@RequestMapping("/tyy/device")
public class DeviceController extends BaseController
{
    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询设备列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:device:list')")
    @GetMapping("/list")
    public TableDataInfo list(Device device)
    {
        startPage();
        List<Device> list = deviceService.selectDeviceList(device);
        return getDataTable(list);
    }

    @PostMapping("/getModels")
    public AjaxResult getModels() {
        List<ModelStatistics> modelStatistics = deviceService.selectModelStatistics();
        System.out.println(modelStatistics);
        return AjaxResult.success(modelStatistics);
    }

    /**
     * 导出设备列表
     */
    @PreAuthorize("@ss.hasPermi('tyy:device:export')")
    @Log(title = "设备", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Device device)
    {
        List<Device> list = deviceService.selectDeviceList(device);
        ExcelUtil<Device> util = new ExcelUtil<Device>(Device.class);
        util.exportExcel(response, list, "设备数据");
    }

    /**
     * 获取设备详细信息
     */
    @PreAuthorize("@ss.hasPermi('tyy:device:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(deviceService.selectDeviceById(id));
    }

    /**
     * 新增设备
     */
    @PreAuthorize("@ss.hasPermi('tyy:device:add')")
    @Log(title = "设备", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Device device)
    {
        return toAjax(deviceService.insertDevice(device));
    }

    /**
     * 修改设备
     */
    @PreAuthorize("@ss.hasPermi('tyy:device:edit')")
    @Log(title = "设备", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Device device)
    {
        return toAjax(deviceService.updateDevice(device));
    }

    /**
     * 删除设备
     */
    @PreAuthorize("@ss.hasPermi('tyy:device:remove')")
    @Log(title = "设备", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(deviceService.deleteDeviceByIds(ids));
    }

    @GetMapping(value = "/getLocation")
    public AjaxResult getLocation(@RequestParam("ip") String ip) {
        if ("".equals(ip)) {
            return AjaxResult.error("惨数ip不能为空");
        }
        String locationRedisKey = "location_" + ip;
        String s1 = stringRedisTemplate.opsForValue().get(locationRedisKey);
        System.out.println(s1);
        if (s1 != null && !"".equals(s1)) {
            return AjaxResult.success(JSONObject.parse(s1));
        }
        String s = HttpUtils.sendGet("https://apis.map.qq.com/ws/location/v1/ip?ip="+ip+"&key=LQOBZ-QC4WH-4PHDX-WVQG7-BY6R7-FGFFG");
        System.out.println(s);
        stringRedisTemplate.opsForValue().set(locationRedisKey, s, 3, TimeUnit.DAYS);
        stringRedisTemplate.opsForValue().set(locationRedisKey, s);
        return AjaxResult.success(JSONObject.parse(s));
    }

    @PostMapping("/getLocations")
    public List getLocations(Device device) {
        List<Map<String, Object>> locations = new ArrayList<>();
        List<Device> list = deviceService.selectDeviceList(device);

        for (Device device1 : list) {
            if (device1.getIpLocationData() == null || device1.getIpLocationData().equals("")) {
                continue;
            }
            //json
            JSONObject jsonObject = JSONObject.parse(device1.getIpLocationData());
            System.out.println(device1.getIpLocationData());
            if (!jsonObject.get("status").equals(0)) {
                continue;
            }
            JSONObject result = jsonObject.getJSONObject("result");
            JSONObject locationObj = result.getJSONObject("location");
            //res
            // {"status":0,"message":"Success","request_id":"7dc7af40c7d943babe57762bb0126182","result":{"ip":"61.141.64.139","location":{"lat":22.53332,"lng":113.93041},"ad_info":{"nation":"中国","province":"广东省","city":"深圳市","district":"南山区","adcode":440305,"nation_code":156}}}
            Map<String, Object> location = new HashMap<>();
            ArrayList<Object> lnglat = new ArrayList<>();
            Random r = new Random();
            float v = (r.nextFloat() - 0.5f) / 100;
            System.out.println(locationObj.get("lng"));
            BigDecimal lng = new BigDecimal(locationObj.get("lng").toString());
            BigDecimal lng2 = lng.add(new BigDecimal(v)).setScale(6,BigDecimal.ROUND_DOWN);
            float v2 = (r.nextFloat() - 0.5f) / 100;
            BigDecimal lat = new BigDecimal(locationObj.get("lat").toString());
            BigDecimal lat2 = lat.add(new BigDecimal(v2)).setScale(6,BigDecimal.ROUND_DOWN);
//            System.out.println(v);
//            System.out.println(locationObj.get("lng"));
//            System.out.println(lng2);
            lnglat.add(lng2);
            lnglat.add(lat2);
            location.put("lnglat", lnglat);
            location.put("name", result.get("ip"));
            location.put("style", 0);
            locations.add(location);
        }
        return locations;
    }

    @PostMapping("/getActiveASAList")
    public AjaxResult getActiveASAList(@RequestBody ASAParam asaParam) {
        if (asaParam == null) {
            asaParam = new ASAParam();
        }
        System.out.println(asaParam);
        if (asaParam.getStartDate() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -30);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            asaParam.setStartDate(calendar.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(asaParam.getStartDate());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            asaParam.setStartDate(calendar.getTime());
        }
        if (asaParam.getEndDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(asaParam.getEndDate());
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MILLISECOND, 999);
            asaParam.setEndDate(calendar.getTime());
        }
        List<ASAItem> asaItems = deviceService.selectActiveASAList(asaParam);
//        System.out.println(asaItems);
        return AjaxResult.success(asaItems);
    }

    @PostMapping("/getNewASAList")
    public AjaxResult getNewASAList(@RequestBody ASAParam asaParam) {
        if (asaParam == null) {
            asaParam = new ASAParam();
        }
        System.out.println(asaParam);
        if (asaParam.getStartDate() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -30);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            asaParam.setStartDate(calendar.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(asaParam.getStartDate());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            asaParam.setStartDate(calendar.getTime());
        }
        if (asaParam.getEndDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(asaParam.getEndDate());
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MILLISECOND, 999);
            asaParam.setEndDate(calendar.getTime());
        }
        List<ASAItem> asaItems = deviceService.selectNewASAList(asaParam);
//        System.out.println(asaItems);
        return AjaxResult.success(asaItems);
    }

    @PostMapping("/getDayActiveTrendList")
    public AjaxResult getDayActiveTrendList(@RequestBody ASAParam asaParam) {
        if (asaParam == null) {
            asaParam = new ASAParam();
        }
        System.out.println(asaParam);
        if (asaParam.getStartDate() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -7);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            asaParam.setStartDate(calendar.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(asaParam.getStartDate());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            asaParam.setStartDate(calendar.getTime());
        }
        if (asaParam.getEndDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(asaParam.getEndDate());
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MILLISECOND, 999);
            asaParam.setEndDate(calendar.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MILLISECOND, 999);
            asaParam.setEndDate(calendar.getTime());
        }

        // 相隔天数
        long difference = (asaParam.getEndDate().getTime() - asaParam.getStartDate().getTime()) / 86400000;
        if (difference < 0l) {
            return AjaxResult.error("开始时间比结束时间大");
        }
        if (difference > 59l) {
            return AjaxResult.error("时间跨度不能大于60天");
        }

        List<ASAItem> trendItems = deviceService.selectDayActiveTrendList(asaParam);

        // to map
        HashMap<String, String> trendItemsMap = new HashMap<>();
        for (ASAItem trendItem : trendItems) {
            trendItemsMap.put(trendItem.getName(), trendItem.getValue());
        }

        // 重新组织数据
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        Calendar cal = Calendar.getInstance();
        Date currentDate = asaParam.getStartDate();
        while (currentDate.before(asaParam.getEndDate())) {
            String currentDateFormat = dateFormat.format(currentDate);
            names.add(currentDateFormat);
            if (trendItemsMap.containsKey(currentDateFormat)) {
                values.add(trendItemsMap.get(currentDateFormat));
            } else {
                values.add("0");
            }

            cal.setTime(currentDate);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            currentDate = cal.getTime();
        }

        HashMap<String, List> result = new HashMap<>();
        result.put("names", names);
        result.put("values", values);
        result.put("items", trendItems);
        return AjaxResult.success(result);
    }

    @PostMapping("/getDayNewTrendList")
    public AjaxResult getDayNewTrendList(@RequestBody ASAParam asaParam) {
        if (asaParam == null) {
            asaParam = new ASAParam();
        }
        System.out.println(asaParam);
        if (asaParam.getStartDate() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -7);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            asaParam.setStartDate(calendar.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(asaParam.getStartDate());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            asaParam.setStartDate(calendar.getTime());
        }
        if (asaParam.getEndDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(asaParam.getEndDate());
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MILLISECOND, 999);
            asaParam.setEndDate(calendar.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MILLISECOND, 999);
            asaParam.setEndDate(calendar.getTime());
        }

        // 相隔天数
        long difference = (asaParam.getEndDate().getTime() - asaParam.getStartDate().getTime()) / 86400000;
        if (difference < 0l) {
            return AjaxResult.error("开始时间比结束时间大");
        }
        if (difference > 59l) {
            return AjaxResult.error("时间跨度不能大于60天");
        }

        List<ASAItem> trendItems = deviceService.selectDayNewTrendList(asaParam);

        // to map
        HashMap<String, String> trendItemsMap = new HashMap<>();
        for (ASAItem trendItem : trendItems) {
            trendItemsMap.put(trendItem.getName(), trendItem.getValue());
        }

        // 重新组织数据
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        Calendar cal = Calendar.getInstance();
        Date currentDate = asaParam.getStartDate();
        while (currentDate.before(asaParam.getEndDate())) {
            String currentDateFormat = dateFormat.format(currentDate);
            names.add(currentDateFormat);
            if (trendItemsMap.containsKey(currentDateFormat)) {
                values.add(trendItemsMap.get(currentDateFormat));
            } else {
                values.add("0");
            }

            cal.setTime(currentDate);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            currentDate = cal.getTime();
        }

        HashMap<String, List> result = new HashMap<>();
        result.put("names", names);
        result.put("values", values);
        result.put("items", trendItems);
        return AjaxResult.success(result);
    }

    @PostMapping("/getMonthActiveTrendList")
    public AjaxResult getMonthActiveTrendList(@RequestBody ASAParam asaParam) {
        if (asaParam == null) {
            asaParam = new ASAParam();
        }
        System.out.println(asaParam);
        if (asaParam.getStartDate() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -6);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MILLISECOND, 0);
            asaParam.setStartDate(calendar.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(asaParam.getStartDate());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MILLISECOND, 0);
            asaParam.setStartDate(calendar.getTime());
        }
        if (asaParam.getEndDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(asaParam.getEndDate());
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MILLISECOND, 999);
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            asaParam.setEndDate(calendar.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MILLISECOND, 999);
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            asaParam.setEndDate(calendar.getTime());
        }

        // 相隔天数
        long difference = (asaParam.getEndDate().getTime() - asaParam.getStartDate().getTime()) / 86400000;
//        System.out.println(asaParam);
//        System.out.println(difference);
        if (difference < 0l) {
            return AjaxResult.error("开始时间比结束时间大");
        }

        List<ASAItem> trendItems = deviceService.selectMonthActiveTrendList(asaParam);

        // to map
        HashMap<String, String> trendItemsMap = new HashMap<>();
        for (ASAItem trendItem : trendItems) {
            trendItemsMap.put(trendItem.getName(), trendItem.getValue());
        }

        // 重新组织数据
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM");
        Calendar cal = Calendar.getInstance();
        Date currentDate = asaParam.getStartDate();
        while (currentDate.before(asaParam.getEndDate())) {
            String currentDateFormat = dateFormat.format(currentDate);
            names.add(currentDateFormat);
            if (trendItemsMap.containsKey(currentDateFormat)) {
                values.add(trendItemsMap.get(currentDateFormat));
            } else {
                values.add("0");
            }

            cal.setTime(currentDate);
            cal.add(Calendar.MONTH, 1);
            currentDate = cal.getTime();
        }

        HashMap<String, List> result = new HashMap<>();
        result.put("names", names);
        result.put("values", values);
        result.put("items", trendItems);
        return AjaxResult.success(result);
    }

    @PostMapping("/getMonthNewTrendList")
    public AjaxResult getMonthNewTrendList(@RequestBody ASAParam asaParam) {
        if (asaParam == null) {
            asaParam = new ASAParam();
        }
        System.out.println(asaParam);
        if (asaParam.getStartDate() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -6);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MILLISECOND, 0);
            asaParam.setStartDate(calendar.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(asaParam.getStartDate());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MILLISECOND, 0);
            asaParam.setStartDate(calendar.getTime());
        }
        if (asaParam.getEndDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(asaParam.getEndDate());
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MILLISECOND, 999);
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            asaParam.setEndDate(calendar.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MILLISECOND, 999);
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            asaParam.setEndDate(calendar.getTime());
        }

        // 相隔天数
        long difference = (asaParam.getEndDate().getTime() - asaParam.getStartDate().getTime()) / 86400000;
//        System.out.println(asaParam);
//        System.out.println(difference);
        if (difference < 0l) {
            return AjaxResult.error("开始时间比结束时间大");
        }

        List<ASAItem> trendItems = deviceService.selectMonthNewTrendList(asaParam);

        // to map
        HashMap<String, String> trendItemsMap = new HashMap<>();
        for (ASAItem trendItem : trendItems) {
            trendItemsMap.put(trendItem.getName(), trendItem.getValue());
        }

        // 重新组织数据
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM");
        Calendar cal = Calendar.getInstance();
        Date currentDate = asaParam.getStartDate();
        while (currentDate.before(asaParam.getEndDate())) {
            String currentDateFormat = dateFormat.format(currentDate);
            names.add(currentDateFormat);
            if (trendItemsMap.containsKey(currentDateFormat)) {
                values.add(trendItemsMap.get(currentDateFormat));
            } else {
                values.add("0");
            }

            cal.setTime(currentDate);
            cal.add(Calendar.MONTH, 1);
            currentDate = cal.getTime();
        }

        HashMap<String, List> result = new HashMap<>();
        result.put("names", names);
        result.put("values", values);
        result.put("items", trendItems);
        return AjaxResult.success(result);
    }
}
