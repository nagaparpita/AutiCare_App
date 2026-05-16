package com.example.auticare;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryMatchGameActivity extends AppCompatActivity implements MemoryGameAdapter.CardClickListener {

    private RecyclerView rvBoard;
    private TextView tvMoves, tvMatches;
    private LinearLayout levelContainer;
    private Button btnRestart;
    private MemoryGameAdapter adapter;
    private List<MemoryCard> cards;
    
    private int indexOfSingleSelectedCard = -1;
    private int numMoves = 0;
    private int numPairsFound = 0;
    private int totalPairsNeeded = 0;
    private String currentLevelName = "";
    private long startTime;

    private DatabaseReference mDatabase;
    private String userId;

    private final int[] allImages = {
            R.drawable.img_dog, R.drawable.img_cat, R.drawable.img_car,
            R.drawable.img_bell, R.drawable.img_door, R.drawable.img_rain,
            R.drawable.ic_star, R.drawable.ic_smiley_face
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        userId = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        rvBoard = findViewById(R.id.rvBoard);
        tvMoves = findViewById(R.id.tvMoves);
        tvMatches = findViewById(R.id.tvMatches);
        levelContainer = findViewById(R.id.levelContainer);
        btnRestart = findViewById(R.id.btnRestart);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        findViewById(R.id.btnEasy).setOnClickListener(v -> setupGame(2, 2, "Easy"));
        findViewById(R.id.btnMedium).setOnClickListener(v -> setupGame(3, 2, "Medium"));
        findViewById(R.id.btnHard).setOnClickListener(v -> setupGame(4, 3, "Hard"));

        btnRestart.setOnClickListener(v -> {
            levelContainer.setVisibility(View.VISIBLE);
            rvBoard.setVisibility(View.GONE);
            btnRestart.setVisibility(View.GONE);
        });
    }

    private void setupGame(int rows, int cols, String levelName) {
        currentLevelName = levelName;
        int numCards = rows * cols;
        totalPairsNeeded = numCards / 2;
        numMoves = 0;
        numPairsFound = 0;
        indexOfSingleSelectedCard = -1;
        startTime = System.currentTimeMillis();

        updateLabels();

        levelContainer.setVisibility(View.GONE);
        rvBoard.setVisibility(View.VISIBLE);
        btnRestart.setVisibility(View.VISIBLE);

        cards = new ArrayList<>();
        List<Integer> selectedImages = new ArrayList<>();
        for (int i = 0; i < totalPairsNeeded; i++) {
            selectedImages.add(allImages[i % allImages.length]);
            selectedImages.add(allImages[i % allImages.length]);
        }
        Collections.shuffle(selectedImages);

        for (int imgRes : selectedImages) {
            cards.add(new MemoryCard(imgRes));
        }

        adapter = new MemoryGameAdapter(this, cards, this);
        rvBoard.setAdapter(adapter);
        rvBoard.setLayoutManager(new GridLayoutManager(this, cols));
        rvBoard.setHasFixedSize(true);
    }

    @Override
    public void onCardClicked(int position) {
        MemoryCard card = cards.get(position);

        if (card.isMatched || card.isFaceUp || (indexOfSingleSelectedCard != -1 && indexOfSingleSelectedCard == position)) {
            return;
        }

        if (indexOfSingleSelectedCard == -1) {
            // Flipped over 0 or 2 cards previously, so this is the first of a new pair
            restoreCards();
            indexOfSingleSelectedCard = position;
        } else {
            // This is the second card of a pair
            checkForMatch(indexOfSingleSelectedCard, position);
            indexOfSingleSelectedCard = -1;
        }

        card.isFaceUp = !card.isFaceUp;
        adapter.notifyDataSetChanged();
    }

    private void checkForMatch(int position1, int position2) {
        numMoves++;
        if (cards.get(position1).identifier == cards.get(position2).identifier) {
            cards.get(position1).isMatched = true;
            cards.get(position2).isMatched = true;
            numPairsFound++;
            Toast.makeText(this, "Match Found! 🌟", Toast.LENGTH_SHORT).show();
            
            if (numPairsFound == totalPairsNeeded) {
                showWinDialog();
            }
        }
        updateLabels();
    }

    private void restoreCards() {
        for (MemoryCard card : cards) {
            if (!card.isMatched) {
                card.isFaceUp = false;
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateLabels() {
        tvMoves.setText("Tries: " + numMoves);
        tvMatches.setText("Matches: " + numPairsFound + " / " + totalPairsNeeded);
    }

    private void showWinDialog() {
        long timeTaken = (System.currentTimeMillis() - startTime) / 1000;
        saveGameData(timeTaken);
        RewardManager.addStars(this, numPairsFound * 2);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_result, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        TextView tvTitle = dialogView.findViewById(R.id.tvResultTitle);
        TextView tvFinalScore = dialogView.findViewById(R.id.tvFinalScore);
        
        tvTitle.setText("Well Done!");
        tvFinalScore.setText("You finished " + currentLevelName + " level\nin " + numMoves + " tries and earned " + (numPairsFound * 2) + " stars!");

        dialogView.findViewById(R.id.btnRestart).setOnClickListener(v -> {
            levelContainer.setVisibility(View.VISIBLE);
            rvBoard.setVisibility(View.GONE);
            btnRestart.setVisibility(View.GONE);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.tvGoBack).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void saveGameData(long timeTaken) {
        if (userId == null) return;
        String key = mDatabase.child("game_reports").child(userId).child("memory_flip").push().getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("level", currentLevelName);
        data.put("totalMoves", numMoves);
        data.put("matchedPairs", numPairsFound);
        data.put("timeTaken", timeTaken);
        data.put("date", System.currentTimeMillis());
        if (key != null) mDatabase.child("game_reports").child(userId).child("memory_flip").child(key).setValue(data);
    }
}
