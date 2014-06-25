package android.support.v4.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;

import fr.cvlaminck.notidroid.android.base.R;

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
     * Color of a step/substep when the user has not reached it. This color
     * will to the stepColor when the user is navigating to this step/substep.
     */
    private int stepBackgroundColor = 0;

    /**
     * Color of a step/substep when this is the current step or this step has been completed
     * by the user.
     */
    private int stepColor = 0;

    /**
     * Diameter of the circle representing a step in the tunnel.
     * Value in px.
     */
    private int stepDiameter = 150;

    /**
     * Diameter of the circle representing a substep in the tunnel.
     * Value in px.
     */
    private int subStepDiameter = 5;

    /**
     * Color of the number of a step. Only step are numbered and this number
     * is displayed inside the circle using this color.
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

    /**
     * Paint that will be used to draw step shape background on the canvas
     */
    final Paint stepBackgroundPaint = new Paint();

    /**
     * Paint that will be used to draw step shape foreground on the canvas
     */
    final Paint stepPaint = new Paint();

    /**
     * Paint that will be used to draw the step number over the step shape.
     */
    final TextPaint textPaint = new TextPaint();


    public PagerTunnelStepsStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        computeAttributeSet(attrs);
        pageListener = new PageListener();
    }

    public PagerTunnelStepsStrip(Context context) {
        super(context);
        computeAttributeSet(null);
        pageListener = new PageListener();
    }

    private void computeAttributeSet(AttributeSet attrs) {
        //We read the attributes using the styleable we have created for this view.
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.PagerTunnelStepsStrip);
        setStepBackgroundColor(stepBackgroundColor = attributes.getColor(R.styleable.PagerTunnelStepsStrip_backgroundStepColor, Color.LTGRAY));
        setStepColor(attributes.getColor(R.styleable.PagerTunnelStepsStrip_stepColor, Color.GREEN));
        stepDiameter = attributes.getDimensionPixelSize(R.styleable.PagerTunnelStepsStrip_stepDiameter, 50); //TODO ; put the default value in dp
        subStepDiameter = attributes.getDimensionPixelSize(R.styleable.PagerTunnelStepsStrip_subStepDiameter, stepDiameter / 2);
        setTextSize(attributes.getDimensionPixelSize(R.styleable.PagerTunnelStepsStrip_android_textSize, (int) (0.8 * stepDiameter)));
        setTextColor(attributes.getColor(R.styleable.PagerTunnelStepsStrip_android_textColor, Color.WHITE));
        attributes.recycle();
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
            final int height = stepDiameter + getPaddingTop() + getPaddingBottom();
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

        //Number that will be displayed for the next step that will be drawn
        int nextDrawnStepNumber = 1;

        //Then we iterate on pages in the pagerAdapter to know if we should draw
        //a step or a substep. The title of the page will determine this.
        for (int pagePosition = 0; pagePosition < pagerAdapter.getCount(); pagePosition++) {
            //Radius of the dot depending on the page being a step or a substep
            final float circleRadius;
            //X-coordinates of the center of the shape representing the step/substep
            final float shapeCenterX = (pagePosition + 1) * (getWidth() / (pagerAdapter.getCount() + 1))
                    + getPaddingTop();
            //Y-coordinates of the center of the shape representing the step/substep
            final float shapeCenterY = getHeight() / 2.0f + getPaddingLeft();

            //If we have passed this step, we use the foreground color otherwise we use the background color.
            //When the user is dragging the page, we make a color transition effect using alpha.
            if (pagePosition <= lastPosition) {
                stepPaint.setAlpha(255);
                stepBackgroundPaint.setAlpha(0);
            } else if (pagePosition == (lastPosition + 1) && lastPositionOffset != 0.0f) {
                stepPaint.setAlpha((int) (lastPositionOffset * 255));
                stepBackgroundPaint.setAlpha((int) (255 - (lastPositionOffset * 255)));
            } else {
                stepBackgroundPaint.setAlpha(255);
                stepPaint.setAlpha(0);
            }

            if (STEP_TITLE.equals(pagerAdapter.getPageTitle(pagePosition))) {
                circleRadius = stepDiameter / 2.0f;
                //We draw the shape for this step
                drawStepShapeOnCanvas(canvas, shapeCenterX, shapeCenterY, circleRadius);
                //And we draw the step number over
                drawStepNumberOnCanvas(canvas, shapeCenterX, shapeCenterY, nextDrawnStepNumber);
                nextDrawnStepNumber++;
            } else if (SUBSTEP_TITLE.equals(pagerAdapter.getPageTitle(pagePosition))) {
                circleRadius = subStepDiameter / 2.0f;
                //We draw the shape for this substep
                drawStepShapeOnCanvas(canvas, shapeCenterX, shapeCenterY, circleRadius);
            } else
                throw new IllegalStateException(pagerAdapter.getPageTitle(pagePosition)
                        + " is not recognized as a step or substep title. Please use STEP_TITLE and SUBSTEP_TITLE as title for your page.");
        }
    }

    /**
     * Draw the step/substep shape on the canvas centered on the provided position.
     *
     * @param canvas       Canvas where the shape will be drawn
     * @param shapeCenterX X-Coordinates of the center of the shape
     * @param shapeCenterY Y-Coordinates of the center of the shape
     * @param circleRadius Size of the shape in px.
     */
    private void drawStepShapeOnCanvas(Canvas canvas, float shapeCenterX, float shapeCenterY, float circleRadius) {
        //Finally, we draw our step/substep on our canvas
        final RectF stepShapeRect = new RectF(shapeCenterX - circleRadius, shapeCenterY - circleRadius,
                shapeCenterX + circleRadius, shapeCenterY + circleRadius);
        //We draw the background dot only if its alpha is greater than 0
        if (stepBackgroundPaint.getAlpha() > 0)
            canvas.drawOval(stepShapeRect, stepBackgroundPaint);
        if (stepPaint.getAlpha() > 0)
            canvas.drawOval(stepShapeRect, stepPaint);
    }

    private void drawStepNumberOnCanvas(Canvas canvas, float shapeCenterX, float shapeCenterY, int stepNumber) {
        final String text = Integer.toString(stepNumber);
        //First we need to measure the width of our text
        final float textWidth = textPaint.measureText(text.toCharArray(), 0, text.length());

        //Then we draw the number
        //When working with font, you must use the descent() and ascent() functions to make precise measurement
        canvas.drawText(text, shapeCenterX - (textWidth / 2),
                shapeCenterY - ((textPaint.descent() + textPaint.ascent()) / 2),
                textPaint);
    }

    public int getStepColor() {
        return stepColor;
    }

    public void setStepColor(int stepColor) {
        this.stepColor = stepColor;
        this.stepPaint.setColor(stepColor);
    }

    public int getStepBackgroundColor() {
        return stepBackgroundColor;
    }

    public void setStepBackgroundColor(int stepBackgroundColor) {
        this.stepBackgroundColor = stepBackgroundColor;
        this.stepBackgroundPaint.setColor(stepBackgroundColor);
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        this.textPaint.setColor(textColor);
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        this.textPaint.setTextSize(textSize);
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
