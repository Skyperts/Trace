package tom.ybxtracelibrary.Entity;

import java.util.ArrayList;

/**
 * Created by a55 on 2017/7/17.
 */

public class TraceCommonBean {
    // 基础参数
    public String v;            // app版本
    public String bid;          // 业务id（返利、官网直购）
    public String mid;          // 用户id
    public String iev;          // 设备信息

    public ArrayList<TraceBean> traces;

}
