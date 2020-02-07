package ai.tools.tensor;

import java.util.Arrays;
import java.util.Random;

public class Tensor {

    protected double[] data;

    protected int[] partialDimensions;
    protected int[] dimensions;
    protected int size;

    public Tensor(Tensor tensor){
        this.partialDimensions = Arrays.copyOf(tensor.partialDimensions, tensor.partialDimensions.length);
        this.dimensions = Arrays.copyOf(tensor.dimensions, tensor.dimensions.length);
        this.size = tensor.size;
        this.data = Arrays.copyOf(tensor.data, tensor.data.length);
    }

    public Tensor(double[] data, int... dimensions) {
        this(dimensions);
        if (this.size != data.length) throw new RuntimeException("array size doesnt fit dimensions: " +
                Arrays.toString(dimensions) + " = " + this.size + " =/= " + data.length);
        this.dimensions = dimensions;
        this.size = 1;
        this.partialDimensions = new int[dimensions.length];
        for (int i = 0; i < dimensions.length; i++) {
            partialDimensions[i] = size;
            size *= dimensions[i];
        }
        this.data = data;
    }

    public Tensor(int... dimensions) {
        this.dimensions = dimensions;
        this.size = 1;
        this.partialDimensions = new int[dimensions.length];
        for (int i = 0; i < dimensions.length; i++) {
            partialDimensions[i] = size;
            size *= dimensions[i];
        }
        this.data = new double[this.size];
    }

    public void transpose(int dimA, int dimB) {
        int dim = partialDimensions[dimA];
        partialDimensions[dimA] = partialDimensions[dimB];
        partialDimensions[dimB] = dim;
        dim = dimensions[dim];
        dimensions[dimA] = dimensions[dimB];
        dimensions[dimB] = dim;
    }

    public void reset(double val){
        for(int i = 0; i < this.size; i++){
            this.data[i] = val;
        }
    }

    public int rank() {
        return dimensions.length;
    }

    public int size() {
        return size;
    }

    public int index(int... indices) {
        if (indices.length != dimensions.length)
            throw new RuntimeException();
        int i = 0;
        for (int k = 0; k < indices.length; k++) {
            if (dimensions[k] <= indices[k]) throw new IndexOutOfBoundsException();
            i += partialDimensions[k] * indices[k];
        }
        return i;
    }

    public double[] getData() {
        return data;
    }

    public void setData(double[] data) {
        if(data.length == this.size)
            this.data = data;
    }


    public int getDimension(int dimension){
        return dimensions[dimension];
    }

    public void randomizeRegular(double min, double max) {
        for(int i = 0; i < this.size; i++){
            data[i] = Math.random() * (max-min) + min;
        }
    }

    public void randomizeRegular(double min, double max, long seed){
        Random r = new Random(seed);
        for(int i = 0; i < this.size; i++){
            data[i] = r.nextDouble() * (max-min) + min;
        }
    }

    public double get(int... indices) {
        int index = index(indices);
        return data[index];
    }

    public void set(double val, int... indices) {
        int index = index(indices);
        data[index] = val;
    }

    public void add(double val, int... indices){
        int index = index(indices);
        data[index] += val;
    }

    public void normalise() {
        this.scale(1d / max());
    }

    public void scale(double scalar) {
        for(int i = 0; i < this.size; i++){
            data[i] *= scalar;
        }
    }

    public double min() {
        double min = this.data[0];
        for (double d : this.data) {
            min = d < min ? d : min;
        }
        return min;
    }

    public double max() {
        double max = this.data[0];
        for (double d : this.data) {
            max = d > max ? d : max;
        }
        return max;
    }

    public Tensor copy() {
        return new Tensor(this);
    }

    @Override
    public String toString() {
        return "Tensor{" +
                "data=" + Arrays.toString(data) +
                '}';
    }

}
