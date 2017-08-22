package tom.ybxtracelibrary;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * Created by a55 on 2017/8/22.
 */

public class MyWindowManager {
    private static MyFloatView                myFloatView;
    /**
     * 用于控制在屏幕上添加或移除悬浮窗
     */
    private static WindowManager              mWindowManager;
    /**
     * 小悬浮窗View的参数
     */
    private static WindowManager.LayoutParams smallWindowParams;

    public static void createFloatView(Context context) {
        //        myFloatView=new MyFloatView(context);
        //        myFV.setImageResource(R.mipmap.ic_about_us_logo);
        //获取WindowManager
        //        mWindowManager=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

        WindowManager windowManager = getWindowManager(context);
        int           screenWidth   = windowManager.getDefaultDisplay().getWidth();
        int           screenHeight  = windowManager.getDefaultDisplay().getHeight();
        if (myFloatView == null) {
            myFloatView = new MyFloatView(context);
            if (smallWindowParams == null) {
                smallWindowParams = new WindowManager.LayoutParams();
                int type = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    //解决Android 7.1.1起不能再用Toast的问题（先解决crash）
                    if (Build.VERSION.SDK_INT > 24) {
                        type = WindowManager.LayoutParams.TYPE_PHONE;
                    } else {
                        type = WindowManager.LayoutParams.TYPE_TOAST;
                    }
                } else {
                    type = WindowManager.LayoutParams.TYPE_PHONE;
                }
                smallWindowParams.type = type;
                smallWindowParams.format = PixelFormat.RGBA_8888;
                smallWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
                smallWindowParams.width = 400;
                smallWindowParams.height = 400;
                smallWindowParams.x = screenWidth;
                smallWindowParams.y = screenHeight / 2;
            }
            myFloatView.setWmParams(smallWindowParams);
            windowManager.addView(myFloatView, smallWindowParams);
        }
        //        //设置LayoutParams(全局变量）相关参数
        //        wmParams = ((HaiApplication)getApplication()).getMywmParams();
        //        int type = 0;
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        //            //解决Android 7.1.1起不能再用Toast的问题（先解决crash）
        //            if(Build.VERSION.SDK_INT > 24){
        //                type = WindowManager.LayoutParams.TYPE_PHONE;
        //            }else{
        //                type = WindowManager.LayoutParams.TYPE_TOAST;
        //            }
        //        } else {
        //            type = WindowManager.LayoutParams.TYPE_PHONE;
        //        }
        //        wmParams.type= type; //设置window type
        //        wmParams.format= PixelFormat.RGBA_8888; //设置图片格式，效果为背景透明
        //        //设置Window flag
        //        wmParams.flags= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        //                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //        wmParams.gravity= Gravity.LEFT|Gravity.TOP; //调整悬浮窗口至左上角
        //        //以屏幕左上角为原点，设置x、y初始值
        //        wmParams.x=0;
        //        wmParams.y=0;
        //        //设置悬浮窗口长宽数据
        //        wmParams.width=40;
        //        wmParams.height=40;
        //        //显示myFloatView图像
        //        mWindowManager.addView(myFloatView, wmParams);
    }

    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @param context 必须为应用程序的Context.
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
     *
     * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
     */
    public static boolean isWindowShowing() {
        return myFloatView != null ;
    }

    /**
     * 将小悬浮窗从屏幕上移除。
     *
     * @param context
     *            必须为应用程序的Context.
     */
    public static void removeSmallWindow(Context context) {
        if (myFloatView != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(myFloatView);
            myFloatView = null;
        }
    }

}
