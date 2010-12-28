package org.onebusaway.king_county_metro_gtfs.service_alerts.snow.model;

import org.onebusaway.utility.text.NaturalStringOrder;

public class ActiveRerouteRef implements Comparable<ActiveRerouteRef> {
  private final String routeId;
  private final String region;

  public ActiveRerouteRef(String routeId, String region) {
    this.routeId = routeId;
    this.region = region;
  }

  public String getRouteId() {
    return routeId;
  }

  public String getRegion() {
    return region;
  }

  @Override
  public String toString() {
    return "routeId=" + routeId + " region=" + region;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((region == null) ? 0 : region.hashCode());
    result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ActiveRerouteRef other = (ActiveRerouteRef) obj;
    if (region == null) {
      if (other.region != null)
        return false;
    } else if (!region.equals(other.region))
      return false;
    if (routeId == null) {
      if (other.routeId != null)
        return false;
    } else if (!routeId.equals(other.routeId))
      return false;
    return true;
  }

  @Override
  public int compareTo(ActiveRerouteRef o) {
    int rc = NaturalStringOrder.compareNatural(this.routeId, o.routeId);
    if (rc != 0)
      return rc;

    return this.region.compareTo(o.region);
  }
}