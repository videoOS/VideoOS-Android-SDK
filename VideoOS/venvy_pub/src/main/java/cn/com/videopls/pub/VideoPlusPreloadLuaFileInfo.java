package cn.com.videopls.pub;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.venvy.AppSecret;
import cn.com.venvy.Config;
import cn.com.venvy.Platform;
import cn.com.venvy.PreloadLuaUpdate;
import cn.com.venvy.common.bean.LuaFileInfo;
import cn.com.venvy.common.http.HttpRequest;
import cn.com.venvy.common.http.base.IRequestHandler;
import cn.com.venvy.common.http.base.IResponse;
import cn.com.venvy.common.http.base.Request;
import cn.com.venvy.common.utils.VenvyAesUtil;
import cn.com.venvy.common.utils.VenvyLog;
import cn.com.venvy.lua.plugin.LVCommonParamPlugin;
import cn.com.videopls.pub.view.VideoOSLuaView;

/**
 * Created by videojj_pls on 2019/8/19.
 * App启动预加载lua文件接口(架构优化版本)
 */

public class VideoPlusPreloadLuaFileInfo extends VideoPlusBaseModel {
    private static final String PRE_LOAD_LUA_URL = "/api/v2/preloadLuaFileInfo";
    private PreloadLuaCallback mPreloadLuaCallback;
    private PreloadLuaUpdate mDownLuaUpdate;

    public VideoPlusPreloadLuaFileInfo(Platform platform, PreloadLuaCallback callback) {
        super(platform);
        this.mPreloadLuaCallback = callback;
    }

    @Override
    public boolean needCheckResponseValid() {
        return false;
    }

    @Override
    public IRequestHandler createRequestHandler() {
        return new IRequestHandler() {
            @Override
            public void requestFinish(Request request, IResponse response) {
                try {
                    if (!response.isSuccess()) {
                        PreloadLuaCallback callback = getPreloadLuaCallback();
                        if (callback != null) {
                            callback.updateError(new Exception("preloadLuaFile info data error"));
                        }
                        return;
                    }
                    JSONObject value = new JSONObject(response.getResult());
                    String encryptData = value.optString("encryptData");
                    if (TextUtils.isEmpty(encryptData)) {
                        PreloadLuaCallback callback = getPreloadLuaCallback();
                        if (callback != null) {
                            callback.updateError(new NullPointerException());
                        }
                        return;
                    }
                    JSONObject needValue = new JSONObject(VenvyAesUtil.decrypt(encryptData, AppSecret.getAppSecret(getPlatform()), AppSecret.getAppSecret(getPlatform())));
                    if (needValue == null) {
                        PreloadLuaCallback callback = getPreloadLuaCallback();
                        if (callback != null) {
                            callback.updateError(new NullPointerException());
                        }
                        return;
                    }
                    final String resCode = needValue.optString("resCode");
                    if (!TextUtils.equals(resCode, "00")) {
                        PreloadLuaCallback callback = getPreloadLuaCallback();
                        if (callback != null) {
                            final String resMsg = needValue.optString("resMsg");
                            if (!TextUtils.isEmpty(resMsg)) {
                                callback.updateError(new Exception(resMsg));
                            } else {
                                callback.updateError(new NullPointerException());
                            }
                        }
                        return;
                    }
                    JSONArray miniAppInfoArray = needValue.optJSONArray("miniAppInfoList");
                    if (miniAppInfoArray == null || miniAppInfoArray.length() <= 0) {
                        PreloadLuaCallback callback = getPreloadLuaCallback();
                        if (callback != null) {
                            callback.updateError(new NullPointerException());
                        }
                        return;
                    }
                    List<LuaFileInfo> luaFileInfoList = new ArrayList<>();
                    for (int i = 0; i < miniAppInfoArray.length(); i++) {
                        JSONObject obj = miniAppInfoArray.optJSONObject(i);
                        if (obj == null) {
                            continue;
                        }
                        String miniAppId = obj.optString("miniAppId");
                        if (TextUtils.isEmpty(miniAppId)) {
                            continue;
                        }
                        JSONArray luaListArray = obj.optJSONArray("luaList");
                        if (luaListArray == null || luaListArray.length() <= 0) {
                            continue;
                        }
                        LuaFileInfo luaFileInfo = new LuaFileInfo();
                        luaFileInfo.setMiniAppId(miniAppId);
                        List<LuaFileInfo.LuaListBean> luaList = luaArray2LuaList(luaListArray);

                        if(luaList != null && luaList.size() > 0){
                            luaFileInfo.setLuaList(luaList);
                            luaFileInfoList.add(luaFileInfo);
                        }
                    }

                    if (luaFileInfoList.size() <= 0) {
                        PreloadLuaCallback callback = getPreloadLuaCallback();
                        if (callback != null) {
                            callback.updateError(new NullPointerException());
                        }
                        return;
                    }

                    if (mDownLuaUpdate == null) {
                        mDownLuaUpdate = new PreloadLuaUpdate(Platform.STATISTICS_DOWNLOAD_STAGE_REAPP,getPlatform(), new PreloadLuaUpdate.CacheLuaUpdateCallback() {
                            @Override
                            public void updateComplete(boolean isUpdateByNetWork) {
                                if (isUpdateByNetWork) {
                                    VideoOSLuaView.destroyLuaScript();
                                }
                                PreloadLuaCallback callback = getPreloadLuaCallback();
                                if (callback != null) {
                                    callback.updateComplete(isUpdateByNetWork);
                                }
                            }

                            @Override
                            public void updateError(Throwable t) {
                                PreloadLuaCallback callback = getPreloadLuaCallback();
                                if (callback != null) {
                                    callback.updateError(t);
                                }
                            }
                        });
                    }
                    mDownLuaUpdate.startDownloadLuaFile(luaFileInfoList);
                } catch (Exception e) {
                    VenvyLog.e(VideoPlusPreloadLuaFileInfo.class.getName(), e);
                    PreloadLuaCallback callback = getPreloadLuaCallback();
                    if (callback != null) {
                        callback.updateError(e);
                    }
                }
            }

            @Override
            public void requestError(Request request, @Nullable Exception e) {
                VenvyLog.e(VideoPlusPreloadLuaFileInfo.class.getName(), "App启动预加载lua文件接口访问失败 " + (e != null ? e.getMessage() : ""));
                PreloadLuaCallback callback = getPreloadLuaCallback();
                if (callback != null) {
                    callback.updateError(e);
                }
            }

            @Override
            public void startRequest(Request request) {

            }

            @Override
            public void requestProgress(Request request, int progress) {

            }
        };
    }

    @Override
    public Request createRequest() {
        return HttpRequest.post(Config.HOST_VIDEO_OS + PRE_LOAD_LUA_URL, createBody());
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mDownLuaUpdate != null) {
            mDownLuaUpdate.destroy();
        }
        mPreloadLuaCallback = null;
    }

    public interface PreloadLuaCallback {
        void updateComplete(boolean isUpdateByNetWork);

        void updateError(Throwable t);
    }

    private PreloadLuaCallback getPreloadLuaCallback() {
        return mPreloadLuaCallback;
    }

    private Map<String, String> createBody() {
        Map<String, String> paramBody = new HashMap<>();
        paramBody.put("commonParam", LVCommonParamPlugin.getCommonParamJson());
        paramBody.put("data", VenvyAesUtil.encrypt(AppSecret.getAppSecret(getPlatform()), AppSecret.getAppSecret(getPlatform()), new JSONObject(paramBody).toString()));
        return paramBody;
    }
}
