package tom.ybxtracelibrary.annotation;

import android.support.annotation.StringDef;

/**
 * Created by a55 on 2017/8/15.
 */
@StringDef({EventType.Event_Launch, EventType.Event_Pageview, EventType.Event_Register, EventType.Event_Event})
public @interface EventType {
    String Event_Launch = "la";
    String Event_Pageview = "pv";
    String Event_Register = "re";
    String Event_Event = "e";
}
