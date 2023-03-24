package com.shynee.main.chess.AI;

import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Square;

import java.util.Random;

/**
 * Zobrist: Used to create a unique hash for a board position.
 */
public class Zobrist {

    // An array of random numbers for every piece and every square
    private static long[][] zobristKeys = new long[12][64];
    // An array of random number that represents the castling rights of both sides
    private static long[] castling = new long[4];
    // A random number that represents if its blacks turn to move
    private static long isBlackTurn;

    /**
     * Fills each array with a non-negative random number.
     * This approach allows us to get a unique number
     * for every piece on every square. It also allows
     * us to hash things like castling rights and turn
     * to move.
     */
    public static void initializeKeys(){
        // Use a seed to make sure zobrist keys are the same every time the program is run
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

    /**
     * Generates a unique zobrist hash for a given board position.
     * This is done by xor-ing the random numbers for every piece
     * at a square on the board.
     * @param board ChessBoard class containing the board position we want to hash
     * @return The zobrist key of the given board position
     */
    public static long generateKey(ChessBoard board){
        Square[] squares = board.getSquares();
        long key = 0;

        for (Square s : squares){
            // Black pieces are stored using their piece type + 6
            if (s.hasPiece()) key ^= zobristKeys[s.getPiece().color ? s.getPiece().type : s.getPiece().type+6][s.getArrayPosition()];
        }

        // Check for castling rights
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
