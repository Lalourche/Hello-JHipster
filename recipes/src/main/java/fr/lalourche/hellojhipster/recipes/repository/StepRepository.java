package fr.lalourche.hellojhipster.recipes.repository;

import fr.lalourche.hellojhipster.recipes.domain.Step;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Step entity.
 */
@SuppressWarnings("unused")
@Repository
public interface StepRepository extends JpaRepository<Step, Long> {}
