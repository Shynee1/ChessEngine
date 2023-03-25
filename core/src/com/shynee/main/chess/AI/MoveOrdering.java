package com.shynee.main.chess.AI;

import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Move;
import com.shynee.main.chess.Piece;

import java.util.List;

/**
 * MoveOrdering -- Static class used to order moves by how likely they are to be a good move
 * This should theoretically help the speed of the search and prioritize certain moves in a "quiet" position
 */
public class MoveOrdering {

    // Increase capture score so that a capture of any kind is always the best move
    private final static int pieceCaptureMultiplier = 10;
    private final static int maxMoveCount = 218;

    /**
     * Orders moves based on how likely they are to be a good move.
     * Should increase search efficiency and prioritize certain moves in a "quiet" position.
     *
     * @param board ChessBoard class representing current board state
     * @param moves List of moves to be ordered
     * @return New list of moves sorted by how likely each move is to be a good move.
     */
    public static List<Move> orderMoves(ChessBoard board, List<Move> moves){
        int[] moveScores = new int[maxMoveCount];

        for (int i = 0; i < moves.size(); i++){
            int score = 0;
            Move move = moves.get(i);

            Piece movePiece = board.getSquare(move.piecePos).getPiece();
            Piece targetPiece = board.getSquare(move.squarePos).getPiece();

            // Prioritize capturing a high value piece with a low value piece
            if (targetPiece != null){
                score = pieceCaptureMultiplier * Evaluation.getPieceValue(targetPiece.type) - Evaluation.getPieceValue(movePiece.type);
            }

            // Prioritize promoting a pawn to a queen
            if (movePiece.type == Piece.PAWN){
                if (move.isPromotion) score += Evaluation.getPieceValue(Piece.QUEEN);
            }

            moveScores[i] = score;
        }

        return sort(moves, moveScores);
    }

    /**
     * Sorts a list of moves by performing a selection sort on an int[].
     * Since the values of the List<Move> correspond to the values of the int[],
     * we can swap them when we perform the swap for the selection sort.
     *
     * @param moves List of moves we want to be sorted.
     * @param moveScores Int[] whose values correspond to the values of the List<Move>
     * @return The list of moves sorted in descending order
     */
    private static List<Move> sort(List<Move> moves, int[] moveScores){
        for (int i = 0; i < moves.size() - 1; i++) {
            int maxIndex = i;
            for (int j = i+1; j < moves.size(); j++){
                if (moveScores[j] > moveScores[maxIndex]){
                    maxIndex = j;
                }
            }

            swap(moves, moveScores, i, maxIndex);
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
