package net.firekesti.pinnumberpicker;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.TextView;

/**
 * Ripped from AOSP source by kkelly on 11/2/15.
 */
public final class PinNumberPicker extends FrameLayout {
    private static final int NUMBER_VIEWS_RES_ID[] = {
            R.id.previous2_number,
            R.id.previous_number,
            R.id.current_number,
            R.id.next_number,
            R.id.next2_number};
    private static final int CURRENT_NUMBER_VIEW_INDEX = 2;
    private static Animator sFocusedNumberEnterAnimator;
    private static Animator sFocusedNumberExitAnimator;
    private static Animator sAdjacentNumberEnterAnimator;
    private static Animator sAdjacentNumberExitAnimator;
    private static float sAlphaForFocusedNumber;
    private static float sAlphaForAdjacentNumber;
    private int mMinValue;
    private int mMaxValue;
    private int mCurrentValue;
    private int mNextValue;
    private final int mNumberViewHeight;
    private PinNumberPicker mNextNumberPicker;
    private boolean mCancelAnimation;
    private final View mNumberViewHolder;
    private final View mBackgroundView;
    private boolean mArrowsEnabled = false;
    private final View mNumberUpView;
    private final View mNumberDownView;
    private final TextView[] mNumberViews;
    private final OverScroller mScroller;
    private OnFinalNumberDoneListener mListener;
    private boolean allowPlaceholder;
    private String placeholderChar;

    public PinNumberPicker(Context context) {
        this(context, null);
    }

    public PinNumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PinNumberPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        View view = inflate(context, R.layout.pin_number_picker, this);
        mNumberViewHolder = view.findViewById(R.id.number_view_holder);
        mBackgroundView = view.findViewById(R.id.focused_background);
        mNumberUpView = view.findViewById(R.id.number_up_arrow);
        mNumberDownView = view.findViewById(R.id.number_down_arrow);
        mNumberViews = new TextView[NUMBER_VIEWS_RES_ID.length];
        for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
            mNumberViews[i] = (TextView) view.findViewById(NUMBER_VIEWS_RES_ID[i]);
        }
        Resources resources = context.getResources();
        mNumberViewHeight = resources.getDimensionPixelOffset(
                R.dimen.pin_number_picker_text_view_height);
        mScroller = new OverScroller(context);
        mNumberViewHolder.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                updateFocus();
            }
        });
        mNumberViewHolder.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_UP:
                        case KeyEvent.KEYCODE_DPAD_DOWN: {
                            if (!mScroller.isFinished() || mCancelAnimation) {
                                endScrollAnimation();
                            }
                            if (mScroller.isFinished() || mCancelAnimation) {
                                mCancelAnimation = false;
                                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                                    mNextValue = adjustValueInValidRange(mCurrentValue + 1);
                                    startScrollAnimation(true);
                                    mScroller.startScroll(0, 0, 0, mNumberViewHeight,
                                            getResources().getInteger(
                                                    R.integer.pin_number_scroll_duration));
                                } else {
                                    mNextValue = adjustValueInValidRange(mCurrentValue - 1);
                                    startScrollAnimation(false);
                                    mScroller.startScroll(0, 0, 0, -mNumberViewHeight,
                                            getResources().getInteger(
                                                    R.integer.pin_number_scroll_duration));
                                }
                                updateText();
                                invalidate();
                            }
                            return true;
                        }
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_UP:
                        case KeyEvent.KEYCODE_DPAD_DOWN: {
                            mCancelAnimation = true;
                            return true;
                        }
                    }
                }
                return false;
            }
        });
        mNumberViewHolder.setScrollY(mNumberViewHeight);
        mListener = new OnFinalNumberDoneListener();
    }

    public static void loadResources(Context context) {
        if (sFocusedNumberEnterAnimator == null) {
            TypedValue outValue = new TypedValue();
            context.getResources().getValue(
                    R.dimen.pin_alpha_for_focused_number, outValue, true);
            sAlphaForFocusedNumber = outValue.getFloat();
            context.getResources().getValue(
                    R.dimen.pin_alpha_for_adjacent_number, outValue, true);
            sAlphaForAdjacentNumber = outValue.getFloat();
            sFocusedNumberEnterAnimator = AnimatorInflater.loadAnimator(context,
                    R.animator.pin_focused_number_enter);
            sFocusedNumberExitAnimator = AnimatorInflater.loadAnimator(context,
                    R.animator.pin_focused_number_exit);
            sAdjacentNumberEnterAnimator = AnimatorInflater.loadAnimator(context,
                    R.animator.pin_adjacent_number_enter);
            sAdjacentNumberExitAnimator = AnimatorInflater.loadAnimator(context,
                    R.animator.pin_adjacent_number_exit);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            mNumberViewHolder.setScrollY(mScroller.getCurrY() + mNumberViewHeight);
            updateText();
            invalidate();
        } else if (mCurrentValue != mNextValue) {
            mCurrentValue = mNextValue;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            int keyCode = event.getKeyCode();
            if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                setNextValue(keyCode - KeyEvent.KEYCODE_0);
            } else if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER
                    && keyCode != KeyEvent.KEYCODE_ENTER) {
                return super.dispatchKeyEvent(event);
            }
            if (mNextNumberPicker == null) {
                // The user is done - they pressed DPAD_CENTER or ENTER and there's no next number picker.
                mListener.onDone();
            } else {
                mNextNumberPicker.requestFocus();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mNumberViewHolder.setFocusable(enabled);
        for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
            mNumberViews[i].setEnabled(enabled);
        }
    }

    public void startScrollAnimation(boolean scrollUp) {
        if (scrollUp) {
            sAdjacentNumberExitAnimator.setTarget(mNumberViews[1]);
            sFocusedNumberExitAnimator.setTarget(mNumberViews[2]);
            sFocusedNumberEnterAnimator.setTarget(mNumberViews[3]);
            sAdjacentNumberEnterAnimator.setTarget(mNumberViews[4]);
        } else {
            sAdjacentNumberEnterAnimator.setTarget(mNumberViews[0]);
            sFocusedNumberEnterAnimator.setTarget(mNumberViews[1]);
            sFocusedNumberExitAnimator.setTarget(mNumberViews[2]);
            sAdjacentNumberExitAnimator.setTarget(mNumberViews[3]);
        }
        sAdjacentNumberExitAnimator.start();
        sFocusedNumberExitAnimator.start();
        sFocusedNumberEnterAnimator.start();
        sAdjacentNumberEnterAnimator.start();
    }

    public void endScrollAnimation() {
        sAdjacentNumberExitAnimator.end();
        sFocusedNumberExitAnimator.end();
        sFocusedNumberEnterAnimator.end();
        sAdjacentNumberEnterAnimator.end();
        mCurrentValue = mNextValue;
        mNumberViews[1].setAlpha(sAlphaForAdjacentNumber);
        mNumberViews[2].setAlpha(sAlphaForFocusedNumber);
        mNumberViews[3].setAlpha(sAlphaForAdjacentNumber);
    }

    public void setValueRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException(
                    "The min value should be greater than or equal to the max value");
        }
        mMinValue = min;
        mMaxValue = max;
        mNextValue = mCurrentValue = mMinValue - 1;
        clearText();
        mNumberViews[CURRENT_NUMBER_VIEW_INDEX].setText("—");
    }

    public void setArrowsEnabled(boolean enabled) {
        mArrowsEnabled = enabled;
    }

    public void setNextNumberPicker(PinNumberPicker picker) {
        mNextNumberPicker = picker;
    }

    public int getValue() {
        if (mCurrentValue < mMinValue || mCurrentValue > mMaxValue) {
            throw new IllegalStateException("Value is not set");
        }
        return mCurrentValue;
    }

    // Will take effect when the focus is updated.
    public void setNextValue(int value) {
        if (value < mMinValue || value > mMaxValue) {
            throw new IllegalStateException("Value is not set");
        }
        mNextValue = adjustValueInValidRange(value);
    }

    public void setCurrentValue(int value) {
        setNextValue(value);
        mCurrentValue = mNextValue;
        clearText();
    }

    public void updateFocus() {
        endScrollAnimation();
        if (mNumberViewHolder.isFocused()) {
            mBackgroundView.setVisibility(View.VISIBLE);
            if (mArrowsEnabled) {
                mNumberUpView.setVisibility(View.VISIBLE);
                mNumberDownView.setVisibility(View.VISIBLE);
            }
            updateText();
        } else {
            mBackgroundView.setVisibility(View.GONE);
            if (mArrowsEnabled) {
                mNumberUpView.setVisibility(View.GONE);
                mNumberDownView.setVisibility(View.GONE);
            }
            if (!mScroller.isFinished()) {
                mCurrentValue = mNextValue;
                mScroller.abortAnimation();
            }
            clearText();
            mNumberViewHolder.setScrollY(mNumberViewHeight);
        }
    }

    private void clearText() {
        for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
            if (i != CURRENT_NUMBER_VIEW_INDEX) {
                mNumberViews[i].setText("");
            } else if (mCurrentValue >= mMinValue && mCurrentValue <= mMaxValue) {
                String value = String.valueOf(mCurrentValue);
                if (allowPlaceholder && value.length() > 1) {
                    value = placeholderChar;
                }
                mNumberViews[i].setText(value);
            }
        }
    }

    private void updateText() {
        if (mNumberViewHolder.isFocused()) {
            if (mCurrentValue < mMinValue || mCurrentValue > mMaxValue) {
                mNextValue = mCurrentValue = mMinValue;
            }
            int value = adjustValueInValidRange(mCurrentValue - CURRENT_NUMBER_VIEW_INDEX);
            for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
                String text = String.valueOf(adjustValueInValidRange(value));
                if (allowPlaceholder && text.length() > 1) {
                    text = placeholderChar;
                }
                mNumberViews[i].setText(text);
                value = adjustValueInValidRange(value + 1);
            }
        }
    }

    private int adjustValueInValidRange(int value) {
        int interval = mMaxValue - mMinValue + 1;
        if (value < mMinValue - interval || value > mMaxValue + interval) {
            throw new IllegalArgumentException("The value( " + value
                    + ") is too small or too big to adjust");
        }
        return (value < mMinValue) ? value + interval
                : (value > mMaxValue) ? value - interval : value;
    }

    public void setAllowPlaceholder(boolean allowPlaceholder) {
        this.allowPlaceholder = allowPlaceholder;
    }

    public void setPlaceholderCharacter(String val) {
        placeholderChar = val;
    }

    /*
    Interface code for a callback when CENTER/ENTER is pressed on the last picker
     */
    public void setOnFinalNumberDoneListener(OnFinalNumberDoneListener listener) {
        this.mListener = listener;
    }

    public interface DoneListener {
        void onDone();
    }
}
