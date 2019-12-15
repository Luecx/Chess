package game.ai.evaluator;

import board.Board;
import board.SlowBoard;
import game.Game;
import game.ai.ordering.SimpleOrderer;
import game.ai.search.AI;
import game.ai.search.AlphaBeta;
import game.ai.tools.tensor.Tensor2D;
import game.ai.tools.tensor.Tensor3D;

import java.lang.reflect.Array;
import java.util.*;

public class GeneticEvaluator implements Evaluator {



    private Tensor2D PAWN_VALUES = new Tensor2D(new double[][]{
            {0, 0, 0, 0, 0, 0, 0, 0},
            {50, 50, 50, 50, 50, 50, 50, 50},
            {10, 10, 20, 30, 30, 20, 10, 10},
            {5,  5, 10, 25, 25, 10,  5,  5},
            {0,  0,  0, 20, 20,  0,  0,  0},
            {5, -5,-10,  0,  0,-10, -5,  5},
            {5, 10, 10,-20,-20, 10, 10,  5},
            {0, 0, 0, 0, 0, 0, 0, 0}
    });

    private Tensor2D BISHOP_VALUES = new Tensor2D(new double[][]{
            {-20, -10, -10, -10, -10, -10, -10, -20},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-10, 0, 5, 10, 10, 5, 0, -10},
            {-10, 5, 5, 10, 10, 5, 5, -10},
            {-10, 0, 10, 10, 10, 10, 0, -10},
            {-10, 10, 10, 10, 10, 10, 10, -10},
            {-10, 5, 0, 0, 0, 0, 5, -10},
            {-20, -10, -10, -10, -10, -10, -10, -20,},
    });

    private Tensor2D ROOK_VALUES = new Tensor2D(new double[][]{
            {0, 0, 0, 0, 0, 0, 0, 0},
            {5, 10, 10, 10, 10, 10, 10, 5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {0, 0, 0, 5, 5, 0, 0, 0}
    });

    private Tensor2D KNIGHT_VALUES = new Tensor2D(new double[][]{
            {-50, -40, -30, -30, -30, -30, -40, -50},
            {-40, -20, 0, 0, 0, 0, -20, -40},
            {-30, 0, 10, 15, 15, 10, 0, -30},
            {-30, 5, 15, 20, 20, 15, 5, -30},
            {-30, 0, 15, 20, 20, 15, 0, -30},
            {-30, 5, 10, 15, 15, 10, 5, -30},
            {-40, -20, 0, 5, 5, 0, -20, -40},
            {-50, -40, -30, -30, -30, -30, -40, -50},
    });
    private Tensor2D QUEEN_VALUES = new Tensor2D(new double[][]{
            {-20, -10, -10, -5, -5, -10, -10, -20},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-10, 0, 5, 5, 5, 5, 0, -10},
            {-5, 0, 5, 5, 5, 5, 0, -5},
            {0, 0, 5, 5, 5, 5, 0, -5},
            {-10, 5, 5, 5, 5, 5, 0, -10},
            {-10, 0, 5, 0, 0, 0, 0, -10},
            {-20, -10, -10, -5, -5, -10, -10, -20}
    });

    public static final Tensor2D KING_VALUES_MID = new Tensor2D(new double[][]{
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-20, -30, -30, -40, -40, -30, -30, -20},
            {-10, -20, -20, -20, -20, -20, -20, -10},
            {20, 20, 0, 0, 0, 0, 20, 20},
            {20, 30, 10, 0, 0, 10, 30, 20}
    });

    private int[] EVALUATE_PRICE = new int[]{0, 100, 500, 320, 330, 900, 20000};
    private Tensor3D POSITION_PRICE = new Tensor3D(PAWN_VALUES, ROOK_VALUES, KNIGHT_VALUES, BISHOP_VALUES, QUEEN_VALUES);


    @Override
    public double evaluate(Board board) {
        int ev = 0;
        for (int i = 0; i < 8; i++) {
            for (int n = 0; n < 8; n++) {

                int v = ((board.getPiece((byte) i, (byte) n)));
                int b = v > 0 ? 1 : -1;

                if (v != 0) {
                    ev += b * EVALUATE_PRICE[Math.abs(v)];
                    if (Math.abs(v) < 6) {
                        if (v > 0) {
                            ev += (b * POSITION_PRICE.get(Math.abs(v) - 1,7-n,i));
                        } else {
                            ev += (b * POSITION_PRICE.get(Math.abs(v) - 1,n,i));
                        }
                    } else {
                        if (v > 0) {
                            ev += (b * KING_VALUES_MID.get(7-n,i));
                        } else {
                            ev += (b * KING_VALUES_MID.get(n,i));
                        }
                    }
                }
            }
        }
        return ev;
    }

    public void mutate(double mutationStrength) {
        Random r = new Random((int)(Math.random() * 10000000));
        for(int i = 0; i < EVALUATE_PRICE.length; i++){
            EVALUATE_PRICE[i] *= (1+r.nextGaussian() * mutationStrength);
        }
    }

    public static void evolve(ArrayList<Object[]> ais, int games, int survivors, double mutationStrength, double crossoverStrength){
        for(int i = 0; i < ais.size(); i++){
            ais.get(i)[1] = 0;
        }
        for(int g = 0; g < games; g++){
            Collections.shuffle(ais);


            for(int i = 0; i < ais.size(); i+=2){
                double score = playAMatch((AlphaBeta)ais.get(i)[0], (AlphaBeta)ais.get(i + 1)[0]);
                ais.get(i)[1] = (Integer)ais.get(i)[1] + score;
                ais.get(i+1)[1] = (Integer)ais.get(i+1)[1] - score;
            }

        }
        ais.sort(Comparator.comparingDouble(o -> (double) o[1]));

        for(int i = survivors; i < ais.size(); i++){
            GeneticEvaluator v = (GeneticEvaluator)((AlphaBeta)ais.get(i)[0]).getEvaluator();
            GeneticEvaluator parent = (GeneticEvaluator)((AlphaBeta)ais.get((int)(Math.random() * survivors))[0]).getEvaluator();

            v.crossover(parent,crossoverStrength);
        }
        for(int i = 0; i < ais.size(); i++){


            GeneticEvaluator v = (GeneticEvaluator)((AlphaBeta)ais.get(i)[0]).getEvaluator();
            System.out.println(ais.get(i)[1] + "  " + ais.get(i)[0] + "  " + Arrays.toString(v.getEVALUATE_PRICE()));
            v.mutate(mutationStrength);
        }
        System.out.println();

    }

    public static ArrayList<Object[]> generatePopulation(int count, int depth, int qDepth){
        ArrayList<Object[]> res = new ArrayList<>();

        for(int i = 0; i < count;i ++){
            GeneticEvaluator gen1 = new GeneticEvaluator();
            gen1.mutate(0.3);
            AlphaBeta ai1 = new AlphaBeta(gen1, new SimpleOrderer(), depth, qDepth);
            res.add(new Object[]{ai1, 0});
        }

        return res;
    }

    public static double playAGame(AlphaBeta ai1, AlphaBeta ai2){
        Game game = new Game(new SlowBoard(), ai1, ai2);
        FinnEvaluator evaluator = new FinnEvaluator();
        final int[] moves = {0};
        game.addBoardChangedListener(new Runnable() {
            @Override
            public void run() {
                moves[0]++;
                if(moves[0] % 2 == 0)
                    System.out.print("\r move: " + moves[0] +  "   rated: " + evaluator.evaluate(game.getBoard()));
                if(moves[0] >= 60){
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
        double score = evaluation > 0 ? 0.5:(evaluation < 0 ? -0.5d:0);

        return score;
    }

    public static double playAMatch(AlphaBeta ai1, AlphaBeta ai2){
        return playAGame(ai1,ai2) - playAGame(ai2, ai1);
    }


    public void crossover(GeneticEvaluator parent, double strength){
        for(int i = 0; i < EVALUATE_PRICE.length; i++){
            EVALUATE_PRICE[i] += (parent.EVALUATE_PRICE[i] - EVALUATE_PRICE[i]) * strength;
        }
    }

    public static void main(String[] args) {
        ArrayList<Object[]> population = generatePopulation(10,4,2);
        evolve(population, 1, 4, 0.1,0.8);
    }

    public int[] getEVALUATE_PRICE() {
        return EVALUATE_PRICE;
    }
}
