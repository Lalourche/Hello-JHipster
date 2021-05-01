package fr.lalourche.hellojhipster.steps.repository;

import fr.lalourche.hellojhipster.steps.domain.Technique;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Technique entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TechniqueRepository extends MongoRepository<Technique, String> {}
