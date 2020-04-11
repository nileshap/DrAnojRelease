package com.endo.intellih.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.endo.intellih.R;

/**
 * Created by admin on 10/5/2016.
 */
public class PlanetFragment extends Fragment {


    public static final String ARG_PLANET_NUMBER ="1";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.article_view, container, false);
    }
}
