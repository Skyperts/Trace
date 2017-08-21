package tom.ybxtracelibrary;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
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
    private static volatile String mPurl;     // event事件的当前页，pageview事件的前一页

    private static volatile YbxTrace instance;
    private static          Context  mContext;

    public static void initTrace(Context context, TraceCommonBean traceCommonBean, int strategy) {
        // 读取assets下的mappingtxt文件
        //        String mapping = readAssetsTxt(context, fileName);
        //        if (!TextUtils.isEmpty(mapping)) {
        //            traceMapBean = new Gson().fromJson(mapping, TraceMapBean.class);
        //        }
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
    public void pageView(Activity activity, String purl, String tt) {
        if (uploadSwitch) {
            TraceBean traceBean = new TraceBean();
            traceBean.en = EventType.Event_Pageview;
            buildBaseParam(activity, traceBean);

            traceBean.purl = purl;
            //        traceBean.pref = pref;
            traceBean.pref = mPurl;
            traceBean.chid = mChid;
            traceBean.tt = tt;
            //        traceBean.pa = pa;

            upload(traceBean);
        }
    }

    /**
     * 目前有点击事件和订单事件  渠道开端传入渠道号
     *
     * @param activity
     * @param chid     渠道开端点击事件时必须传入，新渠道开端点击事件时重制
     */
    public void event(Activity activity, String purl, String tt, String pa, String category, String action, HashMap<String, String> kv, String chid) {
        if (uploadSwitch) {
            TraceBean traceBean = new TraceBean();
            traceBean.en = EventType.Event_Event;
            buildBaseParam(activity, traceBean);

            mPurl = purl;
            if (!TextUtils.isEmpty(chid)) {
                mChid = chid;
            }

            traceBean.purl = purl;
            //        traceBean.pref = pref;
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
                maps.put("pref", TextUtils.isEmpty(traceBean.pref) ? "" : traceBean.pref);
                maps.put("chid", TextUtils.isEmpty(traceBean.chid) ? "" : traceBean.chid);
                maps.put("tt", TextUtils.isEmpty(traceBean.tt) ? "" : traceBean.tt);
                //                maps.put("pa", TextUtils.isEmpty(traceBean.pa) ? "" : traceBean.pa);
                break;
            case EventType.Event_Event:
                maps.put("purl", TextUtils.isEmpty(traceBean.purl) ? "" : traceBean.purl);
                //                maps.put("pref", TextUtils.isEmpty(traceBean.pref) ? "" : traceBean.pref);
                maps.put("chid", TextUtils.isEmpty(traceBean.chid) ? "" : traceBean.chid);
                maps.put("tt", TextUtils.isEmpty(traceBean.tt) ? "" : traceBean.tt);
                maps.put("pa", TextUtils.isEmpty(traceBean.pa) ? "" : traceBean.pa);

                maps.put("ca", TextUtils.isEmpty(traceBean.ca) ? "" : traceBean.ca);
                maps.put("ac", TextUtils.isEmpty(traceBean.ac) ? "" : traceBean.ac);

                // kv
                if (traceBean.kv != null) {
                    for (String key : traceBean.kv.keySet()) {
                        maps.put(key, traceBean.kv.get(key));
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

    //    private static String readAssetsTxt(Context context, String fileName) {
    //        try {
    //            //Return an AssetManager instance for your application's package
    //            InputStream is   = context.getAssets().open(fileName);
    //            int         size = is.available();
    //            // Read the entire asset into a local byte buffer.
    //            byte[] buffer = new byte[size];
    //            is.read(buffer);
    //            is.close();
    //            // Convert the buffer into a string.
    //            String text = new String(buffer, "utf-8");
    //            // Finally stick the string into the text view.
    //            return text;
    //        } catch (IOException e) {
    //            // Should never happen!
    //            //            throw new RuntimeException(e);
    //            e.printStackTrace();
    //        }
    //        return "";
    //    }

    //    /**
    //     //     * 添加生成的点击事件节点
    //     //     */
    //    public void addClick(String goActivityName, String curlId, Context present, String presentActivityName, String cpos, String cpath) {
    //        TraceBean traceBean = new TraceBean();
    //        traceBean.a = "click";
    //
    //        String curlName = goActivityName;
    //        if (traceMapBean != null) {
    //            HashMap<String, String> vcs = traceMapBean.vc;
    //            if (vcs != null && vcs.containsKey(curlName)) {
    //                curlName = vcs.get(curlName);
    //            }
    //        }
    //        traceBean.curl = curlName;
    //        traceBean.curlId = curlId;
    //
    //        traceBean.t = System.currentTimeMillis();
    //
    //        String preName = presentActivityName;
    //        if (traceMapBean != null) {
    //            HashMap<String, String> vcs = traceMapBean.vc;
    //            if (vcs != null && vcs.containsKey(preName)) {
    //                preName = vcs.get(preName);
    //            }
    //        }
    //        traceBean.l = preName;
    //        traceBean.ltag = present.toString();
    //
    //        traceBean.cpos = cpos;
    //        traceBean.cpath = cpath;
    //
    //        if (uploadStrategy == 1) {    //  0批量1即时
    //            traceBean.uid = traceCommonBean.uid;
    //            traceBean.u = traceCommonBean.u;
    //            traceBean.app = traceCommonBean.app;
    //            traceBean.ver = traceCommonBean.ver;
    //            traceBean.p = traceCommonBean.p;
    //            traceBean.sh = traceCommonBean.sh;
    //            traceBean.sw = traceCommonBean.sw;
    //            traceBean.dh = traceCommonBean.dh;
    //            traceBean.dw = traceCommonBean.dw;
    //            traceBean.uagent = traceCommonBean.uagent;
    //
    //            String json = new Gson().toJson(traceBean);
    //            Logger.d("analysAct------------>" + json);
    //
    //            uploadImmediately(traceBean);
    //
    //        } else {
    //            traces.add(traceBean);
    //        }
    //    }
    //
    //    /**
    //     * 添加进入页面的事件
    //     *
    //     * @param curl
    //     * @param curlId
    //     */
    //    public void addPageview(Context mContext, String curl, String curlId) {
    //
    //        TraceBean traceBean = new TraceBean();
    //        if (uploadStrategy == 1) {    //  0批量1即时
    //            traceBean.uid = traceCommonBean.uid;
    //            traceBean.u = traceCommonBean.u;
    //            traceBean.app = traceCommonBean.app;
    //            traceBean.ver = traceCommonBean.ver;
    //            traceBean.p = traceCommonBean.p;
    //            traceBean.sh = traceCommonBean.sh;
    //            traceBean.sw = traceCommonBean.sw;
    //            traceBean.dh = traceCommonBean.dh;
    //            traceBean.dw = traceCommonBean.dw;
    //            traceBean.uagent = traceCommonBean.uagent;
    //
    //            traceBean.a = "pageview";
    //            traceBean.curl = curl;
    //            traceBean.curlId = curlId;
    //            traceBean.t = System.currentTimeMillis();
    //
    //            uploadImmediately(traceBean);
    //        } else {
    //            traceBean.a = "pageview";
    //            traceBean.curl = curl;
    //            traceBean.curlId = curlId;
    //            traceBean.t = System.currentTimeMillis();
    //
    //            traces.add(traceBean);
    //        }
    //    }
}
