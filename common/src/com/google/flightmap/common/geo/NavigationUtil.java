/*
 * Copyright (C) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.flightmap.common.geo;

import com.google.flightmap.common.data.LatLng;

/**
 * Utility class for navigation calculations such as distance, bearing, etc.
 */
public class NavigationUtil {
  /**
   * Multiply meters by this constant to obtain nautical miles.
   */
  public static final double METERS_TO_NM = 0.000539956803;

  /**
   * Multiply meters by this constant to obtain statute miles.
   */
  public static final double METERS_TO_MILE = 0.000621371192;

  /**
   * Multiply meters by this constant to obtain kilometers.
   */
  public static final double METERS_TO_KM = .001;

  /**
   * Meter constant.
   */
  public static final double METERS = 1;

  /**
   * Multiply meters by this constant to obtain feet.
   */
  public static final double METERS_TO_FEET = 3.2808399;

  /**
   * Multiply meters-per-second by this constant to obtain knots.
   */
  public static final double METERS_PER_SEC_TO_KNOTS = 1.94384449;

  /**
   * Multiply meters-per-second by this constant to obtain (statute) miles per
   * hour.
   */
  public static final double METERS_PER_SEC_TO_MPH = 2.23693629;

  /**
   * Multiply meters-per-second by this constant to obtain kilometers per hour.
   */
  public static final double METERS_PER_SEC_TO_KPH = 3.6;

  /** Earth radius in meters. */
  public static final double EARTH_RADIUS = 6371009;

  /**
   * Session timestamp for caching purposes.
   */
  private static final long SESSION_TIME_MILLIS = System.currentTimeMillis();

  /**
   * Utility class: default and only constructor is private.
   */
  private NavigationUtil() {
  }

  /**
   * Simplifies display of distances and speeds in various units.
   * <p>
   * Short distance units are used when the main unit distance is less than 1.0.
   * Rather than showing fractional units, switch to a shorter unit (e.g. go
   * from kilometers to meters).
   * <p>
   * Note there are no short speed units. There hasn't been a need for them, but
   * they could be added here later if needed.
   */
  public enum DistanceUnits {
    MILES("mi", METERS_TO_MILE, "ft", METERS_TO_FEET, "mph", METERS_PER_SEC_TO_MPH),
    NAUTICAL_MILES("nm", METERS_TO_NM, "ft", METERS_TO_FEET, "kts", METERS_PER_SEC_TO_KNOTS),
    KILOMETERS("km", METERS_TO_KM, "m", METERS, "kph", METERS_PER_SEC_TO_KPH);

    /** Distance abbreviation such as mi or nm. */
    public final String distanceAbbreviation;
    /** Short distance abbreviation such as ft or m. */
    public final String shortDistanceAbbreviation;
    /** Speed abbreviation such as mph or kts. */
    public final String speedAbbreviation;

    /** Multiply meters by this value to convert to this unit distance. */
    private final double perMeter;
    /** Multiply meters by this value to convert to short unit distance. */
    private final double shortDistancePerMeter;
    /** Multiply meters per second by this value to convert to this unit speed. */
    private final double perMetersPerSecond;

    /**
     * @param distanceAbbreviation abbreviation for distance units.
     * @param perMeter multiply meters by this to convert to this unit.
     * @param shortDistanceAbbreviation abbreviation for short distance units.
     * @param shortDistancePerMeter multiply meters by this to convert to this
     *        short distance unit.
     * @param speedAbbreviation abbreviation for speed units.
     * @param perMetersPerSecond multiply meters-per-second by this to convert
     *        to this unit.
     */
    private DistanceUnits(String distanceAbbreviation, double perMeter,
        String shortDistanceAbbreviation, double shortDistancePerMeter, String speedAbbreviation,
        double perMetersPerSecond) {
      this.distanceAbbreviation = distanceAbbreviation;
      this.perMeter = perMeter;
      this.shortDistanceAbbreviation = shortDistanceAbbreviation;
      this.shortDistancePerMeter = shortDistancePerMeter;
      this.speedAbbreviation = speedAbbreviation;
      this.perMetersPerSecond = perMetersPerSecond;
    }

    /**
     * Returns {@code meters} converted to this unit's distance.
     */
    public double getDistance(double meters) {
      return meters * perMeter;
    }

    /**
     * Returns {@code distanceUnits} in meters.
     */
    public double getMeters(double distanceUnits) {
      return distanceUnits / perMeter;
    }

    /**
     * Returns {@code meters} converted to this unit's short distance.
     */
    public double getShortDistance(double meters) {
      return meters * shortDistancePerMeter;
    }

    /**
     * Returns {@code shortDistanceUnits} in meters.
     */
    public double getMetersFromShortDistance(double shortDistanceUnits) {
      return shortDistanceUnits / shortDistancePerMeter;
    }

    /**
     * Returns {@code metersPerSecond} converted to this unit's speed.
     */
    public double getSpeed(double metersPerSecond) {
      return metersPerSecond * perMetersPerSecond;
    }
  }

  /**
   * Normalizes {@code bearing} to be in the range [0-360). Some Android SDK
   * methods return negative bearings for what's normally 180-359 degrees.
   * 
   * @param bearing bearing in degrees.
   */
  public static double normalizeBearing(double bearing) {
    while (bearing >= 360) {
      bearing -= 360;
    }
    while (bearing < 0) {
      bearing += 360;
    }
    return bearing;
  }

  /**
   * Returns the distance in meters between {@code point1} and {@code point2}.
   * 
   * @see NavigationUtil#computeDistanceUsingRadians
   */
  public static double computeDistance(LatLng point1, LatLng point2) {
    final double lat1 = point1.latRad();
    final double lng1 = point1.lngRad();
    final double lat2 = point2.latRad();
    final double lng2 = point2.lngRad();

    return computeDistanceUsingRadians(lat1, lng1, lat2, lng2);
  }

  /**
   * Returns the distance in meters between ({@code lat1E6}, {@code lng1E6}) and
   * ({@code lat2E6}, {@code lng2E6}).
   * 
   * @see NavigationUtil#computeDistanceUsingRadians
   */
  public static double computeDistance(int lat1E6, int lng1E6, int lat2E6, int lng2E6) {
    final double lat1 = Math.toRadians(lat1E6 * 1e-6);
    final double lng1 = Math.toRadians(lng1E6 * 1e-6);
    final double lat2 = Math.toRadians(lat2E6 * 1e-6);
    final double lng2 = Math.toRadians(lng2E6 * 1e-6);

    return computeDistanceUsingRadians(lat1, lng1, lat2, lng2);
  }

  /**
   * Returns the distance in meters between {@code point1} and ({@code lat2E6},
   * {@code lng2E6}).
   * 
   * @see NavigationUtil#computeDistanceUsingRadians
   */
  public static double computeDistance(LatLng point1, int lat2E6, int lng2E6) {
    return computeDistance(point1.lat, point1.lng, lat2E6, lng2E6);
  }

  /**
   * Returns the distance in meters between ({@code lat1Degrees}, {@code
   * lng1Degrees}) and ( {@code lat2Degrees}, {@code lng2Degrees}).
   * 
   * @see NavigationUtil#computeDistanceUsingRadians
   */
  public static double computeDistance(double lat1Degrees, double lng1Degrees, double lat2Degrees,
      double lng2Degrees) {
    final double lat1 = Math.toRadians(lat1Degrees);
    final double lng1 = Math.toRadians(lng1Degrees);
    final double lat2 = Math.toRadians(lat2Degrees);
    final double lng2 = Math.toRadians(lng2Degrees);

    return computeDistanceUsingRadians(lat1, lng1, lat2, lng2);
  }

  /**
   * Returns the distance in meters between ({@code lat1}, {@code lng1}) and (
   * {@code lat2}, {@code lng2}).
   * <p>
   * Calculation is done by the Haversine Formula.
   * 
   * @param lat1Radians Latitude of first point, in radians
   * @param lng1Radians Longitude of first point, in radians
   * @param lat2Radians Latitude of second point, in radians
   * @param lng2Radians Longitude of second point, in radians
   * 
   * @see <a href="http://en.wikipedia.org/wiki/Haversine_formula"
   *      target="_parent">Haversine formula</a>
   */
  public static double computeDistanceUsingRadians(double lat1Radians, double lng1Radians,
      double lat2Radians, double lng2Radians) {
    final double dLat = lat2Radians - lat1Radians;
    final double dLng = lng2Radians - lng1Radians;
    final double a =
        haversin(dLat) + Math.cos(lat1Radians) * Math.cos(lat2Radians) * haversin(dLng);
    return EARTH_RADIUS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  /**
   * Computes the magnetic variation at a given position.
   * <p>
   * Note: typically the magnetic variation is only applied to the numeric
   * display of a bearing or track. It <b>should not</b> be applied to drawing
   * the map, vectors, etc.
   * 
   * @param position Latitude and longitude
   * @param height Height (meters)
   * 
   * @return Magnetic variation (degrees). West positive, East negative.
   */
  public static double getMagneticVariation(final LatLng position, final double height) {
    final double latRad = position.latRad();
    final double lngRad = position.lngRad();
    final double heightKm = height / 1000.0;

    final double varRad = MagField.GetMagVar(latRad, lngRad, heightKm, SESSION_TIME_MILLIS, null);
    return Math.toDegrees(varRad);
  }


  /**
   * Returns the trigonometric haversine of an angle.
   * <p>
   * 
   * @param x Angle, in radians
   * @return <pre>
   * haversin(x) = (1-cos(x))/2 = sin^2(x/2)
   * </pre>
   * 
   * @see <a href="http://en.wikipedia.org/wiki/Versine"
   *      target="_parent">Versine function</a>
   */
  private static double haversin(final double x) {
    return Math.pow(Math.sin(x / 2), 2);
  }

  /**
   * Returns initial course from {@code start} to {@code end} on a great circle.
   * 
   * @param start Starting point
   * @param end End point
   * @return Initial course, in clockwise degrees from North.
   */
  public static double getInitialCourse(final LatLng start, final LatLng end) {
    final double lat1 = start.latRad();
    final double lng1 = start.lngRad();
    final double lat2 = end.latRad();
    final double lng2 = end.lngRad();
    final double dLng = lng2 - lng1;
    final double lat1Cos = Math.cos(lat1);
    final double lat1Sin = Math.sin(lat1);
    final double lat2Cos = Math.cos(lat2);
    final double lat2Sin = Math.sin(lat2);
    final double dLngCos = Math.cos(dLng);
    final double dLngSin = Math.sin(dLng);

    final double y = dLngSin * lat2Cos;
    final double x = lat1Cos * lat2Sin - lat1Sin * lat2Cos * dLngCos;
    final double C = Math.toDegrees(Math.atan2(y, x));
    return euclideanMod(C, 360.0);
  }

  /**
   * Returns Euclidean modulo of {@code x}, {@code y}.
   * 
   * @see <a href="http://portal.acm.org/citation.cfm?id=128862"
   *      target="_parent"> The Euclidean definition of the functions div and
   *      mod</a>
   */
  public static double euclideanMod(final double x, final double y) {
    final double mod = x % y;
    return (mod < 0) ? mod + y : mod;
  }

  /**
   * Returns Euclidean modulo of {@code x}, {@code y}.
   * 
   * @see #euclideanMod(double, double)
   */
  public static float euclideanMod(final float x, final float y) {
    final float mod = x % y;
    return (mod < 0) ? mod + y : mod;
  }

  /**
   * Returns point along given radial and distance from {@code o}.
   * 
   * @param o Starting point
   * @param course Inital course, in degrees clockwise from True North.
   * @param d Distance along radial, in meters.
   * @return Point on radial {@code course} and distance {@code d} from {@code
   *         o}.
   * 
   * */
  public static LatLng getPointAlongRadial(final LatLng o, final double course, final double d) {
    final double crsRad = Math.toRadians(course);
    final double crsRadCos = Math.cos(crsRad);
    final double crsRadSin = Math.sin(crsRad);

    final double dRad = d / EARTH_RADIUS;
    final double dRadCos = Math.cos(dRad);
    final double dRadSin = Math.sin(dRad);

    final double lat1 = o.latRad();
    final double lng1 = o.lngRad();
    final double lat1Cos = Math.cos(lat1);
    final double lat1Sin = Math.sin(lat1);

    final double lat = Math.asin(lat1Sin * dRadCos + lat1Cos * dRadSin * crsRadCos);
    final double latSin = Math.sin(lat);
    final double dLngY = crsRadSin * dRadSin * lat1Cos;
    final double dLngX = dRadCos - lat1Sin * latSin;
    final double dLng = Math.atan2(dLngY, dLngX);
    final double lng = euclideanMod(lng1 + dLng + Math.PI, 2 * Math.PI) - Math.PI;
    final LatLng destination = LatLng.fromRadians(lat, lng);
    return destination;
  }

  /**
   * Returns a string in the form hours:minutes:seconds for {@code
   * timeInSeconds}. If hours is 0, it will be omitted.
   */
  public static String getHoursMinutesSeconds(float timeInSeconds) {
    int hours = (int) (timeInSeconds / 60 / 60);
    int minutes = (int) (timeInSeconds / 60) - (hours * 60);
    int seconds = (int) (timeInSeconds - (hours * 60 * 60) - (minutes * 60));

    if (hours == 0) {
      return String.format("%d:%02d", minutes, seconds);
    } else {
      return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
  }
}
