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
import io.IO;
import io.Testing;

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


        //<editor-fold desc="formats">
        String head =           "┌──────────┬┬";
        String bottom =         "└──────────┴┴";
        String seperator =      "├──────────┼┼";
        String two_entries =         "│ %8s ││ ";
        String one_entries =         "│ %8s ││ ";
        String one_entries_string =         "│ %8s ││ ";
        for(int i = 0; i < ais.size(); i++){

            if(i == ais.size()-1){

                seperator +=        "───────────┤";
                head +=        "───────────┐";
                bottom +=        "───────────┘";
            }else{

                seperator +=        "───────────┼";
                head +=        "───────────┬";
                bottom +=        "───────────┴";
            }
            one_entries_string += "%-9s │ ";
            one_entries += " %-+7.2f  │ ";
            two_entries += "%-2s %6s │ ";
        }
        two_entries += "\n";
        one_entries += "\n";
        one_entries_string += "\n";
        System.out.println(head);
        Object[] ar = new Object[2*ais.size() + 1];
        ar[0] = "AI";
        for(int i = 0; i < ais.size(); i++){
            ar[2*i+1] = i+1;
            ar[2*i+2] = "";
        }
        System.out.format(two_entries, ar);
        ar[0] = "";
        for(int i = 0; i < ais.size(); i++){
            ar[2*i+1] = "vs";
            ar[2*i+2] = "score";
        }
        System.out.println(seperator);
        System.out.format(two_entries, ar);

        System.out.println(seperator);
        //</editor-fold>

        //<editor-fold desc="iterations">
        ArrayList<Object[]> temp = new ArrayList<>(ais);
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



            ar = new Object[ais.size()*2 + 1];
            ar[0] = "game " + (g+1);
            for(int i = 0; i < ais.size(); i++){
                ar[i*2+2] = "["+IO.doubleToString((double)temp.get(i)[1],1)+"]";

                int partnerIndex;
                if(ais.indexOf(temp.get(i)) % 2 == 0){
                    partnerIndex = ais.indexOf(temp.get(i)) +1;
                }else{
                    partnerIndex = ais.indexOf(temp.get(i)) -1;
                }
                int realIndex = temp.indexOf(ais.get(partnerIndex)) + 1;

                ar[i*2+1] =realIndex;
            }
            System.out.format("\r" + two_entries, ar);
        }
        //</editor-fold>#

        System.out.println(seperator);
        System.out.println(seperator);

        //<editor-fold desc="crossing">
        ar = new Object[1+ais.size()];
        ar[0] = "parent";
        ais.sort(Comparator.comparingDouble(o -> -(double) o[1]));
        for(int i = 0; i < ais.size(); i++){
            ar[i+1] = "   -";
        }
        for(int i = survivors; i < ais.size(); i++){


            GeneticEvaluator v = (GeneticEvaluator)((PVSearch)ais.get(i)[0]).getEvaluator();
            Object[] parentArray = ais.get((int)(Math.random() * survivors));
            GeneticEvaluator parent = (GeneticEvaluator)((PVSearch)parentArray[0]).getEvaluator();

            ar[temp.indexOf(ais.get(i))+1] = "   " + (temp.indexOf(parentArray) + 1);

            v.crossover(parent,crossoverStrength);
        }
        System.out.format(one_entries_string, ar);
        System.out.println(seperator);
        //</editor-fold>

        for(int i = 0; i < ais.size(); i++){
            GeneticEvaluator v = (GeneticEvaluator)((PVSearch)ais.get(i)[0]).getEvaluator();
            //System.out.println(ais.get(i)[1] + "  " + ais.get(i)[0] + "  " + Arrays.toString(v.getEvolvableValues()));
            v.mutate(mutationStrength);
        }

        //<editor-fold desc="output">
        for(int i = 0; i < ((GeneticEvaluator)((PVSearch)ais.get(0)[0]).getEvaluator()).getEvolvableValues().length; i++){
            ar = new Object[ais.size() + 1];
            ar[0] = "param " + (i+1);
            for(int n = 0; n < ais.size(); n++){
                GeneticEvaluator ev = (GeneticEvaluator)((PVSearch)ais.get(n)[0]).getEvaluator();
                //ar[n+1] = IO.doubleToString(ev.getEvolvableValues()[i],2);
                ar[n+1] = ev.getEvolvableValues()[i];
            }
            System.out.format(one_entries, ar);
        }
        System.out.println(bottom);
        //</editor-fold>

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
        //System.out.println();

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
        ArrayList<Object[]> population = generatePopulation(14,6,4, new NoahEvaluator());

        for(int i = 0; i < 5; i++){
            evolve(population, 6, 2, 0.1,0.8);
        }
        System.out.println("Finished");
    }

}
