package com.shynee.main.chess.AI;

import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Move;
import com.shynee.main.chess.Piece;

import java.util.Comparator;
import java.util.List;

/**
 * MoveOrdering -- Static class used to order moves by how likely they are to be a good move
 * This should theoretically help the speed of the search and prioritize certain moves in a "quiet" position
 */
public class MoveOrdering implements Comparator<Move> {

    // Increase capture score so that a capture of any kind is always the best move
    private final static int pieceCaptureMultiplier = 10;
    private final static int maxMoveCount = 218;

    private int[] moveScores;

    /**
     * Orders moves based on how likely they are to be a good move.
     * Should increase search efficiency and prioritize certain moves in a "quiet" position.
     *
     * @param board ChessBoard class representing current board state
     * @param moves List of moves to be ordered
     * @return New list of moves sorted by how likely each move is to be a good move.
     */
    public List<Move> orderMoves(ChessBoard board, TranspositionTable table, List<Move> moves){
        moveScores = new int[maxMoveCount];

        for (int i = 0; i < moves.size(); i++){
            int score = 0;

            Move move = moves.get(i);
            move.setSortIndex(i);

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

        moves.sort(this);

        return moves;
    }

    @Override
    public int compare(Move o1, Move o2) {
        return Integer.compare(moveScores[o2.getSortIndex()], moveScores[o1.getSortIndex()]);
    }
}
