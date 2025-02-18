package cn.com.venvy.lua.plugin;

import android.text.TextUtils;

import com.taobao.luaview.util.DimenUtil;
import com.taobao.luaview.util.LuaUtil;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import cn.com.venvy.App;
import cn.com.venvy.Config;
import cn.com.venvy.Platform;
import cn.com.venvy.PlatformInfo;
import cn.com.venvy.common.bean.VideoFrameSize;
import cn.com.venvy.common.bean.VideoPlayerSize;
import cn.com.venvy.common.debug.DebugStatus;
import cn.com.venvy.common.interf.IMediaControlListener;
import cn.com.venvy.common.interf.ScreenStatus;
import cn.com.venvy.common.interf.VideoType;
import cn.com.venvy.common.utils.VenvyDeviceUtil;
import cn.com.venvy.common.utils.VenvyLog;
import cn.com.venvy.lua.binder.VenvyLVLibBinder;

public class LVVideoPlugin {

    private static SdkVersion sSdkVersion;
    private static IsDebug sIsDebug;
    private static ChangeEnvironment sChangeEnvironment;

    public static void install(VenvyLVLibBinder venvyLVLibBinder, Platform platform) {
        venvyLVLibBinder.set("sdkVersion", sSdkVersion == null ? sSdkVersion = new SdkVersion() : sSdkVersion);
        venvyLVLibBinder.set("isDebug", sIsDebug == null ? sIsDebug = new IsDebug() : sIsDebug);
        venvyLVLibBinder.set("setDebug", sChangeEnvironment == null ? sChangeEnvironment = new ChangeEnvironment() : sChangeEnvironment);
        venvyLVLibBinder.set("getVideoSize", new VideoSize(platform));
        venvyLVLibBinder.set("getVideoFrame", new VideoFrame(platform));
        venvyLVLibBinder.set("currentDirection", new CurrentScreenDirection(platform));
        venvyLVLibBinder.set("isFullScreen", new IsFullScreen(platform));
        venvyLVLibBinder.set("appKey", new AppKey(platform));
        venvyLVLibBinder.set("appSecret", new AppSecret(platform));
        venvyLVLibBinder.set("nativeVideoID", new GetVideoID(platform));
        venvyLVLibBinder.set("platformID", new GetPlatformId(platform));
        venvyLVLibBinder.set("getVideoCategory", new GetCategory(platform));
        venvyLVLibBinder.set("getConfigExtendJSONString", new GetExtendJSONString(platform));
        venvyLVLibBinder.set("osType", new OsType(platform));
        venvyLVLibBinder.set("isPhone", new IsPhone());
        venvyLVLibBinder.set("getVideoEpisode", new GetVideoEpisode(platform)); // 获取剧集名称
        venvyLVLibBinder.set("getVideoTitle", new GetVideoTitle(platform)); // 获取视频标题
        venvyLVLibBinder.set("getVideoInfo", new GetVideoInfo(platform)); // 获取剧集名称

    }


    private static class ChangeEnvironment extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            final int fixIndex = VenvyLVLibBinder.fixIndex(args);
            Integer type = LuaUtil.getInt(args, fixIndex + 1);
            if (type != null) {
                DebugStatus.EnvironmentStatus status = DebugStatus.EnvironmentStatus.getStatusByIntType(type);
                DebugStatus.changeEnvironmentStatus(status);
            }
            return LuaValue.TRUE;
        }
    }

    private static class GetExtendJSONString extends VarArgFunction {
        private Platform mPlatform;

        GetExtendJSONString(Platform platform) {
            this.mPlatform = platform;
        }

        @Override
        public Varargs invoke(Varargs args) {
            return mPlatform != null && mPlatform.getPlatformInfo() != null && mPlatform.getPlatformInfo().getExtendDict() != null && mPlatform.getPlatformInfo().getExtendDict().size() > 0 ? LuaUtil.toTable(mPlatform.getPlatformInfo().getExtendDict()) : LuaValue.NIL;
        }
    }

    private static class GetCategory extends VarArgFunction {
        private Platform mPlatform;

        GetCategory(Platform platform) {
            this.mPlatform = platform;
        }

        @Override
        public Varargs invoke(Varargs args) {
            return mPlatform != null && mPlatform.getPlatformInfo().getVideoCategory() != null ? LuaValue.valueOf(mPlatform.getPlatformInfo().getVideoCategory()) : LuaValue.NIL;
        }
    }


    private static class GetPlatformId extends VarArgFunction {
        private Platform mPlatform;

        GetPlatformId(Platform platform) {
            this.mPlatform = platform;
        }

        @Override
        public Varargs invoke(Varargs args) {
            return mPlatform != null && mPlatform.getPlatformInfo().getThirdPlatformId() != null ? LuaValue.valueOf(mPlatform.getPlatformInfo().getThirdPlatformId()) : LuaValue.NIL;
        }
    }

    private static class GetVideoID extends VarArgFunction {
        private Platform mPlatform;

        GetVideoID(Platform platform) {
            this.mPlatform = platform;
        }

        @Override
        public Varargs invoke(Varargs args) {
            return mPlatform != null && mPlatform.getPlatformInfo().getVideoId() != null ? LuaValue.valueOf(mPlatform.getPlatformInfo().getVideoId()) : LuaValue.NIL;
        }
    }

    private static class IsFullScreen extends VarArgFunction {
        private Platform mPlatform;

        IsFullScreen(Platform platform) {
            this.mPlatform = platform;
        }

        @Override
        public Varargs invoke(Varargs args) {
            int value = mPlatform != null ? mPlatform.getPlatformInfo().getInitDirection().getId() : ScreenStatus.SMALL_VERTICAL.getId();

            boolean b = value == ScreenStatus.FULL_VERTICAL.getId();
            VenvyLog.i("IsFullScreen b= " + b);
            return LuaValue.valueOf(b);
        }
    }

    private static class CurrentScreenDirection extends VarArgFunction {
        private Platform mPlatform;

        CurrentScreenDirection(Platform platform) {
            this.mPlatform = platform;
        }

        @Override
        public Varargs invoke(Varargs args) {
            return mPlatform != null ? LuaValue.valueOf(mPlatform.getPlatformInfo().getInitDirection().getId()) : LuaValue.valueOf(ScreenStatus.SMALL_VERTICAL.getId());
        }
    }


    /**
     * 获取AppKey
     */
    private static class AppKey extends VarArgFunction {
        private Platform mPlatform;

        AppKey(Platform platform) {
            this.mPlatform = platform;
        }

        @Override
        public Varargs invoke(Varargs args) {
            return mPlatform != null ? LuaValue.valueOf(mPlatform.getPlatformInfo().getAppKey()) : LuaValue.NIL;
        }
    }

    /**
     * 获取AppSecret
     */
    private static class AppSecret extends VarArgFunction {
        private Platform mPlatform;

        AppSecret(Platform platform) {
            this.mPlatform = platform;
        }

        @Override
        public Varargs invoke(Varargs args) {
            return mPlatform != null && mPlatform.getPlatformInfo() != null && !TextUtils.isEmpty(mPlatform.getPlatformInfo().getAppSecret()) ? LuaValue.valueOf(mPlatform.getPlatformInfo().getAppSecret()) : LuaValue.valueOf(cn.com.venvy.AppSecret.getAppSecret(mPlatform));
        }
    }

    /**
     * 获取sdk版本号
     */
    private static class SdkVersion extends VarArgFunction {
        @Override
        public LuaValue invoke(Varargs args) {
            return LuaValue.valueOf(Config.SDK_VERSION != null ? Config.SDK_VERSION : "");
        }
    }


    private static class VideoSize extends VarArgFunction {
        private Platform mPlatform;

        VideoSize(Platform platform) {
            this.mPlatform = platform;
        }

        @Override
        public Varargs invoke(Varargs args) {

            final int fixIndex = VenvyLVLibBinder.fixIndex(args);
            Integer type = LuaUtil.getInt(args, fixIndex + 1);

            if (mPlatform != null) {
                float width = 0.0f;
                float height = 0.0f;
                float marginTop = 0.0f;

                if (type != null) {
                    switch (type) {
                        case 0: // 竖屏小屏
                            if (mPlatform.getMediaControlListener() != null) {
                                VideoPlayerSize videoPlayerSize = mPlatform.getMediaControlListener().getVideoSize();
                                if (videoPlayerSize != null && videoPlayerSize.mVerVideoHeight > 0) {
                                    width = DimenUtil.pxToDpi(videoPlayerSize.mVerVideoWidth);
                                    height = DimenUtil.pxToDpi(videoPlayerSize.mVerVideoHeight);
                                    marginTop = DimenUtil.pxToDpi(videoPlayerSize.mPortraitSmallScreenOriginY);
                                    break;
                                }
                            }
                            width = DimenUtil.pxToDpi(mPlatform.getPlatformInfo().getVerVideoWidth());
                            height = DimenUtil.pxToDpi(mPlatform.getPlatformInfo().getVerVideoHeight());
                            break;

                        case 1: // 竖屏全屏
                            if (mPlatform.getMediaControlListener() != null) {
                                VideoPlayerSize videoPlayerSize = mPlatform.getMediaControlListener().getVideoSize();
                                if (videoPlayerSize != null && videoPlayerSize.mVerVideoHeight > 0) {
                                    width = DimenUtil.pxToDpi(videoPlayerSize.mFullScreenContentWidth);
                                    height = DimenUtil.pxToDpi(videoPlayerSize.mFullScreenContentHeight);
                                    marginTop = DimenUtil.pxToDpi(videoPlayerSize.mPortraitSmallScreenOriginY);
                                    break;
                                }
                            }
                            width = DimenUtil.pxToDpi(Math.min(mPlatform.getPlatformInfo().getVideoWidth(), mPlatform.getPlatformInfo().getVideoHeight()));
                            height = DimenUtil.pxToDpi(Math.max(mPlatform.getPlatformInfo().getVideoWidth(), mPlatform.getPlatformInfo().getVideoHeight()));
                            break;

                        default: //横屏全屏
                            if (mPlatform.getMediaControlListener() != null) {
                                VideoPlayerSize videoPlayerSize = mPlatform.getMediaControlListener().getVideoSize();
                                if (videoPlayerSize != null && videoPlayerSize.mVerVideoHeight > 0) {
                                    width = DimenUtil.pxToDpi(videoPlayerSize.mFullScreenContentWidth);
                                    height = DimenUtil.pxToDpi(videoPlayerSize.mFullScreenContentHeight);
                                    marginTop = DimenUtil.pxToDpi(videoPlayerSize.mPortraitSmallScreenOriginY);
                                    break;
                                }
                            }
                            width = DimenUtil.pxToDpi(mPlatform.getPlatformInfo().getVideoWidth());
                            height = DimenUtil.pxToDpi(mPlatform.getPlatformInfo().getVideoHeight());
                            break;
                    }
                }
                LuaValue[] luaValue = new LuaValue[]{LuaValue.valueOf(width), LuaValue.valueOf(height), LuaValue.valueOf(marginTop)};
                return LuaValue.varargsOf(luaValue);
            } else {
                return LuaValue.NIL;
            }

        }
    }

    private static class VideoFrame extends VarArgFunction {
        private Platform mPlatform;

        VideoFrame(Platform platform) {
            this.mPlatform = platform;
        }

        @Override
        public Varargs invoke(Varargs args) {

            if (mPlatform == null || mPlatform.getMediaControlListener() == null) {
                LuaValue[] luaValue = new LuaValue[]{LuaValue.valueOf(0), LuaValue.valueOf(0), LuaValue.valueOf(0), LuaValue.valueOf(0)};
                return LuaValue.varargsOf(luaValue);
            }
            VideoFrameSize videoFrameSize = mPlatform.getMediaControlListener().getVideoFrameSize();
            if (videoFrameSize == null) {
                LuaValue[] luaValue = new LuaValue[]{LuaValue.valueOf(0), LuaValue.valueOf(0), LuaValue.valueOf(0), LuaValue.valueOf(0)};
                return LuaValue.varargsOf(luaValue);
            }
            float videoWidth = DimenUtil.pxToDpi(videoFrameSize.mVideoFrameWidth);
            float videoHeight = DimenUtil.pxToDpi(videoFrameSize.mVideoFrameHeight);
            float x = DimenUtil.pxToDpi(videoFrameSize.mVideoFrameX);
            float y = DimenUtil.pxToDpi(videoFrameSize.mVideoFrameY);
            LuaValue[] luaValue = new LuaValue[]{LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(videoWidth), LuaValue.valueOf(videoHeight)};
            return LuaValue.varargsOf(luaValue);

        }
    }

    /**
     * 判断sdk是否是debug状态
     */
    private static class IsDebug extends VarArgFunction {
        @Override
        public LuaValue invoke(Varargs args) {
            return LuaValue.valueOf(DebugStatus.getCurrentEnvironmentStatus().getEnvironmentValue());
        }
    }

    private static class OsType extends VarArgFunction {
        private Platform mPlatform;

        OsType(Platform platform) {
            this.mPlatform = platform;
        }

        @Override
        public Varargs invoke(Varargs args) {
            int value = mPlatform != null ? mPlatform.getPlatformInfo().getVideoType().getId() : VideoType.VIDEOOS.getId();
            return LuaValue.valueOf(value);
        }
    }


    /**
     * 判断是否是手机设备
     * <p>
     * 支持sim card 且 屏幕尺寸小于20则认为是手机设备
     */
    private static class IsPhone extends VarArgFunction {
        @Override
        public LuaValue invoke(Varargs args) {
            boolean isPhone = VenvyDeviceUtil.isSupportSimCard(App.getContext()) && VenvyDeviceUtil.getScreenDimension(App.getContext()) < 20.0;

            return LuaValue.valueOf(isPhone);
        }
    }

    /**
     * 获取剧集名称
     */
    private static class GetVideoEpisode extends VarArgFunction {
        private Platform platform;

        public GetVideoEpisode(Platform platform) {
            this.platform = platform;
        }

        @Override
        public LuaValue invoke(Varargs args) {
            if (platform.getMediaControlListener() != null && !TextUtils.isEmpty(platform.getMediaControlListener().getVideoEpisode())) {
                return LuaValue.valueOf(platform.getMediaControlListener().getVideoEpisode());
            }
            return LuaValue.valueOf("");
        }
    }

    /**
     * 获取视频标题
     */
    private static class GetVideoTitle extends VarArgFunction {
        private Platform platform;

        public GetVideoTitle(Platform platform) {
            this.platform = platform;
        }

        @Override
        public LuaValue invoke(Varargs args) {
            if (platform.getMediaControlListener() != null && !TextUtils.isEmpty(platform.getMediaControlListener().getVideoTitle())) {
                return LuaValue.valueOf(platform.getMediaControlListener().getVideoTitle());
            }
            return LuaValue.valueOf("");
        }
    }

    /**
     * 获取视频信息
     */
    private static class GetVideoInfo extends VarArgFunction {
        private Platform platform;

        public GetVideoInfo(Platform platform) {
            this.platform = platform;
        }

        @Override
        public Varargs invoke(Varargs args) {
            LuaTable table = LuaValue.tableOf();
            PlatformInfo platformInfo = platform.getPlatformInfo();
            if (!TextUtils.isEmpty(platformInfo.getVideoId())) {
                table.set("videoId", platformInfo.getVideoId());
            }
            IMediaControlListener mediaControlListener = platform.getMediaControlListener();
            if (mediaControlListener != null) {
                if (!TextUtils.isEmpty(mediaControlListener.getVideoEpisode())) {
                    table.set("episode", mediaControlListener.getVideoEpisode());
                }
                if (!TextUtils.isEmpty(mediaControlListener.getVideoTitle())) {
                    table.set("title", mediaControlListener.getVideoTitle());
                }
            }
            if (!TextUtils.isEmpty(platformInfo.getThirdPlatformId())) {
                table.set("platformId", platformInfo.getThirdPlatformId());
            }
            if (!TextUtils.isEmpty(platformInfo.getVideoCategory())) {
                table.set("category", platformInfo.getVideoCategory());
            }
            if (platformInfo.getExtendDict() != null && platformInfo.getExtendDict().size() > 0) {
                table.set("extendDict", LuaUtil.toTable(platformInfo.getExtendDict()));
            }
            return table;
        }
    }

}
