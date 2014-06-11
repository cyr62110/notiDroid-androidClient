package fr.cvlaminck.notidroid.android.base.views;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * View pager indicator that shows the view pager as a tunnel with steps.
 */
public class PagerTunnelSteps
    extends View {

    /**
     * Color of dots representing both steps and substeps in the tunnel.
     */
    private int stepColor = 0;

    /**
     * Color of dots representing both steps and substeps in the tunnel when this
     * is the current step or this step has been completed.
     */
    private int enabledStepColor = 0;

    /**
     * Radius of a dot representing a step in the tunnel in px.
     */
    private int stepDotRadius = 150;

    /**
     * Radius of a dot representing a substep in px.
     */
    private int subStepRadius = 5;

    /**
     * Color of the number of the step. This number is displayed in the dot
     * representing the step.
     */
    private int textColor = 0;

    /**
     * Size of the number of the step.
     */
    private int textSize = 0;

    /**
     *
     */
    private ViewPager viewPager = null;

    public PagerTunnelSteps(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PagerTunnelSteps(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Must measure with an exact width");
        }

        if(heightMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(widthSize, heightSize);
        } else {
            //The height of our component is equal to the radius of step dot + top/bottom padding
            final int height = stepDotRadius + getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(widthSize, height);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
