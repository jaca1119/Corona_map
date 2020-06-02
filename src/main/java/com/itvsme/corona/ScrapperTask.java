package com.itvsme.corona;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScrapperTask
{
    @Value("${pol.content}")
    private String polContent;
    @Value("${confirmed.global}")
    private String confirmedGlobal;
    @Value("${deaths.global}")
    private String deathsGlobal;
    @Value("${recovered.global}")
    private String recoveredGlobal;

    private List<StateInfo> stateInfos = new ArrayList<>();
    private List<MarkerPoint> worldInfo = new ArrayList<>();

    private int polishRecovered;
    private String lastUpdate;
    private String lastWorldUpdate;

    private int wholeWorldInfected;
    private int wholeWorldRecovered;
    private int wholeWorldDeaths;

    private StateInfoRepo stateInfoRepo;

    public ScrapperTask(StateInfoRepo stateInfoRepo)
    {
        this.stateInfoRepo = stateInfoRepo;
    }

    @Scheduled(fixedDelay = 8 * 60 * 60 * 1000)
    public void downloadInfo() throws IOException
    {
        Document doc = Jsoup.connect(polContent).get();

        Elements elements = doc.select("#registerData");

        List<StateInfo> data = new ArrayList<>();

        elements.forEach(element ->
        {
            ObjectMapper objectMapper = new ObjectMapper();
            try
            {
                JsonNode jsonNode = objectMapper.readTree(element.text());
                JsonNode parsedData = jsonNode.get("parsedData");

                StateInfo[] stateInfos = objectMapper.readValue(parsedData.asText(), StateInfo[].class);

                data.addAll(Arrays.asList(stateInfos));
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        this.stateInfos = data;

        lastUpdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        stateInfoRepo.saveAll(getStateInfos());
    }

    @Scheduled(fixedDelay = 8 * 60 * 60 * 1000)
    public void downloadWholeWorldInfo() throws IOException
    {
        CSVParser parseCountries = downloadAndGetParsedData(confirmedGlobal);
        CSVParser parseInfected = downloadAndGetParsedData(confirmedGlobal);
        CSVParser parseDeaths = downloadAndGetParsedData(deathsGlobal);
        CSVParser parseRecovered = downloadAndGetParsedData(recoveredGlobal);

        List<Country> countries = getCountries(parseCountries);
        Map<Integer, String[]> confirmedInfected = getConfirmedInfected(parseInfected);
        Map<Integer, String[]> confirmedDeaths = getConfirmedDeaths(parseDeaths);
        Map<Integer, String[]> confirmedRecovered = getConfirmedRecovered(parseRecovered);

        List<MarkerPoint> markerPoints = new ArrayList<>();

        confirmedRecovered.forEach((id, provinceAndRecovered) ->
        {
            MarkerPoint markerPoint = new MarkerPoint(getCountryFromList(countries, provinceAndRecovered[0]),
                    getIntValueFromMap(confirmedInfected, provinceAndRecovered[0]),
                    getIntValueFromMap(confirmedDeaths, provinceAndRecovered[0]),
                    Integer.parseInt(provinceAndRecovered[1]));

            markerPoints.add(markerPoint);
        });

        this.worldInfo = markerPoints;
    }

    private Country getCountryFromList(List<Country> countries, String searchedCountryName)
    {
        int i = countries.indexOf(new Country(searchedCountryName));
        Country country = countries.get(i);
        countries.remove(i);
        return country;
    }

    private int getIntValueFromMap(Map<Integer, String[]> map, String searchedValue)
    {
        int value = 0;
        for (Iterator<Map.Entry<Integer, String[]>> iterator = map.entrySet().iterator(); iterator.hasNext(); )
        {
            Map.Entry<Integer, String[]> entry = iterator.next();
            String[] provinceAndRecovered = entry.getValue();
            if (provinceAndRecovered[0].equals(searchedValue))
            {
                value = Integer.parseInt(provinceAndRecovered[1]);
                iterator.remove();
                break;
            }
        }

        return value;
    }

    private Map<Integer, String[]> getConfirmedRecovered(CSVParser parseRecovered)
    {
        Map<Integer, String[]> recovered = getLatestRows(parseRecovered);

        recovered.forEach((id, provinceAndRecovered) ->
        {
            if (provinceAndRecovered[0].equals("Poland"))
            {
                polishRecovered = Integer.parseInt(provinceAndRecovered[1]);
            }
        });

        calculateWholeWorldRecovered(getIntegerValues(recovered));

        return recovered;
    }

    private Map<Integer, String[]> getConfirmedDeaths(CSVParser parseDeaths)
    {
        Map<Integer, String[]> latestRowsDeaths = getLatestRows(parseDeaths);

        calculateWholeWorldDeaths(getIntegerValues(latestRowsDeaths));

        return latestRowsDeaths;
    }

    private void calculateWholeWorldDeaths(Collection<Integer> deathsInEachCountry)
    {
        wholeWorldDeaths = deathsInEachCountry.stream().mapToInt(deathsInCountry -> deathsInCountry).sum();
    }

    private void calculateWholeWorldRecovered(Collection<Integer> recovered)
    {
        wholeWorldRecovered = recovered.stream().mapToInt(recoveredInCountry -> recoveredInCountry).sum();
    }

    private Map<Integer, String[]> getConfirmedInfected(CSVParser parseInfected)
    {
        Map<Integer, String[]> latestRowsInfected = getLatestRows(parseInfected);

        calculateWholeWorldInfected(getIntegerValues(latestRowsInfected));

        return latestRowsInfected;
    }

    private Collection<Integer> getIntegerValues(Map<Integer, String[]> globalValues)
    {
        return globalValues.values().stream().map(provinceAndValue -> Integer.valueOf(provinceAndValue[1])).collect(Collectors.toCollection(ArrayList::new));
    }

    private void calculateWholeWorldInfected(Collection<Integer> infected)
    {
        wholeWorldInfected = infected.stream().mapToInt(infectedInCountry -> infectedInCountry).sum();
    }

    private List<Country> getCountries(CSVParser parser)
    {
        List<Country> countries = new ArrayList<>();

        for (CSVRecord row : parser)
        {
            String countryOrRegion = row.get("Country/Region");

            double latOfCountry = Double.parseDouble(row.get("Lat"));
            double lngOfCountry = Double.parseDouble(row.get("Long"));

            Country country = new Country(latOfCountry, lngOfCountry, countryOrRegion);
            countries.add(country);
        }

        return countries;
    }

    private CSVParser downloadAndGetParsedData(String URL) throws IOException
    {
        RestTemplate template = new RestTemplate();
        String csvRaw = template.getForObject(URL, String.class);

        StringReader reader = new StringReader(csvRaw);

        return CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
    }

    private Map<Integer, String[]> getLatestRows(CSVParser parser)
    {
        Map<Integer, String[]> latestRowValues = new HashMap<>();

        String updateDay = null;
        String rowValue;

        int i = 0;
        for (CSVRecord row : parser)
        {
            try
            {
                updateDay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("M/d/yy"));
                rowValue = row.get(updateDay);
            } catch (IllegalArgumentException e)
            {
                updateDay = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("M/d/yy"));
                rowValue = row.get(updateDay);
            }

            latestRowValues.put(i, new String[]{row.get("Country/Region"), rowValue});

            ++i;
        }

        if (this.lastWorldUpdate == null)
        {
            try
            {
                Date worldUpdate = new SimpleDateFormat("M/d/yy").parse(updateDay);
                this.lastWorldUpdate = new SimpleDateFormat("dd-MM-yyyy").format(worldUpdate);
            } catch (ParseException e)
            {
                e.printStackTrace();
            }
        }

        return latestRowValues;
    }

    public List<StateInfo> getStateInfos()
    {
        return stateInfos;
    }

    public List<MarkerPoint> getWorldInfo()
    {
        return worldInfo;
    }

    public String getLastUpdate()
    {
        return lastUpdate;
    }

    public int getPolishRecoveredInfo()
    {
        return polishRecovered;
    }

    public int getWholeWorldInfected()
    {
        return wholeWorldInfected;
    }

    public int getWholeWorldRecovered()
    {
        return wholeWorldRecovered;
    }

    public int getWholeWorldDeaths()
    {
        return wholeWorldDeaths;
    }

    public String getLastWorldUpdate()
    {
        return lastWorldUpdate;
    }
}