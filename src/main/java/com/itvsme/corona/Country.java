package com.itvsme.corona;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Country
{
    private double lat;
    private double lng;
    private String name;

    public Country(double lat, double lng, String name)
    {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public Country(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "Country{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return name.equals(country.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
