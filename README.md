# VideoOS SDK 文档

## SDK集成
有两种方式将Video++互动层添加到你的工程：

- 使用项目依赖
- 手动添加配置compile project(':venvy_pub')

##### 兼容性
```
minSdkVersion 16
targetSdkVersion 26 或更高版本进行编译
```

#### 快速集成SDK
1.	使用Gradle集成，具体可参看SDK demo工程配置：
在 root/build.gradle 下设置
repositories {
  mavenCentral()
  maven { url 'https://dl.bintray.com/videoli/maven/' }
}
在 app/build.gradle 下设置
dependencies {
  implementation 'cn.com.videopls.pub:1.1.0'
}
(注：发布地址待定)


2. 配置AndroidManifest.xml 
AndroidManifest.xml需要配置：
    <!-- 允许程序打开网络套接字 -->
    <uses-permission android:name="android.permission.INTERNET" />
   
3. 依赖的第三方库(具体视平台不同而不一致)

```
 implementation "com.github.bumptech.glide:glide:3.8.0"
 implementation "com.squareup.okhttp3:okhttp:3.8.0"
 implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.0.2'
```
	  
## 互动层对接	

### SDK初始化
在 `Application` 项目入口初始化SDK。

示例代码：

```
 VideoPlus.appCreate(Application application);
```
### 对接`VideoPlusView`
	
1. 根据需要接入的`SDK`创建`VideoOsView`，将`SDK`需要的信息配置在`VideoPlusAdapter`中。
	
	* videoID 为点播视频url或直播房间号
	* types 为视频类型（点播or直播），默认为点播

2. 初始化`VideoPlusAdapter`， `VideoOsView`就是生成的互动层，将这个`view`添加到播放器层之上就可以了。`SDK`所需参数需复写VideoPlusAdapter相关方法,详细作用请看注释。

```
	//适配器VideoPlusAdapter (注：VideoView代表平台播放器,非必填)
	public class PlusAdapter extends VideoPlusAdapter {
    private VideoView player;

    public PlusAdapter(VideoView player) {
        this.player = player;
    }
   

    /***
     * 设置配置信息
     * @return Provider配置信息类
     * 注:setVideoID(String videoId)为点播视频ID,直播为房间号
     *    setVideoType(VideoType videoType)为视频类型，VideoType.VIDEOOS点播 VideoType.LIVEOS直播
     */
    @Override
    public Provider createProvider() {
        Provider provider = new Provider.Builder().setVideoID(String.valueOf(12)).setVideoType(VideoType.LIVEOS).build();
        return provider;
    }

    /***
     *
     * @return IMediaControlListener 平台方播放器相关状态
     */
    @Override
    public IMediaControlListener buildMediaController() {
        return new VideoOSMediaController() {
        
            /**
             * horVideoWidth 横屏播放器 width 
             * horVideoHeight 横屏播放器 height
             * verVideoWidth 竖屏播放器 width
             * verVideoHeight 竖屏播放器 height
             * portraitSmallScreenOriginY 异形屏顶部偏移量
             */
            @Override
            public VideoPlayerSize getVideoSize() {
                return new VideoPlayerSize(VenvyUIUtil.getScreenWidth(player.getContext()), 
                VenvyUIUtil.getScreenHeight(player.getContext()),
                        VenvyUIUtil.getScreenWidth(player.getContext()), 200, 0);
            }

            /**
             * 播放器当前播放时间(单位:毫秒)，点播必须复写处理 直播无需此操作。
             */
            @Override
            public long getCurrentPosition() {
                return player != null ? player.getCurrentPosition() : -1;
            }
        };
    }

    //广告展示事件监听
    @Override
    public IWidgetShowListener buildWidgetShowListener() {
        return new IWidgetShowListener<WidgetInfo>() {
            @Override
            public void onShow(WidgetInfo info) {
            }
        };
    }

    //广告点击事件监听
    @Override
    public IWidgetClickListener buildWidgetClickListener() {
        return return new IWidgetClickListener<WidgetInfo>() {
            @Override
            public void onClick(@Nullable WidgetInfo info) {
            }
         };
    }

    //广告关闭事件监听
    @Override
    public IWidgetCloseListener buildWidgetCloseListener() {
        return new IWidgetCloseListener<WidgetInfo>() {
        @Override
            public void onClose(WidgetInfo info) {
            }
        };
    }

    //注册网络图片架构插件
    @Override
    public Class<? extends IImageLoader> buildImageLoader() {
        return GlideImageLoader.class;
    }

    //注册网络请求架构插件
    @Override
    public Class<? extends IRequestConnect> buildConnectProvider() {
        return cn.com.venvy.common.okhttp.OkHttpHelper.class;
    }

    //MQTT长连接结构插件
    @Override
    public Class<? extends ISocketConnect> buildSocketConnect() {
        return VenvyMqtt.class;
    }
}
详细调用请查看官网Demo项目。
```
 
3. 接着，设置设置适配器，代码如下所示

```
   VideoPlusView plusView = new VideoOsView(Context context);
   PlusAdapter plusAdapter = new PlusAdapter(MediaPlay play);
   plusView.setVideoOSAdapter(plusAdapter);
```

4. 全部完成之后调用 `start` ，开启互动层。
    plusView.start();  
5. 如退出播放页面或直播间，调用`stop`方法
    plusView.stop();
6. 其它
6.1屏幕旋转时, 相应的需要对 VideoPlusView 做处理:
plusAdapter.notifyVideoScreenChanged(ScreenStatus.SMALL_VERTICAL);
* SMALL_VERTICAL 播放器竖屏非全屏
* FULL_VERTICAL 播放器竖屏全屏
* LANDSCAPE 播放器横屏

6.2中插视频广告暂停唤醒调用:
plusAdapter.notifyMediaStatusChanged(MediaStatus.PLAYING);

6.3 WidgetInfo 广告事件回调参数说明
* adID 为广告的唯一标识
* adName 为广告名
* eventType 为广告触发的事件，包括展示、点击、关闭等
* actionType 为对接方需要做的操作，包括打开外链，暂停视频，播放视频
* url 为外链地址

#### Demo项目功能

1.Demo项目首页分为直播,点播，点击进入对应的平台。
2.进入默认开启播放器 开启互动。
3.底部提供俩个配置按钮，实现对互动广告的配置。
4.底部“模拟”按钮为测试本地广告功能。
5.底部右侧按钮为互动配置项，点击弹出配置项，可输入“素材名称”以“VideoID”(注:二选一即可)
  其中输入“素材名称”为展示未投放的广告，输入“VideoID”展示已投放的广告。

#### 注意事项

1. VideoPlusAdapter Provider参数为视频的标识(原url),可以用url作为参数 或 使用拼接 ID的方式来识别(前提为与pc对接并通过)。
2. 文档中的代码仅供参考，实际参数请根据项目自行配置。
3. 请将互动层置于合适位置以防阻挡事件分发操作。
4. 最佳位置为加载控制栏的下方,播放器上方。