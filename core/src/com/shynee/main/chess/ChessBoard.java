package com.shynee.main.chess;

import com.badlogic.gdx.graphics.Color;
import com.shynee.main.chess.AI.Zobrist;
import com.shynee.main.utils.Constants;

import java.util.*;

/**
 * ChessBoard -- Responsible for all things relating to the virtual chess board.
 * This includes moving/unmoving pieces, keeping track of check/draw/pinned pieces, etc.
 * Also includes functions to highlight squares and legal moves.
 */
public class ChessBoard {

    private Square[] board;

    private final HashMap<Square, Move> possibleMoves;

    private final Stack<Piece> captures;
    private final Stack<Long> boardHistory;

    private final MoveCalculator moveCalculator;

    private final List<Square> coloredSquares;

    public final List<Move> pinnedPieces;
    public final List<Move> checkingMoves;
    public final List<Move> blockingMoves;

    public long zobristKey;
    public boolean gameRunning;
    public boolean playerColor;

    public int numPly = 0;
    public int numPlyForDraw = 0;

    public Square whiteKingSquare;
    public boolean isWhiteCheck;

    public Square blackKingSquare;
    public boolean isBlackCheck;

    public boolean isDoubleCheck;

    private boolean colorToMove;

    public ChessBoard(String FEN, boolean playerColor){
        this.board = new Square[64];
        this.colorToMove = true;
        this.playerColor = playerColor;

        this.isWhiteCheck = false;
        this.isBlackCheck = false;
        this.isDoubleCheck = false;

        this.possibleMoves = new HashMap<>();

        this.captures = new Stack<>();
        this.boardHistory = new Stack<>();

        this.coloredSquares = new ArrayList<>();
        this.pinnedPieces = new ArrayList<>();
        this.checkingMoves = new ArrayList<>();
        this.blockingMoves = new ArrayList<>();

        this.moveCalculator = new MoveCalculator();

        // Generate random zobrist numbers
        Zobrist.initializeKeys();
        // Create board based on FEN
        loadPosition(FEN, playerColor);
        // Precompute pseudo moves
        moveCalculator.precomputeMoves(this, playerColor);

        this.gameRunning = true;
    }

    /**
     * Moves a piece in the chess game.
     * Also keeps track of mate/draw.
     *
     * @param move Move to make on the board.
     * @param inSearch True if the method is called while searching for the best move.
     */
    public void makeMove(Move move, boolean inSearch) {
        Square previousSquare = board[move.piecePos];
        Square newSquare = board[move.squarePos];

        clearUI();
        pinnedPieces.clear();
        checkingMoves.clear();
        blockingMoves.clear();

        // Increment move counters
        numPly++;
        numPlyForDraw++;

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

        // Update king positions
        if (newSquare.hasKing()) {
            if (newSquare.getPiece().color) this.whiteKingSquare = newSquare;
            else this.blackKingSquare = newSquare;
        }

        // Highlight moves
        highlightSquare(previousSquare, Constants.MOVE_COLOR);
        highlightSquare(newSquare, Constants.MOVE_COLOR);

        moveCalculator.recomputeMoves(this, previousSquare, newSquare);

        this.isWhiteCheck = handleCheck(true);
        this.isBlackCheck = handleCheck(false);

        if (!inSearch) newSquare.getPiece().hasMoved = true;

        if (!inSearch && (moveCalculator.getLegalMoves(this, true).isEmpty() || moveCalculator.getLegalMoves(this, false).isEmpty())){
            if (isWhiteCheck || isBlackCheck) {
                System.out.println("checkmate");
            } else {
                System.out.println("draw by stalemate");
            }
            gameRunning = false;
        }

        if (!inSearch && numPlyForDraw == 50){
            System.out.println("draw by move counter");
            gameRunning = false;
        }

        this.zobristKey = Zobrist.generateKey(this);
        if (!inSearch && isRepeatPosition(zobristKey)){
            System.out.println("Draw by repetition");
            gameRunning = false;
        }

        this.colorToMove = !colorToMove;

        if (!inSearch){
            // Clear draw counter if a piece or pawn was captured
            if (newSquare.getPiece().type == Piece.PAWN || captures.peek() != null){
                numPlyForDraw = 0;
                boardHistory.clear();
            } else{
                boardHistory.push(zobristKey);
            }
        }
    }

    /**
     * Unmakes a move on the board.
     * This includes unmoving pieces, decrementing counters, replace takes, etc.
     * The piecePos/squarePos of the move are flipped (squarePos = current, piecePos = new)
     *
     * @param move Move to unmake on the board.
     * @param inSearch True if the method is called while searching for the best move.
     */
    public void unmakeMove(Move move, boolean inSearch){
        Square startSquare = board[move.piecePos];
        Square newSquare = board[move.squarePos];

        clearUI();
        pinnedPieces.clear();
        checkingMoves.clear();
        blockingMoves.clear();

        // Remove last capture and decrement moves
        Piece lastCapture = captures.pop();
        numPly--;

        // Reset king position
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

        // Recompute check
        this.isWhiteCheck = handleCheck(true);
        this.isBlackCheck = handleCheck(false);

        // Reset zobrist key
        this.zobristKey = Zobrist.generateKey(this);

        this.colorToMove = !colorToMove;

        if (!inSearch && boardHistory.size() > 0) {
            boardHistory.pop ();
        }
    }

    /**
     * Handles castling by moving the correctly moving the king/rook.
     * @param kingSquare Square that contains the king.
     * @param directionOffset Direction of the castle (1 = kingside, -1 = queenside)
     * @return Square the contains the king after moving.
     */
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

        // Recompute rook moves
        moveCalculator.recomputeMoves(this, rookSquare, newRookSquare);

        kingSquare.setPiece(null);
        rookSquare.setPiece(null);

        return newKingSquare;
    }

    /**
     * Determines whether the king is in check.
     * Also updates pinned pieces and checking moves.
     *
     * @param color Color of the king to determine check.
     * @return True if the king of the color is in check.
     */
    private boolean handleCheck(boolean color){
        Square kingSquare = color ? whiteKingSquare : blackKingSquare;
        boolean check = false;

        List<Move> moveIntersection = moveCalculator.findMoveIntersection(kingSquare, !color);
        if (moveIntersection.isEmpty()) return false;

        // Used to determine double check
        HashSet<Integer> checkingPieces = new HashSet<>();

        // Iterate through all pseudo moves that contain the king
        for (Move checkMove : moveIntersection){
            Square attackingPiece = board[checkMove.piecePos];

            List<Move> legalMoves = moveCalculator.getLegalMovesForSquare(attackingPiece, checkMove.directionOffset);

            // Check for pinned pieces (pseudo moves contain king but legal moves don't)
            if (!BoardUtility.containsSquare(legalMoves, kingSquare)) {
                Move pinnedPiece = BoardUtility.getPieceIfOne(board, legalMoves, color);
                if (pinnedPiece != null && BoardUtility.numPieces(board, moveCalculator.getMovesForSquare(attackingPiece, checkMove.directionOffset)) == 1) pinnedPieces.add(pinnedPiece);
                continue;
            }

            checkingPieces.add(checkMove.piecePos);

            // Update checking/blocking moves
            checkingMoves.addAll(moveCalculator.getMovesForSquare(attackingPiece, checkMove.directionOffset));
            blockingMoves.addAll(moveCalculator.getLegalMovesForSquare(attackingPiece, checkMove.directionOffset));
            blockingMoves.add(new Move(checkMove.piecePos, checkMove.piecePos, 0));

            check = true;
        }

        this.isDoubleCheck = checkingPieces.size() > 1;

        return check;
    }

    /**
     * Finds all possible moves for a given piece and displays them on the screen.
     * @param squarePosition Array position of the square to generate moves for.
     */
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
     * Uses FENUtility to generate load data based on FEN string.
     * Takes LoadData and applies it current board state.
     *
     * @param fen FEN string to load.
     */
    public void loadPosition(String fen, boolean color){
        LoadData boardData = FenUtility.loadPosition(fen, color);

        this.board = boardData.boardRepresentation;
        this.colorToMove = boardData.colorToMove;
        this.whiteKingSquare = boardData.whiteKingSquare;
        this.blackKingSquare = boardData.blackKingSquare;

        // Update castling rights
        if (!boardData.whiteCastleKing && board[7].hasPiece()) board[7].getPiece().hasMoved = true;
        if (!boardData.whiteCastleQueen && board[0].hasPiece()) board[0].getPiece().hasMoved = true;
        if (!boardData.blackCastleKing && board[63].hasPiece()) board[63].getPiece().hasMoved = true;
        if (!boardData.blackCastleQueen && board[56].hasPiece()) board[56].getPiece().hasMoved = true;

        this.numPly = boardData.plyCount;
        // Recompute zobrist key
        this.zobristKey = Zobrist.generateKey(this);
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

    /**
     * Checks if a move is legal on the current board state.
     * @param prev Current position of the piece.
     * @param current New position to move the piece to.
     * @return True if the move is legal.
     */
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

    public boolean isRepeatPosition(long zobristKey){
        return boardHistory.contains(zobristKey);
    }


}
