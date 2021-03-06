package com.bluebox.game;

import java.util.Iterator;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import sun.rmi.runtime.Log;

public class GameScreen implements Screen {
    final MyGdxGame game;

    Texture dropImage;
    Texture bucketImage;
    Texture bucketImageGreen;
    Sound dropSound;
    Music rainMusic;
    OrthographicCamera camera;
    Rectangle bucket;
    Array<Rectangle> raindrops;
    Array<Rectangle> cubos;
    long lastDropTime;
    int dropsGathered;
    boolean direccion=false;
    TextureAtlas textureAtlas;

    // Constant rows and columns of the sprite sheet
    private static final int FRAME_COLS = 4, FRAME_ROWS = 3;

    // Objects used
    Animation<TextureRegion> walkAnimation; // Must declare frame type (TextureRegion)
    Texture walkSheet;
    SpriteBatch spriteBatch;
    Sprite banana;
    private float elapsedTime = 0;
    int velocidadCubo=190;

    public Animation<TextureRegion> runningAnimation;
    // A variable for tracking elapsed time for the animation
    float stateTime;


    public GameScreen(final MyGdxGame game) {
        this.game = game;

        // load the images for the droplet and the bucket, 64x64 pixels eachx
        walkSheet = new Texture(Gdx.files.internal("spritesheet.png"));
        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("cubo.png"));
        bucketImageGreen = new Texture(Gdx.files.internal("cuboVerde9.png"));

        // load the drop sound effect and the rain background "music"
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain2.mp3"));
        rainMusic.setLooping(true);

        textureAtlas = new TextureAtlas("sprites.atlas");
        Sprite sprite = textureAtlas.createSprite("banana");
        banana = textureAtlas.createSprite("banana");
        runningAnimation = new Animation<TextureRegion>(0.133f, textureAtlas.findRegions("cuboVerde"), Animation.PlayMode.NORMAL);
// Use the split utility method to create a 2D array of TextureRegions. This is
        // possible because this sprite sheet contains frames of equal size and they are
        // all aligned.
        TextureRegion[][] tmp = TextureRegion.split(walkSheet,
                walkSheet.getWidth() / FRAME_COLS,
                walkSheet.getHeight() / FRAME_ROWS);

// Place the regions into a 1D array in the correct order, starting from the top
        // left, going across first. The Animation constructor requires a 1D array.
        TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                walkFrames[index++] = tmp[i][j];
            }
        }


        // Initialize the Animation with the frame interval and array of frames
        walkAnimation = new Animation<TextureRegion>(0.25f, walkFrames);

        // Instantiate a SpriteBatch for drawing and reset the elapsed animation
        // time to 0
        spriteBatch = new SpriteBatch();
        stateTime = 0f;

        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // create a Rectangle to logically represent the bucket
        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2; // center the bucket horizontally
        bucket.y = 20; // bottom left corner of the bucket is 20 pixels above
        // the bottom screen edge
        bucket.width = 64;
        bucket.height = 64;

        // create the raindrops array and spawn the first raindrop
        raindrops = new Array<Rectangle>();
        cubos= new Array<Rectangle>();
        spawnRaindrop();
        spawnCubo();

    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800 - 64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    private void spawnCubo() {
            Rectangle cubo = new Rectangle();
            cubo.x = 800 / 2 - 64 / 2;
            if (cubos.size==0){
                cubo.y = 0;
            }else{
                cubo.y = cubos.size*64;
            }
            cubo.width = 64;
            cubo.height = 64;
            cubos.add(cubo);
        Random rd = new Random();
        direccion=rd.nextBoolean();
        velocidadCubo=velocidadCubo+100;
    }

    @Override
    public void render(float delta) {
        // clear the screen with a dark blue color. The
        // arguments to glClearColor are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.

        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time
        TextureRegion currentFrame = walkAnimation.getKeyFrame(stateTime, true);


        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);
        // begin a new batch and draw the bucket and
        // all drops
        game.batch.begin();
        //banana.draw(game.batch);
        game.font.draw(game.batch, "Box to win: " + cubos.size+"/7", 10, 470);
        int i =0;
            elapsedTime += Gdx.graphics.getDeltaTime();
        for (Rectangle cubo : cubos) {
            if (cubos.size-1==i){
                game.batch.draw(bucketImage, cubo.x, cubo.y, cubo.width, cubo.height);
//                elapsedTime=0;
            }else if (cubos.size-2==i){
                game.batch.draw(runningAnimation.getKeyFrame(elapsedTime, false), cubo.x, cubo.y, cubo.width, cubo.height);
            }else{
                game.batch.draw(bucketImageGreen, cubo.x, cubo.y, cubo.width, cubo.height);
            }
            i++;
        }
        //Rectangle cubo = cubos.get(cubos.size-1);
        //game.batch.draw(bucketImage, cubo.x, cubo.y, cubo.width, cubo.height);

        //game.batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height);
        for (Rectangle raindrop : raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        game.batch.end();

        // process user input
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            /*Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            cubo.x = touchPos.x - 64 / 2;*/
//            spriteBatch.begin();
//            spriteBatch.draw(currentFrame, 50, 50); // Draw current frame at (50, 50)
//            spriteBatch.end();

            elapsedTime =0;
//            elapsedTime += Gdx.graphics.getDeltaTime();

//            game.batch.begin();
//            game.batch.draw(runningAnimation.getKeyFrame(elapsedTime, false), cubos.get(cubos.size-1).x, cubos.get(cubos.size-1).y, cubos.get(cubos.size-1).width, cubos.get(cubos.size-1).height);
//            game.batch.end();
            if (cubos.size==7){
                game.setScreen(new YouWin(game));
                dispose();
            }else if (cubos.size>1){
                if ((cubos.get(cubos.size-2).x-32)-(cubos.get(cubos.size-1).x-32)<=20 && (cubos.get(cubos.size-2).x-32)-(cubos.get(cubos.size-1).x-32)>=-20){
                    spawnCubo();
                }else{
                    game.setScreen(new MainMenuScreen(game));
                    dispose();
                }
            }else{
                spawnCubo();
            }
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT))
            cubos.get(cubos.size-1).x -= velocidadCubo * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Keys.RIGHT))
            cubos.get(cubos.size-1).x += velocidadCubo * Gdx.graphics.getDeltaTime();

        // make sure the bucket stays within the screen bounds
        if (cubos.get(cubos.size-1).x < 0){
            // bucket.x = 0;
            direccion=false;
        }
        if (cubos.get(cubos.size-1).x > 800 - 64){
            //bucket.x = 800 - 64;
            direccion=true;
        }

        // check if we need to create a new raindrop
        //if (TimeUtils.nanoTime() - lastDropTime > 1000000000)
            //spawnRaindrop();
        if (direccion){
            cubos.get(cubos.size-1).x -= velocidadCubo * Gdx.graphics.getDeltaTime();
        }else{
            cubos.get(cubos.size-1).x += velocidadCubo * Gdx.graphics.getDeltaTime();
        }

        // move the raindrops, remove any that are beneath the bottom edge of
        // the screen or that hit the bucket. In the later case we increase the
        // value our drops counter and add a sound effect.
        /*Iterator<Rectangle> iter = raindrops.iterator();
        while (iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if (raindrop.y + 64 < 0)
                iter.remove();
            if (raindrop.overlaps(bucket)) {
                dropsGathered++;
                dropSound.play();
                iter.remove();
            }
        }*/
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
        rainMusic.play();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
        textureAtlas.dispose();
    }

}