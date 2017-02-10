package org.onebusaway.android.io.test;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;

import org.onebusaway.android.app.Application;
import org.onebusaway.android.io.ObaApi;
import org.onebusaway.android.io.elements.ObaRoute;
import org.onebusaway.android.io.elements.ObaStop;
import org.onebusaway.android.io.request.ObaRouteIdsForAgencyRequest;
import org.onebusaway.android.io.request.ObaRouteIdsForAgencyResponse;
import org.onebusaway.android.io.request.ObaStopsForRouteRequest;
import org.onebusaway.android.io.request.ObaStopsForRouteResponse;
import org.onebusaway.android.io.request.ObaTripsForRouteResponse;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Z440-2 on 2/9/2017.
 */

public class RoutesForAgencyTest extends ObaTestCase{
        private static final int ROUTES_LOADER = 5677;
        private static final int VEHICLES_LOADER = 5700;
        private static final int ROUTE_LOADER = 5679;
        private Loader<ObaStopsForRouteResponse> mRouteLoader;
        private Loader<ObaRouteIdsForAgencyResponse> mRoutesLoader;
        ObaRoute mRoute;

        private RouteLoaderListener mRouteLoaderListener;

        private Loader<ObaTripsForRouteResponse> mVehiclesLoader;

        public void testST() {
            ObaRouteIdsForAgencyRequest.Builder builder =
                    new ObaRouteIdsForAgencyRequest.Builder(getContext(), "40");
            ObaRouteIdsForAgencyRequest request = builder.build();
            ObaRouteIdsForAgencyResponse response = request.call();
            assertOK(response);
            final String[] routes = response.getRouteIds();
            assertEquals("40_593", routes[1]);

            String finalRoute = "";
            for(String route :  Arrays.copyOfRange(routes, 0, 2)) {
                Bundle b = new Bundle();
                b.putString("mRouteId", route);
                finalRoute = route;
                mRouteLoaderListener = new RouteLoaderListener();
                mRouteLoader = mRouteLoaderListener.onCreateLoader(ROUTES_LOADER, b);
                mRouteLoader.registerListener(0, mRouteLoaderListener);
                mRouteLoader.startLoading();
            }
            assertEquals("40_593", finalRoute);
        }

        public void testRouteLoader() {
            Bundle b = new Bundle();
            b.putString("mRouteId", "1_44");
            mRouteLoaderListener = new RouteLoaderListener();
            mRouteLoader = mRouteLoaderListener.onCreateLoader(ROUTES_LOADER, b);
            mRouteLoader.registerListener(0, mRouteLoaderListener);
            mRouteLoader.startLoading();
        }

    public void testAtlantaUrl() {
        Application.get().setCustomApiUrl("atlanta.onebusaway.org");
        String apiUrl = Application.get().getCustomApiUrl();
        Log.i("url", apiUrl);
        assertEquals("atlanta.onebusaway.org", apiUrl);
    }

    //
    // Loaders
    //

    public static class RoutesLoader extends AsyncTaskLoader<ObaStopsForRouteResponse> {

        private final String mRouteId;

        public RoutesLoader(Context context, String routeId) {
            super(context);
            mRouteId = routeId;
        }

        @Override
        public ObaStopsForRouteResponse loadInBackground() {
            if (Application.get().getCurrentRegion() == null &&
                    TextUtils.isEmpty(Application.get().getCustomApiUrl())) {
                return null;
            }
            //Make OBA REST API call to the server and return result
            return new ObaStopsForRouteRequest.Builder(getContext(), mRouteId)
                    .setIncludeShapes(true)
                    .build()
                    .call();
        }

        @Override
        public void deliverResult(ObaStopsForRouteResponse data) {
            //mResponse = data;
            super.deliverResult(data);
        }

        @Override
        public void onStartLoading() {
            forceLoad();
        }
    }

    public class RouteLoaderListener implements LoaderManager.LoaderCallbacks<ObaStopsForRouteResponse>,
            Loader.OnLoadCompleteListener<ObaStopsForRouteResponse> {

        @Override
        public Loader<ObaStopsForRouteResponse> onCreateLoader(int id,
                                                               Bundle args) {
            return new RoutesLoader(getContext(), args.getString("mRouteId"));
        }

        @Override
        public void onLoadFinished(Loader<ObaStopsForRouteResponse> loader,
                                   ObaStopsForRouteResponse response) {

            if (response == null || response.getCode() != ObaApi.OBA_OK) {
                return;
            }

            ObaRoute route = response.getRoute(response.getRouteId());

            assertEquals("40", route.getAgencyId());
            // Set the stops for this route
            List<ObaStop> stops = response.getStops();

        }

        @Override
        public void onLoaderReset(Loader<ObaStopsForRouteResponse> loader) {

        }

        @Override
        public void onLoadComplete(Loader<ObaStopsForRouteResponse> loader,
                                   ObaStopsForRouteResponse response) {
            onLoadFinished(loader, response);
        }
    }

    /*
    private static class RouteLoader extends AsyncTaskLoader<ObaRouteIdsForAgencyResponse> {

        //private final double lat;
        //private final double lon;

        public RouteLoader(Context context) {
            super(context);
        }

        @Override
        public ObaRouteIdsForAgencyResponse loadInBackground() {
            if (Application.get().getCurrentRegion() == null &&
                    TextUtils.isEmpty(Application.get().getCustomApiUrl())) {
                //We don't have region info or manually entered API to know what server to contact
                Log.d(TAG, "Trying to load stops for route from server " +
                        "without OBA REST API endpoint, aborting...");
                return null;
            }
            //Location location = LocationUtils.makeLocation(33.7490, 84.3880);
            //Make OBA REST API call to the server and return result
            return new ObaRouteIdsForAgencyRequest.Builder(getContext(), "MARTA")
                    .build()
                    .call();

        }

        @Override
        public void deliverResult(ObaRouteIdsForAgencyResponse data) {
            //mResponse = data;
            super.deliverResult(data);
        }

        @Override
        public void onStartLoading() {
            forceLoad();
        }
    }

    public class RoutesLoaderListener implements LoaderManager.LoaderCallbacks<ObaRouteIdsForAgencyResponse>,
            Loader.OnLoadCompleteListener<ObaRouteIdsForAgencyResponse> {

        @Override
        public Loader<ObaRouteIdsForAgencyResponse> onCreateLoader(int id,
                                                                   Bundle args) {
            return new RouteLoader(mFragment.getActivity());
        }

        @Override
        public void onLoadFinished(Loader<ObaRouteIdsForAgencyResponse> loader,
                                   ObaRouteIdsForAgencyResponse response) {

            if (response == null || response.getCode() != ObaApi.OBA_OK) {
                BaseMapFragment.showMapError(response);
                return;
            }
            String[] routes = response.getRouteIds();
            for(String route :  Arrays.copyOfRange(routes, 1, 5)) {
                Bundle b = new Bundle();
                b.putString("mRouteId", route);

                mRouteLoaderListener = new RouteLoaderListener();
                mRouteLoader = mRouteLoaderListener.onCreateLoader(ROUTES_LOADER, b);
                mRouteLoader.registerListener(0, mRouteLoaderListener);
                mRouteLoader.startLoading();

                mVehicleLoaderListener = new RoutesMapController.VehicleLoaderListener();
                mVehiclesLoader = mVehicleLoaderListener.onCreateLoader(VEHICLES_LOADER, b);
                mVehiclesLoader.registerListener(0, mVehicleLoaderListener);
                mVehiclesLoader.startLoading();

            }

            // wait to zoom till we have the right response
            obaMapView.postInvalidate();
        }

        @Override
        public void onLoaderReset(Loader<ObaRouteIdsForAgencyResponse> loader) {

        }

        @Override
        public void onLoadComplete(Loader<ObaRouteIdsForAgencyResponse> loader,
                                   ObaRouteIdsForAgencyResponse response) {
            onLoadFinished(loader, response);
        }
    }*/
}