package game.ai.tools.tensor;

import java.util.Arrays;
import java.util.Random;

public class Tensor1D extends Tensor{

    public Tensor1D(Tensor1D tensor){
        this.partialDimensions = Arrays.copyOf(tensor.partialDimensions, tensor.partialDimensions.length);
        this.dimensions = Arrays.copyOf(tensor.dimensions, tensor.dimensions.length);
        this.size = tensor.size;
        this.data = Arrays.copyOf(tensor.data, tensor.data.length);
    }

    public Tensor1D(double[] data) {
        super(data.length);
        this.data = data;
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
        return 1;
    }

    public int size() {
        return size;
    }

    public int index(int... indices) {
        return indices[0];
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

    public double get(int index) {
        return data[index];
    }

    public void set(double val, int index) {
        data[index] = val;
    }

    public Tensor1D copy() {
        return new Tensor1D(this);
    }

    @Override
    public String toString() {
        return "Tensor{" +
                "data=" + Arrays.toString(data) +
                '}';
    }


    public static void main(String[] args) {
        Tensor1D tensor1D = new Tensor1D(new double[]{1,2,3,4});
        System.out.println(tensor1D);
        System.out.println(tensor1D.size);
        System.out.println(Arrays.toString(tensor1D.dimensions));
        System.out.println(Arrays.toString(tensor1D.partialDimensions));

    }
}
