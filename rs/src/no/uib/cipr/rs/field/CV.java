package no.uib.cipr.rs.field;

import java.io.Serializable;
import java.util.Arrays;

import no.uib.cipr.rs.fluid.Component;
import no.uib.cipr.rs.fluid.Components;
import no.uib.cipr.rs.fluid.Composition;
import no.uib.cipr.rs.fluid.EquationOfState;
import no.uib.cipr.rs.fluid.EquationOfStateData;
import no.uib.cipr.rs.fluid.Phase;
import no.uib.cipr.rs.fluid.PhaseData;
import no.uib.cipr.rs.fluid.PhaseDataDouble;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.rock.RockFluid;

/**
 * The system state in a control volume
 */
public class CV implements Serializable {

    private static final long serialVersionUID = 9113207757638598174L;

    /**
     * Associated grid element
     */
    private final transient Element el;

    /**
     * Rock state
     */
    private final transient Rock rock;

    /**
     * Rock/fluid properties
     */
    private final transient RockFluid rockFluid;

    /**
     * Equation of state for fluid property calculations
     */
    private final transient EquationOfState eos;

    /**
     * Fluid components
     */
    private final transient Components components;

    /**
     * Equation of state data for each phase
     */
    private final PhaseData<EquationOfStateData> eosData = new PhaseData<EquationOfStateData>();

    /**
     * Oil phase pressure [Pa]
     */
    private double p;

    /**
     * Overall mass composition [mol]
     */
    private final Composition N;

    /**
     * System temperature [K]
     */
    private double T;

    /**
     * Effective porosity [-]
     */
    private double phi;

    /**
     * Initial fluid pressure, for use with compaction [Pa]
     */
    private double p0;

    /**
     * Oil/water capillary pressure [Pa]
     */
    private double pcow;

    /**
     * Gas/oil capillary pressure [Pa]
     */
    private double pcgo;

    /**
     * Relative permeabilities [-]
     */
    private final PhaseDataDouble kr = new PhaseDataDouble();

    /**
     * Phase saturations [-]
     */
    private final PhaseDataDouble S = new PhaseDataDouble();

    /**
     * Phase mass densities [kg/m^3]
     */
    private final PhaseDataDouble rho = new PhaseDataDouble();

    /**
     * Heat capacity summed over phases and the rock [J/K]
     */
    private double delta;

    /**
     * Phase mobilities [1/(Pa*s)]
     */
    private final PhaseDataDouble lambda = new PhaseDataDouble();

    /**
     * Residual volume [m^3]
     */
    private double R;

    /**
     * Residual volume derivative with pressure [m^3/Pa]
     */
    private transient double dRdp;

    /**
     * Residual volume derivative with temperature [m^3/K]
     */
    private transient double dRdT;

    /**
     * Residual volume derivative with molar masses [m^3/mol]
     */
    private final transient double[] dRdN;

    /**
     * The component mobilities. [mol/(m^3*Pa*s)]
     */
    private final PhaseDataDouble[] compMob;

    /**
     * The energy mobilities. [J/(m^3*Pa*s)]
     */
    private final PhaseDataDouble energyMob = new PhaseDataDouble();

    /**
     * Thermal run or not
     */
    private transient final boolean thermal;

    /**
     * Sets up a control volume
     * 
     * @param el
     *                Associated grid element
     * @param rockFluid
     *                Rock/fluid properties
     * @param eos
     *                Equation of state
     * @param components
     *                Components database
     * @param thermal
     *                For a thermal run
     */
    public CV(Element el, RockFluid rockFluid, EquationOfState eos,
            Components components, boolean thermal) {

        this.el = el;
        rock = el.rock;
        this.rockFluid = rockFluid;
        this.eos = eos;
        this.components = components;
        this.thermal = thermal;

        N = new Composition(components);

        int numComponents = components.numComponents();
        dRdN = new double[numComponents];
        compMob = new PhaseDataDouble[numComponents];
        for (Component nu : components.all())
            compMob[nu.index()] = new PhaseDataDouble();

        for (Phase phase : Phase.all())
            eosData.set(phase, new EquationOfStateData(components, phase));
    }

    /**
     * Sets the oil phase pressure
     */
    public void setPressure(double p) {
        this.p = p;
    }

    /**
     * Sets the reference pressure, for use in compaction
     */
    void setReferencePressure(double p0) {
        this.p0 = p0;
    }

    /**
     * Gets the reference pressure, for use with restarts
     */
    double getReferencePressure() {
        return p0;
    }

    /**
     * Gets the oil phase pressure
     * 
     * @return [Pa]
     */
    public double getPressure() {
        return p;
    }

    /**
     * Gets a phase pressure, taking capillary pressures into account
     * 
     * @return [Pa]
     */
    public double getPhasePressure(Phase phase) {
        switch (phase) {
        case WATER:
            return p - pcow;
        case OIL:
            return p;
        case GAS:
            return p + pcgo;
        default:
            throw new RuntimeException();
        }
    }

    /**
     * Sets the temperature
     */
    public void setTemperature(double T) {
        this.T = T;
    }

    /**
     * Gets the temperature
     * 
     * @return [K]
     */
    public double getTemperature() {
        return T;
    }

    /**
     * Gets the molar mass composition
     * 
     * @return [mol]
     */
    public Composition getComposition() {
        return N;
    }

    /**
     * Gets the equation of state data for a phase
     */
    public EquationOfStateData getEquationOfStateData(Phase phase) {
        return eosData.get(phase);
    }

    /**
     * Gets the saturation of a phase
     * 
     * @return [-]
     */
    public double getSaturation(Phase phase) {
        return S.get(phase);
    }

    /**
     * Gets the mass density of a phase
     * 
     * @return [kg/m^3]
     */
    public double getMassDensity(Phase phase) {
        return rho.get(phase);
    }

    /**
     * Gets the total heat capacity across the phases and rock
     * 
     * @return [J/K]
     */
    public double getHeatCapacity() {
        return delta;
    }

    /**
     * Gets the effective porosity
     * 
     * @return [-]
     */
    public double getPorosity() {
        return phi;
    }

    /**
     * Gets the phase mobility
     * 
     * @return [1/(Pa*s)]
     */
    public double getPhaseMobility(Phase phase) {
        return lambda.get(phase);
    }

    /**
     * Gets the component mobility
     * 
     * @return [mol/(m^3*Pa*s)]
     */
    public double getComponentMobility(Phase phase, Component nu) {
        return compMob[nu.index()].get(phase);
    }

    /**
     * Gets the energy mobility
     * 
     * @return [J/(m^3*Pa*s)]
     */
    public double getEnergyMobility(Phase phase) {
        return energyMob.get(phase);
    }

    /**
     * Gets the residual volume
     * 
     * @return [m^3]
     */
    public double getResidualVolume() {
        return R;
    }

    /**
     * Gets the residual volume derivative with pressure
     * 
     * @return [m^3/Pa]
     */
    public double getResidualVolumeDerivativePressure() {
        return dRdp;
    }

    /**
     * Gets the residual volume derivative with temperature
     * 
     * @return [m^3/K]
     */
    public double getResidualVolumeDerivativeTemperature() {
        return dRdT;
    }

    /**
     * Gets the residual volume derivative with component molar mass
     * 
     * @return [m^3/mol]
     */
    public double getResidualVolumeDerivativeMolarMass(Component nu) {
        return dRdN[nu.index()];
    }

    /**
     * Checks if a given phase is present
     */
    public boolean isPhasePresent(Phase phase) {
        return eosData.get(phase).isPresent();
    }

    /**
     * Calculates all secondary variables (fluid, rock, rock/fluid)
     */
    void calculateSecondaries() {

        // Phase equilibrium
        calculateFluidProperties();

        // Porosity and saturations
        calculateRockProperties();

        // Relative permeability and capillary pressures
        calculateRockFluidProperties();

        // Residual volume and its derivatives
        calculateResidualVolume();

        // lambda = kr / mu
        calculatePhaseMobility();

        // compMob = C * xi * lambda
        calculateComponentMobility();

        // energyMob = h * rho * lambda
        if (thermal)
            calculateEnergyMobility();
    }

    /**
     * Performs the phase flashing
     */
    private void calculateFluidProperties() {
        // Perform phase equilibrium calculations (flash)
        eos.calculatePhaseState(p, N, T, eosData);

        // The porous media heat capacity
        delta = el.volume * rock.getRockHeatCapacity();

        double[] dkgdN = new double[components.numComponents()];
        for (Phase phase : Phase.all()) {
            EquationOfStateData phaseEosData = eosData.get(phase);

            // Phase presence
            if (!phaseEosData.isPresent()) {
                rho.set(phase, 0);
                continue;
            }

            // Heat capacity
            delta += phaseEosData.getVolume() * phaseEosData.getHeatCapacity();

            // Phase weight
            double kg = 0;
            Arrays.fill(dkgdN, 0);
            Composition N = phaseEosData.getComposition();
            for (Component nu : components.all()) {
                double Mw = nu.getMolecularWeight();
                kg += Mw * N.getMoles(nu);
            }

            // Mass density
            double Vl = phaseEosData.getVolume();
            rho.set(phase, kg / Vl);
        }

        if (thermal && delta <= 0)
            throw new RuntimeException("Heat capacity in element "
                    + (el.index + 1) + " is " + delta
                    + ", but it must be positive");
    }

    /**
     * Calculates porosity and saturations
     */
    private void calculateRockProperties() {
        double cr = rock.getRockCompaction();
        double dp = p - p0;
        phi = rock.getInitialPorosity() * (1 + cr * dp + cr * cr * dp * dp / 2);

        // Total fluid volume
        double Vf = 0;
        for (Phase phase : Phase.all())
            Vf += eosData.get(phase).getVolume();

        // Saturations
        for (Phase phase : Phase.all())
            S.set(phase, eosData.get(phase).getVolume() / Vf);
    }

    /**
     * Calculates rock/fluid properties (relative permeability, capillary
     * pressures)
     */
    private void calculateRockFluidProperties() {
        rockFluid.calculateRelativePermeability(S, el, kr);

        pcow = rockFluid.calculateOilWaterCapillaryPressure(S, el);
        pcgo = rockFluid.calculateGasOilCapillaryPressure(S, el);
    }

    /**
     * Calculates the residual volume and all associated derivatives
     */
    private void calculateResidualVolume() {
        double V = el.volume;
        double Vp = phi * V;

        R = Vp;

        dRdp = rock.getRockCompaction() * Vp;
        dRdT = 0;
        Arrays.fill(dRdN, 0);

        for (Phase phase : Phase.all()) {
            EquationOfStateData phaseEosData = eosData.get(phase);

            R -= phaseEosData.getVolume();

            dRdp -= phaseEosData.getdVdp();
            dRdT -= phaseEosData.getdVdT();
            for (Component nu : components.all())
                dRdN[nu.index()] -= phaseEosData.getdVdN(nu);
        }

        // Increasing the pressure must decrease the overall volume
        if (dRdp <= 0)
            throw new RuntimeException("dR/dp is " + dRdp + " in element "
                    + (el.index + 1) + ", but it must be positive. "
                    + "Check PVT data or datum depth/pressure:\n" + this);
    }

    /**
     * Calculates phase mobilities
     */
    private void calculatePhaseMobility() {
        lambda.zero();

        for (Phase phase : Phase.all()) {
            double krl = kr.get(phase);
            double mul = eosData.get(phase).getViscosity();

            if (mul != 0)
                lambda.set(phase, krl / mul);
        }
    }

    /**
     * Calculates component mobilities
     */
    private void calculateComponentMobility() {
        for (Phase phase : Phase.all()) {
            EquationOfStateData phaseEosData = eosData.get(phase);

            Composition N = phaseEosData.getComposition();
            double xi = phaseEosData.getMolarDensity();
            double phaseLambda = lambda.get(phase);

            for (Component nu : components.all()) {
                int index = nu.index();

                double C = N.getMoleFraction(nu);
                compMob[index].set(phase, C * xi * phaseLambda);
            }
        }
    }

    /**
     * Calculates energy mobilities
     */
    private void calculateEnergyMobility() {
        for (Phase phase : Phase.all()) {
            EquationOfStateData phaseEosData = eosData.get(phase);

            double hrho = phaseEosData.getEnthalpyDensity();
            double phaseLambda = lambda.get(phase);

            energyMob.set(phase, hrho * phaseLambda);
        }
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();

        string.append(String.format("%-12s%15g\n", "pressure", p));
        string.append(String.format("%-12s%15g\n", "temperature", T));
        string.append(N.toString());

        return string.toString();
    }
}
