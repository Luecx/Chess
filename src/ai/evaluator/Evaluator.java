package ai.evaluator;

import board.Board;

import java.lang.reflect.Field;

/**
 * The interface is used to create Evaluators.
 * Evaluators are used in Search-algorithms in the leaf-nodes and
 * take up most of the calculation time.
 *
 */
public interface Evaluator<T extends Evaluator<T>> {

    /**
     * this method returns a value that evaluates the board.
     * A higher score should favor white where a lower (maybe negative) score
     * favors black.
     *
     * @param board
     * @return the evaluation
     */
    public double evaluate(Board board);


    /**
     * defines the values that can be evolved/tuned
     * @return
     */
    public abstract double[] getEvolvableValues();

    /**
     * sets the tuned values
     * @param ar
     */
    public abstract void setEvolvableValues(double[] ar);

    /**
     * copies this evaluator object
     * @return
     */
    public abstract T copy();

    /**
     * returns the static exchange evaluation on the board for the given square/color
     * the result equals 0 if the exchange is equal OR losing!
     *
     * @param board
     * @param sq
     * @param color
     * @return
     */
    public abstract double staticExchangeEvaluation(Board board, int sq, int color);


    public static void createParameters(Class<?> cl, double[] params){

        Field[] ar = cl.getDeclaredFields();

        int c = 0;
        for(Field f:ar){
            String name = f.getName();
            if(name.startsWith("PARAMETER")){ ;
                System.out.format("%-80s %-50s %n","private double " + name + " = ", (int)params[c]+";");
                c++;
            }
        }
    }

    public static void createFunction_getEvolvableValues(Class<?> cl){
        Field[] ar = cl.getDeclaredFields();

        System.out.println("@Override");
        System.out.println("public abstract double[] getEvolvableValues(){");
        System.out.println("    return new double[]{");

        for(Field f:ar){
            String name = f.getName();
            if(name.startsWith("PARAMETER")){
                System.out.println(name + ",");
            }
        }

        System.out.println("};}");
    }

    public static void createFunction_setEvolvableValues(Class<?> cl){
        Field[] ar = cl.getDeclaredFields();

        System.out.println("@Override");
        System.out.println("public abstract void setEvolvableValues(double[] ar){");

        int counter = 0;

        for(Field f:ar){
            String name = f.getName();
            if(name.startsWith("PARAMETER")){
                System.out.println(name +" = ar["+counter+"];");
                counter ++;
            }
        }

        System.out.println("}");
    }

    public static void main(String[] args) {
       createFunction_getEvolvableValues(AdvancedEvaluator.class);
        createFunction_setEvolvableValues(AdvancedEvaluator.class);
    }

}
