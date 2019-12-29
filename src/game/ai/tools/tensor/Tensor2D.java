package game.ai.tools.tensor;


public class Tensor2D extends Tensor {

    int pd1,pd2;

    public Tensor2D(Tensor2D tensor) {
        super(tensor);
        updatePartialDimensions();
    }

    public Tensor2D(double[][] data){
        this.dimensions = new int[2];
        this.dimensions[0] = data.length;
        this.dimensions[1] = data[0].length;
        this.partialDimensions = new int[]{1,this.dimensions[0]};
        this.size = this.dimensions[0] * this.dimensions[1];
        this.data = new double[size];
        updatePartialDimensions();

        for(int i = 0; i < this.dimensions[0]; i++){
            for(int n = 0; n < this.dimensions[1]; n++){
                this.data[index(i,n)] = data[i][n];
            }
        }
    }

    public Tensor2D(double[] data, int d1, int d2) {
        super(data, d1,d2);
        updatePartialDimensions();
    }

    public Tensor2D(int d1, int d2) {
        super(d1,d2);
        updatePartialDimensions();
    }

    public int index(int d1, int d2) {
        return d1 * pd1 + d2 * pd2;
    }

    public double get(int d1, int d2) {
        int index = index(d1,d2);
        return data[index];
    }

    public void set(double val, int d1, int d2) {
        int index = index(d1,d2);
        data[index] = val;
    }

    @Override
    public void transpose(int dimA, int dimB) {
        super.transpose(dimA, dimB);
        updatePartialDimensions();
    }

    @Override
    public Tensor2D copy() {
        return new Tensor2D(this);
    }

    private void updatePartialDimensions(){
        this.pd1 = partialDimensions[0];
        this.pd2 = partialDimensions[1];
    }

    public static void main(String[] args) {
        Tensor2D tensor2D = new Tensor2D(new double[][]{{1,2}, {3,4}});
        System.out.println(tensor2D);
        tensor2D.scale(-1);
        System.out.println(tensor2D);
    }



}
