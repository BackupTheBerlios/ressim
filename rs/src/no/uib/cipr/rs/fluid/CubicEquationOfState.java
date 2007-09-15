package no.uib.cipr.rs.fluid;

import static no.uib.cipr.rs.util.Constants.R;
import no.uib.cipr.matrix.DenseLU;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.CubicEquation;

/**
 * Cubic equation of state for an oil/gas mixture
 */
public class CubicEquationOfState extends WaterEquationOfState {

    /**
     * Variants of the equation of state
     */
    private enum EOS {

        /**
         * Soave-Redlich-Kwong
         */
        SRK(-1, 0, 0.42748, 0.08664) {
            @Override
            public double m(double omega) {
                return 0.48508 + 1.55171 * omega - 0.15613 * omega * omega;
            }

            @Override
            public String toString() {
                return "Soave-Redlich-Kwong";
            }
        },

        /**
         * Peng-Robinson
         */
        PR(-(1 + Math.sqrt(2)), -(1 - Math.sqrt(2)), 0.457235529, 0.077796074) {
            @Override
            public double m(double omega) {
                if (omega > 0.49)
                    return 0.379642 + 1.48503 * omega - 0.164423 * omega
                            * omega + 0.016666 * omega * omega * omega;
                else
                    return 0.37464 + 1.53226 * omega - 0.26992 * omega * omega;
            }

            @Override
            public String toString() {
                return "Peng-Robinson";
            }
        };

        public final double delta1, delta2;

        public final double OmegaA, OmegaB;

        private EOS(double delta1, double delta2, double OmegaA, double OmegaB) {
            this.delta1 = delta1;
            this.delta2 = delta2;
            this.OmegaA = OmegaA;
            this.OmegaB = OmegaB;
        }

        public abstract double m(double omega);

        public static EOS getEOS(String name) {
            if (name.equalsIgnoreCase("SRK"))
                return SRK;
            else if (name.equalsIgnoreCase("PR"))
                return PR;
            else
                throw new IllegalArgumentException(
                        "Unknown Equation-Of-State: " + name);
        }
    }

    /**
     * Components database
     */
    final Components components;

    /**
     * Equation of state
     */
    final EOS eos;

    /**
     * Parameters for all the components
     */
    final ComponentParameters componentParameters;

    /**
     * Parameters for the oil and gas phases
     */
    final PhaseParameters oilParameters, gasParameters;

    /**
     * Flash algorithm
     */
    private final Flash flash;

    /**
     * Viscosity calculation
     */
    private final HydrocarbonViscosity viscosity;

    /**
     * Binary interaction parameters
     */
    double[][] dij;

    /**
     * Volume shift parameters for each component [-]
     */
    double[] s;

    /**
     * Sets up the cubic equation of state with all associated methods
     */
    public CubicEquationOfState(Configuration config, Components components) {
        super(config, components);

        this.components = components;

        /*
         * Type of cubic equation of state
         */

        eos = EOS.getEOS(config.getString("EOS", "PR"));
        System.out.println(config.trace() + "Using " + eos
                + " cubic equation of state");

        componentParameters = new ComponentParameters();
        oilParameters = new PhaseParameters(Phase.OIL);
        gasParameters = new PhaseParameters(Phase.GAS);

        /*
         * Flash algorithm
         */

        flash = new Flash(config);

        /*
         * Read EOS specific component data
         */

        readComponentData(config);

        /*
         * Hydrocarbon viscosity calculation
         */

        viscosity = new HydrocarbonViscosity(config);
    }

    /**
     * Reads in the binary interaction coefficients and volume shifts
     */
    private void readComponentData(Configuration config) {
        int num = components.numComponents();
        s = new double[num];
        dij = new double[num][num];

        Configuration data = config.getConfiguration("ComponentData");
        for (Component nu : components.hc()) {

            int i = nu.index();

            Configuration sub = data.getConfiguration(nu.name());

            // Volume shift parameter
            s[i] = sub.getDouble("s", 0);

            Configuration binary = sub.getConfiguration("Binary");
            for (Component mu : components.hc()) {

                int j = mu.index();

                // Symmetrical binary interaction coefficients
                dij[i][j] = dij[j][i] = binary.getDouble(mu.name(), 0);
            }
        }
    }

    @Override
    public void calculatePhaseState(double p, Composition N, double T,
            PhaseData<EquationOfStateData> data) {

        /*
         * Calculate the properties of the water phase
         */

        super.calculatePhaseState(p, N, T, data);

        /*
         * Split the hydrocarbons into an oil and a gas phase
         */

        EquationOfStateData oilEosData = data.oil;
        EquationOfStateData gasEosData = data.gas;

        Composition oil = oilEosData.getComposition();
        Composition gas = gasEosData.getComposition();

        componentParameters.update(T);

        flash.flash(p, T, N, oil, gas);

        // Due to round-offs, globally non-present components may appear in the
        // oil and gas phases
        for (Component nu : components.hc())
            if (N.getMoles(nu) == 0) {
                oil.setMoles(nu, 0);
                gas.setMoles(nu, 0);
            }

        /*
         * Calculate volumes and derivatives
         */

        oilEosData.setPresent(oil.getMoles() > 0);
        gasEosData.setPresent(gas.getMoles() > 0);

        boolean oilPresent = oilEosData.isPresent();
        boolean gasPresent = gasEosData.isPresent();

        // Volumes and densities
        if (oilPresent)
            calculatePhaseProperties(p, T, oilParameters, oilEosData);
        if (gasPresent)
            calculatePhaseProperties(p, T, gasParameters, gasEosData);

        // The derivatives
        if (oilPresent && gasPresent)
            calculateVolumeDerivativesTwoPhase(p, T, N, oilEosData, gasEosData);
        else if (oilPresent)
            calculateVolumeDerivativesSinglePhase(p, T, oilParameters,
                    oilEosData);
        else if (gasPresent)
            calculateVolumeDerivativesSinglePhase(p, T, gasParameters,
                    gasEosData);

        /*
         * Calculate hydrocarbon viscosities
         */

        if (oilPresent) {
            double xi_oil = oilEosData.getMolarDensity();
            double mu_oil = viscosity.calculateViscosity(T, oil, xi_oil);
            oilEosData.setViscosity(mu_oil);
        }

        if (gasPresent) {
            double xi_gas = gasEosData.getMolarDensity();
            double mu_gas = viscosity.calculateViscosity(T, gas, xi_gas);
            gasEosData.setViscosity(mu_gas);
        }

        /*
         * Calculate hydrocarbon enthalpies
         */

        // TODO
    }

    /**
     * Calculates phase properties and derivatives after a phase-split
     */
    private void calculatePhaseProperties(double p, double T,
            PhaseParameters phaseParameters, EquationOfStateData phaseEosData) {
        final double RT = R * T;
        double Z = phaseParameters.Z;
        double V = Z * phaseEosData.getComposition().getMoles() * RT / p;
        double xi = p / (Z * RT);

        phaseEosData.setVolume(V);
        phaseEosData.setMolarDensity(xi);
    }

    /**
     * Calculates volume derivatives for a single hydrocarbon phase system
     */
    private void calculateVolumeDerivativesSinglePhase(double p, double T,
            PhaseParameters phaseParameters, EquationOfStateData phaseEosData) {
        double V = phaseEosData.getVolume();
        double Z = phaseParameters.Z;

        double dZdp = phaseParameters.dZdp;
        double dZdT = phaseParameters.dZdT;

        phaseEosData.setdVdp((V / Z) * dZdp - V / p);
        phaseEosData.setdVdT((V / Z) * dZdT + V / T);

        Composition N = phaseEosData.getComposition();
        double Nl = N.getMoles();
        for (Component nu : components.hc()) {
            int i = nu.index();

            double dZdN = phaseParameters.dZdN[i];

            phaseEosData.setdVdN(nu, (V / Z) * dZdN + V / Nl);
        }
    }

    /**
     * Calculates volume derivatives for a two phase hydrocarbon system
     */
    private void calculateVolumeDerivativesTwoPhase(double p, double T,
            Composition N, EquationOfStateData oilEosData,
            EquationOfStateData gasEosData) {

        /*
         * Calculate the molar mass derivatives of the oil phase
         */

        flash.calculateMolarMassDerivatives(N);

        /*
         * Set the volume derivatives
         */

        double Vo = oilEosData.getVolume();
        double Vg = gasEosData.getVolume();

        double Zo = oilParameters.Z;
        double Zg = gasParameters.Z;

        double dZodp = oilParameters.dZdp;
        double dZgdp = gasParameters.dZdp;

        double dZodT = oilParameters.dZdT;
        double dZgdT = gasParameters.dZdT;

        double dNodp = 0, dNodT = 0;
        for (Component nu : components.hc()) {
            int i = nu.index();

            dNodp += flash.dNodp[i];
            dNodT += flash.dNodT[i];

            dZodp += oilParameters.dZdN[i] * flash.dNodp[i];
            dZgdp -= gasParameters.dZdN[i] * flash.dNodp[i];

            dZodT += oilParameters.dZdN[i] * flash.dNodT[i];
            dZgdT -= gasParameters.dZdN[i] * flash.dNodT[i];
        }
        double dNgdp = -dNodp, dNgdT = -dNodT;

        double No = oilEosData.getComposition().getMoles();
        double Ng = gasEosData.getComposition().getMoles();

        oilEosData.setdVdp((Vo / Zo) * dZodp + (Vo / No) * dNodp - Vo / p);
        gasEosData.setdVdp((Vg / Zg) * dZgdp + (Vg / Ng) * dNgdp - Vg / p);

        oilEosData.setdVdT((Vo / Zo) * dZodT + (Vo / No) * dNodT + Vo / T);
        gasEosData.setdVdT((Vg / Zg) * dZgdT + (Vg / Ng) * dNgdT + Vg / T);

        for (Component nu : components.hc()) {
            int i = nu.index();

            double dVodNi = 0, dVgdNi = 0;
            for (Component mu : components.hc()) {
                int j = mu.index();

                if (N.getMoles(mu) == 0)
                    continue;

                double dNodNi = flash.dNodN[j][i];
                double dNgdNi = -dNodNi;
                if (i == j)
                    dNgdNi++;

                double dZodN = oilParameters.dZdN[j];
                double dZgdN = gasParameters.dZdN[j];

                dVodNi += ((Vo / Zo) * dZodN + Vo / No) * dNodNi;
                dVgdNi += ((Vg / Zg) * dZgdN + Vg / Ng) * dNgdNi;
            }

            oilEosData.setdVdN(nu, dVodNi);
            gasEosData.setdVdN(nu, dVgdNi);
        }
    }

    /**
     * Parameters for all the components
     */
    private class ComponentParameters {

        final double[] mi, ai, bi, daidT;

        final double[][] aij, daijdT;

        public ComponentParameters() {
            int num = components.numComponents();

            mi = new double[num];
            ai = new double[num];
            bi = new double[num];
            daidT = new double[num];

            aij = new double[num][num];
            daijdT = new double[num][num];

            for (Component nu : components.hc()) {
                int i = nu.index();

                double Tci = nu.getCriticalTemperature();
                double pci = nu.getCriticalPressure();
                double omegai = nu.getAcentricFactor();

                mi[i] = eos.m(omegai);
                bi[i] = eos.OmegaB * R * Tci / pci;
            }
        }

        public void update(double T) {
            for (Component nu : components.hc()) {
                int i = nu.index();

                double Tci = nu.getCriticalTemperature();
                double pci = nu.getCriticalPressure();

                double Tr = T / Tci;

                double alpha = 1 + mi[i] * (1 - Math.sqrt(Tr));
                double dalphadT = -(mi[i] / 2) * (1 / Math.sqrt(Tci * T));
                ai[i] = eos.OmegaA * R * R * Tci * Tci * alpha * alpha / pci;
                daidT[i] = 2 * (ai[i] / alpha) * dalphadT;
            }

            for (Component nu : components.hc()) {
                int i = nu.index();

                for (Component mu : components.hc()) {
                    int j = mu.index();

                    double sqr_ai_aj = Math.sqrt(ai[i] * ai[j]);
                    aij[i][j] = (1 - dij[i][j]) * sqr_ai_aj;
                    daijdT[i][j] = (1 - dij[i][j])
                            * (ai[j] * daidT[i] + ai[i] * daidT[j])
                            / (2 * sqr_ai_aj);
                }
            }
        }
    }

    /**
     * Parameters for a hydrocarbon phase
     */
    private class PhaseParameters {

        private final Phase phase;

        private double a, b;

        private double[] dadN, dbdN;

        private double dadT;

        private double A, B;

        private double dAdp, dBdp;

        private double dAdT, dBdT;

        private double[] dAdN, dBdN;

        double Z;

        double dZdp, dZdT;

        double[] dZdN;

        double[] f;

        double[] phi;

        double[] dfdp, dfdT;

        double[][] dfdN;

        public PhaseParameters(Phase phase) {
            this.phase = phase;

            int num = components.numComponents();

            dadN = new double[num];
            dbdN = new double[num];

            dAdN = new double[num];
            dBdN = new double[num];

            dZdN = new double[num];

            f = new double[num];
            phi = new double[num];

            dfdp = new double[num];
            dfdT = new double[num];
            dfdN = new double[num][num];
        }

        /**
         * Calculates new phase parameters
         * 
         * @param p
         *                System pressure
         * @param T
         *                System temperature
         * @param N
         *                Phase composition
         */
        public void calculate(double p, double T, Composition N) {
            calculatePhaseParameters(p, T, N);

            calculatePhaseCompressibility();

            applyVolumeTranslation(p, T, N);

            calculateFugacities(p, N);
        }

        /**
         * Calculates phase parameters (a, b, A, B) and derivatives
         */
        private void calculatePhaseParameters(double p, double T, Composition N) {
            a = b = dadT = 0;

            for (Component nu : components.hc()) {
                int i = nu.index();

                double Ci = N.getMoleFraction(nu);

                b += Ci * componentParameters.bi[i];

                for (Component mu : components.hc()) {
                    int j = mu.index();

                    double Cj = N.getMoleFraction(mu);

                    a += Ci * Cj * componentParameters.aij[i][j];
                    dadT += Ci * Cj * componentParameters.daijdT[i][j];
                }
            }

            double Nl = N.getMoles();
            for (Component nu : components.hc()) {
                int i = nu.index();

                dbdN[i] = (componentParameters.bi[i] - b) / Nl;

                double sum = 0;
                for (Component mu : components.hc()) {
                    int j = mu.index();

                    sum += N.getMoleFraction(mu)
                            * componentParameters.aij[i][j];
                }

                dadN[i] = 2 * (sum - a) / Nl;
            }

            final double RT = R * T;
            A = p * a / (RT * RT);
            B = p * b / RT;

            dAdp = A / p;
            dBdp = B / p;

            dAdT = -2 * (A / T) + (A / a) * dadT;
            dBdT = -B / T;

            for (Component nu : components.hc()) {
                int i = nu.index();

                dAdN[i] = (A / a) * dadN[i];
                dBdN[i] = (B / b) * dbdN[i];
            }
        }

        /**
         * Calculates the Z-factor and derivatives
         */
        private void calculatePhaseCompressibility() {
            double a0 = -A * B - (B * B) * (1 + B) * eos.delta1 * eos.delta2;
            double a1 = A + B * (1 + B) * (eos.delta1 + eos.delta2)
                    + eos.delta1 * eos.delta2 * B * B;
            double a2 = -1 - B * (1 + eos.delta1 + eos.delta2);

            CubicEquation eqn = new CubicEquation(a0, a1, a2);

            if (phase == Phase.OIL) {
                // Get the smallest positive root
                Z = eqn.getSmallestRoot();
                if (Z < 0) {
                    Z = eqn.getMiddleRoot();
                    if (Z < 0)
                        Z = eqn.getLargestRoot();
                }
            } else if (phase == Phase.GAS)
                Z = eqn.getLargestRoot();

            // Z must be positive
            if (Z <= 0)
                throw new RuntimeException("Z = " + Z + " <= 0");

            dZdp = dZdX(a1, a2, dAdp, dBdp);
            dZdT = dZdX(a1, a2, dAdT, dBdT);
            for (Component nu : components.hc()) {
                int i = nu.index();
                dZdN[i] = dZdX(a1, a2, dAdN[i], dBdN[i]);
            }
        }

        /**
         * Applies a volume shift to the Z-factor and its derivatives
         */
        private void applyVolumeTranslation(double p, double T, Composition N) {
            final double RT = R * T;

            double Nl = N.getMoles();
            double shift = 0;
            for (Component nu : components.hc()) {
                int i = nu.index();
                double ci = s[i] * componentParameters.bi[i];

                shift += N.getMoleFraction(nu) * ci;
            }

            Z -= (p / RT) * shift;

            dZdp -= (1 / RT) * shift;
            dZdT -= (p / (RT * T)) * shift;

            for (Component nu : components.hc()) {
                int i = nu.index();
                double ci = s[i] * componentParameters.bi[i];

                dZdN[i] -= (p / RT) * (ci - shift) / Nl;
            }
        }

        /**
         * Calculates a derivative of Z
         */
        private double dZdX(double a1, double a2, double dAdX, double dBdX) {
            double da0dX = -dAdX * B - dBdX
                    * (A + B * (2 + 3 * B) * eos.delta1 * eos.delta2);
            double da1dX = dAdX
                    + dBdX
                    * ((1 + 2 * B) * (eos.delta1 + eos.delta2) + 2 * eos.delta1
                            * eos.delta2 * B);
            double da2dX = -dBdX * (1 + eos.delta1 + eos.delta2);

            return -(da2dX * Z * Z + da1dX * Z + da0dX)
                    / (3 * Z * Z + 2 * a2 * Z + a1);
        }

        /**
         * Calculates fugacities
         */
        private void calculateFugacities(double p, Composition N) {
            double Nl = N.getMoles();

            double w = (Z - eos.delta2 * B) / (Z - eos.delta1 * B);
            double lnw = Math.log(w);
            for (Component nu : components.hc()) {
                int i = nu.index();

                double alpha = 0;
                for (Component mu : components.hc()) {
                    int j = mu.index();

                    double Cj = N.getMoleFraction(mu);
                    alpha += Cj * componentParameters.aij[i][j];
                }
                alpha *= (2 / a);

                double beta = componentParameters.bi[i] / b;

                double y = Math.exp(beta * (Z - 1)) / (Z - B);
                double v = A * (alpha - beta) / ((eos.delta2 - eos.delta1) * B);
                phi[i] = y * Math.pow(w, v);

                double Ci = N.getMoleFraction(nu);
                f[i] = p * Ci * phi[i];

                double dalphadT = -(alpha / a) * dadT;
                for (Component mu : components.hc()) {
                    int j = mu.index();

                    double Cj = N.getMoleFraction(mu);
                    dalphadT += (2 / a) * Cj * componentParameters.daijdT[i][j];
                }

                double dphidp = dphidX(y, w, lnw, v, phi[i], alpha, beta, dAdp,
                        dBdp, dZdp, 0, 0);
                double dphidT = dphidX(y, w, lnw, v, phi[i], alpha, beta, dAdT,
                        dBdT, dZdT, dalphadT, 0);

                dfdp[i] = f[i] / p + (f[i] / phi[i]) * dphidp;
                dfdT[i] = (f[i] / phi[i]) * dphidT;

                for (Component mu : components.hc()) {
                    int j = mu.index();

                    double dalphadN = -(alpha / a) * dadN[j] + (2 / a)
                            * componentParameters.aij[i][j] / Nl - alpha / Nl;
                    double dbetadN = -(beta / b) * dbdN[j];

                    double dphidN = dphidX(y, w, lnw, v, phi[i], alpha, beta,
                            dAdN[j], dBdN[j], dZdN[j], dalphadN, dbetadN);

                    double delta = (i == j) ? 1 : 0;
                    double div = (Ci == 0) ? 1 : Ci;
                    dfdN[i][j] = (f[i] / div) * (delta - Ci) / Nl
                            + (f[i] / phi[i]) * dphidN;
                }
            }
        }

        /**
         * Derivative of the fugacity coefficient
         */
        private double dphidX(double y, double w, double lnw, double v,
                double phi, double alpha, double beta, double dAdX,
                double dBdX, double dZdX, double dalphadX, double dbetadX) {

            double dydX = (dbetadX * (Z - 1) + beta * dZdX) * y - (y / (Z - B))
                    * (dZdX - dBdX);
            double dvdX = (dAdX * (alpha - beta) + A * (dalphadX - dbetadX))
                    / ((eos.delta2 - eos.delta1) * B) - (v / B) * dBdX;
            double dwdX = (dZdX - eos.delta2 * dBdX - w
                    * (dZdX - eos.delta1 * dBdX))
                    / (Z - eos.delta1 * B);

            return dydX * (phi / y) + phi * (dvdX * lnw + (v / w) * dwdX);
        }
    }

    /**
     * Flash calculations for determining phase equilibrium and molar mass
     * derivatives. A combination of the reliable sucessive-substitution
     * approach and the fast Newton-Raphson approach is adopted
     */
    private class Flash {

        /**
         * Hydrocarbon fractions
         */
        private final double[] C;

        /**
         * K-values
         */
        private final double[] K;

        /**
         * Fugacity matrix and residual matrix. The fugacity matrix is used in
         * both the Newton-Raphson flash and molar mass calculations, while the
         * residual matrix is uses just for the molar mass calculations
         */
        private final DenseMatrix F, R;

        /**
         * Residual vector and solution vector for the Newton-Raphson flash
         */
        private final Vector r, x;

        /**
         * For solving Newton-Raphson flashes and calculating molar mass
         * derivatives
         */
        private final DenseLU lu;

        /**
         * Derivative of the oil molar mass with pressure [mol/Pa]
         */
        final double[] dNodp;

        /**
         * Derivative of the oil molar mass with temperature [mol/K]
         */
        final double[] dNodT;

        /**
         * Derivative of the oil molar mass with total moles [-]
         */
        final double[][] dNodN;

        /**
         * Switching criteria between sucessive substitution and Newton-Raphson
         * [Pa]
         */
        private final double criteria;

        /**
         * Convergence tolerance [Pa]
         */
        private final double tolerance;

        /**
         * Prints some debugging information
         */
        private final boolean verbose;

        /**
         * Allocates datastructures for the flash calculations
         */
        public Flash(Configuration config) {
            int num = components.numComponents();

            C = new double[num];
            K = new double[num];

            F = new DenseMatrix(num - 1, num - 1);
            R = new DenseMatrix(num - 1, num + 1); // pressure and temperature

            lu = new DenseLU(num - 1, num - 1);
            r = new DenseVector(num - 1);
            x = new DenseVector(num - 1);

            dNodp = new double[num];
            dNodT = new double[num];
            dNodN = new double[num][num];

            criteria = config.getDouble("FlashSwitchingCriteria", 1e+4);
            tolerance = config.getDouble("FlashTolerance", 1e-1);
            verbose = config.getBoolean("FlashVerbose", false);

            if (tolerance <= 0)
                throw new IllegalArgumentException(config.trace()
                        + "FlashTolerance must be positive");
        }

        public Flash(Flash flash) {
            int num = components.numComponents();

            C = new double[num];
            K = new double[num];

            F = new DenseMatrix(num - 1, num - 1);
            R = new DenseMatrix(num - 1, num + 1); // pressure and temperature

            lu = new DenseLU(num - 1, num - 1);
            r = new DenseVector(num - 1);
            x = new DenseVector(num - 1);

            dNodp = new double[num];
            dNodT = new double[num];
            dNodN = new double[num][num];

            criteria = flash.criteria;
            tolerance = flash.tolerance;
            verbose = flash.verbose;
        }

        /**
         * Splits the given global composition into an oil and a gas composition
         * 
         * @param N
         *                System composition [mol]
         * @param oil
         *                Resulting oil composition
         * @param gas
         *                Resulting gas composition
         */
        public void flash(double p, double T, Composition N, Composition oil,
                Composition gas) {
            setHydrocarbonFractions(N);

            double residual = Double.MAX_VALUE;
            double L = 0.5;

            // Use Wilson's K-value estimate initially, unless both phases are
            // already present, in which K-values are found from fugacities
            if (oil.getMoles() == 0 || gas.getMoles() == 0)
                calculateInitialK(p, T);
            else {
                oilParameters.calculate(p, T, oil);
                gasParameters.calculate(p, T, gas);

                setK();
                L = oil.getMoles() / (oil.getMoles() + gas.getMoles());
            }

            do {
                if (residual > criteria) {

                    /*
                     * Sucessive substitution
                     */

                    if (verbose)
                        System.err.format("%10e (%s)\n", residual, "SS");

                    // Find the liquid mole fraction using K
                    L = solveRachfordRice(L);

                    // Update the oil and gas compositions using L
                    setOilGasComposition(N, L, oil, gas);

                    // Calculate new fugacity coefficients
                    if (L > 0)
                        oilParameters.calculate(p, T, oil);
                    if (L < 1)
                        gasParameters.calculate(p, T, gas);

                    // Stop if in a single-phase region
                    if (L == 0) { // no liquid
                        oil.zero();
                        break;
                    } else if (L == 1) { // no vapor
                        gas.zero();
                        break;
                    }

                    // Update the K-values using fugacity coefficients
                    setK();

                } else {

                    /*
                     * Newton-Raphson
                     */

                    if (verbose)
                        System.err.format("%10e (%s)\n", residual, "NR");

                    // Perform fugacity linearisation
                    buildJacobian(N);

                    // In-place LU factorisation for dNo
                    lu.factor(F).solve(new DenseMatrix(x.set(-1, r), false));

                    // Find scaling factor to ensure positive mole numbers
                    double alpha = 1;
                    for (Component nu : components.hc()) {
                        int i = nu.index();

                        if (N.getMoles(nu) == 0)
                            continue;

                        double No = oil.getMoles(nu), Ng = gas.getMoles(nu);
                        double dNo = x.get(i - 1);
                        if (No + alpha * dNo < 0)
                            alpha = -No / dNo;
                        if (Ng - alpha * dNo < 0)
                            alpha = Ng / dNo;
                    }

                    // Update the compositions
                    for (Component nu : components.hc()) {
                        int i = nu.index();

                        if (N.getMoles(nu) == 0)
                            continue;

                        double dNo = alpha * x.get(i - 1);
                        oil.addMoles(nu, dNo);
                        gas.addMoles(nu, -dNo);
                    }

                    // Assume both oil and gas phases
                    oilParameters.calculate(p, T, oil);
                    gasParameters.calculate(p, T, gas);
                }

            } while ((residual = calculateResidual()) > tolerance);
        }

        /**
         * Calculates the fugacity residual
         */
        private double calculateResidual() {
            for (Component nu : components.hc()) {
                int i = nu.index();
                r.set(i - 1, oilParameters.f[i] - gasParameters.f[i]);
            }

            return r.norm(Vector.Norm.Infinity);
        }

        /**
         * Builds the Jacobian matrix for the Newton-Raphson flash
         */
        private void buildJacobian(Composition N) {
            F.zero();

            for (Component nu : components.hc()) {
                int i = nu.index();

                // Handle missing components
                double Ci = N.getMoleFraction(nu);
                if (Ci == 0) {
                    F.set(i - 1, i - 1, 1);
                    continue;
                }

                for (Component mu : components.hc()) {
                    int j = mu.index();

                    double dfodN = oilParameters.dfdN[i][j];
                    double dfgdN = gasParameters.dfdN[i][j];

                    F.set(i - 1, j - 1, dfodN + dfgdN);
                }
            }
        }

        /**
         * Calculates molar mass derivatives for a stable two-phase hydrocarbon
         * mixture
         * 
         * @param N
         *                Total composition. Used for checking which components
         *                are present
         */
        public void calculateMolarMassDerivatives(Composition N) {

            /*
             * Form linear system
             */

            R.zero();
            F.zero();

            // Pressure and temperature column indices
            int pi = components.numComponents() - 1;
            int Ti = pi + 1;

            for (Component nu : components.hc()) {
                int i = nu.index();

                // Handle missing components
                if (N.getMoles(nu) == 0) {
                    F.set(i - 1, i - 1, 1);
                    continue;
                }

                double dfodp = oilParameters.dfdp[i];
                double dfgdp = gasParameters.dfdp[i];

                double dfodT = oilParameters.dfdT[i];
                double dfgdT = gasParameters.dfdT[i];

                R.set(i - 1, pi, dfgdp - dfodp);
                R.set(i - 1, Ti, dfgdT - dfodT);

                for (Component mu : components.hc()) {
                    int j = mu.index();

                    if (N.getMoles(mu) == 0)
                        continue;

                    double dfodN = oilParameters.dfdN[i][j];
                    double dfgdN = gasParameters.dfdN[i][j];

                    F.set(i - 1, j - 1, dfodN + dfgdN);
                    R.set(i - 1, j - 1, dfgdN);
                }
            }

            /*
             * Calculate all the molar mass derivatives
             */

            // LU factorization and solve
            lu.factor(F).solve(R);

            // Extract the derivatives
            for (Component nu : components.hc()) {
                int i = nu.index();

                dNodp[i] = R.get(i - 1, pi);
                dNodT[i] = R.get(i - 1, Ti);

                for (Component mu : components.hc()) {
                    int j = mu.index();

                    dNodN[i][j] = R.get(i - 1, j - 1);
                }
            }
        }

        /**
         * Sets the hydrocarbon mole fractions
         */
        private void setHydrocarbonFractions(Composition N) {
            double NHC = N.getMoles() - N.getMoles(components.water());
            for (Component nu : components.hc()) {
                int i = nu.index();
                C[i] = N.getMoles(nu) / NHC;
            }
        }

        /**
         * Sets the composition of the oil and gas phases, using the calculated
         * liquid fraction and K-values
         */
        private void setOilGasComposition(Composition N, double L,
                Composition oil, Composition gas) {
            double NHC = N.getMoles() - N.getMoles(components.water());
            double No = L * NHC, Ng = (1 - L) * NHC;
            for (Component nu : components.hc()) {
                int i = nu.index();
                double Co = C[i] / (L + (1 - L) * K[i]);
                oil.setMoles(nu, Co * No);
                gas.setMoles(nu, K[i] * Co * Ng);
            }
        }

        /**
         * Solves the Rachford-Rice equation for the liquid hydrocarbon
         * fraction. Ensures that the fraction is bounded within [0,1]
         * 
         * @return Liquid hydrocarbon fraction
         */
        private double solveRachfordRice(double L) {

            /*
             * Check if a two-phase solution exists
             */

            // Single-phase gas?
            double f0 = f(0);
            if (f0 >= 0)
                return 0;

            // Single-phase oil?
            double f1 = f(1);
            if (f1 <= 0)
                return 1;

            /*
             * Possibly two hydrocarbon phases
             */

            double f = f(L);

            while (Math.abs(f) > 1e-12) {
                double dfdL = dfdL(L);

                // Ensure that L remains in (0,1)
                double alpha = 1;
                if (f > 0) {
                    double alpha0 = L * (dfdL / f);
                    if (alpha0 < alpha)
                        alpha = alpha0;
                } else {
                    double alpha1 = (L - 1) * (dfdL / f);
                    if (alpha1 < alpha)
                        alpha = alpha1;
                }

                L -= alpha * f / dfdL;

                f = f(L);
            }

            return L;
        }

        /**
         * Evaluates the Rachford-Rice equation
         */
        private double f(double L) {
            double f = 0;

            for (Component nu : components.hc()) {
                int i = nu.index();
                double nom = (K[i] - 1) * C[i];
                double denom = L + (1 - L) * K[i];
                if (denom != 0)
                    f += nom / denom;
            }

            return f;
        }

        /**
         * Derivative of the Rachford-Rice equation with respect to the liquid
         * hydrocarbon fraction
         */
        private double dfdL(double L) {
            double dfdL = 0;

            for (Component nu : components.hc()) {
                int i = nu.index();
                double factor = (K[i] - 1) / (L + (1 - L) * K[i]);
                dfdL += factor * factor * C[i];
            }

            return dfdL;
        }

        /**
         * Sets initial K-values based on Wilson's correlation formula
         */
        private void calculateInitialK(double p, double T) {
            for (Component nu : components.hc()) {
                int i = nu.index();
                double Tri = nu.getCriticalTemperature() / T;
                double pri = nu.getCriticalPressure() / p;
                double omega = nu.getAcentricFactor();
                K[i] = pri * Math.exp(5.373 * (1 + omega) * (1 - Tri));
            }
        }

        /**
         * Update the K-values using fugacity coefficients
         */
        private void setK() {
            for (Component nu : components.hc()) {
                int i = nu.index();
                K[i] = oilParameters.phi[i] / gasParameters.phi[i];
            }
        }
    }

    /**
     * Calculates the viscosity of a hydrocarbon phase using the
     * Lohrenz-Bray-Clark correlation
     */
    private class HydrocarbonViscosity {

        /**
         * Parameters for the LBC viscosity calculations
         */
        private final double a1, a2, a3, a4, a5;

        private final double[] sqrtM = new double[components.numComponents()],
                zeta_inv = new double[components.numComponents()];

        /**
         * Reads in parameters for the LBC hydrocarbon viscosity calculation
         */
        public HydrocarbonViscosity(Configuration config) {
            Configuration viscosity = config.getConfiguration("Viscosity");

            a1 = viscosity.getDouble("a1", 0.1023);
            a2 = viscosity.getDouble("a2", 0.023364);
            a3 = viscosity.getDouble("a3", 0.058533);
            a4 = viscosity.getDouble("a4", -0.040758);
            a5 = viscosity.getDouble("a5", 0.0093324);

            // Pre-calculations
            for (Component nu : components.hc()) {
                int i = nu.index();

                double M = nu.getMolecularWeight();
                sqrtM[i] = Math.sqrt(M);

                double Tc = nu.getCriticalTemperature();
                double pc = nu.getCriticalPressure();

                zeta_inv[i] = sqrtM[i] * Math.pow(pc, 2. / 3)
                        / Math.pow(Tc, 1. / 6);
            }
        }

        public HydrocarbonViscosity(HydrocarbonViscosity viscosity) {
            a1 = viscosity.a1;
            a2 = viscosity.a2;
            a3 = viscosity.a3;
            a4 = viscosity.a4;
            a5 = viscosity.a5;

            int num = components.numComponents();
            System.arraycopy(viscosity.sqrtM, 0, sqrtM, 0, num);
            System.arraycopy(viscosity.zeta_inv, 0, zeta_inv, 0, num);
        }

        /**
         * Calculates the viscosity of a hydrocarbon phase using the
         * Lohrenz-Bray-Clark correlation
         * 
         * @param T
         *                System temperature [K]
         * @param N
         *                Phase composition [mol]
         * @param xi
         *                Phase molar density [mol/m^3]
         * @return Phase viscosity [Pa*s]
         */
        public double calculateViscosity(double T, Composition N, double xi) {
            final double c1 = 3.4e-4, c2 = 1.778e-4, c3 = 4.58, c4 = 1.67;
            final double e1 = 0.94, e2 = 0.625;
            final double Tdiv = 1.5;

            double Vcl = 0, Tcl = 0, Ml = 0, pcl = 0, M2l = 0, musl = 0;
            for (Component nu : components.hc()) {
                int i = nu.index();

                double C = N.getMoleFraction(nu);
                double M = nu.getMolecularWeight();

                double Tc = nu.getCriticalTemperature();
                double pc = nu.getCriticalPressure();
                double Vc = nu.getCriticalMolarVolume();

                Vcl += C * Vc;
                Tcl += C * Tc;
                Ml += C * M;
                pcl += C * pc;
                M2l += C * sqrtM[i];

                /*
                 * Calculate the diluted component gas viscosity
                 */

                double mui = 0;
                double Tr = T / Tc;
                if (Tr > Tdiv)
                    mui = c1 * Math.pow(Tr, e1);
                else
                    mui = c2 * Math.pow(c3 * Tr - c4, e2);
                mui *= zeta_inv[i];

                musl += C * mui * sqrtM[i];
            }
            musl /= M2l;

            double zeta_inv = Math.sqrt(Ml) * Math.pow(pcl, 2. / 3)
                    / Math.pow(Tcl, 1. / 6);
            double xir = xi * Vcl;
            double chi = a1 + xir * (a2 + xir * (a3 + xir * (a4 + xir * a5)));

            double mu = musl + zeta_inv * (chi * chi * chi * chi - 1e-4);
            if (Double.isNaN(mu))
                return 0;
            else
                return mu * 1e-3; // From cP to Pa*s
        }
    }
}
