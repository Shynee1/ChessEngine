package com.shynee.main.chess;

import java.util.*;

public class MoveCalculator {

    private HashMap<Square, List<Move>> pseudoMoves;
    private HashMap<Integer, int[]> directionOffsets;

    private Square[] squares;
    private ChessBoard board;
    private List<Move> possibleMoves;

    private boolean primaryPieceColor;
    private int squarePosition;
    private Square square;

    public MoveCalculator() {
        initDirectionOffsets();
    }

    public void precomputeMoves(ChessBoard chessBoard){
        this.pseudoMoves = new HashMap<>();
        this.board = chessBoard;
        this.squares = chessBoard.getSquares();

        for (Square square : squares) {
            if (square.hasPiece()) {
                pseudoMoves.put(square, calculateMove(square));
            }
        }
    }

    public void recomputeMoves(ChessBoard chessBoard, Square previousSquare, Square newSquare){
        this.squares = chessBoard.getSquares();
        this.board = chessBoard;

        pseudoMoves.remove(previousSquare);
        pseudoMoves.put(newSquare, calculateMove(newSquare));

    }

    public List<Move> getLegalMoves(ChessBoard chessBoard, boolean color, boolean inIntersection, boolean includeColor){
        List<Move> legalMoves = new ArrayList<>();
        this.board = chessBoard;

        boolean isWhiteChecked = board.isWhiteCheck;
        boolean isBlackChecked = board.isBlackCheck;

        if (isWhiteChecked && !inIntersection) legalMoves.addAll(recomputeBlocks(board.blockingMoves, true));
        if (isBlackChecked && !inIntersection) legalMoves.addAll(recomputeBlocks(board.blockingMoves, false));

        for (Square s : pseudoMoves.keySet()){
            if (s.getPiece().color != color) continue;

            List<Move> legals;
            if (inIntersection) legals = getLegalMovesForSquare(s, includeColor);
            else legals = getLegalMovesForSquare(s, s.getPiece().color?isWhiteChecked:isBlackChecked, board.checkingMoves, board.pinnedPieces);

            legalMoves.addAll(legals);
        }

        return legalMoves;

    }

    public List<Move> getLegalMoves(ChessBoard board, boolean color){
        return getLegalMoves(board, color, false, false);
    }

    public List<Move> getLegalCaptures(ChessBoard board, boolean color){
        List<Move> legalCaptures = getLegalMoves(board, color);
        legalCaptures.removeIf(m->!squares[m.squarePos].hasPiece());
        return legalCaptures;
    }

    private List<Move> recomputeBlocks(List<Move> blockingMoves, boolean color){
        List<Move> possibleBlocks = new ArrayList<>();

        for (Move m : blockingMoves){
            possibleBlocks.addAll(findLegalMoveIntersection(squares[m.squarePos], color, false, true));
        }
        return possibleBlocks;
    }


    public List<Move> findLegalMoveIntersection(Square square, boolean color, boolean includeColor, boolean forBlocks) {
        List<Move> legalMoveIntersection = new ArrayList<>();

        for (Move move : getLegalMoves(board, color, true, includeColor)){
            if (forBlocks && squares[move.piecePos].getPiece().type == Piece.KING) continue;
            if (move.squarePos == square.getArrayPosition()) legalMoveIntersection.add(move);
        }

        return legalMoveIntersection;
    }


    public List<Move> findMoveIntersection(Square square, boolean color){
        List<Move> intersectedSquares = new ArrayList<>();

        for (List<Move> moves : pseudoMoves.values()){
            for (Move m : moves) {
                if (m.squarePos == square.getArrayPosition() && squares[m.piecePos].getPiece().color == color) intersectedSquares.add(m);
            }

        }
        return intersectedSquares;
    }

    public List<Move> getLegalMovesForSquare(Square square, int directionOffset){
        List<Move> res = new ArrayList<>();
        for (Move m : getLegalMovesForSquare(square, false)){
            if (m.directionOffset == directionOffset) res.add(m);
        }
        return res;
    }

    private List<Move> getLegalKingMoves(Square kingSquare, boolean isColorChecked, List<Move> checkingMoves){

        List<Move> legalMoves = new ArrayList<>();
        int kingPos = kingSquare.getArrayPosition();

        if (canCastle(kingSquare, true) && !isColorChecked) legalMoves.add(new Move(kingPos,kingPos+3, 1).setCastle());
        if (canCastle(kingSquare, false) && !isColorChecked) legalMoves.add(new Move(kingPos, kingPos-4, -1).setCastle());

        for (Move m : getLegalMovesForSquare(kingSquare, false)){
            if (findLegalMoveIntersection(squares[m.squarePos], !kingSquare.getPiece().color, true, false).isEmpty()) legalMoves.add(m);

            if (!isColorChecked) continue;

            if (containsMove(m, checkingMoves)) legalMoves.remove(m);
        }

        return legalMoves;
    }

    private boolean containsMove(Move m, List<Move> moves){
        for (Move move : moves){
            if (m.squarePos == move.squarePos && move.directionOffset != 0) return true;
        }

        return false;
    }

    private boolean canCastle(Square kingSquare, boolean right){
        if (!kingSquare.hasPiece() || kingSquare.getPiece().hasMoved) return false;

        int addRookIdx = right ? 3 : -4;
        Square rookSquare = squares[kingSquare.getArrayPosition()+addRookIdx];

        if (!rookSquare.hasPiece() || rookSquare.getPiece().hasMoved || rookSquare.getPiece().type != Piece.ROOK) return false;

        if (right){
            for (int i = kingSquare.getArrayPosition()+1; i < kingSquare.getArrayPosition() + addRookIdx; i++){
                if (squares[i].hasPiece() && findLegalMoveIntersection(squares[i], !kingSquare.getPiece().color, false, false).isEmpty()) return false;
            }
        } else {
            for (int i = kingSquare.getArrayPosition()-1; i > kingSquare.getArrayPosition() + addRookIdx; i--){
                if (squares[i].hasPiece() && findLegalMoveIntersection(squares[i], !kingSquare.getPiece().color, false, false).isEmpty()) return false;
            }
        }

        return true;
    }

    public List<Move> getMovesForSquare(Square square, int directionOffset){
        List<Move> pseudoDirection = new ArrayList<>(pseudoMoves.get(square));
        pseudoDirection.removeIf(m -> m.directionOffset != directionOffset);
        return pseudoDirection;
    }

    private Move containsSquare(List<Move> moves, Square square){
        for (Move m : moves){
            if (square.getArrayPosition() == m.squarePos) return m;
        }
        return null;
    }

    public List<Move> getLegalMovesForSquare(Square square, boolean isColorChecked, List<Move> checkingMoves, List<Move> pinnedPieces){
        List<Move> legalMoves = new ArrayList<>();

        if (square.hasKing()) return getLegalKingMoves(square, isColorChecked, checkingMoves);

        if (isColorChecked) return legalMoves;

        Move pinnedPiece = containsSquare(pinnedPieces, square);

        if (pinnedPiece == null) legalMoves.addAll(getLegalMovesForSquare(square, false));
        else legalMoves.addAll(getLegalMovesForSquare(square, pinnedPiece.directionOffset));

        return legalMoves;
    }

    public List<Move> getLegalMovesForSquare(Square square, boolean includeColor){
        List<Move> pseudoMovesForSquare = pseudoMoves.get(square);
        List<Move> legalMoves = new ArrayList<>();

        HashSet<Integer> invalidDirections = new HashSet<>();

        for (Move m : pseudoMovesForSquare){
            Square s = squares[m.squarePos];

            if (!square.getPiece().hasMoved) m.setFirstMove();

            if (invalidDirections.contains(m.directionOffset)) continue;

            if (square.getPiece().type == Piece.PAWN){
                if (Math.abs(m.directionOffset) != 8) {
                    if (!s.hasPiece()) continue;
                } else {
                    if (squares[m.squarePos].hasPiece()) continue;

                    int firstSquarePos = square.getPiece().color ? m.squarePos-8 : m.squarePos+8;
                    if (firstSquarePos != m.piecePos && (squares[firstSquarePos].hasPiece())) continue;
                }
            }


            if (s.hasPiece()) {
                invalidDirections.add(m.directionOffset);
                if (s.getPiece().color == square.getPiece().color && !includeColor) continue;
            }

            legalMoves.add(m);
        }
        return legalMoves;
    }

    private List<Move> calculateMove(Square square){
        this.possibleMoves = new ArrayList<>();
        this.primaryPieceColor = square.getPiece().color;
        this.squarePosition = square.getArrayPosition();
        this.square = square;

        int pieceType = square.getPiece().type;

        int[] allPossibleDirections = directionOffsets.get(pieceType);
        for (int a : allPossibleDirections){
            switch(pieceType){
                case Piece.QUEEN,Piece.BISHOP,Piece.ROOK ->  calculateSlidingMove(a);
                case Piece.KNIGHT, Piece.KING -> calculateNonSlidingMove(a);
                case Piece.PAWN -> calculatePawnMove(a);
                default -> throw new RuntimeException("Not a recognized piece type");
            }

        }
        return possibleMoves;
    }

    private void calculatePawnMove(int directionOffset) {
        if (!primaryPieceColor) directionOffset = -directionOffset;

        int newPos = squarePosition + directionOffset;
        Piece pawn = square.getPiece();

        if (!isValid(squarePosition, newPos, Piece.PAWN)) return;

        if (!pawn.hasMoved && Math.abs(directionOffset) == 8 && isValid(squarePosition, newPos+directionOffset, Piece.PAWN)) {
            //Add square 2 spaces ahead
            possibleMoves.add(new Move(squarePosition,newPos+directionOffset, directionOffset));
        }

        Move pawnMove = new Move(squarePosition, newPos, directionOffset);

        int promotionSquare = primaryPieceColor ? 7 : 0;
        if (squares[newPos].getTransform().position.y/90 == promotionSquare) pawnMove.setPromotion();

        possibleMoves.add(pawnMove);
    }

    private void calculateNonSlidingMove(int directionOffset){
        int newPos = squarePosition+directionOffset;

        if (!isValid(squarePosition, newPos, square.getPiece().type)) return;

        possibleMoves.add(new Move(squarePosition, newPos, directionOffset));
    }

    private void calculateSlidingMove(int directionOffset){
        int newPos = squarePosition+directionOffset;
        int prevPos = squarePosition;

        while (isValid(prevPos, newPos, square.getPiece().type)) {
            possibleMoves.add(new Move(squarePosition, newPos, directionOffset));

            prevPos = newPos;
            newPos += directionOffset;
        }
    }

    private void initDirectionOffsets(){
        directionOffsets = new HashMap<>();
        directionOffsets.put(0, new int[]{7, 8, 9, -1, 1, -9, -8, -7});
        directionOffsets.put(1, new int[]{7, 8, 9, -1, 1, -9, -8, -7});
        directionOffsets.put(2, new int[]{7, 9, -7, -9});
        directionOffsets.put(3, new int[]{15, 17, 6, 10, -15, -17, -6, -10});
        directionOffsets.put(4, new int[]{8, 1, -8, -1});
        directionOffsets.put(5, new int[]{8, 7, 9});
    }

    private boolean isValid(int prevPos, int currentPos, int type){
        int[] newRF = ChessBoard.getRankAndFile(currentPos);
        int[] prevRF = ChessBoard.getRankAndFile(prevPos);
        boolean isValidForType;

        switch (type){
            case Piece.BISHOP, Piece.QUEEN, Piece.ROOK, Piece.KING -> isValidForType = checkSpacing(prevRF[0], newRF[0], prevRF[1], newRF[1], 1, true);
            case Piece.KNIGHT, Piece.PAWN -> isValidForType = checkSpacing(prevRF[0], newRF[0], prevRF[1], newRF[1], 2, false);
            default -> isValidForType = false;
        }
        return (currentPos >= 0 && currentPos < 64) && isValidForType;
    }

    private boolean checkSpacing(int prevRank, int newRank, int prevFile, int newFile, int target, boolean equals){
        int max = Math.max(Math.abs(prevRank - newRank), Math.abs(prevFile - newFile));
        return equals ? (max == target) : (max <= target);
    }
}
