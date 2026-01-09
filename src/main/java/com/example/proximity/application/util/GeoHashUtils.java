package com.example.proximity.application.util;

import java.util.ArrayList;
import java.util.List;

public final class GeoHashUtils {
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    private GeoHashUtils() {
    }

    public static String encode(double latitude, double longitude, int length) {
        double[] latRange = {-90.0, 90.0};
        double[] lonRange = {-180.0, 180.0};
        StringBuilder geohash = new StringBuilder();
        boolean even = true;
        int bit = 0;
        int ch = 0;

        while (geohash.length() < length) {
            if (even) {
                double mid = (lonRange[0] + lonRange[1]) / 2;
                if (longitude >= mid) {
                    ch |= 1 << (4 - bit);
                    lonRange[0] = mid;
                } else {
                    lonRange[1] = mid;
                }
            } else {
                double mid = (latRange[0] + latRange[1]) / 2;
                if (latitude >= mid) {
                    ch |= 1 << (4 - bit);
                    latRange[0] = mid;
                } else {
                    latRange[1] = mid;
                }
            }

            even = !even;
            if (bit < 4) {
                bit++;
            } else {
                geohash.append(BASE32.charAt(ch));
                bit = 0;
                ch = 0;
            }
        }

        return geohash.toString();
    }

    public static BoundingBox decodeBoundingBox(String geohash) {
        double[] latRange = {-90.0, 90.0};
        double[] lonRange = {-180.0, 180.0};
        boolean even = true;

        for (int i = 0; i < geohash.length(); i++) {
            int idx = BASE32.indexOf(geohash.charAt(i));
            for (int n = 4; n >= 0; n--) {
                int bit = (idx >> n) & 1;
                if (even) {
                    refineRange(lonRange, bit);
                } else {
                    refineRange(latRange, bit);
                }
                even = !even;
            }
        }

        return new BoundingBox(latRange[0], latRange[1], lonRange[0], lonRange[1]);
    }

    public static List<String> neighbors(String geohash) {
        BoundingBox box = decodeBoundingBox(geohash);
        double latSpan = box.maxLat() - box.minLat();
        double lonSpan = box.maxLon() - box.minLon();
        double lat = (box.minLat() + box.maxLat()) / 2;
        double lon = (box.minLon() + box.maxLon()) / 2;
        int length = geohash.length();

        List<String> neighbors = new ArrayList<>();
        for (int dLat = -1; dLat <= 1; dLat++) {
            for (int dLon = -1; dLon <= 1; dLon++) {
                double neighborLat = lat + (dLat * latSpan);
                double neighborLon = lon + (dLon * lonSpan);
                neighbors.add(encode(neighborLat, neighborLon, length));
            }
        }
        return neighbors;
    }

    private static void refineRange(double[] range, int bit) {
        double mid = (range[0] + range[1]) / 2;
        if (bit == 1) {
            range[0] = mid;
        } else {
            range[1] = mid;
        }
    }

    public record BoundingBox(double minLat, double maxLat, double minLon, double maxLon) {
    }
}
