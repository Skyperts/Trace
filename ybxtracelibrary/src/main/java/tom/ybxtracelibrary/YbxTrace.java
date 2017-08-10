package tom.ybxtracelibrary;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tom.ybxtracelibrary.Entity.TraceCommonBean;
import tom.ybxtracelibrary.Entity.TraceBean;
import tom.ybxtracelibrary.Net.ApiRequest;
import tom.ybxtracelibrary.Utils.SPUtils;

/**
 * Created by a55 on 2017/7/17.
 */

public class YbxTrace {
    // 存储转化率事件的list
    private static ArrayList<TraceBean> traces     = new ArrayList<>();    // 批量上传时存储事件
    private static ArrayList<TraceBean> errorCache = new ArrayList<>();   // 存储上传失败的事件

    // 存储转化率事件上传策略
    private static int uploadStrategy = 1;     //    0是批量上传，1是即时上传

    // 存储转化率事件的基础参数
    private static TraceCommonBean traceCommonBean = new TraceCommonBean();     //    0是批量上传，1是即时上传

    private static volatile YbxTrace instance;
    private static Context mContext;

    public static YbxTrace getInstance(Context context) {
        mContext = context;
        if (instance == null) {
            synchronized (YbxTrace.class) {
                if (instance == null) {
                    instance = new YbxTrace();
                }
            }
        }
        return instance;
    }

    private void setTraceCommonBean(TraceCommonBean traceCommonBean) {
        this.traceCommonBean = traceCommonBean;
    }

    /**
     * 添加生成的点击事件节点
     */
    private void addClick(String curl, String curlId, Context per, String perl, String cpos, String cpath) {
        TraceBean traceBean = new TraceBean();
        if (uploadStrategy == 1) {    //  0批量1即时
            traceBean.uid = traceCommonBean.uid;
            traceBean.u = traceCommonBean.u;
            traceBean.app = traceCommonBean.app;
            traceBean.ver = traceCommonBean.ver;
            traceBean.p = traceCommonBean.p;
            traceBean.sh = traceCommonBean.sh;
            traceBean.sw = traceCommonBean.sw;
            traceBean.dh = traceCommonBean.dh;
            traceBean.dw = traceCommonBean.dw;
            traceBean.uagent = traceCommonBean.uagent;

            traceBean.a = "click";
            traceBean.curl = curl;
            traceBean.curlId = curlId;
            traceBean.t = System.currentTimeMillis();
            traceBean.l = perl;
            traceBean.ltag = per.toString();
            traceBean.cpos = cpos;
            traceBean.cpath = cpath;

            uploadImmediately(traceBean, true);

        } else {
            traceBean.a = "click";
            traceBean.curl = curl;
            traceBean.curlId = curlId;
            traceBean.t = System.currentTimeMillis();
            traceBean.l = perl;
            traceBean.ltag = per.toString();
            traceBean.cpos = cpos;
            traceBean.cpath = cpath;

            traces.add(traceBean);
        }
    }

    /**
     * 添加进入页面的事件
     *
     * @param curl
     * @param curlId
     */
    private void addPageview(Context mContext, String curl, String curlId) {

        TraceBean traceBean = new TraceBean();
        if (uploadStrategy == 1) {    //  0批量1即时
            traceBean.uid = traceCommonBean.uid;
            traceBean.u = traceCommonBean.u;
            traceBean.app = traceCommonBean.app;
            traceBean.ver = traceCommonBean.ver;
            traceBean.p = traceCommonBean.p;
            traceBean.sh = traceCommonBean.sh;
            traceBean.sw = traceCommonBean.sw;
            traceBean.dh = traceCommonBean.dh;
            traceBean.dw = traceCommonBean.dw;
            traceBean.uagent = traceCommonBean.uagent;

            traceBean.a = "pageview";
            traceBean.curl = curl;
            traceBean.curlId = curlId;
            traceBean.t = System.currentTimeMillis();

            uploadImmediately(traceBean, false);
        } else {
            traceBean.a = "pageview";
            traceBean.curl = curl;
            traceBean.curlId = curlId;
            traceBean.t = System.currentTimeMillis();

            traces.add(traceBean);
        }
    }

    /**
     * 即时上传
     *
     * @param traceBean
     */
    private void uploadImmediately(final TraceBean traceBean, boolean isaddClick) {

        HashMap<String, String> maps = new HashMap<>();
        maps.put("uid", TextUtils.isEmpty(traceBean.uid) ? "" : traceBean.uid);
        maps.put("u", TextUtils.isEmpty(traceBean.u) ? "" : traceBean.u);
        maps.put("app", TextUtils.isEmpty(traceBean.app) ? "" : traceBean.app);
        maps.put("ver", TextUtils.isEmpty(traceBean.ver) ? "" : traceBean.ver);
        maps.put("p", TextUtils.isEmpty(traceBean.p) ? "" : traceBean.p);
        maps.put("sh", TextUtils.isEmpty(String.valueOf(traceBean.sh)) ? "" : String.valueOf(traceBean.sh));
        maps.put("sw", TextUtils.isEmpty(String.valueOf(traceBean.sw)) ? "" : String.valueOf(traceBean.sw));
        maps.put("dh", TextUtils.isEmpty(String.valueOf(traceBean.dh)) ? "" : String.valueOf(traceBean.dh));
        maps.put("dw", TextUtils.isEmpty(String.valueOf(traceBean.dw)) ? "" : String.valueOf(traceBean.dw));
        maps.put("uagent", TextUtils.isEmpty(traceBean.uagent) ? "" : traceBean.uagent);
        maps.put("a", TextUtils.isEmpty(traceBean.a) ? "" : traceBean.a);
        maps.put("curl", TextUtils.isEmpty(traceBean.curl) ? "" : traceBean.curl);
        maps.put("curlId", TextUtils.isEmpty(traceBean.curlId) ? "" : traceBean.curlId);

        if (isaddClick) {
            maps.put("l", TextUtils.isEmpty(traceBean.l) ? "" : traceBean.l);
            maps.put("ltag", TextUtils.isEmpty(traceBean.ltag) ? "" : traceBean.ltag);
            maps.put("cpos", TextUtils.isEmpty(traceBean.cpos) ? "" : traceBean.cpos);
            maps.put("cpath", TextUtils.isEmpty(traceBean.cpath) ? "" : traceBean.cpath);
        }

        ApiRequest.getApiLogstash().activityAnas(maps).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                Logger.d("Ybxtrace————————————成功上传事件");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                errorCache.add(traceBean);
                String data = new Gson().toJson(errorCache);
                SPUtils.put(mContext, "errorCach", data);
            }
        });

    }

}