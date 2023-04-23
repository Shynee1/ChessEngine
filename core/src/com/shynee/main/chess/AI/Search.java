package com.shynee.main.chess.AI;

import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Move;

import java.util.List;

/**
 * Search -- Used to search for the best possible move in a given position.
 */
public class Search {

    private final ChessBoard board;
    private final MoveOrdering moveOrdering;
    private final TranspositionTable tt;

    private Move bestMoveInIteration;
    private Move bestMove = null;

    private int bestEvalInIteration;
    private int numTranspositions;
    private int numPositions;

    private boolean abortSearch;

    // Extreme numbers that can be used as alpha/beta values
    private final int positiveInfinity = 9999999;
    private final int negativeInfinity = -positiveInfinity;
    // Uniquely identifiable number that will not conflict with search
    private final int mateScore = 100000;

    public Search(ChessBoard board){
        this.board = board;
        this.tt = new TranspositionTable(board, 64000);
        this.moveOrdering = new MoveOrdering();
    }

    /**
     * Search a given position with a target depth.
     * Stops the search when the target depth is reached, mate is found, or search timer expires
     * @param targetDepth Max depth the search can run to.
     * @return Best possible move in the position.
     */
    public Move startSearch(int targetDepth){
        numTranspositions = 0;
        numPositions = 0;
        abortSearch = false;
        boolean isIterative = true;

        int bestEval = 0;
        int finalDepth = 0;

        // Clearing the table prevents weird checkmate bug
        tt.clear();

        if (isIterative){
            for (int i = 1; i <= targetDepth; i++){
                search(i, negativeInfinity, positiveInfinity, 0);

                if (abortSearch) break;

                bestMove = bestMoveInIteration;
                bestEval = bestEvalInIteration;
                finalDepth = i;

                // Stop the search if mate is found
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

    /**
     * Performs recursive NegaMax search with alpha-beta pruning.
     * @param depth Maximum number of moves looked ahead.
     * @param alpha Lower bounds for search values (initially set to negative infinity)
     * @param beta Upper bounds for search values (initially set to infinity)
     * @param plyFromRoot Current number of moves from initial position
     * @return Maximum evaluation of position
     */
    private int search(int depth, int alpha, int beta, int plyFromRoot){
        numPositions++;
        if (abortSearch) return 0;

        if (plyFromRoot > 0){
            // Check for draw by repetition
            if (board.isRepeatPosition(board.zobristKey)) return 0;

            // Any mate we find cannot be better than one we already found
            alpha = Math.max(alpha, -mateScore+plyFromRoot);
            beta = Math.min(beta, mateScore-plyFromRoot);
            if (alpha>=beta)
                return alpha;
        }

        // Check if the position has already been searched
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

        legalMoves = moveOrdering.orderMoves(board, tt, legalMoves);
        for (Move legalMove : legalMoves) {
            board.makeMove(legalMove, true);
            int eval = -search(depth-1, -beta, -alpha, plyFromRoot+1);
            board.unmakeMove(legalMove, true);

            // This position is worse than one we have already found
            if (eval >= beta) {
                tt.storeEvaluation(depth, TranspositionTable.LOWER, eval, legalMove);
                return beta;
            }

            // This is the best position so far
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

    /**
     * Performs a limited search of all captures before returning a static evaluation.
     * Used to prevent a false evaluation because search stopped right
     * before the opponent was able to capture your piece.
     *
     * @param alpha Alpha value of the search
     * @param beta Beta value of the search
     * @return Static evaluation after all captures have been processed.
     */
    private int quiescenceSearch(int alpha, int beta){
        int eval = Evaluation.evaluate(board);

        if (eval >= beta) return beta;
        if (eval > alpha) alpha = eval;

        boolean color = board.colorToMove();
        List<Move> legalCaptures = board.getMoveCalculator().getLegalCaptures(board, color);
        moveOrdering.orderMoves(board, tt, legalCaptures);

        for (Move move : legalCaptures) {

            board.makeMove(move, true);
            eval = -quiescenceSearch(-beta, -alpha);
            board.unmakeMove(move, true);

            if (eval >= beta) return beta;
            if (eval > alpha) alpha = eval;
        }

        return alpha;
    }

    /**
     * Detects if a score is checkmate.
     * It does this by adding the max ply-from-root (1000)
     * and seeing if the score is above the default mate score.
     *
     * @param score The score to be checked.
     * @return true if the score is a checkmate score
     */
    public boolean isMateScore(int score){
        return Math.abs(score) + 1000 > mateScore;
    }

    public void abortSearch(){
        abortSearch = true;
    }


}
