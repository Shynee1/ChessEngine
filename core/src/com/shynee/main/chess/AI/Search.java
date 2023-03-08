package com.shynee.main.chess.AI;

import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Move;

import java.util.List;

public class Search {

    private ChessBoard board;
    private Move bestMoveInIteration;
    private int numTranspositions;

    private final int positiveInfinity = 9999999;
    private final int negativeInfinity = -positiveInfinity;

    private final TranspositionTable tt;

    public Search(ChessBoard board){
        this.board = board;
        this.tt = new TranspositionTable(board, 64000);
    }

    public Move startSearch(int targetDepth){
        int finalEval = search(targetDepth, negativeInfinity, positiveInfinity, 0);
        System.out.println(finalEval);
        return bestMoveInIteration;
    }

    private int search(int depth, int alpha, int beta, int plyFromRoot){
        /*int t = tt.lookupEvaluation(depth, alpha, beta);
        if (t != Integer.MIN_VALUE){
            if (plyFromRoot == 0) bestMoveInIteration = tt.getCurrentMove();
            numTranspositions++;
            return t;
        }

         */

        if (depth == 0) {
            return quiescenceSearch(alpha, beta);
        }

        boolean color = board.colorToMove();
        List<Move> legalMoves = board.getMoveCalculator().getLegalMoves(board, color);
        int hashFlag = tt.LOWER;

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
                tt.storeEvaluation(depth, tt.UPPER, eval, legalMove);
                return beta;
            }

            if (eval > alpha) {
                alpha = eval;
                hashFlag = tt.EXACT;
                if (plyFromRoot == 0) this.bestMoveInIteration = legalMove;
            }

            tt.storeEvaluation(depth, hashFlag, eval, legalMove);
        }

        return alpha;
    }

    private int quiescenceSearch(int alpha, int beta){
        int eval = Evaluation.evaluate(board);

        if (eval >= beta) return beta;
        if (eval > alpha) alpha = eval;

        boolean color = board.colorToMove();
        List<Move> legalCaptures = board.getMoveCalculator().getLegalCaptures(board, color);
        MoveOrdering.orderMoves(board, legalCaptures);

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
