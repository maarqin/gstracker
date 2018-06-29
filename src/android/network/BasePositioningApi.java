package com.gstracker.cordova.plugin.network;

import android.content.Context;

import java.util.ArrayList;

import com.gstracker.cordova.plugin.model.Position;

/**
 * Created by thomaz on 05/06/18.
 */
public final class BasePositioningApi {

    public abstract static class All extends SuccessCallback<ArrayList<Position>> {

        public All(Context context) {
            super(context);

            rest.getPositions().enqueue(this);
        }
    }

    public abstract static class Create extends SuccessCallback<Void> {

        public Create(Context context, ArrayList<Position> positions) {
            super(context);

            rest.setPositions(positions).enqueue(this);
        }
    }

}
