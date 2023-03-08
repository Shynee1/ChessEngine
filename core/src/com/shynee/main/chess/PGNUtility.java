package com.shynee.main.chess;

import java.util.List;

public class PGNUtility {

    public static Move pgnToMove(ChessBoard board, String pgn, int num){
        boolean color = num % 2 == 1;

        pgn = pgn.replace("+", "");

        if (pgn.equals("O-O")) return new Move(color ? board.whiteKingSquare.getArrayPosition() : board.blackKingSquare.getArrayPosition(), 0, 1).setCastle();
        if (pgn.equals("O-O-O")) return new Move(color ? board.whiteKingSquare.getArrayPosition() : board.blackKingSquare.getArrayPosition(), 0, -1).setCastle();

        char[] chars = pgn.toCharArray();
        if (chars.length < 2 || Character.isDigit(chars[0])) return null;
        int charIdx = 0;

        int pieceType = 5;
        if (Character.isUpperCase(chars[charIdx])){
            switch(chars[charIdx]){
                case 'Q' -> pieceType = 1;
                case 'B' -> pieceType = 2;
                case 'N' -> pieceType = 3;
                case 'R' -> pieceType = 4;
                case 'K' -> pieceType = 0;
            }
            charIdx++;
        }

        if (chars[charIdx] == 'd') charIdx++;

        boolean isTake = false;
        if (chars[charIdx] == 'x'){
            isTake = true;
            charIdx++;
        }

        int file = (chars[charIdx]-97);
        charIdx++;

        int rank = Character.getNumericValue(chars[charIdx]);

        List<Move> legalMoves = board.getMoveCalculator().getLegalMoves(board, false);
        legalMoves.addAll(board.getMoveCalculator().getLegalMoves(board, true));

        for (Move m : legalMoves){
            if (BoardUtility.getArrayIndex(rank, file) == m.squarePos && board.getSquare(m.piecePos).getPiece().type == pieceType && board.getSquare(m.squarePos).hasPiece() == isTake)
                return m;
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
