/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math.ode.nonstiff;

import java.util.Arrays;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.MatrixVisitorException;
import org.apache.commons.math.linear.RealMatrixPreservingVisitor;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.CombinedEventsManager;
import org.apache.commons.math.ode.sampling.NordsieckStepInterpolator;
import org.apache.commons.math.ode.sampling.StepHandler;


/**
 * This class implements implicit Adams-Moulton integrators for Ordinary
 * Differential Equations.
 *
 * <p>Adams-Moulton methods (in fact due to Adams alone) are implicit
 * multistep ODE solvers. This implementation is a variation of the classical
 * one: it uses adaptive stepsize to implement error control, whereas
 * classical implementations are fixed step size. The value of state vector
 * at step n+1 is a simple combination of the value at step n and of the
 * derivatives at steps n+1, n, n-1 ... Since y'<sub>n+1</sub> is needed to
 * compute y<sub>n+1</sub>,another method must be used to compute a first
 * estimate of y<sub>n+1</sub>, then compute y'<sub>n+1</sub>, then compute
 * a final estimate of y<sub>n+1</sub> using the following formulas. Depending
 * on the number k of previous steps one wants to use for computing the next
 * value, different formulas are available for the final estimate:</p>
 * <ul>
 *   <li>k = 1: y<sub>n+1</sub> = y<sub>n</sub> + h y'<sub>n+1</sub></li>
 *   <li>k = 2: y<sub>n+1</sub> = y<sub>n</sub> + h (y'<sub>n+1</sub>+y'<sub>n</sub>)/2</li>
 *   <li>k = 3: y<sub>n+1</sub> = y<sub>n</sub> + h (5y'<sub>n+1</sub>+8y'<sub>n</sub>-y'<sub>n-1</sub>)/12</li>
 *   <li>k = 4: y<sub>n+1</sub> = y<sub>n</sub> + h (9y'<sub>n+1</sub>+19y'<sub>n</sub>-5y'<sub>n-1</sub>+y'<sub>n-2</sub>)/24</li>
 *   <li>...</li>
 * </ul>
 *
 * <p>A k-steps Adams-Moulton method is of order k+1.</p>
 *
 * <h3>Implementation details</h3>
 *
 * <p>We define scaled derivatives s<sub>i</sub>(n) at step n as:
 * <pre>
 * s<sub>1</sub>(n) = h y'<sub>n</sub> for first derivative
 * s<sub>2</sub>(n) = h<sup>2</sup>/2 y''<sub>n</sub> for second derivative
 * s<sub>3</sub>(n) = h<sup>3</sup>/6 y'''<sub>n</sub> for third derivative
 * ...
 * s<sub>k</sub>(n) = h<sup>k</sup>/k! y(k)<sub>n</sub> for k<sup>th</sup> derivative
 * </pre></p>
 *
 * <p>The definitions above use the classical representation with several previous first
 * derivatives. Lets define
 * <pre>
 *   q<sub>n</sub> = [ s<sub>1</sub>(n-1) s<sub>1</sub>(n-2) ... s<sub>1</sub>(n-(k-1)) ]<sup>T</sup>
 * </pre>
 * (we omit the k index in the notation for clarity). With these definitions,
 * Adams-Moulton methods can be written:
 * <ul>
 *   <li>k = 1: y<sub>n+1</sub> = y<sub>n</sub> + s<sub>1</sub>(n+1)</li>
 *   <li>k = 2: y<sub>n+1</sub> = y<sub>n</sub> + 1/2 s<sub>1</sub>(n+1) + [ 1/2 ] q<sub>n+1</sub></li>
 *   <li>k = 3: y<sub>n+1</sub> = y<sub>n</sub> + 5/12 s<sub>1</sub>(n+1) + [ 8/12 -1/12 ] q<sub>n+1</sub></li>
 *   <li>k = 4: y<sub>n+1</sub> = y<sub>n</sub> + 9/24 s<sub>1</sub>(n+1) + [ 19/24 -5/24 1/24 ] q<sub>n+1</sub></li>
 *   <li>...</li>
 * </ul></p>
 *
 * <p>Instead of using the classical representation with first derivatives only (y<sub>n</sub>,
 * s<sub>1</sub>(n+1) and q<sub>n+1</sub>), our implementation uses the Nordsieck vector with
 * higher degrees scaled derivatives all taken at the same step (y<sub>n</sub>, s<sub>1</sub>(n)
 * and r<sub>n</sub>) where r<sub>n</sub> is defined as:
 * <pre>
 * r<sub>n</sub> = [ s<sub>2</sub>(n), s<sub>3</sub>(n) ... s<sub>k</sub>(n) ]<sup>T</sup>
 * </pre>
 * (here again we omit the k index in the notation for clarity)
 * </p>
 *
 * <p>Taylor series formulas show that for any index offset i, s<sub>1</sub>(n-i) can be
 * computed from s<sub>1</sub>(n), s<sub>2</sub>(n) ... s<sub>k</sub>(n), the formula being exact
 * for degree k polynomials.
 * <pre>
 * s<sub>1</sub>(n-i) = s<sub>1</sub>(n) + &sum;<sub>j</sub> j (-i)<sup>j-1</sup> s<sub>j</sub>(n)
 * </pre>
 * The previous formula can be used with several values for i to compute the transform between
 * classical representation and Nordsieck vector. The transform between r<sub>n</sub>
 * and q<sub>n</sub> resulting from the Taylor series formulas above is:
 * <pre>
 * q<sub>n</sub> = s<sub>1</sub>(n) u + P r<sub>n</sub>
 * </pre>
 * where u is the [ 1 1 ... 1 ]<sup>T</sup> vector and P is the (k-1)&times;(k-1) matrix built
 * with the j (-i)<sup>j-1</sup> terms:
 * <pre>
 *        [  -2   3   -4    5  ... ]
 *        [  -4  12  -32   80  ... ]
 *   P =  [  -6  27 -108  405  ... ]
 *        [  -8  48 -256 1280  ... ]
 *        [          ...           ]
 * </pre></p>
 *
 * <p>Using the Nordsieck vector has several advantages:
 * <ul>
 *   <li>it greatly simplifies step interpolation as the interpolator mainly applies
 *   Taylor series formulas,</li>
 *   <li>it simplifies step changes that occur when discrete events that truncate
 *   the step are triggered,</li>
 *   <li>it allows to extend the methods in order to support adaptive stepsize.</li>
 * </ul></p>
 *
 * <p>The predicted Nordsieck vector at step n+1 is computed from the Nordsieck vector at step
 * n as follows:
 * <ul>
 *   <li>Y<sub>n+1</sub> = y<sub>n</sub> + s<sub>1</sub>(n) + u<sup>T</sup> r<sub>n</sub></li>
 *   <li>S<sub>1</sub>(n+1) = h f(t<sub>n+1</sub>, Y<sub>n+1</sub>)</li>
 *   <li>R<sub>n+1</sub> = (s<sub>1</sub>(n) - S<sub>1</sub>(n+1)) P<sup>-1</sup> u + P<sup>-1</sup> A P r<sub>n</sub></li>
 * </ul>
 * where A is a rows shifting matrix (the lower left part is an identity matrix):
 * <pre>
 *        [ 0 0   ...  0 0 | 0 ]
 *        [ ---------------+---]
 *        [ 1 0   ...  0 0 | 0 ]
 *    A = [ 0 1   ...  0 0 | 0 ]
 *        [       ...      | 0 ]
 *        [ 0 0   ...  1 0 | 0 ]
 *        [ 0 0   ...  0 1 | 0 ]
 * </pre>
 * From this predicted vector, the corrected vector is computed as follows:
 * <ul>
 *   <li>y<sub>n+1</sub> = y<sub>n</sub> + S<sub>1</sub>(n+1) + [ -1 +1 -1 +1 ... &plusmn;1 ] r<sub>n+1</sub></li>
 *   <li>s<sub>1</sub>(n+1) = h f(t<sub>n+1</sub>, y<sub>n+1</sub>)</li>
 *   <li>r<sub>n+1</sub> = R<sub>n+1</sub> + (s<sub>1</sub>(n+1) - S<sub>1</sub>(n+1)) P<sup>-1</sup> u</li>
 * </ul>
 * where the upper case Y<sub>n+1</sub>, S<sub>1</sub>(n+1) and R<sub>n+1</sub> represent the
 * predicted states whereas the lower case y<sub>n+1</sub>, s<sub>n+1</sub> and r<sub>n+1</sub>
 * represent the corrected states.</p>
 *
 * <p>The P<sup>-1</sup>u vector and the P<sup>-1</sup> A P matrix do not depend on the state,
 * they only depend on k and therefore are precomputed once for all.</p>
 *
 * @version $Revision$ $Date$
 * @since 2.0
 */
public class AdamsMoultonIntegrator extends AdamsIntegrator {

    /**
     * Build an Adams-Moulton integrator with the given order and error control parameters.
     * @param nSteps number of steps of the method excluding the one being computed
     * @param minStep minimal step (must be positive even for backward
     * integration), the last step can be smaller than this
     * @param maxStep maximal step (must be positive even for backward
     * integration)
     * @param scalAbsoluteTolerance allowed absolute error
     * @param scalRelativeTolerance allowed relative error
     * @exception IllegalArgumentException if order is 1 or less
     */
    public AdamsMoultonIntegrator(final int nSteps,
                                  final double minStep, final double maxStep,
                                  final double scalAbsoluteTolerance,
                                  final double scalRelativeTolerance)
        throws IllegalArgumentException {
        super("Adams-Moulton", nSteps, nSteps + 1, minStep, maxStep,
              scalAbsoluteTolerance, scalRelativeTolerance);
    }

    /**
     * Build an Adams-Moulton integrator with the given order and error control parameters.
     * @param nSteps number of steps of the method excluding the one being computed
     * @param minStep minimal step (must be positive even for backward
     * integration), the last step can be smaller than this
     * @param maxStep maximal step (must be positive even for backward
     * integration)
     * @param vecAbsoluteTolerance allowed absolute error
     * @param vecRelativeTolerance allowed relative error
     * @exception IllegalArgumentException if order is 1 or less
     */
    public AdamsMoultonIntegrator(final int nSteps,
                                  final double minStep, final double maxStep,
                                  final double[] vecAbsoluteTolerance,
                                  final double[] vecRelativeTolerance)
        throws IllegalArgumentException {
        super("Adams-Moulton", nSteps, nSteps + 1, minStep, maxStep,
              vecAbsoluteTolerance, vecRelativeTolerance);
    }


    /** {@inheritDoc} */
    @Override
    public double integrate(final FirstOrderDifferentialEquations equations,
                            final double t0, final double[] y0,
                            final double t, final double[] y)
        throws DerivativeException, IntegratorException {

        final int n = y0.length;
        sanityChecks(equations, t0, y0, t, y);
        setEquations(equations);
        resetEvaluations();
        final boolean forward = t > t0;

        // initialize working arrays
        if (y != y0) {
            System.arraycopy(y0, 0, y, 0, n);
        }
        final double[] yDot = new double[y0.length];
        final double[] yTmp = new double[y0.length];

        // set up two interpolators sharing the integrator arrays
        final NordsieckStepInterpolator interpolator = new NordsieckStepInterpolator();
        interpolator.reinitialize(y, forward);
        final NordsieckStepInterpolator interpolatorTmp = new NordsieckStepInterpolator();
        interpolatorTmp.reinitialize(yTmp, forward);

        // set up integration control objects
        for (StepHandler handler : stepHandlers) {
            handler.reset();
        }
        CombinedEventsManager manager = addEndTimeChecker(t0, t, eventsHandlersManager);


        // compute the initial Nordsieck vector using the configured starter integrator
        start(t0, y, t);
        interpolator.reinitialize(stepStart, stepSize, scaled, nordsieck);
        interpolator.storeTime(stepStart);

        double hNew = stepSize;
        interpolator.rescale(hNew);

        boolean lastStep = false;
        while (!lastStep) {

            // shift all data
            interpolator.shift();

            double error = 0;
            for (boolean loop = true; loop;) {

                stepSize = hNew;

                // predict a first estimate of the state at step end (P in the PECE sequence)
                final double stepEnd = stepStart + stepSize;
                interpolator.setInterpolatedTime(stepEnd);
                System.arraycopy(interpolator.getInterpolatedState(), 0, yTmp, 0, y0.length);

                // evaluate a first estimate of the derivative (first E in the PECE sequence)
                computeDerivatives(stepEnd, yTmp, yDot);

                // update Nordsieck vector
                final double[] predictedScaled = new double[y0.length];
                for (int j = 0; j < y0.length; ++j) {
                    predictedScaled[j] = stepSize * yDot[j];
                }
                final Array2DRowRealMatrix nordsieckTmp = updateHighOrderDerivativesPhase1(nordsieck);
                updateHighOrderDerivativesPhase2(scaled, predictedScaled, nordsieckTmp);

                // apply correction (C in the PECE sequence)
                error = nordsieckTmp.walkInOptimizedOrder(new Corrector(y, predictedScaled, yTmp));

                if (error <= 1.0) {

                    // evaluate a final estimate of the derivative (second E in the PECE sequence)
                    computeDerivatives(stepEnd, yTmp, yDot);

                    // update Nordsieck vector
                    final double[] correctedScaled = new double[y0.length];
                    for (int j = 0; j < y0.length; ++j) {
                        correctedScaled[j] = stepSize * yDot[j];
                    }
                    updateHighOrderDerivativesPhase2(predictedScaled, correctedScaled, nordsieckTmp);

                    // discrete events handling
                    interpolatorTmp.reinitialize(stepEnd, stepSize, correctedScaled, nordsieckTmp);
                    interpolatorTmp.storeTime(stepStart);
                    interpolatorTmp.shift();
                    interpolatorTmp.storeTime(stepEnd);
                    if (manager.evaluateStep(interpolatorTmp)) {
                        final double dt = manager.getEventTime() - stepStart;
                        if (Math.abs(dt) <= Math.ulp(stepStart)) {
                            // rejecting the step would lead to a too small next step, we accept it
                            loop = false;
                        } else {
                            // reject the step to match exactly the next switch time
                            hNew = dt;
                            interpolator.rescale(hNew);
                        }
                    } else {
                        // accept the step
                        scaled    = correctedScaled;
                        nordsieck = nordsieckTmp;
                        interpolator.reinitialize(stepEnd, stepSize, scaled, nordsieck);
                        loop = false;
                    }

                } else {
                    // reject the step and attempt to reduce error by stepsize control
                    final double factor = computeStepGrowShrinkFactor(error);
                    hNew = filterStep(stepSize * factor, forward, false);
                    interpolator.rescale(hNew);
                }

            }

            // the step has been accepted (may have been truncated)
            final double nextStep = stepStart + stepSize;
            System.arraycopy(yTmp, 0, y, 0, n);
            interpolator.storeTime(nextStep);
            manager.stepAccepted(nextStep, y);
            lastStep = manager.stop();

            // provide the step data to the step handler
            for (StepHandler handler : stepHandlers) {
                interpolator.setInterpolatedTime(nextStep);
                handler.handleStep(interpolator, lastStep);
            }
            stepStart = nextStep;

            if (!lastStep && manager.reset(stepStart, y)) {

                // some events handler has triggered changes that
                // invalidate the derivatives, we need to restart from scratch
                start(stepStart, y, t);
                interpolator.reinitialize(stepStart, stepSize, scaled, nordsieck);

            }

            if (! lastStep) {
                // in some rare cases we may get here with stepSize = 0, for example
                // when an event occurs at integration start, reducing the first step
                // to zero; we have to reset the step to some safe non zero value
                stepSize = filterStep(stepSize, forward, true);

                // stepsize control for next step
                final double  factor     = computeStepGrowShrinkFactor(error);
                final double  scaledH    = stepSize * factor;
                final double  nextT      = stepStart + scaledH;
                final boolean nextIsLast = forward ? (nextT >= t) : (nextT <= t);
                hNew = filterStep(scaledH, forward, nextIsLast);
                interpolator.rescale(hNew);
            }

        }

        final double stopTime  = stepStart;
        stepStart = Double.NaN;
        stepSize  = Double.NaN;
        return stopTime;

    }

    /** Corrector for current state in Adams-Moulton method.
     * <p>
     * This visitor implements the Taylor series formula:
     * <pre>
     * Y<sub>n+1</sub> = y<sub>n</sub> + s<sub>1</sub>(n+1) + [ -1 +1 -1 +1 ... &plusmn;1 ] r<sub>n+1</sub>
     * </pre>
     * </p>
     */
    private class Corrector implements RealMatrixPreservingVisitor {

        /** Previous state. */
        private final double[] previous;

        /** Current scaled first derivative. */
        private final double[] scaled;

        /** Current state before correction. */
        private final double[] before;

        /** Current state after correction. */
        private final double[] after;

        /** Simple constructor.
         * @param previous previous state
         * @param scaled current scaled first derivative
         * @param state state to correct (will be overwritten after visit)
         */
        public Corrector(final double[] previous, final double[] scaled, final double[] state) {
            this.previous = previous;
            this.scaled   = scaled;
            this.after    = state;
            this.before   = state.clone();
        }

        /** {@inheritDoc} */
        public void start(int rows, int columns,
                          int startRow, int endRow, int startColumn, int endColumn) {
            Arrays.fill(after, 0.0);
        }

        /** {@inheritDoc} */
        public void visit(int row, int column, double value)
            throws MatrixVisitorException {
            if ((row & 0x1) == 0) {
                after[column] -= value;
            } else {
                after[column] += value;
            }
        }

        /**
         * End visiting te Nordsieck vector.
         * <p>The correction is used to control stepsize. So its amplitude is
         * considered to be an error, which must be normalized according to
         * error control settings. If the normalized value is greater than 1,
         * the correction was too large and the step must be rejected.</p>
         * @return the normalized correction, if greater than 1, the step
         * must be rejected
         */
        public double end() {

            double error = 0;
            for (int i = 0; i < after.length; ++i) {
                after[i] += previous[i] + scaled[i];
                final double yScale = Math.max(Math.abs(previous[i]), Math.abs(after[i]));
                final double tol = (vecAbsoluteTolerance == null) ?
                                   (scalAbsoluteTolerance + scalRelativeTolerance * yScale) :
                                   (vecAbsoluteTolerance[i] + vecRelativeTolerance[i] * yScale);
                final double ratio  = (after[i] - before[i]) / tol;
                error += ratio * ratio;
            }

            return Math.sqrt(error / after.length);

        }
    }

}
