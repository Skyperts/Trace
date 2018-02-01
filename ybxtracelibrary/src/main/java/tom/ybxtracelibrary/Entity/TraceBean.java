package tom.ybxtracelibrary.Entity;

import java.util.HashMap;

/**
 * Created by a55 on 2017/7/17.
 */

public class TraceBean extends TraceCommonBean {
    // 基础参数
    public String en;           // 事件名
    public String pl;           // 平台
    public String ip;           // 客户端ip地址
    public String sid;          //
    public String lot;          // 经度 longitude
    public String lat;          // 纬度 latitude
    public String sdk;          // 数据收集SDK来源 js
    public String gid;          // 设备id
    public String l;            // 手机系统语言
    public String ct;           // 访问(触发)时间戳(毫秒级)
    public String rst;          // app指手机分辨率

    public String purl;         // 当前页面标识
    public String purlh;        // 当前页面标识Hash
    public String pref;         // 前一页面标识
    public String prefh;        // 前一页面标识Hash
    public String pa;           // 用户点击路径path
    public String chid;         // 渠道id  待定义
    public String tt;           // 页面标题
    public String x;            // 页面点击的x坐标
    public String y;            // 页面点击的y坐标

    public String fid;          // 外部渠道字段 默认空字符串，"MW"魔窗 "Push"推送

    public String ca;           // Event事件类目category，目前有点击类目（c click）和订单类目(o order)
    public String ac;           // Event事件动作，每一个动作有对应的类目

    public HashMap<String, String> kv;         // 预留参数

}
