package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.pm.Population;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.optimization.sampling.FullFactorialSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.DataSet;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Sample;
import it.unicam.quasylab.sibilla.core.tools.stl.QuantitativeMonitor;
import it.unicam.quasylab.sibilla.core.util.Interval;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.langs.pm.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.pm.PopulationModelGenerator;
import it.unicam.quasylab.sibilla.langs.stl.StlLoader;
import it.unicam.quasylab.sibilla.langs.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.langs.stl.StlMonitorFactory;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public class OptimizationStlFormulaExample {

    private abstract static class Example{
        protected SimulationEnvironment se;
        protected RandomGenerator rg;

        protected int numbersOfSamples;

        public Example(long seed, int numbersOfSamples){
            this.se = new SimulationEnvironment();
            this.rg = new DefaultRandomGenerator(seed);
            this.numbersOfSamples = numbersOfSamples;
        }

        public Example(){
            this(1,25);
        }
    }


    /**
     *
     * Refer to <a href="https://arxiv.org/pdf/1402.1450.pdf">the case</a>
     * k_i  [0.005, 0.3]
     * k_r  [0.005, 0.2]
     */
    private static class SirExample extends Example{
        final String FORMULA_SIR =
                """
                measure #I; \s
                formula formula_id [] : ( \\E[100,120][ #I <= 0] )&& (\\G[0,100][ #I > 0 ]) ;
                """;
        final String MODEL___SIR =
                """
                param k_i = 0.05;
                param k_r = 0.05;
                
                const startS = 95;           /* Initial number of S agents */
                const startI = 5;           /* Initial number of I agents */
                const startR = 0;            /* Initial number of R agents */
                                            
                species S;
                species I;
                species R;
                                            
                rule infection {
                    S|I -[ #S * %I * k_i ]-> I|I
                }
                                            
                rule recovered {
                    I -[ #I * k_r ]-> R
                }
                                           
                system init = S<startS>|I<startI>|R<startR>;
                """;

        PopulationModelDefinition pmd;
        PopulationModel pm;
        QuantitativeMonitor<PopulationState> formula;

        public SirExample() throws ModelGenerationException, StlModelGenerationException {
            PopulationModelGenerator pmg = new PopulationModelGenerator(this.MODEL___SIR);
            this.pmd = pmg.getPopulationModelDefinition();
            this.pm = pmd.createModel();
            StlLoader stlLoader = new StlLoader(this.FORMULA_SIR);
            StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(getMeasuresMapFunction());
            this.formula = stlModelFactory.getQuantitativeMonitor("formula_id", new double[]{});
            //this.formula = getQuantitativeFormula();

        }


        /**
         * Stl formula Formula
         * \E[100,120][ #I <= 0] && \G[0,100][ #I > 0 ]
         * @return the stl formula
         */
        private QuantitativeMonitor<PopulationState> getQuantitativeFormula(){
            QuantitativeMonitor<PopulationState> infectedDoNotExist = QuantitativeMonitor.atomicFormula( pm -> 0 - pm.getOccupancy(1));
            QuantitativeMonitor<PopulationState> infectedExist = QuantitativeMonitor.atomicFormula( pm -> pm.getOccupancy(1));
            QuantitativeMonitor<PopulationState> eventuallyInfectedDoNotExist = QuantitativeMonitor.eventually(
                    new Interval(100,120),
                    infectedDoNotExist
            );
            QuantitativeMonitor<PopulationState> globallyInfectedExist = QuantitativeMonitor.globally(
                    new Interval(0,100),
                    infectedExist
            );
            return QuantitativeMonitor.conjunction(eventuallyInfectedDoNotExist,globallyInfectedExist);

        }


        private Map<String, ToDoubleFunction<PopulationState>> getMeasuresMapFunction(){
            Map<String, ToDoubleFunction<PopulationState>> measuresMapping = new HashMap<>();
            measuresMapping.put("#S", s -> s.getOccupancy(0));
            measuresMapping.put("#I", s -> s.getOccupancy(1));
            measuresMapping.put("#R", s -> s.getOccupancy(2));
            return measuresMapping;
        }

        private PopulationState getInitialState(){
            return new PopulationState(
                    3,
                    new Population(0,95),
                    new Population(1,5),
                    new Population(2,0)
            );
        }



        private Trajectory<PopulationState> getTrajectory (){
            Trajectory<PopulationState> trajectorySampled = super.se.sampleTrajectory(
                    super.rg,
                    this.pm,
                    this.getInitialState(),
                    this.formula.getTimeHorizon());
            Trajectory<PopulationState> trajectory  =new Trajectory<>();
            double timeHorizon = formula.getTimeHorizon();
            for (Sample<PopulationState> sample : trajectorySampled.getData()){
                if(sample.getTime() <= timeHorizon)
                    trajectory.add(sample.getTime(),sample.getValue());
            }
            trajectory.setEnd(timeHorizon);
            return trajectory;
        }

        public void setParameters(Map<String,Double> parameters){
            for (String key : parameters.keySet()){
                this.pmd.setParameter(key,new SibillaDouble(parameters.get(key)));
            }
            this.pm = this.pmd.createModel();
        }

        public double robustness(int numberOfSample){
            double r = 0;
            for (int i = 0; i < numberOfSample; i++) {
                r += formula.monitor(getTrajectory()).valueAt(0);
            }
            return  r/numberOfSample;
        }

        public ToDoubleFunction<Map<String,Double>> getRobustnessFunction(){
            return m -> {
                this.setParameters(m);
                return this.robustness(super.numbersOfSamples);
            };
        }

    }



    private static class RumorSpreadingExample extends Example{
        final String FORMULA_RS =
                """
                measure #spreader; 
                measure #ignorant; 
                formula formula_id [] : ( \\G[3,5][ #ignorant > 0] )&& (\\F[0,1][ \\G[0,1][  ] ]) ;
                """;
        final String MODEL___RS =
                """
                        param k_i = 0.05;
                        param k_r = 0.05;
                                        
                        const initial_spreaders = 90;
                        const initial_ignorants = 10;
                        const initial_blockers = 0;
                                                    
                        species spreader;
                        species ignorant;
                        species blocker;
                                                    
                        rule spreading {
                            spreader|ignorant -[ k_s ]-> spreader|spreader
                        }
                                                    
                        rule stop spreading_1 {
                            spreader|spreader -[ k_r ]-> spreader|blocker
                        }
                        
                        rule stop spreading_2 {
                            blocker|spreader -[ k_r ]-> blocker|blocker
                        }
                                                   
                        system init = spreader<initial_spreaders>|ignorant<ignorant>|blocker<initial_blockers>;
                        """;

        PopulationModelDefinition pmd;
        PopulationModel pm;
        QuantitativeMonitor<PopulationState> formula;

        public RumorSpreadingExample() throws ModelGenerationException, StlModelGenerationException {
            PopulationModelGenerator pmg = new PopulationModelGenerator(this.MODEL___RS);
            this.pmd = pmg.getPopulationModelDefinition();
            this.pm = pmd.createModel();
            StlLoader stlLoader = new StlLoader(this.FORMULA_RS);
            StlMonitorFactory<PopulationState> stlModelFactory = stlLoader.getModelFactory(getMeasuresMapFunction());
            this.formula = stlModelFactory.getQuantitativeMonitor("formula_id", new double[]{});

        }


        private Map<String, ToDoubleFunction<PopulationState>> getMeasuresMapFunction(){
            Map<String, ToDoubleFunction<PopulationState>> measuresMapping = new HashMap<>();
            measuresMapping.put("#spreader", s -> s.getOccupancy(0));
            measuresMapping.put("#ignorant", s -> s.getOccupancy(1));
            measuresMapping.put("#blocker", s -> s.getOccupancy(2));
            return measuresMapping;
        }

        private PopulationState getInitialState(){
            return new PopulationState(
                    3,
                    new Population(0,90),
                    new Population(1,10),
                    new Population(2,0)
            );
        }



        private Trajectory<PopulationState> getTrajectory (){
            Trajectory<PopulationState> trajectorySampled = super.se.sampleTrajectory(
                    super.rg,
                    this.pm,
                    this.getInitialState(),
                    this.formula.getTimeHorizon());
            Trajectory<PopulationState> trajectory  =new Trajectory<>();
            double timeHorizon = formula.getTimeHorizon();
            for (Sample<PopulationState> sample : trajectorySampled.getData()){
                if(sample.getTime() <= timeHorizon)
                    trajectory.add(sample.getTime(),sample.getValue());
            }
            trajectory.setEnd(timeHorizon);
            return trajectory;
        }

        public void setParameters(Map<String,Double> parameters){
            for (String key : parameters.keySet()){
                this.pmd.setParameter(key,new SibillaDouble(parameters.get(key)));
            }
            this.pm = this.pmd.createModel();
        }

        public double robustness(int numberOfSample){
            double r = 0;
            for (int i = 0; i < numberOfSample; i++) {
                r += formula.monitor(getTrajectory()).valueAt(0);
            }
            return  r/numberOfSample;
        }

        public ToDoubleFunction<Map<String,Double>> getRobustnessFunction(){
            return m -> {
                this.setParameters(m);
                return this.robustness(super.numbersOfSamples);
            };
        }

    }

    /**
     *
     * Refer to <a href="https://arxiv.org/pdf/1402.1450.pdf">the case</a>
     * k_i  [0.005, 0.3]
     * k_r  [0.005, 0.2]
     */
    @Disabled
    @Test
    public void testSir() throws StlModelGenerationException, ModelGenerationException, IOException {

        SirExample sirExample = new SirExample();

//        ToDoubleFunction<Map<String,Double>> robustnessFunction = m -> {
//            sirExample.setParameters(m);
//            return sirExample.robustness(100);
//        };

        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("k_i",0.005,0.3),
                new ContinuousInterval("k_r",0.005,0.2));

        DataSet dataSet = new DataSet(hr, new FullFactorialSamplingTask(),20,sirExample.getRobustnessFunction());
        CSVWriter writer = new CSVWriter("/Users/lorenzomatteucci/Documents/testSTLSIR", "", "");
        writer.write("dataset",dataSet);
        System.out.println(dataSet);



//        double robustness = sirExample.robustness(10);
//        System.out.println(robustness);
//        Map<String,Double> parameter = new HashMap<>();
//        parameter.put("k_i",0.5);
//        parameter.put("k_r",0.5);
//        sirExample.setParameters(parameter);
//        robustness = sirExample.robustness(10);
//        System.out.println(robustness);




    }


//    public void method() throws ModelGenerationException {
//        SimulationEnvironment se = new SimulationEnvironment();
//        RandomGenerator rg = new DefaultRandomGenerator(1);
//        PopulationModel pm = getPopulationModel(MODEL___SIR, new HashMap<>());
//
//        ToDoubleFunction<Map<String,Double>> function = m -> {
//            PopulationModel populationModel;
//            try {
//                populationModel =getPopulationModel(MODEL___SIR, m);
//            } catch (ModelGenerationException e) {
//                throw new RuntimeException(e);
//            }
//            //QuantitativeMonitor<PopulationState> qm = getPopulationFormula(FORMULA_SIR,populationModel.me)
//
//            return 0.0;
//        };
//    }
//


//     @Test
//     @Disabled
//     public void stlTestOnSIR_1() throws ModelGenerationException {
//
//
//         SimulationEnvironment se = new SimulationEnvironment();
//         RandomGenerator rg = new DefaultRandomGenerator(1);
//
//         QuantitativeMonitor<PopulationState> qMonitor = QuantitativeMonitor.eventually(
//                 new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(2) -25),
//                 0.0,
//                 1000.0
//         );
//
//         int numberOfSample = 100;
//         double robustness = 0.0;
//
//         for (int i = 0; i < numberOfSample; i++) {
//             robustness += getRobustness(qMonitor,se,rg,getPopulationModel(),getPopulationState());
//         }
//         robustness = robustness/numberOfSample;
//
//         assertEquals(17.23,robustness);
//     }






    //     private PopulationModel getPopulationModel() throws ModelGenerationException {
//         PopulationModelGenerator pmg = new PopulationModelGenerator(TEST_SIR);
//         PopulationModelDefinition pmd = pmg.getPopulationModelDefinition();
//         pmd.setParameter("infectionRate",new SibillaDouble(0.008));
//         PopulationModel pm = pmd.createModel();
//         return pm;
//     }
//
//
//     private PopulationState getPopulationState(){
//         return new PopulationState(
//                 3,
//                 new Population(0,95),
//                 new Population(1,5),
//                 new Population(2,0)
//         );
//     }
//
//     private  <S extends ImmutableState>  double getRobustness(QuantitativeMonitor<S> mon,SimulationEnvironment se,RandomGenerator rg, Model<S> model, S initialState){
//         double timeHorizon = mon.getTimeHorizon();
//         Trajectory<S> t = getTrajectory(se,rg,model,initialState,timeHorizon);
//         Signal s = mon.monitor(t);
//
//         System.out.println(t);
//         System.out.println("ROBUSTNESS : " + s.valueAt(0));
//         System.out.println("====================================================================");
//
//         return s.valueAt(0);
//
//     }
//
//     public <S extends ImmutableState>  Trajectory<S> getTrajectory (SimulationEnvironment se, RandomGenerator randomGenerator, Model<S> model, S initialState, double deadline){
//         Trajectory<S> t = se.sampleTrajectory(randomGenerator,model,initialState,deadline);
//         return t;
//     }



//     @Test
//     @Disabled
//     public void stlTestOnSIR_1() throws ModelGenerationException {
//
//
//         SimulationEnvironment se = new SimulationEnvironment();
//         RandomGenerator rg = new DefaultRandomGenerator(1);
//
//         QuantitativeMonitor<PopulationState> qMonitor = QuantitativeMonitor.eventually(
//                 new QuantitativeMonitor.AtomicMonitor<>(s -> s.getOccupancy(2) -25),
//                 0.0,
//                 1000.0
//         );
//
//         int numberOfSample = 100;
//         double robustness = 0.0;
//
//         for (int i = 0; i < numberOfSample; i++) {
//             robustness += getRobustness(qMonitor,se,rg,getPopulationModel(),getPopulationState());
//         }
//         robustness = robustness/numberOfSample;
//
//         assertEquals(17.23,robustness);
//     }


}
