package fr.lalourche.hellojhipster.recipes;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchTest {

    @Test
    void servicesAndRepositoriesShouldNotDependOnWebLayer() {
        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("fr.lalourche.hellojhipster.recipes");

        noClasses()
            .that()
            .resideInAnyPackage("fr.lalourche.hellojhipster.recipes.service..")
            .or()
            .resideInAnyPackage("fr.lalourche.hellojhipster.recipes.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..fr.lalourche.hellojhipster.recipes.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses);
    }
}
