package io.github.PlantsVsZombies10;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

public class GameInputProcessor implements InputProcessor {
    private final Main main;

    public GameInputProcessor(Main main) {
        this.main = main;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        main.onTouch(screenX, screenY);
        return true;
    }

    @Override public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.UP || keycode == Input.Keys.W){
            main.setCameraMode(false);
        } else if(keycode == Input.Keys.DOWN || keycode == Input.Keys.S){
            main.setCameraMode(true);
        } else if (keycode == Input.Keys.D) {
            main.toggleDebug();
        }
        return true;
    }

    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }

}
