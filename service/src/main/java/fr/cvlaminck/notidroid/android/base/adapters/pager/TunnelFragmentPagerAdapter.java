package fr.cvlaminck.notidroid.android.base.adapters.pager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTunnelStepsStrip;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * FragmentAdapter that should be used for ViewPager with PagerTunnelStepsStrip to
 * display a real login/sign/payment tunnel with well defined steps.
 * <p/>
 * Since it inherits from FragmentStatePagerAdapter, the state of the tunnel will be kept
 * during activity life cycle.
 */
public class TunnelFragmentPagerAdapter
        extends FragmentStatePagerAdapter {

    /**
     * List of all steps in the tunnel.
     */
    private List<InternalTunnelStep> internalTunnelSteps = null;

    public TunnelFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        internalTunnelSteps = new ArrayList<>();
    }

    /**
     * Add a new step in the tunnel.
     * <p/>
     * /!\ Only use AA enhanced fragment with this adapter.
     *
     * @param stepClass Enhanced class of the fragment displaying the step to the user.
     */
    public void addStep(@NotNull Class<? extends Fragment> stepClass) {
        if (stepClass == null)
            throw new IllegalArgumentException("stepClass must not be null");
        final InternalTunnelStep internalTunnelStep = new InternalTunnelStep(stepClass);
        internalTunnelSteps.add(internalTunnelStep);
    }

    /**
     * Add a new substep in the tunnel.
     * <p/>
     * /!\ Only use AA enhanced fragment with this adapter.
     *
     * @param stepClass Enhanced class of the fragment displaying the step to the user.
     * @throws java.lang.IllegalStateException Thrown if you do not have added a step before this first substep.
     */
    public void addSubStep(@NotNull Class<? extends Fragment> stepClass) {
        if (stepClass == null)
            throw new IllegalArgumentException("stepClass must not be null");
        if (internalTunnelSteps.size() == 0)
            throw new IllegalStateException("You must add a step before adding a substep to the tunnel.");
        final InternalTunnelStep internalTunnelStep = new InternalTunnelStep(true, stepClass);
        internalTunnelSteps.add(internalTunnelStep);
    }

    @Override
    public Fragment getItem(int position) {
        return internalTunnelSteps.get(position).newInstance();
    }

    @Override
    public int getCount() {
        return internalTunnelSteps.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return internalTunnelSteps.get(position).getTitle();
    }

    /**
     * Representation of a step/substep in the tunnel.
     */
    private class InternalTunnelStep {

        /**
         * Is this a full step or a sub step.
         */
        private boolean isSubStep;

        /**
         * Class of the Fragment representing this step/substep
         */
        private Class<? extends Fragment> stepClass;

        private InternalTunnelStep(Class<? extends Fragment> stepClass) {
            this(false, stepClass);
        }

        private InternalTunnelStep(boolean isSubStep, Class<? extends Fragment> stepClass) {
            this.isSubStep = isSubStep;
            this.stepClass = stepClass;
        }

        /**
         * Return the right title so the page will be recognized as a step/substep
         * by the PagerTunnelStepsStrip component.
         */
        public String getTitle() {
            return isSubStep ? PagerTunnelStepsStrip.SUBSTEP_TITLE : PagerTunnelStepsStrip.STEP_TITLE;
        }

        /**
         * Instantiate the Fragment using its builder.
         * <p/>
         * Throws an InternalStateException if the fragment class cannot be instantiated.
         */
        public Fragment newInstance() {
            try {
                //First, we need to find the builder method and call it to get an instance of the builder
                final Method builderMethod = stepClass.getDeclaredMethod("builder", new Class<?>[]{});
                final Class<?> builderClass = builderMethod.getReturnType();
                final Object builderInstance = builderMethod.invoke(stepClass);
                //Then we can build our fragment
                final Method buildMethod = builderClass.getMethod("build", new Class<?>[]{});
                return (Fragment) buildMethod.invoke(builderInstance);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new IllegalStateException("Cannot instantiate fragment '" + stepClass.getSimpleName() + "'", e);
            }
        }

    }

}
