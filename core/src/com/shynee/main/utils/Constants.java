package com.shynee.main.utils;

import com.badlogic.gdx.graphics.Color;

public class Constants {

    // World space definitions
    public final static int WORLD_WIDTH = 1280;
    public final static int WORLD_HEIGHT = 720;
    public final static int SQUARE_SIZE = Constants.WORLD_HEIGHT/8;

    // ChessBoard constants
    public final static String DEFAULT_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0";

    // Square colors
    public final static Color MOVE_COLOR = new Color(50, 50, 0, 0.2f);
    public final static Color TAKE_COLOR = new Color(1, 0, 0, 0.3f);

}
