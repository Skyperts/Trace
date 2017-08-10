package tom.ybxtracelibrary.Entity;

import java.util.ArrayList;

/**
 * Created by a55 on 2017/7/17.
 */

public class TraceCommonBean {
    // 用户Id
    public String uid;
    // 用户Key，客户端的唯一Key，APP就给设备号(（比如IDFA标识，安卓设备号）)，web和wap就自动生成一个客户端唯一值
    public String u;
    // 应用标识
    public String app;
    // 应用版本号
    public String ver;
    // 平台
    public String p;
    // 屏幕宽度
    public int sh;
    // 屏幕高度
    public int sw;
    // 屏幕分辨率宽
    public String dh;
    // 屏幕分辨率高
    public String dw;
    // 设备信息(OS, 手机型号等)
    public String uagent;

    public ArrayList<TraceBean> traces;

}
