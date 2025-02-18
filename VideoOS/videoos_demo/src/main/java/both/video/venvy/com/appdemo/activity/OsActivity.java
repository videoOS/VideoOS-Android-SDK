package both.video.venvy.com.appdemo.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;

import java.util.HashMap;

import both.video.venvy.com.appdemo.R;
import both.video.venvy.com.appdemo.utils.AssetsUtil;
import cn.com.venvy.common.router.IRouterCallback;
import cn.com.venvy.common.utils.VenvyLog;

/**
 * Created by videojj_pls on 2018/9/13.
 */

public class OsActivity extends BasePlayerActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View settingView = getSettingView();
        mRootView.addView(settingView);

    }

    @Override
    protected boolean isLiveOS() {
        return false;
    }

    @Override
    public void onClick(View v) {
        int ID = v.getId();
        if (R.id.bt_os_setting_mall == ID) {
            if (mVideoPlusView == null)
                return;
            mVideoPlusView.stop();
            Uri uri = Uri.parse("LuaView://defaultLuaView?template=os_vote_hotspot.lua&id=os_vote_hotspot");
//            Uri uri = Uri.parse("LuaView://applets?appletId=123&template=test.lua&id=test");
            HashMap<String, String> params = new HashMap<>();
            params.put("data", AssetsUtil.readFileAssets("local_vote.json", OsActivity.this));
            mVideoPlusView.navigation(uri, params, new IRouterCallback() {
                @Override
                public void arrived() {
                    mVideoPlusView.start();
                }

                @Override
                public void lost() {

                }
            });
        } else if (R.id.bt_os_setting_close_window == ID) {
            if (mVideoPlusView == null)
                return;
            mVideoPlusView.closeInfoView();
        }

    }

    /***
     * 底部设置控件
     */
    private View getSettingView() {
        ConstraintLayout mSettingView = null;
        if (mSettingView == null) {
            mSettingView = (ConstraintLayout) LayoutInflater.from(this)
                    .inflate(R.layout.layout_os_setting_button, mRootView, false);
            mSettingView.findViewById(R.id.bt_os_setting_mall).setOnClickListener(this);
        }

        return mSettingView;
    }


}
