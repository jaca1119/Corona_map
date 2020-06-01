package com.itvsme.corona;

import lombok.Getter;

@Getter
public class MarkerPoint
{
    private Country country;
    private int confirmedInfected;
    private int confirmedDeaths;
    private int confirmedRecovered;

    public MarkerPoint(Country country, int confirmedInfected, int confirmedDeaths, int confirmedRecovered)
    {
        this.country = country;
        this.confirmedInfected = confirmedInfected;
        this.confirmedDeaths = confirmedDeaths;
        this.confirmedRecovered = confirmedRecovered;
    }

    @Override
    public String toString()
    {
        return "MarkerPoint{" +
                "country=" + country +
                ", confirmedInfected=" + confirmedInfected +
                ", confirmedDeaths=" + confirmedDeaths +
                ", confirmedRecovered=" + confirmedRecovered +
                '}';
    }
}
