package both.video.venvy.com.appdemo.widget;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;

import java.util.List;

import both.video.venvy.com.appdemo.R;
import both.video.venvy.com.appdemo.adapter.VideoUrlConfigAdapter;
import both.video.venvy.com.appdemo.bean.ConfigBean;
import razerdp.basepopup.BasePopupWindow;

/**
 * Created by Lucas on 2019/8/6.
 */
public class VideoUrlPopup extends BasePopupWindow {
    private VideoUrlConfigAdapter mConfigAdapter;

    public VideoUrlPopup(Context context) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public View onCreateContentView() {
        View root = createPopupById(R.layout.config_popup);
        RecyclerView mRecyclerView = root.findViewById(R.id.rv_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mConfigAdapter = new VideoUrlConfigAdapter(null);
        mRecyclerView.setAdapter(mConfigAdapter);
        return root;
    }

    @Override
    protected Animator onCreateShowAnimator() {
        return super.onCreateShowAnimator();
    }

    @Override
    protected Animation onCreateShowAnimation() {
        return getDefaultAlphaAnimation(true);
    }

    @Override
    protected Animation onCreateDismissAnimation() {
        return null;
    }

    public void addData(List<ConfigBean> info) {
        if (info == null || info.size() <= 0) {
            return;
        }
        mConfigAdapter.addData(info);
    }

    public VideoUrlConfigAdapter getAdapter() {
        return mConfigAdapter;
    }
}
