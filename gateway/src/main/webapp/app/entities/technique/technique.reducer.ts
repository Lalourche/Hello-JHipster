import axios from 'axios';
import { ICrudSearchAction, ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { ITechnique, defaultValue } from 'app/shared/model/technique.model';

export const ACTION_TYPES = {
  SEARCH_TECHNIQUES: 'technique/SEARCH_TECHNIQUES',
  FETCH_TECHNIQUE_LIST: 'technique/FETCH_TECHNIQUE_LIST',
  FETCH_TECHNIQUE: 'technique/FETCH_TECHNIQUE',
  CREATE_TECHNIQUE: 'technique/CREATE_TECHNIQUE',
  UPDATE_TECHNIQUE: 'technique/UPDATE_TECHNIQUE',
  PARTIAL_UPDATE_TECHNIQUE: 'technique/PARTIAL_UPDATE_TECHNIQUE',
  DELETE_TECHNIQUE: 'technique/DELETE_TECHNIQUE',
  RESET: 'technique/RESET',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<ITechnique>,
  entity: defaultValue,
  updating: false,
  updateSuccess: false,
};

export type TechniqueState = Readonly<typeof initialState>;

// Reducer

export default (state: TechniqueState = initialState, action): TechniqueState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.SEARCH_TECHNIQUES):
    case REQUEST(ACTION_TYPES.FETCH_TECHNIQUE_LIST):
    case REQUEST(ACTION_TYPES.FETCH_TECHNIQUE):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true,
      };
    case REQUEST(ACTION_TYPES.CREATE_TECHNIQUE):
    case REQUEST(ACTION_TYPES.UPDATE_TECHNIQUE):
    case REQUEST(ACTION_TYPES.DELETE_TECHNIQUE):
    case REQUEST(ACTION_TYPES.PARTIAL_UPDATE_TECHNIQUE):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.SEARCH_TECHNIQUES):
    case FAILURE(ACTION_TYPES.FETCH_TECHNIQUE_LIST):
    case FAILURE(ACTION_TYPES.FETCH_TECHNIQUE):
    case FAILURE(ACTION_TYPES.CREATE_TECHNIQUE):
    case FAILURE(ACTION_TYPES.UPDATE_TECHNIQUE):
    case FAILURE(ACTION_TYPES.PARTIAL_UPDATE_TECHNIQUE):
    case FAILURE(ACTION_TYPES.DELETE_TECHNIQUE):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
    case SUCCESS(ACTION_TYPES.SEARCH_TECHNIQUES):
    case SUCCESS(ACTION_TYPES.FETCH_TECHNIQUE_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.FETCH_TECHNIQUE):
      return {
        ...state,
        loading: false,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.CREATE_TECHNIQUE):
    case SUCCESS(ACTION_TYPES.UPDATE_TECHNIQUE):
    case SUCCESS(ACTION_TYPES.PARTIAL_UPDATE_TECHNIQUE):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.DELETE_TECHNIQUE):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: {},
      };
    case ACTION_TYPES.RESET:
      return {
        ...initialState,
      };
    default:
      return state;
  }
};

const apiUrl = 'api/techniques';
const apiSearchUrl = 'api/_search/techniques';

// Actions

export const getSearchEntities: ICrudSearchAction<ITechnique> = (query, page, size, sort) => ({
  type: ACTION_TYPES.SEARCH_TECHNIQUES,
  payload: axios.get<ITechnique>(`${apiSearchUrl}?query=${query}`),
});

export const getEntities: ICrudGetAllAction<ITechnique> = (page, size, sort) => ({
  type: ACTION_TYPES.FETCH_TECHNIQUE_LIST,
  payload: axios.get<ITechnique>(`${apiUrl}?cacheBuster=${new Date().getTime()}`),
});

export const getEntity: ICrudGetAction<ITechnique> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_TECHNIQUE,
    payload: axios.get<ITechnique>(requestUrl),
  };
};

export const createEntity: ICrudPutAction<ITechnique> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_TECHNIQUE,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<ITechnique> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_TECHNIQUE,
    payload: axios.put(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const partialUpdate: ICrudPutAction<ITechnique> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.PARTIAL_UPDATE_TECHNIQUE,
    payload: axios.patch(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<ITechnique> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_TECHNIQUE,
    payload: axios.delete(requestUrl),
  });
  dispatch(getEntities());
  return result;
};

export const reset = () => ({
  type: ACTION_TYPES.RESET,
});
