package com.shynee.main.chess.AI;

import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Piece;

import java.util.List;

public class Evaluation {

    private final static int pawnValue = 100;
    private final static int knightValue = 300;
    private final static int bishopValue = 300;
    private final static int rookValue = 500;
    private final static int queenValue = 900;

    public static int evaluate(ChessBoard board){
        int whiteEval = countMaterial(board, true);
        int blackEval = countMaterial(board, false);

        whiteEval += evalPieceSquareTables(board, true);
        blackEval += evalPieceSquareTables(board, false);

        //Negative = bad, positive = good
        int perspective = board.colorToMove()?1:-1;

        return (whiteEval-blackEval)*perspective;
    }

    private static int countMaterial(ChessBoard board, boolean color){
        int material = 0;

        material += board.numPieces(Piece.PAWN, color) * pawnValue;
        material += board.numPieces(Piece.KNIGHT, color) * knightValue;
        material += board.numPieces(Piece.BISHOP, color) * bishopValue;
        material += board.numPieces(Piece.ROOK, color) * rookValue;
        material += board.numPieces(Piece.QUEEN, color) * queenValue;

        return material;
    }

    private static int evalPieceSquareTables(ChessBoard board, boolean color){
        int value = 0;

        value += evalPieceSquareTable(board, PieceSquareTables.queens, Piece.QUEEN, color);
        value += evalPieceSquareTable(board, PieceSquareTables.bishops, Piece.BISHOP, color);
        value += evalPieceSquareTable(board, PieceSquareTables.knights, Piece.KNIGHT, color);
        value += evalPieceSquareTable(board, PieceSquareTables.rooks, Piece.ROOK, color);
        value += evalPieceSquareTable(board, PieceSquareTables.pawns, Piece.PAWN, color);
        value += evalPieceSquareTable(board, PieceSquareTables.kingMiddle, Piece.KING, color);

        return value;
    }

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
