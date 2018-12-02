package com.example.jhalm.tamz_projekt_adventure;

import java.util.ArrayList;
import java.util.List;

public class Player
{
    public double x;
    public double y;
    public int room;
    public List<Integer> keys;
    public int score;
    public int lives;
    public int maxLives;
    public boolean jump;

    public Player(int lives)
    {
        this.keys = new ArrayList<Integer>();
        this.maxLives = this.lives = lives;
        this.score = 0;
        this.jump = false;
    }
}
