

package com.andryr.guitartuner;


public class Pitch {//тон для открытых струн
    float frequency;//частота
    String name;// здесь могла быть ваша реклама

    public Pitch(float frequency, String name) {
        this.frequency = frequency;
        this.name = name;
    }
}
