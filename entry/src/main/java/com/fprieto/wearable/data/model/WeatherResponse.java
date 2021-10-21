package com.fprieto.wearable.data.model;

/**
 * [{"id":56,"type":"programming","setup":"How do you check if a webpage is HTML5?","punchline":"Try it out on Internet Explorer"}]
 */
public class WeatherResponse {

    private int id;
    private String name;
    private String setup;
    private Weather[] weather;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSetup() {
        return setup;
    }

    public void setSetup(String setup) {
        this.setup = setup;
    }

    public Weather[] getWeather() {
        return weather;
    }

    public void setWeather(Weather[] weathers) {
        this.weather = weathers;
    }

}
