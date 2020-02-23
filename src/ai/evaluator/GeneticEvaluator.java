package ai.evaluator;

import ai.search.AdvancedSearch;
import board.FastBoard;
import board.setup.Setup;
import game.Game;
import ai.ordering.SystematicOrderer;
import ai.reducing.SimpleReducer;
import ai.search.AI;
import ai.tools.threads.Pool;
import ai.tools.threads.PoolFunction;
import io.IO;

import java.io.*;
import java.util.*;

public abstract class GeneticEvaluator<T extends GeneticEvaluator<T>> implements Evaluator {


    private double geneticScore;

    public double getGeneticScore() {
        return geneticScore;
    }

    public void setGeneticScore(double geneticScore) {
        this.geneticScore = geneticScore;
    }

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

    public static void evolve(ArrayList<GeneticEvaluator> geneticEvaluators,
                              AdvancedSearch ai,
                              int games,
                              int survivors,
                              double mutationStrength,
                              double crossoverStrength,
                              int threads){


        //<editor-fold desc="generating AIS">
        ai.setPrint_overview(false);
        HashMap<GeneticEvaluator, AI> ais = new HashMap<>();
        for(GeneticEvaluator ev:geneticEvaluators){
            AI a = duplicateAI(ai);
            ((AdvancedSearch) a).setEvaluator(ev);
            ais.put(ev, a);
        }
        //</editor-fold>

        //<editor-fold desc="formats">
        String head =           "┌──────────┬┬";
        String bottom =         "└──────────┴┴";
        String seperator =      "├──────────┼┼";
        String two_entries =         "│ %8s ││ ";
        String one_entries =         "│ %8s ││ ";
        String one_entries_string =         "│ %8s ││ ";
        for(int i = 0; i < geneticEvaluators.size(); i++){

            if(i == geneticEvaluators.size()-1){

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
        Object[] ar = new Object[2*geneticEvaluators.size() + 1];
        ar[0] = "AI";
        for(int i = 0; i < geneticEvaluators.size(); i++){
            ar[2*i+1] = i+1;
            ar[2*i+2] = "";
        }
        System.out.format(two_entries, ar);
        ar[0] = "";
        for(int i = 0; i < geneticEvaluators.size(); i++){
            ar[2*i+1] = "vs";
            ar[2*i+2] = "score";
        }
        System.out.println(seperator);
        System.out.format(two_entries, ar);

        System.out.println(seperator);
        //</editor-fold>

        //<editor-fold desc="iterations">
        ArrayList<GeneticEvaluator> temp = new ArrayList<>(geneticEvaluators);
        for(int i = 0; i < geneticEvaluators.size(); i++){
            geneticEvaluators.get(i).setGeneticScore(0);
        }

        Pool pool = new Pool(threads);

        for(int g = 0; g < games; g++){

            Collections.shuffle(geneticEvaluators);

            PoolFunction function = index -> {
                double score = playAMatch(ais.get(geneticEvaluators.get(index * 2)), ais.get(geneticEvaluators.get(index * 2 + 1)));
                geneticEvaluators.get(index*2).geneticScore += score;
                geneticEvaluators.get(index*2 + 1).geneticScore -= score;
            };
            pool.execute(function, geneticEvaluators.size() / 2, true);

            ar = new Object[geneticEvaluators.size()*2 + 1];
            ar[0] = "game " + (g+1);
            for(int i = 0; i < geneticEvaluators.size(); i++){
                ar[i*2+2] = "["+IO.doubleToString((double)temp.get(i).geneticScore,1)+"]";

                int partnerIndex;
                if(geneticEvaluators.indexOf(temp.get(i)) % 2 == 0){
                    partnerIndex = geneticEvaluators.indexOf(temp.get(i)) +1;
                }else{
                    partnerIndex = geneticEvaluators.indexOf(temp.get(i)) -1;
                }
                int realIndex = temp.indexOf(geneticEvaluators.get(partnerIndex)) + 1;

                ar[i*2+1] =realIndex;
            }
            System.out.format("\r" + two_entries, ar);
        }
        //</editor-fold>#

        System.out.println(seperator);
        System.out.println(seperator);

        //<editor-fold desc="crossing">
        ar = new Object[1+geneticEvaluators.size()];
        ar[0] = "parent";
        geneticEvaluators.sort(Comparator.comparingDouble(o -> -(double) o.geneticScore));
        for(int i = 0; i < geneticEvaluators.size(); i++){
            ar[i+1] = "   -";
        }
        for(int i = survivors; i < geneticEvaluators.size(); i++){
            GeneticEvaluator v = geneticEvaluators.get(i);
            GeneticEvaluator parent = geneticEvaluators.get((int)(Math.random() * survivors));

            ar[temp.indexOf(geneticEvaluators.get(i))+1] = "   " + (temp.indexOf(parent) + 1);

            v.crossover(parent,crossoverStrength);
        }
        System.out.format(one_entries_string, ar);
        System.out.println(seperator);
        //</editor-fold>

        for(int i = 0; i < geneticEvaluators.size(); i++){
            GeneticEvaluator v = geneticEvaluators.get(i);
            //System.out.println(geneticEvaluators.get(i)[1] + "  " + geneticEvaluators.get(i)[0] + "  " + Arrays.toString(v.getEvolvableValues()));
            v.mutate(mutationStrength);
        }

        //<editor-fold desc="output">
        for(int i = 0; i < geneticEvaluators.get(0).getEvolvableValues().length; i++){
            ar = new Object[geneticEvaluators.size() + 1];
            ar[0] = "param " + (i+1);
            for(int n = 0; n < geneticEvaluators.size(); n++){
                GeneticEvaluator ev = geneticEvaluators.get(n);
                //ar[n+1] = IO.doubleToString(ev.getEvolvableValues()[i],2);
                ar[n+1] = ev.getEvolvableValues()[i];
            }
            System.out.format(one_entries, ar);
        }
        System.out.println(bottom);
        //</editor-fold>

    }

    public static ArrayList<GeneticEvaluator> generatePopulation(int count, GeneticEvaluator evaluator){
        ArrayList<GeneticEvaluator> res = new ArrayList<>();

        for(int i = 0; i < count;i ++){
            GeneticEvaluator gen1 = evaluator.copy();
            gen1.mutate(0.1);

            res.add(gen1);
        }

        return res;
    }

    public static double playAGame(AI ai1, AI ai2){


        Game game = new Game(new FastBoard(Setup.DEFAULT), ai1, ai2);
        NoahEvaluator evaluator = new NoahEvaluator();
        final int[] moves = {0};
        game.addBoardChangedListener((m) -> {
            moves[0]++;
            if(moves[0] % 2 == 0)
                //System.out.print("\r move: " + moves[0] +  "   rated: " + evaluator.evaluate(game.getBoard()));
            if(moves[0] >= 120){
                game.interrupt();
            }
        });
        game.move(null);
        if(game.getBoard().isDraw()){
            return 0;
        }

        double evaluation = evaluator.evaluate(game.getBoard());
        double score = evaluation > 0 ? 0.2:(evaluation < 0 ? -0.2d:0);

        if(evaluation == 0){
            return 0;
        } else if (Math.abs(evaluation) < 1000) {
            return 0.2 * evaluation / Math.abs(evaluation);
        } else{
            return 1 * evaluation / Math.abs(evaluation);
        }
    }

    public static double playAMatch(AI ai1, AI ai2){
        return playAGame(ai1,ai2) - playAGame(ai2, ai1);
    }

    public static AdvancedSearch duplicateAI(AdvancedSearch ai){
        AdvancedSearch search = new AdvancedSearch(ai.getEvaluator(), ai.getOrderer(), ai.getReducer(), ai.getLimit_flag(), ai.getLimit());
        search.setPrint_overview(ai.isPrint_overview());
        search.setUse_LMR(ai.isUse_LMR());
        search.setUse_transposition(ai.isUse_transposition());
        search.setUse_killer_heuristic(ai.isUse_killer_heuristic());
        search.setUse_iteration(ai.isUse_iteration());
        return search;
    }

    public static GeneticEvaluator getEvaluatorFromEntry(Object[] o){
        return (GeneticEvaluator) ((AdvancedSearch)o[0]).getEvaluator();
    }

    public static void writePopulation(String file, ArrayList<GeneticEvaluator> ais){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for(GeneticEvaluator o:ais){
                writer.write(Arrays.toString(o.getEvolvableValues()) + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<GeneticEvaluator> readPopulation(String file, GeneticEvaluator template){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;

            ArrayList<GeneticEvaluator> objects = new ArrayList<>();

            while((line=reader.readLine()) != null){
                String k = line.substring(1,line.indexOf("]")-1);
                String[] split = k.split(",");
                double[] out = new double[split.length];
                for(int i = 0; i < out.length;i++){
                    out[i] = Double.parseDouble(split[i].trim());
                }
                GeneticEvaluator g = template.copy();
                g.setEvolvableValues(out);
                objects.add(g);
            }

            reader.close();

            return objects;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        ArrayList<GeneticEvaluator> population = readPopulation("noahEvaluator_2.population", new NoahEvaluator());

//        writePopulation("test.population", population);
//
//        population = readPopulation("test.population", new NoahEvaluator());
//        System.out.println(Arrays.toString(getEvaluatorFromEntry(population.get(0)).getEvolvableValues()));


        AdvancedSearch AdvancedSearch = new AdvancedSearch(new NoahEvaluator2(), new SystematicOrderer(), new SimpleReducer(), 2,6);
        AdvancedSearch.setUse_null_moves(true);
        AdvancedSearch.setUse_LMR(true);
        AdvancedSearch.setUse_killer_heuristic(true);



        for(int i = 0; i < 1; i++){
            evolve(population, AdvancedSearch,16, 32, 0,0, 4);
            //writePopulation("noahEvaluator_2.population", population);
        }
//        System.out.println("Finished");
    }

}
