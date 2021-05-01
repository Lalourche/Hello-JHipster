package fr.lalourche.hellojhipster.gateway;

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
            .importPackages("fr.lalourche.hellojhipster.gateway");

        noClasses()
            .that()
            .resideInAnyPackage("fr.lalourche.hellojhipster.gateway.service..")
            .or()
            .resideInAnyPackage("fr.lalourche.hellojhipster.gateway.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..fr.lalourche.hellojhipster.gateway.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses);
    }
}
