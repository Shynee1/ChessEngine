package com.shynee.main.chess;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.shynee.main.utils.Constants;
import com.shynee.main.utils.Transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ChessBoard {

    private final Square[] board;
    private final int squareSize = Constants.WORLD_HEIGHT/8;

    private final HashMap<Character, Piece> piecesMap;
    private final HashMap<Square, Move> possibleMoves;

    private final Stack<Piece> captures;
    public Stack<Move> lastMove= new Stack<>();
    private int numPly = 0;

    private final MoveCalculator moveCalculator;

    private final List<Square> coloredSquares;

    private final List<Move> pinnedPieces;
    private final List<Move> checkingMoves;
    private final List<Move> blockingMoves;

    public Square whiteKingSquare;
    public boolean isWhiteCheck;

    public Square blackKingSquare;
    public boolean isBlackCheck;

    private boolean lastMovePromotion;
    private boolean isTurnToMove;

    public ChessBoard(String FEN, boolean playerColor){
        this.board = new Square[64];
        this.isTurnToMove = playerColor;

        this.isWhiteCheck = false;
        this.isBlackCheck = false;
        this.lastMovePromotion = false;

        this.piecesMap = new HashMap<>();
        this.possibleMoves = new HashMap<>();

        this.captures = new Stack<>();

        this.coloredSquares = new ArrayList<>();
        this.pinnedPieces = new ArrayList<>();
        this.checkingMoves = new ArrayList<>();
        this.blockingMoves = new ArrayList<>();

        this.moveCalculator = new MoveCalculator();

        initPiecesMap();
        initFEN(FEN);

        moveCalculator.precomputeMoves(this);
    }

    public void makeMove(Move move){
        Square previousSquare = board[move.piecePos];
        Square newSquare = board[move.squarePos];

        clearUI();
        pinnedPieces.clear();
        checkingMoves.clear();
        blockingMoves.clear();

        try{
            previousSquare.getPiece().hasMoved = true;
        } catch (NullPointerException e){
            System.out.println("PiecePos: " + move.piecePos);
            System.out.println("SquarePos: " + move.squarePos);
        }

        numPly++;
        lastMove.push(move);

        if (move.isCastle) {
            newSquare = handleCastle(previousSquare, move.directionOffset);
            captures.push(null);
        }
        else {
            captures.push(newSquare.getPiece());
            newSquare.setPiece(previousSquare.getPiece());
            previousSquare.setPiece(null);
        }

        if (newSquare.hasKing()){
            if (newSquare.getPiece().color) this.whiteKingSquare = newSquare;
            else this.blackKingSquare = newSquare;
        }

        handlePromotions(newSquare);

        if (!colorToMove()){
            highlightSquare(previousSquare, Constants.MOVE_COLOR);
            highlightSquare(newSquare, Constants.MOVE_COLOR);
        }


        moveCalculator.recomputeMoves(this, previousSquare, newSquare);

        this.isWhiteCheck = handleCheck(true);
        this.isBlackCheck = handleCheck(false);

        moveCalculator.recomputeLegalMoves(isWhiteCheck, isBlackCheck, checkingMoves, pinnedPieces, blockingMoves);

        /*
        if ((isWhiteCheck && moveCalculator.getLegalMoves(true).isEmpty()) || (isBlackCheck && moveCalculator.getLegalMoves(false).isEmpty()))
            Main.changeScene(new ChessScene(Constants.DEFUALT_FEN, true));
            
         */


        this.isTurnToMove = !isTurnToMove;
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

        } else if (lastMovePromotion){
            startSquare.setPiece(new Piece(Piece.PAWN, newSquare.getPiece().color));;
            this.lastMovePromotion = false;
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

        moveCalculator.recomputeLegalMoves(isWhiteCheck, isBlackCheck, checkingMoves, pinnedPieces, blockingMoves);

        this.isTurnToMove = !isTurnToMove;
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

            if (!containsSquare(legalMoves, kingSquare)) {
                Move pinnedPiece = getPiece(legalMoves, color);
                if (pinnedPiece != null) pinnedPieces.add(pinnedPiece);
                continue;
            }

            checkingMoves.addAll(moveCalculator.getMovesForSquare(attackingPiece, checkMove.directionOffset));
            blockingMoves.addAll(moveCalculator.getLegalMovesForSquare(attackingPiece, checkMove.directionOffset));
            blockingMoves.add(new Move(checkMove.piecePos, checkMove.piecePos, 0));

            check = true;
        }

        return check;
    }

    private boolean containsSquare(List<Move> moves, Square target){
        for (Move m : moves){
            if (m.squarePos == target.getArrayPosition()) return true;
        }

        return false;
    }

    private Move getPiece(List<Move> moves, boolean color){
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

    private void handlePromotions(Square square){
        boolean pieceColor = square.getPiece().color;

        int targetSquare = pieceColor ? 7 : 0;
        if (square.getPiece().type == Piece.PAWN && square.getTransform().position.y/squareSize == targetSquare){
            this.lastMovePromotion = true;
            square.promotePiece(Piece.QUEEN);
        }
    }


    public void generatePossibleMoves(int squarePosition){
        Square square = board[squarePosition];

        List<Move> legalMoves = moveCalculator.getLegalMoves(square.getPiece().color);
        legalMoves.removeIf(m-> m.piecePos!=squarePosition);

        for (Move m : legalMoves){
            Square s = board[m.squarePos];

            possibleMoves.put(s, m);

            if (s.hasPiece()) s.setColor(Constants.TAKE_COLOR);
            else s.setPossibleMove(true);
        }
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

    public boolean canMove(int squarePos){
        return board[squarePos].hasPiece() && board[squarePos].getPiece().color == isTurnToMove;
    }

    /**
     * Initializes Square[] based on a given FEN string
     * @param FEN Representation of the starting chess board using characters and numbers
     */
    private void initFEN(String FEN){
        int rank = 7;
        int file = 0;

        for (char c : FEN.toCharArray()){
            if (c == '/'){
                file = 0;
                rank--;
                continue;
            }

            //If there is a number, initialize that many squares as empty
            if (Character.isDigit(c)) file = handleDigit(Character.getNumericValue(c), rank, file);

            //If there is a letter, initialize that square as the corresponding piece
            if (Character.isAlphabetic(c)) file = handleLetter(c, rank, file);
        }
    }

    private Transform generateTransform(int rank, int file){
        return new Transform(new Vector2(squareSize * file, squareSize * rank), new Vector2(squareSize, squareSize));
    }

    private int handleDigit(int digit, int rank, int file){
        for (int i = 0; i < digit; i++){
            int idx = getArrayIndex(rank, file);
            board[idx] = new Square(generateTransform(rank, file), idx);
            file++;
        }

        return file;
    }

    private int handleLetter(char c, int rank, int file){
        Piece p = piecesMap.get(c);
        int idx = getArrayIndex(rank, file);
        board[idx] = new Square(generateTransform(rank, file), idx).setPiece(Piece.copy(p));

        if (p.type == Piece.KING) {
            if (p.color) whiteKingSquare = board[idx];
            else blackKingSquare = board[idx];
        }

        return file+1;
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

    private void initPiecesMap(){
        piecesMap.put('k', new Piece(0, true));
        piecesMap.put('q', new Piece(1, true));
        piecesMap.put('b', new Piece(2, true));
        piecesMap.put('n', new Piece(3, true));
        piecesMap.put('r', new Piece(4, true));
        piecesMap.put('p', new Piece(5, true));
        piecesMap.put('K', new Piece(0, false));
        piecesMap.put('Q', new Piece(1, false));
        piecesMap.put('B', new Piece(2, false));
        piecesMap.put('N', new Piece(3, false));
        piecesMap.put('R', new Piece(4, false));
        piecesMap.put('P', new Piece(5, false));
    }

    public Square[] getSquares(){
        return board;
    }


    public List<Square> getSquares(boolean color){
        List<Square> squaresForColor = new ArrayList<>();
        for (Square s : board){
            if (s.hasPiece() && s.getPiece().color == color) squaresForColor.add(s);
        }

        return squaresForColor;
    }

    public MoveCalculator getMoveCalculator(){
        return this.moveCalculator;
    }

    public boolean colorToMove(){
        return isTurnToMove;
    }

    public int numPieces(int type, boolean color){
        int numPieces = 0;

        for (Square s : board){
            if (s.hasPiece() && s.getPiece().type == type && s.getPiece().color == color) numPieces++;
        }

        return numPieces;
    }

    public Move getMove(int prevPos, int newPos){
        for (Move m : getMoveCalculator().getLegalMoves()){
            if (m.piecePos == prevPos && m.squarePos == newPos) return m;
        }

        return null;
    }


}
