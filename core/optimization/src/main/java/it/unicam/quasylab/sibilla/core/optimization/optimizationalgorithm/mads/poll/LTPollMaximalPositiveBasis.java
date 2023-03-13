package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll;

public class LTPollMaximalPositiveBasis extends AbstractPollMethod{
    @Override
    protected int[][] getPositiveBasis(int dimension, double deltaMesh) {
        LTDirection ltDirection = new LTDirection();
        return ltDirection.getMaximalPositiveBasis(dimension,deltaMesh);
    }
}
