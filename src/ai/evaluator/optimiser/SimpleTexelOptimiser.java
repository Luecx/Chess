package ai.evaluator.optimiser;

import ai.evaluator.AdvancedEndGameEvaluator;
import ai.evaluator.AdvancedEvaluator;
import ai.evaluator.AdvancedMidGameEvaluator;
import ai.evaluator.Evaluator;
import ai.evaluator.decider.BoardPhaseDecider;
import ai.evaluator.decider.SimpleDecider;
import ai.tools.threads.Pool;
import ai.tools.threads.PoolFunction;
import board.Board;
import board.FastBoard;
import io.IO;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class SimpleTexelOptimiser {


    public static int WHITE_WIN = 1;
    public static int BLACK_WIN = -1;
    public static int DRAW = 0;

    private ArrayList<Board> fen_strings = new ArrayList<>();
    private ArrayList<Integer>  results = new ArrayList<>();


    private Pool pool;

    /**
     * reads the first count entries in the given file.
     * @param file
     * @param template
     * @param count
     */
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


                if(index == count){
                    break;
                }

                index++;

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

    public void iterationGradient(Evaluator evaluator, double K, double eta) {
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

    public double[] iterationLocally(Evaluator evaluator, double K, int cores){

        this.pool = null;
        Pool pool = new Pool(cores);
        final double[] params               = evaluator.getEvolvableValues();
        final int   nParams                 = params.length;
        final int[] unchangedIterations     = new int[nParams];
        final int[] recentChange            = new int[nParams];
        final boolean[] improved = {true};

        Evaluator[] evaluators = new Evaluator[cores];
        for(int c = 0; c < cores; c++){
            evaluators[c] = evaluator.copy();
        }

        while (improved[0]) {
            improved[0] = false;
            PoolFunction poolFunction = (index, core) -> {
                boolean thisParamChanged = false;
                Evaluator eval = evaluators[core];


                if(unchangedIterations[index] < recentChange[index]){
                    unchangedIterations[index] ++;
                    return;
                }

                eval.setEvolvableValues(params);
                double E0 = error(eval, K);

                params[index] += 1;
                eval.setEvolvableValues(params);
                double EU = error(eval, K);

                params[index] -= 2;
                eval.setEvolvableValues(params);
                double EL = error(eval, K);

                params[index] ++;
                double dEdP = (EU-EL)/2;
                double sign = Math.signum(dEdP);

                double dP = -sign * Math.min(5,Math.floor(Math.abs(dEdP) * 1E5));

                if(dP == 0){
                    thisParamChanged = false;
                }else{
                    params[index] += dP;
                    thisParamChanged = true;
                }

                //System.out.println(index + "  " + dEdP + "  " + sign * Math.min(5,Math.floor(Math.abs(dEdP) * 1E5)));

//                params[index] -= Math.min(5,Math.floor(dEdP * 100));
//                System.out.println(Math.min(5,Math.floor(dEdP * 100)));

//                eval.setEvolvableValues(params);
//                double newE = error(eval, K);
//                if (newE < E0) {
//                    thisParamChanged = true;
//                } else {
//                    params[index] -= 2;
//                    eval.setEvolvableValues(params);
//                    newE =  error(eval, K);
//                    if (newE < E0) {
//                        thisParamChanged = true;
//                    }else{
//                        params[index] ++;
//                    }
//                }


                if(thisParamChanged){
                    improved[0] = true;
                    unchangedIterations[index] = 0;
                    recentChange       [index] = 0;
                }else{
                    unchangedIterations[index] = 0;
                    recentChange       [index] ++;
                }
            };


            pool.executeSequential(poolFunction, nParams, true);

            System.out.println();
            System.out.println(Arrays.toString(params));

            evaluator.setEvolvableValues(params);
            System.out.println("bestE:" +error(evaluator,K));
        }
        pool.stop();
        return params;
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
    public double computeK(Evaluator evaluator, double min_dEdK, double init_K, double eta, int cores) {
        pool = new Pool(cores);
        double K = init_K;
        double dK = 0.01;
        double dEdK = 1;

        while(Math.abs(dEdK) > min_dEdK){
            dEdK = (error(evaluator, K + dK) - error(evaluator, K - dK)) / (2 * dK);
            System.out.format("K: %-2.6f  Error: %-2.6f  dE/dK: %-1.2E\n", K,error(evaluator, K),dEdK);
            K -= dEdK * eta;
        }
        pool.stop();
        return K;
    }

    public double error(Evaluator evaluator, double K){

        if(pool != null){
            final Double[] score = {0d};

            int tasks = fen_strings.size();
            int threads = pool.getAvailableThreads();

            pool.executeTotal((index, core) -> {
                double pScore = 0;
                double lower = (double) (tasks) / threads * index;
                double upper = (double) (tasks) / threads * (index + 1);
                for (int i = (int) lower; i < (int) upper; i++) {
                    double qi = (int) evaluator.evaluate(fen_strings.get(i));
                    double expected = results.get(i) == DRAW ? 0.5 :
                            results.get(i) == WHITE_WIN ? 1 : 0;
                    double sig = SimpleTexelOptimiser.this.sigmoid(qi, K);
                    pScore += (expected - sig) * (expected - sig);
                }
                synchronized (score) {
                    score[0] += pScore;
                }
            }, threads, false);
            return score[0] / tasks;
        }else
        {
            double total = 0;
            for(int i = 0; i < fen_strings.size(); i++){
                double qi = (int)evaluator.evaluate(fen_strings.get(i));
                double expected = results.get(i) == DRAW ? 0.5:
                        results.get(i) == WHITE_WIN ? 1:0;
                double sig = sigmoid(qi, K);
                total += (expected - sig) * (expected - sig);
            }
            return total / fen_strings.size();
        }

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
        tex.readFile("resources/quiet-labeled.epd",
                     new FastBoard(),
                     1000000);

        AdvancedEvaluator evaluator2 = new AdvancedEvaluator(new SimpleDecider());
//        evaluator2.setEvolvableValues(new double[]{
//                70.0, 99.0, 78.0, 97.0, 66.0, 81.0, 196.0, 901.0, 620.0, 623.0, 1323.0, 19956.0, 56.0, -2.0,
//                5.0, 47.0, -38.0, -14.0, -17.0, -82.0, -43.0, -41.0, -33.0, -34.0, 35.0, 103.0, -12.0, -63.0,
//                125.0, 23.0, 73.0, 46.0, -5.0, -8.0, 112.0, 91.0, 78.0, 73.0, 66.0, 60.0, 233.0, 1041.0,
//                703.0, 735.0, 1445.0, 20084.0, 64.0, -1.0, 4.0, 62.0, -38.0, -11.0, 0.0, -69.0, -54.0, -67.0,
//                -79.0, -86.0, 60.0, 121.0, -13.0, -91.0, 140.0, 30.0, 70.0, 61.0, -14.0, -8.0
//        });
        double K = tex.computeK(evaluator2, 1E-5, 1.339017, 10,1);

        tex.iterationLocally(evaluator2, K, 6);


    }

}
