package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;

public class MyWorm extends Worm {
    @SerializedName("weapon")
    public Weapon weapon;

    @SerializedName("snowballs")
    public snowballs snowballs;

    @SerializedName("bananaBombs")
    public bananaBombs bananaBombs;


}
