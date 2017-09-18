package tom.ybxtracelibrary;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tom.ybxfloatviewlibrary.MyWindowManager;
import tom.ybxtracelibrary.Entity.TraceBean;
import tom.ybxtracelibrary.Entity.TraceCommonBean;
import tom.ybxtracelibrary.Entity.TraceMapBean;
import tom.ybxtracelibrary.Net.ApiRequest;
import tom.ybxtracelibrary.Utils.DeviceUtils;
import tom.ybxtracelibrary.Utils.SPUtils;
import tom.ybxtracelibrary.annotation.EventType;

/**
 * Created by a55 on 2017/7/17.
 */

public class YbxTrace {

    // 存储转化率事件的list
    private static ArrayList<TraceBean> traces     = new ArrayList<>();    // 批量上传时存储事件
    private static ArrayList<TraceBean> errorCache = new ArrayList<>();    // 存储上传失败的事件

    // 存储转化率事件上传策略
    private static int uploadStrategy = 1;     //    0是批量上传，1是即时上传

    private static boolean uploadSwitch = true;     //    上传开关， 默认打开

    // 存储转化率事件的基础参数
    private static TraceCommonBean mTraceCommonBean = new TraceCommonBean();
    private static TraceMapBean    traceMapBean     = new TraceMapBean();

    private static volatile String mChid;     // 渠道id
    private static volatile String page;      // event事件的当前页，pageview事件的前一页
    private static volatile String mPurl;     // event事件的当前页，pageview事件的前一页

    private static volatile String pageh;     // event事件的当前页hash，pageview事件的前一页hash
    private static volatile String mPurlh;    // event事件的当前页hash，pageview事件的前一页hash

    private static volatile YbxTrace instance;
    private static          Context  mContext;

    public static void initTrace(Context context, TraceCommonBean traceCommonBean, int strategy) {
        mContext = context;

        //app基础信息
        mTraceCommonBean = traceCommonBean;

        // 上传策略
        uploadStrategy = strategy;

    }

    public static YbxTrace getInstance() {
        if (instance == null) {
            synchronized (YbxTrace.class) {
                if (instance == null) {
                    instance = new YbxTrace();
                }
            }
        }
        return instance;
    }

    public void setTraceCommonBean(TraceCommonBean traceCommonBean) {
        mTraceCommonBean = traceCommonBean;
    }

    public TraceCommonBean getTraceCommonBean() {
        return mTraceCommonBean;
    }

    public static void setUploadSwitch(boolean uploadSwitch) {
        YbxTrace.uploadSwitch = uploadSwitch;
    }

    public void clearChid() {
        mChid = "";
    }

    public void switchBottomTab(Fragment fragment, String pageview) {
        page = pageview;

        String purlh      = "";
        String purlString = fragment.toString();
        if (!TextUtils.isEmpty(purlString)) {
            purlh = purlString.substring(purlString.indexOf("@") + 1);
        }
        pageh = purlh;
    }

    public void showLogWindow(Context context, int width, int heigh) {
        MyWindowManager.createFloatView(context, width, heigh);
    }

    public void dismissLogWindow(Context context, int width, int heigh) {
        MyWindowManager.removeWindow(context);
    }

    /**
     * 第一次进入app，每次推出再进入app
     */
    public void launch(Activity activity) {
        if (uploadSwitch) {
            TraceBean traceBean = new TraceBean();
            traceBean.en = EventType.Event_Launch;

            buildBaseParam(activity, traceBean);

            upload(traceBean);
        }
    }

    /**
     * 注册事件
     */
    public void register(Activity activity) {
        if (uploadSwitch) {
            TraceBean traceBean = new TraceBean();
            traceBean.en = EventType.Event_Register;
            buildBaseParam(activity, traceBean);

            upload(traceBean);
        }
    }

    /**
     * 页面事件
     */
    public void pageView(Activity activity, String pref, String prefh, String purl, String purlh, String tt) {
        if (uploadSwitch) {
            TraceBean traceBean = new TraceBean();
            traceBean.en = EventType.Event_Pageview;
            buildBaseParam(activity, traceBean);

            traceBean.purl = transferString(purl);
            traceBean.purlh = purlh;

            traceBean.pref = transferString(pref);
            traceBean.prefh = prefh;

            traceBean.chid = mChid;
            traceBean.tt = tt;

            upload(traceBean);
        }
    }

    private String transferString(String s) {
        if (!TextUtils.isEmpty(s) && s.contains("&")){
            s = s.replace("&", "<A>");
        }
        return s;
    }

    /**
     * 目前有点击事件和订单事件  渠道开端传入渠道号
     *
     * @param activity
     * @param chid     渠道开端点击事件时必须传入，新渠道开端点击事件时重制
     */
    public void event(Activity activity, String pref, String prefh, String purl, String purlh, String tt, String pa, String category, String action, HashMap<String, String> kv, String chid) {
        if (uploadSwitch) {
            TraceBean traceBean = new TraceBean();
            traceBean.en = EventType.Event_Event;
            buildBaseParam(activity, traceBean);

            if (!TextUtils.isEmpty(chid)) {
                mChid = chid;
            }

            traceBean.purl = transferString(purl);
            traceBean.purlh = purlh;
            traceBean.pref = transferString(pref);
            traceBean.prefh = prefh;

            traceBean.tt = tt;
            traceBean.pa = pa;

            traceBean.ca = category;
            traceBean.ac = action;

            traceBean.chid = mChid;
            // kv
            if (kv != null) {
                traceBean.kv = kv;
            }

            upload(traceBean);
        }
    }

    private void buildBaseParam(Activity activity, TraceBean traceBean) {
        traceBean.v = mTraceCommonBean.v;
        traceBean.bid = mTraceCommonBean.bid;
        traceBean.mid = mTraceCommonBean.mid;
        traceBean.iev = mTraceCommonBean.iev;

        traceBean.ip = DeviceUtils.getIp();
        traceBean.pl = "Android";
        traceBean.sdk = "java";
        traceBean.gid = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        traceBean.ct = System.currentTimeMillis() + "";
        traceBean.l = DeviceUtils.getDeviceLanguage(activity);
        traceBean.rst = DeviceUtils.getDensityWidth(activity) + "*" + DeviceUtils.getDensityHeight(activity);

    }

    private void upload(TraceBean traceBean) {
        if (uploadStrategy == 1) {    //  0批量1即时

            String json = new Gson().toJson(traceBean);
            Logger.d("YbxTrace---" + json);

            if (MyWindowManager.isWindowShowing()) {
                MyWindowManager.notifyDataChange(json);
            }

            uploadImmediately(traceBean);

        } else {
            traces.add(traceBean);
        }
    }

    /**
     * 即时上传
     *
     * @param traceBean
     */
    private void uploadImmediately(TraceBean traceBean) {
        HashMap<String, String> maps = creatMap(traceBean);
        requestUpload(maps, traceBean);

    }

    private HashMap<String, String> creatMap(TraceBean traceBean) {
        HashMap<String, String> maps = new HashMap<>();
        // 基础类参数
        maps.put("en", TextUtils.isEmpty(traceBean.en) ? "" : traceBean.en);
        maps.put("v", TextUtils.isEmpty(traceBean.v) ? "" : traceBean.v);
        maps.put("bid", TextUtils.isEmpty(traceBean.bid) ? "" : traceBean.bid);
        maps.put("ip", TextUtils.isEmpty(traceBean.ip) ? "" : traceBean.ip);
        maps.put("pl", TextUtils.isEmpty(traceBean.pl) ? "" : traceBean.pl);
        maps.put("sdk", TextUtils.isEmpty(String.valueOf(traceBean.sdk)) ? "" : String.valueOf(traceBean.sdk));
        maps.put("gid", TextUtils.isEmpty(String.valueOf(traceBean.gid)) ? "" : String.valueOf(traceBean.gid));
        maps.put("mid", TextUtils.isEmpty(String.valueOf(traceBean.mid)) ? "" : String.valueOf(traceBean.mid));
        maps.put("ct", TextUtils.isEmpty(String.valueOf(traceBean.ct)) ? "" : String.valueOf(traceBean.ct));
        maps.put("l", TextUtils.isEmpty(traceBean.l) ? "" : traceBean.l);
        maps.put("iev", TextUtils.isEmpty(traceBean.iev) ? "" : traceBean.iev);
        maps.put("rst", TextUtils.isEmpty(traceBean.rst) ? "" : traceBean.rst);
        // 特有参数
        switch (traceBean.en) {
            case EventType.Event_Launch:
                break;
            case EventType.Event_Register:
                break;
            case EventType.Event_Pageview:
                maps.put("purl", TextUtils.isEmpty(traceBean.purl) ? "" : traceBean.purl);
                maps.put("purlh", TextUtils.isEmpty(traceBean.purlh) ? "" : traceBean.purlh);
                maps.put("pref", TextUtils.isEmpty(traceBean.pref) ? "" : traceBean.pref);
                maps.put("prefh", TextUtils.isEmpty(traceBean.prefh) ? "" : traceBean.prefh);
                maps.put("chid", TextUtils.isEmpty(traceBean.chid) ? "" : traceBean.chid);
                maps.put("tt", TextUtils.isEmpty(traceBean.tt) ? "" : traceBean.tt);
                //                maps.put("pa", TextUtils.isEmpty(traceBean.pa) ? "" : traceBean.pa);
                break;
            case EventType.Event_Event:
                maps.put("purl", TextUtils.isEmpty(traceBean.purl) ? "" : traceBean.purl);
                maps.put("purlh", TextUtils.isEmpty(traceBean.purlh) ? "" : traceBean.purlh);
                maps.put("pref", TextUtils.isEmpty(traceBean.pref) ? "" : traceBean.pref);
                maps.put("prefh", TextUtils.isEmpty(traceBean.prefh) ? "" : traceBean.prefh);
                maps.put("chid", TextUtils.isEmpty(traceBean.chid) ? "" : traceBean.chid);
                maps.put("tt", TextUtils.isEmpty(traceBean.tt) ? "" : traceBean.tt);
                maps.put("pa", TextUtils.isEmpty(traceBean.pa) ? "" : traceBean.pa);

                maps.put("ca", TextUtils.isEmpty(traceBean.ca) ? "" : traceBean.ca);
                maps.put("ac", TextUtils.isEmpty(traceBean.ac) ? "" : traceBean.ac);

                // kv
                if (traceBean.kv != null) {
                    for (String key : traceBean.kv.keySet()) {
                        maps.put(key, TextUtils.isEmpty(traceBean.kv.get(key)) ? "" : traceBean.kv.get(key));
                    }
                }
                break;
        }
        return maps;
    }

    private void requestUpload(HashMap<String, String> maps, final TraceBean traceBean) {
        ApiRequest.getApiLogstash().activityAnas(maps).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Logger.d("Ybxtrace成功上传事件———" + call.request().url());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.d("Ybxtrace上传事件失败———" + call.request().url());
                Logger.d("Ybxtrace上传事件失败———" + t.getMessage());

                try {
                    String               errorData = (String) SPUtils.get(mContext, "errorCach", "");
                    ArrayList<TraceBean> errorList;
                    if (TextUtils.isEmpty(errorData)) {
                        errorList = new ArrayList<>();
                        errorList.add(traceBean);
                    } else {
                        errorList = new Gson().fromJson(errorData, new TypeToken<ArrayList<TraceBean>>() {
                        }.getType());
                        errorList.add(traceBean);
                    }
                    String data = new Gson().toJson(errorList);
                    SPUtils.put(mContext, "errorCach", data);
                } catch (Exception e) {

                }

            }
        });
    }

    /**
     * 在app进入后台
     * 上传失败的缓存
     *
     * @param context
     */
    public void uploadErrorCache(Context context) {
        try {
            String data = (String) SPUtils.get(context, "errorCach", "");
            if (TextUtils.isEmpty(data))
                return;
            ArrayList<TraceBean> errorCach = new Gson().fromJson(data, new TypeToken<ArrayList<TraceBean>>() {
            }.getType());
            SPUtils.remove(context, "errorCach");
            for (TraceBean traceBean : errorCach) {
                uploadImmediately(traceBean);
            }
        } catch (Exception e) {

        }
    }

}
