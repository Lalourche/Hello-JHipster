import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, openFile, byteSize } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './recipe.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IRecipeDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const RecipeDetail = (props: IRecipeDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { recipeEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="recipeDetailsHeading">
          <Translate contentKey="gatewayApp.recipe.detail.title">Recipe</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{recipeEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="gatewayApp.recipe.name">Name</Translate>
            </span>
          </dt>
          <dd>{recipeEntity.name}</dd>
          <dt>
            <span id="cooking">
              <Translate contentKey="gatewayApp.recipe.cooking">Cooking</Translate>
            </span>
          </dt>
          <dd>{recipeEntity.cooking}</dd>
          <dt>
            <span id="cookingTime">
              <Translate contentKey="gatewayApp.recipe.cookingTime">Cooking Time</Translate>
            </span>
          </dt>
          <dd>{recipeEntity.cookingTime}</dd>
          <dt>
            <span id="picture">
              <Translate contentKey="gatewayApp.recipe.picture">Picture</Translate>
            </span>
          </dt>
          <dd>
            {recipeEntity.picture ? (
              <div>
                {recipeEntity.pictureContentType ? (
                  <a onClick={openFile(recipeEntity.pictureContentType, recipeEntity.picture)}>
                    <img src={`data:${recipeEntity.pictureContentType};base64,${recipeEntity.picture}`} style={{ maxHeight: '30px' }} />
                  </a>
                ) : null}
                <span>
                  {recipeEntity.pictureContentType}, {byteSize(recipeEntity.picture)}
                </span>
              </div>
            ) : null}
          </dd>
          <dt>
            <Translate contentKey="gatewayApp.recipe.ingredients">Ingredients</Translate>
          </dt>
          <dd>
            {recipeEntity.ingredients
              ? recipeEntity.ingredients.map((val, i) => (
                  <span key={val.id}>
                    <a>{val.id}</a>
                    {recipeEntity.ingredients && i === recipeEntity.ingredients.length - 1 ? '' : ', '}
                  </span>
                ))
              : null}
          </dd>
          <dt>
            <Translate contentKey="gatewayApp.recipe.steps">Steps</Translate>
          </dt>
          <dd>
            {recipeEntity.steps
              ? recipeEntity.steps.map((val, i) => (
                  <span key={val.id}>
                    <a>{val.id}</a>
                    {recipeEntity.steps && i === recipeEntity.steps.length - 1 ? '' : ', '}
                  </span>
                ))
              : null}
          </dd>
        </dl>
        <Button tag={Link} to="/recipe" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/recipe/${recipeEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ recipe }: IRootState) => ({
  recipeEntity: recipe.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(RecipeDetail);
