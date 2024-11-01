/*
b * Copyright 2013 Simon Willeke
 * contact: hamstercount@hotmail.com
 */

/*
    This file is part of Blockinger.

    Blockinger is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Blockinger is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Blockinger.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von Blockinger.

    Blockinger ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Option) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    Blockinger wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHELEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */

package org.blockinger.game.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.Window;

import androidx.fragment.app.FragmentActivity;

import org.blockinger.game.BlockBoardView;
import org.blockinger.game.R;
import org.blockinger.game.WorkThread;
import org.blockinger.game.components.Controls;
import org.blockinger.game.components.Display;
import org.blockinger.game.components.GameState;
import org.blockinger.game.components.Sound;

public class GameActivity extends FragmentActivity {

    public Sound sound;
    public Controls controls;
    public Display display;
    public GameState game;
    private WorkThread mainThread;
    private DefeatDialogFragment dialog;
    private boolean hardDropOnLeft;
    private boolean pauseOnBottom;
    private boolean useComposeUi;

    public static final int NEW_GAME = 0;
    public static final int RESUME_GAME = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        hardDropOnLeft = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_layoutswap", false);
        pauseOnBottom = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_pause_on_bottom", false);
        useComposeUi = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_composeui", false);
        /* Read Starting Arguments */
        Bundle b = getIntent().getExtras();
        int value = NEW_GAME;

        /* Create Components */
        game = (GameState) getLastCustomNonConfigurationInstance();
        if (game == null) {
            /* Check for Resuming (or Resumption?) */
            if (b != null) {
                value = b.getInt("mode");
            }

            if ((value == NEW_GAME)) {
                game = GameState.getNewInstance(this);
                game.setLevel(b.getInt("level"));
            } else {
                game = GameState.getInstance(this);
            }
        }

        game.reconnect(this);
        dialog = new DefeatDialogFragment();
        controls = new Controls(this);
        display = new Display(this);
        sound = new Sound(this);

        if (useComposeUi) {
            setContentView(GameUiKt.makeComposeUiView(this, game, controls, hardDropOnLeft, pauseOnBottom));
        } else {
            setContentView(hardDropOnLeft ? R.layout.activity_game_alt : R.layout.activity_game);
        }


        /* Init Components */
        if (game.isResumable()) {
            sound.startMusic(Sound.GAME_MUSIC, game.getSongtime());
        }
        sound.loadEffects();
        if (b != null) {
            value = b.getInt("mode");
            if (b.getString("playername") != null) {
                game.setPlayerName(b.getString("playername"));
            }
        } else {
            game.setPlayerName(getResources().getString(R.string.anonymous));
        }
        dialog.setCancelable(false);
        if (!game.isResumable()) {
            gameOver(game.getScore(), game.getTimeString(), game.getAPM());
        }

        if (!useComposeUi) {
            initViewThings();
        }
    }

    /**
     * Called by BlockBoardView upon completed creation
     */
    public void startGame(BlockBoardView caller) {
        mainThread = new WorkThread(this, caller.getHolder());
        mainThread.setFirstTime(false);
        game.setRunning(true);
        mainThread.setRunning(true);
        mainThread.start();
    }

    /**
     * Called by BlockBoardView upon destruction
     */
    public void destroyWorkThread() {
        boolean retry = true;
        mainThread.setRunning(false);
        while (retry) {
            try {
                mainThread.join();
                retry = false;
            } catch (InterruptedException ignored) {

            }
        }
    }

    /**
     * Called by GameState upon Defeat
     */
    public void putScore(long score) {
        String playerName = game.getPlayerName();
        if (playerName == null || playerName.equals("")) {
            playerName = getResources().getString(R.string.anonymous);//"Anonymous";
        }

        Intent data = new Intent();
        data.putExtra(MainActivity.PLAYERNAME_KEY, playerName);
        data.putExtra(MainActivity.SCORE_KEY, score);
        setResult(MainActivity.RESULT_OK, data);

        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sound.pause();
        sound.setInactive(true);
        game.setRunning(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sound.pause();
        sound.setInactive(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        game.setSongtime(sound.getSongtime());
        sound.release();
        sound = null;
        game.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sound.resume();
        sound.setInactive(false);

        /* Check for changed Layout */
        boolean tempswap = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_layoutswap", false);
        boolean useCompose = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_composeui", false);
        if (hardDropOnLeft != tempswap) {
            hardDropOnLeft = tempswap;
            if (useCompose) {
                setContentView(GameUiKt.makeComposeUiView(getBaseContext(), game, controls, hardDropOnLeft, pauseOnBottom));
            } else {
                if (hardDropOnLeft) {
                    setContentView(R.layout.activity_game_alt);
                } else {
                    setContentView(R.layout.activity_game_pause);
                }
            }
        }
        game.setRunning(true);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return game;
    }

    public void gameOver(long score, String gameTime, int apm) {
        dialog.setData(score, gameTime, apm);
        dialog.show(getSupportFragmentManager(), "hamster");
    }


    private void initViewThings() {
        /* Register Button callback Methods */
        findViewById(R.id.pausebutton_1).setOnClickListener(arg0 -> GameActivity.this.finish());
        findViewById(R.id.boardView).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                controls.boardPressed(event.getX(), event.getY());
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                controls.boardReleased();
            }
            v.performClick();
            return true;
        });
        findViewById(R.id.rightButton).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                controls.rightButtonPressed();
                findViewById(R.id.rightButton).setPressed(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                controls.rightButtonReleased();
                findViewById(R.id.rightButton).setPressed(false);
            }
            v.performClick();
            return true;
        });
        findViewById(R.id.leftButton).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                controls.leftButtonPressed();
                findViewById(R.id.leftButton).setPressed(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                controls.leftButtonReleased();
                findViewById(R.id.leftButton).setPressed(false);
            }
            v.performClick();
            return true;
        });
        findViewById(R.id.softDropButton).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                controls.downButtonPressed();
                findViewById(R.id.softDropButton).setPressed(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                controls.downButtonReleased();
                findViewById(R.id.softDropButton).setPressed(false);
            }
            v.performClick();
            return true;
        });
        findViewById(R.id.hardDropButton).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                controls.dropButtonPressed();
                findViewById(R.id.hardDropButton).setPressed(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                controls.dropButtonReleased();
                findViewById(R.id.hardDropButton).setPressed(false);
            }
            v.performClick();
            return true;
        });
        findViewById(R.id.rotateRightButton).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                controls.rotateRightPressed();
                findViewById(R.id.rotateRightButton).setPressed(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                controls.rotateRightReleased();
                findViewById(R.id.rotateRightButton).setPressed(false);
            }
            v.performClick();
            return true;
        });
        findViewById(R.id.rotateLeftButton).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                controls.rotateLeftPressed();
                findViewById(R.id.rotateLeftButton).setPressed(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                controls.rotateLeftReleased();
                findViewById(R.id.rotateLeftButton).setPressed(false);
            }
            v.performClick();
            return true;
        });

        ((BlockBoardView) findViewById(R.id.boardView)).init();
        ((BlockBoardView) findViewById(R.id.boardView)).setHost(this);
    }
}