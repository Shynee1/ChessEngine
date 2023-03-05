package com.shynee.main.chess;

import com.badlogic.gdx.graphics.Color;
import com.shynee.main.Main;
import com.shynee.main.scenes.ChessScene;
import com.shynee.main.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ChessBoard {

    private Square[] board;

    private final HashMap<Square, Move> possibleMoves;

    private final Stack<Piece> captures;
    public int numPly = 0;

    private final MoveCalculator moveCalculator;

    private final List<Square> coloredSquares;

    public final List<Move> pinnedPieces;
    public final List<Move> checkingMoves;
    public final List<Move> blockingMoves;

    public boolean gameRunning = false;

    public Square whiteKingSquare;
    public boolean isWhiteCheck;

    public Square blackKingSquare;
    public boolean isBlackCheck;

    private boolean colorToMove;

    public ChessBoard(String FEN, boolean playerColor){
        this.board = new Square[64];
        this.colorToMove = playerColor;

        this.isWhiteCheck = false;
        this.isBlackCheck = false;

        this.possibleMoves = new HashMap<>();

        this.captures = new Stack<>();

        this.coloredSquares = new ArrayList<>();
        this.pinnedPieces = new ArrayList<>();
        this.checkingMoves = new ArrayList<>();
        this.blockingMoves = new ArrayList<>();

        this.moveCalculator = new MoveCalculator();

        loadPosition(FEN);

        moveCalculator.precomputeMoves(this);

        this.gameRunning = true;
    }

    public void makeMove(Move move, boolean inSearch) {
        Square previousSquare = board[move.piecePos];
        Square newSquare = board[move.squarePos];

        clearUI();
        pinnedPieces.clear();
        checkingMoves.clear();
        blockingMoves.clear();

        previousSquare.getPiece().hasMoved = true;

        numPly++;

        if (move.isCastle) {
            newSquare = handleCastle(previousSquare, move.directionOffset);
            captures.push(null);
        } else if (move.isPromotion) {
            captures.push(newSquare.getPiece());
            newSquare.promotePiece(Piece.QUEEN, previousSquare.getPiece().color);
            previousSquare.setPiece(null);
        } else {
            captures.push(newSquare.getPiece());
            newSquare.setPiece(previousSquare.getPiece());
            previousSquare.setPiece(null);
        }

        if (newSquare.hasKing()) {
            if (newSquare.getPiece().color) this.whiteKingSquare = newSquare;
            else this.blackKingSquare = newSquare;
        }

        highlightSquare(previousSquare, Constants.MOVE_COLOR);
        highlightSquare(newSquare, Constants.MOVE_COLOR);

        moveCalculator.recomputeMoves(this, previousSquare, newSquare);

        this.isWhiteCheck = handleCheck(true);
        this.isBlackCheck = handleCheck(false);

        if (!inSearch && numPly == 100){
            System.out.println("draw by move counter");
            gameRunning = false;
        }

        if (!inSearch && ((isWhiteCheck && moveCalculator.getLegalMoves(this, true).isEmpty() || (isBlackCheck && moveCalculator.getLegalMoves(this, false).isEmpty())))){
            System.out.println("checkmate");
            gameRunning = false;
        }


        this.colorToMove = !colorToMove;

        //if (!inSearch) System.out.println(FenUtility.savePosition(this));
    }

    public void unmakeMove(Move move){
        Square startSquare = board[move.piecePos];
        Square newSquare = board[move.squarePos];

        clearUI();
        pinnedPieces.clear();
        checkingMoves.clear();
        blockingMoves.clear();

        if (move.isFirstMove && !move.isCastle) newSquare.getPiece().hasMoved = false;

        Piece lastCapture = captures.pop();
        numPly--;

        if (newSquare.hasKing() && !move.isCastle){
            if (newSquare.getPiece().color) whiteKingSquare = startSquare;
            else blackKingSquare = startSquare;
        }

        if (move.isCastle){
            Square rookSquare = board[move.piecePos+move.directionOffset];
            Square previousRookSquare = board[move.squarePos];

            previousRookSquare.setPiece(rookSquare.getPiece());
            rookSquare.setPiece(null);

            Square kingSquare = board[move.directionOffset>0 ? move.piecePos+2 : move.piecePos-2];
            Square previousKingSquare = board[move.piecePos];

            previousKingSquare.setPiece(kingSquare.getPiece());
            kingSquare.setPiece(null);

            if (previousKingSquare.getPiece().color) this.whiteKingSquare = previousKingSquare;
            else this.blackKingSquare = previousKingSquare;

            previousKingSquare.getPiece().hasMoved = false;
            previousRookSquare.getPiece().hasMoved = false;

            moveCalculator.recomputeMoves(this, kingSquare, previousKingSquare);
            moveCalculator.recomputeMoves(this, rookSquare, previousRookSquare);

        } else if (move.isPromotion){
            startSquare.setPiece(new Piece(Piece.PAWN, newSquare.getPiece().color));
            newSquare.setPiece(lastCapture);
        } else {
            startSquare.setPiece(newSquare.getPiece());
            newSquare.setPiece(lastCapture);
        }

        if (!move.isCastle) moveCalculator.recomputeMoves(this, newSquare, startSquare);
        if (lastCapture != null) {
            moveCalculator.recomputeMoves(this, newSquare, newSquare);
        }

        this.isWhiteCheck = handleCheck(true);
        this.isBlackCheck = handleCheck(false);

        this.colorToMove = !colorToMove;
    }

    private Square handleCastle(Square kingSquare, int directionOffset){
        boolean isNegative = directionOffset > 0;

        int kingPos = kingSquare.getArrayPosition();
        int rookPos = isNegative ? kingPos+3 : kingPos-4;

        int kingSpaces = isNegative ? 2 : -2;
        int rookSpaces = isNegative ? -2 : 3;

        Square rookSquare = board[rookPos];

        Square newKingSquare = board[kingPos + kingSpaces];
        Square newRookSquare = board[rookPos + rookSpaces];

        newRookSquare.setPiece(rookSquare.getPiece());
        newKingSquare.setPiece(kingSquare.getPiece());

        moveCalculator.recomputeMoves(this, rookSquare, newRookSquare);

        kingSquare.setPiece(null);
        rookSquare.setPiece(null);

        return newKingSquare;
    }

    private boolean handleCheck(boolean color){
        Square kingSquare = color ? whiteKingSquare : blackKingSquare;

        boolean check = false;

        List<Move> moveIntersection = moveCalculator.findMoveIntersection(kingSquare, !color);
        if (moveIntersection.isEmpty()) return false;

        for (Move checkMove : moveIntersection){
            Square attackingPiece = board[checkMove.piecePos];

            List<Move> legalMoves = moveCalculator.getLegalMovesForSquare(attackingPiece, checkMove.directionOffset);

            if (!BoardUtility.containsSquare(legalMoves, kingSquare)) {
                Move pinnedPiece = BoardUtility.getPieceIfOne(board, legalMoves, color);
                if (pinnedPiece != null && BoardUtility.numPieces(board, moveCalculator.getMovesForSquare(attackingPiece, checkMove.directionOffset)) == 1) pinnedPieces.add(pinnedPiece);
                continue;
            }

            checkingMoves.addAll(moveCalculator.getMovesForSquare(attackingPiece, checkMove.directionOffset));
            blockingMoves.addAll(moveCalculator.getLegalMovesForSquare(attackingPiece, checkMove.directionOffset));
            blockingMoves.add(new Move(checkMove.piecePos, checkMove.piecePos, 0));

            check = true;
        }

        return check;
    }

    public void generatePossibleMoves(int squarePosition){
        List<Move> legalMoves = moveCalculator.getLegalMoves(this, colorToMove);
        legalMoves.removeIf(m-> m.piecePos!=squarePosition);

        for (Move m : legalMoves){
            Square s = board[m.squarePos];

            possibleMoves.put(s, m);

            if (s.hasPiece()) s.setColor(Constants.TAKE_COLOR);
            else s.setPossibleMove(true);
        }
    }

    /**
     * Loads board/values from FEN string
     * @param fen FEN string to load
     */
    public void loadPosition(String fen){
        LoadData boardData = FenUtility.loadPosition(fen);

        this.board = boardData.boardRepresentation;
        this.colorToMove = boardData.colorToMove;
        this.whiteKingSquare = boardData.whiteKingSquare;
        this.blackKingSquare = boardData.blackKingSquare;

        if (!boardData.whiteCastleKing && board[7].hasPiece()) board[7].getPiece().hasMoved = true;
        if (!boardData.whiteCastleQueen && board[0].hasPiece()) board[0].getPiece().hasMoved = true;
        if (!boardData.blackCastleKing && board[63].hasPiece()) board[63].getPiece().hasMoved = true;
        if (!boardData.blackCastleQueen && board[56].hasPiece()) board[56].getPiece().hasMoved = true;

        this.numPly = boardData.plyCount;
    }

    public void highlightSquare(int squarePosition, Color color){
        highlightSquare(board[squarePosition], color);
    }

    public void highlightSquare(Square square, Color color){
        square.setColor(color);
        coloredSquares.add(square);
    }

    public void clearColors(){
        for (Square s : coloredSquares){
            s.setColor(null);
        }
        coloredSquares.clear();
    }

    public void clearUI(){
        for (Square s : possibleMoves.keySet()){
            s.setPossibleMove(false);
            if (s.getColor() == Constants.TAKE_COLOR) s.setColor(null);
        }

        clearColors();

        possibleMoves.clear();
    }

    public boolean validateMove(int prev, int current){
        Square prevSquare = board[prev];
        Square currentSquare = board[current];

        return prevSquare != currentSquare && possibleMoves.containsKey(currentSquare);
    }

    public Square getSquare(int squarePos){
        return board[squarePos];
    }

    public Square getSquare(int rank, int file){
        return board[BoardUtility.getArrayIndex(rank, file)];
    }

    public Square[] getSquares(){
        return board;
    }

    public MoveCalculator getMoveCalculator(){
        return this.moveCalculator;
    }

    public boolean colorToMove(){
        return colorToMove;
    }

    public int numPieces(int type, boolean color){
        int numPieces = 0;

        for (Square s : board){
            if (s.hasPiece() && s.getPiece().type == type && s.getPiece().color == color) numPieces++;
        }

        return numPieces;
    }

    public List<Integer> getPiecePosList(int pieceType, boolean color){
        List<Integer> piecePosList = new ArrayList<>();

        for (Square s : board){
            if (s.hasPiece() && s.getPiece().type == pieceType && s.getPiece().color == color) piecePosList.add(s.getArrayPosition());
        }

        return piecePosList;
    }

    public Move getMove(int prevPos, int newPos){
        for (Move m : getMoveCalculator().getLegalMoves(this, colorToMove)){
            if (m.piecePos == prevPos && m.squarePos == newPos) return m;
        }

        return null;
    }


}
