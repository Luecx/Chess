package ai.evaluator;

import board.Board;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

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
     * @param color
     * @return
     */
    public abstract double staticExchangeEvaluation(Board board, int toSquare, int target, int fromSquare, int attacker, int color);


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

    public static void compareParams(String s1, String s2){
        String[] p1 = s1.split("\n");
        String[] p2 = s2.split("\n");

        HashMap<String, Double> m1 = new HashMap<>();
        HashMap<String, Double> m2 = new HashMap<>();

        for(String s:p1){
            s = s.trim();
            s = s.replaceAll("[(static)(double)(private)(public)(final)(protected) ;]","");
            s = s.trim();
            //System.out.println("[A] "+s);
            if(s.contains("=") && s.startsWith("PARAMETER")){
                m1.put(s.split("=")[0], Double.valueOf(s.split("=")[1]));
            }
        }

        for(String s:p2){
            s = s.trim();
            s = s.replaceAll("[(static)(double)(private)(public)(final)(protected) ;]","");
            s = s.trim();
            //System.err.println("[B] "+s);
            if(s.contains("=") && s.startsWith("PARAMETER")){
                m2.put(s.split("=")[0], Double.valueOf(s.split("=")[1]));
            }
        }

        String format = "%-60s %-60s %n";



        for(String param:new ArrayList<>(m1.keySet())){
            if(m2.keySet().contains(param)){
                if(!m1.get(param).equals(m2.get(param))){
                    System.out.format(format, param + "=" + m1.get(param),param + "=" + m2.get(param));
                }
                m1.remove(param);
                m2.remove(param);
            }
        }

        for(String param:m1.keySet()){
            System.out.format(format, param + "=" + m1.get(param), "");
        }
        for(String param:m2.keySet()){
            System.out.format(format, "",param + "=" + m2.get(param));
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
       //createFunction_getEvolvableValues(AdvancedEvaluator.class);
        //createFunction_setEvolvableValues(AdvancedEvaluator.class);
        compareParams("private double PARAMETER_PAWN_TABLE_FACTOR_EARLY = 44.0D;\n"
                      + "    private double PARAMETER_PAWN_CONNECTED_EARLY = 5.0D;\n"
                      + "    private double PARAMETER_PAWN_PASSED_EARLY = 38.0D;\n"
                      + "    private double PARAMETER_PAWN_ISOLATED_EARLY = -18.0D;\n"
                      + "    private double PARAMETER_PAWN_DOUBLED_EARLY = -20.0D;\n"
                      + "    private double PARAMETER_PAWN_CONNECTED_PASSED_EARLY = 30.0D;\n"
                      + "    private double PARAMETER_PAWN_DOUBLED_ISOLATED_EARLY = -20.0D;\n"
                      + "    private double PARAMETER_KNIGHT_TABLE_FACTOR_EARLY = 44.0D;\n"
                      + "    private double PARAMETER_KNIGHT_VALUE_EARLY = 293.0D;\n"
                      + "    private double PARAMETER_KNIGHT_VISIBILITY_EARLY = 10.0D;\n"
                      + "    private double PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_EARLY = -7.0D;\n"
                      + "    private double PARAMETER_KNIGHT_TRAPPED_EARLY = -18.0D;\n"
                      + "    private double PARAMETER_ROOK_TABLE_FACTOR_EARLY = 90.0D;\n"
                      + "    private double PARAMETER_ROOK_VALUE_EARLY = 424.0D;\n"
                      + "    private double PARAMETER_ROOK_VISIBILITY_EARLY = 9.0D;\n"
                      + "    private double PARAMETER_ROOK_VISIBILITY_PAWN_COVER_EARLY = 0.0D;\n"
                      + "    private double PARAMETER_ROOK_TRAPPED_EARLY = -10.0D;\n"
                      + "    private double PARAMETER_ROOK_KING_LINE_EARLY = 24.0D;\n"
                      + "    private double PARAMETER_ROOK_HALF_OPEN_EARLY = 30.0D;\n"
                      + "    private double PARAMETER_ROOK_OPEN_EARLY = 15.0D;\n"
                      + "    private double PARAMETER_BISHOP_TABLE_FACTOR_EARLY = 44.0D;\n"
                      + "    private double PARAMETER_BISHOP_VALUE_EARLY = 301.0D;\n"
                      + "    private double PARAMETER_BISHOP_VISIBILITY_EARLY = 6.0D;\n"
                      + "    private double PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_EARLY = 0.0D;\n"
                      + "    private double PARAMETER_BISHOP_TRAPPED_EARLY = -35.0D;\n"
                      + "    private double PARAMETER_BISHOP_DOUBLED_EARLY = 52.0D;\n"
                      + "    private double PARAMETER_BISHOP_CLOSED_PENALTY_EARLY = -20.0D;\n"
                      + "    private double PARAMETER_BISHOP_OPEN_BONUS_EARLY = 20.0D;\n"
                      + "    private double PARAMETER_QUEEN_TABLE_FACTOR_EARLY = 43.0D;\n"
                      + "    private double PARAMETER_QUEEN_VALUE_EARLY = 870.0D;\n"
                      + "    private double PARAMETER_QUEEN_VISIBILITY_EARLY = 1.0D;\n"
                      + "    private double PARAMETER_QUEEN_TRAPPED_EARLY = -1.0D;\n"
                      + "    private double PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY = 0.0D;\n"
                      + "    private double PARAMETER_KING_TABLE_FACTOR_EARLY = 44.0D;\n"
                      + "    private double PARAMETER_KING_SAFETY_1_EARLY = 10.0D;\n"
                      + "    private double PARAMETER_KING_SAFETY_2_EARLY = 25.0D;\n"
                      + "    private double PARAMETER_KING_SAFETY_3_EARLY = -10.0D;\n"
                      + "    private double PARAMETER_KING_PAWN_SHIELD_EARLY = 15.0D;\n"
                      + "    private double PARAMETER_PAWN_TABLE_FACTOR_LATE = 44.0D;\n"
                      + "    private double PARAMETER_PAWN_PASSED_LATE = 65.0D;\n"
                      + "    private double PARAMETER_PAWN_ISOLATED_LATE = -13.0D;\n"
                      + "    private double PARAMETER_PAWN_DOUBLED_LATE = -29.0D;\n"
                      + "    private double PARAMETER_PAWN_CONNECTED_LATE = 15.0D;\n"
                      + "    private double PARAMETER_PAWN_CONNECTED_PASSED_LATE = 90.0D;\n"
                      + "    private double PARAMETER_PAWN_DOUBLED_ISOLATED_LATE = -40.0D;\n"
                      + "    private double PARAMETER_ROOK_TABLE_FACTOR_LATE = 44.0D;\n"
                      + "    private double PARAMETER_ROOK_VALUE_LATE = 470.0D;\n"
                      + "    private double PARAMETER_ROOK_VISIBILITY_LATE = 9.0D;\n"
                      + "    private double PARAMETER_ROOK_VISIBILITY_PAWN_COVER_LATE = 0.0D;\n"
                      + "    private double PARAMETER_ROOK_TRAPPED_LATE = -40.0D;\n"
                      + "    private double PARAMETER_ROOK_KING_LINE_LATE = 24.0D;\n"
                      + "    private double PARAMETER_ROOK_HALF_OPEN_LATE = 21.0D;\n"
                      + "    private double PARAMETER_ROOK_OPEN_LATE = 2.0D;\n"
                      + "    private double PARAMETER_KNIGHT_TABLE_FACTOR_LATE = 44.0D;\n"
                      + "    private double PARAMETER_KNIGHT_VALUE_LATE = 293.0D;\n"
                      + "    private double PARAMETER_KNIGHT_VISIBILITY_LATE = 8.0D;\n"
                      + "    private double PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_LATE = -5.0D;\n"
                      + "    private double PARAMETER_KNIGHT_TRAPPED_LATE = -18.0D;\n"
                      + "    private double PARAMETER_BISHOP_TABLE_FACTOR_LATE = 44.0D;\n"
                      + "    private double PARAMETER_BISHOP_VALUE_LATE = 301.0D;\n"
                      + "    private double PARAMETER_BISHOP_VISIBILITY_LATE = 6.0D;\n"
                      + "    private double PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_LATE = 0.0D;\n"
                      + "    private double PARAMETER_BISHOP_TRAPPED_LATE = -21.0D;\n"
                      + "    private double PARAMETER_BISHOP_DOUBLED_LATE = 52.0D;\n"
                      + "    private double PARAMETER_BISHOP_CLOSED_PENALTY_LATE = -10.0D;\n"
                      + "    private double PARAMETER_BISHOP_OPEN_BONUS_LATE = 30.0D;\n"
                      + "    private double PARAMETER_QUEEN_TABLE_FACTOR_LATE = 43.0D;\n"
                      + "    private double PARAMETER_QUEEN_VALUE_LATE = 1050.0D;\n"
                      + "    private double PARAMETER_QUEEN_VISIBILITY_LATE = 5.0D;\n"
                      + "    private double PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_LATE = 0.0D;\n"
                      + "    private double PARAMETER_QUEEN_TRAPPED_LATE = -40.0D;\n"
                      + "    private double PARAMETER_KING_TABLE_FACTOR_LATE = 44.0D;\n"
                      + "    private double PARAMETER_KING_SAFETY_1_LATE = 10.0D;\n"
                      + "    private double PARAMETER_KING_SAFETY_2_LATE = 25.0D;\n"
                      + "    private double PARAMETER_KING_SAFETY_3_LATE = -1.0D;\n"
                      + "    private double PARAMETER_KING_PAWN_SHIELD_LATE = 2.0D;",

                      "  private double PARAMETER_PAWN_TABLE_FACTOR_EARLY =                               44;\n"
                      + "    private double PARAMETER_PAWN_CONNECTED_EARLY =                                  5;\n"
                      + "    private double PARAMETER_PAWN_PASSED_EARLY =                                     38;\n"
                      + "    private double PARAMETER_PAWN_ISOLATED_EARLY =                                   -18;\n"
                      + "    private double PARAMETER_PAWN_DOUBLED_EARLY =                                    -20;\n"
                      + "    private double PARAMETER_PAWN_CONNECTED_PASSED_EARLY =                           30;\n"
                      + "    private double PARAMETER_PAWN_DOUBLED_ISOLATED_EARLY =                           -20;\n"
                      + "\n"
                      + "\n"
                      + "    private double PARAMETER_KNIGHT_TABLE_FACTOR_EARLY =                             44;\n"
                      + "    private double PARAMETER_KNIGHT_VALUE_EARLY =                                    293;\n"
                      + "    private double PARAMETER_KNIGHT_VISIBILITY_EARLY =                               10;\n"
                      + "    private double PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_EARLY =                    -7;\n"
                      + "    private double PARAMETER_KNIGHT_TRAPPED_EARLY =                                  -18;\n"
                      + "\n"
                      + "    private double PARAMETER_ROOK_TABLE_FACTOR_EARLY =                               90;\n"
                      + "    private double PARAMETER_ROOK_VALUE_EARLY =                                      424;\n"
                      + "    private double PARAMETER_ROOK_VISIBILITY_EARLY =                                 9;\n"
                      + "    private double PARAMETER_ROOK_VISIBILITY_PAWN_COVER_EARLY =                      0;\n"
                      + "    private double PARAMETER_ROOK_TRAPPED_EARLY =                                    -10;\n"
                      + "    private double PARAMETER_ROOK_KING_LINE_EARLY =                                  24;\n"
                      + "    private double PARAMETER_ROOK_HALF_OPEN_EARLY =                                  30;\n"
                      + "    private double PARAMETER_ROOK_OPEN_EARLY =                                       15;\n"
                      + "\n"
                      + "    private double PARAMETER_BISHOP_TABLE_FACTOR_EARLY =                             44;\n"
                      + "    private double PARAMETER_BISHOP_VALUE_EARLY =                                    301;\n"
                      + "    private double PARAMETER_BISHOP_VISIBILITY_EARLY =                               6;\n"
                      + "    private double PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_EARLY =                    0;\n"
                      + "    private double PARAMETER_BISHOP_TRAPPED_EARLY =                                  -35;\n"
                      + "    private double PARAMETER_BISHOP_DOUBLED_EARLY =                                  52;\n"
                      + "    private double PARAMETER_BISHOP_CLOSED_PENALTY_EARLY =                           -20;\n"
                      + "    private double PARAMETER_BISHOP_OPEN_BONUS_EARLY =                               20;\n"
                      + "\n"
                      + "    private double PARAMETER_QUEEN_TABLE_FACTOR_EARLY =                              43;\n"
                      + "    private double PARAMETER_QUEEN_VALUE_EARLY =                                     800\n"
                      + "    private double PARAMETER_QUEEN_VISIBILITY_EARLY =                                1;\n"
                      + "    private double PARAMETER_QUEEN_TRAPPED_EARLY =                                   -1;\n"
                      + "    private double PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_EARLY =                     0;\n"
                      + "\n"
                      + "    private double PARAMETER_KING_TABLE_FACTOR_EARLY =                               44;\n"
                      + "    private double PARAMETER_KING_SAFETY_1_EARLY =                                   10;\n"
                      + "    private double PARAMETER_KING_SAFETY_2_EARLY =                                   25;\n"
                      + "    private double PARAMETER_KING_SAFETY_3_EARLY =                                   -10;\n"
                      + "    private double PARAMETER_KING_PAWN_SHIELD_EARLY =                                15;\n"
                      + "\n"
                      + "\n"
                      + "\n"
                      + "\n"
                      + "\n"
                      + "    private double PARAMETER_PAWN_TABLE_FACTOR_LATE =                                44;\n"
                      + "    private double PARAMETER_PAWN_PASSED_LATE =                                      65;\n"
                      + "    private double PARAMETER_PAWN_ISOLATED_LATE =                                    -13;\n"
                      + "    private double PARAMETER_PAWN_DOUBLED_LATE =                                     -29;\n"
                      + "    private double PARAMETER_PAWN_CONNECTED_LATE =                                   15;\n"
                      + "    private double PARAMETER_PAWN_CONNECTED_PASSED_LATE =                            90;\n"
                      + "    private double PARAMETER_PAWN_DOUBLED_ISOLATED_LATE =                            -40;\n"
                      + "\n"
                      + "    private double PARAMETER_ROOK_TABLE_FACTOR_LATE =                                44;\n"
                      + "    private double PARAMETER_ROOK_VALUE_LATE =                                       470\n"
                      + "    private double PARAMETER_ROOK_VISIBILITY_LATE =                                  9;\n"
                      + "    private double PARAMETER_ROOK_VISIBILITY_PAWN_COVER_LATE =                       0;\n"
                      + "    private double PARAMETER_ROOK_TRAPPED_LATE =                                     -40;\n"
                      + "    private double PARAMETER_ROOK_KING_LINE_LATE =                                   24;\n"
                      + "    private double PARAMETER_ROOK_HALF_OPEN_LATE =                                   21;\n"
                      + "    private double PARAMETER_ROOK_OPEN_LATE =                                        2;\n"
                      + "\n"
                      + "    private double PARAMETER_KNIGHT_TABLE_FACTOR_LATE =                              44;\n"
                      + "    private double PARAMETER_KNIGHT_VALUE_LATE =                                     293;\n"
                      + "    private double PARAMETER_KNIGHT_VISIBILITY_LATE =                                8;\n"
                      + "    private double PARAMETER_KNIGHT_VISIBILITY_PAWN_COVER_LATE =                     -5;\n"
                      + "    private double PARAMETER_KNIGHT_TRAPPED_LATE =                                   -18;\n"
                      + "\n"
                      + "    private double PARAMETER_BISHOP_TABLE_FACTOR_LATE =                              44;\n"
                      + "    private double PARAMETER_BISHOP_VALUE_LATE =                                     301;\n"
                      + "    private double PARAMETER_BISHOP_VISIBILITY_LATE =                                6;\n"
                      + "    private double PARAMETER_BISHOP_VISIBILITY_PAWN_COVER_LATE =                     0;\n"
                      + "    private double PARAMETER_BISHOP_TRAPPED_LATE =                                   -21;\n"
                      + "    private double PARAMETER_BISHOP_DOUBLED_LATE =                                   52;\n"
                      + "    private double PARAMETER_BISHOP_CLOSED_PENALTY_LATE =                           -10;\n"
                      + "    private double PARAMETER_BISHOP_OPEN_BONUS_LATE =                                30;\n"
                      + "\n"
                      + "    private double PARAMETER_QUEEN_TABLE_FACTOR_LATE =                               43;\n"
                      + "    private double PARAMETER_QUEEN_VALUE_LATE =                                      1000\n"
                      + "    private double PARAMETER_QUEEN_VISIBILITY_LATE =                                 5;\n"
                      + "    private double PARAMETER_QUEEN_VISIBILITY_PAWN_COVER_LATE =                      0;\n"
                      + "    private double PARAMETER_QUEEN_TRAPPED_LATE =                                    -40;\n"
                      + "\n"
                      + "    private double PARAMETER_KING_TABLE_FACTOR_LATE =                                44;\n"
                      + "    private double PARAMETER_KING_SAFETY_1_LATE =                                    10;\n"
                      + "    private double PARAMETER_KING_SAFETY_2_LATE =                                    25;\n"
                      + "    private double PARAMETER_KING_SAFETY_3_LATE =                                    -1;\n"
                      + "    private double PARAMETER_KING_PAWN_SHIELD_LATE =                                 2;"
                      );
    }

}
