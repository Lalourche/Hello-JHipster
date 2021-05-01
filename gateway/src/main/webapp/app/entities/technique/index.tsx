import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import Technique from './technique';
import TechniqueDetail from './technique-detail';
import TechniqueUpdate from './technique-update';
import TechniqueDeleteDialog from './technique-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={TechniqueUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={TechniqueUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={TechniqueDetail} />
      <ErrorBoundaryRoute path={match.url} component={Technique} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={TechniqueDeleteDialog} />
  </>
);

export default Routes;
