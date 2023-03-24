package com.shynee.main.chess;

import java.util.List;

/**
 * BoardUtility: Standard utility class with helpful functions for manipulation the chess board
 */
public class BoardUtility {

    /**
     * Checks if a list of moves contains a square as its final position.
     * This means that the square's position is equal to the final position of one of the moves.
     * @param moves List of moves to be queried
     * @param target Square to be checked
     * @return True if the list contains the square; false if not
     */
    public static boolean containsSquare(List<Move> moves, Square target){
        for (Move m : moves){
            if (m.squarePos == target.getArrayPosition()) return true;
        }

        return false;
    }

    /**
     * Checks if a list of moves only contains one non-king piece.
     * @param board Square[] representation of the chess board
     * @param moves List of moves to be checked
     * @param color Color of the pieces to be checked
     * @return The piece if there is only one or null if otherwise
     */
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

    /**
     * Counts the number of non-king pieces in a list of moves
     * @param board Square[] representation of the chess board
     * @param moves List of moves to be checked
     * @return The number of non-king pieces in the list of moves
     */
    public static int numPieces(Square[] board, List<Move> moves){
        int num = 0;
        for (Move m : moves){
            Square s = board[m.squarePos];
            if (s.hasPiece() && s.getPiece().type != Piece.KING) num++;
        }

        return num;
    }

    /**
     * Converts the rank and file of a board position to its corresponding index in the square array
     * @param rank Rank of board position
     * @param file File of board position
     * @return The index of the board position in the square array
     */
    public static int getArrayIndex(int rank, int file){
        return rank*8+file;
    }

    /**
     * Converts an index in the square[] to a rank and file.
     * Both the rank and file are values from 0 to 7.
     * @param squarePosition The index in the square[] to be converted
     * @return An int[] where int[0] is the rank and int[1] is the file.
     */
    public static int[] getRankAndFile(int squarePosition){
        int[] rankAndFile = new int[2];
        rankAndFile[0] = squarePosition/8;
        rankAndFile[1] = squarePosition - rankAndFile[0] * 8;
        return rankAndFile;
    }

    /**
     * Finds the reduced direction offset used to move a piece.
     * The direction offset must be one of the values contained in the direction offsets array.
     * This allows for the same direction offset regardless of how many squares were moved.
     * @param start Position of the starting square
     * @param end Position of the ending square
     * @return Reduced direction or 0 if no direction offset is found
     */
    private static int getDirectionOffset(int start, int end){
        // Separate positions into rank and file
        int[] rfStart = BoardUtility.getRankAndFile(start);
        int[] rfEnd = BoardUtility.getRankAndFile(end);

        // Find delta rank and file
        int rankDst = rfEnd[0] - rfStart[0];
        int fileDst = rfEnd[1] - rfStart[1];

        // Use pythagorean theorem to find distance between squares
        int squaresMoves = (int) Math.sqrt(rankDst*rankDst + fileDst*fileDst);

        // Find reduced direction offset
        int[] directionOffsets = {7, 8, 9, -1, 1, -9, -8, -7, 15, 17, 6, 10, -15, -17, -6, -10};
        for (int directionOffset : directionOffsets) {
            int dirDst = directionOffset * squaresMoves;
            if (end - dirDst == start) {
                return directionOffset;
            }
        }
        return 0;
    }
}
