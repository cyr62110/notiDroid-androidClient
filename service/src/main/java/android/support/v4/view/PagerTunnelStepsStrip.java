package android.support.v4.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;

/**
 * View pager indicator that shows the view pager as a tunnel with steps.
 */
public class PagerTunnelStepsStrip
        extends View
        implements ViewPager.Decor {
    private final static String TAG = PagerTunnelStepsStrip.class.getSimpleName();

    /**
     * Title that must be associated with the page in the PagerAdapter to display
     * the page as a step on this view.
     */
    public final static String STEP_TITLE = "+";

    /**
     * Title that must be associated with fragment in the PagerAdapter to display
     * the page as a substep on this view.
     */
    public final static String SUBSTEP_TITLE = "-";

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
    private int stepDotDiameter = 150;

    /**
     * Radius of a dot representing a substep in px.
     */
    private int subStepDotDiameter = 5;

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

    /**
     * PagerAdapter currently used by the parent ViewPager.
     */
    private PagerAdapter pagerAdapter = null;

    /**
     * Class that will listen ViewPager events and redraw our view when required.
     */
    private PageListener pageListener = null;

    /**
     * Last position received from the ViewPager on a onPageScrolled event
     * by the PageListener.
     */
    private int lastPosition = 0;

    /**
     * Last position received from the ViewPager on a onPageScrolled event
     * by the PageListener.
     */
    private float lastPositionOffset = 0;


    public PagerTunnelStepsStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        pageListener = new PageListener();
    }

    public PagerTunnelStepsStrip(Context context) {
        super(context);
        pageListener = new PageListener();
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

        if (heightMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(widthSize, heightSize);
        } else {
            //The height of our component is equal to the radius of step dot + top/bottom padding
            final int height = stepDotDiameter + getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(widthSize, height);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        //We check the the direct parent of this view is a ViewPager
        final ViewParent parent = getParent();
        if (!(parent instanceof ViewPager)) {
            throw new IllegalStateException("PagerTunnelStepsStrip must be a direct child of a ViewPager.");
        }

        //Then we set some listeners on the parent ViewPager.
        this.viewPager = (ViewPager) parent;
        this.pagerAdapter = viewPager.getAdapter();
        viewPager.setInternalPageChangeListener(pageListener);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (viewPager != null)
            viewPager.setInternalPageChangeListener(null);
        this.viewPager = null;
        this.pagerAdapter = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //If the pagerAdapter is null or empty, we do not display anything
        if (pagerAdapter == null || pagerAdapter.getCount() == 0)
            return;

        //Paints that will be use to draw our component.
        final Paint stepColorPaint = new Paint();
        stepColorPaint.setColor(Color.GRAY); //TODO
        final Paint enabledStepColorPaint = new Paint();
        enabledStepColorPaint.setColor(Color.GREEN); //TODO

        //Number that will be displayed for the next step that will be drawn
        int nextDrawnStepNumber = 1;

        //Then we iterate on pages in the pagerAdapter to know if we should draw
        //a step or a substep. The title of the page will determine this.
        for (int pagePosition = 0; pagePosition < pagerAdapter.getCount(); pagePosition++) {
            //Color that will be used to draw the dot representing this step/substep
            final Paint dotColorPaint;
            //Radius of the dot depending on the page being a step or a substep
            final float dotRadius = stepDotDiameter / 2.0f;
            //X-coordinates of the center of the dot representing the step/substep
            final float dotCenterX = (pagePosition + 1) * (getWidth() / (pagerAdapter.getCount() + 1));
            //Y-coordinates of the center of the dot representing the step/substep
            final float dotCenterY = getHeight() / 2.0f;

            //If we have passed this step, we use the enable color otherwise we use the other color.
            //When the user is dragging the page, we make a color transition effect.
            if(pagePosition <= lastPosition) {
                dotColorPaint = enabledStepColorPaint;
                stepColorPaint.setAlpha(0);
            } else if(pagePosition == (lastPosition + 1) && lastPositionOffset != 0.0f) {
                dotColorPaint = enabledStepColorPaint;
                dotColorPaint.setAlpha((int) (lastPositionOffset * 255));
                stepColorPaint.setAlpha((int) (255 - (lastPositionOffset * 255)));
            } else {
                dotColorPaint = stepColorPaint;
                stepColorPaint.setAlpha(255);
            }

            if (STEP_TITLE.equals(pagerAdapter.getPageTitle(pagePosition))) {

                nextDrawnStepNumber ++;
            } else if (SUBSTEP_TITLE.equals(pagerAdapter.getPageTitle(pagePosition))) {

            } else
                throw new IllegalStateException(pagerAdapter.getPageTitle(pagePosition)
                        + " is not recognized as a step or substep title. Please use STEP_TITLE and SUBSTEP_TITLE as title for your page.");
            //Finally, we draw our step/substep on our canvas
            final RectF dotRect = new RectF(dotCenterX - dotRadius, dotCenterY - dotRadius,
                    dotCenterX + dotRadius, dotCenterY + dotRadius);
            //We draw the background dot only if its alpha is greater than 0
            if(stepColorPaint.getAlpha() > 0)
                canvas.drawOval(dotRect, stepColorPaint);
            canvas.drawOval(dotRect, dotColorPaint);

        }
    }

    /**
     * Class that will listen ViewPager events and redraw our view when necessary.
     */
    private class PageListener
            implements ViewPager.OnPageChangeListener,
            ViewPager.OnAdapterChangeListener {

        @Override
        public void onAdapterChanged(PagerAdapter oldAdapter, PagerAdapter newAdapter) {
            pagerAdapter = newAdapter;
            invalidate();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.d(TAG, "positon : " + position + "; positionOffset : " + positionOffset);
            lastPosition = position;
            lastPositionOffset = positionOffset;
            invalidate();
        }

        @Override
        public void onPageSelected(int position) {
            invalidate();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

}
