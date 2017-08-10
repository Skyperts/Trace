package tom.ybxtracelibrary.Net;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by a55 on 2017/8/10.
 */

public class ApiRequest {

    static Retrofit mRetrofitLogstash;

    // 获取埋点点上报api方法
    public static ApiLogstash getApiLogstash() {
        return retrofitLogstash().create(ApiRequest.ApiLogstash.class);
    }

    public static Retrofit retrofitLogstash() {
        if (mRetrofitLogstash == null) {

            mRetrofitLogstash = new Retrofit.Builder()
                    .baseUrl("http://api.55haitao.com/")
//                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return mRetrofitLogstash;
    }

    // 打点上报相关接口
    public interface ApiLogstash {
        // 即时上传
        @POST("log1.gif")
        Call<ResponseBody> activityAnas(@QueryMap Map<String, String> maps);

    }
}
