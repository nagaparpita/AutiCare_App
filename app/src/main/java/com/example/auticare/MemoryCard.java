package com.example.auticare;

public class MemoryCard {
    public int identifier;
    public boolean isFaceUp = false;
    public boolean isMatched = false;

    public MemoryCard(int identifier) {
        this.identifier = identifier;
    }
}
