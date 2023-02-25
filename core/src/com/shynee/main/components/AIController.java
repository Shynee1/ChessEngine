package com.shynee.main.components;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.shynee.main.abstracts.Component;
import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Move;
import com.shynee.main.chess.Piece;

import java.util.List;
import java.util.Random;

public class AIController extends Component {

    private final int pawnValue = 10;
    private final int knightValue = 30;
    private final int bishopValue = 30;
    private final int rookValue = 50;
    private final int queenValue = 90;
    private final int kingValue = 900;


    private final boolean color;
    private final ChessBoard board;

    private Move bestMove = null;

    public AIController(boolean aiColor, ChessBoard board){
        this.color = aiColor;
        this.board = board;
    }

    @Override
    public void start() {

    }

    @Override
    public void update(float dt, SpriteBatch batch) {
        if (board.colorToMove() != color) return;

        search(3, Integer.MIN_VALUE, Integer.MAX_VALUE);
        System.out.println("cycle");
        //bestMove = getRandomMove();

        if (bestMove != null) board.makeMove(bestMove);

    }

    private Move getRandomMove(){
        Random random = new Random();

        List<Move> legals = board.getMoveCalculator().getLegalMoves(color);
        if (legals.isEmpty()) return null;

        return legals.get(random.nextInt(legals.size()));
    }

    private int search(int depth, int alpha, int beta){
        //System.out.println(depth);
        if (depth == 0) return quiescenceSearch(alpha, beta);

        boolean color = board.colorToMove();
        List<Move> legalMoves = board.getMoveCalculator().getLegalMoves(color);

        if (legalMoves.isEmpty()){
            if (color ? board.isWhiteCheck : board.isBlackCheck){
                return Integer.MIN_VALUE; //Checkmate
            }
            return 0; //Stalemate
        }

        for (Move legalMove : legalMoves) {

            board.makeMove(legalMove);
            int eval = -search(depth-1, -beta, -alpha);
            board.unmakeMove(legalMove);

            if (eval >= beta) {
                return beta;
            }

            if (eval > alpha) {
                alpha = eval;

                this.bestMove = legalMove;
            }
        }

        return alpha;
    }

    private int quiescenceSearch(int alpha, int beta){
        int eval = evaluate();

        if (eval >= beta) return beta;
        if (eval > alpha) alpha = eval;

        boolean color = board.colorToMove();

        List<Move> legalMoves = board.getMoveCalculator().getLegalMoves(color);

        for (Move legalMove : legalMoves) {

            board.makeMove(legalMove);
            eval = -quiescenceSearch(-beta, -alpha);
            board.unmakeMove(legalMove);

            if (eval >= beta) return beta;
            if (eval > alpha) alpha = eval;
        }
        return alpha;
    }


    private int evaluate(){
        int whiteEval = countMaterial(true);
        int blackEval = countMaterial(false);

        //Negative = bad, positive = good
        int perspective = board.colorToMove()?1:-1;

        return (whiteEval-blackEval)*perspective;
    }

    private int countMaterial(boolean color){
        int material = 0;

        material += board.numPieces(Piece.PAWN, color) * pawnValue;
        material += board.numPieces(Piece.KNIGHT, color) * knightValue;
        material += board.numPieces(Piece.BISHOP, color) * bishopValue;
        material += board.numPieces(Piece.ROOK, color) * rookValue;
        material += board.numPieces(Piece.QUEEN, color) * queenValue;
        material += board.numPieces(Piece.KING, color) * kingValue;

        return material;
    }

}
