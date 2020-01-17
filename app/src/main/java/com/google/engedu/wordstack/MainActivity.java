/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    private Stack<LetterTile> placedTiles = new Stack<LetterTile>();
    private View word1LinearLayout;
    private View word2LinearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                if(word.length() == WORD_LENGTH){
                    words.add(word);
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLatyout(this);
        verticalLayout.addView(stackedLayout, 3);

        word1LinearLayout = findViewById(R.id.word1);
       // word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        word2LinearLayout = findViewById(R.id.word2);
       // word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());

    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }
                placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    if(tile.getParent() instanceof StackedLayout){
                        placedTiles.push(tile);
                    }
                    tile.moveToViewGroup((ViewGroup) v);
                    if (stackedLayout.empty()) {
                        TextView messageBox = (TextView) findViewById(R.id.message_box);
                        if (!checkLayouts()) {
                            messageBox.setText("Sorry, the words were: " + word1 + " " + word2);
                        }
                        else{
                            messageBox.setText("You found a valid solution! The words were: " + word1 + " " + word2);
                        }
                    }
                    return true;
            }
            return false;
        }
    }

    public boolean checkLayouts(){
        int count1 = ((LinearLayout)(word1LinearLayout)).getChildCount();
        int count2 = ((LinearLayout)(word2LinearLayout)).getChildCount();
        LetterTile letterView = null;
        String placed1 = "";
        String placed2 = "";
        Boolean english1 = false;
        Boolean english2 = false;
        for(int i = 0; i < count1; i++){
            letterView = (LetterTile)((LinearLayout)(word1LinearLayout)).getChildAt(i);
            placed1 += letterView.getLetter();
        }
        if(words.contains(placed1)){
            english1 = true;
        }
        for(int i = 0; i < count2; i++){
            letterView = (LetterTile)((LinearLayout)(word2LinearLayout)).getChildAt(i);
            placed2 += letterView.getLetter();
        }
        if(words.contains(placed1)){
            english2 = true;
        }
        return english1 && english2;
    }

    public boolean onStartGame(View view) {
        ((LinearLayout)word1LinearLayout).removeAllViews();
        ((LinearLayout)word2LinearLayout).removeAllViews();
        stackedLayout.clear();
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");
        int index1 = (int)(Math.random() * words.size());
        int index2 = (int)(Math.random() * words.size());
        word1 = words.get(index1);
        word2 = words.get(index2);
        String word1dec = word1;
        String word2dec = word2;
        int count1 = word1.length();
        int count2 = word2.length();
        double trigger = 0;
        String scrambled = "";
        while(!(count1 == 0 || count2 == 0)){
            trigger = Math.random();
            if(trigger > 0.5){
                scrambled = scrambled + word1dec.charAt(0);
                word1dec = word1dec.substring(1);
                count1--;
            }
            else{
                scrambled += word2dec.charAt(0);
                word2dec = word2dec.substring(1);
                count2--;
            }
        }
        if(count1 == 0){
            scrambled += word2dec;
        }
        else{
            scrambled += word1dec;
        }
        char[] scramArr = scrambled.toCharArray();
        messageBox.setText(scrambled);
        for(int i = scramArr.length - 1; i >= 0; i--){
            LetterTile letter = new LetterTile(this, scramArr[i]);
            stackedLayout.push(letter);
        }
        return true;
    }

    public boolean onUndo(View view) {
        if (!placedTiles.empty()) {
            LetterTile tile = placedTiles.pop();
            System.out.println(tile.getLetter());
            tile.moveToViewGroup(stackedLayout);
        }
        return true;
    }
}
