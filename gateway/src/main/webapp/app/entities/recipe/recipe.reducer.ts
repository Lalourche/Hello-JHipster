import axios from 'axios';
import { ICrudSearchAction, ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IRecipe, defaultValue } from 'app/shared/model/recipe.model';

export const ACTION_TYPES = {
  SEARCH_RECIPES: 'recipe/SEARCH_RECIPES',
  FETCH_RECIPE_LIST: 'recipe/FETCH_RECIPE_LIST',
  FETCH_RECIPE: 'recipe/FETCH_RECIPE',
  CREATE_RECIPE: 'recipe/CREATE_RECIPE',
  UPDATE_RECIPE: 'recipe/UPDATE_RECIPE',
  PARTIAL_UPDATE_RECIPE: 'recipe/PARTIAL_UPDATE_RECIPE',
  DELETE_RECIPE: 'recipe/DELETE_RECIPE',
  SET_BLOB: 'recipe/SET_BLOB',
  RESET: 'recipe/RESET',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IRecipe>,
  entity: defaultValue,
  updating: false,
  updateSuccess: false,
};

export type RecipeState = Readonly<typeof initialState>;

// Reducer

export default (state: RecipeState = initialState, action): RecipeState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.SEARCH_RECIPES):
    case REQUEST(ACTION_TYPES.FETCH_RECIPE_LIST):
    case REQUEST(ACTION_TYPES.FETCH_RECIPE):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true,
      };
    case REQUEST(ACTION_TYPES.CREATE_RECIPE):
    case REQUEST(ACTION_TYPES.UPDATE_RECIPE):
    case REQUEST(ACTION_TYPES.DELETE_RECIPE):
    case REQUEST(ACTION_TYPES.PARTIAL_UPDATE_RECIPE):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.SEARCH_RECIPES):
    case FAILURE(ACTION_TYPES.FETCH_RECIPE_LIST):
    case FAILURE(ACTION_TYPES.FETCH_RECIPE):
    case FAILURE(ACTION_TYPES.CREATE_RECIPE):
    case FAILURE(ACTION_TYPES.UPDATE_RECIPE):
    case FAILURE(ACTION_TYPES.PARTIAL_UPDATE_RECIPE):
    case FAILURE(ACTION_TYPES.DELETE_RECIPE):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
    case SUCCESS(ACTION_TYPES.SEARCH_RECIPES):
    case SUCCESS(ACTION_TYPES.FETCH_RECIPE_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.FETCH_RECIPE):
      return {
        ...state,
        loading: false,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.CREATE_RECIPE):
    case SUCCESS(ACTION_TYPES.UPDATE_RECIPE):
    case SUCCESS(ACTION_TYPES.PARTIAL_UPDATE_RECIPE):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.DELETE_RECIPE):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: {},
      };
    case ACTION_TYPES.SET_BLOB: {
      const { name, data, contentType } = action.payload;
      return {
        ...state,
        entity: {
          ...state.entity,
          [name]: data,
          [name + 'ContentType']: contentType,
        },
      };
    }
    case ACTION_TYPES.RESET:
      return {
        ...initialState,
      };
    default:
      return state;
  }
};

const apiUrl = 'api/recipes';
const apiSearchUrl = 'api/_search/recipes';

// Actions

export const getSearchEntities: ICrudSearchAction<IRecipe> = (query, page, size, sort) => ({
  type: ACTION_TYPES.SEARCH_RECIPES,
  payload: axios.get<IRecipe>(`${apiSearchUrl}?query=${query}`),
});

export const getEntities: ICrudGetAllAction<IRecipe> = (page, size, sort) => ({
  type: ACTION_TYPES.FETCH_RECIPE_LIST,
  payload: axios.get<IRecipe>(`${apiUrl}?cacheBuster=${new Date().getTime()}`),
});

export const getEntity: ICrudGetAction<IRecipe> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_RECIPE,
    payload: axios.get<IRecipe>(requestUrl),
  };
};

export const createEntity: ICrudPutAction<IRecipe> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_RECIPE,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<IRecipe> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_RECIPE,
    payload: axios.put(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const partialUpdate: ICrudPutAction<IRecipe> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.PARTIAL_UPDATE_RECIPE,
    payload: axios.patch(`${apiUrl}/${entity.id}`, cleanEntity(entity)),
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<IRecipe> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_RECIPE,
    payload: axios.delete(requestUrl),
  });
  dispatch(getEntities());
  return result;
};

export const setBlob = (name, data, contentType?) => ({
  type: ACTION_TYPES.SET_BLOB,
  payload: {
    name,
    data,
    contentType,
  },
});

export const reset = () => ({
  type: ACTION_TYPES.RESET,
});
