package com.shynee.main.chess;

import java.util.List;

public class PGNUtility {

    public static Move pgnToMove(ChessBoard board, String pgn){
        boolean color = board.colorToMove();

        pgn = pgn.replace("+", "").replace("-", "").replace("x", "").replace("#", "").replace("=", "");
        List<Move> legalMoves = board.getMoveCalculator().getLegalMoves(board, color);
        for (Move move : legalMoves){

            if (pgn.equals("OO")){
                if (move.isCastle && move.directionOffset == 1) return move;
                continue;
            }
            if (pgn.equals("OOO")){
                if (move.isCastle && move.directionOffset == -1) return move;
                continue;
            }

            char[] chars = pgn.toCharArray();
            if (chars.length < 2 || Character.isDigit(chars[0])) return null;

            int file = (chars[chars.length-2]-97);
            int rank = Character.getNumericValue(chars[chars.length-1])-1;
            int pieceType = -1;
            int startRank = -1;
            int startFile = -1;

            //Pawn
            if (Character.isLowerCase(chars[0]) && Character.isAlphabetic(chars[0])){
                if (pgn.length() == 3) startFile = (chars[0]-97);
                pieceType = Piece.PAWN;
            } else if(Character.isUpperCase(chars[0])){
                switch(chars[0]){
                    case 'Q' -> pieceType = Piece.QUEEN;
                    case 'B' -> pieceType = Piece.BISHOP;
                    case 'N' -> pieceType = Piece.KNIGHT;
                    case 'R' -> pieceType = Piece.ROOK;
                    case 'K' -> pieceType = Piece.KING;
                }

                if (pgn.length() == 4){
                    if (Character.isAlphabetic(chars[1])) startFile = (chars[1]-97);
                    else startRank = Character.getNumericValue(chars[1])-1;
                }
            }

            if (BoardUtility.getArrayIndex(rank, file) == move.squarePos && board.getSquare(move.piecePos).getPiece().type == pieceType){
                int[] rf = BoardUtility.getRankAndFile(move.piecePos);

                if (startFile == -1 && startRank == -1) return move;
                else if (startFile != -1 && rf[1] == startFile) return move;
                else if (startRank != -1 && rf[0] == startRank) return move;
            }

        }

        return null;
    }

    public static String moveToPgn(ChessBoard board, Move move){
        String pgn = "";

        if (move.isCastle){
            if(move.directionOffset>0) return "O-O";
            else return "O-O-O";
        }

        int[] rf = BoardUtility.getRankAndFile(move.squarePos);
        char file = (char) ((rf[1]+97));
        char piece = 'd';
        switch(board.getSquare(move.piecePos).getPiece().type){
            case 0 -> piece = 'K';
            case 1 -> piece = 'Q';
            case 2 -> piece = 'B';
            case 3 -> piece = 'N';
            case 4 -> piece = 'R';
        }

        boolean isTake = board.getSquare(move.squarePos).hasPiece();
        if (piece != 'd') pgn += piece;
        else pgn += isTake ? piece : "";

        pgn += isTake ? 'x' : "";
        pgn += file;
        pgn += rf[0]+1;

        return pgn;
    }
}
