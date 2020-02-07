package ai.evaluator.optimiser;

import ai.evaluator.GeneticEvaluator;
import ai.evaluator.NoahEvaluator2;
import board.Board;
import board.FastBoard;
import io.IO;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class SimpleTexelOptimiser {


    public static double loss_probability = 0.3;
    public static double win_probability = 0.7;


    public static int WHITE_WIN = 1;
    public static int BLACK_WIN = -1;
    public static int DRAW = 0;

    private ArrayList<Board> fen_strings = new ArrayList<>();
    private ArrayList<Integer>  results = new ArrayList<>();


    public void readFile(String file, Board template, int count){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(file)));

            int index = 0;

            String line;
            while((line=bufferedReader.readLine()) != null){
                int midIndex = line.indexOf('"');
                String fen = line.substring(0, midIndex-1).trim();
                String res = line.substring(midIndex).trim();

                //System.out.println(fen +  "     "  +res);

                index++;

                if(index == count){
                    break;
                }

                fen_strings.add(IO.read_FEN(template,fen));

                switch (res){
                    case "\"1/2-1/2\";": results.add(DRAW);break;
                    case "\"1-0\";": results.add(WHITE_WIN);break;
                    case "\"0-1\";": results.add(BLACK_WIN);break;
                }
            }

            bufferedReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void iteration(GeneticEvaluator evaluator, double K, double eta) {
        double[] params = evaluator.getEvolvableValues();

        double drX = 0.1;   //value to compute gradients

        double initError = error(evaluator, K);

        System.out.println("Iteration starting...");
        System.out.print("Initial params: " );
        for(double d:params){
            System.out.format(" %+3.1f", d);
        }
        System.out.println();

        for(int i = 0; i < params.length; i++){
            double dX = Math.abs(params[i] * drX);
            params[i] += dX;
            evaluator.setEvolvableValues(params);
            double upperE = error(evaluator, K);
            params[i] -= 2 * dX;
            evaluator.setEvolvableValues(params);
            double lowerE = error(evaluator, K);
            params[i] += dX;
            evaluator.setEvolvableValues(params);

            double dEdX = (upperE - lowerE) / (2 * dX);

//            double change = 0;
//
//            if(Math.abs(dEdX) < 1E-12){
//                change = 0;
//            }else{
//                if(dEdX > 0){
//                    change -= dX * eta;
//                }else{
//                    change += dX * eta;
//                }
//                params[i] += change;
//            }

            double change = -dEdX * eta;
            params[i] += change;
            evaluator.setEvolvableValues(params);

            if(change == 0){
                System.err.format("param: %d change: %+6.7E upperE: %+2.7E lowerE: %+2.7E dEdX: %+2.7E\n", i, change, upperE, lowerE, dEdX);
            }else{
                System.out.format("param: %d change: %+6.7E upperE: %+2.7E lowerE: %+2.7E dEdX: %+2.7E\n", i, change, upperE, lowerE, dEdX);
            }
        }

        double afterError = error(evaluator, K);


        evaluator.setEvolvableValues(params);
        System.out.println("Iteration finished; dE: "+(afterError - initError) + "  E_(i-1): " + initError + " E_i: " + afterError);
        System.out.print("Resulting params: " );
        for(double d:params){
            System.out.format(" %+3.1f", d);
        }
        System.out.println();
    }

    public double score(GeneticEvaluator evaluator){
        double total = 0;
        for(int i = 0; i < fen_strings.size(); i++){
            double p = centipawnAdvantageToProbability((int)evaluator.evaluate(fen_strings.get(i)));
            double expected = results.get(i) == DRAW ? 0.5:
                    results.get(i) == WHITE_WIN ? win_probability:loss_probability;
            total += (p-expected)*(p-expected)/2d;
        }
        return total / fen_strings.size();
    }

    /**
     *
     * Uses gradients to compute optimal K.
     *
     * @param evaluator
     * @param min_dEdK      acts as a threshold. values between 1E-5 and 1E-10 are usually a good choice
     * @param init_K        init_K is the initial starting value for K
     * @param eta           eta is the rate of change
     * @return
     */
    public double computeK(GeneticEvaluator evaluator, double min_dEdK, double init_K, double eta) {
        double K = init_K;
        double dK = 0.01;
        double dEdK = 1;

        while(Math.abs(dEdK) > min_dEdK){
            dEdK = (error(evaluator, K + dK) - error(evaluator, K - dK)) / (2 * dK);
            System.out.format("K: %-2.6f  Error: %-2.6f  dE/dK: %-1.2E\n", K,error(evaluator, K),dEdK);
            K -= dEdK * eta;
        }
        return K;
    }

    public double error(GeneticEvaluator evaluator, double K){
        double total = 0;
        for(int i = 0; i < fen_strings.size(); i++){
            double qi = (int)evaluator.evaluate(fen_strings.get(i));
            double expected = results.get(i) == DRAW ? 0.5:
                    results.get(i) == WHITE_WIN ? win_probability:loss_probability;
            double sig = sigmoid(qi, K);
            total += (expected - sig) * (expected - sig);
        }
        return total / fen_strings.size();
    }

    public double sigmoid(double s, double K){
        return 1d / (1 + Math.exp(-K * s / 400));
    }

    public static int probabiltyToCentipawnAdvantage(double prob){
        return (int)(400 * Math.log10(prob / (1-prob)));
    }

    public static double centipawnAdvantageToProbability(int centipawns){
        return 1d / (1+Math.pow(10, - centipawns/400d));
    }

    public static void main(String[] args) {
        SimpleTexelOptimiser tex = new SimpleTexelOptimiser();
        tex.readFile("resources/quiet-labeled.epd", new FastBoard(), 1000000);
//        System.out.println(tex.score(new NoahEvaluator2()));

        double K = tex.computeK(new NoahEvaluator2(), 1E-5, 0.6173, 10);

        NoahEvaluator2 evaluator2 = new NoahEvaluator2();

        for(int i = 0; i < 500; i++)
            tex.iteration(evaluator2, K, 1E6);

        //System.out.println(tex.error(new NoahEvaluator2(), 600));

    }

}
