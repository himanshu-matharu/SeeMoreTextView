package com.himanshumatharu.seemoretextview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.TypefaceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

public class SeeMoreTextView extends androidx.appcompat.widget.AppCompatTextView {
    private static final int TRIM_MODE_LINES = 0;
    private static final int TRIM_MODE_LENGTH = 1;
    private static final int DEFAULT_TRIM_LENGTH = 105;
    private static final int DEFAULT_TRIM_LINES = 2;
    private static final int INVALID_END_INDEX = -1;
    private static final boolean DEFAULT_SHOW_TRIM_EXPANDED_TEXT = true;
    private static final String ELLIPSIZE = "... ";
    private static final String TAG = "SeeMoreTextView";

    private CharSequence text;
    private BufferType bufferType;
    private boolean readMore = true;
    private int trimLength;
    private CharSequence viewCollapsedText;
    private CharSequence viewExpandedText;
    private final SeeMoreClickableSpan viewMoreSpan;
    private int clickableTextColor;
    private final boolean showTrimExpandedText;

    private int trimMode;
    private int lineEndIndex;
    private int trimLines;

    private boolean animating;
    private final TimeInterpolator expandInterpolator;
    private final TimeInterpolator collapseInterpolator;

    public SeeMoreTextView(Context context){this(context,null);}

    public SeeMoreTextView(Context context, AttributeSet attrs){
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SeeMoreTextView);
        this.trimLength = typedArray.getInt(R.styleable.SeeMoreTextView_trimLength,DEFAULT_TRIM_LENGTH);
        int resourceIdTrimCollapsedText = typedArray.getResourceId(R.styleable.SeeMoreTextView_viewCollapsedText,R.string.read_more);
        int resourceIdTrimExpandedText = typedArray.getResourceId(R.styleable.SeeMoreTextView_viewExpandedText,R.string.read_less);
        this.viewCollapsedText = getResources().getString(resourceIdTrimCollapsedText);
        this.viewExpandedText = getResources().getString(resourceIdTrimExpandedText);
        this.trimLines = typedArray.getInt(R.styleable.SeeMoreTextView_trimLines,DEFAULT_TRIM_LINES);
        this.clickableTextColor = typedArray.getColor(R.styleable.SeeMoreTextView_clickableTextColor, Color.parseColor("#c0c0c0"));
        this.showTrimExpandedText = typedArray.getBoolean(R.styleable.SeeMoreTextView_showTrimExpandedText,DEFAULT_SHOW_TRIM_EXPANDED_TEXT);
        this.trimMode = typedArray.getInt(R.styleable.SeeMoreTextView_trimMode, TRIM_MODE_LINES);
        this.animating = false;
        this.expandInterpolator = new AccelerateDecelerateInterpolator();
        this.collapseInterpolator = new AccelerateDecelerateInterpolator();
        typedArray.recycle();
        viewMoreSpan = new SeeMoreClickableSpan();
        noGlobalLayoutEndIndex();
        setText();
    }

    private void setText(){
        Log.d(TAG,"setText called with text : "+getDisplayableText());
        super.setText(getDisplayableText(),bufferType);
        setMovementMethod(LinkMovementMethod.getInstance());
        setHighlightColor(Color.TRANSPARENT);
    }

    private CharSequence getDisplayableText(){
        return getTrimmedText(text);
    }

    @Override
    public void setText(CharSequence text, BufferType type){
        this.text = text;
        bufferType = type;
        setText();
    }

    private CharSequence getTrimmedText(CharSequence text){
        if (trimMode == TRIM_MODE_LENGTH){
            if (text != null && text.length() > trimLength) {
                if (readMore) {
                    return updateCollapsedText();
                }else{
                    return updateExpandedText();
                }
            }
        }

        if (trimMode == TRIM_MODE_LINES){
            if (text!=null && lineEndIndex > 0){
                if (readMore) {
                    if (getLineCount(text.toString()) > trimLines){
                        return updateCollapsedText();
                    }else if (text.length() > trimLength){
                        return updateCollapsedText();
                    }
                }else {
                    return updateExpandedText();
                }
            }
        }


        return text;
    }

    private CharSequence getDummyTrimmedText(CharSequence text, Boolean stateCollapsed){
        if (trimMode == TRIM_MODE_LENGTH){
            if (text != null && text.length() > trimLength) {
                if (stateCollapsed) {
                    return updateCollapsedText();
                }else{
                    return updateExpandedText();
                }
            }
        }
        if (trimMode == TRIM_MODE_LINES) {
            if (text != null && lineEndIndex > 0) {
                if (stateCollapsed) {
                    if (getLineCount(text.toString()) > trimLines) {
                        return updateCollapsedText();
                    } else if (text.length() > trimLength) {
                        return updateCollapsedText();
                    }
                } else {
                    return updateExpandedText();
                }
            }
        }
        return text;
    }

    private CharSequence updateCollapsedText() {
        int trimEndIndex = text.length();
        switch (trimMode){
            case TRIM_MODE_LINES:
                trimEndIndex = lineEndIndex - (ELLIPSIZE.length() + viewCollapsedText.length());
                if (trimEndIndex < 0){
                    trimEndIndex = trimLength + 1;
                }
                break;
            case TRIM_MODE_LENGTH:
                trimEndIndex = trimLength + 1;
                break;
        }
        SpannableStringBuilder s = new SpannableStringBuilder(text,0,trimEndIndex)
                .append(ELLIPSIZE)
                .append(viewCollapsedText);
        return addClickableSpan(s, viewCollapsedText);
    }

    private CharSequence updateExpandedText(){
        if (showTrimExpandedText){
            SpannableStringBuilder s = new SpannableStringBuilder(text,0,text.length()).append("  ").append(viewExpandedText);
            return addClickableSpan(s, viewExpandedText);
        }
        return text;
    }

    private CharSequence addClickableSpan(SpannableStringBuilder s, CharSequence trimText){
        s.setSpan(viewMoreSpan, s.length() - trimText.length(), s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new TypefaceSpan("sans-serif-medium"), s.length() - trimText.length(), s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return s;
    }

    public void setTrimLength(int trimLength){
        this.trimLength = trimLength;
        setText();
    }

    public void setClickableTextColor(int clickableTextColor){
        this.clickableTextColor = clickableTextColor;
    }

    public void setViewCollapsedText(CharSequence viewCollapsedText){
        this.viewCollapsedText = viewCollapsedText;
    }

    public void setViewExpandedText(CharSequence viewExpandedText){
        this.viewExpandedText = viewExpandedText;
    }

    public void setTrimMode(int trimMode){
        this.trimMode = trimMode;
    }

    public void setTrimLines(int trimLines){
        this.trimLines = trimLines;
    }

    public int getHeightTextView(Context context, CharSequence text){
        TextView textView = new TextView(context);
        textView.setPadding(0,0,0,0);
        textView.setText(text, BufferType.SPANNABLE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, pixelsToSp(getContext(),SeeMoreTextView.this.getTextSize()));
        int textViewWidth = SeeMoreTextView.this.getWidth() - SeeMoreTextView.this.getTotalPaddingStart() - SeeMoreTextView.this.getTotalPaddingEnd();
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(textViewWidth, MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec,heightMeasureSpec);
        return textView.getMeasuredHeight();
    }

    public void expand(){
        if (readMore && !this.animating && trimLines >= 0){
            //measure collapsed height
            Integer collapsedHeight = getHeightTextView(getContext(),getDummyTrimmedText(text,true));

            this.animating = true;

            //measure expanded height
            Integer expandedHeight = getHeightTextView(getContext(),getDummyTrimmedText(text,false));

            final ValueAnimator valueAnimator = ValueAnimator.ofInt(collapsedHeight,expandedHeight);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    SeeMoreTextView.this.setHeight((int) valueAnimator.getAnimatedValue());
                }
            });

            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation){
                    SeeMoreTextView.this.readMore = false;
                    setText();
//                    Log.d(TAG,"starting set text called.");
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    SeeMoreTextView.this.setMinHeight(0);
                    SeeMoreTextView.this.setMaxHeight(Integer.MAX_VALUE);

                    SeeMoreTextView.this.animating = false;

                    final ViewGroup.LayoutParams layoutParams = SeeMoreTextView.this.getLayoutParams();
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    SeeMoreTextView.this.setLayoutParams(layoutParams);
                }
            });

            valueAnimator.setInterpolator(this.expandInterpolator);
            valueAnimator.setDuration(300).start();
        }
    }

    public void collapse(){
        if (!readMore && !this.animating && trimLines >= 0){

            //measure expanded height
            Integer expandedHeight = getHeightTextView(getContext(),getDummyTrimmedText(text,false));

            this.animating = true;

            //measure collapsed height
            Integer collapsedHeight = getHeightTextView(getContext(),getDummyTrimmedText(text,true));

            final ValueAnimator valueAnimator = ValueAnimator.ofInt(expandedHeight,collapsedHeight);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    SeeMoreTextView.this.setHeight((int) valueAnimator.getAnimatedValue());
                }
            });

            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation){
                    SeeMoreTextView.this.readMore = true;
                    setText();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    SeeMoreTextView.this.setMinHeight(0);
                    SeeMoreTextView.this.setMaxHeight(Integer.MAX_VALUE);

                    SeeMoreTextView.this.animating = false;

                    final ViewGroup.LayoutParams layoutParams = SeeMoreTextView.this.getLayoutParams();
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    SeeMoreTextView.this.setLayoutParams(layoutParams);
                }
            });

            valueAnimator.setInterpolator(this.collapseInterpolator);
            valueAnimator.setDuration(300).start();
        }
    }

    public void toggle()
    {
        if (this.readMore)
            this.expand();
        else
            this.collapse();
    }

    public static float pixelsToSp(Context context, float px){
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px/scaledDensity;
    }

    private class SeeMoreClickableSpan extends ClickableSpan {
        @Override
        public void onClick(View widget){
            toggle();
        }

        @Override
        public void updateDrawState(TextPaint ds){
            ds.setColor(clickableTextColor);
        }
    }

    private void noGlobalLayoutEndIndex() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = getViewTreeObserver();
                obs.removeOnGlobalLayoutListener(this);
                refreshLineEndIndex();
                setText();
            }
        });
    }

    private static int getLineCount(String text){
        return text.split("[\n\r]").length;
    }

    private void refreshLineEndIndex(){
        try {
            if (trimLines == 0){
                lineEndIndex = getLayout().getLineEnd(0);
            }else if (trimLines > 0 && getLineCount() >= trimLines){
                lineEndIndex = getLayout().getLineEnd(trimLines - 1);
            }else{
                lineEndIndex = INVALID_END_INDEX;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
