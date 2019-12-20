package board;

import board.moves.Move;
import board.setup.Setup;

import java.util.ArrayList;
import java.util.List;

public class SlowBoardWithPieceList extends SlowBoard {

    public SlowBoardWithPieceList(Setup setup) {
        super(setup);
    }

    public SlowBoardWithPieceList() {
    }

    @Override
    public List<Move> getPseudoLegalMoves() {
        ArrayList<Move> moves = new ArrayList<>(50);
        //if (isGameOver()) return moves;

        if(this.getActivePlayer() == 1){
            for(Integer i:pieceList[7]){
                pseudeLegalMoves_pawn(i, 0, y(i), moves);
            }for(Integer i:pieceList[8]){
                pseudeLegalMoves_rook(i, 0, 0, moves);
            }for(Integer i:pieceList[9]){
                pseudeLegalMoves_knight(i, 0, 0, moves);
            }for(Integer i:pieceList[10]){
                pseudeLegalMoves_bishop(i, 0,0, moves);
            }for(Integer i:pieceList[11]){
                pseudeLegalMoves_queen(i, 0, 0, moves);
            }for(Integer i:pieceList[12]){
                pseudeLegalMoves_king(i, 0, 0, moves);
            }
        }else{
            for(Integer i:pieceList[5]){
                pseudeLegalMoves_pawn(i, 0, y(i), moves);
            }for(Integer i:pieceList[4]){
                pseudeLegalMoves_rook(i, 0, 0, moves);
            }for(Integer i:pieceList[3]){
                pseudeLegalMoves_knight(i, 0, 0, moves);
            }for(Integer i:pieceList[2]){
                pseudeLegalMoves_bishop(i, 0,0, moves);
            }for(Integer i:pieceList[1]){
                pseudeLegalMoves_queen(i, 0, 0, moves);
            }for(Integer i:pieceList[0]){
                pseudeLegalMoves_king(i, 0, 0, moves);
            }
        }
        return moves;
    }


    private ArrayList<Integer>[] pieceList;

    @Override
    public void reset() {
        super.reset();
        pieceList = new ArrayList[13];
        for(int i = 0; i < 13; i++){
            pieceList[i] = new ArrayList<>();
        }
    }

    @Override
    public void setPiece(int index, int piece) {
        field[index] = piece;
        pieceList[6+piece].add(new Integer(index));
    }



}
