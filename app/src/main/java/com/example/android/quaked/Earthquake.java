package com.example.android.quaked;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by HP on 6/22/2018.
 */

public class Earthquake {
    private String mMagnitude, mLocation;

    public String getMagnitude() {
        return mMagnitude;
    }

    public String getLocation() {
        return mLocation;
    }

    public Earthquake(String magnitude, String location) {
        this.mMagnitude = magnitude;
        this.mLocation = location;
    }
}
