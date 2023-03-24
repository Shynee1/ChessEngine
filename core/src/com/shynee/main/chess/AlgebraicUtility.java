package com.shynee.main.chess;

import java.util.List;

/**
 * AlgebraicUtility - Utility class for dealing with algebraic notation.
 * Contains methods for converting notation to move and move to notation.
 */
public class AlgebraicUtility {

    /**
     * Converts algebraic notation (e4) to a legal Move (12:28)
     * @param board ChessBoard class containing helpful board variables
     * @param notation Algebraic notation to convert
     * @return Move if the notation can be played or null if not
     */
    public static Move notationToMove(ChessBoard board, String notation){
        boolean color = board.colorToMove();

        // Remove unnecessary symbols (e.g. +, -, #, x)
        notation = notation.replace("+", "").replace("-", "").replace("x", "").replace("#", "").replace("=", "");

        // Loop through all possible moves
        List<Move> legalMoves = board.getMoveCalculator().getLegalMoves(board, color);
        for (Move move : legalMoves){

            // Check if notation and move are castle
            if (notation.equals("OO")){
                if (move.isCastle && move.directionOffset == 1) return move;
                continue;
            }
            if (notation.equals("OOO")){
                if (move.isCastle && move.directionOffset == -1) return move;
                continue;
            }

            char[] chars = notation.toCharArray();
            // Check for valid notation
            if (chars.length < 2 || Character.isDigit(chars[0])) return null;

            // Rank and file are always last two values
            int file = (chars[chars.length-2]-97);
            int rank = Character.getNumericValue(chars[chars.length-1])-1;

            int pieceType = -1;
            int startRank = -1;
            int startFile = -1;

            // Check if the notation is a pawn move
            if (Character.isLowerCase(chars[0]) && Character.isAlphabetic(chars[0])){
                // Check for rank/file ambiguity (e.g. dxe4)
                if (notation.length() == 3) startFile = (chars[0]-97);
                pieceType = Piece.PAWN;

            } else if(Character.isUpperCase(chars[0])){
                // Get piece type (e.g. Nf4 = Knight)
                switch(chars[0]){
                    case 'Q' -> pieceType = Piece.QUEEN;
                    case 'B' -> pieceType = Piece.BISHOP;
                    case 'N' -> pieceType = Piece.KNIGHT;
                    case 'R' -> pieceType = Piece.ROOK;
                    case 'K' -> pieceType = Piece.KING;
                }

                // Check for rank/file ambiguity (e.g. Ngf4)
                if (notation.length() == 4){
                    if (Character.isAlphabetic(chars[1])) startFile = (chars[1]-97);
                    else startRank = Character.getNumericValue(chars[1])-1;
                }
            }

            // Find corresponding legal move
            if (BoardUtility.getArrayIndex(rank, file) == move.squarePos && board.getSquare(move.piecePos).getPiece().type == pieceType){
                int[] rf = BoardUtility.getRankAndFile(move.piecePos);

                if (startFile == -1 && startRank == -1) return move;
                else if (startFile != -1 && rf[1] == startFile) return move;
                else if (startRank != -1 && rf[0] == startRank) return move;
            }

        }

        return null;
    }

    /**
     * Convert Move (12:28) to algebraic notation (e4)
     * @param board ChessBoard class containing helpful board variables
     * @param move Move to be converted into algebraic notation
     * @return Converted algebraic notation or ""
     */
    public static String moveToNotation(ChessBoard board, Move move){
        String notation = "";

        if (move.isCastle){
            if(move.directionOffset>0) return "O-O";
            else return "O-O-O";
        }

        int[] rf = BoardUtility.getRankAndFile(move.squarePos);
        // Convert file into character (e.g. 4 -> d)
        char file = (char) ((rf[1]+97));
        // Find corresponding piece type (Knight = 'N')
        char piece = 'd';
        switch(board.getSquare(move.piecePos).getPiece().type){
            case 0 -> piece = 'K';
            case 1 -> piece = 'Q';
            case 2 -> piece = 'B';
            case 3 -> piece = 'N';
            case 4 -> piece = 'R';
        }

        boolean isTake = board.getSquare(move.squarePos).hasPiece();
        if (piece != 'd') notation += piece;
        else notation += isTake ? piece : "";

        // Add 'x' if the move is a take
        notation += isTake ? 'x' : "";
        notation += file;
        notation += rf[0]+1;

        return notation;
    }
}
