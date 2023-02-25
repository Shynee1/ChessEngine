package com.shynee.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.shynee.main.abstracts.Scene;
import com.shynee.main.scenes.ChessScene;
import com.shynee.main.utils.Constants;

public class Main extends Game {

	private SpriteBatch batch;
	private static Scene currentScene = null;

	@Override
	public void create () {

		batch = new SpriteBatch();
		changeScene(new ChessScene(Constants.DEFAULT_FEN, true));
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 1, 1, 1);
		currentScene.update(Gdx.graphics.getDeltaTime(), batch);
	}
	
	@Override
	public void dispose () {

	}

	public static void changeScene(Scene newScene){
		if (currentScene != null) currentScene.dispose();

		currentScene = newScene;
		currentScene.start();
	}

	public static Scene currentScene(){
		return Main.currentScene;
	}
}
