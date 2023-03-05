package com.shynee.main.chess;

import java.util.List;

public class BoardUtility {

    public static boolean containsSquare(List<Move> moves, Square target){
        for (Move m : moves){
            if (m.squarePos == target.getArrayPosition()) return true;
        }

        return false;
    }

    public static Move getPieceIfOne(Square[] board, List<Move> moves, boolean color){
        int num = 0;
        Move pieceMove = null;

        for (Move m : moves){
            Square s = board[m.squarePos];
            if (s.hasPiece() && s.getPiece().type != Piece.KING && s.getPiece().color == color) {
                num++;
                pieceMove = new Move(m.piecePos, m.squarePos, -m.directionOffset);
            }
        }

        if (num == 1) return pieceMove;
        else return null;
    }

    public static int numPieces(Square[] board, List<Move> moves){
        int num = 0;
        for (Move m : moves){
            Square s = board[m.squarePos];
            if (s.hasPiece() && s.getPiece().type != Piece.KING) num++;
        }

        return num;
    }

    public static int getArrayIndex(int rank, int file){
        return rank*8+file;
    }

    public static int[] getRankAndFile(int squarePosition){
        int[] rankAndFile = new int[2];
        rankAndFile[0] = squarePosition/8;
        rankAndFile[1] = squarePosition - rankAndFile[0] * 8;
        return rankAndFile;
    }
}
