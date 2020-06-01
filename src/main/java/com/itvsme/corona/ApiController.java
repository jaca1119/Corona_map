package com.itvsme.corona;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class ApiController
{

    private ScrapperTask scrapperTask;

    public ApiController(ScrapperTask scrapperTask)
    {
        this.scrapperTask = scrapperTask;
    }

    @GetMapping
    public String getData(Model model) throws IOException
    {
        model.addAttribute("data", scrapperTask.getStateInfos());
        model.addAttribute("update", scrapperTask.getLastUpdate());
        model.addAttribute("worldUpdate", scrapperTask.getLastWorldUpdate());
        model.addAttribute("points", scrapperTask.getWorldInfo());
        model.addAttribute("recovered", scrapperTask.getPolishRecoveredInfo());
        model.addAttribute("worldInfected", scrapperTask.getWholeWorldInfected());
        model.addAttribute("worldRecovered", scrapperTask.getWholeWorldRecovered());
        model.addAttribute("worldDeaths", scrapperTask.getWholeWorldDeaths());

        return "index";
    }
}
