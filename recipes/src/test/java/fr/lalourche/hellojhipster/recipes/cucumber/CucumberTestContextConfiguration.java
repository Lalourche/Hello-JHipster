package fr.lalourche.hellojhipster.recipes.cucumber;

import fr.lalourche.hellojhipster.recipes.RecipesApp;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

@CucumberContextConfiguration
@SpringBootTest(classes = RecipesApp.class)
@WebAppConfiguration
public class CucumberTestContextConfiguration {}
