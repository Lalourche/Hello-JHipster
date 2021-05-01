package fr.lalourche.hellojhipster.steps.cucumber;

import fr.lalourche.hellojhipster.steps.TechniquesApp;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

@CucumberContextConfiguration
@SpringBootTest(classes = TechniquesApp.class)
@WebAppConfiguration
public class CucumberTestContextConfiguration {}
