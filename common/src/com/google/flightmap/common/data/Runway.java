/*
 * Copyright (C) 2009 Google Inc.
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

package com.google.flightmap.common.data;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
/**
 * Runway data structure.
 */
public class Runway implements Comparable<Runway> {
  private static final List<String> HARD_SURFACE_PREFIXES;

  static {
    HARD_SURFACE_PREFIXES = new LinkedList<String>();
    HARD_SURFACE_PREFIXES.add("ASPH");
    HARD_SURFACE_PREFIXES.add("CONC");
    HARD_SURFACE_PREFIXES.add("PEM");
  }

  public final int airportId;

  public final String letters;

  public final int length;

  public final int width;

  public final String surface;

  public final SortedSet<RunwayEnd> runwayEnds;

  public Runway(final int airportId,
                final String letters,
                final int length,
                final int width,
                final String surface,
                final SortedSet<RunwayEnd> runwayEnds) {
    this.airportId = airportId;
    this.letters = letters;
    this.length = length;
    this.width = width;
    this.surface = surface;
    this.runwayEnds = runwayEnds;
  }

  /**
   * Returns whether this surface is hard.
   */
  public boolean isHardSurface() {
    for (String hardSurfacePrefix: HARD_SURFACE_PREFIXES) {
      if (surface.startsWith(hardSurfacePrefix)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("%s (%dx%d)", letters, this.length, this.width);
  }

  @Override
  public int compareTo(Runway o) {
    if (airportId != o.airportId) {
      throw new IllegalArgumentException("Cannot compare runways from different airports");
    }

    final int lengthDiff = length - o.length;
    if (lengthDiff != 0) {
      return lengthDiff;
    }
    final int widthDiff = width - o.width;
    if (widthDiff != 0) {
      return widthDiff;
    }
    final int lettersDiff = letters.compareTo(o.letters);
    return lettersDiff;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if ( !(obj instanceof Runway)) return false;

    Runway other = (Runway)obj;
    return (airportId == other.airportId) && letters.equals(other.letters);
  }

  @Override
  public int hashCode() {
    return this.letters.hashCode();
  }
}
