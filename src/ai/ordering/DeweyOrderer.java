package ai.ordering;

import ai.evaluator.AdvancedMidGameEvaluator;
import ai.tools.tables.CounterMoveTable;
import ai.tools.tables.KillerTable;
import ai.tools.transpositions.TranspositionEntry;
import ai.tools.transpositions.TranspositionTable;
import board.Board;
import board.FastBoard;
import board.bitboards.BitBoard;
import board.moves.Move;
import board.setup.Setup;
import io.IO;
import io.UCI;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DeweyOrderer implements Orderer {

    private long[] priorityList = {(long) 1e18, (long) 1e16, (long) 1e14, (long) 1e12, (long)1e10, (long)1e8,(long)1e6};

    private int pvPriority = 0;
    private int goodCapturePriority = 1;
    private int killerPriority = 2;
    private int badCapturePriority = 3;
    private int nonCapturePriority = 4;

    private void setPriority(List<Move> collection,
                             int depth,
                             Board board,
                             boolean pvNode,
                             KillerTable killerTable,
                             TranspositionTable transpositionTable,
                             CounterMoveTable counterMoveTable) {

        Move lastMove = null;
        if(!board.getMoveHistory().isEmpty()){
            lastMove = (Move) board.getMoveHistory().lastElement();
        }


        //this bit has to be first
        for (Move m : collection) {
            long priority = 0;
            if (!m.isCapture() && counterMoveTable != null && lastMove != null) {
                m.setOrderPriority((int)counterMoveTable.get(
                        Math.abs(lastMove.getPieceFrom())-1,
                        lastMove.getTo(),
                        Math.abs(m.getPieceFrom())-1,
                        m.getTo()));
            } else {
                NoahOrderer.setOrderPriority(m, board);
            }


            if (killerTable != null && killerTable.isKillerMove(depth, m)) {
                priority += priorityList[killerPriority];
            } else if (m.getPieceTo() != 0) {


                if(m.getSeeScore() == 0){
                    if (AdvancedMidGameEvaluator.EVALUATE_PRICE[Math.abs(m.getPieceTo())] >= AdvancedMidGameEvaluator.EVALUATE_PRICE[Math.abs(m.getPieceFrom())]) {
                        priority += priorityList[goodCapturePriority];
                    } else {
                        priority += priorityList[badCapturePriority];
                    }
                }else{
                    if(m.getSeeScore() > 0){
                        priority += priorityList[goodCapturePriority];
                    }else{
                        priority += priorityList[badCapturePriority];
                    }
                }

            } else {
                priority += priorityList[nonCapturePriority];
            }
            m.setOrderPriority(m.getOrderPriority() + priority);
        }

        long zobrist = board.zobrist();
        if (transpositionTable != null) {
            TranspositionEntry en = (TranspositionEntry) transpositionTable.get(zobrist);
            if (en != null) {
                Move hashMove = en.getBestMove();
                int index = collection.indexOf(hashMove);
                if (index != -1) {
                    collection.get(index).setOrderPriority(collection.get(index).getOrderPriority() + priorityList[pvPriority]);

                }
            }
        }
    }


    @Override
    public void sort(
            List<Move> collection,
            int depth,
            Board board,
            boolean pvNode,
            KillerTable killerTable,
            TranspositionTable transpositionTable,
            CounterMoveTable counterMoveTable) {

        setPriority(collection, depth, board, pvNode, killerTable, transpositionTable, counterMoveTable);

        //ArrayList<Move> collection2 = new ArrayList(collection);

//        collection.sort((o1, o2) -> {
//            long p1 = o1.getOrderPriority();
//            long p2 = o2.getOrderPriority();
//
//            return -Long.compare(p1,p2);
//        });

        //collection.addAll(collection2);

        collection.sort(null);


    }

    public static void main(String args[]) {
        FastBoard fb = new FastBoard(Setup.DEFAULT);

        fb = IO.read_FEN(fb, "r2qk1nr/pp3ppp/2n1p3/1BbpP3/5Bb1/P4N2/1PP2PPP/RN1Q1RK1 b kq - 1 9");
//
//        List<Move> allMoves1 = fb.getPseudoLegalMoves();
//
        SystematicOrderer2 orderer1 = new SystematicOrderer2();
//
//        orderer1.sort(allMoves1,5,fb,false,null,null,null);
//
//        for (Move m : allMoves1) {
//            System.out.println(IO.algebraicNotation(fb,m));
//            System.out.println(m.getOrderPriority());
//        }
//
//        List<Move> allMoves2 = fb.getPseudoLegalMoves();
//
       DeweyOrderer orderer2 = new DeweyOrderer();
//
//        orderer2.sort(allMoves2,5,fb,false,null,null,null);
//
//        System.out.println("-----------------");
//
//        for (Move m : allMoves1) {
//            System.out.println(IO.algebraicNotation(fb,m));
//            System.out.println(m.getOrderPriority());
//        }

        Calendar cal = Calendar.getInstance();

        long start = System.currentTimeMillis();
        //System.out.println(start);

        for (int i=0 ; i < 1000000; i++) {
            List<Move> allMoves1 = fb.getPseudoLegalMoves();
            orderer1.sort(allMoves1,5,fb,false,null,null,null);
        }
        long end = System.currentTimeMillis();

        System.out.println(end-start);

        start = System.currentTimeMillis();
        //System.out.println(start);

        for (int i=0 ; i < 1000000; i++) {
            List<Move> allMoves1 = fb.getPseudoLegalMoves();
            orderer2.sort(allMoves1,5,fb,false,null,null,null);
        }
        end = System.currentTimeMillis();

        System.out.println(end-start);

         start = System.currentTimeMillis();
        //System.out.println(start);

        for (int i=0 ; i < 1000000; i++) {
            List<Move> allMoves1 = fb.getPseudoLegalMoves();
            orderer1.sort(allMoves1,5,fb,false,null,null,null);
        }
         end = System.currentTimeMillis();

        System.out.println(end-start);

        start = System.currentTimeMillis();
        //System.out.println(start);

        for (int i=0 ; i < 1000000; i++) {
            List<Move> allMoves1 = fb.getPseudoLegalMoves();
            orderer2.sort(allMoves1,5,fb,false,null,null,null);
        }
        end = System.currentTimeMillis();

        System.out.println(end-start);
    }

}
