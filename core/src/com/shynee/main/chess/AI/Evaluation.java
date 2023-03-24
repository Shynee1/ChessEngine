package com.shynee.main.chess.AI;

import com.shynee.main.chess.BoardUtility;
import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Piece;
import com.shynee.main.chess.Square;

import java.util.List;

/**
 * Evaluation -- Used to perform static evaluation of a given board position.
 * Factors in material value, piece position, and endgames.
 */
public class Evaluation {

    private final static int pawnValue = 100;
    private final static int knightValue = 300;
    private final static int bishopValue = 300;
    private final static int rookValue = 500;
    private final static int queenValue = 900;

    // Marks the start of the endgame
    private final static int endgameStart = rookValue*2+bishopValue+knightValue;

    /**
     * Performs an evaluation of the current board position.
     * A positive value is good for whoever turn it is to move.
     *
     * @param board ChessBoard class representing current board.
     * @return The evaluation.
     */
    public static int evaluate(ChessBoard board){
        int whiteEval = countMaterial(board, true);
        int blackEval = countMaterial(board, false);

        // Pawns are ignored to make endgame calculations easier
        int whiteMaterialWithoutPawns = whiteEval - (board.numPieces(Piece.PAWN, true)*pawnValue);
        int blackMaterialWithoutPawns = blackEval - (board.numPieces(Piece.PAWN, false)*pawnValue);

        // Endgame weight is used to detect endgame and add more value to remaining pieces
        float whiteEndWeight = getEndgameWeight(whiteMaterialWithoutPawns);
        float blackEndWeight = getEndgameWeight(blackMaterialWithoutPawns);

        whiteEval += kingEndgameEval(board.whiteKingSquare, whiteEval, board.blackKingSquare, blackEval, blackEndWeight);
        blackEval += kingEndgameEval(board.blackKingSquare, blackEval, board.whiteKingSquare, whiteEval, whiteEndWeight);

        whiteEval += evalPieceSquareTables(board, true, blackEndWeight);
        blackEval += evalPieceSquareTables(board, false, whiteEndWeight);

        // Negative = bad, positive = good
        int perspective = board.colorToMove()?1:-1;

        return (whiteEval-blackEval)*perspective;
    }

    /**
     * A value that gets larger as players move farther into the endgame
     * @param materialWithoutPawns Opponent's static material value without pawns.
     * @return The evaluated endgame weight.
     */
    private static float getEndgameWeight(int materialWithoutPawns){
        float multiplier = 1f/endgameStart;
        return 1 - Math.min(1, materialWithoutPawns*multiplier);
    }

    /**
     * Evaluates the position of the king in the endgame.
     * Rewards the friendly king moving closer to the enemy king.
     * Meant to assist with checkmating when search can't see
     * far enough ahead for checkmate.
     *
     * @param friendlyKing Square of the friendly king.
     * @param friendlyEval Current evaluation of the friendly color.
     * @param opponentKing Square of the opponent king.
     * @param opponentEval Current evaluation of the opponent color.
     * @param endgameWeight Value that gets larger as opponent loses more pieces.
     * @return Integer evaluation of the friendly king position.
     */
    private static int kingEndgameEval(Square friendlyKing, int friendlyEval, Square opponentKing, int opponentEval, float endgameWeight){
        // Check if entered endgame
        if (!(friendlyEval > opponentEval + pawnValue * 2 && endgameWeight > 0)) return 0;

        int endgameEval = 0;

        // Calculate center manhattan distance (distance from king to center of board)
        int[] oppRF = BoardUtility.getRankAndFile(opponentKing.getArrayPosition());
        int rankFromCenter = Math.max(3 - oppRF[0], oppRF[0] - 4);
        int fileFromCenter = Math.max(3 - oppRF[1], oppRF[1] - 4);
        int centerManhattanDistance = rankFromCenter+fileFromCenter;

        // Adjust distance to be weighed like piece-square tables
        endgameEval += centerManhattanDistance * 10;

        // Push friendly king closer to opponent (help with checkmate)
        int[] friendRF = BoardUtility.getRankAndFile(friendlyKing.getArrayPosition());
        int rankDst = Math.abs(oppRF[0] - friendRF[0]);
        int fileDst = Math.abs(oppRF[1] - friendRF[1]);
        // 14 is the max distance the kings can be
        int dstKings = 14 - rankDst+fileDst;
        endgameEval += dstKings * 4;

        return (int) (endgameEval*endgameWeight);
    }

    /**
     * Sums the total material of all pieces of a color on the board.
     * @param board ChessBoard class representing current board.
     * @param color Color of the pieces to count.
     * @return Sum of all material values.
     */
    private static int countMaterial(ChessBoard board, boolean color){
        int material = 0;

        material += board.numPieces(Piece.PAWN, color) * pawnValue;
        material += board.numPieces(Piece.KNIGHT, color) * knightValue;
        material += board.numPieces(Piece.BISHOP, color) * bishopValue;
        material += board.numPieces(Piece.ROOK, color) * rookValue;
        material += board.numPieces(Piece.QUEEN, color) * queenValue;

        return material;
    }

    /**
     * Evaluates the position of every piece of a certain color.
     * Positions are evaluated using an array of values that represent every square of the board.
     * These arrays are generalized rules about where pieces should be
     * (e.g. king should stay on the bottom of the board and not wander).
     * See PieceSquareTables class for all values.
     *
     * @param board ChessBoard class representing the current board position.
     * @param color Color of the pieces to evaluate.
     * @param endgameWeight Value that increases as opponent loses more pieces.
     * @return Sum of the position evaluation of every piece of the specified color.
     */
    private static int evalPieceSquareTables(ChessBoard board, boolean color, float endgameWeight){
        int value = 0;

        value += evalPieceSquareTable(board, PieceSquareTables.queens, Piece.QUEEN, color);
        value += evalPieceSquareTable(board, PieceSquareTables.bishops, Piece.BISHOP, color);
        value += evalPieceSquareTable(board, PieceSquareTables.knights, Piece.KNIGHT, color);
        value += evalPieceSquareTable(board, PieceSquareTables.rooks, Piece.ROOK, color);
        value += evalPieceSquareTable(board, PieceSquareTables.pawns, Piece.PAWN, color);

        // This table becomes less important the closer you get to the endgame
        int kingEarlyPhase = PieceSquareTables.read(PieceSquareTables.kingMiddle, Piece.KING, color);
        value += kingEarlyPhase * (1-endgameWeight);

        return value;
    }

    /**
     * Evaluates position of all pieces of a certain type/color using piece-square tables.
     * See above for more details.
     *
     * @param board ChessBoard class representing the current board position.
     * @param table Piece-square table to evaluate.
     * @param pieceType Type of piece to evaluate against table.
     * @param color Color of the piece.
     * @return Sum of the position evaluation of all pieces of the specified type and color.
     */
    private static int evalPieceSquareTable(ChessBoard board, int[] table, int pieceType, boolean color){
        int value = 0;
        List<Integer> pieceList = board.getPiecePosList(pieceType, color);
        for (int piecePos : pieceList){
            value += PieceSquareTables.read(table, piecePos, color);
        }

        return value;
    }

    public static int getPieceValue(int pieceType){
        switch (pieceType){
            case Piece.QUEEN -> {
                return queenValue;
            }
            case Piece.BISHOP -> {
                return bishopValue;
            }
            case Piece.KNIGHT -> {
                return knightValue;
            }
            case Piece.ROOK -> {
                return rookValue;
            }
            case Piece.PAWN -> {
                return pawnValue;
            }
            default -> {
                return 0;
            }
        }
    }
}
