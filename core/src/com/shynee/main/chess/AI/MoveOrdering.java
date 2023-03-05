package com.shynee.main.chess.AI;

import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Move;
import com.shynee.main.chess.Piece;

import java.util.Arrays;
import java.util.List;

public class MoveOrdering {

    private final static int pieceCaptureMultiplier = 5;
    private final static int maxMoveCount = 218;

    public static List<Move> orderMoves(ChessBoard board, List<Move> moves){
        int[] moveScores = new int[maxMoveCount];

        for (int i = 0; i < moves.size(); i++){
            int score = 0;
            Move move = moves.get(i);

            Piece movePiece = board.getSquare(move.piecePos).getPiece();
            Piece targetPiece = board.getSquare(move.squarePos).getPiece();

            if (targetPiece != null){
                score = pieceCaptureMultiplier * Evaluation.getPieceValue(targetPiece.type) - Evaluation.getPieceValue(movePiece.type);
            }

            if (movePiece.type == Piece.PAWN){
                if (move.isPromotion) score += Evaluation.getPieceValue(Piece.QUEEN);
            }

            moveScores[i] = score;
        }

        return sort(moves, moveScores);
    }

    private static List<Move> sort(List<Move> moves, int[] moveScores){
        for (int i = 0; i < moves.size() - 1; i++) {
            for (int j = i + 1; j > 0; j--) {
                int swapIndex = j - 1;
                if (moveScores[swapIndex] < moveScores[j]) {
                    swap(moves, moveScores, j, swapIndex);
                }
            }
        }
        return moves;
    }



    private static void swap(List<Move> moves, int[] moveScores, int posOne, int posTwo){
        Move tempM = moves.get(posOne);
        moves.set(posOne, moves.get(posTwo));
        moves.set(posTwo, tempM);

        int tempI = moveScores[posOne];
        moveScores[posOne] = moveScores[posTwo];
        moveScores[posTwo] = tempI;
    }
}
