package com.shynee.main.chess;

import com.badlogic.gdx.math.Vector2;
import com.shynee.main.utils.Transform;

import java.util.HashMap;

import static com.shynee.main.utils.Constants.SQUARE_SIZE;

/*
  Used to load/save the current board state into FEN notation
 */
public class FenUtility {

    private static final HashMap<Character, Piece> piecesMap = new HashMap<>(){{
        put('k', new Piece(0, false));
        put('q', new Piece(1, false));
        put('b', new Piece(2, false));
        put('n', new Piece(3, false));
        put('r', new Piece(4, false));
        put('p', new Piece(5, false));
        put('K', new Piece(0, true));
        put('Q', new Piece(1, true));
        put('B', new Piece(2, true));
        put('N', new Piece(3, true));
        put('R', new Piece(4, true));
        put('P', new Piece(5, true));
    }};


    /**
     * Converts FEN string into board data
     * @param fen FEN string of board
     * @return Custom LoadData with square[] and boolean values
     */
    public static LoadData loadPosition(String fen){

        String[] args = fen.split(" ");
        if (args.length < 2) throw new RuntimeException("Incorrect FEN loaded");

        LoadData lData = new LoadData();
        Square[] boardRep = new Square[64];

        int rank = 7;
        int file = 0;

        for (char c : args[0].toCharArray()) {
            if (c == '/') {
                file = 0;
                rank--;
                continue;
            }

            //If there is a number, initialize that many squares as empty
            if (Character.isDigit(c)) {
                for (int i = 0; i < Character.getNumericValue(c); i++) {
                    int idx = BoardUtility.getArrayIndex(rank, file);
                    boardRep[idx] = new Square(generateTransform(rank, file), idx);
                    file++;
                }
            }

            //If there is a letter, initialize that square as the corresponding piece
            if (Character.isAlphabetic(c)){
                Piece p = piecesMap.get(c);
                int idx = BoardUtility.getArrayIndex(rank, file);
                boardRep[idx] = new Square(generateTransform(rank, file), idx).setPiece(Piece.copy(p));

                if (p.type == Piece.KING) {
                    if (p.color) lData.whiteKingSquare = boardRep[idx];
                    else lData.blackKingSquare = boardRep[idx];
                }

                file++;
            }
        }

        lData.boardRepresentation = boardRep;
        lData.colorToMove = args[1].equalsIgnoreCase("w");

        String castling = args.length > 2 ? args[2] : "KQkq";
        lData.whiteCastleKing = castling.contains("K");
        lData.whiteCastleQueen = castling.contains("Q");
        lData.blackCastleKing = castling.contains("k");
        lData.blackCastleQueen = castling.contains("q");

        lData.plyCount = args.length > 4 ? Integer.parseInt(args[4]) : 0;

        return lData;
    }

    private static Transform generateTransform(int rank, int file){
        return new Transform(new Vector2(SQUARE_SIZE * file, SQUARE_SIZE * rank), new Vector2(SQUARE_SIZE, SQUARE_SIZE));
    }


    /**
     * Saves current board state into FEN notation
     * @param board ChessBoard class containing all savable information
     * @return String of FEN
     */
    public static String savePosition(ChessBoard board){
        StringBuilder finalFen = new StringBuilder();

        for (int rank = 7; rank >= 0; rank--){
            int emptyFiles = 0;
            for (int file = 0; file < 8; file++){
                Square square = board.getSquare(rank, file);

                if (!square.hasPiece()){
                    emptyFiles++;
                    continue;
                }

                if (emptyFiles != 0){
                    finalFen.append(emptyFiles);
                    emptyFiles = 0;
                }

                char pieceChar = '?';
                switch(square.getPiece().type){
                    case Piece.KING -> pieceChar = 'k';
                    case Piece.QUEEN -> pieceChar = 'q';
                    case Piece.BISHOP -> pieceChar = 'b';
                    case Piece.KNIGHT -> pieceChar = 'n';
                    case Piece.ROOK -> pieceChar = 'r';
                    case Piece.PAWN -> pieceChar = 'p';
                }
                finalFen.append(square.getPiece().color ? Character.toUpperCase(pieceChar) : pieceChar);
            }
            if (emptyFiles != 0) finalFen.append(emptyFiles);
            if (rank != 0) finalFen.append('/');
        }

        finalFen.append(" ");
        finalFen.append(board.colorToMove() ? 'w' : 'b').append(" ");

        boolean whiteKing = board.whiteKingSquare.hasPiece() && !board.whiteKingSquare.getPiece().hasMoved && board.getSquare(7).hasPiece() && !board.getSquare(7).getPiece().hasMoved;
        finalFen.append(whiteKing ? "K" : "");
        boolean whiteQueen = board.whiteKingSquare.hasPiece() && !board.whiteKingSquare.getPiece().hasMoved && board.getSquare(0).hasPiece() && !board.getSquare(0).getPiece().hasMoved;
        finalFen.append(whiteQueen ? "Q" : "");
        boolean blackKing = board.blackKingSquare.hasPiece() && !board.blackKingSquare.getPiece().hasMoved && board.getSquare(63).hasPiece() && !board.getSquare(63).getPiece().hasMoved;
        finalFen.append(blackKing ? "k" : "");
        boolean blackQueen = board.blackKingSquare.hasPiece() && !board.blackKingSquare.getPiece().hasMoved && board.getSquare(56).hasPiece() && !board.getSquare(56).getPiece().hasMoved;
        finalFen.append(blackQueen ? "q" : "");

        finalFen.append(whiteKing || blackKing || whiteQueen || blackQueen ? "" : "-");

        finalFen.append(" ");
        finalFen.append("-");
        finalFen.append(" ");
        finalFen.append(board.numPly);

        return finalFen.toString();
    }
}
