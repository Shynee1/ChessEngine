package com.shynee.main.chess.AI;

import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Move;

import java.util.List;

public class Search {

    private ChessBoard board;
    private Move bestMoveInIteration;
    private Move bestMove = null;

    private int bestEvalInIteration;
    private int numTranspositions;
    private int numPositions;

    private boolean abortSearch;

    private final int positiveInfinity = 9999999;
    private final int negativeInfinity = -positiveInfinity;
    private final int mateScore = 100000;

    private final TranspositionTable tt;

    public Search(ChessBoard board){
        this.board = board;
        this.tt = new TranspositionTable(board, 64000);
    }

    public Move startSearch(int targetDepth){
        numTranspositions = 0;
        numPositions = 0;
        abortSearch = false;
        boolean isIterative = true;

        int bestEval = 0;
        int finalDepth = 0;

        tt.clear();

        if (isIterative){
            for (int i = 1; i <= targetDepth; i++){
                search(i, negativeInfinity, positiveInfinity, 0);

                if (abortSearch) break;

                bestMove = bestMoveInIteration;
                bestEval = bestEvalInIteration;
                finalDepth = i;

                if (isMateScore(bestEval) && finalDepth > 2){
                    break;
                }
            }
        }else {
            search(targetDepth, negativeInfinity, positiveInfinity, 0);
        }

        System.out.println("depth: " + finalDepth);
        System.out.println("eval: " + bestEval);
        System.out.println("transPos: " + numTranspositions);
        System.out.println("pos: " + numPositions);

        return bestMove;
    }

    private int search(int depth, int alpha, int beta, int plyFromRoot){
        numPositions++;
        if (abortSearch) return 0;

        if (plyFromRoot > 0){

            if (board.isRepeatPosition(board.zobristKey)) return 0;

            //Any mate we find cannot be better than one we already found
            alpha = Math.max(alpha, -mateScore+plyFromRoot);
            beta = Math.min(beta, mateScore-plyFromRoot);
            if (alpha>=beta)
                return alpha;
        }

        int t = tt.lookupEvaluation(depth, alpha, beta);
        if (t != Integer.MIN_VALUE){
            if (plyFromRoot == 0) {
                bestMoveInIteration = tt.getCurrentMove();
                bestEvalInIteration = tt.getCurrentEval();
            }
            numTranspositions++;
            return t;
        }

        if (depth == 0) {
            return quiescenceSearch(alpha, beta);
        }

        boolean color = board.colorToMove();
        List<Move> legalMoves = board.getMoveCalculator().getLegalMoves(board, color);
        int hashFlag = TranspositionTable.UPPER;

        if (legalMoves.isEmpty()){
            if (board.isWhiteCheck || board.isBlackCheck){ //Checkmate
                return -1*(mateScore-plyFromRoot);
            }
            return 0; //Stalemate
        }

        legalMoves = MoveOrdering.orderMoves(board, legalMoves);
        for (Move legalMove : legalMoves) {
            board.makeMove(legalMove, true);
            int eval = -search(depth-1, -beta, -alpha, plyFromRoot+1);
            board.unmakeMove(legalMove, true);

            if (eval >= beta) {
                tt.storeEvaluation(depth, TranspositionTable.LOWER, eval, legalMove);
                return beta;
            }

            if (eval > alpha) {
                alpha = eval;
                hashFlag = TranspositionTable.EXACT;
                if (plyFromRoot == 0) {
                    this.bestMoveInIteration = legalMove;
                    this.bestEvalInIteration = eval;
                }
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
            board.unmakeMove(move, true);

            if (eval >= beta) return beta;
            if (eval > alpha) alpha = eval;
        }

        return alpha;
    }

    public boolean isMateScore(int score){
        return Math.abs(score) + 1000 > mateScore;
    }

    public void abortSearch(){
        abortSearch = true;
    }


}
