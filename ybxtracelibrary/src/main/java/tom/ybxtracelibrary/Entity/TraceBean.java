package tom.ybxtracelibrary.Entity;

/**
 * Created by a55 on 2017/7/17.
 */

public class TraceBean extends TraceCommonBean {
    // 操作行为
    public String a;                 // click,pageview
    // 进入的页面，（或访问地址）
    public String curl;
    // 进入的页面，（或访问地址）的唯一Id
    public String curlId;
    // 时间
    public long   t;
    // 进入的页面的关键信息，比如id，url等
    public String ext;               // 暂时未用

    // 进入页面前页，（或访问地址）
    public String l;                 // 仅当a == "click"时特有
    // 进入页面前页，（或访问地址）的Hash
    public String ltag;              // 仅当a == "click"时特有
    // 点击的热区的编码
    public String cpos;              // 仅当a == "click"时特有
    // 点击的热区的Path
    public String cpath;             // 仅当a == "click"时特有


}
