package com.shynee.main.chess.AI;

import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Square;

import java.util.Random;

public class Zobrist {

    private static long[][] zobristKeys = new long[12][64];
    private static long[] castling = new long[4];
    private static long isBlackTurn;

    public static void initializeKeys(){
        long seed = 123456789L;
        Random random = new Random(seed);
        for (int i = 0; i < 12; i++){
            for (int j = 0; j < 64; j++){
                zobristKeys[i][j] = random.nextLong(0, Long.MAX_VALUE);
            }
        }

        for (int i = 0; i < castling.length; i++){
            castling[i] = random.nextLong(0, Long.MAX_VALUE);
        }

        isBlackTurn = random.nextLong(0, Long.MAX_VALUE);
    }

    public static long generateKey(ChessBoard board){
        Square[] squares = board.getSquares();
        long key = 0;

        for (Square s : squares){
            if (s.hasPiece()) key ^= zobristKeys[s.getPiece().color ? s.getPiece().type : s.getPiece().type+6][s.getArrayPosition()];
        }

        boolean whiteCastleKing = board.whiteKingSquare.hasPiece() && !board.whiteKingSquare.getPiece().hasMoved && board.getSquare(7).hasPiece() && !board.getSquare(7).getPiece().hasMoved;
        if (whiteCastleKing) key ^= castling[0];
        boolean whiteCastleQueen = board.whiteKingSquare.hasPiece() && !board.whiteKingSquare.getPiece().hasMoved && board.getSquare(0).hasPiece() && !board.getSquare(0).getPiece().hasMoved;
        if (whiteCastleQueen) key ^= castling[1];
        boolean blackCastleKing = board.blackKingSquare.hasPiece() && !board.blackKingSquare.getPiece().hasMoved && board.getSquare(63).hasPiece() && !board.getSquare(63).getPiece().hasMoved;
        if (blackCastleKing) key ^= castling[2];
        boolean blackCastleQueen = board.blackKingSquare.hasPiece() && !board.blackKingSquare.getPiece().hasMoved && board.getSquare(56).hasPiece() && !board.getSquare(56).getPiece().hasMoved;
        if (blackCastleQueen) key ^= castling[3];

        if (!board.colorToMove()) key ^= isBlackTurn;

        return key;
    }
}
