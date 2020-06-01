package com.itvsme.corona;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public class StateInfo
{
    @Id
    private String id;
    private LocalDateTime creationTime = LocalDateTime.now();

    @JsonProperty(value = "Województwo")
    private String province;
    @JsonProperty("Powiat/Miasto")
    private String town;
    @JsonProperty("Liczba")
    private String numberOfInfected;
    @JsonProperty(value = "Liczba zgonów")
    private int deaths;
    @JsonProperty("Id")
    private String id_scrap;

    public String getId_scrap()
    {
        return id_scrap;
    }

    public void setId_scrap(String id_scrap)
    {
        this.id_scrap = id_scrap;
    }

    public String getProvince()
    {
        return province;
    }

    public void setProvince(String province)
    {
        this.province = province;
    }

    public String getTown()
    {
        return town;
    }

    public void setTown(String town)
    {
        this.town = town;
    }

    public String getNumberOfInfected()
    {
        return numberOfInfected;
    }

    public void setNumberOfInfected(String numberOfInfected)
    {
        this.numberOfInfected = numberOfInfected;
    }

    public int getDeaths()
    {
        return deaths;
    }

    public void setDeaths(int deaths)
    {
        this.deaths = deaths;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String toString()
    {
        return "StateInfo{" +
                "id='" + id + '\'' +
                ", province='" + province + '\'' +
                ", town='" + town + '\'' +
                ", numberOfInfected='" + numberOfInfected + '\'' +
                ", deaths=" + deaths +
                ", id_scrap='" + id_scrap + '\'' +
                '}';
    }
}
