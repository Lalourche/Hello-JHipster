import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { setFileData, openFile, byteSize, Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { IIngredient } from 'app/shared/model/ingredient.model';
import { getEntities as getIngredients } from 'app/entities/ingredient/ingredient.reducer';
import { IStep } from 'app/shared/model/step.model';
import { getEntities as getSteps } from 'app/entities/step/step.reducer';
import { getEntity, updateEntity, createEntity, setBlob, reset } from './recipe.reducer';
import { IRecipe } from 'app/shared/model/recipe.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IRecipeUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const RecipeUpdate = (props: IRecipeUpdateProps) => {
  const [idsingredients, setIdsingredients] = useState([]);
  const [idssteps, setIdssteps] = useState([]);
  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const { recipeEntity, ingredients, steps, loading, updating } = props;

  const { picture, pictureContentType } = recipeEntity;

  const handleClose = () => {
    props.history.push('/recipe');
  };

  useEffect(() => {
    if (isNew) {
      props.reset();
    } else {
      props.getEntity(props.match.params.id);
    }

    props.getIngredients();
    props.getSteps();
  }, []);

  const onBlobChange = (isAnImage, name) => event => {
    setFileData(event, (contentType, data) => props.setBlob(name, data, contentType), isAnImage);
  };

  const clearBlob = name => () => {
    props.setBlob(name, undefined, undefined);
  };

  useEffect(() => {
    if (props.updateSuccess) {
      handleClose();
    }
  }, [props.updateSuccess]);

  const saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const entity = {
        ...recipeEntity,
        ...values,
        ingredients: mapIdList(values.ingredients),
        steps: mapIdList(values.steps),
      };

      if (isNew) {
        props.createEntity(entity);
      } else {
        props.updateEntity(entity);
      }
    }
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="gatewayApp.recipe.home.createOrEditLabel" data-cy="RecipeCreateUpdateHeading">
            <Translate contentKey="gatewayApp.recipe.home.createOrEditLabel">Create or edit a Recipe</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : recipeEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="recipe-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="recipe-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="nameLabel" for="recipe-name">
                  <Translate contentKey="gatewayApp.recipe.name">Name</Translate>
                </Label>
                <AvField
                  id="recipe-name"
                  data-cy="name"
                  type="text"
                  name="name"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="cookingLabel" for="recipe-cooking">
                  <Translate contentKey="gatewayApp.recipe.cooking">Cooking</Translate>
                </Label>
                <AvInput
                  id="recipe-cooking"
                  data-cy="cooking"
                  type="select"
                  className="form-control"
                  name="cooking"
                  value={(!isNew && recipeEntity.cooking) || 'WITH_COOKING'}
                >
                  <option value="WITH_COOKING">{translate('gatewayApp.Cooking.WITH_COOKING')}</option>
                  <option value="WITHOUT_COOKING">{translate('gatewayApp.Cooking.WITHOUT_COOKING')}</option>
                </AvInput>
              </AvGroup>
              <AvGroup>
                <Label id="cookingTimeLabel" for="recipe-cookingTime">
                  <Translate contentKey="gatewayApp.recipe.cookingTime">Cooking Time</Translate>
                </Label>
                <AvField
                  id="recipe-cookingTime"
                  data-cy="cookingTime"
                  type="string"
                  className="form-control"
                  name="cookingTime"
                  validate={{
                    min: { value: 0, errorMessage: translate('entity.validation.min', { min: 0 }) },
                    max: { value: 65535, errorMessage: translate('entity.validation.max', { max: 65535 }) },
                    number: { value: true, errorMessage: translate('entity.validation.number') },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <AvGroup>
                  <Label id="pictureLabel" for="picture">
                    <Translate contentKey="gatewayApp.recipe.picture">Picture</Translate>
                  </Label>
                  <br />
                  {picture ? (
                    <div>
                      {pictureContentType ? (
                        <a onClick={openFile(pictureContentType, picture)}>
                          <img src={`data:${pictureContentType};base64,${picture}`} style={{ maxHeight: '100px' }} />
                        </a>
                      ) : null}
                      <br />
                      <Row>
                        <Col md="11">
                          <span>
                            {pictureContentType}, {byteSize(picture)}
                          </span>
                        </Col>
                        <Col md="1">
                          <Button color="danger" onClick={clearBlob('picture')}>
                            <FontAwesomeIcon icon="times-circle" />
                          </Button>
                        </Col>
                      </Row>
                    </div>
                  ) : null}
                  <input id="file_picture" data-cy="picture" type="file" onChange={onBlobChange(true, 'picture')} accept="image/*" />
                  <AvInput type="hidden" name="picture" value={picture} />
                </AvGroup>
              </AvGroup>
              <AvGroup>
                <Label for="recipe-ingredients">
                  <Translate contentKey="gatewayApp.recipe.ingredients">Ingredients</Translate>
                </Label>
                <AvInput
                  id="recipe-ingredients"
                  data-cy="ingredients"
                  type="select"
                  multiple
                  className="form-control"
                  name="ingredients"
                  value={!isNew && recipeEntity.ingredients && recipeEntity.ingredients.map(e => e.id)}
                >
                  <option value="" key="0" />
                  {ingredients
                    ? ingredients.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.id}
                        </option>
                      ))
                    : null}
                </AvInput>
              </AvGroup>
              <AvGroup>
                <Label for="recipe-steps">
                  <Translate contentKey="gatewayApp.recipe.steps">Steps</Translate>
                </Label>
                <AvInput
                  id="recipe-steps"
                  data-cy="steps"
                  type="select"
                  multiple
                  className="form-control"
                  name="steps"
                  value={!isNew && recipeEntity.steps && recipeEntity.steps.map(e => e.id)}
                >
                  <option value="" key="0" />
                  {steps
                    ? steps.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.id}
                        </option>
                      ))
                    : null}
                </AvInput>
              </AvGroup>
              <Button tag={Link} id="cancel-save" to="/recipe" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </AvForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

const mapStateToProps = (storeState: IRootState) => ({
  ingredients: storeState.ingredient.entities,
  steps: storeState.step.entities,
  recipeEntity: storeState.recipe.entity,
  loading: storeState.recipe.loading,
  updating: storeState.recipe.updating,
  updateSuccess: storeState.recipe.updateSuccess,
});

const mapDispatchToProps = {
  getIngredients,
  getSteps,
  getEntity,
  updateEntity,
  setBlob,
  createEntity,
  reset,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(RecipeUpdate);
