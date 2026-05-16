package com.example.auticare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MemoryGameAdapter extends RecyclerView.Adapter<MemoryGameAdapter.ViewHolder> {

    private final Context context;
    private final List<MemoryCard> cards;
    private final CardClickListener cardClickListener;

    public interface CardClickListener {
        void onCardClicked(int position);
    }

    public MemoryGameAdapter(Context context, List<MemoryCard> cards, CardClickListener cardClickListener) {
        this.context = context;
        this.cards = cards;
        this.cardClickListener = cardClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_memory_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemoryCard card = cards.get(position);
        holder.bind(card, position);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCardFront;
        private final ImageView ivCardBack;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCardFront = itemView.findViewById(R.id.ivCardFront);
            ivCardBack = itemView.findViewById(R.id.ivCardBack);
        }

        public void bind(MemoryCard card, int position) {
            ivCardFront.setImageResource(card.identifier);
            
            if (card.isFaceUp) {
                ivCardFront.setVisibility(View.VISIBLE);
                ivCardBack.setVisibility(View.GONE);
            } else {
                ivCardFront.setVisibility(View.GONE);
                ivCardBack.setVisibility(View.VISIBLE);
            }

            if (card.isMatched) {
                itemView.setAlpha(0.5f);
            } else {
                itemView.setAlpha(1.0f);
            }

            itemView.setOnClickListener(v -> cardClickListener.onCardClicked(position));
        }
    }
}
