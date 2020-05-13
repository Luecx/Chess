package ai.evaluator.optimiser;

import ai.evaluator.AdvancedEvaluator;
import ai.evaluator.AdvancedEvaluatorNew;
import ai.evaluator.Evaluator;
import ai.evaluator.SimpleEvaluator;
import ai.evaluator.decider.SimpleDecider;
import ai.ordering.SystematicOrderer2;
import ai.reducing.SenpaiReducer;
import ai.search.AdvancedSearch;
import ai.tools.threads.Pool;
import board.Board;
import board.FastBoard;
import board.moves.Move;
import com.sun.security.jgss.AuthorizationDataEntry;
import io.IO;
import io.Testing;
import io.UCI;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class SimpleTexelOptimiser {


    public static int WHITE_WIN = 1;
    public static int BLACK_WIN = -1;
    public static int DRAW = 0;

    private ArrayList<Board> fen_strings = new ArrayList<>();
    private ArrayList<Integer>  results = new ArrayList<>();


    //private AdvancedSearch[] searchers;
    private ArrayList<Board>[] boards;


    public void prepare(int cores) {
        //searchers = new AdvancedSearch[cores];





        //boards = new ArrayList[cores];

//        for (int i = 0; i < cores; i++) {
//            searchers[i] = new AdvancedSearch(
//                    //new AdvancedEvaluator(new SimpleDecider()),
//                    new SimpleEvaluator(),
//                    new SystematicOrderer2(),
//                    new SenpaiReducer(1),
//                    AdvancedSearch.FLAG_TIME_LIMIT,
//                    0);
//
//            //boards[i] = new ArrayList<>();
//        }

//        int counter = 0;
//        for(Board b:fen_strings){
//            System.out.println(counter++);
//            for(int i = 0; i < cores; i++){
//                boards[i].add(b.copy());
//            }
//        }


        System.out.println();
    }

    /**
     * reads the first count entries in the given file.
     * @param file
     * @param template
     * @param count
     */
    public void readFile(String file, Board template, int count){
        try {

            System.out.print("reading file...");
            long t = System.currentTimeMillis();

            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(file)));

            int index = 0;

            String line;
            while((line=bufferedReader.readLine()) != null){

                if(index == count){
                    break;
                }

                int midIndex = line.indexOf('c');
                int endIndex = line.lastIndexOf('c');
                String fen = line.substring(0, midIndex-1).trim();
                String res = line.substring(endIndex+2).trim();
                res = res.substring(1, res.length()-2).trim();



                index++;

                fen_strings.add(IO.read_FEN(template,fen));

                try{
                    Double result = Double.parseDouble(res);
                    if(result < 0.3){
                        results.add(BLACK_WIN);
                    }else if(result > 0.7){
                        results.add(WHITE_WIN);
                    }else{
                        results.add(DRAW);
                    }
                }catch (Exception e){
                    switch (res){
                        case "1/2-1/2": results.add(DRAW);break;
                        case "1-0": results.add(WHITE_WIN);break;
                        case "0-1": results.add(BLACK_WIN);break;
                        default: throw new RuntimeException();
                    }
                }


            }


            bufferedReader.close();

            System.out.println(" finished. took: " + (System.currentTimeMillis()-t) + " ms");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public double[] iterationGradientDescent(AdvancedEvaluatorNew evaluator, double K, double eta, int cores){
        prepare(cores);
        Pool pool = new Pool(cores);


        double[] params = evaluator.getEvolvableValues();
        double[] gradBuffer = new double[params.length];

        double lastError = 1000000;

        while(true){
            Arrays.fill(gradBuffer, 0);

            double error = errorWithGradients(evaluator, K, pool, gradBuffer);

            if(error > lastError){
                pool.stop();
                return params;
            }

            params = evaluator.getEvolvableValues();

            System.out.println("error: " + error);
            System.out.println("params: " + Arrays.toString(params));

            double[] tweaked = evaluator.getEvolvableValues();
            for(int i = 0; i < params.length; i++){
                tweaked[i] -= gradBuffer[i] * eta;
            }
            evaluator.setEvolvableValues(tweaked);

        }


    }

    public double[] iterationLocally(Evaluator evaluator, double K, int cores){
        prepare(cores);
        Pool pool = new Pool(cores);
        double[] params = evaluator.getEvolvableValues();
        int nParams = params.length;
        int[] lastTimeChecked = new int[nParams];
        int[] iterationToPause = new int[nParams];
        boolean improved = true;
        Evaluator[] evaluators = new Evaluator[cores];
        for (int c = 0; c < cores; c++) {
            evaluators[c] = evaluator.copy();
        }
        while (improved) {
            improved = false;
            int skipped = 0;
            int changed = 0;
            for (int i = 0; i < nParams; i++) {


                System.out.print("\r"+Testing.loadingBar(i+1, nParams, ""));

                boolean thisParamChanged;
                if (lastTimeChecked[i] < iterationToPause[i]) {
                    lastTimeChecked[i]++;
                    skipped++;
                    continue;
                }
                evaluator.setEvolvableValues(params);
                double E0 = errorMultithreaded(evaluator, K, pool);
                params[i] += 1;
                evaluator.setEvolvableValues(params);
                double EU = errorMultithreaded(evaluator, K, pool);
                params[i] -= 2;
                evaluator.setEvolvableValues(params);
                double EL = errorMultithreaded(evaluator, K, pool);
                params[i]++;
                if (E0 < EU && E0 < EL) {
                    thisParamChanged = false;
                } else {
                    thisParamChanged = true;
                    double dEdP = (EU - EL) / 2;
                    double sign = Math.signum(dEdP);
                    double dP = -sign * Math.min(2, Math.ceil(Math.abs(dEdP) * 1E6));

                    if(Math.abs(dP) < 1){
                        thisParamChanged = false;
                    }

                    params[i] += dP;
                }
                lastTimeChecked[i] = 0;
                if (thisParamChanged) {
                    improved = true;
                    changed++;
                    iterationToPause[i] = 0;
                } else {
                    lastTimeChecked[i] = 0;
                    iterationToPause[i]++;
                }
            }
            System.out.println();
            System.out.println(Arrays.toString(params));
            evaluator.setEvolvableValues(params);
            System.out.println("changed: " + changed + "  skipped: " + skipped + " loss: " + errorMultithreaded(evaluator, K, pool));
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

        prepare(cores);



        double K = init_K;
        double dK = 0.01;
        double dEdK = 1;

        Pool pool = new Pool(cores);

        while(Math.abs(dEdK) > min_dEdK){
            dEdK = (errorMultithreaded(evaluator, K + dK, pool) - errorMultithreaded(evaluator, K - dK, pool)) / (2 * dK);
            System.out.format("K: %-2.6f  Error: %-2.6f  dE/dK: %-1.2E\n", K,errorMultithreaded(evaluator, K, pool),dEdK);
            K -= dEdK * eta;
        }

        pool.stop();

        return K;
    }

    public double errorWithGradients(AdvancedEvaluatorNew evaluator, double K, Pool pool, double[] gradientsBuffer){
        final Double[] score = {0d};

        int tasks = fen_strings.size();
        int threads = pool.getAvailableThreads();

        final AdvancedEvaluatorNew[] evaluators             = new AdvancedEvaluatorNew  [pool.getActiveThreads()];
        final double[]            [] threadGradients        = new double                [pool.getActiveThreads()][];
        for(int i = 0; i < evaluators.length; i++){
            evaluators[i]           = evaluator.copy();
            threadGradients[i]      = new double[gradientsBuffer.length];
        }

        Arrays.fill(gradientsBuffer, 0);

        pool.executeTotal((index, core) -> {
            double pScore = 0;
            double lower = (double) (tasks) / threads * index;
            double upper = (double) (tasks) / threads * (index + 1);


            for (int i = (int) lower; i < (int) upper; i++) {


//                searchers[core].setEvaluator(evaluator);
//                double qi = searchers[core].qSearch(fen_strings.get(i));
                double qi = evaluators[core].evaluate(fen_strings.get(i));
                double expected = results.get(i) == DRAW ? 0.5 :
                        results.get(i) == WHITE_WIN ? 1 : 0;

                double sig              = this.sigmoid(qi, K);
                double sigPrime         = this.sigmoidPrime(qi, K);
                double lossPrime        = -2 * (expected - sig);

                double[] gradients      = evaluators[core].getGradients();

                for(int g = 0; g < gradients.length; g++){
                    threadGradients[core][g] += sigPrime * lossPrime * gradients[g];
                }

                //System.out.println(IO.write_FEN(fen_strings.get(i)) + " " + qi);

                pScore += (expected - sig) * (expected - sig);
            }
            synchronized (score) {
                score[0] += pScore;
            }
        }, threads, false);

        for(int t = 0; t < pool.getActiveThreads(); t++){
            for(int g = 0; g < gradientsBuffer.length; g++){
                gradientsBuffer[g] += threadGradients[t][g];
            }
        }

        return score[0] / tasks;
    }

    private double errorSingleThreaded(Evaluator evaluator, double K){
//        if(searchers[0] == null){
//            prepare(0);
//        }
        double total = 0;
        for(int i = 0; i < fen_strings.size(); i++){
            //searchers[0].setEvaluator(evaluator);
            //double qi = searchers[0].qSearch(fen_strings.get(i));
            double qi= evaluator.evaluate(fen_strings.get(i));
            double expected = results.get(i) == DRAW ? 0.5:
                    results.get(i) == WHITE_WIN ? 1:0;

            double sig = sigmoid(qi, K);
            total += (expected - sig) * (expected - sig);
        }
        return total / fen_strings.size();
    }

    private double errorMultithreaded(Evaluator evaluator, double K, Pool pool){
        final Double[] score = {0d};

        int tasks = fen_strings.size();
        int threads = pool.getAvailableThreads();


        pool.executeTotal((index, core) -> {
            double pScore = 0;
            double lower = (double) (tasks) / threads * index;
            double upper = (double) (tasks) / threads * (index + 1);


            for (int i = (int) lower; i < (int) upper; i++) {
//                searchers[core].setEvaluator(evaluator);
//                double qi = searchers[core].qSearch(fen_strings.get(i));
                double qi = evaluator.evaluate(fen_strings.get(i));
                double expected = results.get(i) == DRAW ? 0.5 :
                        results.get(i) == WHITE_WIN ? 1 : 0;

                //System.out.println(IO.write_FEN(fen_strings.get(i)) + " " + qi);

                double sig = SimpleTexelOptimiser.this.sigmoid(qi, K);

                pScore += (expected - sig) * (expected - sig);
            }
            synchronized (score) {
                score[0] += pScore;
            }
        }, threads, false);
        return score[0] / tasks;
    }


    public double sigmoid(double s, double K){
        return 1d / (1 + Math.exp(-K * s / 400));
    }

    public double sigmoidPrime(double s, double K) {
        double ex = Math.exp(-s * K / 400);
        return (K * ex) / (400 * (ex + 1) * (ex + 1));
    }

    public static int probabiltyToCentipawnAdvantage(double prob){
        return (int)(400 * Math.log10(prob / (1-prob)));
    }

    public static double centipawnAdvantageToProbability(int centipawns){
        return 1d / (1+Math.pow(10, - centipawns/400d));
    }

    public void compare(Evaluator ev1, Evaluator ev2, double eps){
        for(Board b:fen_strings){
            double e1 = ev1.evaluate(b);
            double e2 = ev2.evaluate(b);
            if(Math.abs(e1-e2) > eps){
                System.err.println(IO.write_FEN(b));
            }
        }
    }

    public void overview(){
        int promotions = 0;
        int goodCaps = 0;
        int equalCaps = 0;
        int badCaps = 0;
        int inCheck = 0;
        int givesCheck = 0;

        AdvancedEvaluator evaluator = new AdvancedEvaluator(new SimpleDecider());

        for(Board b:fen_strings){

            if(b.isInCheck(b.getActivePlayer())){
                inCheck ++;
            }

            for(Object m:b.getPseudoLegalMoves()){
                Move move = (Move)m;

                if(!b.isLegal(move)){
                    continue;
                }

                if(b.givesCheck(move)){
                    givesCheck++;
                }
                if(move.isCapture()){
                    double see = evaluator.staticExchangeEvaluation(b, move.getTo(), move.getPieceTo(), move.getFrom(), move.getPieceFrom(), b.getActivePlayer());
                    if(see > 0){
                        goodCaps++;
                    }else if(see == 0){
                        equalCaps ++;
                    }else{
                        badCaps ++;
                    }
                }
                if(move.isPromotion()){
                    promotions ++;
                }
            }
        }
        System.out.println(fen_strings.size() + " positions with:");
        System.out.format("%10s %-10s %n", inCheck , " position with king in check");
        System.out.format("%10s %-10s %n", givesCheck , " moves that give check");
        System.out.format("%10s %-10s %n", promotions , " promotions");
        System.out.format("%10s %-10s %n", goodCaps , " good captures");
        System.out.format("%10s %-10s %n", equalCaps , " equal captures");
        System.out.format("%10s %-10s %n", badCaps, " bad captures");
    }


    public static void main(String[] args) {
        SimpleTexelOptimiser tex = new SimpleTexelOptimiser();
        tex.readFile("resources/lichess-new-labeled.epd",
                     new FastBoard(),
                     2500000);

        AdvancedEvaluatorNew evaluator2 = new AdvancedEvaluatorNew(new SimpleDecider());



        double K = tex.computeK(evaluator2, 1E-9, 2.6485215, 100,1);
        tex.iterationGradientDescent(evaluator2, K, 0.01,12);


    }

}
