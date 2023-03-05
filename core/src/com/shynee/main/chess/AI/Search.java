package com.shynee.main.chess.AI;

import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Move;

import java.util.List;

public class Search {

    private ChessBoard board;
    private Move bestMoveInIteration;

    private final int positiveInfinity = 9999999;
    private final int negativeInfinity = -positiveInfinity;

    public Search(ChessBoard board){
        this.board = board;
    }

    public Move startSearch(int depth){
        search(depth, negativeInfinity, positiveInfinity, 0);
        return bestMoveInIteration;
    }

    private int search(int depth, int alpha, int beta, int plyFromRoot){
        if (depth == 0) {
            return quiescenceSearch(alpha, beta);
        }

        boolean color = board.colorToMove();
        List<Move> legalMoves = board.getMoveCalculator().getLegalMoves(board, color);

        if (legalMoves.isEmpty()){
            if (board.isWhiteCheck || board.isBlackCheck){
                return negativeInfinity+plyFromRoot;
            }
            return 0; //Stalemate
        }

        legalMoves = MoveOrdering.orderMoves(board, legalMoves);
        for (Move legalMove : legalMoves) {
            board.makeMove(legalMove, true);
            int eval = -search(depth-1, -beta, -alpha, plyFromRoot+1);
            board.unmakeMove(legalMove);

            if (eval >= beta) {
                return beta;
            }

            if (eval > alpha) {
                alpha = eval;
                if (plyFromRoot == 0) this.bestMoveInIteration = legalMove;
            }
        }

        return alpha;
    }

    private int quiescenceSearch(int alpha, int beta){
        int eval = Evaluation.evaluate(board);

        if (eval >= beta) return beta;
        if (eval > alpha) alpha = eval;

        boolean color = board.colorToMove();
        List<Move> legalCaptures = board.getMoveCalculator().getLegalCaptures(board, color);

        for (Move move : legalCaptures) {

            board.makeMove(move, true);
            eval = -quiescenceSearch(-beta, -alpha);
            board.unmakeMove(move);

            if (eval >= beta) return beta;
            if (eval > alpha) alpha = eval;
        }

        return alpha;
    }

}
