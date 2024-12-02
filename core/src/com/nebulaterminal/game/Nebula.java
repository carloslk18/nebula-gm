package com.nebulaterminal.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.sql.Time;
import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;

public class Nebula extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img, tNave, tEnemy, tMissile;
	private Sprite nave, missile;
	private float posX, posY, velocity, xMissile, yMissile;
	private boolean attack, gameOver;
	private Array<Rectangle> enemies;
	private long lastEnemyTime;
	private int score, power, numEnemies;
	private FreeTypeFontGenerator generator;
	private FreeTypeFontGenerator.FreeTypeFontParameter parameter;
	private BitmapFont bitmap;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("bg.png");
		tNave = new Texture("spaceship.png");
		nave = new Sprite(tNave);
		posX = 0;
		posY = 0;
		velocity = 15;

		tMissile = new Texture("missile.png");
		missile = new Sprite(tMissile);
		xMissile = posX;
		yMissile = posY;
		attack = false;
		gameOver = false;

		tEnemy = new Texture("enemy.png");
		enemies = new Array<Rectangle>();
		lastEnemyTime = 0;
		numEnemies = 999999999;

		score = 0;
		generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
		parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.color = Color.WHITE;
		parameter.borderColor = Color.BLACK;
		parameter.borderWidth = 1;
		parameter.size = 30;
		bitmap = generator.generateFont(parameter);

		power = 3;

	}

	@Override
	public void render () {
		moveNave();
		moveMissile();
		moveEnemies();
		ScreenUtils.clear(1, 0, 0, 1);
		batch.begin();
		batch.draw(img, 0, 0);

		if(!gameOver){
			if(attack){
				batch.draw(missile, xMissile + nave.getWidth()/2, yMissile + nave.getHeight()/2-12);
			}
			batch.draw(nave, posX, posY);
			for(Rectangle enemy:enemies){
				batch.draw(tEnemy, enemy.x, enemy.y);
			}
			
			bitmap.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight()-20);
			bitmap.draw(batch, "Power: " + power, Gdx.graphics.getWidth()-130, Gdx.graphics.getHeight()-20);
		}
		else{
			bitmap.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight()-20);
			bitmap.draw(batch, "GAME OVER" , Gdx.graphics.getWidth()-150, Gdx.graphics.getHeight()-20);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.ENTER)){
			score = 0;
			power = 3;
			posX = 0;
			posY = 0;
			gameOver = false;
		}
		

		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
		tNave.dispose();
	}

	private void moveNave(){
		if (Gdx.input.isKeyPressed(Input.Keys.D)){
			if(posX < Gdx.graphics.getWidth()-nave.getWidth()) {
				posX += velocity;
			}
		}
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			if(posX > 0) {
				posX -= velocity;
			}
		}
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			if(posY < Gdx.graphics.getHeight()-nave.getHeight()) {
				posY += velocity;
			}
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			if(posY > 0) {
				posY -= velocity;
			}
		}
	}

	private void moveMissile(){
		if(Gdx.input.isKeyPressed(Input.Keys.SPACE) && !attack ){
			attack = true;
			yMissile = posY;
		}
		if(attack){
			if(xMissile < Gdx.graphics.getWidth()){
				xMissile += 40;
			}
			else{
			xMissile = posX;
			attack = false;
			}
		}			
		else{
			xMissile = posX; 
			yMissile = posY;
		}
	}

	private void spawnEnemies(){
		Rectangle enemy = new Rectangle(Gdx.graphics.getWidth(), MathUtils.random(0, Gdx.graphics.getHeight() - tEnemy.getHeight()), tEnemy.getWidth(), tEnemy.getHeight());
		enemies.add(enemy);
		lastEnemyTime = TimeUtils.nanoTime();

	}

	private void moveEnemies(){
		if (TimeUtils.nanoTime()-lastEnemyTime > numEnemies) {
			spawnEnemies();

		}

		for(Iterator<Rectangle> iter = enemies.iterator(); iter.hasNext();){
			Rectangle enemy = iter.next();
			enemy.x -= 400 * Gdx.graphics.getDeltaTime();

			
			//Colisão com o míssil

			if (collide(enemy.x, enemy.y, enemy.width, enemy.height, xMissile, yMissile, missile.getWidth(), missile.getHeight()) && attack) {
				++score;
				//System.out.println("Score: " + ++score);
				if (score % 10 == 0){
					numEnemies -= 100;
				}
				attack = false;
				iter.remove();
			}

			//Colisão com a nave

				else if (collide(enemy.x, enemy.y, enemy.width, enemy.height, posX, posY, nave.getWidth(), nave.getHeight())&& !gameOver){
				//System.out.print("Colidiu");
				--power;
				if (power <= 0) {
					gameOver = true;
				}
				iter.remove();
			}

			if (enemy.x + tEnemy.getWidth() < 0) {
				iter.remove();			
			}
		}
	}

	private boolean collide(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2){
		if (x1 + w1 > x2 && x1 < x2 + w2 && y1 + h1 > y2 && y1 < y2 + h2){
			return true;
		}
		return false;
	}
}