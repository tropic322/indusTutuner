

package com.andryr.guitartuner;

import android.content.Context;

import java.lang.ref.PhantomReference;


public class Tuning {
    String name;// имя строя
    Pitch[] pitches;// технические данные для настройки (частоты и название ноты)

    public Tuning(String name, Pitch[] pitches) {
        this.name = name;
        this.pitches = pitches;
    }


    public int closestPitchIndex(float freq) {//скорее всего подсчет частоты
        int index = -1;
        float dist = Float.MAX_VALUE;
        for (int i = 0; i < pitches.length; i++) {
            Pitch pitch = pitches[i];
            float d = Math.abs(freq - pitch.frequency);
            if (d < dist) {
                index = i;
                dist = d;
            }
        }
        return index;
    }

    public static Tuning getTuning(Context context, String name) {//выделить один метод для изменения, вынести блоки питчей в другйо класс
        if (name.equals(context.getString(R.string.standard_tuning_val))) {//здесь можно использовать map
            return new Tuning(name,
                    new Pitch[]{
                            new Pitch(82.41F, "E"),
                            new Pitch(110.00F, "A"),
                            new Pitch(146.83F, "D"),
                            new Pitch(196.00F, "G"),
                            new Pitch(246.94F, "B"),
                            new Pitch(329.63F, "E"),
                    });
        }
        else if (name.equals(context.getString(R.string.open_a_tuning_val))) {
            return new Tuning(name,
                    new Pitch[]{
                            new Pitch(82.41F, "E"),
                            new Pitch(110.00F, "A"),
                            new Pitch(164.81F, "E"),
                            new Pitch(220.00F, "A"),
                            new Pitch(277.18F, "C"),
                            new Pitch(329.63F, "E"),
                    });
        }
        else if (name.equals(context.getString(R.string.open_g_tuning_val))) {
            return new Tuning(name,
                    new Pitch[]{
                            new Pitch(73.42F, "D"),
                            new Pitch(98.00F, "G"),
                            new Pitch(146.83F, "D"),
                            new Pitch(196.00F, "G"),
                            new Pitch(246.94F, "B"),
                            new Pitch(293.66F, "D"),
                    });
        }
        else if (name.equals(context.getString(R.string.open_d_tuning_val))) {
            return new Tuning(name,
                    new Pitch[]{
                            new Pitch(73.42F, "D"),
                            new Pitch(110.00F, "A"),
                            new Pitch(146.83F, "D"),
                            new Pitch(185.00F, "F"),
                            new Pitch(220.00F, "A"),
                            new Pitch(293.66F, "D"),
                    });
        }
        else if (name.equals(context.getString(R.string.drop_d_tuning_val))) {
            return new Tuning(name,
                    new Pitch[]{
                            new Pitch(73.42F, "D"),
                            new Pitch(110.00F, "A"),
                            new Pitch(146.83F, "D"),
                            new Pitch(196.00F, "G"),
                            new Pitch(246.94F, "B"),
                            new Pitch(329.63F, "E"),
                    });
        }
        return null;
    }


}
