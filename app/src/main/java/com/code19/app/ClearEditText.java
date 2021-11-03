package com.code19.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;

public class ClearEditText extends AppCompatEditText implements TextWatcher, View.OnFocusChangeListener {

    private Drawable mClearDrawable;//清除按钮图片
    private boolean hasFocus;

    private TextWatcher mTextWatcher;
    private OnFocusChangeListener mOnFocusChangeListener;

    /**
     * 构造函数常规操作，这里不做过多的说明
     */
    public ClearEditText(Context context) {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        hasFocus = hasFocus();
        //获取清除图标，这个图标是通过布局文件里面的drawableEnd或者drawableRight来设置的
        //getCompoundDrawables：Returns drawables for the left, top, right, and bottom borders.
        //getCompoundDrawablesRelative：Returns drawables for the start, top, end, and bottom borders.
        mClearDrawable = getCompoundDrawables()[2];
        if (mClearDrawable == null) {
            mClearDrawable = getCompoundDrawablesRelative()[2];
        }
        if (mClearDrawable != null) {
            //设置图标的位置以及大小,getIntrinsicWidth()获取显示出来的大小而不是原图片的大小
            mClearDrawable.setBounds(0, 0,
                    mClearDrawable.getIntrinsicWidth(),
                    mClearDrawable.getIntrinsicHeight());
        }
        //默认设置隐藏图标
        setClearIconVisible(false);
        //设置焦点改变的监听
        setOnFocusChangeListener(this);
        //设置输入框里面内容发生改变的监听
        addTextChangedListener(this);
    }

    /**
     * 用记住我们按下的位置来模拟点击事件
     * 当我们按下的位置在 EditText的宽度 - 文本右边到图标左边缘的距离 - 图标左边缘至控件右边缘的距离
     * 到 EditText的右边缘 之间就算点击了图标，竖直方向就以 EditText高度为边界
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && mClearDrawable != null) {
            //getTotalPaddingRight()图标左边缘至控件右边缘的距离
            //getCompoundDrawablePadding()表示从文本右边到图标左边缘的距离
            int left = getWidth() - getTotalPaddingRight() - getCompoundDrawablePadding();
            boolean touchable = event.getX() > left && event.getX() < getWidth();
            if (touchable) {
                this.setText("");
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 当ClearEditText焦点发生变化的时候：
     * 有焦点并且输入的文本内容不为空时则显示清除按钮
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.hasFocus = hasFocus;
        setClearIconVisible(hasFocus && !TextUtils.isEmpty(getText()));
        if (mOnFocusChangeListener != null) {
            mOnFocusChangeListener.onFocusChange(v, hasFocus);
        }
    }


    /**
     * 当输入框里面内容发生变化的时候：
     * 有焦点并且输入的文本内容不为空时则显示清除按钮
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        setClearIconVisible(hasFocus && s.length() > 0);
        if (mTextWatcher != null) {
            mTextWatcher.onTextChanged(s, start, count, after);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (mTextWatcher != null) {
            mTextWatcher.beforeTextChanged(s, start, count, after);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mTextWatcher != null) {
            mTextWatcher.afterTextChanged(s);
        }
    }

    /**
     * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     *
     * @param visible
     */
    protected void setClearIconVisible(boolean visible) {
        if (mClearDrawable == null) return;
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    /**
     * 1、是自己本身的话则调用父类的实现，
     * 2、是外部设置的就自己处理回调回去
     */
    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        if (l instanceof ClearEditText) {
            super.setOnFocusChangeListener(l);
        } else {
            mOnFocusChangeListener = l;
        }
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        if (watcher instanceof ClearEditText) {
            super.addTextChangedListener(watcher);
        } else {
            mTextWatcher = watcher;
        }
    }
}
