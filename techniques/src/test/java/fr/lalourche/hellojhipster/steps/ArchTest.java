package fr.lalourche.hellojhipster.steps;

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
            .importPackages("fr.lalourche.hellojhipster.steps");

        noClasses()
            .that()
            .resideInAnyPackage("fr.lalourche.hellojhipster.steps.service..")
            .or()
            .resideInAnyPackage("fr.lalourche.hellojhipster.steps.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..fr.lalourche.hellojhipster.steps.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses);
    }
}
