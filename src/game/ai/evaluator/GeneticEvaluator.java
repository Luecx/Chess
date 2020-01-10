package game.ai.evaluator;

import board.Board;
import board.SlowBoard;
import board.setup.Setup;
import game.Game;
import game.ai.ordering.SimpleOrderer;
import game.ai.ordering.SystematicOrderer;
import game.ai.reducing.SimpleReducer;
import game.ai.search.AI;
import game.ai.search.AlphaBeta;
import game.ai.search.PVSearch;
import game.ai.tools.tensor.Tensor2D;
import game.ai.tools.tensor.Tensor3D;

import java.lang.reflect.Array;
import java.util.*;

public abstract class GeneticEvaluator<T extends GeneticEvaluator<T>> implements Evaluator {


    public void mutate(double mutationStrength){
        double[] vals = this.getEvolvableValues();
        Random r = new Random((long)(Math.random() * Long.MAX_VALUE));
        for(int i = 0; i < vals.length; i++){
            vals[i] += r.nextGaussian() * mutationStrength * vals[i];
        }
        this.setEvolvableValues(vals);
    }

    public void crossover(GeneticEvaluator parent, double strength){
        double[] vals = this.getEvolvableValues();
        double[] other = parent.getEvolvableValues();
        for(int i = 0; i < vals.length; i++){
            vals[i] += (other[i] - vals[i]) * strength;
        }
        this.setEvolvableValues(vals);
    }


    public abstract double[] getEvolvableValues();

    public abstract void setEvolvableValues(double[] ar);

    public abstract T copy();


    public static void evolve(ArrayList<Object[]> ais, int games, int survivors, double mutationStrength, double crossoverStrength){
        for(int i = 0; i < ais.size(); i++){
            ais.get(i)[1] = new Double(0);
        }
        for(int g = 0; g < games; g++){
            Collections.shuffle(ais);


            for(int i = 0; i < ais.size(); i+=2){
                double score = playAMatch((PVSearch)ais.get(i)[0], (PVSearch)ais.get(i + 1)[0]);
                ais.get(i)[1] = (Double)ais.get(i)[1] + (double) score;
                ais.get(i+1)[1] = (Double) ais.get(i + 1)[1] - (double) score;
            }

        }
        ais.sort(Comparator.comparingDouble(o -> -(double) o[1]));

        for(int i = survivors; i < ais.size(); i++){
            GeneticEvaluator v = (GeneticEvaluator)((PVSearch)ais.get(i)[0]).getEvaluator();
            GeneticEvaluator parent = (GeneticEvaluator)((PVSearch)ais.get((int)(Math.random() * survivors))[0]).getEvaluator();

            v.crossover(parent,crossoverStrength);
        }
        for(int i = 0; i < ais.size(); i++){
            GeneticEvaluator v = (GeneticEvaluator)((PVSearch)ais.get(i)[0]).getEvaluator();
            System.out.println(ais.get(i)[1] + "  " + ais.get(i)[0] + "  " + Arrays.toString(v.getEvolvableValues()));
            v.mutate(mutationStrength);
        }
        System.out.println();

    }

    public static ArrayList<Object[]> generatePopulation(int count, int depth, int qDepth, GeneticEvaluator evaluator){
        ArrayList<Object[]> res = new ArrayList<>();

        for(int i = 0; i < count;i ++){
            GeneticEvaluator gen1 = evaluator.copy();
            gen1.mutate(0.1);
            PVSearch ai1 = new PVSearch(gen1, new SystematicOrderer(), new SimpleReducer(), 2, depth, qDepth);
            ai1.setUse_null_moves(true);
            ai1.setUse_LMR(true);
            ai1.setPrint_overview(false);
            ai1.setUse_transposition(false);

            res.add(new Object[]{ai1, new Double(0)});
        }

        return res;
    }

    public static double playAGame(AI ai1, AI ai2){
        Game game = new Game(new SlowBoard(Setup.DEFAULT), ai1, ai2);
        FinnEvaluator evaluator = new FinnEvaluator();
        final int[] moves = {0};
        game.addBoardChangedListener(new Runnable() {
            @Override
            public void run() {
                moves[0]++;
                if(moves[0] % 2 == 0)
                    System.out.print("\r move: " + moves[0] +  "   rated: " + evaluator.evaluate(game.getBoard()));
                if(moves[0] >= 120){
                    game.interrupt();
                }
            }
        });
        game.move(null);
        System.out.println();

        if(game.getBoard().isGameOver()){
            return game.getBoard().winner();
        }

        double evaluation = evaluator.evaluate(game.getBoard());

        double score = evaluation > 0 ? 0.2:(evaluation < 0 ? -0.2d:0);

        return score;
    }

    public static double playAMatch(PVSearch ai1, PVSearch ai2){
        return playAGame(ai1,ai2) - playAGame(ai2, ai1);
    }



    public static void main(String[] args) {
        ArrayList<Object[]> population = generatePopulation(20,6,4, new NoahEvaluator());

        evolve(population, 10, 4, 0.1,0.8);
        for(int i = 0; i < 1; i++)
        System.out.println("Finished");
    }

}
