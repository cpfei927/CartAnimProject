package com.cpfei.cartanimproject;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @Author: chengpengfei
 * @CreateDate: 2019/9/9 18:14
 * @UpdateUser: 更新者：
 * @UpdateDate: 2019/9/9 18:14
 */
public class CartAnimUtils {

    private Context context;
    private PathMeasure mPathMeasure;

    private RelativeLayout mShoppingCartRly;
    private View startView, endView;
    private TextView goodsImg;

    // 贝塞尔曲线中间过程点坐标
    private float[] mCurrentPosition = new float[2];

    private int mCenterY = 200;

    private int duration = 300; // 毫秒

    public CartAnimUtils(Context context) {
        this.context = context;
    }

    private void init() {
        ViewGroup rootView = (ViewGroup) ((Activity) context).getWindow().getDecorView();
        if (rootView == null) return;
        mShoppingCartRly = new RelativeLayout(context);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mShoppingCartRly.setLayoutParams(lp);
        mShoppingCartRly.setId(Integer.MAX_VALUE);
        mShoppingCartRly.setBackgroundResource(android.R.color.transparent);
        rootView.addView(mShoppingCartRly);
    }

    private void remove() {
        if (mShoppingCartRly == null) return;
        ViewGroup rootView = (ViewGroup) ((Activity) context).getWindow().getDecorView();
        if (rootView == null) return;
        if (goodsImg != null) {
            mShoppingCartRly.removeView(goodsImg);
        }
        rootView.removeView(mShoppingCartRly);
    }

    public void setCenterY(int centerY) {
        this.mCenterY = centerY;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * 添加商品到购物车
     * @author leibing
     * @createTime 2016/09/28
     * @lastModify 2016/09/28
     * @param goodsImg 商品图标
     * @return
     */
    public void addGoodsToCart(TextView goodsImg, final View startView, final View endView) {
        this.startView = startView;
        this.endView = endView;
        this.goodsImg = goodsImg;

        init();

        if (startView == null || endView == null || goodsImg == null) {
            return;
        }

        RelativeLayout parent = (RelativeLayout) goodsImg.getParent();
        if (parent != null) {
            parent.removeAllViews();
        }
        mShoppingCartRly.addView(goodsImg);

        goodsImg.post(new Runnable() {
            @Override
            public void run() {
                initAnim();
            }
        });
    }

    private void initAnim() {

        Log.d("TAG", "goodsImg width = " + goodsImg.getWidth() + ", goodsImgHeight = " + goodsImg.getHeight());
        // 得到父布局的起始点坐标（用于辅助计算动画开始/结束时的点的坐标）
        int[] parentLocation = new int[2];
        mShoppingCartRly.getLocationInWindow(parentLocation);

        // 得到商品图片的坐标（用于计算动画开始的坐标）
        int startLoc[] = new int[2];
        startView.getLocationInWindow(startLoc);
        startLoc[0] += startView.getWidth() / 2;
        startLoc[1] += startView.getHeight() / 2;

        // 得到购物车图片的坐标(用于计算动画结束后的坐标)
        int endLoc[] = new int[2];
        endView.getLocationInWindow(endLoc);
        endLoc[0] += endView.getWidth() / 2;
        endLoc[1] += endView.getHeight() / 2;

        // 开始掉落的商品的起始点：商品起始点-父布局起始点+该商品图片的一半
        float startX = startLoc[0] - parentLocation[0] - goodsImg.getWidth() / 2;
        float startY = startLoc[1] - parentLocation[1] - goodsImg.getHeight() / 2;

        // 商品掉落后的终点坐标：购物车起始点-父布局起始点+购物车图片的1/5
        float toX = endLoc[0]  - parentLocation[0] - goodsImg.getWidth() / 2;
        float toY = endLoc[1]  - parentLocation[1] - goodsImg.getHeight() / 2;

        Log.d("TAG", "startX = " + startX + ", startY = " + startY + ", toX = " + toX + ", toY = " + toY);

        // 开始绘制贝塞尔曲线
        Path path = new Path();
        // 移动到起始点（贝塞尔曲线的起点）
        path.moveTo(startX, startY);
        // 使用二阶贝塞尔曲线：注意第一个起始坐标越大，贝塞尔曲线的横向距离就会越大，一般按照下面的式子取即可
        path.quadTo((startX + toX) / 2, startY - mCenterY, toX, toY);
        // mPathMeasure用来计算贝塞尔曲线的曲线长度和贝塞尔曲线中间插值的坐标，如果是true，path会形成一个闭环
        mPathMeasure = new PathMeasure(path, false);

        // 属性动画实现（从0到贝塞尔曲线的长度之间进行插值计算，获取中间过程的距离值）
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, mPathMeasure.getLength());
        valueAnimator.setDuration(duration);

        // 匀速线性插值器
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 当插值计算进行时，获取中间的每个值，
                // 这里这个值是中间过程中的曲线长度（下面根据这个值来得出中间点的坐标值）
                float value = (Float) animation.getAnimatedValue();
                // 获取当前点坐标封装到mCurrentPosition
                // boolean getPosTan(float distance, float[] pos, float[] tan) ：
                // 传入一个距离distance(0<=distance<=getLength())，然后会计算当前距离的坐标点和切线，pos会自动填充上坐标，这个方法很重要。
                // mCurrentPosition此时就是中间距离点的坐标值
                mPathMeasure.getPosTan(value, mCurrentPosition, null);
                // 移动的商品图片（动画图片）的坐标设置为该中间点的坐标
                goodsImg.setTranslationX(mCurrentPosition[0]);
                goodsImg.setTranslationY(mCurrentPosition[1]);
            }
        });

        // 开始执行动画
        valueAnimator.start();

        // 动画结束后的处理
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                Animation anim =new RotateAnimation(0f, -20f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setFillAfter(true); // 设置保持动画最后的状态
                anim.setDuration(200); // 设置动画时间
                anim.setInterpolator(new AnticipateInterpolator()); // 设置插入器
                anim.setRepeatCount(1);
                anim.setFillBefore(true);
                anim.setRepeatMode(Animation.REVERSE);
                endView.startAnimation(anim);
                // 把执行动画的商品图片从父布局中移除
                remove();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

    }


}
