package com.zcmeng.comment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pop.hl.com.poplibrary.BasePopView;
import pop.hl.com.poplibrary.CameraPicPopView;
import pop.hl.com.poplibrary.CommentView;
import pop.hl.com.poplibrary.OnEventListenner;
import pop.hl.com.poplibrary.SharePopView;
import pop.hl.com.poplibrary.base.BasePop;

public class MainActivity extends AppCompatActivity {

    private BasePop.Builder builder;
    private TextView tvStart;
    private ConstraintLayout rootView;

    private OnEventListenner.OnShareClickListenner onShareClickListenner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStart = findViewById(R.id.tvStart);
        rootView = findViewById(R.id.rootView);
        tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                shareView(view);
                cameraView(view);
            }
        });

        onShareClickListenner = new OnEventListenner.OnShareClickListenner() {
            @Override
            public void onClick(View view, int pos) {
                Toast.makeText(MainActivity.this, "pos=" + pos, Toast.LENGTH_SHORT).show();
            }
        };

    }

    private void shareView(View view){
        List<String> _share2Name = new ArrayList<>();
        _share2Name.add("华为");
        _share2Name.add("阿里");
        _share2Name.add("小米");
        _share2Name.add("毛豆");
        _share2Name.add("无聊");
        _share2Name.add("华为");
        _share2Name.add("阿里");
        _share2Name.add("小米");
        _share2Name.add("毛豆");
        _share2Name.add("无聊");
        List<Integer> _share2Icon = new ArrayList<>();
        _share2Icon.add(R.mipmap.huawei);
        _share2Icon.add(R.mipmap.xiaomi);
        _share2Icon.add(R.mipmap.moredo);
        _share2Icon.add(R.mipmap.share_link);
        _share2Icon.add(R.mipmap.huawei);
        _share2Icon.add(R.mipmap.xiaomi);
        _share2Icon.add(R.mipmap.moredo);
        _share2Icon.add(R.drawable.share_link);
        ///< 点击事件回调

        ///< 显示链表添加的分享图标
        int randomValue = new Random().nextInt(2);
        builder = SharePopView.showShare(this, view,
                randomValue == 1 ? _share2Name : null,
                randomValue == 1 ? _share2Icon : null,
//                (new Random().nextInt(2)) == 1 ?
//                        BasePopView.SIMPLE_GRAVITY.FROM_TOP :
                        BasePopView.SIMPLE_GRAVITY.FROM_BOTTOM,
                (new Random().nextInt(2)) == 1 ?
                        SharePopView.SHOW_TYPE.HORIZON :
                        SharePopView.SHOW_TYPE.GRID,
                onShareClickListenner);

//        ///< 显示库默认的显示列表
//        builder = SharePopView.showShareFTencent(this, view,
//                randomValue == 1 ? _share2Name : null,
//                randomValue == 1 ? _share2Icon : null,
//                BasePopView.SIMPLE_GRAVITY.FROM_BOTTOM,
//                (new Random().nextInt(2)) == 1 ?
//                        SharePopView.SHOW_TYPE.HORIZON :
//                        SharePopView.SHOW_TYPE.GRID,
//                onShareClickListenner);
    }

    private void commentView(){
        builder = CommentView.showComment(MainActivity.this,
                "输入评论呀", "啦啦啦，我是卖报的小行家！"/*上一次的记录*/,
                "#FFf5c5c0", false,
                new OnEventListenner.SendBackListener() {
                    @Override
                    public void sendBack(String inputText) {
                        builder.dissmiss();
                        Toast.makeText(MainActivity.this, inputText.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cameraView(View view){
        ///< 相机，相册弹窗 - 分别包含了相机相册按钮区域背景，字体颜色；取消按钮背景和字体颜色；相机相册按钮哪个被按下的回调
        builder = CameraPicPopView.showCamera(this, view,
                "#ffffff", "#000000",
                "#f2f5f7", "#000000",
                new OnEventListenner.OnCameraClickListenner() {
                    @Override
                    public void onClick(View view, CameraPicPopView.CALLBACK_TYPE callback_type) {
                            if (callback_type == CameraPicPopView.CALLBACK_TYPE.CAMERA){
                                Toast.makeText(MainActivity.this, "照相机", Toast.LENGTH_SHORT).show();
                            }else if (callback_type == CameraPicPopView.CALLBACK_TYPE.PHOTO){
                                Toast.makeText(MainActivity.this, "图库", Toast.LENGTH_SHORT).show();
                            }
                    }
                });
    }

}
